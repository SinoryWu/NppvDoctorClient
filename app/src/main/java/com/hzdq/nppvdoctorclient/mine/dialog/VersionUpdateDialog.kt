package com.hzdq.nppvdoctorclient.mine.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.hzdq.nppvdoctorclient.R
import com.hzdq.nppvdoctorclient.mine.MineViewModel

class VersionUpdateDialog(context: Context, val mineViewModel: MineViewModel, val lifecycleOwner: LifecycleOwner):
    Dialog(context, R.style.CustomDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_update_version)
        val progress = findViewById<ProgressBar>(R.id.dialog_spo2_version_update_progressBar_horizontal)
        val percent = findViewById<TextView>(R.id.dialog_spo2_version_update_percent)
        mineViewModel.updateProgress.observe(lifecycleOwner, Observer {
            progress.progress = it
            percent.text = "${it}%"

        })
    }
}