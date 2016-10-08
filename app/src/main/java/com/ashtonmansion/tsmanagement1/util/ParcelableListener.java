package com.ashtonmansion.tsmanagement1.util;

import android.os.Parcel;
import android.os.Parcelable;
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
    private List<ParcelableListener> parcelableListeners;
    private Order orderFetched;
    private String finalBoothID;
    private int idx;

    public ParcelableListener() {
        ////// EMPTY BODY FOR FUTURE USE
    }

    // "De-parcel object
    private ParcelableListener(Parcel sourceParcel) {
        parcelableListeners = new ArrayList<>();

        orderID = sourceParcel.readString();
        if (sourceParcel.readValue(Order.class.getClassLoader()) != null) {
            orderFetched = (Order) sourceParcel.readValue(Order.class.getClassLoader());
        }

        sourceParcel.readTypedList(parcelableListeners, ParcelableListener.CREATOR);
        Log.d("Through Parcel ", "Read");
    }


    public OrderConnector.OnOrderUpdateListener2 getOrderUpdateListener2() {
        return orderUpdateListener2;
    }

    private void setOrderUpdateListener2(OrderConnector.OnOrderUpdateListener2 orderUpdateListener2) {
        this.orderUpdateListener2 = orderUpdateListener2;
    }

    public String getOrderID() {
        return orderID;
    }

    private void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public Order getOrderFetched() {
        return orderFetched;
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
        dest.writeString(orderID);
        dest.writeValue(orderFetched);
        dest.writeTypedList(parcelableListeners);
    }
}