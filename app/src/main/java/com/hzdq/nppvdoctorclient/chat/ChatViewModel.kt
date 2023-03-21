package com.hzdq.nppvdoctorclient.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.toLiveData
import com.hzdq.nppvdoctorclient.chat.paging.ImConversationListDataSourceFactory
import com.hzdq.nppvdoctorclient.chat.paging.ImMessageListDataSource
import com.hzdq.nppvdoctorclient.chat.paging.ImMessageListDataSourceFactory

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    val chatCountTotal = MutableLiveData(0)

    val groupId = MutableLiveData(0)
    //需要回滚到顶部
    var needToScrollToTopMessageList = true

    private val factoryMessageList = ImMessageListDataSourceFactory(application) //paging的工厂类
    val messageListLiveData = factoryMessageList.toLiveData(10)
    //Transformations.switchMap容器A监听B内容的变化，变化时将B内容转化为相应的内容并通知A监听器
    val imMessageListNetWorkStatus = Transformations.switchMap(factoryMessageList.imMessageListDataSource){
        it.networkStatus
    }


    fun resetMessageListQuery(){
        //invalidate DataResource重新初始化
        messageListLiveData.value?.dataSource?.invalidate()
        needToScrollToTopMessageList = true
    }

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
}