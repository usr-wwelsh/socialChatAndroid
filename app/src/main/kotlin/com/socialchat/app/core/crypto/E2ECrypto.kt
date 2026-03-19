package com.socialchat.app.core.crypto

import android.util.Base64
import org.json.JSONObject
import java.math.BigInteger
import java.security.*
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.spec.*
import javax.crypto.*
import javax.crypto.spec.*

/**
 * ECDH P-256 + AES-GCM end-to-end encryption, compatible with the web client's
 * Web Crypto API implementation (same key format, same algorithms).
 */
object E2ECrypto {

    // ---------------------------------------------------------------------------
    // Key generation
    // ---------------------------------------------------------------------------

    fun generateKeyPair(): KeyPair {
        val gen = KeyPairGenerator.getInstance("EC")
        gen.initialize(ECGenParameterSpec("secp256r1"))
        return gen.generateKeyPair()
    }

    // ---------------------------------------------------------------------------
    // JWK export
    // ---------------------------------------------------------------------------

    fun exportPublicKeyJwk(publicKey: ECPublicKey): String {
        val x = base64Url(padTo32(publicKey.w.affineX.toByteArray()))
        val y = base64Url(padTo32(publicKey.w.affineY.toByteArray()))
        return JSONObject().apply {
            put("kty", "EC")
            put("crv", "P-256")
            put("x", x)
            put("y", y)
        }.toString()
    }

    fun exportPrivateKeyJwk(keyPair: KeyPair): String {
        val pub = keyPair.public as ECPublicKey
        val priv = keyPair.private as ECPrivateKey
        val x = base64Url(padTo32(pub.w.affineX.toByteArray()))
        val y = base64Url(padTo32(pub.w.affineY.toByteArray()))
        val d = base64Url(padTo32(priv.s.toByteArray()))
        return JSONObject().apply {
            put("kty", "EC")
            put("crv", "P-256")
            put("x", x)
            put("y", y)
            put("d", d)
        }.toString()
    }

    // ---------------------------------------------------------------------------
    // JWK import
    // ---------------------------------------------------------------------------

    fun importPublicKeyJwk(jwkString: String): ECPublicKey {
        val jwk = JSONObject(jwkString)
        val x = BigInteger(1, fromBase64Url(jwk.getString("x")))
        val y = BigInteger(1, fromBase64Url(jwk.getString("y")))
        val ecSpec = getEcSpec()
        val point = ECPoint(x, y)
        val keySpec = ECPublicKeySpec(point, ecSpec)
        return KeyFactory.getInstance("EC").generatePublic(keySpec) as ECPublicKey
    }

    fun importPrivateKeyJwk(jwkString: String): ECPrivateKey {
        val jwk = JSONObject(jwkString)
        val d = BigInteger(1, fromBase64Url(jwk.getString("d")))
        val ecSpec = getEcSpec()
        val keySpec = ECPrivateKeySpec(d, ecSpec)
        return KeyFactory.getInstance("EC").generatePrivate(keySpec) as ECPrivateKey
    }

    // ---------------------------------------------------------------------------
    // ECDH shared key derivation
    // ---------------------------------------------------------------------------

    /** Derives a 256-bit AES key from an ECDH exchange — compatible with Web Crypto deriveKey. */
    fun deriveSharedKey(myPrivateKey: ECPrivateKey, theirPublicKey: ECPublicKey): SecretKey {
        val agreement = KeyAgreement.getInstance("ECDH")
        agreement.init(myPrivateKey)
        agreement.doPhase(theirPublicKey, true)
        val sharedSecret = agreement.generateSecret()
        // P-256 shared secret X-coordinate is 32 bytes → AES-256 key directly
        return SecretKeySpec(sharedSecret.copyOf(32), "AES")
    }

    // ---------------------------------------------------------------------------
    // Message encryption / decryption
    // ---------------------------------------------------------------------------

    data class EncryptedMessage(val ciphertext: String, val iv: String)

    fun encryptMessage(plaintext: String, sharedKey: SecretKey): EncryptedMessage {
        val iv = ByteArray(12).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, sharedKey, GCMParameterSpec(128, iv))
        val ciphertextBytes = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        return EncryptedMessage(base64Std(ciphertextBytes), base64Std(iv))
    }

    fun decryptMessage(ciphertext: String, iv: String, sharedKey: SecretKey): String {
        val ciphertextBytes = fromBase64Std(ciphertext)
        val ivBytes = fromBase64Std(iv)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, sharedKey, GCMParameterSpec(128, ivBytes))
        return String(cipher.doFinal(ciphertextBytes), Charsets.UTF_8)
    }

    // ---------------------------------------------------------------------------
    // Private key wrapping (PBKDF2 + AES-GCM) — compatible with web crypto.js
    // ---------------------------------------------------------------------------

    data class EncryptedKeyBlob(val encryptedData: String, val iv: String, val salt: String)

    fun encryptPrivateKey(privateKeyJwk: String, password: String): EncryptedKeyBlob {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(12).also { SecureRandom().nextBytes(it) }
        val wrappingKey = deriveWrappingKey(password, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, wrappingKey, GCMParameterSpec(128, iv))
        val encrypted = cipher.doFinal(privateKeyJwk.toByteArray(Charsets.UTF_8))
        return EncryptedKeyBlob(base64Std(encrypted), base64Std(iv), base64Std(salt))
    }

    fun decryptPrivateKey(blob: EncryptedKeyBlob, password: String): String {
        val salt = fromBase64Std(blob.salt)
        val iv = fromBase64Std(blob.iv)
        val encryptedData = fromBase64Std(blob.encryptedData)
        val wrappingKey = deriveWrappingKey(password, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, wrappingKey, GCMParameterSpec(128, iv))
        return String(cipher.doFinal(encryptedData), Charsets.UTF_8)
    }

    private fun deriveWrappingKey(password: String, salt: ByteArray): SecretKey {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password.toCharArray(), salt, 100_000, 256)
        return SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
    }

    // ---------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------

    private fun getEcSpec(): ECParameterSpec {
        val params = AlgorithmParameters.getInstance("EC")
        params.init(ECGenParameterSpec("secp256r1"))
        return params.getParameterSpec(ECParameterSpec::class.java)
    }

    /** Strip leading sign byte and/or left-pad to exactly 32 bytes. */
    private fun padTo32(bytes: ByteArray): ByteArray {
        val stripped = if (bytes.size == 33 && bytes[0] == 0.toByte()) bytes.copyOfRange(1, 33) else bytes
        return if (stripped.size < 32) ByteArray(32 - stripped.size) + stripped else stripped
    }

    /** Standard Base64 (no wrap) — web client uses btoa which is standard base64. */
    private fun base64Std(bytes: ByteArray): String =
        Base64.encodeToString(bytes, Base64.NO_WRAP)

    private fun fromBase64Std(s: String): ByteArray =
        Base64.decode(s, Base64.DEFAULT)

    /** URL-safe Base64 without padding for JWK coordinates. */
    private fun base64Url(bytes: ByteArray): String =
        Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)

    private fun fromBase64Url(s: String): ByteArray {
        val padded = s + "=".repeat((4 - s.length % 4) % 4)
        return Base64.decode(padded.replace('-', '+').replace('_', '/'), Base64.DEFAULT)
    }
}
