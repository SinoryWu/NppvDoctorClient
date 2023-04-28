package com.hzdq.nppvdoctorclient.services

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.hzdq.nppvdoctorclient.H5Detail2Activity
import com.hzdq.nppvdoctorclient.R
import com.hzdq.nppvdoctorclient.databinding.ActivityAddServiceBinding
import com.hzdq.nppvdoctorclient.dataclass.DataClassBack
import com.hzdq.nppvdoctorclient.dataclass.DataClassJump
import com.hzdq.nppvdoctorclient.util.ActivityCollector
import com.hzdq.nppvdoctorclient.util.TokenDialogUtil

class AddServiceActivity : AppCompatActivity() {
    private lateinit var binding:ActivityAddServiceBinding
    private val launcherActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val code = it.resultCode
        if (code == RESULT_OK){
            val data = it.data?.getStringExtra("h5data")
            val js = "javascript:onTransfer({ data: \'$data\'})"
            binding.webView.evaluateJavascript(js,null)
        }

    }
    private var tokenDialogUtil: TokenDialogUtil? = null
    override fun onDestroy() {
        ActivityCollector.removeActivity(this)
        tokenDialogUtil?.disMissTokenDialog()
        super.onDestroy()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_add_service)
        tokenDialogUtil = TokenDialogUtil(this)
        ActivityCollector.addActivity(this)
        binding.head.back.setOnClickListener {
            finish()
        }
        binding.head.content.text = "新建服务"
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


        binding.webView.loadUrl(path!!)

        //添加JavascriptInterface后JS可通过Android字段调用JavaAndJsCallInterface类中的任何方法
        binding.webView.addJavascriptInterface(JavaAndJsCallInterface(),"Android")



    }

    /**
     * JS要调用的Java中的类.
     */
    inner class JavaAndJsCallInterface{
        @JavascriptInterface
        fun jump(data:String){
            val dataClassJump = Gson().fromJson(data, DataClassJump::class.java)
            val intent = Intent(this@AddServiceActivity, H5Detail2Activity::class.java)
            intent.putExtra("title","选择已有患者")
            intent.putExtra("path",dataClassJump.path)
            launcherActivity.launch(intent)
        }

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