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

    public static String getUnformattedTagName(String tagName, String tagType){
        String unformattedString = "";
        if (tagType.equalsIgnoreCase("Size") || tagType.equalsIgnoreCase("Area")){
            unformattedString = tagName.substring(7);
        } else if (tagType.equalsIgnoreCase("Category")){
            unformattedString = tagName.substring(11);
        } else {
            Log.d("GlobalUt:", " an unrecognized tag type was passed to GlobalUtils");
        }
        return unformattedString;
    }
}
