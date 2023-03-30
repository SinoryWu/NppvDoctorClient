package com.hzdq.nppvdoctorclient.chat.paging

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.hzdq.nppvdoctorclient.body.BodyDoctorList
import com.hzdq.nppvdoctorclient.body.BodyImConversationList
import com.hzdq.nppvdoctorclient.dataclass.DataClassDoctorList
import com.hzdq.nppvdoctorclient.dataclass.DoctorList
import com.hzdq.nppvdoctorclient.dataclass.ImConversationList
import com.hzdq.nppvdoctorclient.retrofit.RetrofitSingleton
import com.hzdq.nppvdoctorclient.util.Shp
import com.hzdq.nppvdoctorclient.util.ToastUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 *Time:2023/3/27
 *Author:Sinory
 *Description:
 */
//枚举类 网络状态
enum class DoctorListNetWorkStatus{
    DOCTOR_LIST_INITIAL_LOADING, //第一次加载中
    DOCTOR_LIST_INITIAL_LOADED, //第一次加载完成
    DOCTOR_LIST_LOADING, //第二次加载中
    DOCTOR_LIST_LOADED, //第二次加载完成
    DOCTOR_LIST_FAILED, //加载失败
    DOCTOR_LIST_COMPLETED //加载完成
}
class DoctorListDataSource (private val context: Context):
    PageKeyedDataSource<Int, DoctorList>(){
    var retry: (()-> Any)? = null  //retry可以是任何对象 retry表示重新加载时需要加载的对象
    private val _networkStatus = MutableLiveData<DoctorListNetWorkStatus>()
    //网络状态的LiveData
    val networkStatus: LiveData<DoctorListNetWorkStatus> = _networkStatus
    val retrofitSingleton = RetrofitSingleton.getInstance(context)
    val shp = Shp(context)
    var bodyDoctorList : BodyDoctorList? =  null

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, DoctorList>
    ) {
        //        retry = null //重置retry
        bodyDoctorList = BodyDoctorList(null,shp.getDoctorSearchName(),1,10,null)
        retry = {loadInitial(params,callback)}
        _networkStatus.postValue(DoctorListNetWorkStatus.DOCTOR_LIST_INITIAL_LOADING) //网络加载状态为第一次加载
        retrofitSingleton.api().getDoctorList(bodyDoctorList!!).enqueue(object :Callback<DataClassDoctorList>{
            override fun onResponse(
                call: Call<DataClassDoctorList>,
                response: Response<DataClassDoctorList>
            ) {

                if (response.body()?.code.equals("1")){
                    bodyDoctorList = null
                    val dataList = response.body()?.data?.list

                    dataList?.let { callback.onResult(it,null,2) }
                    _networkStatus.postValue(DoctorListNetWorkStatus.DOCTOR_LIST_INITIAL_LOADED)
                    if (response.body()?.data?.boolLastPage == true){
                        _networkStatus.postValue(DoctorListNetWorkStatus.DOCTOR_LIST_COMPLETED)
                        return
                    }
                }
            }

            override fun onFailure(call: Call<DataClassDoctorList>, t: Throwable) {

                bodyDoctorList = null
                //保存一个函数用{} 如果第一次加载失败了把loadInitial保存下来
                _networkStatus.postValue(DoctorListNetWorkStatus.DOCTOR_LIST_COMPLETED)
                retry = {loadInitial(params,callback)} //retry的对象就是保存下来的对象 retry重新尝试加载当前的请求
                _networkStatus.postValue(DoctorListNetWorkStatus.DOCTOR_LIST_FAILED) //网络加载状态为失败
                ToastUtil.showToast(context,"获取医生/医助列表网络请求失败")
            }

        })
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, DoctorList>) {
        retry = null
        bodyDoctorList = BodyDoctorList(null,shp.getDoctorSearchName(),params.key,10,null)
        _networkStatus.postValue(DoctorListNetWorkStatus.DOCTOR_LIST_LOADING) //网络加载状态为正在加载
        retrofitSingleton.api().getDoctorList(bodyDoctorList!!).enqueue(object :Callback<DataClassDoctorList>{
            override fun onResponse(
                call: Call<DataClassDoctorList>,
                response: Response<DataClassDoctorList>
            ) {

                if (response.body()?.code.equals("1")){
                    bodyDoctorList = null
                    if (params.key > response.body()?.data?.pages!!){
                        _networkStatus.postValue(DoctorListNetWorkStatus.DOCTOR_LIST_COMPLETED)
                        return
                    }

                    val dataList = response.body()?.data?.list

                    dataList?.let { callback.onResult(it, params.key+1) } //callback.onResult将当前列表传入，第二个参数用当前页数+1 也就是下一页的页数
                    _networkStatus.postValue(DoctorListNetWorkStatus.DOCTOR_LIST_LOADED) //网络加载状态为加载完成
                }
            }

            override fun onFailure(call: Call<DataClassDoctorList>, t: Throwable) {
                _networkStatus.postValue(DoctorListNetWorkStatus.DOCTOR_LIST_COMPLETED)
                retry = {loadAfter(params,callback)} //retry重新尝试加载当前的请求
                _networkStatus.postValue(DoctorListNetWorkStatus.DOCTOR_LIST_FAILED) //网络加载状态为失败
            }

        })
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, DoctorList>) {
        TODO("Not yet implemented")
    }


}