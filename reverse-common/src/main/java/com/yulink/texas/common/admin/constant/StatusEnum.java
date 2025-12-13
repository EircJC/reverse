package com.yulink.texas.common.admin.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatusEnum {
    ENABLED("0", "启用"),
    DISABLED("1", "禁用"),
    DELETED("2", "删除");

    private String status;
    private String desc;

    public static String getDesc(String status) {
        for (StatusEnum statusEnum : values()) {
            if (statusEnum.getStatus().equals(status)) {
                return statusEnum.getDesc();
            }
        }
        return "";
    }

    public static boolean isEnable(String status) {
        return ENABLED.getStatus().equals(status);
    }
}
