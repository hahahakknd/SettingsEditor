package kkj.settingseditor

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.util.Log

class MyDatabase private constructor (context: Context) {
    companion object {
        private const val TAG = "SettingsEditor.MyDatabase"

        @Volatile
        private var instance: MyDatabase? = null
        fun getInstance() = instance
        @Synchronized
        fun makeInstance(context: Context) {
            instance?: MyDatabase(context).also { instance = it }
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
        val dataList = arrayListOf<Array<String>>()
        val projection
            = arrayOf(BaseColumns._ID,
                      DbContract.FavoriteSettingsEntry.COLUMN_NAME,
                      DbContract.FavoriteSettingsEntry.COLUMN_VALUE)
        val selection = null
        val selectionArgs = null
        val groupBy = null
        val having = null
        val sortOrder = null
        mDb.query(
            DbContract.FavoriteSettingsEntry.TABLE_FAVOR,
            projection,
            selection,              // The columns for the WHERE clause
            selectionArgs,          // The values for the WHERE clause
            groupBy,                   // don't group the rows
            having,                   // don't filter by row groups
            sortOrder               // The sort order
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val id = getDataAsString(cursor, 0)
                val name = getDataAsString(cursor, 1)
                val value = getDataAsString(cursor, 2)
                dataList.add(arrayOf(id, name, value))
            }
        }
        return dataList
    }

    fun write(name: String, value: String): Boolean {
        val values = ContentValues().apply {
            put(DbContract.FavoriteSettingsEntry.COLUMN_NAME, name)
            put(DbContract.FavoriteSettingsEntry.COLUMN_VALUE, value)
        }
        val result = mDb.insert(DbContract.FavoriteSettingsEntry.TABLE_FAVOR, null, values).toInt()
        return result != -1
    }

    class MyDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
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
        companion object {
            // If you change the database schema, you must increment the database version.
            const val DATABASE_VERSION = 1
            const val DATABASE_NAME = "my_database.db"
        }
    }
}