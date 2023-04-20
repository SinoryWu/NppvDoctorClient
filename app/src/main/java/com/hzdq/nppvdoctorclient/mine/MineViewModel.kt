package com.hzdq.nppvdoctorclient.mine

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hzdq.nppvdoctorclient.body.BodyModifyPassword
import com.hzdq.nppvdoctorclient.body.BodySendMsg
import com.hzdq.nppvdoctorclient.body.BodyVersion
import com.hzdq.nppvdoctorclient.dataclass.*
import com.hzdq.nppvdoctorclient.retrofit.RetrofitSingleton
import com.hzdq.nppvdoctorclient.util.Shp
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class MineViewModel(application: Application) : AndroidViewModel(application) {

    val retrofitSingleton = RetrofitSingleton.getInstance(application.applicationContext)
    //退出登录 show.弹出动画，close.动画消失
    val logOut = MutableLiveData("")

    //版本更新进度
    val updateProgress = MutableLiveData(0)

    val newPassword = MutableLiveData("")
    val repeatPassword = MutableLiveData("")

    val shp = Shp(application.applicationContext)

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


    val sendMsgCode = MutableLiveData(0)
    val sendMsgMsg = MutableLiveData("")
    fun sendMsg(bodySendMsg: BodySendMsg){
        retrofitSingleton.api().sendMsg(bodySendMsg).enqueue(object :Callback<DataClassNoData>{
            override fun onResponse(
                call: Call<DataClassNoData>,
                response: Response<DataClassNoData>
            ) {
                try {
                    sendMsgMsg.value = "${response.body()?.msg}"
                    if (response.body()?.code.equals("1")){


                        sendMsgCode.value = 1


                    }else if(response.body()?.code.equals("11") || response.body()?.code.equals("8")){
                        sendMsgCode.value = 11
                    }else {
                        sendMsgCode.value = response.body()?.code?.toInt()
                    }
                }catch (e: Exception){

                    sendMsgMsg.value = "错误！请求响应码：${response.code()}"
                    sendMsgCode.value = 200
                }
            }

            override fun onFailure(call: Call<DataClassNoData>, t: Throwable) {
                sendMsgMsg.value = "发送短信网络请求失败"
                sendMsgCode.value = 404
            }

        })
    }

    val changeCode = MutableLiveData(0)
    val changeMsg = MutableLiveData("")
    fun changePassword(bodyModifyPassword: BodyModifyPassword){
        retrofitSingleton.api().changePassword(bodyModifyPassword).enqueue(object :Callback<DataClassGeneralBoolean>{
            override fun onResponse(
                call: Call<DataClassGeneralBoolean>,
                response: Response<DataClassGeneralBoolean>
            ) {
                try {
                    changeMsg.value = "${response.body()?.msg}"
                    if (response.body()?.code.equals("1")){


                        changeCode.value = 1


                    }else if(response.body()?.code.equals("11") || response.body()?.code.equals("8")){
                        changeCode.value = 11
                    }else {
                        changeCode.value = response.body()?.code?.toInt()
                    }
                }catch (e: Exception){

                    changeMsg.value = "错误！请求响应码：${response.code()}"
                    changeCode.value = 200
                }
            }

            override fun onFailure(call: Call<DataClassGeneralBoolean>, t: Throwable) {
                changeMsg.value = "发送短信网络请求失败"
                changeCode.value = 404
            }

        })
    }

    /**
     * 获取最新版本
     */
    val version = MutableLiveData("")
    val downLoadAddress = MutableLiveData("")
    val versionCode = MutableLiveData(0)
    val versionMsg = MutableLiveData("")
    fun postVersion(bodyVersion: BodyVersion){
        retrofitSingleton.api().postLatestVersion(bodyVersion).enqueue(object :Callback<DataClassVersion>{
            override fun onResponse(
                call: Call<DataClassVersion>,
                response: Response<DataClassVersion>
            ) {
                try {
                    versionMsg.value = "${response.body()?.msg}"
                    if (response.body()?.code.equals("1")){
                        if (null != response.body()?.data){
                            if (null != response.body()?.data?.downloadAddress){
                                downLoadAddress.value = response.body()?.data?.downloadAddress
                            }

                            version.value = response.body()?.data?.versionNo
                        }

                        versionCode.value  = 1

                    }else if(response.body()?.code.equals("11") || response.body()?.code.equals("8")){
                        versionCode.value = 11
                    }else {
                        versionCode.value = response.body()?.code?.toInt()
                    }
                }catch (e: Exception){

                    versionMsg.value = "错误！请求响应码：${response.code()}"
                    versionCode.value = 200
                }
            }

            override fun onFailure(call: Call<DataClassVersion>, t: Throwable) {
                versionMsg.value = "获取最新版本网络请求失败"
                versionCode.value = 404
            }

        })
    }


}