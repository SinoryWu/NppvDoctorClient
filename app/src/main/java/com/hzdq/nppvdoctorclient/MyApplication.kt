package com.hzdq.nppvdoctorclient

import android.app.Activity
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.hzdq.nppvdoctorclient.util.ActivityLifecycleListener
import com.hzdq.nppvdoctorclient.util.CrashHandlers
import com.liulishuo.filedownloader.FileDownloader


class MyApplication  : Application() {
    private var isBackground = true


    companion object {
        private var application: MyApplication? = null
    }


    override fun onCreate() {
        super.onCreate()
        application = this
//        CrashCat.getInstance(applicationContext,"${getExternalFilesDir("")}","crash.log").start()
        // 不耗时，做一些简单初始化准备工作，不会启动下载进程
        FileDownloader.setupOnApplicationOnCreate(application)

        CrashHandlers.getInstance().init(this)
        registerActivityLifecycleCallbacks(ActivityLifecycleListener())
//        listenForForeground();
//        listenForScreenTurningOff();
    }

    //手机息屏
    private fun listenForScreenTurningOff() {
        val screenStateFilter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d("myApplication", "锁屏 ")
                isBackground = true
                notifyBackground()
            }
        }, screenStateFilter)
    }


    //应用切换至前台
    private var activityCount = 0
    private fun listenForForeground() {
        registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {
                Log.d("myApplication", "前台 ")
                if (isBackground) {
                    isBackground = false
                }
            }

            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }

    //应用切换至后台
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            isBackground = true
            Log.d("myApplication", "后台 ")
            notifyBackground()
        }
    }

    private fun notifyForeground() {
        // This is where you can notify listeners, handle session tracking, etc
    }


    private fun notifyBackground() {
        // This is where you can notify listeners, handle session tracking, etc

    }

    fun isBackground(): Boolean {
        return isBackground
    }

}