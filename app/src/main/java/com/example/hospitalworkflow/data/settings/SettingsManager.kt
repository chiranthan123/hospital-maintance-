package com.example.hospitalworkflow.data.settings

import android.content.Context
import android.content.SharedPreferences

object SettingsManager {
    private const val PREFS_NAME = "hospital_workflow_settings_prefs"
    private const val KEY_GOOGLE_AI_API_KEY = "google_ai_studio_api_key"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getGoogleAiApiKey(context: Context): String {
        return getPrefs(context).getString(KEY_GOOGLE_AI_API_KEY, "") ?: ""
    }

    fun saveGoogleAiApiKey(context: Context, apiKey: String) {
        getPrefs(context).edit().putString(KEY_GOOGLE_AI_API_KEY, apiKey.trim()).apply()
    }

    fun clearGoogleAiApiKey(context: Context) {
        getPrefs(context).edit().remove(KEY_GOOGLE_AI_API_KEY).apply()
    }

    fun isGoogleAiApiKeyConfigured(context: Context): Boolean {
        return getGoogleAiApiKey(context).isNotBlank()
    }
}
