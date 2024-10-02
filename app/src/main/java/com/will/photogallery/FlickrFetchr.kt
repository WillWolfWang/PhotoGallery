package com.will.photogallery

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.will.photogallery.api.FlickrApi
import com.will.photogallery.api.FlickrResponse
import com.will.photogallery.api.PhotoResponse
import com.will.photogallery.data.GalleryItem
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

/**
 * Google 推荐的仓库模式
 */
class FlickrFetchr {

    private val flickrApi: FlickrApi

    init {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.flickr.com/")
            // Retrofit 默认会把网络响应数据反序列化为 OkHttp3.ResponseBody 对象
            // 要想让 Retrofit 把网络数据反序列化为 String 类型，需要指定一个
            // 数据类型转换器，比如 Square 中的 ScalarsConverterFactory
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        flickrApi = retrofit.create(FlickrApi::class.java)
    }

    private lateinit var flickrRequest: Call<FlickrResponse>
    fun fetchPhotos(): LiveData<List<GalleryItem>> {
        val responseLiveData: MutableLiveData<List<GalleryItem>> = MutableLiveData()


        // 调用 flickrApi.fetchContents(); 并不是执行网络请求，而是返回一个
        // 代表网络请求的 Call<String> 对象。
        flickrRequest = flickrApi.fetchPhotos();

        flickrRequest.enqueue(object : Callback<FlickrResponse> {
            override fun onResponse(call: Call<FlickrResponse>, response: Response<FlickrResponse>) {
//                Log.e("WillWolf", "onResponse-->${response.body()}")
                val flickrResponse: FlickrResponse? = response.body()
                val photoResponse: PhotoResponse? = flickrResponse?.photos
                var galleryItems: List<GalleryItem> = photoResponse?.galleryItems ?: mutableListOf()
                // 过滤那些带空 urls 值的图片
                galleryItems = galleryItems.filterNot {
                    it.url.isBlank()
                }
                responseLiveData.value = galleryItems
            }

            override fun onFailure(call: Call<FlickrResponse>, t: Throwable) {
                // 可以判断 isCanceled 是不是取消了请求，导致的 onFailure
                if (flickrRequest.isCanceled) {
                    Log.e("WillWolf", "onFailure-->" + flickrRequest.isCanceled)
                } else {
                    Log.e("WillWolf", "onFailure-->" + t)
                }
            }
        })

        return responseLiveData
    }

    // 添加一个 工作 线程注解，只是提示作用
    @WorkerThread
    fun fetchPhoto(url: String): Bitmap? {
        val response: Response<ResponseBody> = flickrApi.fetchUrlBytes(url).execute()
        val bitmap: Bitmap? = response.body()?.byteStream()?.use {
            BitmapFactory.decodeStream(it)
        }

        return bitmap
    }

    fun cancelRequestInFlight() {
        Log.e("WillWolf", "flickrRequest.isInitialized-->" + ::flickrRequest.isInitialized)
        // :: 操作符是 kotlin 的反射操作符，用于获取属性或函数的引用
        // isInitialized 是一个特殊的属性，只能用于 lateinit 修饰的变量
        // 检查变量是否初始化
        if (::flickrRequest.isInitialized) {
            flickrRequest.cancel()
        }
    }
}