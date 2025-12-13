package com.yulink.texas.common.web.aspect;

import com.yulink.texas.common.utils.mapper.JsonMapper;
import com.yulink.texas.common.web.util.LogUtils;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * API 访问日志
 * 类似 @see  RestControllerAspect
 */
@Aspect
@Slf4j
public class APILogAspect {

    @Around("@within(org.springframework.web.bind.annotation.RestController) || @annotation(org.springframework.web.bind.annotation.RestController)")
    private Object controllerAspect(ProceedingJoinPoint pjp) throws Throwable {
        try {
            LogUtils.fillLogId();
            long millsTime = System.currentTimeMillis();
            HttpServletRequest request = getRequest();
            String uri = request != null ? request.getRequestURI() : "";
            log.info("millsTime:{}|url:{}|start|request: {}", millsTime, uri, getParamsMap(request));

            Object response = pjp.proceed();

            log.info("millsTime:{}|url:{}|end|response: {}| cost:{} ms", millsTime, uri,
                JsonMapper.nonEmptyMapper().toJson(response), System.currentTimeMillis() - millsTime);
            return response;
        } finally {
            MDC.clear();
        }
    }

    private HttpServletRequest getRequest() {
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        return ra == null ? null : ((ServletRequestAttributes) ra).getRequest();
    }

    private Map<String, String> getParamsMap(HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
        if (request == null || request.getParameterMap() == null) {
            return map;
        }
        Map<String, String[]> parameterMap = request.getParameterMap();

        parameterMap.forEach((key, values) -> {
            //过来掉data参数
            if(!key.equalsIgnoreCase("data")){
                String value = values == null ? "" : String.join(",", values);
                map.put(key, value);
            }
        });
        return map;
    }

}
