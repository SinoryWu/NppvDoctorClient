package com.hzdq.nppvdoctorclient.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.pdf.PrintedPdfDocument
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


/**
 *Time:2023/11/23
 *Author:Sinory
 *Description:
 */

class MyPrintAdapter(val context: Context,val  filePath: String,val fileName:String) :
    PrintDocumentAdapter() {


    private var pageHeight = 0
    private var pageWidth = 0
    private var mPdfDocument: PdfDocument? = null
    private var totalpages = 1

    private var mlist: ArrayList<Bitmap>? = null
    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes?,
        cancellationSignal: CancellationSignal,
        callback: LayoutResultCallback,
        metadata: Bundle?
    ) {
        mPdfDocument = PrintedPdfDocument(context, newAttributes!!) //创建可打印PDF文档对象
        pageHeight = PrintAttributes.MediaSize.ISO_A4.heightMils * 72 / 1000 //设置尺寸
        pageWidth = PrintAttributes.MediaSize.ISO_A4.widthMils * 72 / 1000
        if (cancellationSignal.isCanceled()) {
            callback.onLayoutCancelled()
            return
        }
        var mFileDescriptor: ParcelFileDescriptor? = null
        var pdfRender: PdfRenderer? = null
        var page: PdfRenderer.Page? = null
        try {
            mFileDescriptor =
                ParcelFileDescriptor.open(File(filePath), ParcelFileDescriptor.MODE_READ_ONLY)
            if (mFileDescriptor != null) pdfRender = PdfRenderer(mFileDescriptor)
            mlist = ArrayList()
            if (pdfRender!!.pageCount > 0) {
                totalpages = pdfRender.pageCount
                for (i in 0 until pdfRender.pageCount) {
                    page?.close()
                    page = pdfRender.openPage(i)
                    val bmp = Bitmap.createBitmap(
                        page.width * 2,
                        page.height * 2,
                        Bitmap.Config.ARGB_8888
                    )
                    page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    mlist!!.add(bmp)
                }
            }
            page?.close()
            mFileDescriptor?.close()
            if (null != pdfRender) pdfRender.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        if (totalpages > 0) {
            val builder = PrintDocumentInfo.Builder(fileName)
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(totalpages) //构建文档配置信息
            val info = builder.build()
            callback.onLayoutFinished(info, true)
        } else {
            callback.onLayoutFailed("Page count is zero.")
        }
    }

    override fun onWrite(
        pageRanges: Array<PageRange>,
        destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal,
        callback: WriteResultCallback
    ) {
        for (i in 0 until totalpages) {
            if (pageInRange(pageRanges, i)) //保证页码正确
            {
                val newPage = PageInfo.Builder(
                    pageWidth,
                    pageHeight, i
                ).create()
                val page = mPdfDocument!!.startPage(newPage) //创建新页面
                if (cancellationSignal.isCanceled()) {  //取消信号
                    callback.onWriteCancelled()
                    mPdfDocument!!.close()
                    mPdfDocument = null
                    return
                }
                drawPage(page, i) //将内容绘制到页面Canvas上
                mPdfDocument!!.finishPage(page)
            }
        }
        try {
            mPdfDocument!!.writeTo(
                FileOutputStream(
                    destination.fileDescriptor
                )
            )
        } catch (e: IOException) {
            callback.onWriteFailed(e.toString())
            return
        } finally {
            mPdfDocument!!.close()
            mPdfDocument = null
        }
        callback.onWriteFinished(pageRanges)
    }

    private fun pageInRange(pageRanges: Array<PageRange>, page: Int): Boolean {
        for (i in pageRanges.indices) {
            if (page >= pageRanges[i].start &&
                page <= pageRanges[i].end
            ) return true
        }
        return false
    }

    //页面绘制（渲染）
    private fun drawPage(page: PdfDocument.Page, pagenumber: Int) {
        val canvas: Canvas = page.canvas
        if (mlist != null) {
            val paint = Paint()
            val bitmap = mlist!![pagenumber]
            val bitmapWidth = bitmap.width
            val bitmapHeight = bitmap.height
            // 计算缩放比例
            val scale = pageWidth.toFloat() / bitmapWidth.toFloat()
            // 取得想要缩放的matrix参数
            val matrix = Matrix()
            matrix.postScale(scale, scale)
            canvas.drawBitmap(bitmap, matrix, paint)
        }
    }


}