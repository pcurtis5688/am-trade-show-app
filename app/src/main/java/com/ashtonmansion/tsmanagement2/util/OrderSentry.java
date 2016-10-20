package com.ashtonmansion.tsmanagement2.util;

import android.content.Context;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v3.base.Reference;
import com.clover.sdk.v3.inventory.InventoryConnector;
import com.clover.sdk.v3.inventory.Item;
import com.clover.sdk.v3.order.LineItem;
import com.clover.sdk.v3.order.OrderConnector;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul curtis
 * (pcurtis5688@gmail.com)
 * on 10/13/2016.
 */

class OrderSentry implements OrderConnector.OnOrderUpdateListener2 {
    ////// INITIAL OR UPDATED DATA
    private String orderId;
    private Context sentryContext;
    ////// PERSISTENT DATA
    private List<LineItem> specificBoothWatchList;

    ////// CONSTRUCTOR / PUBLIC METHODS
    OrderSentry(Context sentryContext, String orderId) {
        this.orderId = orderId;
        this.sentryContext = sentryContext;
        this.specificBoothWatchList = new ArrayList<>();
        checkInitialLineItems();
    }

    String getOrderId() {
        return orderId;
    }

    ////// ORDER SENTRY PRIVATE METHODS
    private void checkInitialLineItems() {
        GetLineItemsForOrderTask getLineItemsForOrderTask = new GetLineItemsForOrderTask();
        getLineItemsForOrderTask.setDataAndDelegate(this, sentryContext, orderId);
        getLineItemsForOrderTask.execute();
    }

    void receiveInitialLineItemListAndProcess(List<LineItem> lineItems) {
        if (null != lineItems && lineItems.size() > 0) {
            for (LineItem currentLineItem : lineItems) {
                if (currentLineItem.getName().contains("Booth #")) {
                    specificBoothWatchList.add(currentLineItem);
                    Log.d("Sentry", "Specific Booth located as initial line item and added to watch list...");
                }
            }
        } else if (null != lineItems && lineItems.size() == 0) {
            Log.d("Sentry", "Initial Line Item List was 0 upon sentry creation...");
        } else {
            Log.d("Sentry", "Null Initial Line Items");
        }
    }

    void processLineItemAddedInternal(List<LineItem> lineItemsAdded) {
        for (LineItem currentLineItem : lineItemsAdded) {
            if (currentLineItem.getName().contains("Booth #")) {
                specificBoothWatchList.add(currentLineItem);
                Log.d("Sentry", "Specific booth added to order detected and line item added to watch list...");
            }
        }
    }

    void informResetToAvailableSuccessful(boolean handleTriggeredItemsSuccessful) {
        ////// NO LONGER HAVE ACCESS TO DELETED ITEMS IN CLOVER
        ////// BODY LEFT FOR CLARITY
        Log.d("Sentry", "Triggered Items Handled Successfully: " + handleTriggeredItemsSuccessful);
    }

    ////// ORDER LISTENER METHOD IMPLEMENTATIONS
    @Override
    public void onOrderCreated(String orderId) {
        Log.d("Sentry: ", "onOrderCreated() hit... should new Sentry spawn?");
    }

    @Override
    public void onOrderUpdated(String orderId, boolean selfChange) {
        ////// THIS METHOD IS *ALSO* CALLED IN ADDITION TO ADD/DELETE METHODS
        ////// WHEN SWAP OCCURS
        Log.d("Sentry: ", "onOrderUpdated() hit");
    }

    @Override
    public void onOrderDeleted(String orderId) {
        Log.d("Sentry: ", "onOrderDeleted() hit");
    }

    @Override
    public void onOrderDiscountAdded(String orderId, String discountId) {
        Log.d("Sentry: ", "onOrderDiscountAdded() hit");
    }

    @Override
    public void onOrderDiscountsDeleted(String orderId, List<String> discountIds) {
        Log.d("Sentry: ", "onOrderDiscountsDeleted() hit");
    }

