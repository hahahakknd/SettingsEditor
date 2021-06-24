package kkj.settingseditor.ui

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import kkj.settingseditor.MyUtils
import kkj.settingseditor.R
import kkj.settingseditor.data.MyDatabase
import kkj.settingseditor.data.MyMemoryDatabase
import kkj.settingseditor.data.MySettings

class CardViewAdapter(context: Context, private val page: Int, private val fragmentManager: FragmentManager?)
        : RecyclerView.Adapter<CardViewAdapter.ViewHolder>() {
    companion object {
        private const val TAG = "SettingsEditor.CardViewAdapter"

        const val ID = 0
        const val NAME = 1
        const val VALUE = 2

        private const val FAVORITE_PAGE = 0
        private const val GLOBAL_PAGE = 1
        private const val SYSTEM_PAGE = 2
        private const val SECURE_PAGE = 3
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mItem = emptyArray<String>()
        var mItemViewPosition = -1
        val mNameView = itemView.findViewById(R.id.item_name) as TextView
        val mValueView = itemView.findViewById(R.id.item_value) as TextView
    }

    class UIHandler(looper: Looper, private val adapter: CardViewAdapter) : Handler(looper) {
        companion object {
            private const val UPDATE_ID = 0
        }

        fun updateUI() {
            if (!sendEmptyMessage(UPDATE_ID)) {
                Log.e(TAG, "Fail to update ui. Cannot send message to handler.")
            }
        }

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                UPDATE_ID -> {
                    adapter.notifyDataSetChanged()
                }
                else -> {
                    Log.w(TAG, "Unknown handle message(" + msg.what.toString() + ").")
                }
            }
        }
    }

    class DataHandler(looper: Looper, private val uiHandler: UIHandler, private val adapter: CardViewAdapter) : Handler(looper) {
        companion object {
            private const val REFRESH_ITEMS = 0
            private const val SEARCH_ALL_ITEMS = 1
            private const val SEARCH_ITEMS = 2
        }

        fun refreshItems() {
            if (!sendEmptyMessage(REFRESH_ITEMS)) {
                Log.e(TAG, "Fail to refresh items. Cannot send message to handler.")
            }
        }

        fun searchAllItems() {
            if (!sendEmptyMessage(SEARCH_ALL_ITEMS)) {
                Log.e(TAG, "Fail to search all items. Cannot send message to handler.")
            }
        }

        fun searchItems(pattern: String) {
            if (!sendMessage(obtainMessage(SEARCH_ITEMS, pattern))) {
                Log.e(TAG, "Fail to search items. Cannot send message to handler.")
            }
        }

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                REFRESH_ITEMS -> {
                    val items =
                        when (adapter.page) {
                            FAVORITE_PAGE
                                -> MyDatabase.getInstance()?.read() ?: emptyArray()               // Favorite
                            GLOBAL_PAGE
                                -> MySettings.getInstance()?.readGlobalSettings() ?: emptyArray() // Global
                            SYSTEM_PAGE
                                -> MySettings.getInstance()?.readSystemSettings() ?: emptyArray() // System
                            SECURE_PAGE
                                -> MySettings.getInstance()?.readSecureSettings() ?: emptyArray() // Secure
                            else
                                -> emptyArray()
                        }
                    adapter.mMemoryDatabase.refresh(items)
                    adapter.mItemList.clear()
                    adapter.mItemList.addAll(adapter.mMemoryDatabase.searchAll())
                    uiHandler.updateUI()
                }
                SEARCH_ALL_ITEMS -> {
                    adapter.mItemList.clear()
                    adapter.mItemList.addAll(adapter.mMemoryDatabase.searchAll())
                    uiHandler.updateUI()
                }
                SEARCH_ITEMS -> {
                    val pattern = msg.obj as String
                    adapter.mItemList.clear()
                    adapter.mItemList.addAll(adapter.mMemoryDatabase.search(pattern))
                    uiHandler.updateUI()
                }
                else -> {
                    Log.w(TAG, "Unknown handle message(" + msg.what.toString() + ").")
                }
            }
        }
    }

    private val mItemList = ArrayList<Array<String>>()
    private val mMemoryDatabase = MyMemoryDatabase(context)
    private val mDataHandler: DataHandler

    init {
        val handlerThread = HandlerThread(TAG)
        handlerThread.start()
        mDataHandler = DataHandler(handlerThread.looper, UIHandler(context.mainLooper,this),this)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item, parent,false)
        val viewHolder = ViewHolder(view)

        view.setOnClickListener {
            fragmentManager.let {
                if (it != null) {
                    EditBoxDialogFragment(
                        viewHolder.mItem,
                        viewHolder.mItemViewPosition,
                        this
                    ).show(it, "EditBox")
                } else {
                    MyUtils.showSnackbarWithAutoDismiss(
                        view,
                        "Do not edit settings, FragmentManager is null."
                    )
                }
            }
        }

        if (page == FAVORITE_PAGE) {
            view.setOnLongClickListener {
                MyDatabase.getInstance()?.delete(viewHolder.mItem[ID])
                mMemoryDatabase.delete(viewHolder.mItem[ID])
                mItemList.remove(viewHolder.mItem)
                notifyItemRemoved(viewHolder.mItemViewPosition)

                MyUtils.showSnackbarWithAutoDismiss(
                    view,
                    "Delete the settings item(${viewHolder.mItem[NAME]}) to favorite list."
                )
                return@setOnLongClickListener true
            }
        } else {
            view.setOnLongClickListener {
                MyDatabase.getInstance()?.write(
                    viewHolder.mItem[ID],
                    viewHolder.mItem[NAME],
                    viewHolder.mItem[VALUE]
                )

                MyUtils.showSnackbarWithAutoDismiss(
                    view,
                    "Add the settings item(${viewHolder.mItem[NAME]}) to favorite list."
                )
                return@setOnLongClickListener true
            }
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, itemViewPos: Int) {
        holder.mItem = mItemList[itemViewPos]
        holder.mItemViewPosition = itemViewPos
        holder.mNameView.text = mItemList[itemViewPos][NAME]
        holder.mValueView.text = mItemList[itemViewPos][VALUE]
    }

    override fun getItemCount(): Int {
        return mItemList.size
    }

    fun changeItem(itemViewPos: Int, value: String) {
        // 여기서 디비와 셋팅의 값 변경

        mItemList[itemViewPos][VALUE] = value
        notifyItemChanged(itemViewPos)
    }

    fun refreshItems() {
        mDataHandler.refreshItems()
    }

    fun searchAllItems() {
        mDataHandler.searchAllItems()
    }

    fun searchItems(pattern: String) {
        mDataHandler.searchItems(pattern)
    }
}
