package kkj.settingseditor.data

import android.content.Context
import android.database.Cursor
import android.provider.Settings

class MySettings private constructor (context: Context) {
    companion object {
        private const val TAG: String = "SettingsEditor.MySettings"

        @Volatile
        private var instance: MySettings? = null
        fun getInstance() = instance
        @Synchronized
        fun makeInstance(context: Context) {
            instance ?: MySettings(context).also { instance = it }
        }
    }

    private val mContentResolver = context.contentResolver

    private fun getDataAsString(cursor: Cursor, index: Int): String {
        when (cursor.getType(index)) {
            Cursor.FIELD_TYPE_NULL -> {
                return "NULL"
            }
            Cursor.FIELD_TYPE_INTEGER -> {
                return cursor.getInt(index).toString()
            }
            Cursor.FIELD_TYPE_FLOAT -> {
                return cursor.getFloat(index).toString()
            }
            Cursor.FIELD_TYPE_STRING -> {
                return cursor.getString(index)
            }
            Cursor.FIELD_TYPE_BLOB -> {
                return cursor.getBlob(index).toString()
            }
            else -> {
                return "INVALID TYPE"
            }
        }
    }

    private fun querySettings(uri: android.net.Uri): Array<Array<String>> {
        mContentResolver?.query(
            uri,
            arrayOf("_id", "name", "value"),
            null,
            null,
            null  // "name ASC", Settings 값들은 정렬이 안된다. XML 로 바껴서???
        )?.use { cursor ->
            val result = Array(cursor.count) { Array(cursor.columnCount) { "" } }
            var rowIndex = 0
            while (cursor.moveToNext()) {
                for (colIndex in 0 until cursor.columnCount) {
                    result[rowIndex][colIndex] = getDataAsString(cursor, colIndex)
                }
                rowIndex++
            }
            return result
        }

        return emptyArray()
    }

    fun readGlobalSettings(): Array<Array<String>> {
        return querySettings(Settings.Global.CONTENT_URI)
    }

    fun readSystemSettings(): Array<Array<String>> {
        return querySettings(Settings.System.CONTENT_URI)
    }

    fun readSecureSettings(): Array<Array<String>> {
        return querySettings(Settings.Secure.CONTENT_URI)
    }

    fun writeGlobalSettings(name: String, value: String): Boolean {
        return Settings.Global.putString(mContentResolver, name, value)
    }

    fun writeSystemSettings(name: String, value: String): Boolean {
        return Settings.System.putString(mContentResolver, name, value)
    }

    fun writeSecureSettings(name: String, value: String): Boolean {
        return Settings.Secure.putString(mContentResolver, name, value)
    }
}