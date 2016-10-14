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

import java.util.ArrayList;

/**
 * Created by paul curtis on 10/12/2016.
 */

public class EventManagerReceiver extends BroadcastReceiver implements AsyncResponse {
    ////// INPUTS FROM RECEIVER
    private Intent fromIntent;
    private String orderID;
    private String itemID;
    private Context fromContext;
    private String itemName;
    ////// PUBLIC ACCESS VARS
    ////// PRIVATE CONNEX & DATA
    public OrderSentry orderSentry;

    ////// Test purposes segment
    @Override
    public void onReceive(Context context, Intent intent) {
        this.fromContext = context;
        this.fromIntent = intent;
        if (intent.getAction().equalsIgnoreCase("com.clover.intent.action.LINE_ITEM_ADDED")) {
            itemID = intent.getStringExtra(Intents.EXTRA_CLOVER_ITEM_ID);
            orderID = intent.getStringExtra(Intents.EXTRA_CLOVER_ORDER_ID);
            checkItemName();
        }
    }

    public void checkItemName() {
        GetItemNameTask getItemNameTask = new GetItemNameTask();
        getItemNameTask.setData(fromContext, itemID);
        getItemNameTask.setDelegateAsyncResponse(this);
        getItemNameTask.execute();
    }

    @Override
    public void processFinish(String itemName) {
        this.itemName = itemName;
        AddOrderListenerTask addOrderListenerTask = new AddOrderListenerTask();
        addOrderListenerTask.setContextAndOrderId(fromContext, orderID);
        addOrderListenerTask.setDelegateAsyncResponse(this);
        addOrderListenerTask.execute();
    }

    @Override
    public void processOrderListenerFinish(OrderSentry orderSentry) {
        ////// ONLY NEED TO SWITCH ACTIVITY IF ITEM IS SELECT BOOTH
        this.orderSentry = orderSentry;
        if (itemName.equalsIgnoreCase("select booth")) {
            Intent selectBoothForOrderIntent = new Intent(fromContext, BoothReservationShowSelection.class);
            selectBoothForOrderIntent.putExtra("orderid", orderID);
            selectBoothForOrderIntent.putExtra("itemid", itemID);
            ArrayList<OrderSentry> orderSentries = new ArrayList<>();
            orderSentries.add(orderSentry);
            selectBoothForOrderIntent.putExtra("orderSentry", orderSentry);
            selectBoothForOrderIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            fromContext.getApplicationContext().startActivity(selectBoothForOrderIntent);
        }
    }

    public void setOrderSentry(OrderSentry orderSentry) {
        this.orderSentry = orderSentry;
    }

    public OrderSentry getOrderSentry() {
        return orderSentry;
    }
}

class GetItemNameTask extends AsyncTask<Void, Void, String> {
    private AsyncResponse delegate = null;
    private InventoryConnector inventoryConnector;
    ////// INPUTS
    private String itemID;
    ////// RESULT ITEM NAME
    private String itemName;

    void setData(Context taskContext, String itemID) {
        Context appContext = taskContext.getApplicationContext();
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

    public void setDelegateAsyncResponse(EventManagerReceiver callingReceiver) {
        delegate = callingReceiver;
    }
}

class AddOrderListenerTask extends AsyncTask<Void, Void, OrderSentry> {
    private AsyncResponse delegate = null;
    private OrderConnector orderConnectorToAddListener;
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
        orderConnectorToAddListener = new OrderConnector(appContext, CloverAccount.getAccount(appContext), null);
        orderConnectorToAddListener.connect();
    }

    @Override
    protected OrderSentry doInBackground(Void... params) {
        try {
            orderSentry = new OrderSentry(orderID, appContext);
            orderConnectorToAddListener.addOnOrderChangedListener(orderSentry);
        } catch (Exception e) {
            Log.d("ExceptionCheckInBooth: ", e.getMessage(), e.getCause());
        }
        return orderSentry;
    }

    @Override
    protected void onPostExecute(OrderSentry orderListenerSentry) {
        super.onPostExecute(orderListenerSentry);
        orderConnectorToAddListener.disconnect();
        delegate.processOrderListenerFinish(orderListenerSentry);
    }
}

////// don't touch
interface AsyncResponse {
    void processFinish(String output);

    void processOrderListenerFinish(OrderSentry orderUpdateListener2s);
}

