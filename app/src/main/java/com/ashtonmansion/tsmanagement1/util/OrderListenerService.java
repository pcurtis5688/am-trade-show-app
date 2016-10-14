package com.ashtonmansion.tsmanagement1.util;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.clover.sdk.v3.order.OrderConnector;

import java.util.List;

/**
 * Created by paul on 10/14/2016.
 */

public class OrderListenerService extends Service implements OrderConnector.OnOrderUpdateListener2 {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onOrderUpdated(String orderId, boolean selfChange) {
        Log.d("Order was updated, ", "orderId: " + orderId);
    }

    @Override
    public void onOrderCreated(String orderId) {
        Log.d("Order was created, ", "orderId: " + orderId);
    }

    @Override
    public void onOrderDeleted(String orderId) {
        Log.d("Order was deleted, ", "orderId: " + orderId);

    }

    @Override
    public void onOrderDiscountAdded(String orderId, String discountId) {
        Log.d("dscnt added, ", "orderId: " + orderId);

    }

    @Override
    public void onOrderDiscountsDeleted(String orderId, List<String> discountIds) {
        Log.d("Dscnt deleted, ", "orderId: " + orderId);

    }

    @Override
    public void onLineItemsAdded(String orderId, List<String> lineItemIds) {
        Log.d("LI - ADDED, ", "orderId: " + orderId);

    }

    @Override
    public void onLineItemsUpdated(String orderId, List<String> lineItemIds) {
        Log.d("LI - UPDATED", "orderId: " + orderId);

    }

    @Override
    public void onLineItemsDeleted(String orderId, List<String> lineItemIds) {
        Log.d("LI - DELETED", "orderId: " + orderId);
    }

    @Override
    public void onLineItemModificationsAdded(String orderId, List<String> lineItemIds, List<String> modificationIds) {
        Log.d("LI - MOD +", "orderId: " + orderId);
    }

    @Override
    public void onLineItemDiscountsAdded(String orderId, List<String> lineItemIds, List<String> discountIds) {
        Log.d("LineItem Dscnt, ", "orderId: " + orderId);
    }

    @Override
    public void onLineItemExchanged(String orderId, String oldLineItemId, String newLineItemId) {
        Log.d("LI - EXCHANGED ", "orderId: " + orderId);
    }

    @Override
    public void onPaymentProcessed(String orderId, String paymentId) {
        Log.d("PAYMENT PROCSSD", "orderId: " + orderId);
    }

    @Override
    public void onRefundProcessed(String orderId, String refundId) {
        Log.d("Refund Procssed", "orderId: " + orderId);
    }

    @Override
    public void onCreditProcessed(String orderId, String creditId) {
        Log.d("On Credit Procssed ", "orderId: " + orderId);
    }
}
