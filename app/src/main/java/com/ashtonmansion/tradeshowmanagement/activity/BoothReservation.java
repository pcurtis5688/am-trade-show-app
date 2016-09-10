package com.ashtonmansion.tradeshowmanagement.activity;

import android.accounts.Account;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.ashtonmansion.amtradeshowmanagement.R;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v3.base.Reference;
import com.clover.sdk.v3.inventory.Category;
import com.clover.sdk.v3.inventory.InventoryConnector;
import com.clover.sdk.v3.inventory.Item;

import java.util.ArrayList;
import java.util.List;

public class BoothReservation extends AppCompatActivity {
    private Context boothReservationActivityContext;
    ////////CLOVER DATA
    private Account merchantAccount;
    private InventoryConnector inventoryConnector;
    private Category passedShowObj;
    private Category show;
    private List<Item> boothList;
    ////////UI OBJECTS
    private TableLayout boothListTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booth_reservation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //////////////FIELD DEFINITIONS & DATA HANDLING
        boothReservationActivityContext = this;
        boothListTable = (TableLayout) findViewById(R.id.booth_selection_booth_table);

        Bundle extrasBundle = getIntent().getExtras();
        if (extrasBundle != null) {
            passedShowObj = (Category) extrasBundle.get("show");
        }

        GetShowBoothsTask getShowBoothsTask = new GetShowBoothsTask();
        getShowBoothsTask.execute();
    }



    private class GetShowBoothsTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(boothReservationActivityContext);
            progressDialog.setMessage("Loading Booths...");
            progressDialog.show();
            boothList = new ArrayList<>();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                merchantAccount = CloverAccount.getAccount(boothReservationActivityContext);
                inventoryConnector = new InventoryConnector(boothReservationActivityContext, merchantAccount, null);
                inventoryConnector.connect();

                ////////FIND CATEGORY IN CLOVER, POPULATE BOOTH REFERENCE LIST
                List<Category> categoryList = inventoryConnector.getCategories();
                List<Reference> boothReferenceList = new ArrayList<>();
                for (Category category : categoryList) {
                    if (category.getId().equalsIgnoreCase(passedShowObj.getId())) {
                        show = category;
                        if (show.hasItems()) boothReferenceList = show.getItems();
                    }
                }

                //////////////ITERATE REF LIST AND ADD BOOTHS TO LIST
                for (Reference boothRef : boothReferenceList) {
                    Item currentBooth = inventoryConnector.getItem(boothRef.getId());
                    currentBooth.setTags(inventoryConnector.getTagsForItem(currentBooth.getId()));
                    boothList.add(currentBooth);
                }
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
            createBoothSelectionTable();
            progressDialog.dismiss();
        }
    }

    private void createBoothSelectionTable() {

        for (Item booth : boothList) {
            TableRow newBoothRow = new TableRow(boothReservationActivityContext);

            TextView boothNumberTv = new TextView(boothReservationActivityContext);
            boothNumberTv.setText(booth.getSku());

            newBoothRow.addView(boothNumberTv);

            boothListTable.addView(newBoothRow);

        }
    }

    private void selectBoothAction() {

        // TODO: 9/10/2016 do once all super activity smooth
    }

}
