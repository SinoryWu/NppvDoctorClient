package com.hzdq.nppvdoctorclient.push;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.hyphenate.notification.EMNotificationMessage;
import com.hyphenate.notification.core.EMNotificationIntentReceiver;
import com.hzdq.nppvdoctorclient.MainActivity;

/**
 * Time:2023/3/24
 * Author:Sinory
 * Description:
 */
public class EasemobReceiver extends EMNotificationIntentReceiver {
    @Override
    public void onNotifyMessageArrived(Context context, EMNotificationMessage notificationMessage) {

        Log.d("EasemobReceiver", "onNotifyMessageArrived: "+notificationMessage);
        if(!notificationMessage.isNeedNotification()){
            String params = notificationMessage.getExtras(); // 判断是透传消息，获取附加字段去处理。

        }
    }

    @Override
    public void onNotificationClick(Context context, EMNotificationMessage notificationMessage) {
        super.onNotificationClick(context, notificationMessage);
    }
}
