package com.yulink.texas.common.exception;

public class ServiceException extends BaseException {

    public ServiceException(ErrorCode errorCode) {
        super(errorCode.getMsg(), errorCode);
    }

    public ServiceException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

    public ServiceException(String message, ErrorCode errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }
}
