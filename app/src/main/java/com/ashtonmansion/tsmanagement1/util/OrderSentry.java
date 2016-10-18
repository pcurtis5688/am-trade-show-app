package com.ashtonmansion.tsmanagement1.util;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.clover.sdk.util.CloverAccount;
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
    private List<String> specificBoothWatchList;

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
        for (LineItem currentLineItem : lineItems) {
            if (currentLineItem.getName().contains("Booth #")) {
                specificBoothWatchList.add(currentLineItem.getId());
                Log.d("Sentry", "Specific Booth located as initial line item and added to watch list...");
            }
        }
    }

    void processLineItemAddedInternal(List<LineItem> lineItemsAdded) {
        for (LineItem currentLineItem : lineItemsAdded) {
            if (currentLineItem.getName().contains("Booth #")) {
                specificBoothWatchList.add(currentLineItem.getId());
                Log.d("Sentry", "Specific booth added to order detected and line item added to watch list...");
            } else {
                Log.d("Sentry", "Ignored non-booth object: " + currentLineItem.getName());
            }
        }
    }

    void processLineItemDeletedInternal() {
        ////// NO LONGER HAVE ACCESS TO DELETED ITEMS IN CLOVER
        ////// BODY LEFT FOR CLARITY
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
        for (String lineItemDeletedID : lineItemIDsDeleted) {
            if (specificBoothWatchList.contains(lineItemDeletedID)) {
                Log.d("Sentry", "Watch List ID Triggered... " + lineItemDeletedID
                        + "\n Need to set that booth to available...");
            }
        }
        ProcessLineItemDeletedTask processLineItemDeletedTask = new ProcessLineItemDeletedTask(this, sentryContext, lineItemIDsDeleted);
        processLineItemDeletedTask.execute();
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

class ProcessLineItemDeletedTask extends AsyncTask<Void, Void, List<LineItem>> {
    ////// CALLER AND CONNECTOR
    private OrderSentry caller;
    private OrderConnector orderConnector;
    ////// INPUTS FROM CALLER
    private String orderID;
    private Context callingContext;
    private List<String> deletedLineItemIDsTriggered;
    ////// OUTPUTS TO CALLER
    private List<LineItem> lineItemsDeletedList;

    ProcessLineItemDeletedTask(OrderSentry caller,
                               Context callingContext,
                               List<String> deletedLineItemIDsTriggered) {
        this.caller = caller;
        this.orderID = caller.getOrderId();
        this.callingContext = callingContext;
        this.deletedLineItemIDsTriggered = deletedLineItemIDsTriggered;
        Log.d("Sentry", "Deleted Line Item IDs" + deletedLineItemIDsTriggered.toString());
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        ////// INITIALIZE LIST FOR PROCESS LINE ITEM TASK
        lineItemsDeletedList = new ArrayList<>();
        orderConnector = new OrderConnector(callingContext, CloverAccount.getAccount(callingContext), null);
        orderConnector.connect();
    }

    @Override
    protected List<LineItem> doInBackground(Void... params) {
        ////// SENTRY'S CONNECTION POST ITEM DELETION... USE TO MAKE AVAILABLE IF WAS A SPECIFIC BOOTH
        try {
            //// TODO: 10/18/2016 later
        } catch (Exception e) {
            Log.d("Sentry excptn", e.getMessage(), e.getCause());
            e.printStackTrace();
        }
        return lineItemsDeletedList;
    }

    @Override
    protected void onPostExecute(List<LineItem> lineItemsDeleted) {
        super.onPostExecute(lineItemsDeleted);
        caller.processLineItemDeletedInternal();
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