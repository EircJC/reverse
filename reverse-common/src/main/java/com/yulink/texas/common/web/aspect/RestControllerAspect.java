package com.yulink.texas.common.web.aspect;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yulink.texas.common.web.annotations.IgnoredApiLog;
import com.yulink.texas.common.web.constants.HeaderConstants;
import com.yulink.texas.common.web.util.IpUtil;
import com.yulink.texas.common.web.util.LogUtils;
import java.lang.reflect.Method;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

/**
 * @desc 请求参数、响应体统一日志打印
 * @see  APILogAspect
 */
@Slf4j(topic = "access")
@Aspect
public class RestControllerAspect {

	private String getMethodName(ProceedingJoinPoint joinPoint) {
		String methodName = joinPoint.getSignature().toShortString();
		String shortMethodNameSuffix = "(..)";
		if (methodName.endsWith(shortMethodNameSuffix)) {
			methodName = methodName.substring(0, methodName.length() - shortMethodNameSuffix.length());
		}
		return methodName;
	}

	/**
	 * 环绕通知
	 *
	 * @param joinPoint 连接点
	 * @return 切入点返回值
	 * @throws Throwable 异常信息
	 */
	@Around("@within(org.springframework.web.bind.annotation.RestController) || @annotation(org.springframework.web.bind.annotation.RestController)")
	public Object apiLog(ProceedingJoinPoint joinPoint) throws Throwable {

		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
			.getRequest();

		String ip = IpUtil.getRealIp(request);
		String methodName = this.getMethodName(joinPoint);
		String params = this.getParamsJson(joinPoint);

		String callSource = request.getHeader(HeaderConstants.CALL_SOURCE);
		String userAgent = request.getHeader("user-agent");
		boolean needPrintLog = isNeedPrintLog(joinPoint);

		Object result;
		try {
			LogUtils.fillLogId();
			if (needPrintLog) {
				log.info(
					"start request|method:{}|params:{}|ip:{}|callSource:{}|userAgent:{}",
					methodName, params, ip, callSource, userAgent);
			}
			long start = System.currentTimeMillis();
			result = joinPoint.proceed();
			if (needPrintLog) {
				log.info("end request|method:{}|response:{}|cost:{} millis", methodName,
					this.deleteSensitiveContent(result), System.currentTimeMillis() - start);
			}
		} finally {
			MDC.clear();
		}
		return result;
	}

	private boolean isNeedPrintLog(ProceedingJoinPoint point) {
		try {
			String methodName = point.getSignature().getName();
			Class<?> clazz = point.getTarget().getClass();
			IgnoredApiLog ignoredApiLog = null;
			for (Method method : clazz.getMethods()) {
				if (method.getName().equals(methodName)) {
					ignoredApiLog = method.getAnnotation(IgnoredApiLog.class);
				}
			}
			return ignoredApiLog == null;
		} catch (Exception e) {
			return true;
		}
	}

	private String getParamsJson(ProceedingJoinPoint joinPoint) {
		Object[] args = joinPoint.getArgs();
		StringBuilder sb = new StringBuilder();
		for (Object arg : args) {
			//移除敏感内容
			String paramStr;
			if (arg instanceof HttpServletResponse) {
				paramStr = HttpServletResponse.class.getSimpleName();
			} else if (arg instanceof HttpServletRequest) {
				paramStr = HttpServletRequest.class.getSimpleName();
			} else if (arg instanceof MultipartFile) {
				long size = ((MultipartFile) arg).getSize();
				paramStr = MultipartFile.class.getSimpleName() + " size:" + size;
			} else {
				paramStr = this.deleteSensitiveContent(arg);
			}
			sb.append(paramStr).append(",");
		}
		if (sb.length() > 0) {
			return sb.deleteCharAt(sb.length() - 1).toString();
		} else {
			return Strings.EMPTY;
		}
	}

	/**
	 * 删除参数中的敏感内容
	 *
	 * @param obj 参数对象
	 * @return 去除敏感内容后的参数对象
	 */
	private String deleteSensitiveContent(Object obj) {
		JSONObject jsonObject = new JSONObject();
		if (obj == null || obj instanceof Exception) {
			return jsonObject.toJSONString();
		}
		try {
			return JSON.toJSONString(obj);
		} catch (Exception e) {
			return String.valueOf(obj);
		}
	}
}
