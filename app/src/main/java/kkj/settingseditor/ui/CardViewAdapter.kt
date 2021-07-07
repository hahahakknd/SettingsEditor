package kkj.settingseditor.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kkj.settingseditor.MyUtils
import kkj.settingseditor.R
import kkj.settingseditor.data.MyDataCenter
import kkj.settingseditor.data.MyDataCenter.Item

class CardViewAdapter(
        val dataType: Int,
        private val editBoxDialogFragment: EditBoxDialogFragment
) : RecyclerView.Adapter<CardViewAdapter.ViewHolder>() {
    companion object {
        private const val TAG = "SettingsEditor.CardViewAdapter"
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mItem: Item? = null
        var mItemViewPosition = -1
        val mNameView = itemView.findViewById(R.id.item_name) as TextView
        val mValueView = itemView.findViewById(R.id.item_value) as TextView
    }

    private val mItems = ArrayList<Item>()

    fun updateItems(items: ArrayList<Item>) {
        mItems.clear()
        mItems.addAll(items)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item, parent,false)
        val viewHolder = ViewHolder(view)

        view.setOnClickListener {
            viewHolder.mItem?.let { item ->
                editBoxDialogFragment.show(item.name, item.value) { it2 ->
                    MyUtils.showSnackbarWithAutoDismiss(view, it2)
                }
            }
        }

        if (dataType == MyDataCenter.SETTINGS_FAVORITE) {
            view.setOnLongClickListener {
                viewHolder.mItem?.let { item ->
                    MyDataCenter.getInstance()?.deleteFavorite(item)
                    MyUtils.showSnackbarWithAutoDismiss(
                        view,
                        "Delete the settings item(${item.name}) to favorite list."
                    )
                }
                return@setOnLongClickListener true
            }
        } else {
            view.setOnLongClickListener {
                viewHolder.mItem?.let { item ->
                    MyDataCenter.getInstance()?.addFavorite(item)
                    MyUtils.showSnackbarWithAutoDismiss(
                        view,
                        "Add the settings item(${item.name}) to favorite list."
                    )
                }
                return@setOnLongClickListener true
            }
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, itemViewPos: Int) {
        holder.mItem = mItems[itemViewPos]
        holder.mItemViewPosition = itemViewPos
        holder.mNameView.text = holder.mItem?.name ?: ""
        holder.mValueView.text = holder.mItem?.value ?: ""
    }

    override fun getItemCount(): Int {
        return mItems.size
    }
}
