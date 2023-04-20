package com.hzdq.nppvdoctorclient.util;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Time:2023/3/28
 * Author:Sinory
 * Description:
 */
public class TimeIntervalUtil {
    public static boolean isTimeDifferenceGreaterThan(String time1, String time2, int minutes) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date1 = format.parse(time1);
            Date date2 = format.parse(time2);

            long diff = Math.abs(date2.getTime() - date1.getTime());
//            long diffSeconds = diff / 1000;
            long diffMinutes = diff / 60000;
//            Log.d("asdsadsad", "diffSeconds: "+diffSeconds);
            return diffMinutes > minutes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
