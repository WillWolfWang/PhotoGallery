package com.will.photogallery.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.will.photogallery.FlickrFetchr
import com.will.photogallery.R
import com.will.photogallery.api.FlickrApi
import com.will.photogallery.api.FlickrResponse
import com.will.photogallery.data.GalleryItem
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

        // 调用 flickrApi.fetchContents(); 并不是执行网络请求，而是返回一个
        // 代表网络请求的 Call<String> 对象。
        val flickrLiveData: LiveData<List<GalleryItem>> = FlickrFetchr().fetchPhotos()

        flickrLiveData.observe(viewLifecycleOwner, Observer {galleryItems->
            Log.e("WillWolf", "response: $galleryItems")
        })

        return view
    }

    // 伴生对象内的方法，可以使用 类.方法名 的方式调用
    companion object {
        fun newInstance(): PhotoGalleryFragment {
            return PhotoGalleryFragment()
        }
    }
}