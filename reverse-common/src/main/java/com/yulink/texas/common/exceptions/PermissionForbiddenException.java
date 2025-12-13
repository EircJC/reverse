package com.yulink.texas.common.exceptions;

import com.yulink.texas.common.exception.ErrorCode;
import com.yulink.texas.common.utils.StringUtil;

/**
 * @desc 权限不足异常
 */
public class PermissionForbiddenException extends BusinessException {

    public PermissionForbiddenException() {
        super(ErrorCode.PERMISSION_NO_ACCESS);
    }

    public PermissionForbiddenException(Object data) {
        super(ErrorCode.PERMISSION_NO_ACCESS);
        this.data = data;
    }

    public PermissionForbiddenException(ErrorCode resultCode) {
        super(resultCode);
    }

    public PermissionForbiddenException(ErrorCode resultCode, Object data) {
        super(resultCode, data);
    }

    public PermissionForbiddenException(String msg) {
        super(ErrorCode.PERMISSION_NO_ACCESS, msg);
    }

    public PermissionForbiddenException(String formatMsg, Object... objects) {
        super(ErrorCode.PERMISSION_NO_ACCESS);
        this.message = StringUtil.formatIfArgs(formatMsg, "{}", objects);
    }

}
