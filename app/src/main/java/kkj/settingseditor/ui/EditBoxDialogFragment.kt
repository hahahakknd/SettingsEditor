package kkj.settingseditor.ui

import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import kkj.settingseditor.R

class EditBoxDialogFragment(name: String, value: String) : DialogFragment() {
    companion object {
        private const val TAG = "SettingsEditor.CardViewAdapter"
    }

    private val mItemName = name
    private val mItemValue = value

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_editbox, container,false)

        val itemNameView = view.findViewById(R.id.edit_box_item_name) as TextView
        itemNameView.text = mItemName

        val itemValueView = view.findViewById(R.id.edit_box_item_value) as EditText
        itemValueView.setText(mItemValue)

        val okView = view.findViewById(R.id.edit_box_ok_button) as Button
        okView.setOnClickListener {
            dismiss()
        }

        val cancelView = view.findViewById(R.id.edit_box_cancel_button) as Button
        cancelView.setOnClickListener {
            dismiss()
        }

        return view
    }
}
