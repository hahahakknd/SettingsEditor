package kkj.settingseditor.data

import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.*
import android.util.Log

// 여기는 일종의 Data Server 개념이다.
// Singleton 으로 구현되어 Process 가 종료될 때까지 살아있는다.
// UI 에서 자유롭게 필요한 Data 들을 가져다 쓸 수 있다.
// Background 로 Data 를 처리하고, Main Thread 에서 UI 처리를 할 수 있도록 구조가 필요하다.
// 이걸 가지고 ViewModel 구조에 적용시킬 수 있나?? 해봤는데 안된다..... 먼가 코드가 꼬인다. 머가 잘못된거지??
class MyDataCenter private constructor(context: Context) {
    companion object {
        private const val TAG = "SettingsEditor.MyDataCenter"

        const val SETTINGS_FAVORITE = 0
        const val SETTINGS_GLOBAL = 1
        const val SETTINGS_SYSTEM = 2
        const val SETTINGS_SECURE = 3

        private object SettingsTable {
            const val TBL_NAME = "settings"
            const val COL_NAME = "name"
            const val COL_VALUE = "value"
            const val COL_TYPE = "type"
            const val COL_IS_FAVORITE = "is_favorite"
        }

        private object SQL {
            const val CREATE_TABLE =
                "CREATE TABLE ${SettingsTable.TBL_NAME}(" +
                             "${SettingsTable.COL_NAME} TEXT NOT NULL, " +
                             "${SettingsTable.COL_VALUE} TEXT, " +
                             "${SettingsTable.COL_TYPE} INTEGER NOT NULL, " +
                             "${SettingsTable.COL_IS_FAVORITE} INTEGER NOT NULL)"
            const val DROP_TABLE = "DROP TABLE IF EXISTS ${SettingsTable.TBL_NAME}"
            const val DELETE_TABLE = "DELETE FROM ${SettingsTable.TBL_NAME}"
            const val REFRESH_SETTINGS =
                "INSERT INTO ${SettingsTable.TBL_NAME} " +
                "VALUES (?,?,?,?)"
            const val UPDATE_FAVORITE_SQL =
                "UPDATE ${SettingsTable.TBL_NAME} " +
                "SET ${SettingsTable.COL_IS_FAVORITE}=? " +
                "WHERE ${SettingsTable.COL_NAME}=? AND ${SettingsTable.COL_TYPE}=?"
            const val SEARCH_SETTINGS =
                "SELECT ${SettingsTable.COL_NAME}, ${SettingsTable.COL_VALUE}, ${SettingsTable.COL_TYPE} " +
                "FROM ${SettingsTable.TBL_NAME} " +
                "WHERE ${SettingsTable.COL_TYPE}=? AND ${SettingsTable.COL_NAME} LIKE ?" +
                "ORDER BY ${SettingsTable.COL_NAME} COLLATE NOCASE ASC"
            const val SEARCH_ALL_SETTINGS =
                "SELECT ${SettingsTable.COL_NAME}, ${SettingsTable.COL_VALUE}, ${SettingsTable.COL_TYPE} " +
                "FROM ${SettingsTable.TBL_NAME} " +
                "WHERE ${SettingsTable.COL_TYPE}=? " +
                "ORDER BY ${SettingsTable.COL_NAME} COLLATE NOCASE ASC"
            const val SEARCH_FAVORITE =
                "SELECT ${SettingsTable.COL_NAME}, ${SettingsTable.COL_VALUE}, ${SettingsTable.COL_TYPE} " +
                "FROM ${SettingsTable.TBL_NAME} " +
                "WHERE ${SettingsTable.COL_IS_FAVORITE}=1 AND ${SettingsTable.COL_NAME} LIKE ? " +
                "ORDER BY ${SettingsTable.COL_NAME} COLLATE NOCASE ASC"
            const val SEARCH_ALL_FAVORITE =
                "SELECT ${SettingsTable.COL_NAME}, ${SettingsTable.COL_VALUE}, ${SettingsTable.COL_TYPE} " +
                "FROM ${SettingsTable.TBL_NAME} " +
                "WHERE ${SettingsTable.COL_IS_FAVORITE}=1 " +
                "ORDER BY ${SettingsTable.COL_NAME} COLLATE NOCASE ASC"
            const val UPDATE_SETTINGS =
                "UPDATE ${SettingsTable.TBL_NAME} " +
                "SET ${SettingsTable.COL_VALUE}=? " +
                "WHERE ${SettingsTable.COL_NAME}=? AND ${SettingsTable.COL_TYPE}=?"
        }

        @Volatile
        private var instance: MyDataCenter? = null
        fun getInstance() = instance

        @JvmStatic
        @Synchronized
        fun makeInstance(context: Context) {
            instance ?: MyDataCenter(context).also { instance = it }
        }
    }

