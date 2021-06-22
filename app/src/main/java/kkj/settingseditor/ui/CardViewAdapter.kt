package kkj.settingseditor.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import kkj.settingseditor.data.MyDatabase
import kkj.settingseditor.MyUtils
import kkj.settingseditor.R
import kkj.settingseditor.data.MyMemoryDatabase

class  CardViewAdapter : RecyclerView.Adapter<CardViewAdapter.ViewHolder>() {
    companion object {
        private const val TAG = "SettingsEditor.CardViewAdapter"
        private const val ID = 0
        private const val NAME = 1
        private const val VALUE = 2
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mItemPosition = -1
        val mNameView = itemView.findViewById(R.id.item_name) as TextView
        val mValueView = itemView.findViewById(R.id.item_value) as TextView
    }

    // Adapter 에 추가된 데이터를 저장하기 위한 ArrayList
    private val mItemList = ArrayList<Array<String>>()
    private var mPageNumber = -1
    private var mFragmentManager: FragmentManager? = null

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // create a new view
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
        val viewHolder = ViewHolder(view)

        view.setOnClickListener {
            mFragmentManager?.let {
                EditBoxDialogFragment(
                    viewHolder.mItemPosition,
                    mItemList[viewHolder.mItemPosition][NAME],
                    mItemList[viewHolder.mItemPosition][VALUE],
                    this
                ).show(it, "EditBox")
            }
        }

        if (mPageNumber == 0) {
            view.setOnLongClickListener {
                if (viewHolder.mItemPosition == -1) {
                    return@setOnLongClickListener true
                }

                val name = mItemList[viewHolder.mItemPosition][NAME]

                MyDatabase.getInstance()?.delete(mItemList[viewHolder.mItemPosition][ID])
                MyMemoryDatabase.getInstance()?.delete(mItemList[viewHolder.mItemPosition][ID])
                mItemList.removeAt(viewHolder.mItemPosition)
                notifyItemRemoved(viewHolder.mItemPosition)
                MyUtils.showSnackbarWithAutoDismiss(
                    view,
                    "Delete the settings item($name) to favorite list."
                )
                return@setOnLongClickListener true
            }
        } else {
            view.setOnLongClickListener {
                if (viewHolder.mItemPosition == -1) {
                    return@setOnLongClickListener true
                }

                MyDatabase.getInstance()?.write(
                    mItemList[viewHolder.mItemPosition][ID],
                    mItemList[viewHolder.mItemPosition][NAME],
                    mItemList[viewHolder.mItemPosition][VALUE]
                )
                MyUtils.showSnackbarWithAutoDismiss(
                    view,
                    "Add the settings item(${mItemList[viewHolder.mItemPosition][NAME]}) to favorite list."
                )
                return@setOnLongClickListener true
            }
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mItemPosition = position
        holder.mNameView.text = mItemList[position][NAME]
        holder.mValueView.text = mItemList[position][VALUE]
    }

    override fun getItemCount(): Int {
        return mItemList.size
    }

    fun changeItem(pos: Int, value: String) {
        // 여기서 디비와 셋팅의 값 변경

        mItemList[pos][VALUE] = value
        notifyItemChanged(pos)
    }

    fun initItems(items: ArrayList<Array<String>>) {
        MyMemoryDatabase.getInstance()?.refresh(items)
        searchAllItems()
    }

    fun searchAllItems() {
        mItemList.clear()
        MyMemoryDatabase.getInstance()?.searchAll()?.let { mItemList.addAll(it) }
        notifyDataSetChanged()
    }

    fun searchItems(pattern: String) {
        mItemList.clear()
        MyMemoryDatabase.getInstance()?.search(pattern)?.let { mItemList.addAll(it) }
        notifyDataSetChanged()
    }

    fun setPageNumber(pageNumber: Int) {
        mPageNumber = pageNumber
    }

    fun setFragmentManager(fragmentManager: FragmentManager) {
        mFragmentManager = fragmentManager
    }
}
