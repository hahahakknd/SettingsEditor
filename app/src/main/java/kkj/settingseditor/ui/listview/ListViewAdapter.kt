package kkj.settingseditor.ui.listview

import android.content.Context
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import kkj.settingseditor.MyDatabase
import kkj.settingseditor.R

import java.util.ArrayList

// ListViewAdapter 의 생성자
class ListViewAdapter : BaseAdapter() {
    companion object {
        private const val TAG = "SettingsEditor.ListViewAdapter"
        private const val ID = 0
        private const val NAME = 1
        private const val VALUE = 2
    }

    // Adapter 에 추가된 데이터를 저장하기 위한 ArrayList
    private val mListViewItemList = ArrayList<Array<String>>()
    private var mPageNumber = -1

    // Adapter 에 사용되는 데이터의 개수를 리턴 (필수 구현)
    override fun getCount(): Int {
        return mListViewItemList.size
    }

    // position 에 위치한 데이터를 화면에 출력하는데 사용될 View 를 리턴 (필수 구현)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView?:
            (parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
                    .inflate(R.layout.item, parent, false)
        val nameTextView = view.findViewById(R.id.item_name) as TextView
        val valueEditText = view.findViewById(R.id.item_value) as EditText
        val listViewItem = mListViewItemList[position]

        if (mPageNumber == 0) {
            nameTextView.setOnLongClickListener {
                MyDatabase.getInstance()?.delete(listViewItem[ID])
                mListViewItemList.removeAt(position)
                notifyDataSetChanged()
                Snackbar.make(view, "Delete the settings item to favorite list.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show()
                return@setOnLongClickListener true
            }
        } else {
            nameTextView.setOnLongClickListener {
                MyDatabase.getInstance()?.write(listViewItem[ID], listViewItem[NAME], listViewItem[VALUE])
                Snackbar.make(view, "Add the settings item to favorite list.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show()
                return@setOnLongClickListener true
            }
        }

        valueEditText.setOnKeyListener { v, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                val imm = parent.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                valueEditText.clearFocus()
            }
            return@setOnKeyListener true
        }

        nameTextView.text = listViewItem[NAME]
        valueEditText.setText(listViewItem[VALUE])

        return view
    }

    // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
    override fun getItem(position: Int): Any {
        return mListViewItemList[position]
    }

    // 아이템 데이터 추가를 위한 함수. 개발자가 원하는대로 작성 가능.
    fun addItems(items: ArrayList<Array<String>>) {
        mListViewItemList.clear()
        mListViewItemList.addAll(items)
        notifyDataSetChanged()
    }

    fun setPageNumber(pageNumber: Int) {
        mPageNumber = pageNumber
    }
}
