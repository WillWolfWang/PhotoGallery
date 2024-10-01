package com.will.photogallery.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.will.photogallery.R
import com.will.photogallery.api.FlickrApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

class PhotoGalleryFragment: Fragment() {
    private lateinit var rv: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_photo_gallery, container, false)

        rv = view.findViewById(R.id.rv_photoView)
        rv.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
        }

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://www.flickr.com/")
            // Retrofit 默认会把网络响应数据反序列化为 OkHttp3.ResponseBody 对象
            // 要想让 Retrofit 把网络数据反序列化为 String 类型，需要指定一个
            // 数据类型转换器，比如 Square 中的 ScalarsConverterFactory
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()

        val flickrApi: FlickrApi = retrofit.create(FlickrApi::class.java)

        // 调用 flickrApi.fetchContents(); 并不是执行网络请求，而是返回一个
        // 代表网络请求的 Call<String> 对象。
        val flickrHomePageRequest: Call<String> = flickrApi.fetchContents();

        flickrHomePageRequest.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                Log.e("WillWolf", "onResponse-->${response.body()}")
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e("WillWolf", "onFailure-->" + t)
            }

        })

//        flickrHomePageRequest.execute()

        return view
    }

    // 伴生对象内的方法，可以使用 类.方法名 的方式调用
    companion object {
        fun newInstance(): PhotoGalleryFragment {
            return PhotoGalleryFragment()
        }
    }
}