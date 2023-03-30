package com.hzdq.nppvdoctorclient.util

import android.view.View


/**
 * 防止按钮重复点击
 */
object ViewClickDelay {
    var hash: Int = 0
    var lastClickTime: Long = 0
    var SPACE_TIME: Long = 300

    fun View.clickDelay(clickAction: () -> Unit) {
        this.setOnClickListener {
            if (this.hashCode() != hash) {
                hash = this.hashCode()
                lastClickTime = System.currentTimeMillis()
                clickAction()
            } else {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastClickTime > SPACE_TIME) {
                    lastClickTime = System.currentTimeMillis()
                    clickAction()
                }
            }
        }
    }
}

