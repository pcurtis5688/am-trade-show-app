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
import com.ashtonmansion.tradeshowmanagement.util.GlobalUtils;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v3.inventory.Attribute;
import com.clover.sdk.v3.inventory.InventoryConnector;
import com.clover.sdk.v3.inventory.Item;
import com.clover.sdk.v3.inventory.Option;
import com.clover.sdk.v3.inventory.Tag;
import com.clover.sdk.v3.payments.Payment;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CreateBooth extends AppCompatActivity {
    ///////ACTIVITY VARS
    private Context createBoothActivityContext;
    private ProgressDialog progressDialog;
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

    private class CreateShowTask extends AsyncTask<Void, Void, Void> {
        private Item newBooth;
        private String createBoothNameFieldData;
        private String createBoothSizeFieldData;
        private String createBoothAreaFieldData;
        private String createBoothCategoryFieldData;
        private Tag sizeTag;
        private Tag areaTag;
        private Tag categoryTag;
        private TradeShowDB tradeShowDB;
        private boolean localDbCreateSuccess;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(createBoothActivityContext);
            progressDialog.setMessage("Creating Booth...");
            progressDialog.show();

            newBooth = new Item();
            createBoothNameFieldData = createBoothNameField.getText().toString();
            createBoothSizeFieldData = createBoothSizeField.getText().toString();
            createBoothAreaFieldData = createBoothAreaField.getText().toString();
            createBoothCategoryFieldData = createBoothCategoryField.getText().toString();

            List<Tag> tagListForBooth = new ArrayList<>();
            sizeTag = new Tag().setName("Size - " + createBoothSizeField.getText().toString());
            areaTag = new Tag().setName("Area - " + createBoothAreaField.getText().toString());
            categoryTag = new Tag().setName("Category - " + createBoothCategoryField.getText().toString());

            tagListForBooth.add(sizeTag);
            tagListForBooth.add(areaTag);
            tagListForBooth.add(categoryTag);
            newBooth.setTags(tagListForBooth);

            newBooth.setName(createBoothNameField.getText().toString());
            newBooth.setPrice(GlobalUtils.getLongFromFormattedPriceString(createBoothPriceField.getText().toString()));
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                //// CONNECT TO INVENTORY, CREATE TAGS FOR BOOTH IN INVENTORY, CREATE BOOTH
                merchantAccount = CloverAccount.getAccount(createBoothActivityContext);
                inventoryConnector = new InventoryConnector(createBoothActivityContext, merchantAccount, null);

                inventoryConnector.connect();
                inventoryConnector.createTag(sizeTag);
                inventoryConnector.createTag(areaTag);
                inventoryConnector.createTag(categoryTag);
                String returnedBoothID = inventoryConnector.createItem(newBooth).getId();

                newBooth.setId(returnedBoothID);
                /////AFTER GETTING CLOVER ID, DO LOCAL INSERTS
                tradeShowDB = new TradeShowDB(createBoothActivityContext);
                localDbCreateSuccess = tradeShowDB.createBoothItem(newBooth.getId(), newBooth.getName(),
                        newBooth.getPrice(), createBoothSizeFieldData, createBoothAreaFieldData,
                        createBoothCategoryFieldData);
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
            progressDialog.dismiss();
            if (!localDbCreateSuccess) {
                Log.e("Add Local Booth: ", "CREATE BOOTH W ID: " + newBooth.getId() + " , " + localDbCreateSuccess);
            }
            finish();
        }
    }
}
