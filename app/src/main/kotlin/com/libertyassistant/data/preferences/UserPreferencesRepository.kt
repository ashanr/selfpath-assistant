package com.libertyassistant.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.libertyassistant.ai.ApiProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val userPreferences: Flow<UserPreferences> = dataStore.data.map { prefs ->
        val providerName = prefs[UserPreferencesKeys.API_PROVIDER]
        UserPreferences(
            useApi = prefs[UserPreferencesKeys.USE_API] ?: false,
            apiProvider = providerName?.let { name ->
                ApiProvider.entries.firstOrNull { it.name == name }
            } ?: ApiProvider.OPENAI,
            apiKey = prefs[UserPreferencesKeys.API_KEY] ?: "",
            apiModel = prefs[UserPreferencesKeys.API_MODEL] ?: ""
        )
    }

    suspend fun setUseApi(useApi: Boolean) {
        dataStore.edit { it[UserPreferencesKeys.USE_API] = useApi }
    }

    suspend fun setApiProvider(provider: ApiProvider) {
        dataStore.edit { it[UserPreferencesKeys.API_PROVIDER] = provider.name }
    }

    suspend fun setApiKey(key: String) {
        dataStore.edit { it[UserPreferencesKeys.API_KEY] = key }
    }

    suspend fun setApiModel(model: String) {
        dataStore.edit { it[UserPreferencesKeys.API_MODEL] = model }
    }
}
