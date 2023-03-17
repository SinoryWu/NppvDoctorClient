package com.hzdq.nppvdoctorclient

import android.app.Application
import com.hzdq.nppvdoctorclient.util.CrashHandlers
import com.liulishuo.filedownloader.FileDownloader

class MyApplication  : Application() {
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
    }

}