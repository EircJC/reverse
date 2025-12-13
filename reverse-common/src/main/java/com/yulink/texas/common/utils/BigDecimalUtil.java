package com.yulink.texas.common.utils;

import java.math.BigDecimal;

/**
 * @author liupanpan
 */
public class BigDecimalUtil {
    private BigDecimalUtil() {
    }

    public static boolean isPositive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    public static boolean isNullOrNegative(BigDecimal value){
        return value == null || value.compareTo(BigDecimal.ZERO) < 0;
    }
}
