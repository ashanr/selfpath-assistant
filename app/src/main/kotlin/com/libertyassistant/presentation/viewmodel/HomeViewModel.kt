package com.libertyassistant.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.libertyassistant.ai.AssistantMode
import com.libertyassistant.ai.InferenceEngine
import com.libertyassistant.domain.model.AIResponse
import com.libertyassistant.domain.model.JournalEntry
import com.libertyassistant.domain.usecase.GenerateAIResponseUseCase
import com.libertyassistant.domain.usecase.SaveJournalEntryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val inputText: String = "",
    val selectedMode: AssistantMode = AssistantMode.PHILOSOPHICAL,
    val aiResponse: AIResponse? = null,
    val isLoading: Boolean = false,
    val isModelReady: Boolean = false,
    val isInitializing: Boolean = false,
    val errorMessage: String? = null,
    val savedSuccess: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val generateAIResponseUseCase: GenerateAIResponseUseCase,
    private val saveJournalEntryUseCase: SaveJournalEntryUseCase,
    private val inferenceEngine: InferenceEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        initializeEngine()
    }

    private fun initializeEngine() {
        viewModelScope.launch {
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
            val response = generateAIResponseUseCase(state.inputText, state.selectedMode)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                aiResponse = response,
                errorMessage = if (response.isError) response.errorMessage else null
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
