package kkj.settingseditor.data

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

class MyDatabase private constructor (context: Context) {
    companion object {
        private const val TAG = "SettingsEditor.MyDatabase"

        @Volatile
        private var instance: MyDatabase? = null
        fun getInstance() = instance
        @Synchronized
        fun makeInstance(context: Context) {
            instance ?: MyDatabase(context).also { instance = it }
        }
    }

    private object DbContract {
        const val CREATE_TABLE_SQL
            = "CREATE TABLE ${FavoriteSettingsEntry.TABLE_FAVOR} " +
                "(${BaseColumns._ID} INTEGER PRIMARY KEY, " +
                "${FavoriteSettingsEntry.COLUMN_NAME} TEXT NOT NULL, " +
                "${FavoriteSettingsEntry.COLUMN_VALUE} TEXT)"
        const val DELETE_TABLE_SQL = "DROP TABLE IF EXISTS ${FavoriteSettingsEntry.TABLE_FAVOR}"

        object FavoriteSettingsEntry : BaseColumns {
            const val TABLE_FAVOR = "favorite_settings"
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

    fun read(): ArrayList<Array<String>> {
        val sql = "SELECT ${BaseColumns._ID}, " +
                         "${DbContract.FavoriteSettingsEntry.COLUMN_NAME}, " +
                         "${DbContract.FavoriteSettingsEntry.COLUMN_VALUE} " +
                  "FROM ${DbContract.FavoriteSettingsEntry.TABLE_FAVOR} " +
                  "ORDER BY ${DbContract.FavoriteSettingsEntry.COLUMN_NAME} COLLATE NOCASE ASC"
        val whereArgs = null
        val result = ArrayList<Array<String>>()

        mDb.rawQuery(sql, whereArgs)?.use { cursor ->
            while (cursor.moveToNext()) {
                val colData = Array(cursor.columnCount) { "" }
                for (colIdx in 0 until cursor.columnCount) {
                    colData[colIdx] = getDataAsString(cursor, colIdx)
                }
                result.add(colData)
            }
        }

        return result
    }

    fun write(id: String, name: String, value: String) {
        var isUpdate = false
        val selectSql = "SELECT count(*) " +
                        "FROM ${DbContract.FavoriteSettingsEntry.TABLE_FAVOR} " +
                        "WHERE ${BaseColumns._ID}=?"
        val selectWhereArgs = arrayOf(id)
        mDb.rawQuery(selectSql, selectWhereArgs)?.use { cursor ->
            cursor.moveToNext()
            isUpdate = (getDataAsString(cursor,0).toInt() != 0)
        }

        val sql: String
        val bindArgs: Array<String>
        if (isUpdate) {
            sql = "UPDATE ${DbContract.FavoriteSettingsEntry.TABLE_FAVOR} " +
                  "SET ${DbContract.FavoriteSettingsEntry.COLUMN_VALUE}=? " +
                  "WHERE ${BaseColumns._ID}=?"
            bindArgs = arrayOf(value, id)
        } else {
            sql = "INSERT INTO ${DbContract.FavoriteSettingsEntry.TABLE_FAVOR}(" +
                       "${BaseColumns._ID}, " +
                       "${DbContract.FavoriteSettingsEntry.COLUMN_NAME}, " +
                       "${DbContract.FavoriteSettingsEntry.COLUMN_VALUE}" +
                  ") " +
                  "VALUES (?,?,?)"
            bindArgs = arrayOf(id, name, value)
        }
        mDb.execSQL(sql, bindArgs)
    }

    fun delete(id: String) {
        val deleteSql = "DELETE FROM ${DbContract.FavoriteSettingsEntry.TABLE_FAVOR} " +
                        "WHERE ${BaseColumns._ID}=?"
        val deleteWhereArgs = arrayOf(id)
        mDb.execSQL(deleteSql, deleteWhereArgs)
    }

    private class MyDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
        companion object {
            // If you change the database schema, you must increment the database version.
            const val DATABASE_VERSION = 1
            const val DATABASE_NAME = "my_database.db"
        }

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(DbContract.CREATE_TABLE_SQL)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(DbContract.DELETE_TABLE_SQL)
            onCreate(db)
        }

        override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            onUpgrade(db, oldVersion, newVersion)
        }
    }
}