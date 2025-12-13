package com.yulink.texas.common.exceptions;

import com.yulink.texas.common.exception.ErrorCode;
import com.yulink.texas.common.utils.StringUtil;

/**
 * @desc 远程访问异常
 */
public class RemoteAccessException extends BusinessException {

    public RemoteAccessException() {
        super(ErrorCode.INTERFACE_OUTTER_INVOKE_ERROR);
    }

    public RemoteAccessException(Object data) {
        super(ErrorCode.INTERFACE_OUTTER_INVOKE_ERROR);
        this.data = data;
    }

    public RemoteAccessException(ErrorCode resultCode) {
        super(resultCode);
    }

    public RemoteAccessException(ErrorCode resultCode, Object data) {
        super(resultCode, data);
    }

    public RemoteAccessException(String msg) {
        super(ErrorCode.INTERFACE_OUTTER_INVOKE_ERROR, msg);
    }

    public RemoteAccessException(String formatMsg, Object... objects) {
        super(ErrorCode.INTERFACE_OUTTER_INVOKE_ERROR);
        this.message = StringUtil.formatIfArgs(formatMsg, "{}", objects);
    }

}
