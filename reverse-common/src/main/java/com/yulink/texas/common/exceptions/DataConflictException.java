package com.yulink.texas.common.exceptions;

import com.yulink.texas.common.exception.ErrorCode;
import com.yulink.texas.common.utils.StringUtil;

/**
 * @desc 数据已经存在异常
 */
public class DataConflictException extends BusinessException {

    public DataConflictException() {
        super(ErrorCode.DATA_ALREADY_EXISTED);
    }

    public DataConflictException(Object data) {
        super(ErrorCode.DATA_ALREADY_EXISTED);
        this.data = data;
    }

    public DataConflictException(ErrorCode resultCode) {
        super(resultCode);
    }

    public DataConflictException(ErrorCode resultCode, Object data) {
        super(resultCode, data);
    }

    public DataConflictException(String msg) {
        super(ErrorCode.DATA_ALREADY_EXISTED, msg);
    }

    public DataConflictException(String formatMsg, Object... objects) {
        super(ErrorCode.DATA_ALREADY_EXISTED);
        this.message = StringUtil.formatIfArgs(formatMsg, "{}", objects);
    }

}
