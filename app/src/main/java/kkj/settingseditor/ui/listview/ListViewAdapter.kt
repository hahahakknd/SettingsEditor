package kkj.settingseditor.ui.listview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import kkj.settingseditor.MySettings
import kkj.settingseditor.R

import java.util.ArrayList

// ListViewAdapter 의 생성자
class ListViewAdapter : BaseAdapter() {
    // Adapter 에 추가된 데이터를 저장하기 위한 ArrayList
    private val listViewItemList = ArrayList<Array<String>>()

    // Adapter 에 사용되는 데이터의 개수를 리턴 (필수 구현)
    override fun getCount(): Int {
        return listViewItemList.size
    }

    // position 에 위치한 데이터를 화면에 출력하는데 사용될 View 를 리턴 (필수 구현)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView?:
            (parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
                    .inflate(R.layout.item, parent, false)
        val nameTextView = view.findViewById(R.id.item_name) as TextView
        val valueEditText = view.findViewById(R.id.item_value) as EditText
        valueEditText.setOnClickListener { Toast.makeText(parent.context, valueEditText.text, Toast.LENGTH_LONG).show() }
        val listViewItem = listViewItemList[position]

        nameTextView.text = listViewItem[0]
        valueEditText.setText(listViewItem[1])

        return view
    }

    // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
    override fun getItem(position: Int): Any {
        return listViewItemList[position]
    }

    // 아이템 데이터 추가를 위한 함수. 개발자가 원하는대로 작성 가능.
    fun addItem(key: String, value: String) {
        listViewItemList.add(arrayOf(key, value))
    }
}
