package com.hzdq.nppvdoctorclient.chat

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.hzdq.nppvdoctorclient.body.BodyImMessageList
import com.hzdq.nppvdoctorclient.body.BodyReadAllMsg
import com.hzdq.nppvdoctorclient.body.BodySendMessage
import com.hzdq.nppvdoctorclient.chat.paging.ImConversationListDataSourceFactory
import com.hzdq.nppvdoctorclient.chat.paging.ImMessageListDataSource
import com.hzdq.nppvdoctorclient.chat.paging.ImMessageListDataSourceFactory
import com.hzdq.nppvdoctorclient.chat.paging.ImMessageListNetWorkStatus
import com.hzdq.nppvdoctorclient.dataclass.DataClassGeneralBoolean
import com.hzdq.nppvdoctorclient.dataclass.DataClassImMessageList
import com.hzdq.nppvdoctorclient.dataclass.DataClassNoData
import com.hzdq.nppvdoctorclient.dataclass.ImMessageList
import com.hzdq.nppvdoctorclient.retrofit.RetrofitSingleton
import com.hzdq.nppvdoctorclient.util.ToastUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    val chatCountTotal = MutableLiveData(0)

    val groupId = MutableLiveData(0)

    val retrofitSingleton = RetrofitSingleton.getInstance(application.applicationContext)
    //需要回滚到顶部
    var needToScrollToTopMessageList = true

    private val factoryMessageList = ImMessageListDataSourceFactory(application) //paging的工厂类

    val messageListLiveData = factoryMessageList.toLiveData(10)
    //Transformations.switchMap容器A监听B内容的变化，变化时将B内容转化为相应的内容并通知A监听器
    val imMessageListNetWorkStatus = Transformations.switchMap(factoryMessageList.imMessageListDataSource){
        it.networkStatus
    }


