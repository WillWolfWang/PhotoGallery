package com.will.photogallery.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.will.photogallery.FlickrFetchr
import com.will.photogallery.R
import com.will.photogallery.api.FlickrApi
import com.will.photogallery.api.FlickrResponse
import com.will.photogallery.data.GalleryItem
import com.will.photogallery.viewmodel.PhotoGalleryViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

class PhotoGalleryFragment: Fragment() {
    private lateinit var rv: RecyclerView
    private lateinit var viewMode: PhotoGalleryViewModel
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_photo_gallery, container, false)

        rv = view.findViewById(R.id.rv_photoView)
        rv.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
        }

        viewMode = ViewModelProvider(this).get(PhotoGalleryViewModel::class.java)
        // 调用 flickrApi.fetchContents(); 并不是执行网络请求，而是返回一个
        // 代表网络请求的 Call<String> 对象。



        return view
    }

    // 在 onViewCreate 函数里开始观察，可以保证 UI 部件和其他关联对象随时做好
    // 响应准备。保证 PhotoGalleryFragment 重新被关联，且其视图重建后
    // LiveData 订阅能重新添加回来
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewMode.galleryItemLiveData.observe(viewLifecycleOwner, Observer {galleryItems->
//            Log.e("WillWolf", "response: $galleryItems")
            rv.adapter = PhotoAdapter(galleryItems)
        })
    }

    // 伴生对象内的方法，可以使用 类.方法名 的方式调用
    companion object {
        fun newInstance(): PhotoGalleryFragment {
            return PhotoGalleryFragment()
        }
    }

    private class PhotoGalleryViewHolder(tvItem: TextView): RecyclerView.ViewHolder(tvItem) {
        // 定义了一个 lamada
        val bindTitle: (CharSequence) -> Unit = tvItem::setText
    }

    private class PhotoAdapter(private val galleryItems: List<GalleryItem>): RecyclerView.Adapter<PhotoGalleryViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoGalleryViewHolder {
            val textView = TextView(parent.context)
            return PhotoGalleryViewHolder(textView)
        }

        override fun getItemCount(): Int {
            return galleryItems.size
        }

        override fun onBindViewHolder(holder: PhotoGalleryViewHolder, position: Int) {
            holder.bindTitle(galleryItems.get(position).title)
        }

    }
}