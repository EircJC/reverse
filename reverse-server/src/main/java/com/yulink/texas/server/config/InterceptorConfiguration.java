package com.yulink.texas.server.config;

import com.yulink.texas.common.web.interceptor.ResponseResultInterceptor;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfiguration implements WebMvcConfigurer {

    @Resource
    private ResponseResultInterceptor responseResultInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        String apiUri = "/**";

        //响应结果控制拦截
        registry.addInterceptor(responseResultInterceptor)
            .addPathPatterns(apiUri);
    }

    @Bean
    public ResponseResultInterceptor responseResultInterceptor() {
        return new ResponseResultInterceptor();
    }

}
