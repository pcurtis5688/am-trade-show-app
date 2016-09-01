package com.ashtonmansion.tradeshowmanagement.util;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by paul on 8/31/2016.
 */
public class GlobalUtils {
    public static String formatLongPrice(long price) {
        NumberFormat numberFormatter = NumberFormat.getCurrencyInstance(Locale.US);
        return numberFormatter.format(price / 100.0);
    }
}
