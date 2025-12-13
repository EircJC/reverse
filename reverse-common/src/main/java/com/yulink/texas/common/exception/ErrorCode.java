package com.yulink.texas.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    SUCCESS("000000","success"),
    SERVER_API_SUCCESS("0","success"), // 小程序前端只能识别0为success
    ERROR_INTERNAL_SERVER("000001", "服务异常，请稍后重试"),
    ERROR_BAD_REQUEST("000002", "请求失败，请重试"),
    ERROR_UNAUTHORIZED("000003", "权限不足"),
    ERROR_FORBIDDEN("000004", "禁止访问"),
    ERROR_PARAM("000005", "参数错误"),
    ERROR_MEMORY("000006", "内存异常"),
    ERROR_NOT_EXIST("000007", "对象不存在"),
    ERROR_ALREADY_EXIST("000008", "对象已经存在"),
    ERROR_USER_NOT_EXIST("000009", "用户不存在"),
    ERROR_USER_PASSWORD("000010", "用户或密码错误"),
    ERROR_CAPTCHA("000013","验证码错误或者验证码已过期"),
    ERROR_USER_STATUS("000014","用户状态异常"),
    ERROR_USER_JWT("000015","认证失败, 请重新登录"),
    ERROR_ROLE_NOT_DEL("000017","此角色不能删除"),
    ERROR_ADMINUSER_NOT_DEL("000018","此管理员不能删除"),
    ERROR_PASSWORD_COMPLEXITY_NOT_MACHER("000019","密码复杂度不匹配,请保证:1、长度不小于6位 2、必须以字母开头 、必须包含特殊字符 4、必须包含数字"),
    ERROR_INPUT ("00003", "输入错误"),
    ERROR_ACCESS_KEY_UNAUTHORIZED("001006", "Unauthorized access"),
    ERROR_EMAIL_FORMAT("001009", "邮箱格式错误"),
    ERROR_EMAIL_MISMATCH("001011", "邮箱不匹配"),
    ERROR_LINK_OVERDUE("001012", "链接过期"),
    ERROR_EMAIL_IS_NULL("001018", "邮箱为空"),

    ERROR_CONTACT_NOT_EXIST("000021", "ERROR_CONTACT_NOT_EXIST"),
    ERROR_PHONE_ERROR("000025", "phone format error"),
    ERROR_EMAIL_ERROR("000026", "email format error"),
    ERROR_ALARM_REGISTER_TYPE("002011", "ERROR_ALARM_REGISTER_TYPE"),
    ERROR_ALARM_REGISTER_NUM("002012", "ERROR_ALARM_REGISTER_NUM"),

    NEED_TO_LOGIN("000403", "NEED_TO_LOGIN"),

    ERROR_UNKNOWN("999999", "未知错误"),



    /* 参数错误：10001-19999 */
    PARAM_IS_INVALID("10001", "参数无效"),
    PARAM_IS_BLANK("10002", "参数为空"),
    PARAM_TYPE_BIND_ERROR("10003", "参数类型错误"),
    PARAM_NOT_COMPLETE("10004", "参数缺失"),

    /* 用户错误：20001-29999*/
    USER_NOT_LOGGED_IN("20001", "用户未登录"),
    USER_LOGIN_ERROR("20002", "账号不存在或密码错误"),
    USER_ACCOUNT_FORBIDDEN("20003", "账号已被禁用"),
    USER_NOT_EXIST("20004", "用户不存在"),
    USER_HAS_EXISTED("20005", "用户已存在"),

    /* 业务错误：30001-39999 */
    SPECIFIED_QUESTIONED_USER_NOT_EXIST("30001", "某业务出现问题"),
    ERROR_UPLOAD_IMAGE_EMPTY("30002", "上传图片未空"),
    ERROR_UPLOAD_IMAGE_NUMBER_EXCEED("30003", "上传图片个数过多"),
    ERROR_UPLOAD_IMAGE_SIZE_EXCEED("30004", "上传图片大小超过上限"),
    ERROR_UPLOAD_IMAGE_FAIL("30005", "上传图片失败"),
    ERROR_IMAGE_NOT_EXIST("30006", "图片不存在"),
    ERROR_DELETE_IMAGE_FAIL("30007", "删除图片失败"),

    /* 系统错误：40001-49999 */
    SYSTEM_INNER_ERROR("40001", "系统繁忙，请稍后重试"),

    /* 数据错误：50001-599999 */
    RESULE_DATA_NONE("50001", "数据未找到"),
    DATA_IS_WRONG("50002", "数据有误"),
    DATA_ALREADY_EXISTED("50003", "数据已存在"),

    /* 接口错误：60001-69999 */
    INTERFACE_INNER_INVOKE_ERROR("60001", "内部系统接口调用异常"),
    INTERFACE_OUTTER_INVOKE_ERROR("60002", "外部系统接口调用异常"),
    INTERFACE_FORBID_VISIT("60003", "该接口禁止访问"),
    INTERFACE_ADDRESS_INVALID("60004", "接口地址无效"),
    INTERFACE_REQUEST_TIMEOUT("60005", "接口请求超时"),
    INTERFACE_EXCEED_LOAD("60006", "接口负载过高"),

    /* 权限错误：70001-79999 */
    PERMISSION_NO_ACCESS("70001", "无访问权限");

    private String code;
    private String msg;
}
