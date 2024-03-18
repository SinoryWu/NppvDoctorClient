package com.hzdq.nppvdoctorclient

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.webkit.*
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.print.PrintHelper
import com.google.gson.Gson
import com.hzdq.nppvdoctorclient.databinding.ActivityH5detail2Binding
import com.hzdq.nppvdoctorclient.dataclass.DataClassBack
import com.hzdq.nppvdoctorclient.dataclass.DataClassData
import com.hzdq.nppvdoctorclient.dataclass.DataClassJump
import com.hzdq.nppvdoctorclient.retrofit.URLCollection
import com.hzdq.nppvdoctorclient.util.*
import com.permissionx.guolindev.PermissionX
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class H5Detail2Activity : AppCompatActivity() {
    private lateinit var binding: ActivityH5detail2Binding
    private var tokenDialogUtil: TokenDialogUtil? = null
    private var customView: View? = null
    private var popupwindow: PopupWindow? = null
    private var list: List<String>? = null
    private lateinit var shp: Shp

    private val launcherActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val code = it.resultCode


            if (code == RESULT_OK) {
                setResult(RESULT_OK)
                binding.webView.reload()
            } else if (it.resultCode == 20) {
                val data = it.data?.getStringExtra("h5data")
                val js = "javascript:onTransfer({ data: \'$data\'})"
                binding.webView.evaluateJavascript(js, null)
            } else if (it.resultCode == 30) {
                val data = it.data?.getStringExtra("sn")
                Log.d("asdsad", ":$data ")
                val js = "javascript:onTransfer({ data: \'$data\'})"
                binding.webView.evaluateJavascript(js, null)
            }


        }

    override fun onDestroy() {
        tokenDialogUtil?.disMissTokenDialog()
        ActivityCollector.removeActivity(this)
        super.onDestroy()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCollector.addActivity(this)
        tokenDialogUtil = TokenDialogUtil(this)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_h5detail2)
        binding.head.content.text = intent.getStringExtra("title")
        shp = Shp(this)
        binding.head.back.setOnClickListener {
            finish()
        }
        val path = intent.getStringExtra("path")
        val webSettings: WebSettings = binding.webView.getSettings()

        webSettings.javaScriptEnabled = true
        webSettings.setDomStorageEnabled(true)
        webSettings.setAppCacheMaxSize(1024 * 1024 * 8);
        val appCachePath = this.cacheDir.absolutePath;
        webSettings.setAppCachePath(appCachePath);
        webSettings.setAllowFileAccess(true)
        webSettings.setAppCacheEnabled(true)
        webSettings.setDatabaseEnabled(true)
        webSettings.allowFileAccessFromFileURLs = true
        webSettings.allowUniversalAccessFromFileURLs = true



        binding.webView.isVerticalScrollBarEnabled = false


        binding.webView.loadUrl(URLCollection.H5_BASE_URL + path)
        //添加JavascriptInterface后JS可通过Android字段调用JavaAndJsCallInterface类中的任何方法
        binding.webView.addJavascriptInterface(JavaAndJsCallInterface(), "Android")

        binding.head.more.setOnClickListener {
            if (popupwindow == null) {
                initPopupWindowView()
                popupwindow?.showAsDropDown(
                    it,
                    SizeUtil.dip2px(this, -56f),
                    SizeUtil.dip2px(this, -14f)
                )

            }
        }


    }

    /**
     * JS要调用的Java中的类.
     */
    inner class JavaAndJsCallInterface {
        @JavascriptInterface
        fun showMenu(data: String) {
            val content = data.replace("\"", "").replace("[", "").replace("]", "")
            list = content.split(",")
            if (content.equals("") || content == null) {
                runOnUiThread {
                    binding.head.more.visibility = View.GONE
                }
            } else {
                runOnUiThread {
                    binding.head.more.visibility = View.VISIBLE
                }
            }

        }

        @JavascriptInterface
        fun back(data: String) {
            if (data.equals("undefined") || data == null || data.equals("") || data.equals("null")) {
                setResult(RESULT_OK)
                finish()
            } else {
                val dataClassBack = Gson().fromJson(data, DataClassBack::class.java)
                val intent = Intent()
                intent.putExtra("h5data", dataClassBack.data)

                if (dataClassBack.path == "/server/updateStatus") {
                    setResult(20, intent)
                } else {
                    setResult(RESULT_OK, intent)
                }
                finish()
            }
        }

        @JavascriptInterface
        fun beforeRefresh(data: String) {

            setResult(RESULT_OK)
        }

        @JavascriptInterface
        fun jump(data: String) {
            val dataClassJump = Gson().fromJson(data, DataClassJump::class.java)
            var intent1 = Intent(this@H5Detail2Activity, H5DetailActivity::class.java)
            if (intent.getStringExtra("title").equals("医生详情") || intent.getStringExtra("title")
                    .equals("患者详情")
            ) {
                intent1.putExtra("title", "服务详情")
            } else {
                intent1.putExtra("title", "更新状态")
            }
            intent1.putExtra("path", dataClassJump.path)
            if (dataClassJump.path!!.contains("/server/sku-list")) {
                intent1.putExtra("title", "选择发货内容")
                launcherActivity.launch(intent1)
            } else {
                launcherActivity.launch(intent1)
            }

        }

        @JavascriptInterface
        fun scanQRCode(data: String){
            permission()
        }

        @JavascriptInterface
        fun sendImageData(data: String) {

            val dataClass = Gson().fromJson(data, DataClassData::class.java)

//            ImageUtil.doPrintPictures(dataClass.data,this@H5Detail2Activity)

//            ImageUtil.saveBitmapFile(dataClass.data,this@H5Detail2Activity)
//            ImageUtil.onPrintImg(this@H5Detail2Activity)
            ImageUtil.saveBitmapForPdf(dataClass.data, this@H5Detail2Activity)
            ImageUtil.onPrintImg(this@H5Detail2Activity)
        }


    }


    fun permission() {
        CoroutineScope(Dispatchers.Main).launch {
            PermissionX.init(this@H5Detail2Activity).permissions(
                Manifest.permission.CAMERA,
            ).request { allGrand, _, _ ->

                if (allGrand) {
                    val intent1 = Intent(this@H5Detail2Activity, ScanActivity::class.java)

                    launcherActivity.launch(intent1)
                } else {
                    ToastUtil.showToast(this@H5Detail2Activity, "未打开相应权限")
                }

            }

        }


    }

    fun initPopupWindowView() {
        // // 获取自定义布局文件pop.xml的视图

        customView = layoutInflater.inflate(
            R.layout.layout_bubble,
            null, false
        )
//        customView?.systemUiVisibility = HideUI(requireActivity()).uiOptions()
        // 创建PopupWindow实例,280,160分别是宽度和高度
        popupwindow = PopupWindow(
            customView,
            SizeUtil.dip2px(this, 98f),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        val update = customView?.findViewById<TextView>(R.id.bubble_update)
        val refund = customView?.findViewById<TextView>(R.id.bubble_refund)
        for (i in 0 until list!!.size) {
            if (list!![i].equals("1")) {
                update!!.visibility = View.VISIBLE
            }
            if (list!![i].equals("2")) {
                refund!!.visibility = View.VISIBLE
            }
        }
        update?.setOnClickListener {
            val js = "javascript:onMenu({ type: \'1\'})"
            binding.webView.evaluateJavascript(js, null)
            if (popupwindow != null && popupwindow!!.isShowing()) {
                popupwindow!!.dismiss()
                popupwindow = null
            }
        }
        refund?.setOnClickListener {
            val js = "javascript:onMenu({ type: \'2\'})"
            binding.webView.evaluateJavascript(js, null)
            if (popupwindow != null && popupwindow!!.isShowing()) {
                popupwindow!!.dismiss()
                popupwindow = null
            }
        }

        popupwindow?.setOutsideTouchable(true)
        popupwindow?.setFocusable(true)
        popupwindow?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        customView?.setOnTouchListener(View.OnTouchListener { v, event ->
            if (popupwindow != null && popupwindow!!.isShowing()) {
                popupwindow!!.dismiss()
                popupwindow = null
            }
            true
        })
        popupwindow?.setOnDismissListener(PopupWindow.OnDismissListener {
            popupwindow = null
        })
    }
}


