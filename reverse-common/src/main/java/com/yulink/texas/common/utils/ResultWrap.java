package com.yulink.texas.common.utils;

import com.google.common.collect.ImmutableMap;
import com.yulink.texas.common.exception.ErrorCode;
import com.yulink.texas.common.model.Response;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;


public class ResultWrap {
    public static final String SUCCESS_CODE = "000000";

    public static final String DATA_ITEMS = "items";

    private ResultWrap() {
    }

    public static <T> Response<T> ok(T data) {
        return new Response<>(SUCCESS_CODE, StringUtils.EMPTY, data);
    }

    public static <T> Response<List<T>> ok(List<T> data) {
        Map<String, List<T>> items = ImmutableMap.of(DATA_ITEMS, data);
        return new Response(SUCCESS_CODE, StringUtils.EMPTY, items);
    }

    public static Response error(ErrorCode errorCode) {
        return new Response<>(String.valueOf(errorCode.getCode()), errorCode.getMsg());
    }

    public static Response error(int code, String msg) {
        return new Response(String.valueOf(code), msg);
    }

    public static Response error(String code, String msg) {
        return new Response(code, msg);
    }
}
