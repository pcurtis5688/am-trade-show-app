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

import java.util.List;

////// don't touch
interface AsyncResponse {
    void processFinish(String output);

    void processOrderListenerFinish(OrderConnector.OnOrderUpdateListener2 orderUpdateListener2s);
}

/**
 * Created by paul curtis on 10/12/2016.
 */

public class EventManagerReceiver extends BroadcastReceiver implements AsyncResponse {
    ////// PUBLIC ACCESS VARS
    ////// PRIVATE CONNEX & DATA
    public OrderConnector.OnOrderUpdateListener2 orderUpdateListener2;
    ////// INPUTS FROM RECEIVER
    private Intent fromIntent;
    private String orderID;
    private String itemID;
    private Context fromContext;
    private String itemName;

    ////// Test purposes segment
    @Override
    public void onReceive(Context context, Intent intent) {
        this.fromContext = context;
        this.fromIntent = intent;
        //  if (intent.getAction().equalsIgnoreCase("com.clover.intent.action.LINE_ITEM_ADDED")) {
        if (null != itemID)
            itemID = intent.getStringExtra(Intents.EXTRA_CLOVER_ITEM_ID);
        else itemID = "";
        if (null != orderID)
            orderID = intent.getStringExtra(Intents.EXTRA_CLOVER_ORDER_ID);
        else orderID = "";
        checkItemName();
        // }
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
    public void processOrderListenerFinish(OrderConnector.OnOrderUpdateListener2 orderUpdateListener2) {
        ////// ONLY NEED TO SWITCH ACTIVITY IF ITEM IS SELECT BOOTH
        this.orderUpdateListener2 = orderUpdateListener2;
        if (null != itemName && itemName.equalsIgnoreCase("select booth")) {
            Intent selectBoothForOrderIntent = new Intent(fromContext, BoothReservationShowSelection.class);
            selectBoothForOrderIntent.putExtra("orderid", orderID);
            selectBoothForOrderIntent.putExtra("itemid", itemID);
            selectBoothForOrderIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            fromContext.getApplicationContext().startActivity(selectBoothForOrderIntent);
        }
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

class AddOrderListenerTask extends AsyncTask<Void, Void, OrderConnector.OnOrderUpdateListener2> {
    private AsyncResponse delegate = null;
    private OrderConnector orderConnectorToAddListener;
    ////// INPUTS
    private Context appContext;
    private String orderID;
    ////// RESULT IS AN ORDER SENTRY
    private OrderConnector.OnOrderUpdateListener2 orderUpdateListener2;

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
    protected OrderConnector.OnOrderUpdateListener2 doInBackground(Void... params) {
        try {
            orderUpdateListener2 = new OrderListenerService();
            orderConnectorToAddListener.addOnOrderChangedListener(orderUpdateListener2);
        } catch (Exception e) {
            Log.d("ExceptionCheckInBooth: ", e.getMessage(), e.getCause());
        }
        return orderUpdateListener2;
    }

    @Override
    protected void onPostExecute(OrderConnector.OnOrderUpdateListener2 orderListenerSentry) {
        super.onPostExecute(orderListenerSentry);
        orderConnectorToAddListener.disconnect();
        delegate.processOrderListenerFinish(orderListenerSentry);
    }
}

