package kkj.settingseditor.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kkj.settingseditor.MyDatabase
import kkj.settingseditor.MyUtils
import kkj.settingseditor.R

class  CardViewAdapter : RecyclerView.Adapter<CardViewAdapter.ViewHolder>() {
    companion object {
        private const val TAG = "SettingsEditor.CardViewAdapter"
        private const val ID = 0
        private const val NAME = 1
        private const val VALUE = 2
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mNameView = itemView.findViewById(R.id.item_name) as TextView
        val mValueView = itemView.findViewById(R.id.item_value) as TextView
    }

    // Adapter 에 추가된 데이터를 저장하기 위한 ArrayList
    private val mItemList = ArrayList<Array<String>>()
    private var mPageNumber = -1
    private var mItemPosition = -1

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // create a new view
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)

        view.setOnClickListener {
            MyUtils.showSnackbarWithAutoDismiss(view, "Click 하면 Edit box 를 띄워야 한다.")
        }

        if (mPageNumber == 0) {
            view.setOnLongClickListener {
                if (mItemPosition == -1) {
                    return@setOnLongClickListener true
                }

                MyDatabase.getInstance()?.delete(mItemList[mItemPosition][ID])
                mItemList.removeAt(mItemPosition)
                notifyDataSetChanged()
                MyUtils.showSnackbarWithAutoDismiss(view, "Delete the settings item(${mItemList[mItemPosition][NAME]}) to favorite list.")
                return@setOnLongClickListener true
            }
        } else {
            view.setOnLongClickListener {
                if (mItemPosition == -1) {
                    return@setOnLongClickListener true
                }

                MyDatabase.getInstance()?.write(mItemList[mItemPosition][ID], mItemList[mItemPosition][NAME], mItemList[mItemPosition][VALUE])
                MyUtils.showSnackbarWithAutoDismiss(view, "Add the settings item(${mItemList[mItemPosition][NAME]}) to favorite list.")
                return@setOnLongClickListener true
            }
        }

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        mItemPosition = position
        holder.mNameView.text = mItemList[position][NAME]
        holder.mValueView.text = mItemList[position][VALUE]
    }

    override fun getItemCount(): Int {
        return mItemList.size
    }

    // 아이템 데이터 추가를 위한 함수. 개발자가 원하는대로 작성 가능.
    fun addItems(items: ArrayList<Array<String>>) {
        mItemList.clear()
        mItemList.addAll(items)
        notifyDataSetChanged()
    }

    fun setPageNumber(pageNumber: Int) {
        mPageNumber = pageNumber
    }
}
