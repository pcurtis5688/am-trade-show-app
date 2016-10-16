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
 * Created by paul curtis (pcurtis5688@gmail.com) on 10/13/2016.
 */

public class OrderSentry implements OrderConnector.OnOrderUpdateListener2 {
    ////// INITIAL OR UPDATED DATA
    private String orderId;
    private Context sentryContext;
    ////// UTILITY DATA
    private List<String> lineItemsAddedList;

    ////// LIST OF IDS THAT INDICATE SPECIFIC
    OrderSentry(Context sentryContext, String orderId) {
        this.orderId = orderId;
        this.sentryContext = sentryContext;
        this.lineItemsAddedList = new ArrayList<>();
        Log.d("Sentry", "Coming online....");
    }

    String getOrderId() {
        return orderId;
    }

    void processLineItemAddedInternal(List<LineItem> lineItemsAdded) {
        for (LineItem currentLineItem : lineItemsAdded) {
            if (currentLineItem.getName().equalsIgnoreCase("select booth")) {
                Log.d("Sentry", "\'Select Booth\' Located...");
            } else if (currentLineItem.getName().equalsIgnoreCase("electricity")) {
                Log.d("Sentry", "\'Electricity\' Located...");
            }
        }
    }

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
        ////// THIS METHOD CALLED AFTER LINE ITEM DELETION WHEN BOOTH SWAP OCCURS
        Log.d("Sentry ", "Line Item Added (with ID(s)): " + lineItemIds.toString());
        for (String lineItemID : lineItemIds) {
            lineItemsAddedList.add(lineItemID);
        }
        ProcessLineItemAddedTask processLineItemAddedTask = new ProcessLineItemAddedTask(this, sentryContext, lineItemIds);
        processLineItemAddedTask.execute();
    }

    @Override
    public void onLineItemsUpdated(String orderId, List<String> lineItemIds) {
        Log.d("Sentry: ", "onLineItemsUpdated() hit");
    }

    @Override
    public void onLineItemsDeleted(String orderId, List<String> lineItemIds) {
        ////// ensure not a specific booth that was removed
        Log.d("Sentry ", "Line Item Deleted (with ID(s)): " + lineItemIds.toString());
        for (String lineItemID : lineItemIds) {
            if (lineItemsAddedList.contains(lineItemID)) {
                Log.d("Sentry", "Deletion of previously added item...");
            }
        }
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