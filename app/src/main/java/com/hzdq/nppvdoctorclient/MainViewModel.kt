package com.hzdq.nppvdoctorclient

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.hzdq.nppvdoctorclient.dataclass.DataClassGeneralBoolean
import com.hzdq.nppvdoctorclient.retrofit.RetrofitSingleton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

/**
 *Time:2023/3/17
 *Author:Sinory
 *Description:
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val retrofitSingleton = RetrofitSingleton.getInstance(application.applicationContext)

    val netWorkTimeOut = MutableLiveData(0)
    /**
     * 登出
     */
    val logOutCode = MutableLiveData(0)
    val logOutMsg = MutableLiveData("")
    fun logOut(){
        retrofitSingleton.api().logOut().enqueue(object :Callback<DataClassGeneralBoolean>{
            override fun onResponse(
                call: Call<DataClassGeneralBoolean>,
                response: Response<DataClassGeneralBoolean>
            ) {
                Log.d("logOut", "onResponse:${response.body()} ")
                try {
                    logOutMsg.value = "${response.body()?.msg}"
                    if (response.body()?.code.equals("1")){

                       logOutCode.value = 1


                    }else if(response.body()?.code.equals("11")){
                        logOutCode.value = 11
                    }else {
                        logOutCode.value = response.body()?.code?.toInt()
                    }
                }catch (e: Exception){

                    logOutMsg.value = "错误！请求响应码：${response.code()}"
                    logOutCode.value = 200
                }
            }

            override fun onFailure(call: Call<DataClassGeneralBoolean>, t: Throwable) {
                logOutMsg.value = "密码登录网络请求失败"
                logOutCode.value = 404
                netWorkTimeOut.value  = 1
            }

        })
    }
}