package com.hzdq.nppvdoctorclient.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.webkit.*
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.hzdq.nppvdoctorclient.R
import com.hzdq.nppvdoctorclient.databinding.FragmentPatientBinding
import com.hzdq.nppvdoctorclient.dataclass.DataClassJump
import com.hzdq.nppvdoctorclient.H5DetailActivity
import com.hzdq.nppvdoctorclient.retrofit.URLCollection
import com.hzdq.nppvdoctorclient.util.Shp
import com.hzdq.nppvdoctorclient.util.TokenDialogUtil

class PatientFragment : Fragment() {

    private val TAG = "PatientFragment"
    private lateinit var binding :FragmentPatientBinding
    private lateinit var shp :Shp
    private var tokenDialogUtil: TokenDialogUtil? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_patient, container, false)
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
        initView()
    }

    private fun initView(){
        binding.head.content.text= "患者列表"

        binding.head.back.visibility = View.GONE

//        WebView.setWebContentsDebuggingEnabled(true)



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


        Log.d(TAG, "patientList:${URLCollection.getPatientList(shp)} ")

       
        binding.webView.loadUrl("${URLCollection.getPatientList(shp)}")

        //添加JavascriptInterface后JS可通过Android字段调用JavaAndJsCallInterface类中的任何方法
        binding.webView.addJavascriptInterface(JavaAndJsCallInterface(),"Android")



    }


    /**
     * JS要调用的Java中的类.
     */
    inner class JavaAndJsCallInterface{
        @JavascriptInterface
        fun jump(data:String){
            val dataClassJump = Gson().fromJson(data,DataClassJump::class.java)
            val intent = Intent(requireActivity(), H5DetailActivity::class.java)
            intent.putExtra("path",dataClassJump.path)
            intent.putExtra("title","患者详情")
            startActivity(intent)
            Log.d(TAG, "jump: ${dataClassJump.path}")
        }

        @JavascriptInterface
        fun tokenInvalidation(data: String){
            tokenDialogUtil?.showTokenDialog()
        }
    }

}