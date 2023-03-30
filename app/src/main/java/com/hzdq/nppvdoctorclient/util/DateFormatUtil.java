package com.hzdq.nppvdoctorclient.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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


    /**
     * 像微信一样显示时间
     * @param dateString
     * @return
     */
    public static String likeWeChatTime(String dateString) {
        String result = "";
        DateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = null;
        try {
            date = inputDateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (date != null) {
            Calendar inputCal = Calendar.getInstance();
            inputCal.setTime(date);

            Calendar currentCal = Calendar.getInstance();

            // Check if the given date is today
            if (inputCal.get(Calendar.YEAR) == currentCal.get(Calendar.YEAR) &&
                    inputCal.get(Calendar.DAY_OF_YEAR) == currentCal.get(Calendar.DAY_OF_YEAR)) {
                SimpleDateFormat todayFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                result = todayFormat.format(date);
            }
            // Check if the given date is yesterday
            else if (inputCal.get(Calendar.YEAR) == currentCal.get(Calendar.YEAR) &&
                    inputCal.get(Calendar.DAY_OF_YEAR) == currentCal.get(Calendar.DAY_OF_YEAR) - 1) {
                int hourOfDay = inputCal.get(Calendar.HOUR_OF_DAY);
                if (hourOfDay >= 0 && hourOfDay < 6) {
                    SimpleDateFormat yesterdayFormat = new SimpleDateFormat("昨天 凌晨HH:mm", Locale.getDefault());
                    result = yesterdayFormat.format(date);
                } else if (hourOfDay >= 6 && hourOfDay < 12) {
                    SimpleDateFormat yesterdayFormat = new SimpleDateFormat("昨天 上午HH:mm", Locale.getDefault());
                    result = yesterdayFormat.format(date);
                } else if (hourOfDay >= 12 && hourOfDay < 18) {
                    SimpleDateFormat yesterdayFormat = new SimpleDateFormat("昨天 下午HH:mm", Locale.getDefault());
                    result = yesterdayFormat.format(date);
                } else if (hourOfDay >= 18 && hourOfDay < 24) {
                    SimpleDateFormat yesterdayFormat = new SimpleDateFormat("昨天 晚上HH:mm", Locale.getDefault());
                    result = yesterdayFormat.format(date);
                }
            } else {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                result = dateFormat.format(date);
            }
        }

        return result;
    }

}
