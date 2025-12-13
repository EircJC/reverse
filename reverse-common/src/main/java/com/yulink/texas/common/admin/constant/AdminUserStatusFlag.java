package com.yulink.texas.common.admin.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AdminUserStatusFlag {

    NORMAL("1", "正常"),
    FORBIDDEN("2", "已禁用");

    String code;
    String name;

    public static AdminUserStatusFlag ofCode(String code) {
        for (AdminUserStatusFlag adminUserStatusFlag : values()) {
            if (adminUserStatusFlag.code.equals(code)) {
                return adminUserStatusFlag;
            }
        }
        return null;
    }

    public static String getName(String code) {
        for (AdminUserStatusFlag item : values()) {
            if (item.getCode().equals(code)) {
                return item.getName();
            }
        }
        return code;
    }
}
