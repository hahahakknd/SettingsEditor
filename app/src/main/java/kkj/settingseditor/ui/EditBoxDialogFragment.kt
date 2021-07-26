package kkj.settingseditor.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import kkj.settingseditor.R

class EditBoxDialogFragment(private val fragmentManager: FragmentManager) {
    companion object {
        private const val TAG = "SettingsEditor.CardViewAdapter"
    }

    interface EditBoxListener {
        fun okListener(itemValue: String)
        fun cancelListener(itemValue: String)
    }

    class InnerDialogFragment(
            private val itemName: String,
            private val itemValue: String,
            private val listener: (itemValue: String) -> Unit
    ) : DialogFragment() {
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
            itemNameView.text = itemName

            val itemValueView = view.findViewById(R.id.edit_box_item_value) as EditText
            itemValueView.setText(itemValue)

            val okView = view.findViewById(R.id.edit_box_ok_button) as Button
            okView.setOnClickListener {
                listener.invoke(itemValueView.text.toString())
                dismiss()
            }

            val cancelView = view.findViewById(R.id.edit_box_cancel_button) as Button
            cancelView.setOnClickListener {
                dismiss()
            }

            return view
        }
    }

    fun show(itemName: String, itemValue: String, listener: (itemValue: String) -> Unit) {
        InnerDialogFragment(itemName, itemValue, listener).show(fragmentManager,"EditBox")
    }
}
