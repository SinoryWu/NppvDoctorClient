package com.hzdq.nppvdoctorclient.util

import java.util.regex.Pattern

object PasswordUtil {
    /**
     * 判断是否为纯字母
     * @param str
     * @return
     */
    fun isAllLetters(str: String): Boolean {
        val pattern2 = Pattern.compile("[a-zA-Z]+$")
        val matcher2 = pattern2.matcher(str)
        return matcher2.matches()
    }

    /**
     * 判断是否为纯数字
     * @param str
     * @return
     */
    fun isNumeric(str: String): Boolean {
        val pattern1 = Pattern.compile("[0-9]*")
        val matcher1 = pattern1.matcher(str)
        return matcher1.matches()
    }

}