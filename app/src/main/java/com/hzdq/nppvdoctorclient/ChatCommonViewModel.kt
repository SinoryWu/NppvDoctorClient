package com.hzdq.nppvdoctorclient

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.huawei.hms.framework.common.ContextCompat.registerReceiver
import com.huawei.hms.utils.UIUtil.isBackground
import com.hyphenate.EMCallBack
import com.hyphenate.EMGroupChangeListener
import com.hyphenate.EMMessageListener
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMCustomMessageBody
import com.hyphenate.chat.EMMucSharedFile
import com.hyphenate.chat.EMOptions
import com.hyphenate.push.EMPushConfig
import com.hzdq.nppvdoctorclient.dataclass.*
import com.hzdq.nppvdoctorclient.retrofit.RetrofitSingleton
import com.hzdq.nppvdoctorclient.util.DateUtil
import com.hzdq.nppvdoctorclient.util.NotificationUtil
import com.hzdq.nppvdoctorclient.util.Shp
import com.hzdq.nppvdoctorclient.util.ToolUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


/**
 *Time:2023/3/17
 *Author:Sinory
 *Description:通用viewmodel
 */
class ChatCommonViewModel : ViewModel() {
    private val TAG = "ChartCommonViewModel"
    private val SCAN_PERIOD: Long = 10
    var ctx: Context? = null
    private var shp: Shp? = null
    private var retrofitSingleton: RetrofitSingleton? = null

    private val timeChangeReceiver: TimeChangeReceiver? = null
    private var emClient: EMClient? = null
    val intentFilter = IntentFilter()

    fun setContext(ctx: Context) {
        this.ctx = ctx
        shp = Shp(ctx)
        retrofitSingleton = RetrofitSingleton.getInstance(ctx)
    }

    private var activityCount = 0
    fun registerActivityLifecycleCallbacks(application: Application){
        application.registerActivityLifecycleCallbacks(object :Application.ActivityLifecycleCallbacks{
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

            override fun onActivityStarted(activity: Activity) {
                activityCount++
            }

            override fun onActivityResumed(activity: Activity) {

            }

            override fun onActivityPaused(activity: Activity) {

            }

            override fun onActivityStopped(activity: Activity) {
                activityCount--
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

            }

            override fun onActivityDestroyed(activity: Activity) {

            }

        })
    }

    /**
     * 根据activityCount,判断app状态 前台或后台
     * 前台true 后台false
     */
    fun getAppStatus():Boolean {
        if (activityCount == 0) {
            //App进入后台或者APP锁屏了
            Log.d("getAppStatus", "后台: ")
                return false

        } else {
            //App进入前台
            Log.d("getAppStatus", "前台: ")
                return true

        }
    }

    //1.获取app详细信息,2.获取当前用户回话token
    val networkTimeout = MutableLiveData(0)

