package com.yulink.texas.common.web.constants;


import com.yulink.texas.common.web.enums.ApiStyleEnum;
import com.yulink.texas.common.web.enums.CallSourceEnum;

/**
 * @desc Header的key罗列
 */
public class HeaderConstants {

	/**
	 * 用户的登录token
	 */
	public static final String X_TOKEN = "X-Token";

	/**
	 * api的版本号
	 */
	public static final String API_VERSION = "Api-Version";

	/**
	 * app版本号
	 */
	public static final String APP_VERSION = "App-Version";

	/**
	 * 调用来源 {@link CallSourceEnum}
	 */
	public static final String CALL_SOURCE = "Call-Source";

	/**
	 * API的返回格式 {@link ApiStyleEnum}
	 */
	public static final String API_STYLE = "Api-Style";

	/**
	 * MDC 标志
	 */
	public static final String X_LOG_ID = "x-log-id";
}
