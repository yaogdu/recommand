package com.demai.util;

import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Created by dear on 16/4/18.
 */
public class FormatUtil {

    static DecimalFormat df = new DecimalFormat("#.00");

    static {
        df.setRoundingMode(RoundingMode.FLOOR);
    }

    public static String format(double digit) {
        return df.format(digit);
    }
}
