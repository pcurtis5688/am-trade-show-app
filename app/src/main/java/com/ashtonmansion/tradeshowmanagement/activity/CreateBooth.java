package com.ashtonmansion.tradeshowmanagement.activity;

import android.accounts.Account;
import android.app.ProgressDialog;
import android.content.Context;
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

public class CreateBooth extends AppCompatActivity {
    ///////ACTIVITY VARS
    private Context createBoothActivityContext;
    ///////CLOVER VARS
    private Account merchantAccount;
    private InventoryConnector inventoryConnector;
    ///////UI AND DATA VARS
    private EditText createBoothNameField;
    private EditText createBoothNumberField;
    private EditText createBoothPriceField;
    private EditText createBoothSizeField;
    private EditText createBoothAreaField;
    private EditText createBoothCategoryField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_booth);
        Toolbar toolbar = (Toolbar) findViewById(R.id.create_booth_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ////CONNECT FIELD ITEMS
        createBoothActivityContext = this;
        createBoothNameField = (EditText) findViewById(R.id.create_booth_name_field);
        createBoothNumberField = (EditText) findViewById(R.id.create_booth_number_field);

        createBoothPriceField = (EditText) findViewById(R.id.create_booth_price_field);
        createBoothPriceField.addTextChangedListener(new TextWatcher() {

            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().equals(current)) {
                    createBoothPriceField.removeTextChangedListener(this);
                    String cleanString = charSequence.toString().replaceAll("[$,.]", "");
                    double parsed = Double.parseDouble(cleanString);
                    NumberFormat numberFormatter = NumberFormat.getCurrencyInstance(Locale.US);
                    String formattedPrice = numberFormatter.format(parsed / 100.0);
                    current = formattedPrice;
                    createBoothPriceField.setText(formattedPrice);
                    createBoothPriceField.setSelection(formattedPrice.length());
                    createBoothPriceField.addTextChangedListener(this);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        createBoothSizeField = (EditText) findViewById(R.id.create_booth_size_field);
        createBoothAreaField = (EditText) findViewById(R.id.create_booth_area_field);
        createBoothCategoryField = (EditText) findViewById(R.id.create_booth_category_field);
    }

    public void createBoothFinalizeAction(View view) {
        CreateShowTask createShowTask = new CreateShowTask();
        createShowTask.execute();
    }

    private void closeOutActivity() {
        finish();
    }

    private class CreateShowTask extends AsyncTask<Void, Void, Void> {
        private Item newBooth;
        private ProgressDialog progressDialog;
        private TradeShowDB tradeShowDB;
        private boolean localDbCreateSuccess;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(createBoothActivityContext);
            progressDialog.setMessage("Creating Booth...");
            progressDialog.show();

            newBooth = new Item();
            newBooth.setName(createBoothNameField.getText().toString());
            newBooth.setSku(createBoothNumberField.getText().toString());

            //// TODO: 8/31/2016 unformat price for clover storage
            long priceLongFormat = 0;
            double priceDoubleFormat = 0.00;
            NumberFormat numberFormatter = NumberFormat.getCurrencyInstance(Locale.US);
            try {
                Number parsedNumber = numberFormatter.parse(createBoothPriceField.getText().toString());
                priceLongFormat = parsedNumber.longValue();
                priceDoubleFormat = parsedNumber.doubleValue();
            } catch (ParseException e1) {
                Log.e("Parse Exception: ", e1.getClass().getName() + ", " + e1.getMessage());
            } finally {
                newBooth.setPrice(priceLongFormat);
            }
            Log.i("priceLongFormat: ", "" + priceLongFormat);
            Log.i("priceDoubleFormat: ", "" + priceDoubleFormat);
            //// TODO: 8/31/2016 test tags see if can use for the extra data

        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                merchantAccount = CloverAccount.getAccount(createBoothActivityContext);
                inventoryConnector = new InventoryConnector(createBoothActivityContext, merchantAccount, null);
                inventoryConnector.connect();

                //// CREATE THE BOOTH IN CLOVER
                String returnedBoothID = (inventoryConnector.createItem(newBooth)).getId();
                newBooth.setId(returnedBoothID);

                /////AFTER GETTING CLOVER ID, DO LOCAL INSERTS
                tradeShowDB = new TradeShowDB(createBoothActivityContext);
                localDbCreateSuccess = tradeShowDB.createBoothItem(newBooth.getId(), newBooth.getName(), newBooth.getSku(),
                        newBooth.getPrice(), newBooth.getAlternateName(), newBooth.getAlternateName(),
                        newBooth.getAlternateName());
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
            if (localDbCreateSuccess) {
                progressDialog.dismiss();
                closeOutActivity();
            } else {
                Log.e("Add Local Booth: ", "CREATE BOOTH W ID: " + newBooth.getId() + " , " + localDbCreateSuccess);
                //// TODO: 8/31/2016 validate???? somewhere here.
            }
        }
    }
}
