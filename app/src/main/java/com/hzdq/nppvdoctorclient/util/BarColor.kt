package com.hzdq.nppvdoctorclient.util

import android.app.Activity
import android.graphics.Color
import android.view.WindowManager

object BarColor {
    fun setBarColor(activity: Activity, color: String) {
        val window = activity.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.parseColor(color)
    }
}