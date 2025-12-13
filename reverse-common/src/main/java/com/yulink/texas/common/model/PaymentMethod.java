package com.yulink.texas.common.model;

import com.yulink.texas.common.exception.ErrorPaymentMethodException;
import lombok.Getter;

/**
 * Need class description here...
 *
 * @Author: liupanpan
 * @Date: 2018/9/13
 * @Copyright (c) 2013, yulink.io All Rights Reserved
 */

public enum PaymentMethod {
    PPLNS("0"),
    PPS("1"),
    SOLO("2"),
    PPA("3"),
    PPLNSA("4"),
    SOLOA("5"),
    FPPS("6")
    ;

    @Getter
    private String code;

    PaymentMethod(String code) {
        this.code = code;
    }


    public static PaymentMethod ofCode(String code) throws ErrorPaymentMethodException {
        for (PaymentMethod paymentMethod : PaymentMethod.values()) {
            if (paymentMethod.getCode().equals(code)) {
                return paymentMethod;
            }
        }
        throw new ErrorPaymentMethodException();
    }
}
