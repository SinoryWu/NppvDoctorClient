package com.hzdq.nppvdoctorclient.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.hzdq.nppvdoctorclient.R;


public class ToastUtil {

    private static Toast toast = null;

    public static void showToast(Context context,
                                 String content) {
        View toastview= LayoutInflater.from(context).inflate(R.layout.layout_toast,null);
        TextView text = (TextView) toastview.findViewById(R.id.tv_toast);

        if (toast == null) {
            text.setText(content);
            toast = new Toast(context);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setView(toastview);
        } else {
            text.setText(content);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setView(toastview);
        }

        toast.show();


    }

    /**
     * 这个方法可以顶掉前一个toast只保证屏幕上有一个toast显示
     * @param context
     * @param content
     */
    public static void showToast2(Context context,
                                  String content) {
        View toastview= LayoutInflater.from(context).inflate(R.layout.layout_toast,null);
        TextView text = (TextView) toastview.findViewById(R.id.tv_toast);
        text.setText(content);
        if (toast == null) {

            toast = new Toast(context);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setView(toastview);
            toast.show();
        } else {

            toast.cancel();
            toast = null;
            toast = new Toast(context);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setView(toastview);
            toast.show();
        }



    }




    public static void cancelToast(){
        if (toast != null){
            toast.cancel();
        }

    }
}
