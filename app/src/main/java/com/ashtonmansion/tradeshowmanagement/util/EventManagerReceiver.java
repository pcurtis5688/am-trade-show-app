package com.ashtonmansion.tradeshowmanagement.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v3.inventory.InventoryConnector;

/**
 * Created by paul on 9/23/2016.
 */

public class EventManagerReceiver extends BroadcastReceiver implements AsyncResponse {
    private String itemName = "";
    private Context eventManagerReceiverContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        eventManagerReceiverContext = context;
        if (intent.getAction().equalsIgnoreCase("com.clover.intent.action.LINE_ITEM_ADDED")) {
            String itemID = intent.getStringExtra("com.clover.intent.extra.ITEM_ID");
            GetItemNameTask getItemNameTask = new GetItemNameTask();
            getItemNameTask.delegate = this;
            getItemNameTask.setData(context, itemID);
            getItemNameTask.execute();
            String orderID = "";
        }
    }

    @Override
    public void processFinish(String itemName) {
        this.itemName = itemName;
        checkIfGenericBooth();
    }

    private void checkIfGenericBooth(){
        Toast.makeText(eventManagerReceiverContext, "item name:" + itemName, Toast.LENGTH_LONG).show();
        if (itemName.toLowerCase().contains("booth")){
            // blah
        }
    }
}

interface AsyncResponse {
    void processFinish(String output);
}

class GetItemNameTask extends AsyncTask<Void, Void, String> {
    private InventoryConnector inventoryConnector;
    public AsyncResponse delegate = null;
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