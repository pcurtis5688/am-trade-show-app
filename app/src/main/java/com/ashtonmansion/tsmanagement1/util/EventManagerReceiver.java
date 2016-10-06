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
import com.clover.sdk.v3.order.LineItem;
import com.clover.sdk.v3.order.Order;
import com.clover.sdk.v3.order.OrderConnector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

interface AsyncResponse {
    void processFinish(String output);

    void processOrderListenerFinish(ParcelableListener parcelableListener);
}

public class EventManagerReceiver extends BroadcastReceiver implements AsyncResponse {
    private String orderID = "";
    private String itemID;
    private String itemName = "";
    private Context fromContext;
    private Intent fromIntent;
    private GetItemNameTask getItemNameTask;
    private OrderConnector.OnOrderUpdateListener2 orderListenerGlobal;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.fromContext = context;
        this.fromIntent = intent;
        if (intent.getAction().equalsIgnoreCase("com.clover.intent.action.LINE_ITEM_ADDED")) {
            itemID = intent.getStringExtra(Intents.EXTRA_CLOVER_ITEM_ID);
            orderID = intent.getStringExtra(Intents.EXTRA_CLOVER_ORDER_ID);

            getItemNameTask = new GetItemNameTask();
            getItemNameTask.setDelegateAsyncResponse(this);
            getItemNameTask.setData(context, itemID);
            getItemNameTask.execute();
        }
    }

    @Override
    public void processFinish(String itemName) {
        this.itemName = itemName;
        if (itemName.toLowerCase().contains("select booth")) {
            AddOrderListenerTask addOrderListenerTask = new AddOrderListenerTask();
            addOrderListenerTask.setContextAndOrderID(fromContext, orderID);
            addOrderListenerTask.setDelegateAsyncResponse(this);
            addOrderListenerTask.execute();
        }
    }

    @Override
    public void processOrderListenerFinish(ParcelableListener parcelableListener) {
        ArrayList<ParcelableListener> parcelableListenerList = new ArrayList<>();
        parcelableListenerList.add(parcelableListener);
        startReserveActivity(parcelableListenerList);
    }

    private void startReserveActivity(ArrayList<ParcelableListener> parcelableListenerList) {
        Intent selectBoothForOrderIntent = new Intent(fromContext, BoothReservationShowSelection.class);
        selectBoothForOrderIntent.putParcelableArrayListExtra("parcelablelistener", parcelableListenerList);
        selectBoothForOrderIntent.putExtra("orderid", orderID);
        selectBoothForOrderIntent.putExtra("itemid", itemID);
        selectBoothForOrderIntent.putExtra("itemname", itemName);
        selectBoothForOrderIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        fromContext.getApplicationContext().startActivity(selectBoothForOrderIntent);
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

    public void setDelegateAsyncResponse(EventManagerReceiver callingReceiver) {
        delegate = callingReceiver;
    }
}

class AddOrderListenerTask extends AsyncTask<Void, Void, ParcelableListener> {
    public AsyncResponse delegate = null;
    private OrderConnector orderConnector;
    ////// INPUTS
    private Context appContext;
    private String orderID;
    ////// RESULT ITEM NAME
    private Order orderFetched;
    private OrderConnector.OnOrderUpdateListener2 orderUpdateListener2;
    private ParcelableListener parcelableListener;

