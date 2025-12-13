package com.yulink.texas.common.exceptions;

import com.yulink.texas.common.exception.ErrorCode;
import com.yulink.texas.common.utils.StringUtil;

/**
 * @desc 参数无效异常
 */
public class ParameterInvalidException extends BusinessException {

    public ParameterInvalidException() {
        super(ErrorCode.PARAM_IS_INVALID);
    }

    public ParameterInvalidException(Object data) {
        super(ErrorCode.PARAM_IS_INVALID);
        super.data = data;
    }

    public ParameterInvalidException(ErrorCode resultCode) {
        super(resultCode);
    }

    public ParameterInvalidException(ErrorCode resultCode, Object data) {
        super(resultCode, data);
    }

    public ParameterInvalidException(String msg) {
        super(ErrorCode.PARAM_IS_INVALID, msg);
    }

    public ParameterInvalidException(String formatMsg, Object... objects) {
        super(ErrorCode.PARAM_IS_INVALID);
        this.message = StringUtil.formatIfArgs(formatMsg, "{}", objects);
    }

}
