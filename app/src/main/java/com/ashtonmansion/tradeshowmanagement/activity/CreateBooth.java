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
import com.clover.sdk.v3.base.Reference;
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
    ///////CLOVER VARS
    private Account merchantAccount;
    private InventoryConnector inventoryConnector;
    private String showID;
    private String showName;
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
        ///////GET CURRENT SHOWID & SHOWNAME from CONFIGURE BOOTHS ACTIVITY
        Bundle extrasBundle = getIntent().getExtras();
        if (extrasBundle != null) {
            showID = extrasBundle.get("showid").toString();
            showName = extrasBundle.get("showname").toString();
        }
    }

    public void createBoothFinalizeAction(View view) {
        CreateBoothTask createBoothTask = new CreateBoothTask();
        createBoothTask.execute();
    }

    private class CreateBoothTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;
        private Item newBooth = new Item();
        private Item newBoothItemReturned = new Item();
        private String createBoothNameFieldData;
        private String createBoothNumberFieldData;
        private String createBoothSizeFieldData;
        private String createBoothAreaFieldData;
        private String createBoothCategoryFieldData;
        private List<Tag> tagListForNewBooth = new ArrayList<>();
        private Category matchingShowPulledFromClover = new Category();
        private List<Category> showListForNewBooth = new ArrayList<>();
        private List<Reference> boothReferenceListForShow = new ArrayList<>();
        private Reference newBoothReferenceForShow = new Reference();
        //LOCAL DATABASE VARS
        private TradeShowDB tradeShowDB;
        private boolean localDbCreateSuccess;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(createBoothActivityContext);
            progressDialog.setMessage("Creating Booth...");
            progressDialog.show();
            ///////////////GRAB DATA FROM UI FIELDS
            createBoothNameFieldData = createBoothNameField.getText().toString();
            createBoothNumberFieldData = createBoothNumberField.getText().toString();
            createBoothSizeFieldData = createBoothSizeField.getText().toString();
            createBoothAreaFieldData = createBoothAreaField.getText().toString();
            createBoothCategoryFieldData = createBoothCategoryField.getText().toString();
            ///////////////CREATE A NEW ITEM OBJECT AND POPULATE DATA
            newBooth.setName(createBoothNameFieldData);
            newBooth.setSku(createBoothNumberFieldData);
            newBooth.setPrice(GlobalUtils.getLongFromFormattedPriceString(createBoothPriceField.getText().toString()));

            //// TODO: 9/2/2016 price is still effed grab from edit booth
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                //////////CONNECT TO INVENTORY
                merchantAccount = CloverAccount.getAccount(createBoothActivityContext);
                inventoryConnector = new InventoryConnector(createBoothActivityContext, merchantAccount, null);
                inventoryConnector.connect();

                //////////SET ITEM OBJ FOR NEW BOOTH TO THE ID CLOVER-RETURNED ITEM
                //////////EVERYTHING AFTER ABOVE LINE NEEDS TO USE THE ITEM OBJ RETURNED
                newBoothItemReturned = inventoryConnector.createItem(newBooth);

                //////////CREATE EACH NEW TAG ASSIGNED TO THE NEW BOOTH IN INV.
                tagListForNewBooth.add(new Tag().setName("Size - " + createBoothSizeFieldData));
                tagListForNewBooth.add(new Tag().setName("Area - " + createBoothAreaFieldData));
                tagListForNewBooth.add(new Tag().setName("Category - " + createBoothCategoryFieldData));
                for (Tag newBoothTag : tagListForNewBooth) {
                    inventoryConnector.createTag(newBoothTag);
                }
                newBoothItemReturned.setTags(tagListForNewBooth);

                //////NOW, CONFIGURE THE CATEGORY (SHOW) FOR THE NEW BOOTH
                List<Category> entireShowList = inventoryConnector.getCategories();
                for (Category tempShow : entireShowList) {
                    if (tempShow.getId().equalsIgnoreCase(showID))
                        matchingShowPulledFromClover = tempShow;
                }
                showListForNewBooth.add(matchingShowPulledFromClover);
                newBoothItemReturned.setCategories(showListForNewBooth);

                /////CREATE BOOTH REFERENCE OBJECT AND PROPERLY ADD TO SYSTEM
                newBoothReferenceForShow.setId(newBoothItemReturned.getId());
                /////IF SHOW OBJECT ALREADY HAD OTHER BOOTHS, OTHERWISE MAKE NEW LIST
                if (matchingShowPulledFromClover.getItems().size() > 0) {
                    boothReferenceListForShow = matchingShowPulledFromClover.getItems();
                } else {
                    boothReferenceListForShow = new ArrayList<>();
                }
                boothReferenceListForShow.add(newBoothReferenceForShow);
                matchingShowPulledFromClover.setItems(boothReferenceListForShow);
                inventoryConnector.updateCategory(matchingShowPulledFromClover);
                inventoryConnector.updateItem(newBoothItemReturned);
                /////DO LOCAL DATABASE INSERTS USING THE COMPLETE CLOVER OBJECT
                tradeShowDB = new TradeShowDB(createBoothActivityContext);
                localDbCreateSuccess =
                        tradeShowDB.createBoothItem(
                                newBoothItemReturned.getId(),
                                newBoothItemReturned.getName(),
                                newBoothItemReturned.getSku(),
                                newBoothItemReturned.getPrice(),
                                createBoothSizeFieldData,
                                createBoothAreaFieldData,
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
            } else {
                finish();
            }
        }
    }
}
