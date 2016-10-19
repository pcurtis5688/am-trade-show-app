package com.ashtonmansion.tsmanagement2.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.ashtonmansion.tsmanagement2.activity.BoothReservationShowSelection;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.Intents;
import com.clover.sdk.v3.order.OrderConnector;

interface AsyncResponse {
    void processOrderListenerFinish(OrderSentry orderSentry);

    void processItemNameCheck(String itemName);
}

/**
 * Created by Paul Curtis
 * (pcurtis5688@gmail.com)
 * on 10/12/2016.
 */

public class EventManagerReceiver extends BroadcastReceiver implements AsyncResponse {
    ////// PRIVATE CONNEX & DATA
    private Context fromContext;
    private Intent fromIntent;
    ////// INPUTS FROM RECEIVER
    private String orderID;
    ////// ORDER SENTRY IMPLEMENTATION
    private OrderSentry orderSentry;

    ////// THIS RECEIVES BOTH ORDER CREATED AND
    @Override
    public void onReceive(Context context, Intent intent) {
        this.fromContext = context;
        this.fromIntent = intent;
        String itemID = intent.getStringExtra(Intents.EXTRA_CLOVER_ITEM_ID);
        this.orderID = intent.getStringExtra(Intents.EXTRA_CLOVER_ORDER_ID);

        ////// IF SENTRY HAS NOT YET BEEN SPAWNED, CREATE ONE
        if (null == ((GlobalClass) fromContext.getApplicationContext()).getOrderSentry()) {
            SpawnOrderSentryTask spawnOrderSentryTask = new SpawnOrderSentryTask();
            spawnOrderSentryTask.setContextAndOrderId(fromContext, orderID);
            spawnOrderSentryTask.setDelegateAsyncResponse(this);
            spawnOrderSentryTask.execute();
            Log.d("Receiver", "Spawning new instance of Sentry....");
        } else {
            /////// ALREADY AVAILABLE IN APPLICATIONCONTEXT, but also set this receiver's
            this.orderSentry = ((GlobalClass) fromContext.getApplicationContext()).getOrderSentry();
            Log.d("Receiver", "Acquiring previous instance of Sentry...");
        }
        GetItemNameTask getItemNameTask = new GetItemNameTask();
        getItemNameTask.setDataAndDelegate(this, fromContext, itemID);
        getItemNameTask.execute();
    }

    @Override
    public void processOrderListenerFinish(OrderSentry spawnedOrderSentry) {
        GlobalClass globalClass = (GlobalClass) this.fromContext.getApplicationContext();
        globalClass.setOrderSentry(spawnedOrderSentry);
        this.orderSentry = spawnedOrderSentry;
        Log.d("Sentry", "Global instance of sentry set...");
    }

    @Override
    public void processItemNameCheck(String itemName) {
        //Log.d("Sentry Receiver", "Item Name: " + itemName + "...");
        if (itemName.equalsIgnoreCase("Select Booth")) {
            Intent boothReservationIntent = new Intent(fromContext, BoothReservationShowSelection.class);
            boothReservationIntent.putExtra("orderid", orderID);
            boothReservationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            fromContext.getApplicationContext().startActivity(boothReservationIntent);
        }
    }
}

class SpawnOrderSentryTask extends AsyncTask<Void, Void, OrderSentry> {
    /**
     * Created by Paul Curtis
     * (pcurtis5688@gmail.com)
     * on 10/12/2016.
     */
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