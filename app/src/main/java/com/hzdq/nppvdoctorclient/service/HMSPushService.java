package com.hzdq.nppvdoctorclient.service;

import android.util.Log;

import com.huawei.hms.push.HmsMessageService;


public class HMSPushService extends HmsMessageService {
    @Override
    public void onCreate() {
        Log.d("HWHMSPush", "onCreate: ");
        super.onCreate();
    }

    @Override
    public void onNewToken(String token) {
        if(token != null && !token.equals("")){
            //没有失败回调，假定token失败时token为null
        }else{

        }
    }
}
