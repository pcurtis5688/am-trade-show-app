package com.ashtonmansion.tsmanagement1.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.Intents;
import com.clover.sdk.v3.order.OrderConnector;

interface AsyncResponse {
    void processOrderListenerFinish(OrderSentry orderSentry);
}

/**
 * Created by Paul Curtis
 * (pcurtis5688@gmail.com)
 * on 10/12/2016.
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

    ////// THIS RECEIVES BOTH ORDER CREATED AND
    @Override
    public void onReceive(Context context, Intent intent) {
        this.fromContext = context;

        if (null == intent.getStringExtra(Intents.EXTRA_CLOVER_ORDER_ID)
                && null == intent.getStringExtra(Intents.EXTRA_CLOVER_ORDER_ID)) {
            itemID = "";
            orderID = "";
            Log.d("Sentry", "Case where both orderID & itemID is null was located");
        } else if (null == intent.getStringExtra(Intents.EXTRA_CLOVER_ITEM_ID)) {
            itemID = "";
            orderID = intent.getStringExtra(Intents.EXTRA_CLOVER_ORDER_ID);
            Log.d("Sentry", "Case where itemID is null was located");
        } else if (null == intent.getStringExtra(Intents.EXTRA_CLOVER_ORDER_ID)) {
            orderID = "";
            itemID = intent.getStringExtra(Intents.EXTRA_CLOVER_ITEM_ID);
            Log.d("Sentry", "Case where orderID is null was located");
        } else {
            itemID = intent.getStringExtra(Intents.EXTRA_CLOVER_ITEM_ID);
            orderID = intent.getStringExtra(Intents.EXTRA_CLOVER_ORDER_ID);
        }

        ////// IF SENTRY HAS NOT YET BEEN SPAWNED, CREATE ONE
        if (null == ((GlobalClass) fromContext.getApplicationContext()).getOrderSentry()) {
            SpawnOrderSentryTask spawnOrderSentryTask = new SpawnOrderSentryTask();
            spawnOrderSentryTask.setContextAndOrderId(fromContext, orderID);
            spawnOrderSentryTask.setDelegateAsyncResponse(this);
            spawnOrderSentryTask.execute();
        } else {
            /////// ALREADY AVAILABLE IN APPLICATIONCONTEXT, but also set one
            this.orderSentry = ((GlobalClass) fromContext.getApplicationContext()).getOrderSentry();
            Log.d("Sentry", "Acquiring previous instance...");
        }

        Log.d("Receiver", "Item ID: " + itemID);
    }

    @Override
    public void processOrderListenerFinish(OrderSentry spawnedOrderSentry) {
        GlobalClass globalClass = (GlobalClass) this.fromContext.getApplicationContext();
        globalClass.setOrderSentry(spawnedOrderSentry);
        this.orderSentry = spawnedOrderSentry;
        Log.d("Sentry", "Global instance of sentry set...");
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