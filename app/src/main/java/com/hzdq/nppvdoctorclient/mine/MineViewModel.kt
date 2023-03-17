package com.hzdq.nppvdoctorclient.mine

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class MineViewModel(application: Application) : AndroidViewModel(application) {

    //退出登录 show.弹出动画，close.动画消失
    val logOut = MutableLiveData("")

    //版本更新进度
    val updateProgress = MutableLiveData(0)

    val newPassword = MutableLiveData("")
    val repeatPassword = MutableLiveData("")

    /**
     * 获取版本号
     */
    fun getVerName(context: Context): String {
        var verName: String? = ""
        try {
            verName = context.packageManager.getPackageInfo(
                "com.hzdq.nppvdoctorclient", 0
            ).versionName
        } catch (e: PackageManager.NameNotFoundException) {
        }
        return "${verName}"
    }

    private val _timeCount = MutableLiveData(-1)
    val timeCount: LiveData<Int> = _timeCount

    fun setTimeCount(x:Int){
        _timeCount.value = x
    }

    //倒计时方法
    val countTime = object : CountDownTimer(60050, 1000) {
        override fun onTick(millisUntilFinished: Long) {

            setTimeCount((millisUntilFinished / 1000).toInt())

        }

        override fun onFinish() {


        }

    }
}