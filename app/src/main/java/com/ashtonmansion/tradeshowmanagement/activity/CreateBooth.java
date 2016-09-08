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
    ///////UI AND DATA VARS
    private EditText createBoothNumberField;
    private EditText createBoothPriceField;
    private EditText createBoothSizeField;
    private EditText createBoothAreaField;
    private EditText createBoothCategoryField;
    ///////CLOVER VARS
    private Item newBooth;
    private List<Tag> newBoothTags;
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
        createBoothCategoryField = (EditText) findViewById(R.id.create_booth_category_field);

        ///////GET CURRENT SHOWID & SHOWNAME from CONFIGURE BOOTHS ACTIVITY
        Bundle extrasBundle = getIntent().getExtras();
        if (extrasBundle != null) {
            showID = (String) extrasBundle.get("showid");
            String showName = (String) extrasBundle.get("showname"); ////CONNECT FIELD ITEMS
            TextView createBoothActivityHeader = (TextView) findViewById(R.id.create_booth_activity_header_w_showname);
            createBoothActivityHeader.setText(String.valueOf(getResources().getString(R.string.title_activity_create_booth) + " - " + showName));
        } else {
            Log.e("Major Error: ", "Create Booth Activity started without a show reference");
            finish();
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
        private String createBoothCategoryFieldData;
        private String formattedBoothSizeTagName;
        private String formattedBoothAreaTagName;
        private String formattedBoothCategoryTagName;
        /////////////below are def new objects dont' need to worry
        private Category show;
        private List<Category> showObjectInListForBooth;
        private Reference newBoothReference;
        private List<Reference> newBoothReferenceInList;
        private Tag boothSizeTagWithID;
        private Tag boothAreaTagWithID;
        private Tag boothCategoryTagWithID;
        private List<Tag> newBoothTags;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //// TODO: 9/4/2016 fix this when done with createbooth testing
            // progressDialog = new ProgressDialog(createBoothActivityContext);
            // progressDialog.setMessage("Saving New Booth...");
            // progressDialog.show();
            //////////////GRAB DATA FROM FIELDS AND POPULATE DATA FIELDS PRIOR TO CREATION
            createBoothNumberFieldData = createBoothNumberField.getText().toString();
            createBoothPriceFieldData = createBoothPriceField.getText().toString();
            createBoothSizeFieldData = createBoothSizeField.getText().toString();
            createBoothAreaFieldData = createBoothAreaField.getText().toString();
            createBoothCategoryFieldData = createBoothCategoryField.getText().toString();
            //////////////ADD PREFIX TO PSEUDO-TAG FIELDS
            formattedBoothSizeTagName = GlobalUtils.getFormattedTagName(createBoothSizeFieldData, "Size");
            formattedBoothAreaTagName = GlobalUtils.getFormattedTagName(createBoothAreaFieldData, "Area");
            formattedBoothCategoryTagName = GlobalUtils.getFormattedTagName(createBoothCategoryFieldData, "Category");
            //////////////CREATE A NEW ITEM OBJECT AND POPULATE DATA
            newBooth = new Item();
            newBooth.setName(createBoothNumberFieldData);
            newBooth.setSku(createBoothNumberFieldData); ////in effect, booth no.
            newBooth.setPrice(GlobalUtils.getLongFromFormattedPriceString(createBoothPriceFieldData));
            //////////////INITIALIZE ADDITIONAL ITEMS
            newBoothReference = new Reference();
            newBoothReferenceInList = new ArrayList<>();
            newBoothTags = new ArrayList<>();

        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                merchantAccount = CloverAccount.getAccount(createBoothActivityContext);
                inventoryConnector = new InventoryConnector(createBoothActivityContext, merchantAccount, null);
                inventoryConnector.connect();
                /////CREATE BOOTH, GET ID, MAKE NEW BOOTH REFERENCE, ADD TO A LIST
                newBooth = inventoryConnector.createItem(newBooth);
                newBoothReference.setId(newBooth.getId());
                newBoothReferenceInList.add(newBoothReference);

                /////LOCATE THE SHOW OBJECT (CATEGORY), ADD REF TO NEW BOOTH, UPDATE
                for (Category theCategory : inventoryConnector.getCategories()) {
                    if (theCategory.getId().equalsIgnoreCase(showID)) {
                        show = theCategory;
                        // boothListForShow = show.getItems();
                        // boothListForShow.add(newBoothReference);
                        // show.setItems(boothListForShow);
                        // // TODO: 9/8/2016 new two lines are probably redundant
                        show.getItems().add(newBoothReference);
                        inventoryConnector.addItemToCategory(newBooth.getId(), show.getId());
                        inventoryConnector.updateCategory(show);
                    }
                }

                //// TODO: 9/8/2016 uncomment if above statement doesn't handle
                //                showObjectInListForBooth = new ArrayList<>();
                //                showObjectInListForBooth.add(show);
                //                newBooth.setCategories(showObjectInListForBooth);

                boothSizeTagWithID = inventoryConnector.createTag(new Tag().setName(formattedBoothSizeTagName));
                boothAreaTagWithID = inventoryConnector.createTag(new Tag().setName(formattedBoothAreaTagName));
                boothCategoryTagWithID = inventoryConnector.createTag(new Tag().setName(formattedBoothCategoryTagName));
                boothSizeTagWithID.setItems(newBoothReferenceInList);
                boothAreaTagWithID.setItems(newBoothReferenceInList);
                boothCategoryTagWithID.setItems(newBoothReferenceInList);
                inventoryConnector.updateTag(boothSizeTagWithID);
                inventoryConnector.updateTag(boothAreaTagWithID);
                inventoryConnector.updateTag(boothCategoryTagWithID);

                newBoothTags.add(boothSizeTagWithID);
                newBoothTags.add(boothAreaTagWithID);
                newBoothTags.add(boothCategoryTagWithID);
                //// TODO: 9/8/2016 test if need below statement
                //   inventoryConnector.getItem(newBooth.getId()).setTags(newBoothTags);
                newBooth.setTags(newBoothTags);
                inventoryConnector.updateItem(newBooth);
                inventoryConnector.updateItemStock(newBooth.getId(), 1);

                GlobalUtils.valuesTester("SizeTagID: ", boothSizeTagWithID.getId());
                GlobalUtils.valuesTester("AreaTagID: ", boothAreaTagWithID.getId());
                GlobalUtils.valuesTester("CatTagID: ", boothCategoryTagWithID.getId());
            } catch (RemoteException | BindingException | ServiceException | ClientException e1) {
                Log.e("Clover Excptn; ", e1.getClass().getName() + " : " + e1.getMessage());
            } finally {
                inventoryConnector.disconnect();
            }

            //// TODO: 9/7/2016 decide on name issue and change here
            /////DO LOCAL DATABASE INSERTS USING THE COMPLETE CLOVER OBJECT
            TradeShowDB tradeShowDB = new TradeShowDB(createBoothActivityContext);
            boolean successfulBoothCreationLOCAL = tradeShowDB.createBoothItem(
                    newBooth.getId(),
                    newBooth.getId(),
                    newBooth.getSku(),
                    newBooth.getPrice(),
                    createBoothSizeFieldData,
                    createBoothAreaFieldData,
                    createBoothCategoryFieldData);

            if (!successfulBoothCreationLOCAL)
                Log.e("Add Local Booth: ", "CREATE BOOTH W ID: " + newBooth.getId() + " , <<FAILED>>");
            tradeShowDB = null;
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            //progressDialog.dismiss();
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
