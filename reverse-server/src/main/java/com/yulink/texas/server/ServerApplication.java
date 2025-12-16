package com.yulink.texas.server;

import com.yulink.texas.common.utils.VersionUtil;
import com.yulink.texas.server.common.utils.SpringUtil;
import com.yulink.texas.server.netty.NettyWebSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import javax.annotation.PostConstruct;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class ServerApplication {

    @Autowired
    private NettyWebSocketServer nettyWebSocketServer;

    public static void main(String[] args) {
        if (VersionUtil.version(args)) {
            return;
        }
        ApplicationContext context = SpringApplication.run(ServerApplication.class, args);
        
        // 手动设置ApplicationContext到SpringUtil
        SpringUtil.setContext(context);
    }

    @PostConstruct
    public void startNettyServer() {
        // 延迟启动Netty服务器，确保Spring完全初始化
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(() -> {
            try {
                nettyWebSocketServer.start();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Failed to start Netty server: " + e.getMessage());
            }
        }, 2, TimeUnit.SECONDS);
    }
}