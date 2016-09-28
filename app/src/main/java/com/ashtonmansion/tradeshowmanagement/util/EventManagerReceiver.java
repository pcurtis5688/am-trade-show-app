package com.ashtonmansion.tradeshowmanagement.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.ashtonmansion.tradeshowmanagement.activity.BoothReservationShowSelection;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.Intents;
import com.clover.sdk.v3.inventory.InventoryConnector;

interface AsyncResponse {
    void processFinish(String output);
}

/**
 * Created by paul on 9/23/2016.
 */

public class EventManagerReceiver extends BroadcastReceiver implements AsyncResponse {
    private String orderID = "";
    private String itemID;
    private String itemName = "";
    private Context fromContext;
    private Intent fromIntent;


    @Override
    public void onReceive(Context context, Intent intent) {
        this.fromContext = context;
        this.fromIntent = intent;
        if (intent.getAction().equalsIgnoreCase("com.clover.intent.action.LINE_ITEM_ADDED")) {
            itemID = intent.getStringExtra(Intents.EXTRA_CLOVER_ITEM_ID);
            orderID = intent.getStringExtra(Intents.EXTRA_CLOVER_ORDER_ID);
            GetItemNameTask getItemNameTask = new GetItemNameTask();
            getItemNameTask.delegate = this;
            getItemNameTask.setData(context, itemID);
            getItemNameTask.execute();
        }

        //String test = intent.getAction();
        //Log.d("Intent Action Name: ", test);
    }

    @Override
    public void processFinish(String itemName) {
        this.itemName = itemName;
        checkIfGenericBooth();
    }

    private void checkIfGenericBooth() {
        if (itemName.toLowerCase().contains("booth")) {
            Intent selectBoothForOrderIntent = new Intent(fromContext, BoothReservationShowSelection.class);
            selectBoothForOrderIntent.putExtra("orderid", orderID);
            selectBoothForOrderIntent.putExtra("itemid", itemID);
            selectBoothForOrderIntent.putExtra("itemname", itemName);
            selectBoothForOrderIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            fromContext.getApplicationContext().startActivity(selectBoothForOrderIntent);
        }
    }
}

class GetItemNameTask extends AsyncTask<Void, Void, String> {
    public AsyncResponse delegate = null;
    private InventoryConnector inventoryConnector;
    ////// INPUTS
    private Context appContext;
    private String itemID;
    ////// RESULT ITEM NAME
    private String itemName;

    void setData(Context taskContext, String itemID) {
        this.appContext = taskContext.getApplicationContext();
        this.itemID = itemID;
        inventoryConnector = new InventoryConnector(appContext, CloverAccount.getAccount(appContext), null);
        inventoryConnector.connect();
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            itemName = inventoryConnector.getItem(itemID).getName();
        } catch (Exception e) {
            Log.d("ExceptionCheckInBooth: ", e.getMessage(), e.getCause());
        }
        return itemName;
    }

    @Override
    protected void onPostExecute(String itemName) {
        super.onPostExecute(itemName);
        inventoryConnector.disconnect();
        delegate.processFinish(itemName);
    }
}