package com.will.photogallery

import android.graphics.Bitmap
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.OnLifecycleEvent
import java.util.concurrent.ConcurrentHashMap

/**
 * 后台下载线程
 * 声明了一个泛型 T，来确定需要使用哪些对象来识别每次下载，
 * 并确定该用下载图片更新哪个 UI 元素
 */
private const val TAG = "ThumbnailDownloader"
private const val MESSAGE_DOWNLOAD = 0
// 构造函数接收 MainHandler 以及一个函数类型参数，将下载结果 bitmap callback 回去
class ThumbnailDownloader<in T : Any>(private val responseHandler: Handler, private val onThumbnailDownloader: (T, Bitmap) -> Unit): HandlerThread(TAG) {
    private var hasQuit = false

    private lateinit var requestHandler: Handler
    private var requestMap = ConcurrentHashMap<T, String>()
    private val flickrFetchr = FlickrFetchr()

    val fragmentLifecycleObserver: LifecycleObserver = object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
        fun setup() {
            Log.e("WillWolf", "start background thread")
            start()
            looper
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun tearDown() {
            Log.e("WillWolf", "start background destroy")
            quit()
        }
    }

    var myViewLifecycleObserver = object : DefaultLifecycleObserver {

        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
            Log.e("WillWolf", "onDestroyView-->")
        }
    }

    val viewLifecycleObserver: LifecycleObserver = object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun clearQueue() {
            Log.e("WillWolf", "clear all request from queue")
            requestHandler.removeMessages(MESSAGE_DOWNLOAD)
            requestMap.clear()
        }
    }

    // 用法不对
    fun observeFragmentLifecycle(fragment: Fragment) {
        fragment.viewLifecycleOwnerLiveData.observe(fragment.viewLifecycleOwner, Observer { lifecycleOwner->
            Log.e("WillWolf", "lifecycleOwner-->" + lifecycleOwner)
            if (lifecycleOwner == null) {
                Log.e("WillWolf", "clear all request from queue")
                requestHandler.removeMessages(MESSAGE_DOWNLOAD)
                requestMap.clear()
            }
        })
    }

    override fun onLooperPrepared() {
        super.onLooperPrepared()
        requestHandler = object : Handler() {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                if (msg.what == MESSAGE_DOWNLOAD) {
                    val target = msg.obj as T
                    handleRequest(target)
                }
            }
        }
    }

    private fun handleRequest(target: T) {
        val url = requestMap.get(target) ?: return
//        Log.e("WillWolf", "handle url: $url")
        val bitmap = flickrFetchr.fetchPhoto(url) ?: return
        responseHandler.post(Runnable {
            if (requestMap[target] != url || hasQuit) {
                return@Runnable
            }
            requestMap.remove(target)
            onThumbnailDownloader(target, bitmap)
        })
    }

    // HandlerThread 退出的方法
    override fun quit(): Boolean {
        hasQuit = true
        return super.quit()
    }





    // T 标识具体哪次下载，url 参数是下载链接
    fun queueThumbnail(target: T, url: String) {
//        Log.e("WillWolf", "Got a URL: $url")
        requestMap.put(target, url)
        requestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget()
    }
}