    @Override
    public void onLineItemsAdded(String orderId, List<String> lineItemIds) {
        ////// NOTIFY SENTRY OF ITEMS ADDED AND LOG OCCURANCE
        ProcessLineItemAddedTask processLineItemAddedTask = new ProcessLineItemAddedTask(this, sentryContext, lineItemIds);
        processLineItemAddedTask.execute();
        Log.d("Sentry ", "Line Item Added (with ID(s)): " + lineItemIds.toString());
    }

    @Override
    public void onLineItemsUpdated(String orderId, List<String> lineItemIds) {
        Log.d("Sentry: ", "onLineItemsUpdated() hit");
    }

    @Override
    public void onLineItemsDeleted(String orderId, List<String> lineItemIDsDeleted) {
        ////// NOTIFY SENTRY OF ITEMS DELETED AND LOG OCCURANCE
        List<LineItem> watchListLineItemsTriggered = new ArrayList<>();
        for (String lineItemDeletedID : lineItemIDsDeleted) {
            for (LineItem watchListLineItem : specificBoothWatchList) {
                if (watchListLineItem.getId().equals(lineItemDeletedID)) {
                    Log.d("Sentry", "Watch List ID Triggered... " + lineItemDeletedID);
                    watchListLineItemsTriggered.add(watchListLineItem);
                }
            }
        }
        HandleWatchlistTriggeredTask handleWatchlistTriggeredTask = new HandleWatchlistTriggeredTask(this, sentryContext, lineItemIDsDeleted, watchListLineItemsTriggered);
        handleWatchlistTriggeredTask.execute();
    }

    @Override
    public void onLineItemModificationsAdded(String orderId, List<String> lineItemIds, List<String> modificationIds) {
        Log.d("Sentry: ", "onLineItemModificationsAdded() hit");
    }

    @Override
    public void onLineItemDiscountsAdded(String orderId, List<String> lineItemIds, List<String> discountIds) {
        Log.d("Sentry: ", "onLineItemDiscountsAdded() hit");
    }

    @Override
    public void onLineItemExchanged(String orderId, String oldLineItemId, String newLineItemId) {
        Log.d("Sentry: ", "onLineItemExchanged() hit");
    }

    @Override
    public void onPaymentProcessed(String orderId, String paymentId) {
        Log.d("Sentry: ", "onPaymentProcessed() hit");
    }

    @Override
    public void onRefundProcessed(String orderId, String refundId) {
        Log.d("Sentry: ", "onRefundProcessed() hit");
    }

    @Override
    public void onCreditProcessed(String orderId, String creditId) {
        Log.d("Sentry: ", "onCreditProcessed() hit");
    }

    private void toastMessageSentryContext(String msg) {
        Toast.makeText(sentryContext, "Toast Message in Sentry Context: " + msg, Toast.LENGTH_LONG).show();
    }
}

class ProcessLineItemAddedTask extends AsyncTask<Void, Void, List<LineItem>> {
    ////// CALLER AND CONNECTOR
    private OrderSentry caller;
    private OrderConnector orderConnector;
    ////// INPUTS FROM CALLER
    private String orderID;
    private Context callingContext;
    private List<String> lineItemsAddedStringIDList;
    ////// OUTPUTS TO CALLER
    private List<LineItem> lineItemsAddedList;

    ProcessLineItemAddedTask(OrderSentry caller, Context callingContext, List<String> lineItemsAddedStringIDList) {
        this.caller = caller;
        this.orderID = caller.getOrderId();
        this.callingContext = callingContext;
        this.lineItemsAddedStringIDList = lineItemsAddedStringIDList;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        lineItemsAddedList = new ArrayList<>();
        orderConnector = new OrderConnector(callingContext, CloverAccount.getAccount(callingContext), null);
        orderConnector.connect();
    }

    @Override
    protected List<LineItem> doInBackground(Void... params) {
        try {
            for (String lineItemAddedString : lineItemsAddedStringIDList) {
                for (LineItem currentLineItem : orderConnector.getOrder(orderID).getLineItems()) {
                    if (currentLineItem.getId().equals(lineItemAddedString)) {
                        lineItemsAddedList.add(currentLineItem);
                    }
                }
            }
        } catch (Exception e) {
            Log.d("Excpt: ", e.getMessage(), e.getCause());
        }
        return lineItemsAddedList;
    }

