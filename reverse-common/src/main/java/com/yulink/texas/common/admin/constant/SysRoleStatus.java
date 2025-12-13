package com.yulink.texas.common.admin.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SysRoleStatus {

    NORMAL("0", "正常"),
    DISABLE("1", "禁用");

    String code;
    String name;

    public static SysRoleStatus ofCode(String code) {
        SysRoleStatus[] sysRoleStatusArr = values();
        for (SysRoleStatus sysRoleStatus : sysRoleStatusArr) {
            if (sysRoleStatus.code.equals(code)) {
                return sysRoleStatus;
            }
        }
        return null;
    }

    public static String getDesc(String code) {
        SysRoleStatus status = ofCode(code);
        return status == null ? code : status.getName();
    }
}
