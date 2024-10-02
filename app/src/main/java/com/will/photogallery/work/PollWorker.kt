package com.will.photogallery.work

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.will.photogallery.FlickrFetchr
import com.will.photogallery.MainActivity
import com.will.photogallery.NOTIFICATION_CHANNEL_ID
import com.will.photogallery.QueryPreferences
import com.will.photogallery.R
import com.will.photogallery.data.GalleryItem

class PollWorker(val context: Context, workerParams: WorkerParameters): Worker(context, workerParams) {
    override fun doWork(): Result {
        Log.e("WillWolf", "do work-->" + Thread.currentThread().name)
        val query = QueryPreferences.getStoredQuery(context)
        val lasResultId = QueryPreferences.getLastResultId(context)
        val items: List<GalleryItem> = if (query.isEmpty()) {
            FlickrFetchr().fetchPhotosRequest()
                .execute()
                .body()?.photos?.galleryItems
        } else {
            FlickrFetchr().searchPhotosRequest(query)
                .execute()
                .body()?.photos?.galleryItems
        }?: emptyList()

        if (items.isEmpty()) {
            return Result.success()
        }

        val resultId = items.first().id
        if (resultId == lasResultId) {
            Log.e("WillWolf", "got an old result:$resultId")
        } else {
            Log.e("WillWolf", "got an new result:$resultId")
            QueryPreferences.setLastResultId(context, resultId)

            val intent = MainActivity.newIntent(context)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
            val resources = context.resources
            val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setTicker(resources.getString(R.string.new_pictures_title))
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle(resources.getString(R.string.new_pictures_title))
                .setContentText(resources.getString(R.string.new_pictures_text))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            val notificationManager = NotificationManagerCompat.from(context)

            notificationManager.notify(0, notification)
        }

        return Result.success()
    }
}