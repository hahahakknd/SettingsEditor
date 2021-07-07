package kkj.settingseditor.data

import android.content.Context
import android.database.Cursor
import android.provider.Settings

class MySettings(context: Context) {
    companion object {
        private const val TAG: String = "SettingsEditor.MySettings"
    }

    private val mContentResolver = context.contentResolver

    private fun read(uri: android.net.Uri): Cursor? {
        // "name ASC", Settings 값들은 정렬이 안된다. XML 로 바껴서???
        return mContentResolver.query(uri,arrayOf("name", "value"),null,null,null)
    }

    fun readGlobalSettings(): Cursor? {
        return read(Settings.Global.CONTENT_URI)
    }
    fun writeGlobalSettings(name: String, value: String): Boolean {
        return Settings.Global.putString(mContentResolver, name, value)
    }

    fun readSystemSettings(): Cursor? {
        return read(Settings.System.CONTENT_URI)
    }
    fun writeSystemSettings(name: String, value: String): Boolean {
        return Settings.System.putString(mContentResolver, name, value)
    }

    fun readSecureSettings(): Cursor? {
        return read(Settings.Secure.CONTENT_URI)
    }
    fun writeSecureSettings(name: String, value: String): Boolean {
        return Settings.Secure.putString(mContentResolver, name, value)
    }
}