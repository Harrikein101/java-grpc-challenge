package com.zerohub.challenge.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DecimalUtils {

    private DecimalUtils() {
    }

    public static BigDecimal getReverseNumber(BigDecimal num) {
        return BigDecimal.ONE.divide(num, Math.max(4, num.precision()), RoundingMode.CEILING);
    }

    public static BigDecimal roundCarefully(BigDecimal num) {
        return num.setScale(4, RoundingMode.HALF_UP);
    }
}
