package com.hzdq.nppvdoctorclient

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hzdq.nppvdoctorclient.dataclass.DataClassImAppInfo
import com.hzdq.nppvdoctorclient.dataclass.DataClassUserImToken
import com.hzdq.nppvdoctorclient.retrofit.RetrofitSingleton
import com.hzdq.nppvdoctorclient.util.Shp
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


/**
 *Time:2023/3/17
 *Author:Sinory
 *Description:共用viewmodel
 */
class CharCommonViewModel: ViewModel() {
    private val TAG = "CharCommonViewModel"
    private  val SCAN_PERIOD: Long = 10
    var ctx:Context? = null
    private var shp : Shp? = null
    private var retrofitSingleton:RetrofitSingleton? = null




    private val timeChangeReceiver: TimeChangeReceiver? = null

    val intentFilter = IntentFilter()

    fun setContext(ctx: Context){
        this.ctx = ctx
        shp = Shp(ctx)
        retrofitSingleton = RetrofitSingleton.getInstance(ctx)
    }

    //1.获取app详细信息,2.获取当前用户回话token
    val networkTimeout = MutableLiveData(0)

    /**
     * 获取app详细信息
     */
    val appInfoCode = MutableLiveData(0)
    val appInfoMsg = MutableLiveData("")
    fun getImAppInfo(){
        retrofitSingleton!!.api().getImAppInfo().enqueue(object :Callback<DataClassImAppInfo>{
            override fun onResponse(
                call: Call<DataClassImAppInfo>,
                response: Response<DataClassImAppInfo>
            ) {
                try {
                    appInfoMsg.value = "${response.body()?.msg}"
                    if (response.body()?.code.equals("1")){
                        Log.d(TAG, "getImAppInfo onResponse:${response.body()} ")
                        response.body()?.data?.appKey?.let { shp?.saveToSp("appKey", it) }
                        response.body()?.data?.clientId?.let { shp?.saveToSp("clientId", it) }
                        response.body()?.data?.clientSecret?.let { shp?.saveToSp("clientSecret", it) }
                        appInfoCode.value = 1

                    }else if(response.body()?.code.equals("11")){
                        appInfoCode.value = 11
                    }else {
                        appInfoCode.value = response.body()?.code?.toInt()
                    }
                }catch (e: Exception){

                    appInfoMsg.value = "错误！请求响应码：${response.code()}"
                    appInfoCode.value = 200
                }
            }

            override fun onFailure(call: Call<DataClassImAppInfo>, t: Throwable) {
                appInfoMsg.value = "获取app详细信息网络请求失败"
                appInfoCode.value = 404
                networkTimeout.value = 1
            }

        })
    }


    /**
     * 获取当前用户回话token
     */
    val imTokenCode = MutableLiveData(0)
    val imTokenMsg = MutableLiveData("")
    fun getImToken(){
        retrofitSingleton!!.api().getUserImToken().enqueue(object :Callback<DataClassUserImToken>{
            override fun onResponse(
                call: Call<DataClassUserImToken>,
                response: Response<DataClassUserImToken>
            ) {
                try {
                    imTokenMsg.value = "${response.body()?.msg}"
                    if (response.body()?.code.equals("1")){
                        Log.d(TAG, "getImToken onResponse:${response.body()} ")
                        response.body()?.data?.imToken?.let { shp?.saveToSp("imToken", it) }
                        response.body()?.data?.imUserName?.let { shp?.saveToSp("imUserName", it) }
                        shp?.saveToSpLone("tokenTimeMillis",System.currentTimeMillis())
                        Log.d(TAG, "getImToken: 请求成功 ")
                        imTokenCode.value = 1
                    }else if(response.body()?.code.equals("11")){
                        imTokenCode.value = 11
                    }else {
                        imTokenCode.value = response.body()?.code?.toInt()
                    }
                }catch (e: Exception){

                    imTokenMsg.value = "错误！请求响应码：${response.code()}"
                    imTokenCode.value = 200
                }
            }

            override fun onFailure(call: Call<DataClassUserImToken>, t: Throwable) {
                imTokenMsg.value = "获取app详细信息网络请求失败"
                imTokenCode.value = 404
                networkTimeout.value = 2
            }

        })
    }


    fun registerTimeChange(){
        intentFilter.addAction(Intent.ACTION_TIME_TICK) //每分钟变化

        intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED) //设置了系统时区

        intentFilter.addAction(Intent.ACTION_TIME_CHANGED) //设置了系统时间


        val timeChangeReceiver = TimeChangeReceiver(shp!!,this)
        ctx!!.registerReceiver(timeChangeReceiver, intentFilter)
    }

    fun unregisterTimeChange(){
        ctx!!.unregisterReceiver(timeChangeReceiver)
    }


    internal class TimeChangeReceiver(val shp: Shp,val charCommonViewModel: CharCommonViewModel) : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_TIME_TICK ->  {
                    //每过一分钟 触发
                    if (System.currentTimeMillis() - shp.getTokenTimeMillis()!! > 1739000){
                        Log.d("TimeChangeReceiver", "ACTION_TIME_TICK:请求一次")
                        charCommonViewModel.getImToken()
                    }
                    Log.d("TimeChangeReceiver", "ACTION_TIME_TICK:${System.currentTimeMillis()}")
                }

                Intent.ACTION_TIME_CHANGED ->                     //设置了系统时间
                    Log.d("TimeChangeReceiver", "ACTION_TIME_CHANGED: ")

                Intent.ACTION_TIMEZONE_CHANGED ->                     //设置了系统时区的action
                    Log.d("TimeChangeReceiver", "ACTION_TIMEZONE_CHANGED: ")
            }
        }
    }

}