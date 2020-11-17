package com.ncsu.imagc.manager

object SharedPreferencesManager {
    const val name: String = "NCSUPreferences"
    enum class SettingsPreferences(val namePreference: String) {
        USE_AZURE("useAzure")
    }
}