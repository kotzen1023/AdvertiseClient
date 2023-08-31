package com.seventhmoon.advertiseclient.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.seventhmoon.advertiseclient.MainActivity


class StartupService : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent!!.action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.e("StartupService", "ACTION_BOOT_COMPLETED")
            val i = Intent(context!!.applicationContext, MainActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(i)
        }
    }
}