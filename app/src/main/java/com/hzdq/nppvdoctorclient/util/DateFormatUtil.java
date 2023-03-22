package com.hzdq.nppvdoctorclient.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Time:2023/3/20
 * Author:Sinory
 * Description:
 */
public class DateFormatUtil {

    public static String getData(String inputDate) {

        // 输入的日期字符串
//        String inputDate = "2023-03-19 16:30:00";

        // 定义需要的日期格式
        String outputFormat;
        Calendar inputCalendar = Calendar.getInstance();
        inputCalendar.setTime(parseDate(inputDate));
        Calendar currentCalendar = Calendar.getInstance();
        if (isSameDay(inputCalendar, currentCalendar)) {
            outputFormat = "HH:mm";
        } else if (isYesterday(inputCalendar, currentCalendar)) {
            outputFormat = "昨天";
        } else if (isWithinWeek(inputCalendar, currentCalendar)) {
            outputFormat = "E";
        } else if (isWithinYear(inputCalendar, currentCalendar)) {
            outputFormat = "MM-dd";
        } else {
            outputFormat = "yyyy-MM-dd";
        }

        // 转换日期格式
        SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat outputDateFormat = new SimpleDateFormat(outputFormat);
        try {
            Date date = inputDateFormat.parse(inputDate);
            String outputDate = outputDateFormat.format(date);

            System.out.println(outputDate);
            return outputDate;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }


    }

    private static Date parseDate(String dateString) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return format.parse(dateString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
                && cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }

    private static boolean isYesterday(Calendar cal1, Calendar cal2) {
        cal2.add(Calendar.DAY_OF_MONTH, -1);
        return isSameDay(cal1, cal2);
    }

    private static boolean isWithinWeek(Calendar cal1, Calendar cal2) {
        long diff = cal2.getTimeInMillis() - cal1.getTimeInMillis();
        return diff <= 7 * 24 * 60 * 60 * 1000; // 一周的毫秒数
    }

    private static boolean isWithinYear(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
    }

}
