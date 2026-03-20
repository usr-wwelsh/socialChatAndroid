package com.socialchat.app.core.crypto

import android.util.Log
import com.socialchat.app.core.network.safeApiCall
import com.socialchat.app.core.preferences.UserPreferences
import com.socialchat.app.data.api.KeysApiService
import com.socialchat.app.data.dto.StoreKeysRequest
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import javax.crypto.SecretKey
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CryptoManager @Inject constructor(
    private val prefs: UserPreferences,
    private val keysApi: KeysApiService
) {
    private val tag = "CryptoManager"
    // In-memory shared key cache per conversationId
    private val sharedKeyCache = mutableMapOf<Int, SecretKey>()

    /**
     * Call after login/register. Fetches or generates the user's ECDH key pair.
     * Stores the decrypted private key JWK in DataStore for the session.
     */
    suspend fun initializeKeys(password: String, userId: Int) {
        try {
            Log.d(tag, "Fetching encrypted private key from server for userId=$userId")
            val resp = keysApi.getMyKeys()
            Log.d(tag, "getMyKeys response: ${resp.code()}")
            if (resp.isSuccessful) {
                val body = resp.body()
                val hasServerKey = !body?.encryptedPrivateKey.isNullOrEmpty()
                Log.d(tag, "encryptedPrivateKey present: $hasServerKey, keyIv: ${body?.keyIv?.take(8)}, keySalt: ${body?.keySalt?.take(8)}")
                if (hasServerKey) {
                    // Server has a key — MUST decrypt it. Always sync from server on login
                    // to stay consistent after DB restores or migrations.
                    val blob = E2ECrypto.EncryptedKeyBlob(
                        encryptedData = body!!.encryptedPrivateKey!!,
                        iv = body.keyIv ?: "",
                        salt = body.keySalt ?: ""
                    )
                    val privateKeyJwk = E2ECrypto.decryptPrivateKey(blob, password)
                    Log.d(tag, "Private key decrypted OK, kty=${org.json.JSONObject(privateKeyJwk).optString("kty")}")
                    prefs.savePrivateKeyJwk(privateKeyJwk)
                    prefs.saveUserId(userId)
                    return
                }
            }

            Log.d(tag, "No keys on server — generating new ECDH P-256 key pair")
            val keyPair = E2ECrypto.generateKeyPair()
            val publicKeyJwk = E2ECrypto.exportPublicKeyJwk(keyPair.public as ECPublicKey)
            val privateKeyJwk = E2ECrypto.exportPrivateKeyJwk(keyPair)
            Log.d(tag, "Key pair generated, uploading public key")

            val encryptedBlob = E2ECrypto.encryptPrivateKey(privateKeyJwk, password)
            safeApiCall {
                val uploadResp = keysApi.storeKeys(
                    StoreKeysRequest(
                        publicKey = publicKeyJwk,
                        encryptedPrivateKey = encryptedBlob.encryptedData,
                        keyIv = encryptedBlob.iv,
                        keySalt = encryptedBlob.salt
                    )
                )
                if (!uploadResp.isSuccessful) throw retrofit2.HttpException(uploadResp)
            }
            Log.d(tag, "Keys uploaded successfully")

            prefs.savePrivateKeyJwk(privateKeyJwk)
            prefs.saveUserId(userId)
        } catch (e: Exception) {
            Log.e(tag, "Failed to initialize crypto keys", e)
        }
    }

    suspend fun hasPrivateKey(): Boolean = !prefs.getPrivateKeyJwk().isNullOrEmpty()

    /**
     * Derives (and caches) the shared AES key for a given conversation.
     * Returns null if private key is not loaded or partner's public key is missing.
     */
    suspend fun getSharedKey(conversationId: Int, partnerPublicKeyJwk: String?): SecretKey? {
        sharedKeyCache[conversationId]?.let { return it }
        if (partnerPublicKeyJwk.isNullOrEmpty()) {
            Log.w(tag, "conv=$conversationId: partner has no public key")
            return null
        }
        val myPrivateJwk = prefs.getPrivateKeyJwk()
        if (myPrivateJwk.isNullOrEmpty()) {
            Log.w(tag, "conv=$conversationId: own private key not in DataStore — user needs to log in again")
            return null
        }
        return try {
            val myPrivate = E2ECrypto.importPrivateKeyJwk(myPrivateJwk) as ECPrivateKey
            val theirPublic = E2ECrypto.importPublicKeyJwk(partnerPublicKeyJwk) as ECPublicKey
            val key = E2ECrypto.deriveSharedKey(myPrivate, theirPublic)
            sharedKeyCache[conversationId] = key
            Log.d(tag, "conv=$conversationId: shared key derived OK")
            key
        } catch (e: Exception) {
            Log.e(tag, "conv=$conversationId: failed to derive shared key", e)
            null
        }
    }

    fun clearSession() {
        sharedKeyCache.clear()
    }
}
