package com.yulink.texas.common.exception;


/**
 * HTTP异常
 *
 * @author zhenbaozhou
 * @date 2018/4/18
 * @copyright (c) 2018, yulink.io All Rights Reserved
 */
public class HttpException extends CommonException {

    public HttpException(String message) {
        super(ErrorConstant.HTTP_ERROR, message);
    }
}
