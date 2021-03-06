package kkj.settingseditor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kkj.settingseditor.data.MyPreferences

class BootReceiver: BroadcastReceiver() {
    companion object {
        private const val TAG: String = "SettingsEditor.BootReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if(context == null) {
            Log.e(TAG, "Context is null.")
            return
        }

        if(intent == null) {
            Log.e(TAG, "Intent is null.")
            return
        }

        val action: String = (intent.action)?:""
        if(Intent.ACTION_BOOT_COMPLETED == action) {
            if (MyPreferences.getInstance()?.serviceAutoRun == true) {
                context.startForegroundService(Intent(context, SettingsMonitoringService::class.java))
                Log.d(TAG, "SettingsMonitoringService is started.")
            }
        } else {
            Log.e(TAG, "Unknown intent action. $action")
        }
    }
}