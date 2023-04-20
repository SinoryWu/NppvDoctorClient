package com.hzdq.nppvdoctorclient.services

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.hzdq.nppvdoctorclient.R
import com.hzdq.nppvdoctorclient.databinding.ActivityH5detail2Binding
import com.hzdq.nppvdoctorclient.dataclass.DataClassBack
import com.hzdq.nppvdoctorclient.retrofit.URLCollection
import com.hzdq.nppvdoctorclient.util.ActivityCollector
import com.hzdq.nppvdoctorclient.util.TokenDialogUtil

class H5Detail2Activity : AppCompatActivity() {
    private lateinit var binding: ActivityH5detail2Binding
    private var tokenDialogUtil: TokenDialogUtil? = null
    override fun onDestroy() {
        tokenDialogUtil?.disMissTokenDialog()
        ActivityCollector.removeActivity(this)
        super.onDestroy()

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCollector.addActivity(this)
        tokenDialogUtil = TokenDialogUtil(this)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_h5detail2)
        binding.head.content.text = intent.getStringExtra("title")

        binding.head.back.setOnClickListener {
            finish()
        }
        val path = intent.getStringExtra("path")
        val webSettings: WebSettings = binding.webView.getSettings()

        webSettings.javaScriptEnabled = true
        webSettings.setDomStorageEnabled(true)
        webSettings.setAppCacheMaxSize(1024*1024*8);
        val appCachePath = this.cacheDir.absolutePath;
        webSettings.setAppCachePath(appCachePath);
        webSettings.setAllowFileAccess(true)
        webSettings.setAppCacheEnabled(true)
        webSettings.setDatabaseEnabled(true)

        binding.webView.isVerticalScrollBarEnabled = false


        binding.webView.loadUrl(URLCollection.H5_BASE_URL+path)
        //添加JavascriptInterface后JS可通过Android字段调用JavaAndJsCallInterface类中的任何方法
        binding.webView.addJavascriptInterface(JavaAndJsCallInterface(),"Android")

    }

    /**
     * JS要调用的Java中的类.
     */
    inner class JavaAndJsCallInterface{
        @JavascriptInterface
        fun back(data:String){
            if (data.equals("undefined") || data == null || data.equals("") || data.equals("null")){
                setResult(RESULT_OK)
                finish()
            }else {
                val dataClassBack = Gson().fromJson(data, DataClassBack::class.java)
                val intent = Intent()
                intent.putExtra("h5data",dataClassBack.data)
                setResult(RESULT_OK,intent)
                finish()
            }

        }

        @JavascriptInterface
        fun tokenInvalidation(data: String){
            tokenDialogUtil?.showTokenDialog()
        }
    }
}