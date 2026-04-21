package com.yulink.texas.server;

import com.yulink.texas.common.utils.VersionUtil;
import com.yulink.texas.server.common.utils.SpringUtil;
import com.yulink.texas.server.netty.NettyWebSocketProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
@EnableConfigurationProperties(NettyWebSocketProperties.class)
public class ServerApplication {

    public static void main(String[] args) {
        if (VersionUtil.version(args)) {
            return;
        }
        ApplicationContext context = SpringApplication.run(ServerApplication.class, args);
        
        // 手动设置ApplicationContext到SpringUtil
        SpringUtil.setContext(context);
    }
}
