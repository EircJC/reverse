package com.yulink.texas.server.config;

import com.yulink.texas.common.web.aspect.RestControllerAspect;
import com.yulink.texas.common.web.handler.GlobalExceptionHandler;
import com.yulink.texas.common.web.handler.ResponseResultHandler;
import com.yulink.texas.core.config.CoreConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


@Configuration
@Import({
    CoreConfiguration.class
})
@ComponentScan(basePackages = "com.yulink.texas.server.*")
public class ServerConfiguration {

    @Bean
    public RestControllerAspect restControllerAspect() {
        return new RestControllerAspect();
    }

    @Bean
    public ResponseResultHandler responseResultHandler() {
        return new ResponseResultHandler();
    }

    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

}
