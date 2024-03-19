package com.seventhmoon.advertiseclient.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.seventhmoon.advertiseclient.MainActivity


class BootReceiver  : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {



        if (intent!!.action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.e(mTag, "onReceive: boot received ${intent.action}")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val serviceIntent = Intent(context, AppStartService::class.java)
                ContextCompat.startForegroundService(context as Context, serviceIntent)
            } else {
                val i = Intent(context!!.applicationContext, MainActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(i)
            }
            /*


             */

        }
    }

    companion object {
        private const val mTag = "BootReceiver"
    }
}