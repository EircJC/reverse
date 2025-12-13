package com.yulink.texas.common.utils.date;

import org.joda.time.DateTime;

/**
 * Need class description here...
 *
 * @Author: liupanpan
 * @Date: 2018/11/20
 * @Copyright (c) 2013, yulink.io All Rights Reserved
 */

public class TimeUtil {

    public final static long SECONDS_MIN_1 = 60;
    public final static long SECONDS_HOUR_1 = 60 * 60;
    public final static long SECONDS_HOUR_3 = 60 * 60 * 3;
    public final static long SECONDS_HOUR_6 = 60 * 60 * 6;
    public final static long SECONDS_HOUR_12 = 60 * 60 * 12;
    public final static long SECONDS_DAY_1 = 60 * 60 * 24;
    public final static long SECONDS_DAY_3 = 3 * 60 * 60 * 24;
    public final static long SECONDS_DAY_7 = 7 * 60 * 60 * 24;

    public final static long MILLIS_SECOND_1 = 1000 ;
    public final static long MILLIS_MIN_1 = 1000 * 60;
    public final static long MILLIS_MIN_10 = 1000 * 60 * 10;
    public final static long MILLIS_HOUR_1 = 1000 * 60 * 60;
    public final static long MILLIS_DAY_1 = 1000 * 60 * 60 * 24;
    public final static long MILLIS_YEAR_1 = MILLIS_DAY_1 * 365;



    public final static long HOURS_DAY_1 = 24;

    public static DateTime lastDays(DateTime dateTime, int days) {
        DateTime buildTime = dateTime.withTime(0, 0, 0, 0);
        return new DateTime(buildTime.getMillis() - MILLIS_DAY_1 * days);
    }

    public static DateTime lastHours(DateTime dateTime, int hours) {
        DateTime buildTime = dateTime.withTime(dateTime.getHourOfDay(), 0, 0, 0);
        return new DateTime(buildTime.getMillis() - MILLIS_HOUR_1 * hours);
    }

    /**
     * 向下取整时间
     */
    public static long buildLowerTime(Long time, long interval) {
        return time / interval * interval ;
    }


    /**
     * 向上取整
     * @param time
     * @param interval
     * @return
     */
    public static long buildUpperTime(Long time, long interval) {
        return time / interval * interval + interval;
    }

    public static boolean lessThanOneDay(long timeSpan) {
        return timeSpan / SECONDS_HOUR_1 < HOURS_DAY_1;
    }
}
