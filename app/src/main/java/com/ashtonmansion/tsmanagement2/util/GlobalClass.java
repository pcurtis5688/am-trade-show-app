package com.ashtonmansion.tsmanagement2.util;

import android.app.Application;

/**
 * Created by Paul Curtis on 10/16/2016.
 */

public class GlobalClass extends Application {
    ////// PUBLIC GLOBAL VARS
    public boolean applicationHasValidPermissions;
    ////// PRIVATE GLOBAL VARS
    private OrderSentry orderSentry;

    public OrderSentry getOrderSentry() {
        return orderSentry;
    }

    public void setOrderSentry(OrderSentry orderSentry) {
        this.orderSentry = orderSentry;
    }

    public boolean isApplicationHasValidPermissions() {
        return applicationHasValidPermissions;
    }

    public void setApplicationHasValidPermissions(boolean applicationHasValidPermissions) {
        this.applicationHasValidPermissions = applicationHasValidPermissions;
    }
}
