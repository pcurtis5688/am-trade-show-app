package com.ashtonmansion.tsmanagement1.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.ashtonmansion.tsmanagement1.activity.BoothReservationShowSelection;
import com.clover.sdk.fragment.ReturnToMerchantDialogFragment;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.Intents;
import com.clover.sdk.v3.inventory.InventoryConnector;
import com.clover.sdk.v3.inventory.Item;
import com.clover.sdk.v3.order.LineItem;
import com.clover.sdk.v3.order.Order;
import com.clover.sdk.v3.order.OrderConnector;

import java.util.ArrayList;
import java.util.List;

interface ListenerAsyncResponse {
    void processGetLineItems(List<LineItem> lineItemsRetrieved);
}

interface AsyncResponse {
    void processFinish(String output);

    void processOrderListenerFinish(OrderConnector.OnOrderUpdateListener2 orderUpdateListener2s);
}

public class EventManagerReceiver extends BroadcastReceiver implements AsyncResponse {
    private String orderID;
    private String itemID;
    private Context fromContext;
    private Intent fromIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.fromContext = context;
        this.fromIntent = intent;
        if (intent.getAction().equalsIgnoreCase("com.clover.intent.action.LINE_ITEM_ADDED")) {
            itemID = intent.getStringExtra(Intents.EXTRA_CLOVER_ITEM_ID);
            orderID = intent.getStringExtra(Intents.EXTRA_CLOVER_ORDER_ID);
            checkItemName();
        }
        Log.d("intent action: ", "" + intent.getAction());
        ////// there must be abetter wayyyyy! no tricky tricky
        //// TODO: 10/12/2016 create a "GLOBAL" delegate and remove these tasks from this class
        //// TODO: 10/12/2016 this class should only have logic for what action was received
    }

    public void checkItemName() {
        GetItemNameTask getItemNameTask = new GetItemNameTask();
        getItemNameTask.setData(fromContext, itemID);
        getItemNameTask.setDelegateAsyncResponse(this);
        getItemNameTask.execute();
    }

    @Override
    public void processFinish(String itemName) {
        ////// ONLY NEED TO DO ANYTHING IF THE ITEM IS A SELECT BOOTH
        if (itemName.toLowerCase().equalsIgnoreCase("select booth")) {
            AddOrderListenerTask addOrderListenerTask = new AddOrderListenerTask();
            addOrderListenerTask.setContextAndOrderId(fromContext, orderID);
            addOrderListenerTask.setDelegateAsyncResponse(this);
            addOrderListenerTask.execute();
        }
    }

    @Override
    public void processOrderListenerFinish(OrderConnector.OnOrderUpdateListener2 orderUpdateListener2) {
        Intent selectBoothForOrderIntent = new Intent(fromContext, BoothReservationShowSelection.class);
        selectBoothForOrderIntent.putExtra("orderid", orderID);
        selectBoothForOrderIntent.putExtra("itemid", itemID);
        selectBoothForOrderIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        fromContext.getApplicationContext().startActivity(selectBoothForOrderIntent);
    }
}

class AddOrderListenerTask extends AsyncTask<Void, Void, OrderConnector.OnOrderUpdateListener2> {
    private AsyncResponse delegate = null;
    private OrderConnector orderConnectorToAddListener;
    ////// INPUTS
    private Context appContext;
    private String orderID;
    ////// RESULT ITEM NAME
    private Order orderFetched;
    private OrderConnector.OnOrderUpdateListener2 orderUpdateListener2;

    void setContextAndOrderId(Context receivedContext, String orderID) {
        this.appContext = receivedContext.getApplicationContext();
        this.orderID = orderID;
    }

    @Override
    protected void onPreExecute() {
        orderConnectorToAddListener = new OrderConnector(appContext, CloverAccount.getAccount(appContext), null);
        orderConnectorToAddListener.connect();
    }

