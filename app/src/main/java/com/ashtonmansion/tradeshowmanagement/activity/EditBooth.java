package com.ashtonmansion.tradeshowmanagement.activity;

import android.accounts.Account;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.ashtonmansion.amtradeshowmanagement.R;

import com.ashtonmansion.tradeshowmanagement.db.TradeShowDB;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v3.inventory.InventoryConnector;
import com.clover.sdk.v3.inventory.Item;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class EditBooth extends AppCompatActivity {
    //////ACTIVITY VARS
    private Context editBoothActivityContext;
    //////CLOVER VARS
    private Account merchantAccount;
    private InventoryConnector inventoryConnector;
    private Item booth;
    //////UI VARS
    private TextView editBoothHeaderTv;
    private EditText editBoothNumberField;
    private EditText editBoothPriceField;
    private EditText editBoothSizeField;
    private EditText editBoothAreaField;
    private EditText editBoothCategoryField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //////////////////NAVIGATION AND UI WORK//////////////
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_booth);
        Toolbar toolbar = (Toolbar) findViewById(R.id.edit_booth_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //////////////////DATA WORK///////////////////////////
        editBoothActivityContext = this;
        Bundle extrasBundle = getIntent().getExtras();
        if (extrasBundle != null) {
            booth = (Item) extrasBundle.get("booth");
            editBoothHeaderTv = (TextView) findViewById(R.id.edit_booth_header);
            editBoothNumberField = (EditText) findViewById(R.id.edit_booth_number_field);
            editBoothPriceField = (EditText) findViewById(R.id.edit_booth_price_field);
            editBoothSizeField = (EditText) findViewById(R.id.edit_booth_size_field);
            editBoothAreaField = (EditText) findViewById(R.id.edit_booth_area_field);
            editBoothCategoryField = (EditText) findViewById(R.id.edit_booth_category_field);

            editBoothHeaderTv.setText("Edit Booth - " + booth.getName());
            //// TODO: 8/31/2016 string resource
            editBoothNumberField.setText(booth.getSku());
            editBoothSizeField.setText("set size");
            editBoothAreaField.setText("set area");
            editBoothCategoryField.setText("category?");

            //// TODO: 8/31/2016 price handling
            editBoothPriceField.setText(booth.getPrice().toString());
            editBoothPriceField.addTextChangedListener(new TextWatcher() {

                private String current = "";

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (!charSequence.toString().equals(current)) {
                        editBoothPriceField.removeTextChangedListener(this);
                        String cleanString = charSequence.toString().replaceAll("[$,.]", "");
                        double parsed = Double.parseDouble(cleanString);
                        NumberFormat numberFormatter = NumberFormat.getCurrencyInstance(Locale.US);
                        String formattedPrice = numberFormatter.format(parsed / 100.0);
                        current = formattedPrice;
                        editBoothPriceField.setText(formattedPrice);
                        editBoothPriceField.setSelection(formattedPrice.length());
                        editBoothPriceField.addTextChangedListener(this);
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });


        } else {
            Log.e("Major error: ", "edit booth started without booth obj");
        }
    }

    public void saveBoothChangesAction(View view) {
        UpdateBoothTask updateBoothTask = new UpdateBoothTask();
        updateBoothTask.execute();
    }

    private class UpdateBoothTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;
        private Item boothToUpdate;
        private String editBoothNumberFieldData;
        private String editBoothPriceFieldData;
        ////////////////
        private long priceLongFormat;
        private double priceDoubleFormat;
        //////////////
        private String editBoothSizeFieldData;
        private String editBoothAreaFieldData;
        private String editBoothCategoryFieldData;
        private boolean sqliteUpdateBoothSuccess;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(editBoothActivityContext);
            progressDialog.setMessage("Saving Booth...");
            progressDialog.show();

            editBoothNumberFieldData = editBoothNumberField.getText().toString();
            editBoothSizeFieldData = editBoothSizeField.getText().toString();
            editBoothAreaFieldData = editBoothAreaField.getText().toString();
            editBoothCategoryFieldData = editBoothCategoryField.getText().toString();

            //PRICE HANDLING BELOW
            editBoothPriceFieldData = editBoothPriceField.getText().toString();
            String cleanString = editBoothPriceFieldData.toString().replaceAll("[$,.]", "");
            priceLongFormat = Long.parseLong(cleanString);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                merchantAccount = CloverAccount.getAccount(editBoothActivityContext);
                inventoryConnector = new InventoryConnector(editBoothActivityContext, merchantAccount, null);
                inventoryConnector.connect();

                boothToUpdate = inventoryConnector.getItem(booth.getId());
                boothToUpdate.setPrice(priceLongFormat);
                boothToUpdate.setSku(editBoothNumberFieldData);

                inventoryConnector.updateItem(boothToUpdate);

                TradeShowDB tradeShowDatabase = new TradeShowDB(editBoothActivityContext);
                sqliteUpdateBoothSuccess = tradeShowDatabase.updateSingleBoothByCloverId(boothToUpdate.getId(), boothToUpdate.getName(),
                        boothToUpdate.getSku(), boothToUpdate.getPrice(), editBoothSizeFieldData,
                        editBoothAreaFieldData, editBoothCategoryFieldData);
            } catch (RemoteException | BindingException | ServiceException | ClientException e1) {
                Log.e("Clover Excptn; ", e1.getClass().getName() + " : " + e1.getMessage());
            } catch (SQLiteException e2) {
                Log.e("SQLiteExcptn: ", e2.getClass().getName() + " - " + e2.getMessage());
            } finally {
                inventoryConnector.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (sqliteUpdateBoothSuccess) {
                progressDialog.dismiss();
                finish();
            } else {
                Log.e("Add Local Booth: ", "CREATE BOOTH W ID: " + boothToUpdate.getId() + " , " + sqliteUpdateBoothSuccess);
            }
        }
    }
}
