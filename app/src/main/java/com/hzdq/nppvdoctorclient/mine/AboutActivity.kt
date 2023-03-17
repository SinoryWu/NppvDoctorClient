package com.hzdq.nppvdoctorclient.mine

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.hzdq.nppvdoctorclient.R
import com.hzdq.nppvdoctorclient.databinding.ActivityAboutBinding
import com.hzdq.nppvdoctorclient.mine.dialog.VersionUpdateDialog
import com.hzdq.nppvdoctorclient.util.ActivityCollector
import com.hzdq.nppvdoctorclient.util.ToastUtil
import com.hzdq.nppvdoctorclient.util.TokenDialogUtil
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloadListener
import com.liulishuo.filedownloader.FileDownloader
import java.io.File

class AboutActivity : AppCompatActivity() {
    private lateinit var binding:ActivityAboutBinding
    private lateinit var mineViewModel:MineViewModel
    private var tokenDialogUtil: TokenDialogUtil? = null
    private var versionUpdateDialog:VersionUpdateDialog? = null
    override fun onDestroy() {
        versionUpdateDialog?.dismiss()
        tokenDialogUtil?.disMissTokenDialog()
        ActivityCollector.removeActivity(this)
        super.onDestroy()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCollector.addActivity(this)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_about)
        mineViewModel = ViewModelProvider(this).get(MineViewModel::class.java)
        initView()
    }


    private fun initView(){
        binding.versionUpdate.content.text  = "版本更新"

        binding.cancellation.content.text = "注销账号"

        binding.version.text = "Version${mineViewModel.getVerName(this)}"

        binding.cancellation.layout.setOnClickListener {
            startActivity(Intent(this,CancelledAccountActivity::class.java))
        }

        binding.head.content.text = "关于"
        binding.head.back.setOnClickListener {
            finish()
        }
    }


    /**
     * 下载更新包到本地
     * @param url
     * @param path
     */
    private fun DownLoadVersionAppFile(url: String, path: String) {
        versionUpdateDialog = VersionUpdateDialog(this,mineViewModel,this)
        versionUpdateDialog?.show()
        versionUpdateDialog?.setCanceledOnTouchOutside(false)
        FileDownloader.getImpl().create(url).setPath(path)
            .setListener(object : FileDownloadListener() {
                override fun pending(task: BaseDownloadTask, soFarBytes: Int, totalBytes: Int) {

                }
                override fun progress(task: BaseDownloadTask, soFarBytes: Int, totalBytes: Int) {

                    mineViewModel.updateProgress.value = ((soFarBytes.toDouble()/totalBytes.toDouble()).toDouble()*100).toInt()
                }
                override fun completed(task: BaseDownloadTask) {
                    mineViewModel.updateProgress.value = 100

                    if (fileIsExists(path)){
                        installApk(path)
                        ActivityCollector.finishAll()
                    }
                }

                override fun paused(task: BaseDownloadTask, soFarBytes: Int, totalBytes: Int) {

                }
                override fun error(task: BaseDownloadTask, e: Throwable) {
                    mineViewModel.updateProgress.value = 100
                    ToastUtil.showToast(this@AboutActivity, "更新包下载失败")
                }

                override fun warn(task: BaseDownloadTask) {

                }
            }).start()
    }


    /**
     * 判断文件是否存在
     * @param filePath
     * @return
     */
    private fun fileIsExists(filePath: String): Boolean {
        try {
            val f = File(filePath)
            if (!f.exists()) {
                return false
            }
        } catch (e: Exception) {
            return false
        }
        return true
    }


    /**
     * 安装apk
     */
    private fun installApk(fileSavePath: String) {
        val file = File(fileSavePath)

        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val data: Uri
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { //判断版本大于等于7.0
            // 通过FileProvider创建一个content类型的Uri
            data =
                FileProvider.getUriForFile(this, "$packageName.fileProvider", file)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // 给目标应用一个临时授权
        } else {
            data = Uri.fromFile(file)
        }
        intent.setDataAndType(data, "application/vnd.android.package-archive")
        startActivity(intent)
    }
}