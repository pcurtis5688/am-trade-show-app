package com.ashtonmansion.tsmanagement2.util;

import android.content.Context;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.util.Log;

import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.ForbiddenException;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v3.customers.Customer;
import com.clover.sdk.v3.customers.PhoneNumber;
import com.clover.sdk.v3.customers.EmailAddress;
import com.clover.sdk.v3.customers.Address;
import com.clover.sdk.v3.inventory.InventoryConnector;
import com.clover.sdk.v3.inventory.Item;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class GlobalUtils {
    /**
     * Created by paul curtis
     * (pcurtis5688@gmail.com
     * on 8/31/2016.
     */
    public static String getOrderIDOnlyFromCode(String boothCode) {
        return boothCode.substring(7);
    }

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

    public static String getFormattedPriceStringFromLong(long longPrice) {
        String priceLongString = Long.toString(longPrice);
        String cleanString = priceLongString.replaceAll("[$,.]", "");
        double parsedBoothPriceDouble = Double.parseDouble(cleanString);
        NumberFormat numberFormatter = NumberFormat.getCurrencyInstance(Locale.US);
        String formattedPrice = numberFormatter.format(parsedBoothPriceDouble / 100.0);
        return formattedPrice;
    }

    public static String getFormattedTagName(String tagFieldData, String tagType) {
        String formattedTagName = "";
        if (tagType.equalsIgnoreCase("Size")) {
            formattedTagName = "Size - " + tagFieldData;
        } else if (tagType.equalsIgnoreCase("Area")) {
            formattedTagName = "Area - " + tagFieldData;
        } else if (tagType.equalsIgnoreCase("Type")) {
            formattedTagName = "Type - " + tagFieldData;
        } else {
            Log.d("UnknownTagType: ", " passed to GlobalUtils");
        }
        return formattedTagName;
    }

    public static String getUnformattedTagName(String tagName, String tagType) {
        String unformattedString = "";
        if (tagType.equalsIgnoreCase("Size")
                || tagType.equalsIgnoreCase("Area")
                || tagType.equalsIgnoreCase("Type")) {
            unformattedString = tagName.substring(7);
        } else {
            Log.d("GlobalUt:", " an unrecognized tag type was passed to GlobalUtils");
        }
        return unformattedString;
    }

    public static Customer getv3CustomerFromv1Customer(com.clover.sdk.v1.customer.Customer v1customer,
                                                       List<com.clover.sdk.v1.customer.PhoneNumber> v1PhoneNumbers,
                                                       List<com.clover.sdk.v1.customer.EmailAddress> v1EmailAddresses,
                                                       List<com.clover.sdk.v1.customer.Address> v1Addresses) {
        ////CREATE NEW V3CUSTOMER SET SINGULAR FIELDS
        Customer v3Customer = new Customer();
        v3Customer.setId(v1customer.getId());
        v3Customer.setFirstName(v1customer.getFirstName());
        v3Customer.setLastName(v1customer.getLastName());
        v3Customer.setMarketingAllowed(v1customer.getMarketingAllowed());
        ////TAKE THE CUSTOMERS V1 PHONE NUMBER LIST, CONVERT, AND SET V3 CUSTOMER PHONE LIST
        v3Customer.setPhoneNumbers(getv3PhoneNumberListFromV1(v1PhoneNumbers));
        ////TAKE THE CUSTOMERS V1 EMAIL ADDRESS LIST, CONVERT, AND SET V3 CUSTOMER EMAIL ADDRESSES
        v3Customer.setEmailAddresses(getv3EmailAddressesFromV1(v1EmailAddresses));
        ////TAKE THE CUSTOMERS V1 ADDRESS LIST, CONVERT, AND SET V3 CUSTOMER ADDRESSES
        v3Customer.setAddresses(getv3AddressesFromV1(v1Addresses));
        ////RETURN THE NEW CUSTOMER
        return v3Customer;
    }

    private static List<PhoneNumber> getv3PhoneNumberListFromV1(List<com.clover.sdk.v1.customer.PhoneNumber> v1PhoneNumbers) {
        List<PhoneNumber> v3PhoneNumberList = new ArrayList<>();
        for (com.clover.sdk.v1.customer.PhoneNumber v1PhoneNumber : v1PhoneNumbers) {
            PhoneNumber v3PhoneNumber = new PhoneNumber();
            v3PhoneNumber.setId(v1PhoneNumber.getId());
            v3PhoneNumber.setPhoneNumber(v1PhoneNumber.getPhoneNumber());
            v3PhoneNumberList.add(v3PhoneNumber);
        }
        return v3PhoneNumberList;
    }

    private static List<EmailAddress> getv3EmailAddressesFromV1(List<com.clover.sdk.v1.customer.EmailAddress> v1EmailAddresses) {
        List<EmailAddress> v3EmailAddresses = new ArrayList<>();
        for (com.clover.sdk.v1.customer.EmailAddress v1EmailAddress : v1EmailAddresses) {
            EmailAddress v3EmailAddress = new EmailAddress();
            v3EmailAddress.setId(v1EmailAddress.getId());
            v3EmailAddress.setEmailAddress(v1EmailAddress.getEmailAddress());
            v3EmailAddresses.add(v3EmailAddress);
        }
        return v3EmailAddresses;
    }

    private static List<Address> getv3AddressesFromV1(List<com.clover.sdk.v1.customer.Address> v1Addresses) {
        List<Address> v3Addresses = new ArrayList<>();

        for (com.clover.sdk.v1.customer.Address v1Address : v1Addresses) {
            Address v3Address = new Address();
            v3Address.setId(v1Address.getId());
            v3Address.setAddress1(v1Address.getAddress1());
            v3Address.setAddress2(v1Address.getAddress2());
            v3Address.setAddress3(v1Address.getAddress3());
            v3Address.setCity(v1Address.getCity());
            v3Address.setState(v1Address.getState());
            v3Address.setZip(v1Address.getZip());
            v3Address.setCountry("USA");
            v3Addresses.add(v3Address);
        }
        return v3Addresses;
    }

    public static List<String> decoupleShowName(String formattedShowName) {
        List<String> result = Arrays.asList(formattedShowName.split(","));
        String notesPart = result.get(3);
        int indexOfShow = notesPart.indexOf("[Show]");
        if (indexOfShow == -1) {
            result.set(3, "");
        } else {
            String desiredNotesPart = notesPart.substring(0, indexOfShow);
            result.set(3, desiredNotesPart);
        }


        return result;
    }

    public static String determinePlatform(Context appContext) {
        String platform = "";
        if ((appContext.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) {
            platform = "mobile";
        } else if ((appContext.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
            platform = "station";
        } else {
            platform = "other";
        }
        return platform;
    }

    public static void valuesTester(String keyName, String value) {
        Log.d("Key: " + keyName, ",Value: " + value);
    }

    public static void doPermissionCheck(Object caller, Context context) {
        PermissionsTestTask permissionsTestTask = new PermissionsTestTask();
        permissionsTestTask.setContextAndCaller(caller, context);
        permissionsTestTask.execute();
    }

    public static boolean getPermissionsValid(Object caller, Context context) {
        doPermissionCheck(caller, context);
        return ((GlobalClass) context.getApplicationContext()).isApplicationHasValidPermissions();
    }

    public void setPermissionsValid(boolean permissionsValid, Context permissionsContext) {
        if (permissionsValid)
            ((GlobalClass) permissionsContext.getApplicationContext()).setApplicationHasValidPermissions(true);
        else
            ((GlobalClass) permissionsContext.getApplicationContext()).setApplicationHasValidPermissions(false);
    }
}

class GetItemNameTask extends AsyncTask<Void, Void, String> {
    private AsyncResponse delegate = null;
    private InventoryConnector inventoryConnector;
    ////// INPUTS
    private String itemID;
    ////// RESULT ITEM NAME
    private String itemName;

    void setDataAndDelegate(EventManagerReceiver eventManagerReceiver, Context taskContext, String itemID) {
        Context appContext = taskContext.getApplicationContext();
        this.itemID = itemID;
        this.delegate = eventManagerReceiver;
        inventoryConnector = new InventoryConnector(appContext, CloverAccount.getAccount(appContext), null);
        inventoryConnector.connect();
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            itemName = inventoryConnector.getItem(itemID).getName();
        } catch (Exception e) {
            Log.d("GlobalUtils", "GetItemNameTask\n'" + e.getMessage(), e.getCause());
        }
        return itemName;
    }

    @Override
    protected void onPostExecute(String itemName) {
        super.onPostExecute(itemName);
        inventoryConnector.disconnect();
        delegate.processItemNameCheck(itemName);
    }
}

class PermissionsTestTask extends AsyncTask<Void, Void, Boolean> {
    ////// CLOVER CONNECTIONS
    private InventoryConnector inventoryConnector;
    private Context accessingContext;
    ////// DATA / RESULT HANDLING
    private Object callingObject;
    private boolean activePermissions;
    private List<Item> inventoryList;

    void setContextAndCaller(Object callingObject, Context accessingContext) {
        this.callingObject = callingObject;
        this.accessingContext = accessingContext;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        activePermissions = true;
        inventoryConnector = new InventoryConnector(accessingContext, CloverAccount.getAccount(accessingContext), null);
        inventoryConnector.connect();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            inventoryList = inventoryConnector.getItems();
        } catch (BindingException | ClientException | RemoteException | ServiceException e1) {
            if (e1.getClass().equals(ForbiddenException.class)) {
                ((GlobalClass) accessingContext.getApplicationContext()).setApplicationHasValidPermissions(false);
                activePermissions = false;
                Log.d("GlobalUtils", "Active permissions set to false due to Forbidden exception");
            }
        }
        return activePermissions;
    }

    @Override
    protected void onPostExecute(Boolean activePermissions) {
        super.onPostExecute(activePermissions);
        inventoryConnector.disconnect();
        inventoryConnector = null;
    }
}