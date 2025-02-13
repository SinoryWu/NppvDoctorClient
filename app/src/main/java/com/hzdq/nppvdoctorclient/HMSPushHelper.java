package com.hzdq.nppvdoctorclient;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

import com.huawei.agconnect.AGConnectOptionsBuilder;
import com.huawei.hms.aaid.HmsInstanceId;
import com.huawei.hms.common.ApiException;

import java.lang.reflect.Method;

/**
 * Created by lzan13 on 2018/4/10.
 */

public class HMSPushHelper {

    private static HMSPushHelper instance;

    private HMSPushHelper(){}

    public static HMSPushHelper getInstance() {
        if (instance == null) {
            instance = new HMSPushHelper();
        }
        return instance;
    }

    /**
     * 申请华为Push Token
     * 1、getToken接口只有在AppGallery Connect平台开通服务后申请token才会返回成功。
     *
     * 2、EMUI10.0及以上版本的华为设备上，getToken接口直接返回token。如果当次调用失败Push会缓存申请，之后会自动重试申请，成功后则以onNewToken接口返回。
     *
     * 3、低于EMUI10.0的华为设备上，getToken接口如果返回为空，确保Push服务开通的情况下，结果后续以onNewToken接口返回。
     *
     * 4、服务端识别token过期后刷新token，以onNewToken接口返回。
     */
    public void getHMSToken(Activity activity){

        try {
            if(Class.forName("com.huawei.hms.api.HuaweiApiClient") != null){

                Class<?> classType = Class.forName("android.os.SystemProperties");
                Log.d("HWHMSPush", "classType: "+classType);
                Method getMethod = classType.getDeclaredMethod("get", new Class<?>[] {String.class});
                String buildVersion = (String)getMethod.invoke(classType, new Object[]{"ro.build.version.emui"});
                Log.d("HWHMSPush", "getHMSToken: "+buildVersion);
                //在某些手机上，invoke方法不报错
                if(!TextUtils.isEmpty(buildVersion)){
                    Log.d("HWHMSPush", "huawei hms push is available!");
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                // read from agconnect-services.json
//                                String appId = AGConnectServicesConfig.fromContext(activity).getString("client/app_id");
                                String appId = new AGConnectOptionsBuilder().build(activity).getString("client/app_id");

                                Log.e("AGConnectOptionsBuilder","appId:"+appId);
                                // 申请华为推送token
                                String token = HmsInstanceId.getInstance(activity).getToken(appId, "HCM");

                                Log.d("HWHMSPush", "get huawei hms push token:" + token);
                                if(token != null && !token.equals("")){
                                    //没有失败回调，假定token失败时token为null
                                    Log.d("HWHMSPush", "register huawei hms push token success token:" + token);
                                    // 上传华为推送token

                                }else{

                                    Log.e("HWHMSPush", "register huawei hms push token fail!");
                                }
                            } catch (ApiException e) {

                            }
                        }
                    }.start();
                }else{

                    Log.d("HWHMSPush", "huawei hms push is unavailable!");
                }
            }else{

                Log.d("HWHMSPush", "no huawei hms push sdk or mobile is not a huawei phone");
            }
        } catch (Exception e) {
            Log.d("HWHMSPush", "no huawei hms push sdk or mobile is not a huawei phone");
        }
    }
}
