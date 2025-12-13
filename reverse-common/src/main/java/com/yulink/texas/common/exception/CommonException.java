package com.yulink.texas.common.exception;

/**
 * yulink 里的公共exception.
 *
 * @author zhenbaozhou
 * @date 2018/4/12
 * @Copyright (c) 2018, yulink.io All Rights Reserved
 */
public class CommonException extends RuntimeException {

    private final String code;
    private final String message;

    public CommonException(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMsg();
    }

    public CommonException(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public CommonException(String code) {
        this.code = code;
        this.message = null;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

//    @Override
//    public Throwable fillInStackTrace() {
//        return null;
//    }
}
