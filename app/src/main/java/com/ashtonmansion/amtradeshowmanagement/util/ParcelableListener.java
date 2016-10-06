package com.ashtonmansion.amtradeshowmanagement.util;

import android.os.Parcel;
import android.os.Parcelable;

import com.clover.sdk.v3.order.Order;
import com.clover.sdk.v3.order.OrderConnector;

import java.util.ArrayList;

public class ParcelableListener implements Parcelable {
    // Creator
    public static final Parcelable.Creator CREATOR
            = new Parcelable.Creator() {
        public ParcelableListener createFromParcel(Parcel in) {
            return new ParcelableListener(in);
        }

        public ParcelableListener[] newArray(int size) {
            return new ParcelableListener[size];
        }
    };
    private String orderID;
    private OrderConnector.OnOrderUpdateListener2 orderUpdateListener2;
    private ArrayList<ParcelableListener> parcelableListeners;
    private Order orderFetched;
    private String finalBoothID;
    private int idx;

    public ParcelableListener() {
        ////// EMPTY BODY FOR FUTURE USE
    }

    // "De-parcel object
    private ParcelableListener(Parcel in) {
        orderID = in.readString();
        if (in.readValue(Order.class.getClassLoader()) != null) {
            orderFetched = (Order) in.readValue(Order.class.getClassLoader());
        }
        idx = in.readInt();
        parcelableListeners = new ArrayList<>();
        in.readTypedList(parcelableListeners, ParcelableListener.CREATOR);
    }

    public OrderConnector.OnOrderUpdateListener2 getOrderUpdateListener2() {
        return orderUpdateListener2;
    }

    public void setOrderUpdateListener2(OrderConnector.OnOrderUpdateListener2 orderUpdateListener2) {
        this.orderUpdateListener2 = orderUpdateListener2;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
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
        dest.writeTypedList(parcelableListeners);
    }
}