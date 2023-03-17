package com.hzdq.nppvdoctorclient.util

import android.content.Context

object SizeUtil {
    fun dip2px(context: Context, dpValue: Float):Int{
        val scale: Float = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }
}