package com.yulink.texas.common.exceptions;

import com.yulink.texas.common.exception.ErrorCode;
import com.yulink.texas.common.utils.StringUtil;

/**
 * @desc 用户未登录异常
 */
public class UserNotLoginException extends BusinessException {

    public UserNotLoginException() {
        super(ErrorCode.USER_NOT_LOGGED_IN);
    }

    public UserNotLoginException(String msg) {
        super(ErrorCode.USER_NOT_LOGGED_IN, msg);
    }

    public UserNotLoginException(String formatMsg, Object... objects) {
        super(ErrorCode.USER_NOT_LOGGED_IN);
        this.message = StringUtil.formatIfArgs(formatMsg, "{}", objects);
    }

}
