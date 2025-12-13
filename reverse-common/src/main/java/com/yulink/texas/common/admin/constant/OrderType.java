package com.yulink.texas.common.admin.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderType {
    DESC("desc"),
    ASC("asc");

    String clause;

    public static OrderType ofClause(String code) {
        for (OrderType orderType : values()) {
            if (orderType.getClause().equals(code)) {
                return orderType;
            }
        }
        return null;
    }

    public static OrderType parseTypeAndDefaultDesc(String code) {
        OrderType orderType = ofClause(code);
        return orderType == null ? DESC : orderType;
    }
}
