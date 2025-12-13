package com.yulink.texas.common.exceptions;

import com.yulink.texas.common.exception.ErrorCode;
import com.yulink.texas.common.utils.StringUtil;

/**
 * @desc 内部服务异常
 */
public class InternalServerException extends BusinessException {

    public InternalServerException() {
        super(ErrorCode.SYSTEM_INNER_ERROR);
    }

    public InternalServerException(String msg, Throwable cause) {
        super(ErrorCode.SYSTEM_INNER_ERROR);
        this.message = StringUtil.formatIfArgs(msg, "{}", cause);
    }

    public InternalServerException(String msg, Throwable cause, Object... objects) {
        super(ErrorCode.SYSTEM_INNER_ERROR);
        this.message = StringUtil.formatIfArgs(msg, "{}", cause, objects);
    }

    public InternalServerException(String msg) {
        super(ErrorCode.SYSTEM_INNER_ERROR, msg);
    }

    public InternalServerException(String formatMsg, Object... objects) {
        super(ErrorCode.SYSTEM_INNER_ERROR, formatMsg);
        this.message = StringUtil.formatIfArgs(formatMsg, "{}", objects);
    }

}
