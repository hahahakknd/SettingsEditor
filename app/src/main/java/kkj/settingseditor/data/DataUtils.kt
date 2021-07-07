package kkj.settingseditor.data

import android.database.Cursor

class DataUtils {
    companion object {
        fun getDataAsString(cursor: Cursor, index: Int): String {
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
    }
}