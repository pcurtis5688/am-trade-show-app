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
    private List<String> specificBoothIdsAttachedToOrder;

    ////// CONSTRUCTOR / PUBLIC METHODS
    OrderSentry(Context sentryContext, String orderId) {
        this.orderId = orderId;
        this.sentryContext = sentryContext;
        this.specificBoothIdsAttachedToOrder = new ArrayList<>();
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
            if (currentLineItem.getName().contains("Booth #"))
                specificBoothIdsAttachedToOrder.add(currentLineItem.getId());
        }
    }

    void processLineItemAddedInternal(List<LineItem> lineItemsAdded) {
        for (LineItem currentLineItem : lineItemsAdded) {
            if (currentLineItem.getName().contains("Booth #")) {
                specificBoothIdsAttachedToOrder.add(currentLineItem.getId());
                Log.d("Sentry", "Specific booth added to order detected...");
            } else {
                Log.d("Sentry", "Ignored non-booth object: " + currentLineItem.getName());
            }
        }
    }

    void processLineItemDeletedInternal(List<LineItem> lineItemsDeleted) {
        ////// TEST EXISTENCE OF SPECIFIC BOOTH
        if (null != lineItemsDeleted && lineItemsDeleted.size() > 0) {
            Log.d("Sentry", lineItemsDeleted.toString());
            for (LineItem lineItem : lineItemsDeleted) {
                if (specificBoothIdsAttachedToOrder.contains(lineItem.getId())) {
                    Log.d("Sentry", "Specific booth being removed from order notification...");
                }
            }
        } else {
            Log.d("Sentry", "Empty method firing process Line Items Internal");
        }
        if (null != lineItemsDeleted && lineItemsDeleted.size() > 0) {
            for (LineItem lineItem : lineItemsDeleted) {
                if (lineItem.getName().contains("Booth #")) {
                    Log.d("Sentry", "Located a specific booth's removal from order...");
                }
            }
        }
    }

    ////// ORDER LISTENER METHOD IMPLEMENTATIONS
    @Override
    public void onOrderCreated(String orderId) {
        Log.d("Sentry: ", "onOrderCreated() hit");
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
        Log.d("Sentry ", "Line Item Added (with ID(s)): " + lineItemIds.toString());
        ProcessLineItemAddedTask processLineItemAddedTask = new ProcessLineItemAddedTask(this, sentryContext, lineItemIds);
        processLineItemAddedTask.execute();
    }

    @Override
    public void onLineItemsUpdated(String orderId, List<String> lineItemIds) {
        Log.d("Sentry: ", "onLineItemsUpdated() hit");
    }

    @Override
    public void onLineItemsDeleted(String orderId, List<String> lineItemIds) {
        ////// NOTIFY SENTRY OF ITEMS DELETED AND LOG OCCURANCE
        ProcessLineItemDeletedTask processLineItemDeletedTask = new ProcessLineItemDeletedTask(this, sentryContext, lineItemIds);
        processLineItemDeletedTask.execute();
        Log.d("Sentry ", "Line Item Deleted (with ID(s)): " + lineItemIds.toString());
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
                        Log.d("SentryTask", "Process Line Item Added() : " + currentLineItem.getName());
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
    private List<String> deletedLineItemIDs;
    ////// OUTPUTS TO CALLER
    private List<LineItem> lineItemsDeletedList;

    ProcessLineItemDeletedTask(OrderSentry caller,
                               Context callingContext,
                               List<String> deletedLineItemIDs) {
        this.caller = caller;
        this.orderID = caller.getOrderId();
        this.callingContext = callingContext;
        this.deletedLineItemIDs = deletedLineItemIDs;
        Log.d("Sentry", "Deleted Line Item IDs" + deletedLineItemIDs.toString());
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        lineItemsDeletedList = new ArrayList<>();
        orderConnector = new OrderConnector(callingContext, CloverAccount.getAccount(callingContext), null);
        orderConnector.connect();
    }

    @Override
    protected List<LineItem> doInBackground(Void... params) {
        try {
            lineItemsDeletedList = orderConnector.getOrder(orderID).getLineItems();

            if (null != lineItemsDeletedList && lineItemsDeletedList.size() > 0) {
                for (String lineItemDeletedString : deletedLineItemIDs) {
                    if (null != orderConnector.getOrder(orderID).getLineItems()) {
                        for (LineItem currentLineItem : orderConnector.getOrder(orderID).getLineItems()) {
                            if (currentLineItem.getId().equals(lineItemDeletedString)) {
                                lineItemsDeletedList.add(currentLineItem);
                            }
                            if (currentLineItem.getName().contains("Booth #")) {
                                Log.d("Sentry", "Detected removal of Specific Booth Object...");

                            }
                        }
                    }
                }
            } else {
                Log.d("Sentry", "WTF");
            }
        } catch (Exception e) {
            Log.d("Sentry excptn", e.getMessage(), e.getCause());
            e.printStackTrace();
        }
        return lineItemsDeletedList;
    }

    @Override
    protected void onPostExecute(List<LineItem> lineItemsDeletedList) {
        caller.processLineItemDeletedInternal(lineItemsDeletedList);
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
            for (LineItem lineitem : lineItems) {
                if (lineitem.getName().contains("Booth #")) {
                    Log.d("Sentry", "Specific Booth As Initial Line Item Detected...");
                } else {
                    Log.d("Sentry", "Other Item Recognized as Initial Line Item");
                }
            }
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