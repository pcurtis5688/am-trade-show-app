package com.ashtonmansion.tradeshowmanagement.util;

import android.util.Log;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * Created by paul on 8/31/2016.
 */
public class GlobalUtils {

    public static long getLongFromFormattedPriceString(String priceString) {
        long priceLongFormat = 0;
        NumberFormat numberFormatter = NumberFormat.getCurrencyInstance(Locale.US);
        try {
            Number parsedNumber = numberFormatter.parse(priceString);
            priceLongFormat = parsedNumber.longValue();
        } catch (ParseException e1) {
            Log.e("Parse Exception: ", e1.getClass().getName() + ", " + e1.getMessage());
        }
        return priceLongFormat;
    }

    public static void valuesTester(String keyName, String value) {
        Log.d("Key: " + keyName, ",Value: " + value);
    }
}
