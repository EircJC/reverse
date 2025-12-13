package com.yulink.texas.common.model;

import com.yulink.texas.common.exception.EnumNotFoundException;
import lombok.Getter;

/**
 * 收益类型
 *
 * @Author: liupanpan
 * @Date: 2018/8/1
 * @Copyright (c) 2013, yulink.io All Rights Reserved
 */

public enum IncomeType {

    PPLNS("0"),
    PPS("1"),
    SOLO("2"),
    PPA_PPS("3"),
    PPA_PPLNS("4"),
    FPPS_PPS("5"),
    FPPS_PLUS("6"),
    SOLOA("7"),
    REFUND("8"),
    DAYMIN("9"),
    BASE("A"),
    POS("B"),
    P2P("C"),
    REWARD("D"),
    MEV("E"), //ETH MEV 收益
    FPPS_PPS_ORIGIN("M"), //FPPS PPS 原币结算
    FPPS_PLUS_ORIGIN("N"), //FPPS P 原币结算

    ;

    @Getter
    private String type;

    IncomeType(String type) {
        this.type = type;
    }

    public static IncomeType ofType(String type) throws EnumNotFoundException {
        for (IncomeType incomeType : IncomeType.values()) {
            if (incomeType.getType().equals(type)) {
                return incomeType;
            }
        }
        throw new EnumNotFoundException();
    }
}
