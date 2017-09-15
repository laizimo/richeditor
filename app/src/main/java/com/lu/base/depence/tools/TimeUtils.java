package com.lu.base.depence.tools;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by 陆正威 on 2017/4/20.
 */

public class TimeUtils {
    private final static Calendar calendar = Calendar.getInstance(TimeZone.getDefault());

    public static long getCurTime() {
        return calendar.getTimeInMillis();
    }

    public static String getCurStringTime() {
        return calendar.getTime().toString();
    }

    public static String getCurTimeYMD() {
        return calendar.get(Calendar.YEAR) + "年 "
                + calendar.get(Calendar.MONTH) + "月 "
                + calendar.get(Calendar.DAY_OF_MONTH) + "日 "
                //+ calendar.get(Calendar.HOUR_OF_DAY) + "时 "
                //+ calendar.get(Calendar.MINUTE) + "分 "
                //+ calendar.get(Calendar.SECOND) + "秒"
                ;
    }

    public static String getCurTimeHMS() {
        return //calendar.get(Calendar.YEAR) + "年 "
                //+ calendar.get(Calendar.MONTH) + "月 "
                //+ calendar.get(Calendar.DAY_OF_MONTH) + "日 "
                +calendar.get(Calendar.HOUR_OF_DAY) + "时 "
                        + calendar.get(Calendar.MINUTE) + "分 "
                        + calendar.get(Calendar.SECOND) + "秒"
                ;
    }

    public static String getCurTimeMDHM() {
        return //calendar.get(Calendar.YEAR) + "年 "
                +calendar.get(Calendar.MONTH) + "月 "
                        + calendar.get(Calendar.DAY_OF_MONTH) + "日 "
                        + calendar.get(Calendar.HOUR_OF_DAY) + "时 "
                        + calendar.get(Calendar.MINUTE) + "分 "
                        //+ calendar.get(Calendar.SECOND) + "秒"
                ;
    }

    public static String getCurTimeMDH() {
        return //calendar.get(Calendar.YEAR) + "年 "
                +calendar.get(Calendar.MONTH) + "月 "
                        + calendar.get(Calendar.DAY_OF_MONTH) + "日 "
                        + calendar.get(Calendar.HOUR_OF_DAY) + "时 "
                        //+ calendar.get(Calendar.MINUTE) + "分 "
                //+ calendar.get(Calendar.SECOND) + "秒"
                ;
    }

}
