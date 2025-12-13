package com.yulink.texas.common.exceptions;

import com.yulink.texas.common.exception.ErrorCode;
import com.yulink.texas.common.utils.StringUtil;

/**
 * @desc 数据没有找到异常
 */
public class DataNotFoundException extends BusinessException {

    public DataNotFoundException() {
        super(ErrorCode.RESULE_DATA_NONE);
    }

    public DataNotFoundException(Object data) {
        super(ErrorCode.RESULE_DATA_NONE);
        this.data = data;
    }

    public DataNotFoundException(ErrorCode resultCode) {
        super(resultCode);
    }

    public DataNotFoundException(ErrorCode resultCode, Object data) {
        super(resultCode, data);
    }

    public DataNotFoundException(String msg) {
        super(ErrorCode.RESULE_DATA_NONE, msg);
    }

    public DataNotFoundException(String formatMsg, Object... objects) {
        super(ErrorCode.RESULE_DATA_NONE);
        this.message = StringUtil.formatIfArgs(formatMsg, "{}", objects);
    }

}
