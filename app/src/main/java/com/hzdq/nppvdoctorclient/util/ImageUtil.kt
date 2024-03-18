package com.hzdq.nppvdoctorclient.util

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.print.PrintAttributes
import android.print.PrintManager
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import androidx.print.PrintHelper
import com.hzdq.nppvdoctorclient.adapter.MyPrintAdapter
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


/**
 *Time:2024/3/11
 *Author:Sinory
 *Description:
 */
object ImageUtil {
    fun convertBase64ToPic(base64: String): Bitmap? {
        var value = base64
        if (base64.contains(",")) {
            value = base64.split(",")[1]
        }
        val decode = Base64.decode(value, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decode, 0, decode.size)
    }

    /**系统打印机--打印图片 */
    fun doPrintPictures(base64: String,context: Context) {
        val bitmap = convertBase64ToPic(base64)
        val photoPrinter = PrintHelper(context)
        photoPrinter.scaleMode = PrintHelper.SCALE_MODE_FIT
        //        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.droids);              //本地图片
        // 设置自定义打印选项（设置左右边距）
        val printAttributes = PrintAttributes.Builder()
            .setMinMargins(PrintAttributes.Margins(2, 0, 2, 0))
            .build()

        bitmap?.let { photoPrinter.printBitmap("image.jpg", it) }

    }


     fun onPrintImg(context: Context) {
        val printManager = context.getSystemService(AppCompatActivity.PRINT_SERVICE) as PrintManager
        val builder = PrintAttributes.Builder()
        builder.setColorMode(PrintAttributes.COLOR_MODE_COLOR)
        printManager.print("img print", MyPrintAdapter(context, "${context.cacheDir}/print.pdf","print.pdf"), builder.build())
    }


    fun saveBitmapFile(base64: String,context: Context) {
        val bitmap = convertBase64ToPic(base64)
        val file = File("${context.cacheDir}/print.pdf") // 将要保存图片的路径
        try {
            val bos = FileOutputStream(file).buffered()
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, bos)
            bos.flush()
            bos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun saveBitmapForPdf(base64: String,context: Context): File? {
        val bitmap = convertBase64ToPic(base64)
        val doc = PdfDocument()
        val pageWidth = PrintAttributes.MediaSize.ISO_A4.widthMils * 72 / 1000
        val scale = pageWidth.toFloat() / bitmap?.width?.toFloat()!!
        val pageHeight = (bitmap.height * scale).toInt()
        val matrix = Matrix()
        matrix.postScale(scale, scale)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val newPage = PageInfo.Builder(pageWidth, pageHeight, 0).create()
        val page = doc.startPage(newPage)
        val canvas: Canvas = page.canvas
        canvas.drawBitmap(bitmap, matrix, paint)
        doc.finishPage(page)
        val file = File(context.cacheDir, "print.pdf")
        var outputStream: FileOutputStream? = null
        try {
            outputStream = FileOutputStream(file)
            doc.writeTo(outputStream)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            doc.close()
            try {
                outputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return file
    }
}