    void setContextAndOrderID(Context receivedContext, String orderID) {
        this.appContext = receivedContext.getApplicationContext();
        this.orderID = orderID;
        orderConnector = new OrderConnector(appContext, CloverAccount.getAccount(appContext), null);
        orderConnector.connect();
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected ParcelableListener doInBackground(Void... params) {
        try {
            orderFetched = orderConnector.getOrder(orderID);

            orderUpdateListener2 = new OrderConnector.OnOrderUpdateListener2() {
                private List<String> selectBoothLineItemIDs;
                private Order fetchedOrder;
                private List<LineItem> initialLineItems;

                public void initializeOrderListener2Object() {
                    initialLineItems = fetchedOrder.getLineItems();
                    for (LineItem lineItem : initialLineItems) {
                        if (null != lineItem.getName() && lineItem.getName().toLowerCase().contains("select booth")) {
                            selectBoothLineItemIDs.add(lineItem.getId());
                            Log.d("only see if slct boo ", lineItem.getName() + " id: " + lineItem.getId());
                        }
                    }
                    this.fetchedOrder = orderFetched;
                }

                @Override
                public void onOrderCreated(String orderId) {
                    Log.d("Listener created", " order total : " + fetchedOrder.getTotal() + "and added to the connector");

                    initialLineItems = new ArrayList<>();
                    selectBoothLineItemIDs = new ArrayList<>();
                    initializeOrderListener2Object();
                }

                @Override
                public void onOrderUpdated(String orderId, boolean selfChange) {
                    Log.d("Order updated heard", " orderID: " + orderId + ", selfchange: " + selfChange);
                }

                @Override
                public void onOrderDeleted(String orderId) {
                    Log.d("Order deleted heard", " orderID: " + orderId);
                }

                @Override
                public void onOrderDiscountAdded(String orderId, String discountId) {
                    Log.d("Order dscnt added", " orderID: " + orderId);
                }

                @Override
                public void onOrderDiscountsDeleted(String orderId, List<String> discountIds) {

                }

                @Override
                public void onLineItemsAdded(String orderId, List<String> lineItemIds) {
                    Log.d("LI Added - ", " orderID: " + orderId + " - lineitems: " + lineItemIds.toString());
                }

                @Override
                public void onLineItemsUpdated(String orderId, List<String> lineItemIds) {
                    Log.d("LI Updated - ", " orderID: " + orderId + " - lineitems: " + lineItemIds.toString());
                    Log.d("SWAPPED HERE", " onLineItemsUpdated()");
                }

                @Override
                public void onLineItemsDeleted(String orderId, List<String> lineItemIds) {
                    Log.d("SWAPPED HERE", " onLineItemsDeleted()");

                    if (!Collections.disjoint(lineItemIds, selectBoothLineItemIDs)) {

                    }
                }

                private boolean isLineItemSpecificBooth() {

                    return false;
                }

                @Override
                public void onLineItemModificationsAdded(String orderId, List<String> lineItemIds, List<String> modificationIds) {
                    Log.d("LI ModAdded - ", " orderID: " + orderId + " - lineitems: " + lineItemIds.toString() + " mod ids: " + modificationIds.toString());
                }

                @Override
                public void onLineItemDiscountsAdded(String orderId, List<String> lineItemIds, List<String> discountIds) {

                }

                @Override
                public void onLineItemExchanged(String orderId, String oldLineItemId, String newLineItemId) {
                }

                @Override
                public void onPaymentProcessed(String orderId, String paymentId) {
                }

                @Override
                public void onRefundProcessed(String orderId, String refundId) {
                }

                @Override
                public void onCreditProcessed(String orderId, String creditId) {

                }
            };


            parcelableListener = new ParcelableListener();
            parcelableListener.setOrderID(orderID);
            parcelableListener.setOrderUpdateListener2(orderUpdateListener2);
            //initializeObjects();
            orderConnector.addOnOrderChangedListener(orderUpdateListener2);
        } catch (Exception e) {
            Log.d("ExceptionCheckInBooth: ", e.getMessage(), e.getCause());
        }
        return parcelableListener;
    }

    @Override
    protected void onPostExecute(ParcelableListener parcelableListener) {
        super.onPostExecute(parcelableListener);
        delegate.processOrderListenerFinish(parcelableListener);
    }

    public void setDelegateAsyncResponse(EventManagerReceiver callingReceiver) {
        delegate = callingReceiver;
    }
}

