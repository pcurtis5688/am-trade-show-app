package com.ashtonmansion.tsmanagement1.util;

import android.app.Application;

/**
 * Created by Paul Curtis on 10/16/2016.
 */

public class GlobalClass extends Application {
    private OrderSentry orderSentry;

    public OrderSentry getOrderSentry() {
        return orderSentry;
    }

    public void setOrderSentry(OrderSentry orderSentry) {
        this.orderSentry = orderSentry;
    }
}
