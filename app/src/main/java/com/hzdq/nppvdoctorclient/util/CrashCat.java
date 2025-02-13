package com.hzdq.nppvdoctorclient.util;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Process;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CrashCat implements Thread.UncaughtExceptionHandler {

    private static CrashCat crashCat;
    private Context mContext;
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    private static String DEVICE_INFO="";
    private File path;
    private File fileName;
    private FileOutputStream fileOutputStream;
    private BufferedOutputStream bufferedOutputStream;
    private static String FILE_NAME = "";
    private Intent intent;
    private PackageManager packageManager;
    private PackageInfo packageInfo;

    private CrashCat(Context context, String filePath, String fileName){
        init(context,filePath,fileName);
    }

    public static CrashCat getInstance(Context context, String filePath, String fileName){
        crashCat = new CrashCat(context,filePath,fileName);
        return  crashCat;
    }

    private void init(Context context, String filePath, String fileName){
        this.mContext = context;
        this.FILE_NAME = fileName;
        try {
            packageManager = mContext.getPackageManager();
            packageInfo = packageManager.getPackageInfo(mContext.getPackageName(),0);
            intent = packageManager.getLaunchIntentForPackage(mContext.getPackageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        } catch (Exception e) {
            writeLog(e.toString());
            intent = null;
        }
        path = new File(filePath);
        if (!path.exists()){
            path.mkdirs();
        }
        StringBuffer sb = new StringBuffer();
        sb.append("DeviceID="+ Build.ID+"\n"); //手机型号
        sb.append("AndroidApi="+ Build.VERSION.SDK_INT+"\n");//手机android版本号 数字 如23
        sb.append("AndroidVersion="+ Build.VERSION.RELEASE+"\n");//android版本 如6.0
        sb.append("Brand="+ Build.BRAND+"\n"); //手机商标
        sb.append("ManuFacture="+ Build.MANUFACTURER+"\n");//生产商
        sb.append("Model="+ Build.MODEL+"\n");//型号
        sb.append("PackageName="+mContext.getPackageName()+"\n"); //应用包名
        sb.append("CurrentVersionName="+packageInfo.versionName+"\n");//app版本号
        DEVICE_INFO = sb.toString();
        writeLog("Application Start");
    }

    public void start(){
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    private void writeLog(String log){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        log = "----------"+simpleDateFormat.format(new Date(System.currentTimeMillis())).toString()+"----------"+"\n"+log+"\n";
        try {
            fileName = new File(path+FILE_NAME);
            if (fileName.exists() && fileName.length() > 10485760){
                fileName.delete();
            }
            fileOutputStream = new FileOutputStream(fileName,true);
            bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            bufferedOutputStream.write(log.getBytes());
            bufferedOutputStream.flush();
            fileOutputStream.close();
            bufferedOutputStream.close();
        } catch (Exception e) {
            Log.e("IO Exception",e.toString());
        }
    }

    private void handlerException(String exception) {
        if (exception !=null){
            try{
                writeLog(DEVICE_INFO+exception.toString());
            }finally {
                try{
                    mContext.startActivity(intent);
                    Process.killProcess(Process.myPid());
                    System.exit(1);
                }catch (Exception e){
                    Log.e("App can not restart",e.toString());
                }
            }
        }
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        StackTraceElement[] stackTraceElements = e.getStackTrace();
        StringBuffer sb = new StringBuffer(e.toString()+"\n");
        for (int i=0,size = stackTraceElements.length;i<size;i++){
            sb.append(stackTraceElements[i].toString()+"\n");
        }
        Log.e("error",sb.toString());
        handlerException(sb.toString());
    }
}
