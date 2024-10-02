package com.will.photogallery.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.will.photogallery.FlickrFetchr
import com.will.photogallery.data.GalleryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PhotoGalleryViewModel: ViewModel() {
    var galleryItemLiveData: LiveData<List<GalleryItem>>
    val fetchr = FlickrFetchr()
    init {
        galleryItemLiveData = fetchr.searchPhotos("planets")
    }

    override fun onCleared() {
        super.onCleared()
        // 如果用户快速按下了返回按钮，取消网络请求
        fetchr.cancelRequestInFlight()
        Log.e("WillWolf", "onCleared")
    }
}