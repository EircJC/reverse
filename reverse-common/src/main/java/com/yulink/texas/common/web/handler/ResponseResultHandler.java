package com.yulink.texas.common.web.handler;

import com.yulink.texas.common.exception.ErrorCode;
import com.yulink.texas.common.jackson.JacksonMapper;
import com.yulink.texas.common.web.annotations.ResponseResult;
import com.yulink.texas.common.web.constants.HeaderConstants;
import com.yulink.texas.common.web.enums.ApiStyleEnum;
import com.yulink.texas.common.web.interceptor.ResponseResultInterceptor;
import com.yulink.texas.common.web.result.DefaultErrorResult;
import com.yulink.texas.common.web.result.DefaultSuccessResult;
import com.yulink.texas.common.web.result.Result;
import com.yulink.texas.common.web.util.RequestContextUtil;
import javax.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * @desc 接口响应体处理器
 */
@ControllerAdvice
public class ResponseResultHandler implements ResponseBodyAdvice<Object> {

	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		HttpServletRequest request = RequestContextUtil.getRequest();
		ResponseResult responseResultAnn = (ResponseResult) request
			.getAttribute(ResponseResultInterceptor.RESPONSE_RESULT);
		return responseResultAnn != null && !ApiStyleEnum.NONE.name().equalsIgnoreCase(request.getHeader(
			HeaderConstants.API_STYLE));
	}

	@Override
	public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
		Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
		ServerHttpResponse response) {
		ResponseResult responseResultAnn = (ResponseResult) RequestContextUtil.getRequest().getAttribute(
			ResponseResultInterceptor.RESPONSE_RESULT);

		Class<? extends Result> resultClazz = responseResultAnn.value();

		if (resultClazz.isAssignableFrom(DefaultSuccessResult.class)) {
			if (body instanceof DefaultErrorResult) {
				return body;
			} else if (body == null || body instanceof String) {
				return JacksonMapper.obj2String(success(body));
			}
			return success(body);
		}

		return body;
	}

    private DefaultSuccessResult success(Object data) {
        DefaultSuccessResult result = DefaultSuccessResult.success(data);
        result.setCode(ErrorCode.SERVER_API_SUCCESS.getCode());
        result.setMsg(ErrorCode.SERVER_API_SUCCESS.getMsg());
        return result;
    }
}
