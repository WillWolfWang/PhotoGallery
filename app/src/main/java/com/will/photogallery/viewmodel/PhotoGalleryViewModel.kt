package com.will.photogallery.viewmodel

//import androidx.lifecycle.Transformations
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.switchMap
import com.will.photogallery.FlickrFetchr
import com.will.photogallery.QueryPreferences
import com.will.photogallery.data.GalleryItem

class PhotoGalleryViewModel(private val app:Application): AndroidViewModel(app) {
    var galleryItemLiveData: LiveData<List<GalleryItem>>
    val fetchr = FlickrFetchr()
    private val mutableSearchTerm = MediatorLiveData<String>()
    // 搜索项
    val searchTerm: String
        get() = mutableSearchTerm.value ?: ""
    init {
        mutableSearchTerm.value = QueryPreferences.getStoredQuery(app)
//       新版本 Transformations.map 引用不到
//        galleryItemLiveData = FlickrFetchr().searchPhotos("planets")
        // 使用 switchMap 后，要给对象赋值
        galleryItemLiveData = mutableSearchTerm.switchMap {searchTerm ->
            if (searchTerm.isBlank()) {
                fetchr.fetchPhotos()
            } else{
                fetchr.searchPhotos(searchTerm)
            }
        }
    }

    fun fetchPhotos(query: String = "") {
        QueryPreferences.setStoredQuery(app, query)
        mutableSearchTerm.value = query
    }

    override fun onCleared() {
        super.onCleared()
        // 如果用户快速按下了返回按钮，取消网络请求
        fetchr.cancelRequestInFlight()
        Log.e("WillWolf", "onCleared")
    }
}