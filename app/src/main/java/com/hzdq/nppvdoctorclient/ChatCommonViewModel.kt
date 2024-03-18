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
import com.hzdq.nppvdoctorclient.dataclass.*
import com.hzdq.nppvdoctorclient.login.LoginActivity
import com.hzdq.nppvdoctorclient.retrofit.RetrofitSingleton
import com.hzdq.nppvdoctorclient.util.*
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


    val intentFilter = IntentFilter()

    fun setContext(ctx: Context) {
        this.ctx = ctx
        shp = Shp(ctx)
        retrofitSingleton = RetrofitSingleton.getInstance(ctx)
    }

    var activityCount = 0
    fun registerActivityLifecycleCallbacks(application: Application){
        application.registerActivityLifecycleCallbacks(object :Application.ActivityLifecycleCallbacks{
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

            override fun onActivityStarted(activity: Activity) {
                Log.d(TAG, "onActivityStarted: ${activity.localClassName}")
                if (!activity.localClassName.equals("login.LoginActivity")){
                    activityCount++
                }

                Log.d(TAG, "activityCount start: $activityCount")
            }

            override fun onActivityResumed(activity: Activity) {

            }

            override fun onActivityPaused(activity: Activity) {

            }

            override fun onActivityStopped(activity: Activity) {

                if (!activity.localClassName.equals("login.LoginActivity")){
                    activityCount--
                }


                Log.d(TAG, "activityCount stop: $activityCount")
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
//    var msgListener = EMMessageListener { msgList ->
//        FileUtil.writeLog("${ctx!!.getExternalFilesDir("log")}/2023-04-06","收到消息--${(msgList[0].body as EMCustomMessageBody).params}")
//        Log.d(TAG, "receiverCount: 收到消息 ${msgList.size}")
//        Log.d(TAG, "msgId: 收到消息 ${msgList[0].msgId}")
//        try {
//            for (i in 0 until msgList.size) {
//                var res = gson.toJson((msgList[i].body as EMCustomMessageBody).params)
//                Log.d(TAG, "receiverCount res:${res} ")
//                dataClassReceiver = gson.fromJson(res, DataClassReceiver::class.java)
//                dataClassReceiver?.conversationId  =msgList[i].conversationId()
//                Log.d(TAG, "receiverCount uid:${shp!!.getUid()} ")
//                //判断是不是在当前打开页面的群发来的消息
//                if (groupThirdPartyId.value.equals(dataClassReceiver?.conversationId)){
//                    if (shp!!.getUid() != dataClassReceiver?.fromUserId){
//                        fromUser = FromUser(0, 0, "", "", 0)
//                        fromUser?.userName = dataClassReceiver?.fromUserName
//                        fromUser?.userType = dataClassReceiver?.fromUserType
//                        imMessageList = ImMessageList(0, 2, fromUser, "", "", 0, "", false)
//                        imMessageList?.fromUser = fromUser
//                        imMessageList?.gmtCreate = DateUtil.stamp2Date(System.currentTimeMillis())
//                        imMessageList?.messageType = dataClassReceiver?.messageType
//                        imMessageList?.message = dataClassReceiver?.messageContent
//                        if (dataClassReceiver?.messageType == 2){
//                            imageList.value?.add(dataClassReceiver?.messageContent!!)
//                        }
//                        receiverList.value?.add(0, imMessageList!!)
//                        fromUser = null
//                        imMessageList = null
//                    }
//                }
//
//                dataClassReceiverList.value?.add(dataClassReceiver!!)
//                Log.d(TAG, "receiverCount:${dataClassReceiverList.value} ")
//            }
//            CoroutineScope(Dispatchers.Main).launch {
//                if (dataClassReceiverList.value!!.size > 0){
//                    Log.d(TAG, "receiverCount:收到消息receiverCount+1 = ${receiverCount.value!! + 1} ")
//                    Log.d(TAG, "receiverCount:getAppStatus ${getAppStatus()} ")
//                    if (shp!!.getUid() != dataClassReceiverList.value!![dataClassReceiverList.value!!.size-1].fromUserId){
//                        if (getAppStatus() == true ){
//                            if (!shp?.getToken().equals("")){
//                                if (ToolUtils.isSilentMode(ctx!!).equals("normal")){
//
//                                    ToolUtils.defaultMediaPlayer(ctx!!)
//                                    ToolUtils.playVibrate(ctx!!,false)
//                                }else if (ToolUtils.isSilentMode(ctx!!).equals("vibrate")){
//                                    Log.d("TAG", "appstatus vibrate: ")
//                                    ToolUtils.playVibrate(ctx!!,false)
//                                }
//                            }
//
//                        }else {
//                            if (!shp?.getToken().equals("")){
//
//                                val name = dataClassReceiverList.value!![dataClassReceiverList.value!!.size-1].fromUserName
//                                val type = dataClassReceiverList.value!![dataClassReceiverList.value!!.size-1].messageType
//                                val content = dataClassReceiverList.value!![dataClassReceiverList.value!!.size-1].messageContent
//                                Log.d(TAG, "type:$type ")
//                                ToastUtil.showToast(ctx!!,content!!)
//                                NotificationUtil.notification("八戒睡眠管理端",name!!,type!!,content!!,ctx!!)
//                            }
//
//
//                        }
////                        val name = dataClassReceiverList.value!![dataClassReceiverList.value!!.size-1].fromUserName
////                        val type = dataClassReceiverList.value!![dataClassReceiverList.value!!.size-1].messageType
////                        val content = dataClassReceiverList.value!![dataClassReceiverList.value!!.size-1].messageContent
////
////                        if (!shp?.getToken().equals("")){
////
////
////                            Log.d(TAG, "type:$type ")
////                            ToastUtil.showToast(ctx!!,content!!)
////                            NotificationUtil.notification("八戒睡眠管理端",name!!,type!!,content!!,ctx!!)
//////                            NotificationUtil.notification2(ctx!!,content)
////
////                        }
//
//
//                    }
//
//
//                    receiverCount.value = receiverCount.value!! + 1
//                }
//
//            }
//        }catch (e:Exception){
//            Log.d(TAG, "receiverCount 没走下去$e ")
//        }
//
//
//
//    }// 收到消息，遍历消息队列，解析和显示。

    //监听群组回调
    val groupAcceptInvitation = MutableLiveData(0)


}