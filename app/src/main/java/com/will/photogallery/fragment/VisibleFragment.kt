package com.will.photogallery.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.will.photogallery.work.PollWorker

abstract class VisibleFragment: Fragment() {

    private val onShowNotification = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            Toast.makeText(context, "Got a broadcast:${intent.action}", Toast.LENGTH_SHORT).show()
        }
    }

    // 设备旋转时，fragment 的 onCreate() 和 onDestroy() 函数中
    // getActivity() 函数会返回不同的值，如果想在这两个方法中实现登记
    // 或者撤销登记，应该使用 requireActivity().getApplicationContext()
    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(PollWorker.ACTION_SHOW_NOTIFICATION)
        requireActivity().registerReceiver(onShowNotification, filter, PollWorker.PERM_PRIVATE, null)
    }

    override fun onStop() {
        super.onStop()
        requireActivity().unregisterReceiver(onShowNotification)
    }
}