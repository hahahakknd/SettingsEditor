package kkj.settingseditor.data

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

class MyMemoryDatabase(context: Context) {
    companion object {
        private const val TAG = "SettingsEditor.MyMemoryDatabase"
        private const val ID = 0
        private const val NAME = 1
        private const val VALUE = 2
    }

    private object DbContract {
        const val CREATE_TABLE_SQL
            = "CREATE TABLE ${SettingsEntry.TABLE_SETTINGS} " +
                "(${BaseColumns._ID} INTEGER PRIMARY KEY, " +
                "${SettingsEntry.COLUMN_NAME} TEXT NOT NULL, " +
                "${SettingsEntry.COLUMN_VALUE} TEXT)"
        const val DELETE_TABLE_SQL = "DROP TABLE IF EXISTS ${SettingsEntry.TABLE_SETTINGS}"

        object SettingsEntry : BaseColumns {
            const val TABLE_SETTINGS = "settings"
            const val COLUMN_NAME = "name"
            const val COLUMN_VALUE = "value"
        }
    }

    private val mDb = MyDatabaseHelper(context).writableDatabase

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

    fun searchAll(): ArrayList<Array<String>> {
        val sql = "SELECT ${BaseColumns._ID}, " +
                         "${DbContract.SettingsEntry.COLUMN_NAME}, " +
                         "${DbContract.SettingsEntry.COLUMN_VALUE} " +
                  "FROM ${DbContract.SettingsEntry.TABLE_SETTINGS} " +
                  "ORDER BY ${DbContract.SettingsEntry.COLUMN_NAME} COLLATE NOCASE ASC"
        val whereArgs = null

        mDb.rawQuery(sql, whereArgs)?.use { cursor ->
            val result = ArrayList<Array<String>>()
            while (cursor.moveToNext()) {
                result.add(
                    arrayOf(
                        getDataAsString(cursor, ID),
                        getDataAsString(cursor, NAME),
                        getDataAsString(cursor, VALUE)
                    )
                )
            }
            return result
        }

        return ArrayList()
    }

    fun search(pattern: String): ArrayList<Array<String>> {
        val sql = "SELECT ${BaseColumns._ID}, " +
                         "${DbContract.SettingsEntry.COLUMN_NAME}, " +
                         "${DbContract.SettingsEntry.COLUMN_VALUE} " +
                  "FROM ${DbContract.SettingsEntry.TABLE_SETTINGS} " +
                  "WHERE ${DbContract.SettingsEntry.COLUMN_NAME} GLOB ? " +
                  "ORDER BY ${DbContract.SettingsEntry.COLUMN_NAME} COLLATE NOCASE ASC"
        val whereArgs = arrayOf("*$pattern*")

        mDb.rawQuery(sql, whereArgs)?.use { cursor ->
            val result = ArrayList<Array<String>>()
            while (cursor.moveToNext()) {
                result.add(
                    arrayOf(
                        getDataAsString(cursor, ID),
                        getDataAsString(cursor, NAME),
                        getDataAsString(cursor, VALUE)
                    )
                )
            }
            return result
        }

        return ArrayList()
    }

    fun refresh(items: Array<Array<String>>) {
        mDb.beginTransaction()
        mDb.execSQL(DbContract.DELETE_TABLE_SQL)
        mDb.execSQL(DbContract.CREATE_TABLE_SQL)

        val sql = "INSERT INTO ${DbContract.SettingsEntry.TABLE_SETTINGS}(" +
                    "${BaseColumns._ID}, " +
                    "${DbContract.SettingsEntry.COLUMN_NAME}, " +
                    "${DbContract.SettingsEntry.COLUMN_VALUE}" +
                ") " +
                "VALUES (?,?,?)"

        for (item in items) {
            val bindArgs = arrayOf(item[ID], item[NAME], item[VALUE])
            mDb.execSQL(sql, bindArgs)
        }
        mDb.setTransactionSuccessful()
        mDb.endTransaction()
    }

    fun delete(id: String) {
        val deleteSql = "DELETE FROM ${DbContract.SettingsEntry.TABLE_SETTINGS} " +
                        "WHERE ${BaseColumns._ID}=?"
        val deleteWhereArgs = arrayOf(id)
        mDb.execSQL(deleteSql, deleteWhereArgs)
    }

    private class MyDatabaseHelper(context: Context) : SQLiteOpenHelper(context, null, null, DATABASE_VERSION) {
        companion object {
            const val DATABASE_VERSION = 1
        }

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(DbContract.CREATE_TABLE_SQL)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL(DbContract.DELETE_TABLE_SQL)
            onCreate(db)
        }

        override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            onUpgrade(db, oldVersion, newVersion)
        }
    }
}