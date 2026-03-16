package com.socialchat.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.socialchat.app.core.network.NetworkResult
import com.socialchat.app.core.network.RetrofitProvider
import com.socialchat.app.core.network.SessionCookieJar
import com.socialchat.app.core.preferences.UserPreferences
import com.socialchat.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val baseUrl: String = "",
    val isSaving: Boolean = false,
    val savedSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: UserPreferences,
    private val retrofitProvider: RetrofitProvider,
    private val authRepository: AuthRepository,
    private val cookieJar: SessionCookieJar
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val url = prefs.baseUrl.first()
            _uiState.update { it.copy(baseUrl = url) }
        }
    }

    fun updateBaseUrl(url: String) = _uiState.update { it.copy(baseUrl = url) }

    fun saveBaseUrl() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val url = _uiState.value.baseUrl.trim().trimEnd('/')
            prefs.setBaseUrl(url)
            prefs.addServer(url)
            retrofitProvider.rebuild(url)
            _uiState.update { it.copy(isSaving = false, savedSuccess = true) }
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            cookieJar.clearCookies()
            onLoggedOut()
        }
    }
}
