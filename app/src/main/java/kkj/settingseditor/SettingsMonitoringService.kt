package kkj.settingseditor

import android.app.*
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log

class SettingsMonitoringService: Service() {
    companion object {
        private const val TAG = "SettingsMonitoringService"
        private const val FOREGROUND_ID = 1
    }

    private val mGlobalSettingsObserver = GlobalSettingsObserver()
    private class GlobalSettingsObserver : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            Log.d(TAG, "GlobalSettingsObserver, selfChange:$selfChange, uri:$uri")
        }
    }

    private val mSystemSettingsObserver = SystemSettingsObserver()
    private class SystemSettingsObserver : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            Log.d(TAG, "SystemSettingsObserver, selfChange:$selfChange, uri:$uri")
        }
    }

    private val mSecureSettingsObserver = SecureSettingsObserver()
    private class SecureSettingsObserver : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            Log.d(TAG, "SecureSettingsObserver, selfChange:$selfChange, uri:$uri")
        }
    }

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        startForeground()
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "onBind")
        registerContentObserver()
        return null
    }

    override fun onRebind(intent: Intent?) {
        Log.d(TAG, "onRebind")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "onUnbind")
        unregisterContentObserver()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
    }

    private fun startForeground() {
        val serviceName: String = SettingsMonitoringService::class.java.simpleName

        val builder: Notification.Builder = Notification.Builder(this, "default")
        builder.setSmallIcon(R.mipmap.ic_launcher)
        builder.setContentTitle(serviceName)
        builder.setContentText("Running Settings Monitoring Service")

        val notificationIntent = Intent(this, SettingsMonitoringService::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
        builder.setContentIntent(pendingIntent)

        val channel = NotificationChannel("default", serviceName, NotificationManager.IMPORTANCE_NONE)
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        startForeground(FOREGROUND_ID, builder.build())
    }

    private fun registerContentObserver() {
        contentResolver?:Log.e(TAG, "ContentResolver is null. So cannot register ContentObserver.")
        contentResolver?.registerContentObserver(Settings.Global.CONTENT_URI, true, mGlobalSettingsObserver)
        contentResolver?.registerContentObserver(Settings.System.CONTENT_URI, true, mSystemSettingsObserver)
        contentResolver?.registerContentObserver(Settings.Secure.CONTENT_URI, true, mSecureSettingsObserver)
    }

    private fun unregisterContentObserver() {
        contentResolver?:Log.e(TAG, "ContentResolver is null. So cannot unregister ContentObserver.")
        contentResolver?.unregisterContentObserver(mGlobalSettingsObserver)
        contentResolver?.unregisterContentObserver(mSystemSettingsObserver)
        contentResolver?.unregisterContentObserver(mSecureSettingsObserver)
    }
}