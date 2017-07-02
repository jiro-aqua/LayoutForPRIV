package jp.gr.aqua.layoutforkeyone

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, i: Intent) {
        val intent = Intent(context, MonitorService::class.java)
        context.startService(intent)

        Log.e("==================>", "BOOT COMPLETED!")
    }
}
