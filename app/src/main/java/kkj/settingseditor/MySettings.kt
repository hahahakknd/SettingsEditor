package kkj.settingseditor

import android.content.Context
import android.database.Cursor
import android.provider.Settings
import android.util.Log

class MySettings private constructor (context: Context) {
    companion object {
        private const val TAG: String = "SettingsEditor.MySettings"
        @Volatile
        private var instance: MySettings? = null
        fun getInstance() = instance
        @Synchronized
        fun makeInstance(context: Context) {
            instance?: MySettings(context).also { instance = it }
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

    private fun querySettings(uri: android.net.Uri): ArrayList<Array<String>> {
        val settings = arrayListOf<Array<String>>()
        mContentResolver?.query(
            uri,
            arrayOf("_id", "name", "value"),
            null,
            null,
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val colData = Array(cursor.columnCount) { "" }
                for (colIdx in 0 until cursor.columnCount) {
                    colData[colIdx] = getDataAsString(cursor, colIdx)
                }
                settings.add(colData)
            }
        }
        return settings
    }

    fun queryGlobalSettings(): ArrayList<Array<String>> {
        return querySettings(Settings.Global.CONTENT_URI)
    }

    fun querySystemSettings(): ArrayList<Array<String>> {
        return querySettings(Settings.System.CONTENT_URI)
    }

    fun querySecureSettings(): ArrayList<Array<String>> {
        return querySettings(Settings.Secure.CONTENT_URI)
    }
}