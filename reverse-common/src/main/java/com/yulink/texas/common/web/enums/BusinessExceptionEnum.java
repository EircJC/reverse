package com.yulink.texas.common.web.enums;

import com.yulink.texas.common.exception.ErrorCode;
import com.yulink.texas.common.exceptions.BusinessException;
import com.yulink.texas.common.exceptions.DataConflictException;
import com.yulink.texas.common.exceptions.DataNotFoundException;
import com.yulink.texas.common.exceptions.InternalServerException;
import com.yulink.texas.common.exceptions.MethodNotAllowException;
import com.yulink.texas.common.exceptions.ParameterInvalidException;
import com.yulink.texas.common.exceptions.PermissionForbiddenException;
import com.yulink.texas.common.exceptions.RemoteAccessException;
import com.yulink.texas.common.exceptions.UserNotLoginException;
import org.springframework.http.HttpStatus;

/**
 * @desc 异常、HTTP状态码、默认自定义返回码 映射类
 *
 * @Author: liupanpan
 * @Date: 2020/5/14
 * @Copyright (c) 2013, yulink.io All Rights Reserved
 */

public enum BusinessExceptionEnum {

    /**
     * 无效参数
     */
    PARAMETER_INVALID(ParameterInvalidException.class, HttpStatus.BAD_REQUEST, ErrorCode.PARAM_IS_INVALID),

    /**
     * 数据未找到
     */
    NOT_FOUND(DataNotFoundException.class, HttpStatus.NOT_FOUND, ErrorCode.RESULE_DATA_NONE),

    /**
     * 接口方法不允许
     */
    METHOD_NOT_ALLOWED(MethodNotAllowException.class, HttpStatus.METHOD_NOT_ALLOWED, ErrorCode.INTERFACE_ADDRESS_INVALID),

    /**
     * 数据已存在
     */
    CONFLICT(DataConflictException.class, HttpStatus.CONFLICT, ErrorCode.DATA_ALREADY_EXISTED),

    /**
     * 用户未登录
     */
    UNAUTHORIZED(UserNotLoginException.class, HttpStatus.UNAUTHORIZED, ErrorCode.USER_NOT_LOGGED_IN),

    /**
     * 无访问权限
     */
    FORBIDDEN(PermissionForbiddenException.class, HttpStatus.FORBIDDEN, ErrorCode.PERMISSION_NO_ACCESS),

    /**
     * 远程访问时错误
     */
    REMOTE_ACCESS_ERROR(RemoteAccessException.class, HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERFACE_OUTTER_INVOKE_ERROR),

    /**
     * 系统内部错误
     */
    INTERNAL_SERVER_ERROR(InternalServerException.class, HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.SYSTEM_INNER_ERROR);

    private Class<? extends BusinessException> eClass;

    private HttpStatus httpStatus;

    private ErrorCode resultCode;

    BusinessExceptionEnum(Class<? extends BusinessException> eClass, HttpStatus httpStatus, ErrorCode resultCode) {
        this.eClass = eClass;
        this.httpStatus = httpStatus;
        this.resultCode = resultCode;
    }

    public Class<? extends BusinessException> getEClass() {
        return eClass;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public ErrorCode getResultCode() {
        return resultCode;
    }

    public static boolean isSupportHttpStatus(int httpStatus) {
        for (BusinessExceptionEnum exceptionEnum : BusinessExceptionEnum.values()) {
            if (exceptionEnum.httpStatus.value() == httpStatus) {
                return true;
            }
        }

        return false;
    }

    public static boolean isSupportException(Class<?> z) {
        for (BusinessExceptionEnum exceptionEnum : BusinessExceptionEnum.values()) {
            if (exceptionEnum.eClass.equals(z)) {
                return true;
            }
        }

        return false;
    }

    public static BusinessExceptionEnum getByHttpStatus(HttpStatus httpStatus) {
        if (httpStatus == null) {
            return null;
        }

        for (BusinessExceptionEnum exceptionEnum : BusinessExceptionEnum.values()) {
            if (httpStatus.equals(exceptionEnum.httpStatus)) {
                return exceptionEnum;
            }
        }

        return null;
    }

    public static BusinessExceptionEnum getByEClass(Class<? extends BusinessException> eClass) {
        if (eClass == null) {
            return null;
        }

        for (BusinessExceptionEnum exceptionEnum : BusinessExceptionEnum.values()) {
            if (eClass.equals(exceptionEnum.eClass)) {
                return exceptionEnum;
            }
        }

        return null;
    }
}
