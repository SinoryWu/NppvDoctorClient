package com.hzdq.nppvdoctorclient.util

import android.content.Context
import android.util.Log
import android.widget.Toast
import java.io.*
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.util.*

object FileUtil {
    /**
     * 判断文件是否存在
     * @param filePath
     * @return
     */
    fun fileIsExists(filePath: String): Boolean {
        try {
            val f = File(filePath)
            if (!f.exists()) {
                return false
            }
        } catch (e: Exception) {
            return false
        }
        return true
    }

    /**
     * 保存数据为txt文件
     */
    fun writeString2Txt(str: String?,path:String) {
        try {
            val fw = FileWriter(path) //SD卡中的路径
            fw.flush()
            fw.write(str)
            fw.close()
            Log.d("FileUtils", "写入完成")
        } catch (e: Exception) {
            Log.d("FileUtils", "写入错误:${e.toString()} ")
        }
    }

    /**
     * 读取文件数组内容
     */
    fun readFileToByteArray(path: String): ByteArray? {
        val file = File(path)
        if (!file.exists()) {
            return null
        }
        return try {
            val `in` = FileInputStream(file)
            val inSize = `in`.channel.size() //判断FileInputStream中是否有内容
            if (inSize == 0L) {
                return null
            }
            val buffer = ByteArray(`in`.available()) //in.available() 表示要读取的文件中的数据长度
            `in`.read(buffer) //将文件中的数据读到buffer中
            buffer
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } finally {
            try {
                System.`in`.close()
            } catch (e: IOException) {
                return null
            }
            //或IoUtils.closeQuietly(in);
        }
    }

    /**
     * 将byte数组写入文件
     */
    fun writeAndFlush(path: String, buffer: ByteArray):Boolean {

        try {
            val out = FileOutputStream(path) //指定写到哪个路径中
            val fileChannel: FileChannel = out.channel
            fileChannel.write(ByteBuffer.wrap(buffer)) //将字节流写入文件中
            fileChannel.force(true) //强制刷新
            fileChannel.close()


            return true
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return false
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * 删除文件
     */
    fun deleteFiles(filePath: String?): Boolean {
        val file = File(filePath)
        return if (file.isFile && file.exists()) {
            file.delete()
        } else false
    }


    /*
        *作者:赵星海
        *时间:2021/3/13 11:44
        *用途:日志写入文件
        */
    fun writeLog(path: String, text: String?) {
//        if (!isTest) return//如果是并非测试包 则不进行日志写入
        var textR = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date().time) + " ---" + text + "\r\n"
        try {
            var fos = FileOutputStream(path, true) //true 末尾
            fos.write(textR.toByteArray())
            fos.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}