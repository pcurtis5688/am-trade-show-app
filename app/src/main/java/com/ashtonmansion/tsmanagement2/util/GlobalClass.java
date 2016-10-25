package com.ashtonmansion.tsmanagement2.util;

import android.app.Application;
import android.content.Intent;

/**
 * Created by Paul Curtis on 10/16/2016.
 */

public class GlobalClass extends Application {
    ////// PUBLIC GLOBAL VARS
    public boolean applicationHasValidPermissions;
    public boolean boothReservationStartedFromRegister;
    public Intent fromRegisterIntent;
    public String fromOrderId;
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

    public Intent getFromRegisterIntent() {
        return fromRegisterIntent;
    }

    public void setFromRegisterIntent(Intent fromRegisterIntent) {
        this.fromRegisterIntent = fromRegisterIntent;
    }

    public boolean isBoothReservationStartedFromRegister() {
        return boothReservationStartedFromRegister;
    }

    public void setBoothReservationStartedFromRegister(boolean boothReservationStartedFromRegister) {
        this.boothReservationStartedFromRegister = boothReservationStartedFromRegister;
    }

    public String getFromOrderId() {
        return fromOrderId;
    }

    public void setFromOrderId(String fromOrderId) {
        this.fromOrderId = fromOrderId;
    }
}
