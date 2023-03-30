package com.hzdq.nppvdoctorclient

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.hzdq.nppvdoctorclient.dataclass.DataClassGeneralBoolean
import com.hzdq.nppvdoctorclient.dataclass.DataClassUserInfo
import com.hzdq.nppvdoctorclient.retrofit.RetrofitSingleton
import com.hzdq.nppvdoctorclient.util.Shp
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
    val shp = Shp(application.applicationContext)
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


    /**
     * 获取用户信息
     */
    val userInfoCode = MutableLiveData(0)
    val userInfoMsg = MutableLiveData("")
    val userName = MutableLiveData("")
    val hospitalName = MutableLiveData("")
    fun getUserInfo(){
        retrofitSingleton.api().getUserInfo().enqueue(object :Callback<DataClassUserInfo>{
            override fun onResponse(
                call: Call<DataClassUserInfo>,
                response: Response<DataClassUserInfo>
            ) {
                try {
                    userInfoMsg.value = "${response.body()?.msg}"
                    if (response.body()?.code.equals("1")){

                        hospitalName.value = response.body()?.data?.hospitalName
                        userName.value = response.body()?.data?.name
                        shp.saveToSp("userName",response.body()?.data?.name!!)
                        shp.saveToSpInt("uid",response.body()?.data?.uid!!)

                        userInfoCode.value = 1


                    }else if(response.body()?.code.equals("11")){
                        userInfoCode.value = 11
                    }else {
                        userInfoCode.value = response.body()?.code?.toInt()
                    }
                }catch (e: Exception){

                    userInfoMsg.value = "错误！请求响应码：${response.code()}"
                    userInfoCode.value = 200
                }
            }

            override fun onFailure(call: Call<DataClassUserInfo>, t: Throwable) {
                userInfoMsg.value = "获取用户信息网络请求失败"
                userInfoCode.value = 404
                netWorkTimeOut.value  = 2
            }

        })
    }

}