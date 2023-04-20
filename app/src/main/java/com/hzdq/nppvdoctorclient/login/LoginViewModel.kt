package com.hzdq.nppvdoctorclient.login

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hzdq.nppvdoctorclient.body.BodyLoginPassword
import com.hzdq.nppvdoctorclient.body.BodyLoginVerificationCode
import com.hzdq.nppvdoctorclient.body.BodySendMsg
import com.hzdq.nppvdoctorclient.dataclass.DataClassLogin
import com.hzdq.nppvdoctorclient.dataclass.DataClassNoData
import com.hzdq.nppvdoctorclient.retrofit.RetrofitSingleton
import com.hzdq.nppvdoctorclient.util.Shp
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    val user = MutableLiveData("")
    val password = MutableLiveData("")

    val phone = MutableLiveData("")
    val verificationCode = MutableLiveData("")
    val retrofitSingleton = RetrofitSingleton.getInstance(application.applicationContext)
    val shp = Shp(application.applicationContext)

    val netWorkTimeOut = MutableLiveData(0)
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

    /**
     * 密码登录
     */
    val loginCode = MutableLiveData(0)
    val loginMsg = MutableLiveData("")
    fun loginPassword(bodyLoginPassword: BodyLoginPassword){
        retrofitSingleton.api().loginPassword(bodyLoginPassword).enqueue(object :Callback<DataClassLogin>{
            override fun onResponse(
                call: Call<DataClassLogin>,
                response: Response<DataClassLogin>
            ) {
                try {
                    loginMsg.value = "${response.body()?.msg}"
                    if (response.body()?.code.equals("1")){

                        shp.saveToSpBoolean("firstLogin",false)
                        response.body()?.data?.token?.let { shp.saveToSp("token", it) }
                        response.body()?.data?.uid?.let { shp.saveToSpInt("uid", it) }
                        response.body()?.data?.roleType?.let { shp.saveToSpInt("roleType", it) }
                        shp.saveToSpBoolean("firstLogin",false)
                        Log.d("loginroletype", "onResponse:${shp.getRoleType()} ")
                        loginCode.value =  1


                    }else if(response.body()?.code.equals("11")){
                        loginCode.value = 11
                    }else {
                        loginCode.value = response.body()?.code?.toInt()
                    }
                }catch (e: Exception){

                    loginMsg.value = "错误！请求响应码：${response.code()}"
                    loginCode.value = 200
                }
            }

            override fun onFailure(call: Call<DataClassLogin>, t: Throwable) {
                loginMsg.value = "密码登录网络请求失败"
                loginCode.value = 404
                netWorkTimeOut.value  = 1
            }

        })
    }

    /**
     * 验证码登录
     */
    fun loginVerificationCode(bodyLoginVerificationCode: BodyLoginVerificationCode){
        retrofitSingleton.api().loginVerificationCode(bodyLoginVerificationCode).enqueue(object :Callback<DataClassLogin>{
            override fun onResponse(
                call: Call<DataClassLogin>,
                response: Response<DataClassLogin>
            ) {
                try {
                    loginMsg.value = "${response.body()?.msg}"
                    if (response.body()?.code.equals("1")){

                        shp.saveToSpBoolean("firstLogin",false)
                        response.body()?.data?.token?.let { shp.saveToSp("token", it) }
                        response.body()?.data?.uid?.let { shp.saveToSpInt("uid", it) }
                        response.body()?.data?.roleType?.let { shp.saveToSpInt("roleType", it) }
                        shp.saveToSpBoolean("firstLogin",false)
                        loginCode.value =  1


                    }else if(response.body()?.code.equals("11")){
                        loginCode.value = 11
                    }else {
                        loginCode.value = response.body()?.code?.toInt()
                    }
                }catch (e: Exception){

                    loginMsg.value = "错误！请求响应码：${response.code()}"
                    loginCode.value = 200
                }
            }

            override fun onFailure(call: Call<DataClassLogin>, t: Throwable) {
                loginMsg.value = "密码登录网络请求失败"
                loginCode.value = 404
                netWorkTimeOut.value  = 2
            }

        })
    }

    /**
     * 发送短信
     */
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
                        sendMsgCode.value =  1


                    }else if(response.body()?.code.equals("11")){
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
                netWorkTimeOut.value  = 3
            }

        })
    }
}