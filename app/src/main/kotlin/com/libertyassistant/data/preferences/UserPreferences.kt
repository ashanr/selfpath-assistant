package com.libertyassistant.data.preferences

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.libertyassistant.ai.ApiProvider

data class UserPreferences(
    val useApi: Boolean = false,
    val apiProvider: ApiProvider = ApiProvider.OPENAI,
    val apiKey: String = "",
    val apiModel: String = ""
)

object UserPreferencesKeys {
    val USE_API = booleanPreferencesKey("use_api")
    val API_PROVIDER = stringPreferencesKey("api_provider")
    val API_KEY = stringPreferencesKey("api_key")
    val API_MODEL = stringPreferencesKey("api_model")
}
