package com.hzdq.nppvdoctorclient

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.hzdq.nppvdoctorclient.dataclass.DataClassDoctorLoad
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
    val refreshWebView = MutableLiveData(0)
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


                    }else if(response.body()?.code.equals("11") || response.body()?.code.equals("8")){
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
    val doctorTitle = MutableLiveData("")
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
                        if (null == response.body()?.data?.mobile){
                            shp.saveToSp("phone","")
                        }else {
                            shp.saveToSp("phone",response.body()?.data?.mobile!!)
                        }


                        shp.saveToSpInt("uid",response.body()?.data?.uid!!)
                        if (response.body()?.data?.roleType == 2){
//                            getDoctorLoad(shp.getUid())
                            doctorTitle.value = response.body()?.data?.doctorDepartment+response.body()?.data?.doctorPosition
                        }
                        userInfoCode.value = 1


                    }else if(response.body()?.code.equals("11") || response.body()?.code.equals("8")){
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



    /**
     * 获取医生详情
     */
    val doctorLoadCode = MutableLiveData(0)
    val doctorLoadMsg = MutableLiveData("")

    fun getDoctorLoad(uid:Int){
        retrofitSingleton.api().getDoctorLoad(uid).enqueue(object :Callback<DataClassDoctorLoad>{
            override fun onResponse(
                call: Call<DataClassDoctorLoad>,
                response: Response<DataClassDoctorLoad>
            ) {
                try {
                    doctorLoadMsg.value = "${response.body()?.msg}"
                    if (response.body()?.code.equals("1")){
                        doctorTitle.value = response.body()?.data?.doctorDepartment+response.body()?.data?.doctorPosition

                        doctorLoadCode.value = 1


                    }else if(response.body()?.code.equals("11") || response.body()?.code.equals("8")){
                        doctorLoadCode.value = 11
                    }else {
                        doctorLoadCode.value = response.body()?.code?.toInt()
                    }
                }catch (e: Exception){

                    doctorLoadMsg.value = "错误！请求响应码：${response.code()}"
                    doctorLoadCode.value = 200
                }
            }

            override fun onFailure(call: Call<DataClassDoctorLoad>, t: Throwable) {
                doctorLoadMsg.value = "获取医生信息网络请求失败"
                doctorLoadCode.value = 404
                netWorkTimeOut.value  = 3
            }

        })
    }
}