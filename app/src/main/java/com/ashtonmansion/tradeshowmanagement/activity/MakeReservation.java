package com.ashtonmansion.tradeshowmanagement.activity;

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.ashtonmansion.amtradeshowmanagement.R;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v3.inventory.InventoryConnector;
import com.clover.sdk.v3.inventory.Item;

import java.util.List;

public class MakeReservation extends AppCompatActivity {
    private Context makeReservationActivityContext;
    private List<Item> boothList;
    private InventoryConnector inventoryConnector;
    private Account merchantAccount;
    private TableLayout boothTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_reservation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        makeReservationActivityContext = this;
        merchantAccount = CloverAccount.getAccount(makeReservationActivityContext);
        inventoryConnector = new InventoryConnector(makeReservationActivityContext, merchantAccount, null);
        boothTable = (TableLayout) findViewById(R.id.make_reservation_booth_table);

        fetchData();
    }

    public void fetchData() {
        GetAvailableBoothsTask getAvailableBoothsTask = new GetAvailableBoothsTask();
        getAvailableBoothsTask.execute();
    }

    private void viewBoothDetail(Item booth) {
        Intent viewBoothDetailIntent = new Intent(makeReservationActivityContext, ViewBoothDetail.class);
        viewBoothDetailIntent.putExtra("booth", booth);
        startActivity(viewBoothDetailIntent);
    }

    private class GetAvailableBoothsTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
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
            for (final Item booth : boothList) {
                TableRow boothRow = new TableRow(makeReservationActivityContext);
                TextView boothIDTV = new TextView(makeReservationActivityContext);
                TextView boothName = new TextView(makeReservationActivityContext);
                boothIDTV.setText(booth.getId());
                boothName.setText(booth.getName());
                boothRow.addView(boothIDTV);
                boothRow.addView(boothName);
                boothRow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        viewBoothDetail(booth);
                    }
                });
                boothTable.addView(boothRow);
            }
        }
    }
}
