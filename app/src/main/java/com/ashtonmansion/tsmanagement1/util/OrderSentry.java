package com.ashtonmansion.tsmanagement1.util;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v3.order.LineItem;
import com.clover.sdk.v3.order.OrderConnector;

import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul curtis on 10/13/2016.
 */

public class OrderSentry implements OrderConnector.OnOrderUpdateListener2 {
    ////// INITIAL OR UPDATED DATA
    private String orderId;
    private Context sentryContext;
    ////// UTILITY DATA
    private List<LineItem> currentLineItems;
    private List<String> lineItemsAddedList;

    ////// LIST OF IDS THAT INDICATE SPECIFIC
    public OrderSentry(Context sentryContext, String orderId) {
        this.orderId = orderId;
        this.sentryContext = sentryContext;
        this.lineItemsAddedList = new ArrayList<>();
        getItemListByOrderId(orderId);
    }

    public void getItemListByOrderId(String orderId) {
        GetItemListByOrderIdTASK getItemListByOrderIdTASK = new GetItemListByOrderIdTASK();
        getItemListByOrderIdTASK.setInputs(this, sentryContext, orderId);
        getItemListByOrderIdTASK.execute();
    }

    public List<LineItem> getCurrentLineItems() {
        return currentLineItems;
    }

    public void setCurrentLineItems(List<LineItem> inputLineItems) {
        this.currentLineItems = inputLineItems;
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
        Log.d("Sentry ", "Line Item Added (with ID(s)): ");
        for (String lineItemID : lineItemIds) {
            Log.d("- ", "(" + lineItemID + ")");
            lineItemsAddedList.add(lineItemID);
            
        }
    }

    @Override
    public void onLineItemsUpdated(String orderId, List<String> lineItemIds) {
        Log.d("Sentry: ", "onLineItemsUpdated() hit");
    }

    @Override
    public void onLineItemsDeleted(String orderId, List<String> lineItemIds) {
        ////// ensure not a specific booth that was removed
        Log.d("Sentry ", "Line Item Deleted (with ID(s)): ");
        for (String lineItemID : lineItemIds) {
            Log.d("- ", "(" + lineItemID + ")");
            if (lineItemsAddedList.contains(lineItemID)) {
                Log.d("RecognizedAdded", "yayyyy");
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

class GetItemListByOrderIdTASK extends AsyncTask<Void, Void, List<LineItem>> {
    ////// CALLER AND CONNECTOR
    private OrderSentry caller;
    private OrderConnector connector;
    ////// INPUTS FROM CALLER
    private Context callingContext;
    private String orderId;
    ////// OUTPUTS TO CALLER
    private List<LineItem> lineItemList;

    public void setInputs(OrderSentry caller, Context callingContext, String orderId) {
        this.caller = caller;
        this.callingContext = callingContext;
        this.orderId = orderId;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        connector = new OrderConnector(callingContext, CloverAccount.getAccount(callingContext), null);
        connector.connect();
    }

    @Override
    protected List<LineItem> doInBackground(Void... params) {
        try {
            lineItemList = connector.getOrder(orderId).getLineItems();
        } catch (Exception e) {
            Log.d("Excpt: ", e.getMessage(), e.getCause());
        }
        return lineItemList;
    }

    @Override
    protected void onPostExecute(List<LineItem> itemsReturned) {
        caller.setCurrentLineItems(itemsReturned);
    }
}