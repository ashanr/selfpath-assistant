package com.libertyassistant.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.libertyassistant.BuildConfig
import com.libertyassistant.data.local.entity.ChatSessionEntity
import com.libertyassistant.data.repository.ChatSessionRepository
import com.libertyassistant.domain.model.RemoteChatSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val remoteSessions: List<RemoteChatSession> = emptyList(),
    val localSessions: List<ChatSessionEntity> = emptyList(),
    val isLoadingRemote: Boolean = false,
    val isAtlasConfigured: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val chatSessionRepository: ChatSessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        observeLocalSessions()
        checkAtlasAndLoad()
    }

    private fun observeLocalSessions() {
        viewModelScope.launch {
            chatSessionRepository.getAllLocalSessions().collect { sessions ->
                _uiState.value = _uiState.value.copy(localSessions = sessions)
            }
        }
    }

    private fun checkAtlasAndLoad() {
        viewModelScope.launch {
            val configured = BuildConfig.MONGO_URI.isNotBlank()
            _uiState.value = _uiState.value.copy(isAtlasConfigured = configured)
            if (configured) refresh()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingRemote = true, errorMessage = null)
            try {
                val remote = chatSessionRepository.fetchRemoteSessions()
                _uiState.value = _uiState.value.copy(
                    remoteSessions = remote,
                    isLoadingRemote = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingRemote = false,
                    errorMessage = "Failed to load remote history: ${e.message}"
                )
            }
        }
    }
}
