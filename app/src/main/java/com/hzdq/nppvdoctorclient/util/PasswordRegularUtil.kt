package com.hzdq.nppvdoctorclient.util

import android.util.Log

/**
 *Time:2023/10/10
 *Author:Sinory
 *Description:
 */
object PasswordRegularUtil {
    fun getResult(password:String):Boolean{
        val uppercaseRegex = Regex("[A-Z]")
        val lowercaseRegex = Regex("[a-z]")
        val digitRegex = Regex("\\d")
//        val symbolRegex = Regex("[@\$!%*?&.,']")
        val symbolRegex = Regex("[@\$!%*?&.,'\"(){}\\[\\]<>;:/\\\\]")
        val uppercaseMatch = uppercaseRegex.containsMatchIn(password)
        val lowercaseMatch = lowercaseRegex.containsMatchIn(password)
        val digitMatch = digitRegex.containsMatchIn(password)
        val symbolMatch = symbolRegex.containsMatchIn(password)

        val categoriesCount = listOf(uppercaseMatch, lowercaseMatch, digitMatch, symbolMatch).count { it }

        if (password.length >= 8 && categoriesCount >= 3) {
            Log.d("passwordLayout", "密码符合要求")
            return true
        } else {
            Log.d("passwordLayout", "密码不符合要求")
            return false
        }
    }
}