package com.hzdq.nppvdoctorclient.fragment

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.hzdq.nppvdoctorclient.R
import com.hzdq.nppvdoctorclient.databinding.FragmentDoctorBinding
import com.hzdq.nppvdoctorclient.dataclass.DataClassJump
import com.hzdq.nppvdoctorclient.H5DetailActivity
import com.hzdq.nppvdoctorclient.MainViewModel
import com.hzdq.nppvdoctorclient.retrofit.URLCollection
import com.hzdq.nppvdoctorclient.util.Shp
import com.hzdq.nppvdoctorclient.util.TokenDialogUtil

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DoctorFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DoctorFragment : Fragment() {


    private lateinit var binding:FragmentDoctorBinding
    private lateinit var shp:Shp
    private var tokenDialogUtil: TokenDialogUtil? = null
    private lateinit var mainViewModel:MainViewModel
    val launcherActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val code = it.resultCode
        if (code == AppCompatActivity.RESULT_OK){
            mainViewModel.refreshWebView.value = 1
        }

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_doctor, container, false)
        return binding.root
    }

    override fun onDestroy() {
        tokenDialogUtil?.disMissTokenDialog()
        super.onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tokenDialogUtil = TokenDialogUtil(requireContext())
        shp = Shp(requireContext())
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        initView()
    }


    private fun initView(){
        binding.head.content.text= "医生列表"
        binding.head.back.visibility = View.GONE

        WebView.setWebContentsDebuggingEnabled(true)



        val webSettings: WebSettings = binding.webView.getSettings()

        webSettings.javaScriptEnabled = true
        webSettings.setDomStorageEnabled(true)
        webSettings.setAppCacheMaxSize(1024*1024*8);
        val appCachePath = requireContext().cacheDir.absolutePath;
        webSettings.setAppCachePath(appCachePath);
        webSettings.setAllowFileAccess(true)
        webSettings.setAppCacheEnabled(true)
        webSettings.setDatabaseEnabled(true)

        binding.webView.isVerticalScrollBarEnabled = false


        binding.webView.webViewClient = object : WebViewClient(){
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
//                val token = shp.getToken()
//                val uid = shp.getUid()!!
//                val body = BodyUser(token,uid)
//                val bodyJson:String =  Gson().toJson(body)
//                val js = "window.localStorage.setItem('androidLoginInfo','" + bodyJson + "');"
//                binding.webview.evaluateJavascript(js,null)
            }

            override fun onPageFinished(view: WebView?, url: String?) {


            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)

            }
        }

        binding.webView.loadUrl(URLCollection.getDoctorList(shp))

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
            val intent = Intent(requireActivity(), H5DetailActivity::class.java)
            intent.putExtra("title","医生详情")
            intent.putExtra("path",dataClassJump.path)
            launcherActivity.launch(intent)

        }

        @JavascriptInterface
        fun tokenInvalidation(data: String){
            tokenDialogUtil?.showTokenDialog()
        }
    }
}