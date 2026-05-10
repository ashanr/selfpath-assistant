package com.libertyassistant.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.libertyassistant.ai.ApiProvider
import com.libertyassistant.data.preferences.UserPreferences
import com.libertyassistant.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefsRepository: UserPreferencesRepository
) : ViewModel() {

    val userPreferences: StateFlow<UserPreferences> = prefsRepository.userPreferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserPreferences()
        )

    fun setUseApi(enabled: Boolean) {
        viewModelScope.launch { prefsRepository.setUseApi(enabled) }
    }

    fun setApiProvider(provider: ApiProvider) {
        viewModelScope.launch { prefsRepository.setApiProvider(provider) }
    }

    fun setApiKey(key: String) {
        viewModelScope.launch { prefsRepository.setApiKey(key) }
    }

    fun setApiModel(model: String) {
        viewModelScope.launch { prefsRepository.setApiModel(model) }
    }
}