    @Override
    protected void onPostExecute(List<LineItem> lineItemsAddedList) {
        caller.processLineItemAddedInternal(lineItemsAddedList);
    }
}

class HandleWatchlistTriggeredTask extends AsyncTask<Void, Void, Boolean> {
    /**
     * Created by paul curtis
     * (pcurtis5688@gmail.com)
     * * on 10/13/2016.
     */
    ////// CALLER AND CONNECTOR
    private OrderSentry caller;
    private InventoryConnector inventoryConnector;
    ////// INPUTS FROM CALLER
    private String orderID;
    private Context callingContext;
    private List<String> deletedLineItemIDsTriggered;
    private List<LineItem> lineItemsDeleted;
    ////// OUTPUTS TO CALLER
    private boolean resetToAvailableSuccessful;

    HandleWatchlistTriggeredTask(OrderSentry caller,
                                 Context callingContext,
                                 List<String> deletedLineItemIDsTriggered,
                                 List<LineItem> lineItemsDeleted) {
        this.caller = caller;
        this.orderID = caller.getOrderId();
        this.callingContext = callingContext;
        this.lineItemsDeleted = lineItemsDeleted;
        this.deletedLineItemIDsTriggered = deletedLineItemIDsTriggered;
        this.resetToAvailableSuccessful = false;
        Log.d("Sentry", "Deleted Line Item IDs Triggered: " + deletedLineItemIDsTriggered.toString());
    }

    @Override
    protected void onPreExecute() {
        ////// INITIALIZE INVENTORY CONNECTION TO HANDLE SETTING AVAILABIL
        super.onPreExecute();
        inventoryConnector = new InventoryConnector(callingContext, CloverAccount.getAccount(callingContext), null);
        inventoryConnector.connect();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            if (null != lineItemsDeleted && lineItemsDeleted.size() > 0) {
                for (LineItem lineItem : lineItemsDeleted) {
                    Reference lineItemReference = lineItem.getItem();
                    Log.d("Sentry", "Cross-referenced Item (set to available) : " + inventoryConnector.getItem(lineItemReference.getId()).getName());
                    Item resetBoothItem = inventoryConnector.getItem(lineItemReference.getId()).setCode("AVAILABLE");
                    inventoryConnector.updateItem(resetBoothItem);
                    resetToAvailableSuccessful = true;
                }
            }
        } catch (ClientException | ServiceException | BindingException | RemoteException e) {
            Log.d("Sentry excptn", e.getMessage(), e.getCause());
            e.printStackTrace();
        }
        return resetToAvailableSuccessful;
    }

    @Override
    protected void onPostExecute(Boolean resetToAvailableSuccessful) {
        super.onPostExecute(resetToAvailableSuccessful);
        inventoryConnector.disconnect();
        caller.informResetToAvailableSuccessful(resetToAvailableSuccessful);
    }
}

class GetLineItemsForOrderTask extends AsyncTask<Void, Void, List<LineItem>> {
    private OrderSentry delegate = null;
    private OrderConnector orderConnector;
    ////// INPUTS
    private String orderID;
    ////// RESULT ITEM NAME
    private List<LineItem> lineItems;

    void setDataAndDelegate(OrderSentry orderSentry, Context taskContext, String orderID) {
        //Context appContext = taskContext.getApplicationContext();
        this.delegate = orderSentry;
        this.orderID = orderID;
        orderConnector = new OrderConnector(taskContext, CloverAccount.getAccount(taskContext), null);
        orderConnector.connect();
    }

    @Override
    protected List<LineItem> doInBackground(Void... params) {
        try {
            lineItems = orderConnector.getOrder(orderID).getLineItems();
        } catch (Exception e) {
            Log.d("ExceptionCheckInBooth: ", e.getMessage(), e.getCause());
        }
        return lineItems;
    }

    @Override
    protected void onPostExecute(List<LineItem> lineItems) {
        super.onPostExecute(lineItems);
        orderConnector.disconnect();
        delegate.receiveInitialLineItemListAndProcess(lineItems);
    }
}