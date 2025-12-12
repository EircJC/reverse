package com.yulink.texas.core.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ServerConfig {

    /**
     * 当前服务API e.g. https://demo.com
     */
    @Value("${config.server.baseUrl}")
    private String baseUrl;

}
