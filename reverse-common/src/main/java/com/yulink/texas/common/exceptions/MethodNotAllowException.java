package com.yulink.texas.common.exceptions;

import com.yulink.texas.common.exception.ErrorCode;
import com.yulink.texas.common.utils.StringUtil;

/**
 * @desc 方法不允许异常
 */
public class MethodNotAllowException extends BusinessException {

    public MethodNotAllowException() {
        super(ErrorCode.INTERFACE_ADDRESS_INVALID);
    }

    public MethodNotAllowException(Object data) {
        super(ErrorCode.INTERFACE_ADDRESS_INVALID);
        this.data = data;
    }

    public MethodNotAllowException(ErrorCode resultCode) {
        super(resultCode);
    }

    public MethodNotAllowException(ErrorCode resultCode, Object data) {
        super(resultCode, data);
    }

    public MethodNotAllowException(String msg) {
        super(ErrorCode.INTERFACE_ADDRESS_INVALID, msg);
    }

    public MethodNotAllowException(String formatMsg, Object... objects) {
        super(ErrorCode.INTERFACE_ADDRESS_INVALID);
        this.message = StringUtil.formatIfArgs(formatMsg, "{}", objects);
    }

}
