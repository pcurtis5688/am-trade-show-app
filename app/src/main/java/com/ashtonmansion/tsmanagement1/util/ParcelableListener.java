package com.ashtonmansion.tsmanagement1.util;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.clover.sdk.v3.order.Order;
import com.clover.sdk.v3.order.OrderConnector;

import java.util.ArrayList;
import java.util.List;

public class ParcelableListener implements Parcelable {
    // Creator
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public ParcelableListener createFromParcel(Parcel in) {
            return new ParcelableListener(in);
        }

        public ParcelableListener[] newArray(int size) {
            return new ParcelableListener[size];
        }
    };
    private String orderID;
    private OrderConnector.OnOrderUpdateListener2 orderUpdateListener2;
    private List<OrderConnector.OnOrderUpdateListener2> updateListener2List;
    private Order orderFetched;
    private String finalBoothID;
    private int idx;

    public ParcelableListener() {
        ////// EMPTY BODY FOR FUTURE USE
    }

    // "De-parcel object
    private ParcelableListener(Parcel sourceParcel) {
        updateListener2List = new ArrayList<>();
        ////// INITIALIZE THE OBJECTS AND READ THE ORDER AND LISTENER
        if (sourceParcel.readValue(Order.class.getClassLoader()) != null) {
            orderFetched = (Order) sourceParcel.readValue(Order.class.getClassLoader());
        }
        orderUpdateListener2 = (OrderConnector.OnOrderUpdateListener2) sourceParcel.readValue(OrderConnector.OnOrderUpdateListener2.class.getClassLoader());

        Log.d("Through Parcel ", "Read");
    }

    public OrderConnector.OnOrderUpdateListener2 getOrderUpdateListener2() {
        return orderUpdateListener2;
    }

    public String getOrderID() {
        return orderID;
    }

    public Order getOrderFetched() {
        return orderFetched;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public void setOrderUpdateListener2(OrderConnector.OnOrderUpdateListener2 orderUpdateListener2) {
        this.orderUpdateListener2 = orderUpdateListener2;
        this.updateListener2List.add(orderUpdateListener2);
    }

    public void setOrderFetched(Order orderFetched) {
        this.orderFetched = orderFetched;
    }

    public void setFinalBoothID(String finalBoothID) {
        this.finalBoothID = finalBoothID;
    }

    ////// IMPLEMENTING PARCELABLE HERE SO THAT DATA CAN BE PASSED
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(orderFetched);
    //    dest.writeTypedList(updateListener2List);
    }
}