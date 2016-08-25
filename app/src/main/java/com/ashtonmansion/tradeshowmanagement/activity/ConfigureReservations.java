package com.ashtonmansion.tradeshowmanagement.activity;

import android.accounts.Account;
import android.content.Context;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.ashtonmansion.amtradeshowmanagement.R;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v3.inventory.InventoryConnector;
import com.clover.sdk.v3.inventory.Item;

import java.util.ArrayList;
import java.util.List;

public class ConfigureReservations extends AppCompatActivity {
    private List<Item> boothList;
    private TextView item_stock_count;
    private InventoryConnector inventoryConnector;
    private Account merchantAccount;
    private Context configureReservationsContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_reservations);
        configureReservationsContext = this;
        merchantAccount = CloverAccount.getAccount(configureReservationsContext);
        inventoryConnector = new InventoryConnector(configureReservationsContext, merchantAccount, null);

        fetchData();
    }

    public void fetchData() {
        UpdateItemStockTask updateStockTask = new UpdateItemStockTask();

        updateStockTask.execute();
    }


    private class UpdateItemStockTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            // params comes from the execute() call: params[0] is the url.
            try {
                boothList = inventoryConnector.getItems();
            } catch (RemoteException | BindingException | ServiceException | ClientException e1) {
                Log.e("Clover Excptn; ", e1.getClass().getName() + " : " + e1.getMessage());
            } finally {
                inventoryConnector.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            item_stock_count = (TextView) findViewById(R.id.item_stock);
            if (boothList != null) {
                item_stock_count.setText(boothList.size());
            } else {
                item_stock_count.setText("Can't fetch items...");
            }
        }
    }
}