    abstract class DataObserver(val id: Int) {
        abstract fun onRefresh()
        abstract fun onChanged(items: ArrayList<Item>)
        abstract fun onUpdated(item: Item)
        abstract fun onAddFavorite(item: Item)
        abstract fun onDeleteFavorite(item: Item)
    }

    abstract class UIObserver(val id: Int) {
        abstract fun onRefresh()
        abstract fun onChanged()
        abstract fun onUpdated()
        abstract fun onAddFavorite()
        abstract fun onDeleteFavorite()
    }

    enum class SettingsType { GLOBAL, SYSTEM, SECURE }
    class Item(val name: String, val value: String, val type: SettingsType)

    private val mDataHandler: DataHandler
    private val mDataObservers = ArrayList<DataObserver>()
    private val mUIObservers = ArrayList<UIObserver>()

    init {
        val handlerThread = HandlerThread(TAG)
        handlerThread.start()
        mDataHandler = DataHandler(handlerThread.looper,
                                   DBHelper(context).writableDatabase,
                                   MyDatabase(context),
                                   MySettings(context),
                                   mDataObservers,
                                   UIHandler(Looper.getMainLooper(), mUIObservers)
        )
    }

    fun registerObserver(dataObserver: DataObserver) {
        for (ov in mDataObservers) {
            if (ov.id == dataObserver.id) {
                Log.e(TAG, "DataObserver(${dataObserver.id}) is Already registered.")
                return
            }
        }
        mDataObservers.add(dataObserver)
    }

    fun registerObserver(uiObserver: UIObserver) {
        for (ov in mUIObservers) {
            if (ov.id == uiObserver.id) {
                Log.e(TAG, "UIObserver(${uiObserver.id}) is Already registered.")
                return
            }
        }
        mUIObservers.add(uiObserver)
    }

    fun unregisterObserver(id: Int) {
        for (ov in mDataObservers) {
            if (ov.id == id) {
                mDataObservers.remove(ov)
                return
            }
        }
        for (ov in mUIObservers) {
            if (ov.id == id) {
                mUIObservers.remove(ov)
                return
            }
        }
    }

    fun clearObservers() {
        mDataObservers.clear()
        mUIObservers.clear()
    }

    fun refresh() {
        val message = Message.obtain()
        message.what = DataHandler.REFRESH
        mDataHandler.sendMsg(message)
    }

    fun searchSettings(dataType: Int, pattern: String) {
        val bundle = Bundle()
        bundle.putInt(DataHandler.TYPE, dataType)
        bundle.putString(DataHandler.PATTERN, pattern)
        val message = Message.obtain()
        message.what = DataHandler.SEARCH_SETTINGS
        message.data = bundle
        mDataHandler.sendMsg(message)
    }

    fun searchAllSettings(dataType: Int) {
        val bundle = Bundle()
        bundle.putInt(DataHandler.TYPE, dataType)
        val message = Message.obtain()
        message.what = DataHandler.SEARCH_ALL_SETTINGS
        message.data = bundle
        mDataHandler.sendMsg(message)
    }

