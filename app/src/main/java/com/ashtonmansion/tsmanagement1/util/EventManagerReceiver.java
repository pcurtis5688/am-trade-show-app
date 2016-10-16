package com.ashtonmansion.tsmanagement1.util;

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

/**
 * Created by Paul Curtis (pcurtis5688@gmail.com) on 10/12/2016.
 */
interface AsyncResponse {
    void processFinish(String output);

    void processOrderListenerFinish(OrderSentry orderSentry);
}

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
        Log.d("Has Sentry : ", "" + hasOrderSentry);
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
        checkItemName();
    }

    public void checkItemName() {
        GetItemNameTask getItemNameTask = new GetItemNameTask();
        getItemNameTask.setDataAndDelegate(this, fromContext, itemID);
        getItemNameTask.execute();
    }

    @Override
    public void processFinish(String itemName) {
        this.itemName = itemName;
        AddOrderListenerTask addOrderListenerTask = new AddOrderListenerTask();
        addOrderListenerTask.setContextAndOrderId(fromContext, orderID, itemName);
        addOrderListenerTask.setDelegateAsyncResponse(this);
        addOrderListenerTask.execute();
    }

    @Override
    public void processOrderListenerFinish(OrderSentry spawnedOrderSentry) {
        if (!hasOrderSentry) {
            this.orderSentry = spawnedOrderSentry;
            this.hasOrderSentry = true;
        }

        if (null != itemName && itemName.equalsIgnoreCase("select booth")) {
            Intent selectBoothForOrderIntent = new Intent(fromContext, BoothReservationShowSelection.class);
            selectBoothForOrderIntent.putExtra("orderid", orderID);
            selectBoothForOrderIntent.putExtra("itemid", itemID);
            selectBoothForOrderIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            fromContext.getApplicationContext().startActivity(selectBoothForOrderIntent);
        }
    }
}


class AddOrderListenerTask extends AsyncTask<Void, Void, OrderSentry> {
    private AsyncResponse delegate = null;
    private OrderConnector orderConnector;
    ////// INPUTS
    private Context appContext;
    private String orderID;
    ////// RESULT IS AN ORDER SENTRY
    private OrderSentry orderSentry;

    void setContextAndOrderId(Context receivedContext, String orderID, String itemName) {
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
            orderSentry.setCurrentLineItems(orderConnector.getOrder(orderID).getLineItems());
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