package com.will.photogallery.viewmodel

//import androidx.lifecycle.Transformations
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.will.photogallery.FlickrFetchr
import com.will.photogallery.data.GalleryItem

class PhotoGalleryViewModel: ViewModel() {
    var galleryItemLiveData: LiveData<List<GalleryItem>>
    val fetchr = FlickrFetchr()
    private val mutableSearchTerm = MediatorLiveData<String>()
    init {
        mutableSearchTerm.value = "planets"
//       新版本 Transformations.map 引用不到
//        galleryItemLiveData = FlickrFetchr().searchPhotos("planets")
        // 使用 switchMap 后，要给对象赋值
        galleryItemLiveData = mutableSearchTerm.switchMap {searchTerm ->
            fetchr.searchPhotos(searchTerm)
        }
    }

    fun fetchPhotos(query: String = "") {
        mutableSearchTerm.value = query
    }

    override fun onCleared() {
        super.onCleared()
        // 如果用户快速按下了返回按钮，取消网络请求
        fetchr.cancelRequestInFlight()
        Log.e("WillWolf", "onCleared")
    }
}