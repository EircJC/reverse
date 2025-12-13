package com.yulink.texas.common.utils.date;


import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DateUtil {

    public static final DateTimeFormatter yyyyMMDDfmt = DateTimeFormat.forPattern("yyyy-MM-dd");
    private static final DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter dayFmt = DateTimeFormat.forPattern("yyyy-MM-dd");
    public static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final SimpleDateFormat day = new SimpleDateFormat("dd");
    public static final SimpleDateFormat standardTime = new SimpleDateFormat("yyyy-MM-dd");

    private static final int TIMESTAMP_SECOND_LENGTH = 10;
    private static final DateTimeFormatter ddHHmm = DateTimeFormat.forPattern("ddHHmm");


    private DateUtil() {

    }

    public static String getStringFromByLong(Long date) {
        if (null == date) {
            return null;
        }
        return fmt.print(date);
    }

    public static Long getLongByStringDate(String time) {
        DateTime date;
        try {
            date = yyyyMMDDfmt.parseDateTime(time);
        } catch (Exception e) {
            return null;
        }
        if (null != date) {
            return date.getMillis();
        }
        return null;
    }

    public static Long getLongByString(String time) {
        DateTime date;
        try {
            date = fmt.parseDateTime(time);
        } catch (Exception e) {
            return null;
        }
        if (null != date) {
            return date.getMillis();
        }
        return null;
    }

    public static String dateStringByDate(Date time) {
        try {
            return yyyyMMDDfmt.print(time.getTime());
        } catch (Exception e) {
            return null;
        }
    }

    public static Date getCurrentDay(DateTime dateTime) {
        final int milliSecondsOfDay = dateTime.millisOfDay().get();
        return dateTime.minus(milliSecondsOfDay).toDate();
    }

    public static DateTime toDateTime(String dateTimeString){
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        DateTime dt = formatter.parseDateTime(dateTimeString);
        return dt;
    }

    public static Date startOfDay(Date date) {
        final DateTime dateTime = new DateTime(date).withTime(0, 0, 0, 0);
        return dateTime.toDate();
    }

    public static DateTime startOfDay(DateTime date) {
        final DateTime dateTime = date.withTime(0, 0, 0, 0);
        return dateTime;
    }

    public static Date nextDay(Date date) {
        final DateTime dateTime = new DateTime(date).plusDays(1);
        return dateTime.toDate();
    }

    public static Date endOfDay(Date date) {
        final DateTime dateTime = new DateTime(date).withTime(23, 59, 59, 999);
        return dateTime.toDate();
    }

    /**
     * 时间戳转毫秒级时间戳
     * 此方法适用于秒级时间戳小于等于10位 11位时间是2286/11/21 1:46:40
     * 毫秒级大于10位 10位毫秒级最大为 1970/4/27 1:46:39
     * @param secondTimestamp（时间戳）
     * @return
     */
    public static long getMillisecondTimestamp(Long secondTimestamp){
        if (secondTimestamp.toString().length() <= TIMESTAMP_SECOND_LENGTH){
            secondTimestamp = secondTimestamp * 1000;
        }
        return secondTimestamp;
    }

    public static String getMinuteString(Date date) {
        return ddHHmm.print(new DateTime(date).toDateTime());
    }


    /**
     * 获取小时开始的时间
     * @param date
     * @return
     */
    public static Date toHourStart(Date date) {
        return new DateTime(date).withMillisOfSecond(0).withSecondOfMinute(0).withMinuteOfHour(0).toDate();
    }

    /**
     * 获取分钟开始的时间
     * @param date
     * @return
     */
    public static Date toMinuteStart(Date date) {
        DateTime dt = new DateTime(date);

        return new DateTime(date).withMillisOfSecond(0).withSecondOfMinute(0).toDate();
    }

    public static Date getDateFromSimpleStr(String str) {
        if (StringUtils.isBlank(str)) {
            return null;
        }
        return dayFmt.parseDateTime(str).toDateTime(DateTimeZone.UTC).toDate();
    }

    public static Date getDateFromString(String str) {
        if (StringUtils.isBlank(str)) {
            return null;
        }

        return fmt.parseDateTime(str).toDateTime(DateTimeZone.UTC).toDate();
    }

    public static String getStringFromDate(Date date) {
        return fmt.print(new DateTime(date));
    }

    public static String getShortStringFromDate(Date date) {
        return dayFmt.print(new DateTime(date).toDateTime());
    }

    public static String getStringFromByDate(Date date) {
        if (null == date)
            return null;
        return format.format(date);
    }

    public static String getStringFromByStandard(Date date) {
        if (null == date)
            return null;
        return standardTime.format(date);
    }

    public static Long getCurrentTimestamp() {
        Date date = new Date();
        return date.getTime();
    }


    /**
     * 当前日
     * @return
     */
    public static String getDay(){
        Date aDate = new Date();
        return day.format(aDate);
    }

    public static Date getDateByString(String time) {
        DateTime date;
        try {
            date = fmt.parseDateTime(time);
        } catch (Exception e) {
            return null;
        }
        if (null != date) {
            return date.toDate();
        }
        return null;
    }


}

