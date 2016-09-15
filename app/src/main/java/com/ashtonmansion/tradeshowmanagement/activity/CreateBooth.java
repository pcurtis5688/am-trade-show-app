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
import android.widget.TextView;

import com.ashtonmansion.amtradeshowmanagement.R;
import com.ashtonmansion.tradeshowmanagement.db.TradeShowDB;
import com.ashtonmansion.tradeshowmanagement.util.GlobalUtils;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v3.inventory.Category;
import com.clover.sdk.v3.inventory.InventoryConnector;
import com.clover.sdk.v3.inventory.Item;
import com.clover.sdk.v3.inventory.Tag;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CreateBooth extends AppCompatActivity {
    ///////ACTIVITY VARS
    private Context createBoothActivityContext;
    ///////UI AND DATA VARS
    private EditText createBoothNumberField;
    private EditText createBoothPriceField;
    private EditText createBoothSizeField;
    private EditText createBoothAreaField;
    private EditText createBoothTypeField;
    ///////CLOVER VARS
    private Item newBooth;
    private String showID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        createBoothActivityContext = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_booth);
        Toolbar toolbar = (Toolbar) findViewById(R.id.create_booth_toolbar);
        setSupportActionBar(toolbar);

        //////DEFINE REST OF THE UI FIELDS FOR ACTIVITY
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
        createBoothTypeField = (EditText) findViewById(R.id.create_booth_type_field);

        ///////GET CURRENT SHOWID & SHOWNAME from CONFIGURE BOOTHS ACTIVITY
        Bundle extrasBundle = getIntent().getExtras();
        if (extrasBundle != null) {
            showID = (String) extrasBundle.get("showid");
            String showName = (String) extrasBundle.get("showname"); ////CONNECT FIELD ITEMS
            TextView createBoothActivityHeader = (TextView) findViewById(R.id.create_booth_activity_header_w_showname);
            createBoothActivityHeader.setText(String.valueOf(getResources().getString(R.string.title_activity_create_booth) + " - " + showName));
        }
    }

    private class CreateBoothTask extends AsyncTask<Void, Void, Void> {
        //////PROGRESS VARS
        private ProgressDialog progressDialog;
        //////CLOVER CONNECTION
        private Account merchantAccount;
        private InventoryConnector inventoryConnector;
        //////NEW BOOTH DATA VARS
        private String createBoothNumberFieldData;
        private String createBoothPriceFieldData;
        private String createBoothSizeFieldData;
        private String createBoothAreaFieldData;
        private String createBoothTypeFieldData;
        private String formattedBoothSizeTagName;
        private String formattedBoothAreaTagName;
        private String formattedBoothTypeTagName;
        private List<Category> showObjectInListForBooth;
        private Tag boothSizeTagWithID;
        private Tag boothAreaTagWithID;
        private Tag boothTypeTagWithID;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(createBoothActivityContext);
            progressDialog.setMessage("Saving New Booth...");
            progressDialog.show();
            //////////////GRAB DATA FROM FIELDS AND POPULATE DATA FIELDS PRIOR TO CREATION
            createBoothNumberFieldData = createBoothNumberField.getText().toString();
            createBoothPriceFieldData = createBoothPriceField.getText().toString();
            createBoothSizeFieldData = createBoothSizeField.getText().toString();
            createBoothAreaFieldData = createBoothAreaField.getText().toString();
            createBoothTypeFieldData = createBoothTypeField.getText().toString();
            //////////////ADD PREFIX TO PSEUDO-TAG FIELDS
            formattedBoothSizeTagName = GlobalUtils.getFormattedTagName(createBoothSizeFieldData, "Size");
            formattedBoothAreaTagName = GlobalUtils.getFormattedTagName(createBoothAreaFieldData, "Area");
            formattedBoothTypeTagName = GlobalUtils.getFormattedTagName(createBoothTypeFieldData, "Type");
            //////////////CREATE A NEW ITEM OBJECT AND POPULATE DATA
            newBooth = new Item();
            newBooth.setName(createBoothNumberFieldData);
            newBooth.setSku(createBoothNumberFieldData); ////in effect, booth no.
            newBooth.setPrice(GlobalUtils.getLongFromFormattedPriceString(createBoothPriceFieldData));
            //////////////INITIALIZE ADDITIONAL ITEMS
            showObjectInListForBooth = new ArrayList<>();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                merchantAccount = CloverAccount.getAccount(createBoothActivityContext);
                inventoryConnector = new InventoryConnector(createBoothActivityContext, merchantAccount, null);
                inventoryConnector.connect();
                /////CREATE BOOTH AND AND ASSIGN RETURN BOOTH TO THE SAME (W ID)
                newBooth = inventoryConnector.createItem(newBooth);

                /////LOCATE THE SHOW OBJECT (CATEGORY), ADD REF TO NEW BOOTH, UPDATE
                for (Category show : inventoryConnector.getCategories()) {
                    if (show.getId().equalsIgnoreCase(showID)) {
                        inventoryConnector.addItemToCategory(newBooth.getId(), show.getId());
                        showObjectInListForBooth.add(show);
                        newBooth.setCategories(showObjectInListForBooth);
                        inventoryConnector.updateItem(newBooth);
                        inventoryConnector.updateItemStock(newBooth.getId(), 1);
                    }
                }
                List<String> boothIdInStringList = new ArrayList<>();
                boothIdInStringList.add(newBooth.getId());

                boothSizeTagWithID = inventoryConnector.createTag(new Tag().setName(formattedBoothSizeTagName));
                boothAreaTagWithID = inventoryConnector.createTag(new Tag().setName(formattedBoothAreaTagName));
                boothTypeTagWithID = inventoryConnector.createTag(new Tag().setName(formattedBoothTypeTagName));

                inventoryConnector.assignItemsToTag(boothSizeTagWithID.getId(), boothIdInStringList);
                inventoryConnector.assignItemsToTag(boothAreaTagWithID.getId(), boothIdInStringList);
                inventoryConnector.assignItemsToTag(boothTypeTagWithID.getId(), boothIdInStringList);
            } catch (RemoteException | BindingException | ServiceException | ClientException e1) {
                Log.e("Clover Excptn; ", e1.getClass().getName() + " : " + e1.getMessage());
            } finally {
                inventoryConnector.disconnect();
            }

            /////DO LOCAL DATABASE INSERTS USING THE COMPLETE CLOVER OBJECT
            TradeShowDB tradeShowDB = new TradeShowDB(createBoothActivityContext);
            boolean successfulBoothCreationLOCAL = tradeShowDB.createBoothItem(
                    newBooth.getId(),
                    newBooth.getId(),
                    newBooth.getSku(),
                    newBooth.getPrice(),
                    createBoothSizeFieldData,
                    createBoothAreaFieldData,
                    createBoothTypeFieldData);

            if (!successfulBoothCreationLOCAL)
                Log.e("Add Local Booth: ", "CREATE BOOTH W ID: " + newBooth.getId() + " , <<FAILED>>");
            tradeShowDB = null;
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            finish();
        }
    }

    public void cancelCreateBooth(View view) {
        finish();
    }

    public void finalizeBoothCreation(View view) {
        CreateBoothTask createBoothTask = new CreateBoothTask();
        createBoothTask.execute();
    }
}
