package com.hzdq.nppvdoctorclient.util

/**
 *Time:2023/3/27
 *Author:Sinory
 *Description:
 */
object EnglishUtil {
    fun isEnglishAlphabet(str: String): Boolean {
        for (c in str) {
            if (!c.isLetter() || !c.isEnglishLetter()) {
                return false
            }
        }
        return true
    }

    fun Char.isEnglishLetter(): Boolean {
        return this in 'a'..'z' || this in 'A'..'Z'
    }
}