package com.will.photogallery.work

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.will.photogallery.FlickrFetchr
import com.will.photogallery.QueryPreferences
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
        }

        return Result.success()
    }
}