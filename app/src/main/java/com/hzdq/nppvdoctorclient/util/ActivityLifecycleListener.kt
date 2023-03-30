package com.hzdq.nppvdoctorclient.util

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log

/**
 *Time:2023/3/30
 *Author:Sinory
 *Description:
 */
class ActivityLifecycleListener : Application.ActivityLifecycleCallbacks {
    private var activityCount = 0
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

    }

    override fun onActivityStarted(activity: Activity) {
        activityCount++
        getAppStatus()
    }

    override fun onActivityResumed(activity: Activity) {

    }

    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityStopped(activity: Activity) {
        activityCount--
        getAppStatus()
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }

    override fun onActivityDestroyed(activity: Activity) {

    }

    /**
     * 根据activityCount,判断app状态
     */
    fun getAppStatus() {
        if (activityCount == 0) {
            //App进入后台或者APP锁屏了
            Log.d("ActivityLifecycleListener", "后台: ")
        } else {
            //App进入前台
            Log.d("ActivityLifecycleListener", "前台: ")
        }
    }

}