package com.hzdq.nppvdoctorclient.chat.paging

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.hzdq.nppvdoctorclient.body.BodyImMessageList
import com.hzdq.nppvdoctorclient.dataclass.DataClassImMessageList
import com.hzdq.nppvdoctorclient.dataclass.ImMessageList
import com.hzdq.nppvdoctorclient.retrofit.RetrofitSingleton
import com.hzdq.nppvdoctorclient.util.Shp
import com.hzdq.nppvdoctorclient.util.ToastUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.util.*

/**
 *Time:2023/3/20
 *Author:Sinory
 *Description:
 */

//枚举类 网络状态
enum class ImMessageListNetWorkStatus{
    IM_MESSAGE_LIST_INITIAL_LOADING, //第一次加载中
    IM_MESSAGE_LIST_INITIAL_LOADED, //第一次加载完成
    IM_MESSAGE_LIST_LOADING, //第二次加载中
    IM_MESSAGE_LIST_LOADED, //第二次加载完成
    IM_MESSAGE_LIST_FAILED, //加载失败
    IM_MESSAGE_LIST_COMPLETED //加载完成
}
class ImMessageListDataSource(private val context: Context):PageKeyedDataSource<Int,ImMessageList>(){
    var retry: (()-> Any)? = null  //retry可以是任何对象 retry表示重新加载时需要加载的对象
    private val _networkStatus = MutableLiveData<ImMessageListNetWorkStatus>()
    //网络状态的LiveData
    val networkStatus: LiveData<ImMessageListNetWorkStatus> = _networkStatus
    val retrofitSingleton = RetrofitSingleton.getInstance(context)
    val shp = Shp(context)
    var index :Int?= null

    val bodyImMessageList = BodyImMessageList(0,1,10,null)
    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, ImMessageList>
    ) {
        Log.d("sadadasd", "onResponse:loadInitial ")
        //        retry = null //重置retry
        bodyImMessageList.groupId = shp.getGroupId()
        bodyImMessageList.index = index
        shp.saveToSpInt("MessageIndex", 0)
        retry = {loadInitial(params,callback)}
        _networkStatus.postValue(ImMessageListNetWorkStatus.IM_MESSAGE_LIST_INITIAL_LOADING) //网络加载状态为第一次加载
        retrofitSingleton.api().getImMessageList(bodyImMessageList).enqueue(object :Callback<DataClassImMessageList>{
            override fun onResponse(
                call: Call<DataClassImMessageList>,
                response: Response<DataClassImMessageList>
            ) {

                try {
                    if (response.body()?.code.equals("1")){

                        val dataList = response.body()?.data?.list
                        Log.d("sadadasd", "onResponse loadInitial:${response.body()?.data?.list?.size} ")
                        if (dataList != null && dataList.size>0){

                            dataList[dataList.size-1].id?.let { index = it }

                        }
                        dataList?.let {

                            callback.onResult(it,null,1)
                        }

                        _networkStatus.postValue(ImMessageListNetWorkStatus.IM_MESSAGE_LIST_INITIAL_LOADED) //网络加载状态为加载完成
                        if (response.body()?.data?.boolLastPage == true){
                            _networkStatus.postValue(ImMessageListNetWorkStatus.IM_MESSAGE_LIST_COMPLETED)
                            return
                        }
                    }
                }catch (e:Exception){
                    ToastUtil.showToast(context,e.toString())
                }

            }

            override fun onFailure(call: Call<DataClassImMessageList>, t: Throwable) {
                //保存一个函数用{} 如果第一次加载失败了把loadInitial保存下来
                _networkStatus.postValue(ImMessageListNetWorkStatus.IM_MESSAGE_LIST_COMPLETED)
                //retry的对象就是保存下来的对象 retry重新尝试加载当前的请求
                retry = {loadInitial(params,callback)}
                //网络加载状态为失败
                _networkStatus.postValue(ImMessageListNetWorkStatus.IM_MESSAGE_LIST_FAILED)
                ToastUtil.showToast(context,"获取消息列表网络请求失败")
            }

        })
    }
    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, ImMessageList>) {
        Log.d("sadadasd", "onResponse:loadAfter ")
        retry = null
        bodyImMessageList.groupId = shp.getGroupId()
        bodyImMessageList.index = index
        _networkStatus.postValue(ImMessageListNetWorkStatus.IM_MESSAGE_LIST_LOADING) //网络加载状态为正在加载
        retrofitSingleton.api().getImMessageList(bodyImMessageList).enqueue(object :Callback<DataClassImMessageList>{
            override fun onResponse(
                call: Call<DataClassImMessageList>,
                response: Response<DataClassImMessageList>
            ) {
                try {
                    if(response.body()?.code.equals("1")){
                        Log.d("sadadasd", "onResponse loadAfter:${response.body()?.data?.list?.size} ")

                        var dataList = response.body()?.data?.list
                        if (dataList?.size== 0){
                            _networkStatus.postValue(ImMessageListNetWorkStatus.IM_MESSAGE_LIST_COMPLETED)
                            return
                        }

                        dataList?.let {

                            callback.onResult(it, 1)
                        } //callback.onResult将当前列表传入，第二个参数用当前页数+1 也就是下一页的页数
                        if (dataList != null && dataList.size>0){

                            dataList[dataList.size-1].id?.let { index = it }

                        }
                        Log.d("sadadasd", "index loadAfter:${index} ")
                        _networkStatus.postValue(ImMessageListNetWorkStatus.IM_MESSAGE_LIST_LOADED) //网络加载状态为加载完成
//                        if (response.body()?.data?.boolLastPage == true){
//                            _networkStatus.postValue(ImMessageListNetWorkStatus.IM_MESSAGE_LIST_COMPLETED)
//                            return
//                        }
                    }
                }catch (e:Exception){
                    ToastUtil.showToast(context,e.toString())
                }

            }

            override fun onFailure(call: Call<DataClassImMessageList>, t: Throwable) {
                _networkStatus.postValue(ImMessageListNetWorkStatus.IM_MESSAGE_LIST_COMPLETED)
                retry = {loadAfter(params,callback)} //retry重新尝试加载当前的请求
                //网络加载状态为失败
                _networkStatus.postValue(ImMessageListNetWorkStatus.IM_MESSAGE_LIST_FAILED)
                ToastUtil.showToast(context,"获取消息列表网络请求失败")
            }

        })
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, ImMessageList>) {


    }




}