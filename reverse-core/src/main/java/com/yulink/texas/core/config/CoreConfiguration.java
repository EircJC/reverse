package com.yulink.texas.core.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;

@Configuration
@ComponentScan(
    basePackages = "com.yulink.texas.core.*",
    excludeFilters = @ComponentScan.Filter({Controller.class}))
@MapperScan(basePackages = "com.yulink.texas.core.mapper")
public class CoreConfiguration {

}
