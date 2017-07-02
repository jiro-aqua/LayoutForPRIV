package jp.gr.aqua.layoutforkeyone

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.preference.PreferenceManager

class MonitorService : Service() {

    //internal var mNotificationManager by lazy {getSystemService(NotificationManager.class)}
    internal var NOTIFICATION_ID = 0x12345678

    private val sharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        val device = sharedPreferences.getString(MainActivity.KEY_RESET_DEVICE,null)
        val ime = sharedPreferences.getString(MainActivity.KEY_RESET_IME,null)
        startForeground(NOTIFICATION_ID, getNotification(this, ime))
        return Service.START_STICKY
    }

    private fun getNotification(context: Context, ime : String ): Notification {

        val intent = Intent(this, ShowKeyboardLayoutScreenActivity::class.java)

        val contextIntent = PendingIntent.getActivity(context, 0, intent, 0)

        val notification = Notification.Builder(this)
                .setContentIntent(contextIntent)
                .setSmallIcon(R.drawable.ic_empty)
                .setContentTitle(getString(R.string.keyboard_layouts_label))
                .setContentText("${getString(R.string.label_reset)}${ime}")
                .setPriority(Notification.PRIORITY_MIN)
                .build()

        return notification
    }

}
