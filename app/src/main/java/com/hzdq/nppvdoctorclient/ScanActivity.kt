package com.hzdq.nppvdoctorclient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.hzdq.nppvdoctorclient.databinding.ActivityScanBinding
import com.hzdq.nppvdoctorclient.util.ActivityCollector
import com.hzdq.nppvdoctorclient.util.BarColor
import com.hzdq.nppvdoctorclient.util.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView

class ScanActivity : AppCompatActivity() {
    private var capture: CaptureManager? = null
    private var barcodeScannerView: DecoratedBarcodeView? = null
    private lateinit var mainViewModel: MainViewModel
    private lateinit var binding:ActivityScanBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCollector.addActivity(this)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_scan)
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        barcodeScannerView = initializeContent()
        capture = CaptureManager(this, barcodeScannerView,mainViewModel)
        capture?.onDestroy()
        capture?.initializeFromIntent(intent, null)
        capture?.decode()
        barcodeScannerView?.resume()
        BarColor.setBarColor(this,"#ffffff")
        mainViewModel.isScanSn.observe(this, Observer {
            if (it == 1){
                val intent = Intent()
                intent.putExtra("sn",mainViewModel.scanSn.value)
                setResult(30,intent)
                finish()
            }

        })

        binding.head.content.text = "扫描识别码"
        binding.head.back.setOnClickListener {
            finish()
        }

    }

    override fun onDestroy() {
        barcodeScannerView = null
        capture?.onDestroy()
        ActivityCollector.removeActivity(this)
        super.onDestroy()
    }

    override fun onResume() {
        capture?.onResume()
        super.onResume()
    }

    /**
     * Override to use a different layout.
     *
     * @return the DecoratedBarcodeView
     */
    fun initializeContent(): DecoratedBarcodeView {

        return findViewById<View>(R.id.dbv) as DecoratedBarcodeView
    }
}