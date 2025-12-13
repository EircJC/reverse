package com.yulink.texas.common.exceptions;

import com.yulink.texas.common.exception.ErrorCode;
import com.yulink.texas.common.utils.StringUtil;
import lombok.Data;

/**
 * Need class description here...
 *
 * @Author: liupanpan
 * @Date: 2020/5/14
 * @Copyright (c) 2013, yulink.io All Rights Reserved
 */

@Data
public class BusinessException extends RuntimeException {


    protected String code;

    protected String message;

    protected ErrorCode resultCode;

    protected Object data;

    public BusinessException() {
    }

    public BusinessException(String message) {
        this();
        this.message = message;
    }

    public BusinessException(String code, String message) {
        this();
        this.code = code;
        this.message = message;
    }

    public BusinessException(String format, Object... objects) {
        this();
        this.message = StringUtil.formatIfArgs(format, "{}", objects);
    }

    public BusinessException(ErrorCode resultCode, Object data) {
        this(resultCode);
        this.data = data;
    }

    public BusinessException(ErrorCode resultCode, String message) {
        this(resultCode);
        this.message = message;
    }

    public BusinessException(ErrorCode resultCode) {
        this.resultCode = resultCode;
        this.code = resultCode.getCode().toString();
        this.message = resultCode.getMsg();
    }

}