    /**
     * 获取app详细信息
     */
    val appInfoCode = MutableLiveData(0)
    val appInfoMsg = MutableLiveData("")
    fun getImAppInfo() {
        retrofitSingleton!!.api().getImAppInfo().enqueue(object : Callback<DataClassImAppInfo> {
            override fun onResponse(
                call: Call<DataClassImAppInfo>,
                response: Response<DataClassImAppInfo>
            ) {
                try {
                    appInfoMsg.value = "${response.body()?.msg}"
                    if (response.body()?.code.equals("1")) {
                        Log.d(TAG, "getImAppInfo onResponse:${response.body()} ")
                        response.body()?.data?.appKey?.let { shp?.saveToSp("appKey", it) }
                        response.body()?.data?.clientId?.let { shp?.saveToSp("clientId", it) }
                        response.body()?.data?.clientSecret?.let {
                            shp?.saveToSp(
                                "clientSecret",
                                it
                            )
                        }
                        appInfoCode.value = 1

                    } else if (response.body()?.code.equals("11")) {
                        appInfoCode.value = 11
                    } else {
                        appInfoCode.value = response.body()?.code?.toInt()
                    }
                } catch (e: Exception) {

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
    fun getImToken() {
        retrofitSingleton!!.api().getUserImToken().enqueue(object : Callback<DataClassUserImToken> {
            override fun onResponse(
                call: Call<DataClassUserImToken>,
                response: Response<DataClassUserImToken>
            ) {
                try {
                    imTokenMsg.value = "${response.body()?.msg}"
                    if (response.body()?.code.equals("1")) {
                        Log.d(TAG, "getImToken onResponse:${response.body()} ")
                        response.body()?.data?.imToken?.let { shp?.saveToSp("imToken", it) }
                        response.body()?.data?.imUserName?.let { shp?.saveToSp("imUserName", it) }
                        shp?.saveToSpLone("tokenTimeMillis", System.currentTimeMillis())
                        Log.d(TAG, "getImToken: 请求成功 ")
                        if (shp?.getFirstLoginIm() == false) {
                            loginIm()
                        }
                        imTokenCode.value = 1
                    } else if (response.body()?.code.equals("11")) {
                        imTokenCode.value = 11
                    } else {
                        imTokenCode.value = response.body()?.code?.toInt()
                    }
                } catch (e: Exception) {

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


    fun registerTimeChange() {
        intentFilter.addAction(Intent.ACTION_TIME_TICK) //每分钟变化

        intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED) //设置了系统时区

        intentFilter.addAction(Intent.ACTION_TIME_CHANGED) //设置了系统时间


        val timeChangeReceiver = TimeChangeReceiver(shp!!, this)
        ctx?.registerReceiver(timeChangeReceiver, intentFilter)
    }

    fun unregisterTimeChange() {
        ctx?.unregisterReceiver(timeChangeReceiver)
    }


    internal class TimeChangeReceiver(val shp: Shp, val chatCommonViewModel: ChatCommonViewModel) :
        BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_TIME_TICK -> {
                    //每过一分钟 触发
                    if (System.currentTimeMillis() - shp.getTokenTimeMillis()!! > 1739000) {
                        Log.d("TimeChangeReceiver", "ACTION_TIME_TICK:请求一次")
                        chatCommonViewModel.getImToken()
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


    fun registerListener() {
        // 注册消息监听
        emClient?.chatManager()?.addMessageListener(msgListener);
        emClient?.groupManager()?.addGroupChangeListener(groupListener);
    }

    fun unregisterListener() {
        // 解注册消息监听
        emClient?.chatManager()?.removeMessageListener(msgListener);
        emClient?.groupManager()?.removeGroupChangeListener(groupListener);
    }

    fun initIm() {
        emClient = EMClient.getInstance()
        val options = EMOptions()
        options.setAppKey(shp?.getAppKey());
        val builder = EMPushConfig.Builder(ctx!!)
//        builder.enableHWPush()
        options.setPushConfig(builder.build());
        emClient?.init(ctx!!, options);

    }

    fun loginIm() {
        Log.d(TAG, "loginIm: ${shp?.getImUserName()}")
        emClient?.loginWithToken(shp?.getImUserName(), shp?.getImToken(), object : EMCallBack {
            // 登录成功回调
            override fun onSuccess() {
                Log.d(TAG, "环信账号登录成功 ")
                CoroutineScope(Dispatchers.Main).launch {
                    shp?.saveToSpBoolean("firstLoginIm", false)
                }
            }

            // 登录失败回调，包含错误信息
            override fun onError(code: Int, error: String) {
                Log.d(TAG, "环信账号登录失败：$error ")
            }

            override fun onProgress(i: Int, s: String) {}
        })
    }

    private fun notification(name: String,type:Int,content:String){
        /**
         * 通知栏（兼容android 8.0以上）
         */
        val isVibrate = true //是否震动

        //1.获取消息服务
        val manager = ctx!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        //默认通道是default
        var channelId = "default"
        //2.如果是android8.0以上的系统，则新建一个消息通道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = "chat"
            /*
          通道优先级别：
          * IMPORTANCE_NONE 关闭通知
          * IMPORTANCE_MIN 开启通知，不会弹出，但没有提示音，状态栏中无显示
          * IMPORTANCE_LOW 开启通知，不会弹出，不发出提示音，状态栏中显示
          * IMPORTANCE_DEFAULT 开启通知，不会弹出，发出提示音，状态栏中显示
          * IMPORTANCE_HIGH 开启通知，会弹出，发出提示音，状态栏中显示
          */
            val channel = NotificationChannel(channelId, "chat", NotificationManager.IMPORTANCE_DEFAULT)
            //设置该通道的描述（可以不写）
            //channel.setDescription("重要消息，请不要关闭这个通知。");
            //是否绕过勿打扰模式
            channel.setBypassDnd(false)
            //是否允许呼吸灯闪烁
            channel.enableLights(true)
            //闪关灯的灯光颜色
            channel.lightColor = Color.RED
            //桌面launcher的消息角标
            channel.canShowBadge()
            //设置是否应在锁定屏幕上显示此频道的通知
            //channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            if (isVibrate) {
                //是否允许震动
                channel.enableVibration(true)
                //先震动1秒，然后停止0.5秒，再震动2秒则可设置数组为：new long[]{1000, 500, 2000}
                channel.vibrationPattern = longArrayOf(1000, 500, 2000)
            } else {
                channel.enableVibration(false)
                channel.vibrationPattern = longArrayOf(0)
            }
            //创建消息通道
            manager.createNotificationChannel(channel)
        }
        //3.实例化通知
        val nc = NotificationCompat.Builder(
            ctx!!,
            channelId!!
        )
        //通知默认的声音 震动 呼吸灯
        nc.setDefaults(NotificationCompat.DEFAULT_ALL)
        //通知标题
        nc.setContentTitle("NPPV医生端")
        //通知内容
        if (type == 1){
            nc.setContentText("${name}：${content}")
        }else {
            nc.setContentText("${name}：[图片]")
        }

        //设置通知的小图标
        nc.setSmallIcon(R.drawable.ic_launcher_foreground)
        //设置通知的大图标
        nc.setLargeIcon(BitmapFactory.decodeResource(ctx!!.resources, R.drawable.ic_launcher_foreground))
        //设定通知显示的时间
        nc.setWhen(System.currentTimeMillis())
        //设置通知的优先级
        nc.priority = NotificationCompat.PRIORITY_MAX
        //设置点击通知之后通知是否消失
        nc.setAutoCancel(true)
        //点击通知打开软件
        val application: Context = ctx!!
        val resultIntent = Intent(application, MainActivity::class.java)
        resultIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        resultIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        val pendingIntent = PendingIntent.getActivity(application, 0, resultIntent, 0)
        nc.setContentIntent(pendingIntent)
        //4.创建通知，得到build
        val notification = nc.build()
        //5.发送通知
        manager.notify(1, notification)


    }



    val gson = Gson()
    val receiverList = MutableLiveData<MutableList<ImMessageList>>(ArrayList())
    val dataClassReceiverList = MutableLiveData<MutableList<DataClassReceiver>>(ArrayList())
    val receiverCount = MutableLiveData(0)
    var dataClassReceiver : DataClassReceiver? = null
    var fromUser : FromUser? = null
    var imMessageList : ImMessageList? = null
    val listId = MutableLiveData(0)
    val imageList = MutableLiveData<MutableList<String>>(ArrayList())
    val groupThirdPartyId = MutableLiveData("")
    var msgListener = EMMessageListener { msgList ->

        Log.d(TAG, "receiverCount: 收到消息 ${msgList.size}")
        Log.d(TAG, "msgId: 收到消息 ${msgList[0].msgId}")
        try {
            for (i in 0 until msgList.size) {
                var res = gson.toJson((msgList[i].body as EMCustomMessageBody).params)
                Log.d(TAG, "receiverCount res:${res} ")
                dataClassReceiver = gson.fromJson(res, DataClassReceiver::class.java)
                dataClassReceiver?.conversationId  =msgList[i].conversationId()
                Log.d(TAG, "receiverCount uid:${shp!!.getUid()} ")
                if (groupThirdPartyId.value.equals(dataClassReceiver?.conversationId)){
                    if (shp!!.getUid() != dataClassReceiver?.fromUserId){
                        fromUser = FromUser(0, 0, "", "", 0)
                        fromUser?.userName = dataClassReceiver?.fromUserName
                        fromUser?.userType = dataClassReceiver?.fromUserType
                        imMessageList = ImMessageList(0, 2, fromUser, "", "", 0, "", false)
                        imMessageList?.fromUser = fromUser
                        imMessageList?.gmtCreate = DateUtil.stamp2Date(System.currentTimeMillis())
                        imMessageList?.messageType = dataClassReceiver?.messageType
                        imMessageList?.message = dataClassReceiver?.messageContent
                        if (dataClassReceiver?.messageType == 2){
                            imageList.value?.add(dataClassReceiver?.messageContent!!)
                        }
                        receiverList.value?.add(0, imMessageList!!)
                        fromUser = null
                        imMessageList = null
                    }
                }

                dataClassReceiverList.value?.add(dataClassReceiver!!)
                Log.d(TAG, "receiverCount:${dataClassReceiverList.value} ")
            }
            CoroutineScope(Dispatchers.Main).launch {
                if (dataClassReceiverList.value!!.size > 0){
                    Log.d(TAG, "receiverCount:收到消息receiverCount+1 = ${receiverCount.value!! + 1} ")
                    Log.d(TAG, "receiverCount:getAppStatus ${getAppStatus()} ")
                    if (getAppStatus() ){
                        if (!shp?.getToken().equals("")){
                            if (ToolUtils.isSilentMode(ctx!!).equals("normal")){
                                ToolUtils.defaultMediaPlayer(ctx!!)
                                ToolUtils.playVibrate(ctx!!,false)
                            }else if (ToolUtils.isSilentMode(ctx!!).equals("vibrate")){
                                ToolUtils.playVibrate(ctx!!,false)
                            }
                        }

                    }else {
                        if (!shp?.getToken().equals("")){
                            val name = dataClassReceiverList.value!![dataClassReceiverList.value!!.size-1].fromUserName
                            val type = dataClassReceiverList.value!![dataClassReceiverList.value!!.size-1].messageType
                            val content = dataClassReceiverList.value!![dataClassReceiverList.value!!.size-1].messageContent
                            Log.d(TAG, "type:$type ")
                            NotificationUtil.notification("NPPV医生端",name!!,type!!,content!!,ctx!!)
                        }


                    }

                    receiverCount.value = receiverCount.value!! + 1
                }

            }
        }catch (e:Exception){
            Log.d(TAG, "receiverCount 没走下去$e ")
        }





    }// 收到消息，遍历消息队列，解析和显示。

    //监听群组回调
    val groupAcceptInvitation = MutableLiveData(0)
    var groupListener = object : EMGroupChangeListener{
        override fun onInvitationReceived(
            groupId: String?,
            groupName: String?,
            inviter: String?,
            reason: String?
        ) {
            Log.d(TAG, "onInvitationReceived: ")
        }

        override fun onRequestToJoinReceived(
            groupId: String?,
            groupName: String?,
            applicant: String?,
            reason: String?
        ) {
            Log.d(TAG, "onRequestToJoinReceived: ")
        }

        override fun onRequestToJoinAccepted(
            groupId: String?,
            groupName: String?,
            accepter: String?
        ) {
            Log.d(TAG, "onRequestToJoinAccepted: ")
        }

        override fun onRequestToJoinDeclined(
            groupId: String?,
            groupName: String?,
            decliner: String?,
            reason: String?
        ) {
            Log.d(TAG, "onRequestToJoinDeclined: ")
        }

        override fun onInvitationAccepted(groupId: String?, invitee: String?, reason: String?) {
            Log.d(TAG, "onInvitationAccepted: ")
        }

        override fun onInvitationDeclined(groupId: String?, invitee: String?, reason: String?) {
            Log.d(TAG, "onInvitationDeclined: ")
        }

        override fun onUserRemoved(groupId: String?, groupName: String?) {
            Log.d(TAG, "onUserRemoved: ")
        }

        override fun onGroupDestroyed(groupId: String?, groupName: String?) {
            Log.d(TAG, "onGroupDestroyed: ")
        }

        override fun onAutoAcceptInvitationFromGroup(
            groupId: String?,
            inviter: String?,
            inviteMessage: String?
        ) {

            //收到邀请 去刷新列表
            CoroutineScope(Dispatchers.Main).launch {
                Log.d(TAG, "onAutoAcceptInvitationFromGroup: ")
                groupAcceptInvitation.value = 1
            }


        }

        override fun onMuteListAdded(
            groupId: String?,
            mutes: MutableList<String>?,
            muteExpire: Long
        ) {
            Log.d(TAG, "onMuteListAdded: ")
        }

        override fun onMuteListRemoved(groupId: String?, mutes: MutableList<String>?) {
            Log.d(TAG, "onMuteListRemoved: ")
        }

        override fun onWhiteListAdded(groupId: String?, whitelist: MutableList<String>?) {
            Log.d(TAG, "onWhiteListAdded: ")
        }

        override fun onWhiteListRemoved(groupId: String?, whitelist: MutableList<String>?) {
            Log.d(TAG, "onWhiteListRemoved: ")
        }

        override fun onAllMemberMuteStateChanged(groupId: String?, isMuted: Boolean) {
            Log.d(TAG, "onAllMemberMuteStateChanged: ")
        }

        override fun onAdminAdded(groupId: String?, administrator: String?) {
            Log.d(TAG, "onAdminAdded: ")
        }

        override fun onAdminRemoved(groupId: String?, administrator: String?) {
            Log.d(TAG, "onAdminRemoved: ")
        }

        override fun onOwnerChanged(groupId: String?, newOwner: String?, oldOwner: String?) {
            Log.d(TAG, "onOwnerChanged: ")
        }

        override fun onMemberJoined(groupId: String?, member: String?) {
            Log.d(TAG, "onMemberJoined: ")
        }

        override fun onMemberExited(groupId: String?, member: String?) {
            Log.d(TAG, "onMemberExited: ")
        }

        override fun onAnnouncementChanged(groupId: String?, announcement: String?) {
            Log.d(TAG, "onAnnouncementChanged: ")
        }

        override fun onSharedFileAdded(groupId: String?, sharedFile: EMMucSharedFile?) {
            Log.d(TAG, "onSharedFileAdded: ")
        }

        override fun onSharedFileDeleted(groupId: String?, fileId: String?) {
            Log.d(TAG, "onSharedFileDeleted: ")
        }
    }




}