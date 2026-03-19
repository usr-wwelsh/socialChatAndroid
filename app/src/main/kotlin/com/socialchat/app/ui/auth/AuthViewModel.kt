package com.socialchat.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.socialchat.app.core.crypto.CryptoManager
import com.socialchat.app.core.network.NetworkResult
import com.socialchat.app.core.network.RetrofitProvider
import com.socialchat.app.core.network.SessionCookieJar
import com.socialchat.app.core.preferences.UserPreferences
import com.socialchat.app.core.socket.SocketManager
import com.socialchat.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthState(
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
    val username: String? = null,
    val serverList: List<String> = emptyList(),
    val selectedServer: String = ""
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val prefs: UserPreferences,
    private val socketManager: SocketManager,
    private val retrofitProvider: RetrofitProvider,
    private val cookieJar: SessionCookieJar,
    private val cryptoManager: CryptoManager
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkSession()
        loadServerList()
    }

    private fun checkSession() {
        viewModelScope.launch {
            val hasSession = prefs.hasSession()
            if (!hasSession) {
                _authState.value = _authState.value.copy(isAuthenticated = false, isLoading = false)
                return@launch
            }
            when (val result = repository.checkSession()) {
                is NetworkResult.Success -> {
                    val authenticated = result.data.authenticated
                    if (authenticated) {
                        socketManager.connect()
                        result.data.user?.id?.let { prefs.saveUserId(it) }
                    }
                    _authState.value = _authState.value.copy(
                        isAuthenticated = authenticated,
                        isLoading = false,
                        username = result.data.user?.username
                    )
                }
                is NetworkResult.Error -> {
                    _authState.value = _authState.value.copy(isAuthenticated = false, isLoading = false)
                }
                else -> {}
            }
        }
    }

    private fun loadServerList() {
        viewModelScope.launch {
            val initialUrl = prefs.getBaseUrl()
            _authState.value = _authState.value.copy(selectedServer = initialUrl)
            prefs.serverList.collect { list ->
                _authState.value = _authState.value.copy(serverList = list)
            }
        }
    }

    fun selectServer(url: String) {
        viewModelScope.launch {
            prefs.setBaseUrl(url)
            cookieJar.clearCookies()
            retrofitProvider.rebuild(url)
            _authState.value = _authState.value.copy(selectedServer = url)
        }
    }

    fun addServer(url: String) {
        viewModelScope.launch {
            val trimmed = url.trim().trimEnd('/')
            prefs.addServer(trimmed)
            selectServer(trimmed)
        }
    }

    fun removeServer(url: String) {
        viewModelScope.launch {
            prefs.removeServer(url)
            if (_authState.value.selectedServer == url) {
                selectServer(UserPreferences.DEFAULT_BASE_URL)
            }
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            when (val result = repository.login(username, password)) {
                is NetworkResult.Success -> {
                    prefs.saveCachedUsername(result.data.username)
                    prefs.saveUserId(result.data.id)
                    socketManager.connect()
                    _authState.value = _authState.value.copy(
                        isAuthenticated = true,
                        isLoading = false,
                        username = result.data.username
                    )
                    cryptoManager.initializeKeys(password, result.data.id)
                }
                is NetworkResult.Error -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                else -> {}
            }
        }
    }

    fun register(username: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            when (val result = repository.register(username, password)) {
                is NetworkResult.Success -> {
                    prefs.saveCachedUsername(result.data.username)
                    prefs.saveUserId(result.data.id)
                    socketManager.connect()
                    _authState.value = _authState.value.copy(
                        isAuthenticated = true,
                        isLoading = false,
                        username = result.data.username
                    )
                    cryptoManager.initializeKeys(password, result.data.id)
                }
                is NetworkResult.Error -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                else -> {}
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            socketManager.disconnect()
            cryptoManager.clearSession()
            prefs.clearSession()
            _authState.value = _authState.value.copy(isAuthenticated = false, isLoading = false)
        }
    }
}
