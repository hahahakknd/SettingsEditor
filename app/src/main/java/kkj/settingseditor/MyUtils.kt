package kkj.settingseditor

import android.util.Log
import android.view.View
import com.google.android.material.snackbar.Snackbar

class MyUtils {
    companion object {
        private const val TAG: String = "SettingsEditor.MyUtils"

        fun showSnackbarWithAction(view: View, text: String) {
            Snackbar
                .make(view, text, Snackbar.LENGTH_INDEFINITE)
                .setAction("OK"){}.show()
        }

        fun showSnackbarWithAction(view: View, text: String, action_text: String) {
            Snackbar
                .make(view, text, Snackbar.LENGTH_INDEFINITE)
                .setAction(action_text){}.show()
        }

        fun showSnackbarWithAction(
                view: View,
                text: String,
                action_text: String,
                listener: View.OnClickListener) {
            Snackbar
                .make(view, text, Snackbar.LENGTH_INDEFINITE)
                .setAction(action_text, listener).show()
        }

        fun showSnackbar(view: View, text: String) {
            Snackbar.make(view, text, Snackbar.LENGTH_INDEFINITE).show()
        }

        fun showSnackbarWithAutoDismiss(view: View, text: String) {
            Snackbar.make(view, text, Snackbar.LENGTH_LONG).show()
        }
    }
}