//    fun resetMessageListQuery(){
//        //invalidate DataResource重新初始化
//        messageListLiveData.value?.dataSource?.invalidate()
//        needToScrollToTopMessageList = true
//    }

    //重新加载失败的数据
    fun retryMessageList(){

        //invoke表示执行
        factoryMessageList.imMessageListDataSource.value?.retry?.invoke()

        needToScrollToTopMessageList = false
    }



    //需要回滚到顶部
    var needToScrollToTopConversationList = true
    val conversationRefresh = MutableLiveData(0)
    val conversationPosition = MutableLiveData(-1)
    private val factoryConversationList = ImConversationListDataSourceFactory(application) //paging的工厂类
    val conversationListLiveData = factoryConversationList.toLiveData(10)
    //Transformations.switchMap容器A监听B内容的变化，变化时将B内容转化为相应的内容并通知A监听器
    val imConversationListNetWorkStatus = Transformations.switchMap(factoryConversationList.imConversationListDataSource){
        it.networkStatus
    }


    fun resetConversationListQuery(){
        //invalidate DataResource重新初始化
        conversationListLiveData.value?.dataSource?.invalidate()
        needToScrollToTopConversationList = true
    }

    //重新加载失败的数据
    fun retryConversationList(){

        //invoke表示执行
        factoryConversationList.imConversationListDataSource.value?.retry?.invoke()

        needToScrollToTopConversationList = false
    }

    val networkTimeOut = MutableLiveData(0)

    /**
     * 发送信息
     */
    val sendMessageCode = MutableLiveData(0)
    val sendMessageMsg = MutableLiveData("")
    fun sendMessage(bodySendMessage: BodySendMessage){
        retrofitSingleton.api().sendMessage(bodySendMessage).enqueue(object :Callback<DataClassGeneralBoolean>{
            override fun onResponse(
                call: Call<DataClassGeneralBoolean>,
                response: Response<DataClassGeneralBoolean>
            ) {
                try {
                    sendMessageMsg.value = "${response.body()?.msg}"
                    if (response.body()?.code.equals("1")){
                        sendMessageCode.value = 1
                    }else if(response.body()?.code.equals("11")){
                        sendMessageCode.value = 11
                    }else {
                        sendMessageCode.value = response.body()?.code?.toInt()
                    }
                }catch (e: Exception){

                    sendMessageMsg.value = "错误！请求响应码：${response.code()}"
                    sendMessageCode.value = 200
                }
            }

            override fun onFailure(call: Call<DataClassGeneralBoolean>, t: Throwable) {
                sendMessageMsg.value = "发送信息网络请求失败"
                sendMessageCode.value = 404
                networkTimeOut.value = 1
            }

        })
    }

    /**
     * 获取消息列表
     */
    val messageList = MutableLiveData<ArrayList<ImMessageList>>()
    var index = MutableLiveData(0)
    var messageCount = 0
    val messageCode = MutableLiveData(0)
    val messageMsg = MutableLiveData("")
    val positionStart = MutableLiveData(0)
    val loaderItemCount = MutableLiveData(0)
    val loadPosition = MutableLiveData(0)
    fun getMessageList(bodyImMessageList: BodyImMessageList){
        retrofitSingleton.api().getImMessageList(bodyImMessageList).enqueue(object :Callback<DataClassImMessageList>{
            override fun onResponse(
                call: Call<DataClassImMessageList>,
                response: Response<DataClassImMessageList>
            ) {
                try {
                    Log.d("getMessageList", "onResponse: ${response.body()}")
                    messageMsg.value = response.body()?.msg
                    if (response.body()?.code.equals("1")){

//                        val dataList = response.body()?.data?.list
//
//                        if (dataList != null && dataList.size>0){
//
//                            dataList[dataList.size-1].id?.let { index = it }
//
//                        }

                        messageCount += 1
                        if(messageCount == 1){
                            messageList.value = response.body()?.data?.list

                            positionStart.value = messageList.value!!.size!!-1
                            index.value = messageList.value!![messageList.value!!.size-1].id

                            messageCode.value = 1
                        }else {
                            Log.d("getMessageList", "预加载: ${response.body()}")
                            val list = response.body()?.data?.list
                            messageList.value?.addAll(list!!)
                            positionStart.value = messageList.value!!.size!!-1
                            loaderItemCount.value = list!!.size
                            index.value = messageList.value!![messageList.value!!.size-1].id
                            loadPosition.value = messageList.value!!.size-10
                            messageCount = 2
                            messageCode.value = 20
                        }

                    }else if (response.body()?.code.equals("11")){
                        messageCode.value = 11
                    }else {
                        messageCode.value = response.body()?.code?.toInt()
                    }
                }catch (e:Exception){
                    messageMsg.value = "错误！请求响应码：${response.code()}"
                    messageCode.value = 200
                }
            }

            override fun onFailure(call: Call<DataClassImMessageList>, t: Throwable) {
                messageMsg.value = "获取消息列表网络请求失败"
                messageCode.value = 404
                networkTimeOut.value = 2
            }
        })
    }


    /**
     * 已读消息
     */
    val readMsgCode = MutableLiveData(0)
    val readMsgMsg = MutableLiveData("")
    fun readAllMsg(bodyReadAllMsg: BodyReadAllMsg){
        retrofitSingleton.api().readAllMsg(bodyReadAllMsg).enqueue(object :Callback<DataClassNoData>{
            override fun onResponse(
                call: Call<DataClassNoData>,
                response: Response<DataClassNoData>
            ) {
                try {
                    readMsgMsg.value = response.body()?.msg
                    if (response.body()?.code.equals("1")){
                        readMsgCode.value = 1
                    }else if (response.body()?.code.equals("11")){
                        readMsgCode.value = 11
                    }else {
                        readMsgCode.value = response.body()?.code?.toInt()
                    }
                }catch (e:Exception){
                    readMsgMsg.value = "错误！请求响应码：${response.code()}"
                    readMsgCode.value = 200
                }
            }

            override fun onFailure(call: Call<DataClassNoData>, t: Throwable) {
                readMsgMsg.value = "获取消息列表网络请求失败"
                readMsgCode.value = 404
                networkTimeOut.value = 3
            }

        })
    }
}