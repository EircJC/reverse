package com.yulink.texas.admin;

import com.yulink.texas.common.utils.VersionUtil;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@SpringBootApplication
public class AdminApplication {

    public static void main(String[] args) throws IOException {
        if (VersionUtil.version(args)) {
            return;
        }
        ConfigurableApplicationContext applicationContext = SpringApplication.run(AdminApplication.class, args);
        applicationContext.registerShutdownHook();
    }

    private CorsConfiguration buildConfig() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("*");
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");
        return corsConfiguration;
    }

    /**
     * 其他的api都会的JwtAuthenticationFilter中进行相关设置,由于这两个api不需要进行权限认证,所以没有JwtAuthenticationFilter过滤,故而为它们设置了单独的过滤器.
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        Map<String, CorsConfiguration> corsConfigurations = new HashMap<>();
        corsConfigurations.put("/api/v1/captcha", buildConfig());
        corsConfigurations.put("/api/v1/auth/login", buildConfig());
        source.setCorsConfigurations(corsConfigurations);
        return new CorsFilter(source);
    }
}
