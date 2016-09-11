package com.ashtonmansion.tradeshowmanagement.util;

import android.util.Log;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * Created by paul on 8/31/2016.
 */
public class GlobalUtils {

    public static long getLongFromFormattedPriceString(String priceString) {
        String passedString = priceString;
        Number number2 = 0;
        double doubleNumber = 0;
        NumberFormat format = NumberFormat.getCurrencyInstance();
        try {
            number2 = format.parse(passedString);
            doubleNumber = number2.doubleValue();
            doubleNumber = doubleNumber * 100;
        } catch (ParseException e) {
            Log.d("ParseExcpt: ", e.getMessage());
        }
        long longPrice = (long) doubleNumber;

        return longPrice;
    }

    public static void valuesTester(String keyName, String value) {
        Log.d("Key: " + keyName, ",Value: " + value);
    }

    public static String getFormattedTagName(String tagFieldData, String tagType) {
        String formattedTagName = "";
        if (tagType.equalsIgnoreCase("Size")) {
            formattedTagName = "Size - " + tagFieldData;
        } else if (tagType.equalsIgnoreCase("Area")) {
            formattedTagName = "Area - " + tagFieldData;
        } else if (tagType.equalsIgnoreCase("Category")) {
            formattedTagName = "Category - " + tagFieldData;
        } else {
            Log.e("UnknownTagType: ", " passed to GlobalUtils");
        }
        return formattedTagName;
    }

    public static String getUnformattedTagName(String tagName, String tagType) {
        String unformattedString = "";
        if (tagType.equalsIgnoreCase("Size") || tagType.equalsIgnoreCase("Area")) {
            unformattedString = tagName.substring(7);
        } else if (tagType.equalsIgnoreCase("Category")) {
            unformattedString = tagName.substring(11);
        } else {
            Log.d("GlobalUt:", " an unrecognized tag type was passed to GlobalUtils");
        }
        return unformattedString;
    }
}
