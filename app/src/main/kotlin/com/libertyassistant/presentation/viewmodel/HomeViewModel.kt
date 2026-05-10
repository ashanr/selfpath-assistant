package com.libertyassistant.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.libertyassistant.ai.AssistantMode
import com.libertyassistant.ai.InferenceEngine
import com.libertyassistant.data.preferences.UserPreferencesRepository
import com.libertyassistant.data.repository.ChatSessionRepository
import com.libertyassistant.domain.model.AIResponse
import com.libertyassistant.domain.model.JournalEntry
import com.libertyassistant.domain.usecase.GenerateAIResponseUseCase
import com.libertyassistant.domain.usecase.SaveJournalEntryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class HomeUiState(
    val inputText: String = "",
    val selectedMode: AssistantMode = AssistantMode.PHILOSOPHICAL,
    val aiResponse: AIResponse? = null,
    val isLoading: Boolean = false,
    val isModelReady: Boolean = false,
    val isInitializing: Boolean = false,
    val errorMessage: String? = null,
    val savedSuccess: Boolean = false,
    val sessionMessageCount: Int = 0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val generateAIResponseUseCase: GenerateAIResponseUseCase,
    private val saveJournalEntryUseCase: SaveJournalEntryUseCase,
    private val inferenceEngine: InferenceEngine,
    private val prefsRepository: UserPreferencesRepository,
    private val chatSessionRepository: ChatSessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var currentSessionId: String = UUID.randomUUID().toString()
    private var sessionStarted = false

    init {
        initializeEngine()
    }

    private fun initializeEngine() {
        viewModelScope.launch {
            val prefs = prefsRepository.userPreferences.first()
            if (prefs.useApi && prefs.apiKey.isNotBlank()) {
                _uiState.value = _uiState.value.copy(
                    isInitializing = false,
                    isModelReady = true
                )
                return@launch
            }
            _uiState.value = _uiState.value.copy(isInitializing = true)
            val ready = inferenceEngine.initialize()
            _uiState.value = _uiState.value.copy(
                isInitializing = false,
                isModelReady = ready,
                errorMessage = if (!ready) inferenceEngine.getInitError() else null
            )
        }
    }

    fun onInputChanged(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text, errorMessage = null)
    }

    fun onModeSelected(mode: AssistantMode) {
        _uiState.value = _uiState.value.copy(selectedMode = mode)
    }

    fun generateResponse() {
        val state = _uiState.value
        if (state.inputText.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Please enter some text first")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            // Lazily create the session on first message
            if (!sessionStarted) {
                chatSessionRepository.createSession(currentSessionId, state.selectedMode.name)
                sessionStarted = true
            }

            val response = generateAIResponseUseCase(state.inputText, state.selectedMode)

            // Persist both sides of the exchange to local Room
            chatSessionRepository.addMessage(currentSessionId, "user", state.inputText)
            if (!response.isError) {
                chatSessionRepository.addMessage(currentSessionId, "assistant", response.content)
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                aiResponse = response,
                errorMessage = if (response.isError) response.errorMessage else null,
                sessionMessageCount = _uiState.value.sessionMessageCount + if (response.isError) 1 else 2
            )
        }
    }

    /** Closes the active session, pushes it to Atlas, and resets the UI for a new chat. */
    fun startNewChat() {
        viewModelScope.launch {
            if (sessionStarted) {
                chatSessionRepository.endAndSyncSession(currentSessionId)
            }
            currentSessionId = UUID.randomUUID().toString()
            sessionStarted = false
            _uiState.value = _uiState.value.copy(
                inputText = "",
                aiResponse = null,
                errorMessage = null,
                sessionMessageCount = 0
            )
        }
    }

    fun saveToJournal() {
        val state = _uiState.value
        val response = state.aiResponse ?: return
        viewModelScope.launch {
            val entry = JournalEntry(
                title = state.inputText.take(50),
                content = state.inputText,
                mode = state.selectedMode.name,
                aiResponse = response.content
            )
            saveJournalEntryUseCase(entry)
            _uiState.value = _uiState.value.copy(savedSuccess = true)
        }
    }

    fun clearSavedSuccess() {
        _uiState.value = _uiState.value.copy(savedSuccess = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        inferenceEngine.release()
    }
}
