package com.ashtonmansion.tsmanagement1.util;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.ashtonmansion.tsmanagement1.activity.BoothReservationShowSelection;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.Intents;
import com.clover.sdk.v3.inventory.InventoryConnector;
import com.clover.sdk.v3.order.OrderConnector;

interface AsyncResponse {
    void processGetItemNameCompletion(String itemIdOut, String itemNameOut);

    void processOrderListenerFinish(OrderSentry orderSentry);
}

/**
 * Created by Paul Curtis (pcurtis5688@gmail.com) on 10/12/2016.
 */

public class EventManagerReceiver extends BroadcastReceiver implements AsyncResponse {
    ////// PUBLIC ACCESS VARS
    ////// PRIVATE CONNEX & DATA
    private Context fromContext;
    ////// INPUTS FROM RECEIVER
    //private Intent fromIntent;
    private String orderID;
    private String itemID;
    ////// ORDER SENTRY IMPLEMENTATION
    private boolean hasOrderSentry;
    private OrderSentry orderSentry;
    ////// OUTPUTS / DECIPHERED DATA
    private String itemName;

    ////// Test purposes segment
    @Override
    public void onReceive(Context context, Intent intent) {
        this.fromContext = context;

        if (null == intent.getStringExtra(Intents.EXTRA_CLOVER_ORDER_ID)
                && null == intent.getStringExtra(Intents.EXTRA_CLOVER_ORDER_ID)) {
            itemID = "";
            orderID = "";
        } else if (null == intent.getStringExtra(Intents.EXTRA_CLOVER_ITEM_ID)) {
            itemID = "";
            orderID = intent.getStringExtra(Intents.EXTRA_CLOVER_ORDER_ID);
        } else if (null == intent.getStringExtra(Intents.EXTRA_CLOVER_ORDER_ID)) {
            orderID = "";
            itemID = intent.getStringExtra(Intents.EXTRA_CLOVER_ITEM_ID);
        } else {
            itemID = intent.getStringExtra(Intents.EXTRA_CLOVER_ITEM_ID);
            orderID = intent.getStringExtra(Intents.EXTRA_CLOVER_ORDER_ID);
        }
        SpawnOrderSentryTask spawnOrderSentryTask = new SpawnOrderSentryTask();
        spawnOrderSentryTask.setContextAndOrderId(fromContext, orderID);
        spawnOrderSentryTask.setDelegateAsyncResponse(this);
        spawnOrderSentryTask.execute();
    }

    public void checkItemName() {
        GetItemNameTask getItemNameTask = new GetItemNameTask();
        getItemNameTask.setDataAndDelegate(this, fromContext, itemID);
        getItemNameTask.execute();
    }

    @Override
    public void processGetItemNameCompletion(String itemID, String itemName) {
        this.itemName = itemName;
        //// TODO: 10/15/2016 uncomment when near to completion
        //if (itemName.equalsIgnoreCase("select booth")){
        SpawnOrderSentryTask spawnOrderSentryTask = new SpawnOrderSentryTask();
        spawnOrderSentryTask.setContextAndOrderId(fromContext, orderID);
        spawnOrderSentryTask.setDelegateAsyncResponse(this);
        spawnOrderSentryTask.execute();
    }

    @Override
    public void processOrderListenerFinish(OrderSentry spawnedOrderSentry) {
        if (null != itemName && itemName.equalsIgnoreCase("select booth")) {
            Intent selectBoothForOrderIntent = new Intent(fromContext, BoothReservationShowSelection.class);
            selectBoothForOrderIntent.putExtra("orderid", orderID);
            selectBoothForOrderIntent.putExtra("itemid", itemID);
            selectBoothForOrderIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            fromContext.getApplicationContext().startActivity(selectBoothForOrderIntent);
        }
    }
}

class SpawnOrderSentryTask extends AsyncTask<Void, Void, OrderSentry> {
    private AsyncResponse delegate = null;
    private OrderConnector orderConnector;
    ////// INPUTS
    private Context appContext;
    private String orderID;
    ////// RESULT IS AN ORDER SENTRY
    private OrderSentry orderSentry;

    void setContextAndOrderId(Context receivedContext, String orderID) {
        this.appContext = receivedContext.getApplicationContext();
        this.orderID = orderID;
    }

    void setDelegateAsyncResponse(EventManagerReceiver callingReceiver) {
        delegate = callingReceiver;
    }

    @Override
    protected void onPreExecute() {
        orderConnector = new OrderConnector(appContext, CloverAccount.getAccount(appContext), null);
        orderConnector.connect();
    }

    @Override
    protected OrderSentry doInBackground(Void... params) {
        try {
            orderSentry = new OrderSentry(appContext, orderID);
            orderConnector.addOnOrderChangedListener(orderSentry);
        } catch (Exception e) {
            Log.d("ExceptionCheckInBooth: ", e.getMessage(), e.getCause());
        }
        return orderSentry;
    }

    @Override
    protected void onPostExecute(OrderSentry orderSentry) {
        super.onPostExecute(orderSentry);
        delegate.processOrderListenerFinish(orderSentry);
    }
}