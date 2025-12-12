package com.yulink.texas.common.utils;

import java.util.Properties;

public class VersionUtil {

    public static boolean version(String[] args) {
        if (args == null || args.length != 1) {
            return false;
        }
        if (!"version".equals(args[0])) {
            return false;
        }
        try {
            Properties properties = new Properties();
            properties.load(VersionUtil.class.getClassLoader().getResourceAsStream("git.properties"));
            System.out.printf("version    -> %s%n", properties.getProperty("git.build.version"));
            System.out.printf("build_time -> %s%n", properties.getProperty("git.build.time"));
            System.out.println("branch     -> " + properties.getProperty("git.branch"));
            System.out.println("commit_id  -> " + properties.getProperty("git.commit.id"));
            System.out.println("build_user -> " + properties.getProperty("git.build.user.email"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    }
}
