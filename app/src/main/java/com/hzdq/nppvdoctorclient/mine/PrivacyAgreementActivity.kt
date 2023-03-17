package com.hzdq.nppvdoctorclient.mine

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.databinding.DataBindingUtil
import com.hzdq.nppvdoctorclient.R
import com.hzdq.nppvdoctorclient.databinding.ActivityPrivacyAgreementBinding
import com.hzdq.nppvdoctorclient.retrofit.URLCollection
import com.hzdq.nppvdoctorclient.util.ActivityCollector

class PrivacyAgreementActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPrivacyAgreementBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_privacy_agreement)
        ActivityCollector.addActivity(this)

        initView()
    }


    private fun initView(){
        if (intent.getStringExtra("type").equals("privacy")){
            binding.head.content.text = "隐私协议"
        }else {
            binding.head.content.text = "用户协议"
        }
        binding.head.back.setOnClickListener {
            finish()
        }

        val webSettings: WebSettings = binding.webView.settings
        webSettings.setSupportZoom(true)
        webSettings.builtInZoomControls = true
        webSettings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
        webSettings.loadWithOverviewMode = true
        webSettings.displayZoomControls = false

        webSettings.javaScriptEnabled = true;
        //3、 加载需要显示的网页

        webSettings.domStorageEnabled = true;

        binding.webView.clearCache(true)
        binding.webView.clearFormData()
        if (intent.getStringExtra("type").equals("privacy")){
            binding.webView.loadUrl(URLCollection.PRIVACY_AGREEMENT)
        }else {
            binding.webView.loadUrl(URLCollection.USER_AGREEMENT)
        }

//        binding.webView.loadUrl("https://www.baidu.com")
        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return super.shouldOverrideUrlLoading(view, url)
            }
        }

    }

    override fun onDestroy() {
        ActivityCollector.removeActivity(this)
        super.onDestroy()
    }
}