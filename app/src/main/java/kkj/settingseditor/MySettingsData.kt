package kkj.settingseditor

import android.content.Context

class MySettingsData(context: Context) {
    companion object {
        private const val TAG = "SharedPreferencesManager"
        private const val PREFERENCES_NAME = "shared_preferences"
        private const val DEFAULT_VALUE_STRING: String = ""
        private const val DEFAULT_VALUE_BOOLEAN: Boolean = false
        private const val DEFAULT_VALUE_INT: Int = -1
        private const val DEFAULT_VALUE_LONG: Long = -1L
        private const val DEFAULT_VALUE_FLOAT: Float = -1F

        const val AUTO_RUN_SERVICE = "auto_run_service"
    }

    private val mSharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun removeKey(key: String) = mSharedPreferences.edit().remove(key).apply()
    fun clear() = mSharedPreferences.edit().clear().apply()

    var serviceAutoRun: Boolean
        get() = mSharedPreferences.getBoolean(AUTO_RUN_SERVICE, DEFAULT_VALUE_BOOLEAN)
        set(value) = mSharedPreferences.edit().putBoolean(AUTO_RUN_SERVICE, value).apply()
}