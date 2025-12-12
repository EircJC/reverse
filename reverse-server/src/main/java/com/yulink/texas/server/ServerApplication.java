package com.yulink.texas.server;

import com.yulink.texas.common.utils.VersionUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class ServerApplication {

    public static void main(String[] args) {
        if (VersionUtil.version(args)) {
            return;
        }
        SpringApplication.run(ServerApplication.class, args);
    }
}
