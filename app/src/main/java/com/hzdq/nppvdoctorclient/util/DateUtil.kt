package com.hzdq.nppvdoctorclient.util

import java.text.SimpleDateFormat
import java.util.*

/**
 *Time:2023/3/22
 *Author:Sinory
 *Description:
 */
object DateUtil {

    fun stamp2Date(time:Long):String{
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val date: String = sdf.format(Date(time))
        return date
    }


}