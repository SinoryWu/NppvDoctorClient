package com.hzdq.nppvdoctorclient.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.hzdq.nppvdoctorclient.MainActivity
import com.hzdq.nppvdoctorclient.R

/**
 *Time:2023/3/29
 *Author:Sinory
 *Description:
 */
object NotificationUtil {
     fun notification(title:String,name:String,type:Int,content:String,context: Context){
        /**
         * 通知栏（兼容android 8.0以上）
         */
        val isVibrate = true //是否震动

        //1.获取消息服务
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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
                channel.vibrationPattern = longArrayOf(0, 200, 300, 200)
            } else {
                channel.enableVibration(false)
                channel.vibrationPattern = longArrayOf(0)
            }
            //创建消息通道
            manager.createNotificationChannel(channel)
        }
        //3.实例化通知
        val nc = NotificationCompat.Builder(
            context,
            channelId!!
        )
        //通知默认的声音 震动 呼吸灯
        nc.setDefaults(NotificationCompat.DEFAULT_ALL)
        //通知标题
        nc.setContentTitle(title)
        //通知内容
        if (type == 1){
            nc.setContentText("${name}：${content}")
        }else {
            nc.setContentText("${name}：[图片]")
        }
        //设置通知的小图标
        nc.setSmallIcon(R.mipmap.ic_launcher)
        //设置通知的大图标
        nc.setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))
        //设定通知显示的时间
        nc.setWhen(System.currentTimeMillis())
        //设置通知的优先级
        nc.priority = NotificationCompat.PRIORITY_MAX
        //设置点击通知之后通知是否消失
        nc.setAutoCancel(true)
        //点击通知打开软件
        val application: Context = context
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
}