    fun updateSettings(item: Item) {
        val bundle = Bundle()
        bundle.putInt(DataHandler.TYPE,
            when (item.type) {
                SettingsType.GLOBAL -> SETTINGS_GLOBAL
                SettingsType.SYSTEM -> SETTINGS_SYSTEM
                SettingsType.SECURE -> SETTINGS_SECURE
            }
        )
        bundle.putString(DataHandler.NAME, item.name)
        bundle.putString(DataHandler.VALUE, item.value)
        val message = Message.obtain()
        message.what = DataHandler.UPDATE_SETTINGS
        message.data = bundle
        mDataHandler.sendMsg(message)
    }

    fun addFavorite(item: Item) {
        val bundle = Bundle()
        bundle.putInt(DataHandler.TYPE,
            when (item.type) {
                SettingsType.GLOBAL -> SETTINGS_GLOBAL
                SettingsType.SYSTEM -> SETTINGS_SYSTEM
                SettingsType.SECURE -> SETTINGS_SECURE
            }
        )
        bundle.putString(DataHandler.NAME, item.name)
        bundle.putString(DataHandler.VALUE, item.value)
        val message = Message.obtain()
        message.what = DataHandler.ADD_FAVORITE
        message.data = bundle
        mDataHandler.sendMsg(message)
    }

    fun deleteFavorite(item: Item) {
        val bundle = Bundle()
        bundle.putInt(DataHandler.TYPE,
            when (item.type) {
                SettingsType.GLOBAL -> SETTINGS_GLOBAL
                SettingsType.SYSTEM -> SETTINGS_SYSTEM
                SettingsType.SECURE -> SETTINGS_SECURE
            }
        )
        bundle.putString(DataHandler.NAME, item.name)
        bundle.putString(DataHandler.VALUE, item.value)
        val message = Message.obtain()
        message.what = DataHandler.DELETE_FAVORITE
        message.data = bundle
        mDataHandler.sendMsg(message)
    }

