package kkj.settingseditor.data

import android.content.Context

class MyPreferences private constructor (context: Context) {
    companion object {
        private const val TAG = "SettingsEditor.MyPreferences"
        private const val PREFERENCES_NAME = "shared_preferences"
        private const val DEFAULT_VALUE_STRING: String = ""
        private const val DEFAULT_VALUE_BOOLEAN: Boolean = false
        private const val DEFAULT_VALUE_INT: Int = -1
        private const val DEFAULT_VALUE_LONG: Long = -1L
        private const val DEFAULT_VALUE_FLOAT: Float = -1F

        const val AUTO_RUN_SERVICE = "auto_run_service"

        @Volatile
        private var instance: MyPreferences? = null
        fun getInstance() = instance
        @Synchronized
        fun makeInstance(context: Context) {
            instance ?: MyPreferences(context).also { instance = it }
        }
    }

    private val mSharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun removeKey(key: String) = mSharedPreferences.edit().remove(key).apply()
    fun clear() = mSharedPreferences.edit().clear().apply()

    var serviceAutoRun: Boolean
        get() = mSharedPreferences.getBoolean(AUTO_RUN_SERVICE, DEFAULT_VALUE_BOOLEAN)
        set(value) = mSharedPreferences.edit().putBoolean(AUTO_RUN_SERVICE, value).apply()
}