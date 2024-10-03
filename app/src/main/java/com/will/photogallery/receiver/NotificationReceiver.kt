package com.will.photogallery.receiver

import android.app.Activity
import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.will.photogallery.work.PollWorker

class NotificationReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.e("WillWolf", "onReceive-->")
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        val requestCode = intent.getIntExtra(PollWorker.REQUEST_CODE, 0)
        val notification: Notification? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(PollWorker.NOTIFICATION, Notification::class.java)
        } else {
            intent.getParcelableExtra(PollWorker.NOTIFICATION)
        }

        val notificationManager = NotificationManagerCompat.from(context)
        notification?.let {
            notificationManager.notify(requestCode, notification)
        }

    }
}