    class DataHandler(
            looper: Looper, private val cacheDb: SQLiteDatabase, private val myDb: MyDatabase,
            private val mySettings: MySettings, private val dataObservers: ArrayList<DataObserver>,
            private val uiHandler: UIHandler
    ) : Handler(looper) {
        companion object {
            const val REFRESH = 0
            const val SEARCH_SETTINGS = 1
            const val SEARCH_ALL_SETTINGS = 2
            const val UPDATE_SETTINGS = 3
            const val ADD_FAVORITE = 4
            const val DELETE_FAVORITE = 5

            const val TYPE = "type"
            const val NAME = "name"
            const val VALUE = "value"
            const val PATTERN = "pattern"
        }

        fun sendMsg(msg: Message) {
            if (!sendMessage(msg)) {
                Log.e(TAG, "Fail to send message to handler.")
            }
        }

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                REFRESH -> {
                    refresh()
                }
                SEARCH_SETTINGS -> {
                    val bundle = msg.data
                    val type = bundle.getInt(TYPE)
                    val pattern = bundle.getString(PATTERN) ?: ""
                    searchSettings(type, pattern)
                }
                SEARCH_ALL_SETTINGS -> {
                    val bundle = msg.data
                    val type = bundle.getInt(TYPE)
                    searchAllSettings(type)
                }
                UPDATE_SETTINGS -> {
                    val bundle = msg.data
                    val name = bundle.getString(NAME) ?: ""
                    val value = bundle.getString(VALUE) ?: ""
                    val type = bundle.getInt(TYPE)
                    updateSettings(name, value, type)
                }
                ADD_FAVORITE -> {
                    val bundle = msg.data
                    val name = bundle.getString(NAME) ?: ""
                    val value = bundle.getString(VALUE) ?: ""
                    val type = bundle.getInt(TYPE)
                    addFavorite(name, value, type)
                }
                DELETE_FAVORITE -> {
                    val bundle = msg.data
                    val name = bundle.getString(NAME) ?: ""
                    val value = bundle.getString(VALUE) ?: ""
                    val type = bundle.getInt(TYPE)
                    deleteFavorite(name, value, type)
                }
                else -> {
                    Log.w(TAG, "Unknown handle message(" + msg.what.toString() + ").")
                }
            }
        }

        private fun refreshSettings(type: Int) {
            val settingsCursor =
                when (type) {
                    SETTINGS_GLOBAL -> {
                        mySettings.readGlobalSettings()
                    }
                    SETTINGS_SYSTEM -> {
                        mySettings.readSystemSettings()
                    }
                    SETTINGS_SECURE -> {
                        mySettings.readSecureSettings()
                    }
                    else -> {
                        Log.e(TAG, "Fail to read settings.")
                        return
                    }
                }

            settingsCursor?.use {
                while (it.moveToNext()) {
                    val colData = ArrayList<String>()
                    for (colIdx in 0 until it.columnCount) {
                        colData.add(DataUtils.getDataAsString(it, colIdx))
                    }
                    colData.add(type.toString())
                    colData.add("0")

                    try {
                        cacheDb.execSQL(SQL.REFRESH_SETTINGS, colData.toArray())
                    } catch (e: SQLException) {
                        Log.e(TAG, "Fail to insert settings. $e")
                    }
                }
            } ?: return

            val favoriteCursor = myDb.read()
            favoriteCursor.use {
                while (it.moveToNext()) {
                    val colData = ArrayList<String>()
                    colData.add("1")
                    for (colIdx in 0 until it.columnCount) {
                        colData.add(DataUtils.getDataAsString(it, colIdx))
                    }

                    try {
                        cacheDb.execSQL(SQL.UPDATE_FAVORITE_SQL, colData.toArray())
                    } catch (e: SQLException) {
                        Log.e(TAG, "Fail to update favorite. $e")
                    }
                }
            }
        }

        private fun refresh() {
            cacheDb.beginTransaction()
            try {
                cacheDb.execSQL(SQL.DELETE_TABLE)
                refreshSettings(SETTINGS_GLOBAL)
                refreshSettings(SETTINGS_SYSTEM)
                refreshSettings(SETTINGS_SECURE)
            } catch (e: SQLException) {
                Log.e(TAG, "Fail to reset data. $e")
            } finally {
                cacheDb.setTransactionSuccessful()
                cacheDb.endTransaction()
            }

            for (ov in dataObservers) {
                ov.onRefresh()
            }

            val message = Message.obtain()
            message.what = UIHandler.REFRESH
            uiHandler.sendMsg(message)
        }

        private fun searchSettings(dataType: Int, pattern: String) {
            val result = ArrayList<Item>()
            if (dataType == SETTINGS_FAVORITE) {
                cacheDb.rawQuery(SQL.SEARCH_FAVORITE, arrayOf("%$pattern%")).use {
                    while (it.moveToNext()) {
                        result.add(
                            Item(
                                DataUtils.getDataAsString(it, 0),
                                DataUtils.getDataAsString(it, 1),
                                when (it.getInt(2)) {
                                    SETTINGS_GLOBAL -> SettingsType.GLOBAL
                                    SETTINGS_SYSTEM -> SettingsType.SYSTEM
                                    SETTINGS_SECURE -> SettingsType.SECURE
                                    else -> continue
                                }
                            )
                        )
                    }
                }
            } else {
                cacheDb.rawQuery(SQL.SEARCH_SETTINGS, arrayOf(dataType.toString(), "%$pattern%")).use {
                    while (it.moveToNext()) {
                        val foundType = it.getInt(2)
                        if (dataType != foundType) {
                            Log.e(TAG, "Invalid settings, find type $dataType but found type $foundType")
                            continue
                        }
                        result.add(
                            Item(
                                DataUtils.getDataAsString(it, 0),
                                DataUtils.getDataAsString(it, 1),
                                when (foundType) {
                                    SETTINGS_GLOBAL -> SettingsType.GLOBAL
                                    SETTINGS_SYSTEM -> SettingsType.SYSTEM
                                    SETTINGS_SECURE -> SettingsType.SECURE
                                    else -> continue
                                }
                            )
                        )
                    }
                }
            }

            for (ov in dataObservers) {
                if (ov.id == dataType) {
                    ov.onChanged(result)
                }
            }

            val bundle = Bundle()
            bundle.putInt(UIHandler.TYPE, dataType)
            val message = Message.obtain()
            message.what = UIHandler.CHANGED
            message.data = bundle
            uiHandler.sendMsg(message)
        }

        private fun searchAllSettings(dataType: Int) {
            val result = ArrayList<Item>()
            if (dataType == SETTINGS_FAVORITE) {
                cacheDb.rawQuery(SQL.SEARCH_ALL_FAVORITE, null).use {
                    while (it.moveToNext()) {
                        result.add(
                            Item(
                                DataUtils.getDataAsString(it, 0),
                                DataUtils.getDataAsString(it, 1),
                                when (it.getInt(2)) {
                                    SETTINGS_GLOBAL -> SettingsType.GLOBAL
                                    SETTINGS_SYSTEM -> SettingsType.SYSTEM
                                    SETTINGS_SECURE -> SettingsType.SECURE
                                    else -> continue
                                }
                            )
                        )
                    }
                }
            } else {
                cacheDb.rawQuery(SQL.SEARCH_ALL_SETTINGS, arrayOf(dataType.toString())).use {
                    while (it.moveToNext()) {
                        val foundType = it.getInt(2)
                        if (dataType != foundType) {
                            Log.e(
                                TAG,
                                "Invalid settings, find type $dataType but found type $foundType"
                            )
                            continue
                        }
                        result.add(
                            Item(
                                DataUtils.getDataAsString(it, 0),
                                DataUtils.getDataAsString(it, 1),
                                when (foundType) {
                                    SETTINGS_GLOBAL -> SettingsType.GLOBAL
                                    SETTINGS_SYSTEM -> SettingsType.SYSTEM
                                    SETTINGS_SECURE -> SettingsType.SECURE
                                    else -> continue
                                }
                            )
                        )
                    }
                }
            }

            for (ov in dataObservers) {
                if (ov.id == dataType) {
                    ov.onChanged(result)
                }
            }

            val bundle = Bundle()
            bundle.putInt(UIHandler.TYPE, dataType)
            val message = Message.obtain()
            message.what = UIHandler.CHANGED
            message.data = bundle
            uiHandler.sendMsg(message)
        }

        private fun updateSettings(name: String, value: String, settingsType: Int) {
//            var isSuccess = false
//            when (settingsType) {
//                SETTINGS_GLOBAL -> {
//                    isSuccess = mySettings.writeGlobalSettings(name, value)
//                }
//                SETTINGS_SYSTEM -> {
//                    isSuccess = mySettings.writeSystemSettings(name, value)
//                }
//                SETTINGS_SECURE -> {
//                    isSuccess = mySettings.writeSecureSettings(name, value)
//                }
//                else -> {}
//            }

            val isSuccess = true
            if (isSuccess) {
                cacheDb.execSQL(SQL.UPDATE_SETTINGS, arrayOf(value, name, settingsType))

                for (ov in dataObservers) {
                    if ((ov.id == SETTINGS_FAVORITE) or (ov.id == settingsType)) {
                        ov.onUpdated(
                            Item(name, value,
                                when (settingsType) {
                                    SETTINGS_GLOBAL -> SettingsType.GLOBAL
                                    SETTINGS_SYSTEM -> SettingsType.SYSTEM
                                    SETTINGS_SECURE -> SettingsType.SECURE
                                    else -> continue
                                }
                            )
                        )
                    }
                }

                val bundle = Bundle()
                bundle.putString(UIHandler.NAME, name)
                bundle.putString(UIHandler.VALUE, value)
                bundle.putInt(UIHandler.TYPE, settingsType)
                val message = Message.obtain()
                message.what = UIHandler.CHANGED
                message.data = bundle
                uiHandler.sendMsg(message)
            }
        }

        private fun addFavorite(name: String, value: String, settingsType: Int) {
            cacheDb.execSQL(SQL.UPDATE_FAVORITE_SQL, arrayOf("1", name, settingsType))
            myDb.write(name, settingsType.toString())

            for (ov in dataObservers) {
                if (ov.id == SETTINGS_FAVORITE) {
                    ov.onAddFavorite(
                        Item(name, value,
                            when (settingsType) {
                                SETTINGS_GLOBAL -> SettingsType.GLOBAL
                                SETTINGS_SYSTEM -> SettingsType.SYSTEM
                                SETTINGS_SECURE -> SettingsType.SECURE
                                else -> continue
                            }
                        )
                    )
                }
            }

            val bundle = Bundle()
            bundle.putString(UIHandler.NAME, name)
            bundle.putString(UIHandler.VALUE, value)
            bundle.putInt(UIHandler.TYPE, settingsType)
            val message = Message.obtain()
            message.what = UIHandler.CHANGED
            message.data = bundle
            uiHandler.sendMsg(message)
        }

        private fun deleteFavorite(name: String, value: String, settingsType: Int) {
            cacheDb.execSQL(SQL.UPDATE_FAVORITE_SQL, arrayOf("0", name, settingsType))
            myDb.delete(name, settingsType.toString())

            for (ov in dataObservers) {
                if (ov.id == SETTINGS_FAVORITE) {
                    ov.onDeleteFavorite(
                        Item(name, value,
                            when (settingsType) {
                                SETTINGS_GLOBAL -> SettingsType.GLOBAL
                                SETTINGS_SYSTEM -> SettingsType.SYSTEM
                                SETTINGS_SECURE -> SettingsType.SECURE
                                else -> continue
                            }
                        )
                    )
                }
            }

            val bundle = Bundle()
            bundle.putString(UIHandler.NAME, name)
            bundle.putString(UIHandler.VALUE, value)
            bundle.putInt(UIHandler.TYPE, settingsType)
            val message = Message.obtain()
            message.what = UIHandler.CHANGED
            message.data = bundle
            uiHandler.sendMsg(message)
        }
    }

    class UIHandler(looper: Looper, private val uiObservers: ArrayList<UIObserver>) : Handler(looper) {
        companion object {
            const val REFRESH = 0
            const val CHANGED = 1
            const val UPDATED = 2
            const val ADD_FAVORITE = 3
            const val DELETE_FAVORITE = 4

            const val TYPE = "type"
            const val NAME = "name"
            const val VALUE = "value"
        }

        fun sendMsg(msg: Message) {
            if (!sendMessage(msg)) {
                Log.e(TAG, "Fail to send message on UI.")
            }
        }

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                REFRESH -> {
                    for (ov in uiObservers) {
                        ov.onRefresh()
                    }
                }
                CHANGED -> {
                    val bundle = msg.data
                    val dataType = bundle.getInt(DataHandler.TYPE)

                    for (ov in uiObservers) {
                        if (ov.id == dataType) {
                            ov.onChanged()
                        }
                    }
                }
                UPDATED -> {
                    val bundle = msg.data
                    val name = bundle.getString(DataHandler.NAME) ?: ""
                    val value = bundle.getString(DataHandler.VALUE) ?: ""
                    val settingsType = bundle.getInt(DataHandler.TYPE)

                    for (ov in uiObservers) {
                        if ((ov.id == SETTINGS_FAVORITE) or (ov.id == settingsType)) {
                            ov.onUpdated()
                        }
                    }
                }
                ADD_FAVORITE -> {
                    val bundle = msg.data
                    val name = bundle.getString(DataHandler.NAME) ?: ""
                    val value = bundle.getString(DataHandler.VALUE) ?: ""
                    val settingsType = bundle.getInt(DataHandler.TYPE)

                    for (ov in uiObservers) {
                        if (ov.id == SETTINGS_FAVORITE) {
                            ov.onAddFavorite()
                        }
                    }
                }
                DELETE_FAVORITE -> {
                    val bundle = msg.data
                    val name = bundle.getString(DataHandler.NAME) ?: ""
                    val value = bundle.getString(DataHandler.VALUE) ?: ""
                    val settingsType = bundle.getInt(DataHandler.TYPE)

                    for (ov in uiObservers) {
                        if (ov.id == SETTINGS_FAVORITE) {
                            ov.onDeleteFavorite()
                        }
                    }
                }
                else -> {
                    Log.w(TAG, "Unknown handle message(" + msg.what.toString() + ").")
                }
            }
        }
    }

    private class DBHelper(context: Context) : SQLiteOpenHelper(context,null,null,1) {
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