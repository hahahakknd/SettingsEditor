package kkj.settingseditor.data

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class MyDatabase(context: Context) {
    companion object {
        private const val TAG = "SettingsEditor.MyDatabase"
        private const val DATABASE_NAME = "my_database.db"
        private const val DATABASE_VERSION = 1

        private object DBTable {
            const val TBL_NAME = "favorite_settings"
            const val COL_NAME = "name"
            const val COL_TYPE = "type"
        }

        private object SQL {
            const val CREATE_TABLE =
                "CREATE TABLE ${DBTable.TBL_NAME}(" +
                             "${DBTable.COL_NAME} TEXT NOT NULL, " +
                             "${DBTable.COL_TYPE} TEXT NOT NULL)"
            const val DROP_TABLE = "DROP TABLE IF EXISTS ${DBTable.TBL_NAME}"
            const val DELETE_TABLE = "DELETE FROM ${DBTable.TBL_NAME}"
            const val READ_ALL_FAVORITE =
                "SELECT ${DBTable.COL_NAME}, ${DBTable.COL_TYPE} " +
                "FROM ${DBTable.TBL_NAME} " +
                "ORDER BY ${DBTable.COL_NAME} COLLATE NOCASE ASC"
            const val CHECK_FAVORITE =
                "SELECT count(*) " +
                "FROM ${DBTable.TBL_NAME} " +
                "WHERE ${DBTable.COL_NAME}=? AND ${DBTable.COL_TYPE}=?"
            const val WRITE_FAVORITE =
                "INSERT INTO ${DBTable.TBL_NAME}(${DBTable.COL_NAME}, ${DBTable.COL_TYPE}) " +
                "VALUES (?,?)"
            const val DELETE_FAVORITE =
                "DELETE FROM ${DBTable.TBL_NAME} " +
                "WHERE ${DBTable.COL_NAME}=? AND ${DBTable.COL_TYPE}=?"
        }
    }

    private val mDb = DBHelper(context).writableDatabase

    fun read(): Cursor {
        return mDb.rawQuery(SQL.READ_ALL_FAVORITE,null)
    }

    fun write(name: String, type: String) {
        val isExist: Boolean
        mDb.rawQuery(SQL.CHECK_FAVORITE, arrayOf(name, type)).use {
            it.moveToNext()
            isExist = (DataUtils.getDataAsString(it, 0).toInt() != 0)
        }

        if (!isExist) {
            mDb.execSQL(SQL.WRITE_FAVORITE, arrayOf(name, type))
        }
    }

    fun delete(name: String, type: String) {
        mDb.execSQL(SQL.DELETE_FAVORITE, arrayOf(name, type))
    }

    private class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(SQL.CREATE_TABLE)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL(SQL.DROP_TABLE)
            onCreate(db)
        }

        override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            onUpgrade(db, oldVersion, newVersion)
        }
    }
}