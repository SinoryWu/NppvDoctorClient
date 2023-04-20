package com.hzdq.nppvdoctorclient.chat

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.toLiveData
import com.hzdq.nppvdoctorclient.body.*
import com.hzdq.nppvdoctorclient.chat.paging.DoctorListDataSourceFactory
import com.hzdq.nppvdoctorclient.chat.paging.ImConversationListDataSourceFactory
import com.hzdq.nppvdoctorclient.chat.paging.ImMessageListDataSourceFactory
import com.hzdq.nppvdoctorclient.dataclass.*
import com.hzdq.nppvdoctorclient.retrofit.RetrofitSingleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.lang.Exception

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    val chatCountTotal = MutableLiveData(0)
    val isChat = MutableLiveData(false)
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
    val conversationListLiveData = factoryConversationList.toLiveData(50)
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
    fun sendMessage(messageType:Int,bodySendMessage: BodySendMessage){
        retrofitSingleton.api().sendMessage(bodySendMessage).enqueue(object :Callback<DataClassGeneralBoolean>{
            override fun onResponse(
                call: Call<DataClassGeneralBoolean>,
                response: Response<DataClassGeneralBoolean>
            ) {
                try {
                    sendMessageMsg.value = "${response.body()?.msg}"
                    if (response.body()?.code.equals("1")){
                        if (messageType == 1){
                            sendMessageCode.value = 1
                        }else {

                            sendMessageCode.value = 20
                        }

                    }else if(response.body()?.code.equals("11") || response.body()?.code.equals("8")){
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
    val preloadItemCount = MutableLiveData(10)
    val messageList = MutableLiveData<ArrayList<ImMessageList>>(ArrayList())
    var index = MutableLiveData(0)
    var messageCount = 0
    val messageCode = MutableLiveData(0)
    val messageMsg = MutableLiveData("")
    val positionStart = MutableLiveData(0)
    val loaderItemCount = MutableLiveData(0)
    val imageList = MutableLiveData<ArrayList<String>>(ArrayList())

    var lastOffset = MutableLiveData(0)
    var lastPosition = MutableLiveData(0)
    val messageTotal = MutableLiveData(0)
    fun getMessageList(bodyImMessageList: BodyImMessageList){
        retrofitSingleton.api().getImMessageList(bodyImMessageList).enqueue(object :Callback<DataClassImMessageList>{
            override fun onResponse(
                call: Call<DataClassImMessageList>,
                response: Response<DataClassImMessageList>
            ) {
                messageMsg.value = response.body()?.msg
                try {
                    if (response.body()?.code.equals("1")){
                        messageCount += 1
                        if(messageCount == 1){
                            preloadItemCount.value = 30
                            messageList.value = response.body()?.data?.list
                            messageTotal.value = response.body()?.data!!.total
                            if (messageList.value!!.size > 0){

                                for (i in  0..messageList.value!!.size-1 ){
                                    if (messageList.value!![i].messageType == 2){

                                        imageList.value!!.add(messageList.value!![i].message!!)
                                    }
                                }
                            }
                            positionStart.value = messageList.value!!.size!!-1
                            if (messageList.value!!.size > 0){
                                index.value = messageList.value!![messageList.value!!.size-1].id
                            }


                            messageCode.value = 1

                        }else {

                            val list = response.body()?.data?.list

                            if (list?.size!! > 0){
                                for (i in  0..list!!.size-1 ){
                                    if (list[i].messageType == 2){

                                        imageList.value!!.add(list!![i].message!!)
                                    }
                                }
                            }

                            messageList.value?.addAll(list!!)
                            positionStart.value = messageList.value!!.size!!-1
                            loaderItemCount.value = list!!.size
                            if (messageList.value!!.size > 0){
                                index.value = messageList.value!![messageList.value!!.size-1].id
                            }


                            messageCount = 2
                            messageCode.value = 20
                        }

                    }else if (response.body()?.code.equals("11") || response.body()?.code.equals("8")){
                        messageCode.value = 11
                    }else {
                        messageCode.value = response.body()?.code?.toInt()
                    }

                }catch (e:Exception){
                    Log.d("getMessageList", "获取消息列表错误: $e")
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
                Log.d("readMsg", "onResponse:${response.body()} ")
                try {
                    readMsgMsg.value = response.body()?.msg
                    if (response.body()?.code.equals("1")){
                        readMsgCode.value = 1
                    }else if (response.body()?.code.equals("11") || response.body()?.code.equals("8")){
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
                readMsgMsg.value = "已读消息网络请求失败"
                readMsgCode.value = 404
                networkTimeOut.value = 3
            }

        })
    }


    /**
     * 上传文件
     */
    val messagePicList = MutableLiveData<ArrayList<String>>(ArrayList())
    val messagePicIndex = MutableLiveData(-1)
    val fileCode = MutableLiveData(0)
    val fileMsg = MutableLiveData("")
    val picAddress = MutableLiveData("")

    fun uploadFile(body: MultipartBody.Part){
        retrofitSingleton.api().uploadFile(body).enqueue(object :Callback<DataClassFile>{
            override fun onResponse(call: Call<DataClassFile>, response: Response<DataClassFile>) {
                try {
                    fileMsg.value = response.body()?.msg
                    if (response.body()?.code.equals("1")){
                        picAddress.value  = response.body()?.data
                        fileCode.value = 1

                    }else if (response.body()?.code.equals("11") || response.body()?.code.equals("8")){
                        fileCode.value = 11
                    }else {
                        fileCode.value = response.body()?.code?.toInt()
                    }
                }catch (e:Exception){
                    fileMsg.value = "错误！请求响应码：${response.code()}"
                    fileCode.value = 200
                }
            }

            override fun onFailure(call: Call<DataClassFile>, t: Throwable) {
                Log.d("TAG", "onFailure:$t ")
                fileMsg.value = "图片上传网络请求失败"
                fileCode.value = 404
                networkTimeOut.value = 4
            }

        })
    }


    /**
     * 获取群成员
     */
    val groupCode = MutableLiveData(0)
    val groupMsg = MutableLiveData("")
    val groupMemberList = MutableLiveData<MutableList<DataGroupMember?>>(ArrayList())
    fun getGroupMembers(groupId:Int){
        retrofitSingleton.api().getGroupMembers(groupId).enqueue(object :Callback<DataClassGroupMember>{
            override fun onResponse(
                call: Call<DataClassGroupMember>,
                response: Response<DataClassGroupMember>
            ) {
                try {
                    groupMsg.value = response.body()?.msg
                    if (response.body()?.code.equals("1")){
                        groupMemberList.value = response.body()?.data
                        groupCode.value = 1

                    }else if (response.body()?.code.equals("11") || response.body()?.code.equals("8")){
                        groupCode.value = 11
                    }else {
                        groupCode.value = response.body()?.code?.toInt()
                    }
                }catch (e:Exception){
                    groupMsg.value = "错误！请求响应码：${response.code()}"
                    groupCode.value = 200
                }
            }

            override fun onFailure(call: Call<DataClassGroupMember>, t: Throwable) {
                groupMsg.value = "获取群成员网络请求失败"
                groupCode.value = 404
                networkTimeOut.value = 5
            }

        })
    }


    /**
     * 退群
     */
    val exitCode = MutableLiveData(0)
    val exitMsg = MutableLiveData("")
    fun exitGroup(){
        retrofitSingleton.api().getGroupRemove(groupId.value!!).enqueue(object :Callback<DataClassExitGroup>{
            override fun onResponse(
                call: Call<DataClassExitGroup>,
                response: Response<DataClassExitGroup>
            ) {
                try {
                    exitMsg.value = response.body()?.msg
                    if (response.body()?.code.equals("1")){

                        exitCode.value = 1

                    }else if (response.body()?.code.equals("11") || response.body()?.code.equals("8")){
                        exitCode.value = 11
                    }else {
                        exitCode.value = response.body()?.code?.toInt()
                    }
                }catch (e:Exception){
                    exitMsg.value = "错误！请求响应码：${response.code()}"
                    exitCode.value = 200
                }
            }

            override fun onFailure(call: Call<DataClassExitGroup>, t: Throwable) {
                exitMsg.value = "退出群聊网络请求失败"
                exitCode.value = 404
                networkTimeOut.value = 6
            }

        })
    }

    /**
     * 获取医生/医助列表
     */
    val doctorListCode = MutableLiveData(0)
    val doctorListMsg = MutableLiveData("")
    val doctorList = MutableLiveData<MutableList<DoctorList>>(ArrayList())
    val doctorListSearch = MutableLiveData<MutableList<DoctorList>>(ArrayList())
    val uidList  = MutableLiveData<ArrayList<Int>>(ArrayList())
    val searchName = MutableLiveData("")
    fun getDoctorList(bodyDoctorList: BodyDoctorList){
        retrofitSingleton.api().getDoctorList(bodyDoctorList).enqueue(object :Callback<DataClassDoctorList>{
            override fun onResponse(
                call: Call<DataClassDoctorList>,
                response: Response<DataClassDoctorList>
            ) {
                try {
                    doctorListMsg.value = response.body()?.msg
                    if (response.body()?.code.equals("1")){


                        response.body()?.data?.list?.let {
                            for (i in 0 until it.size){
                                if (it[i].uid!! !in uidList.value!!){
                                    doctorList.value!!.add(it[i])
                                }

                            }

                        }
                        doctorListCode.value = 1


                    }else if (response.body()?.code.equals("11") || response.body()?.code.equals("8")){
                        doctorListCode.value = 11
                    }else {
                        doctorListCode.value = response.body()?.code?.toInt()
                    }
                }catch (e:Exception){
                    doctorListMsg.value = "错误！请求响应码：${response.code()}"
                    doctorListCode.value = 200
                }
            }

            override fun onFailure(call: Call<DataClassDoctorList>, t: Throwable) {
                doctorListMsg.value = "获取医生/医助列表网络请求失败"
                doctorListCode.value = 404
                networkTimeOut.value = 7
            }

        })
    }


    //需要回滚到顶部
    var needToScrollToTopDoctorList = true
    val doctorListRefresh = MutableLiveData(0)
    private val factoryDoctorList = DoctorListDataSourceFactory(application) //paging的工厂类
    val doctorListLiveData = factoryDoctorList.toLiveData(10)
    //Transformations.switchMap容器A监听B内容的变化，变化时将B内容转化为相应的内容并通知A监听器
    val doctorListNetWorkStatus = Transformations.switchMap(factoryDoctorList.doctorListDataSource){
        it.networkStatus
    }



    fun resetDoctorListQuery(){
        //invalidate DataResource重新初始化
        Log.d("TAG", "resetDoctorListQuery: ")
        doctorListLiveData.value?.dataSource?.invalidate()
        needToScrollToTopDoctorList = true
    }

    //重新加载失败的数据
    fun retryDoctorList(){


        //invoke表示执行
        factoryDoctorList.doctorListDataSource.value?.retry?.invoke()

        needToScrollToTopDoctorList = false
    }


    /**
     * 拉人入群
     */
    val groupInvitationId = MutableLiveData(0)
    val groupInvitationMsg = MutableLiveData("")
    val groupInvitationPosition = MutableLiveData(-1)
    val isDoneCount = MutableLiveData(0)
    val groupInvitationList = MutableLiveData<ArrayList<BodyGroupInvitation>>(ArrayList())
    val groupInvitationListSize = MutableLiveData(0)
    fun groupInvitation(bodyGroupInvitation: BodyGroupInvitation){
        Log.d("AddDoctor", "groupInvitation:$bodyGroupInvitation ")
//        CoroutineScope(Dispatchers.Main).launch {
//            delay(500)
//
//            groupInvitationId.value = 1
//
//        }

        retrofitSingleton.api().groupInvitation(bodyGroupInvitation).enqueue(object :Callback<DataClassNoData>{
            override fun onResponse(
                call: Call<DataClassNoData>,
                response: Response<DataClassNoData>
            ) {
                try {

                    groupInvitationMsg.value = response.body()?.msg
                    if (response.body()?.code.equals("1")){

                        groupInvitationId.value = 1


                    }else if (response.body()?.code.equals("11") || response.body()?.code.equals("8")){
                        groupInvitationId.value = 11

                    }else {
                        groupInvitationId.value = response.body()?.code?.toInt()

                    }
                }catch (e:Exception){
                    groupInvitationMsg.value = "错误！请求响应码：${response.code()}"
                    groupInvitationId.value = 200

                }
            }

            override fun onFailure(call: Call<DataClassNoData>, t: Throwable) {
                groupInvitationMsg.value = "成员入群网络请求失败"
                groupInvitationId.value = 404
                networkTimeOut.value = 8
            }

        })
    }
}