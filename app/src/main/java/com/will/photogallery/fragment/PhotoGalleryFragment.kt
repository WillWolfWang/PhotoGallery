package com.will.photogallery.fragment

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.will.photogallery.FlickrFetchr
import com.will.photogallery.R
import com.will.photogallery.ThumbnailDownloader
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
    private var isLayout = false
    private lateinit var thumbnailDownloader: ThumbnailDownloader<PhotoGalleryViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val responseHandler = Handler(Looper.getMainLooper())

        thumbnailDownloader = ThumbnailDownloader(responseHandler){photoHolder, bitmap ->
            val drawable = BitmapDrawable(resources, bitmap)
            photoHolder.bindImage(drawable)
        }
        lifecycle.addObserver(thumbnailDownloader.fragmentLifecycleObserver)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_photo_gallery, container, false)

        rv = view.findViewById(R.id.rv_photoView)
        rv.apply {
//            layoutManager = GridLayoutManager(requireContext(), 3)
        }

        rv.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (!isLayout) {
                    isLayout = true

                    val width = rv.width
                    Log.e("WillWolf", "width-->" + width)
                    val gridWidth = 360;
                    val num = width / gridWidth
                    rv.layoutManager = GridLayoutManager(requireContext(), num)
                    rv.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }


            }

        })

        viewMode = ViewModelProvider(this).get(PhotoGalleryViewModel::class.java)
        // 调用 flickrApi.fetchContents(); 并不是执行网络请求，而是返回一个
        // 代表网络请求的 Call<String> 对象。
        viewLifecycleOwner.lifecycle.addObserver(thumbnailDownloader.viewLifecycleObserver)

        // 保留 PhotoGalleryFragment 让它和用户看得到的 fragment 生命周期一致
        // 配置发生变化时，fragment 不会重建
        retainInstance = true

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

    override fun onDestroyView() {
        super.onDestroyView()
        viewLifecycleOwner.lifecycle.removeObserver(thumbnailDownloader.viewLifecycleObserver)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(thumbnailDownloader.fragmentLifecycleObserver)
    }

    // 伴生对象内的方法，可以使用 类.方法名 的方式调用
    companion object {
        fun newInstance(): PhotoGalleryFragment {
            return PhotoGalleryFragment()
        }
    }

    private class PhotoGalleryViewHolder(val ivItem: ImageView): RecyclerView.ViewHolder(ivItem) {
        // 定义了一个 函数类型的变量
        // 基本语法 val/var 变量名: (参数类型列表) -> 返回类型 = 函数体或函数引用
        // 参数类型用括号括起来
        // 返回类型用 -> 箭头后跟返回类型，空返回值用 Unit，可空类型使用 ?
        // 可以指定接收者类型，格式为 接收者类型.(参数类型列表)->返回类型

        // 无参，无返回值
        val sampleAction: () -> Unit = { println("Hello") }
        // 有参，有返回值
        val sum : (Int, Int) -> Int = { a, b -> a + b }
        // 可空函数类型
        val nullableFul: ((String) -> Int)? = null
        // 带接收者的函数类型
        val stringOptions: String.() -> Int = {
            // 这里的 this 是 String
            this.length
        }
        // 多个参数，使用类型别名
//        typealias TriFunction<A, B, C, R> = (A, B, C) -> R
//        val triFunc: TriFunction<Int, String, Boolean, Double> = { a, b, c -> 1.0 }
        val triFunc: (Int, String, Boolean) -> Double = { a, b, c -> 1.0 }
        // 使用 lambda 表达式赋值
        val greet: (String) -> String = {name ->
            "hello $name"
        }
        // 使用函数体引用
        fun isEvent(n: Int): Boolean {
            return n % 2 == 0
        }
        val predicate: (Int) -> Boolean = ::isEvent

        // 该方法就是函数体引用方式
        val bindImage: (Drawable) -> Unit = ivItem::setImageDrawable
        // 和下面的方法等效
//        fun bindImage(drawable: Drawable) {
//            ivItem.setImageDrawable(drawable)
//        }

        fun bindGalleryItem(galleryItem: GalleryItem) {
            Glide.with(ivItem).load(galleryItem.url).placeholder( R.drawable.bill_up_close).into(ivItem)
        }
    }

    private inner class PhotoAdapter(private val galleryItems: List<GalleryItem>): RecyclerView.Adapter<PhotoGalleryViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoGalleryViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_photo_gallery, parent, false)
            return PhotoGalleryViewHolder(view as ImageView)
        }

        override fun getItemCount(): Int {
            return galleryItems.size
        }

        override fun onBindViewHolder(holder: PhotoGalleryViewHolder, position: Int) {
//            val placeHolder: Drawable = ContextCompat.getDrawable(requireContext(), R.drawable.bill_up_close) ?: ColorDrawable();
//            holder.bindImage(placeHolder)
//            val galleryItem = galleryItems.get(position)
//            thumbnailDownloader.queueThumbnail(holder, galleryItem.url)
            holder.bindGalleryItem(galleryItems.get(position))
        }

    }
}