    @Override
    protected OrderConnector.OnOrderUpdateListener2 doInBackground(Void... params) {
        try {
            orderFetched = orderConnectorToAddListener.getOrder(orderID);

            orderUpdateListener2 = new OrderConnector.OnOrderUpdateListener2() {
                public int testint;
                ////// APP CONTEXT / CONNECTION HELPERS
                private Context listenerContext;
                private List<String> selectBoothLineItemIDs;
                private String orderId;
                private Order fetchedOrder;
                private List<LineItem> initialLineItems;
                private List<String> lineItemIdsToWatchFor;
                private ListenerDelegate listenerDelegateClass;
                private List<LineItem> fetchedLineItems;

                private void initializeOrderListener2Object() {
                    initialLineItems = fetchedOrder.getLineItems();
                    for (LineItem lineItem : initialLineItems) {
                        if (null != lineItem.getName() && lineItem.getName().toLowerCase().contains("select booth")) {
                            selectBoothLineItemIDs.add(lineItem.getId());
                        }
                    }
                    this.fetchedOrder = orderFetched;
                    this.listenerContext = appContext;
                    this.listenerDelegateClass = new ListenerDelegate(this);
                }

                @Override
                public void onOrderCreated(String orderId) {
                    Log.d("Listener created", " order total : " + fetchedOrder.getTotal() + "and added to the connector");
                    this.orderId = orderId;
                    initialLineItems = new ArrayList<>();
                    selectBoothLineItemIDs = new ArrayList<>();
                    initializeOrderListener2Object();
                }

                @Override
                public void onOrderUpdated(String orderId, boolean selfChange) {
                    ////// THIS METHOD IS *ALSO* CALLED IN ADDITION TO ADD/DELETE METHODS
                    ////// WHEN SWAP OCCURS
                    Log.d("Order updated heard", " orderID: " + orderId + ", selfchange: " + selfChange);

                }

                @Override
                public void onOrderDeleted(String orderId) {
                    Log.d("Order deleted heard", " orderID: " + orderId);
                }

                @Override
                public void onOrderDiscountAdded(String orderId, String discountId) {
                    Log.d("Discount Added", " Order listener from order: " + orderId);
                }

                @Override
                public void onOrderDiscountsDeleted(String orderId, List<String> discountIds) {

                }

                @Override
                public void onLineItemsAdded(String orderId, List<String> lineItemIds) {
                    ////// THIS METHOD CALLED AFTER LINE ITEM DELETION WHEN BOOTH SWAP OCCURS
                    Log.d("LI Added - ", " orderID: " + orderId + " - lineitems: " + lineItemIds.toString());
                }

                @Override
                public void onLineItemsUpdated(String orderId, List<String> lineItemIds) {
                    Log.d("LI Updated - ", " orderID: " + orderId + " - lineitems: " + lineItemIds.toString());
                    Log.d("SWAPPED HERE", " onLineItemsUpdated()");
                }

                @Override
                public void onLineItemsDeleted(String orderId, List<String> lineItemIds) {
                    ////// HERE WE MUST CHECK TO ENSURE THAT IT WAS NOT A SPECIFIC BOOTH
                    ////// THAT WAS REMOVED...
                    GetOrderLineItemsTask getOrderLineItemObjects = new GetOrderLineItemsTask();
                    getOrderLineItemObjects.setDataAndDelegate(listenerContext, orderId, listenerDelegateClass);
                    getOrderLineItemObjects.execute();
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
                    Log.d("LI EXHANGED: ", "order#" + orderId);
                }

                @Override
                public void onPaymentProcessed(String orderId, String paymentId) {
                }

                @Override
                public void onRefundProcessed(String orderId, String refundId) {
                }

                @Override
                public void onCreditProcessed(String orderId, String creditId) {
                    if (creditId.equalsIgnoreCase("li_deletion")) {
                        Log.d("TRICKY TRICKY", "TRICKY TRICKY");
                        for (LineItem lineItem : listenerDelegateClass.getLineItems()) {
                            Log.d("TRICKY! : ", lineItem.getName());
                        }
                    }
                }
            };

            orderConnectorToAddListener.addOnOrderChangedListener(orderUpdateListener2);
        } catch (Exception e) {
            Log.d("ExceptionCheckInBooth: ", e.getMessage(), e.getCause());
        }
        return orderUpdateListener2;
    }

    @Override
    protected void onPostExecute(OrderConnector.OnOrderUpdateListener2 orderListener2) {
        super.onPostExecute(orderListener2);
        orderConnectorToAddListener.disconnect();
        delegate.processOrderListenerFinish(orderListener2);
    }

    void setDelegateAsyncResponse(EventManagerReceiver callingReceiver) {
        delegate = callingReceiver;
    }
}

class GetOrderLineItemsTask extends AsyncTask<Void, Void, List<LineItem>> {
    private ListenerAsyncResponse listenerDelegate = null;
    private OrderConnector orderConnector;
    ////// INPUTS
    private String orderId;
    ////// RESULT - LIST LINE ITEMS
    private List<LineItem> fetchedLineItems;

    void setDataAndDelegate(Context taskContext, String orderId, ListenerDelegate listenerDelegate) {
        //Context appContext = taskContext.getApplicationContext();
        this.orderId = orderId;
        this.listenerDelegate = listenerDelegate;
        orderConnector = new OrderConnector(taskContext, CloverAccount.getAccount(taskContext), null);
        orderConnector.connect();
    }

    @Override
    protected List<LineItem> doInBackground(Void... params) {
        try {
            fetchedLineItems = orderConnector.getOrder(orderId).getLineItems();
        } catch (Exception e) {
            Log.d("ExceptionCheckInBooth: ", e.getMessage(), e.getCause());
            e.printStackTrace();
        }
        return fetchedLineItems;
    }

    @Override
    protected void onPostExecute(List<LineItem> fetchedLineItems) {
        super.onPostExecute(fetchedLineItems);
        orderConnector.disconnect();
        listenerDelegate.processGetLineItems(fetchedLineItems);
    }
}

class ListenerDelegate implements ListenerAsyncResponse {
    private OrderConnector.OnOrderUpdateListener2 parentListener2;
    private List<LineItem> lineItems;

    ListenerDelegate(OrderConnector.OnOrderUpdateListener2 parentListener2) {
        this.parentListener2 = parentListener2;
    }

    public List<LineItem> getLineItems() {
        return this.lineItems;
    }

    @Override
    public void processGetLineItems(List<LineItem> lineItemsRetrieved) {
        this.lineItems = lineItemsRetrieved;
        parentListener2.onCreditProcessed("placeholder", "li_deletion");
    }
}

////// don't touch
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

