package com.yulink.texas.common.utils;

import com.yulink.texas.common.utils.date.DateUtil;
import java.util.Collection;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @Author: chao.jiang
 * @Date: 2025/12/16
 * @Copyright (c) bitmain.com All Rights Reserved
 */
public class CodeUtil {
    public static final String VERIFY_CODE_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    /**
     * 返回6位随机加密短码
     * @return
     */
    public static String encryptCode(){
        return RandomStringUtils.random(8, "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890");
    }


    /**
     * 获取精确到秒的序号作为编号
     * @param prefix 前缀
     * @return
     */
    public static String sequenceNo(String prefix){
        return prefix+ DateUtil.getTimeStampString()+RandomStringUtils.random(6, "1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }


    /**
     * 生成指定长度的随机码
     * @return [a-zA-Z0-9]
     */
    public static String batchSmsVerifyCode(int length) {
        return RandomStringUtils.random(length, VERIFY_CODE_CHARS);
    }

    /**
     * 生成预订单号
     *
     * @return
     */
    public static String preOrderSequenceNo() {
        return "PREORD" + DateUtil.getTimeStampString().substring(0, 10)
            + RandomStringUtils.random(8, "1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ") + getNanoTime();
    }

    /**
     * 获取纳秒数
     * @return
     */
    private static String getNanoTime() {
        return StringUtils.substring(String.valueOf(System.nanoTime()), -16);
    }


    /**
     * 使用给定的字符拼接集合, 调用集合中的元素的toString()方法
     * @param collection 待拼接集合
     * @param separator 分隔符
     * @return 当collection为null或size=0时返回"", 否则返回格式 "1123123,kjkjkj,123kjkj" 的字符串
     */
    public static String join(Collection collection, String separator) {
        StringBuilder sb = new StringBuilder();
        if (collection == null || collection.size() == 0) {
            return "";
        }

        for (Object o : collection) {
            sb.append(o.toString());
            sb.append(separator);
        }
        return sb.toString().substring(0, sb.toString().length() - 1);
    }

    /**
     * 调用参数的toString()方法并且判断其内容是否为空
     * @param obj 待检测对象
     * @return 当obj为null, obj.toString().trim().length == 0 时返回true
     */
    public static boolean isObjectBlank(Object obj) {
        return obj == null || obj.toString().trim().length() == 0;
    }

}
