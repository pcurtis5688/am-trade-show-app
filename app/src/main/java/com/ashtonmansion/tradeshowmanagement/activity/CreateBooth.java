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
    ///////UI AND DATA VARS
    private EditText createBoothNameField;
    private EditText createBoothNumberField;
    private EditText createBoothPriceField;
    private EditText createBoothSizeField;
    private EditText createBoothAreaField;
    private EditText createBoothCategoryField;
    private Context createBoothActivityContext;
    ///////CLOVER VARS
    private Account merchantAccount;
    private InventoryConnector inventoryConnector;
    private String showID;
    private String showName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        createBoothActivityContext = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_booth);
        Toolbar toolbar = (Toolbar) findViewById(R.id.create_booth_toolbar);
        setSupportActionBar(toolbar);

        ///////GET CURRENT SHOWID & SHOWNAME from CONFIGURE BOOTHS ACTIVITY
        Bundle extrasBundle = getIntent().getExtras();
        if (extrasBundle != null) {
            showID = (String) extrasBundle.get("showid");
            showName = (String) extrasBundle.get("showname"); ////CONNECT FIELD ITEMS
            TextView createBoothActivityHeader = (TextView) findViewById(R.id.create_booth_activity_header_w_showname);
            createBoothActivityHeader.setText(String.valueOf(getResources().getString(R.string.title_activity_create_booth) + " - " + showName));
        }
        //////DEFINE REST OF THE UI FIELDS FOR ACTIVITY
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

    private class CreateBoothTask extends AsyncTask<Void, Void, Void> {
        //////PROGRESS VARS
        private ProgressDialog progressDialog;
        //////NEW BOOTH DATA VARS
        private Item newBooth = new Item();
        private String createBoothNameFieldData;
        private String createBoothNumberFieldData;
        private String createBoothPriceFieldData;
        private String createBoothSizeFieldData;
        private String createBoothAreaFieldData;
        private String createBoothCategoryFieldData;
        /////////////below are def new objects dont' need to worry
        private List<Category> newBoothCategories;
        private List<Tag> newBoothTags;
        private Reference newBoothReference;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //// TODO: 9/4/2016 fix this when done with createbooth testing
            // progressDialog = new ProgressDialog(createBoothActivityContext);
            // progressDialog.setMessage("Saving New Booth...");
            // progressDialog.show();

            createBoothNameFieldData = createBoothNameField.getText().toString();
            createBoothNumberFieldData = createBoothNumberField.getText().toString();
            createBoothPriceFieldData = createBoothPriceField.getText().toString();
            createBoothSizeFieldData = createBoothSizeField.getText().toString();
            createBoothAreaFieldData = createBoothAreaField.getText().toString();
            createBoothCategoryFieldData = createBoothCategoryField.getText().toString();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                merchantAccount = CloverAccount.getAccount(createBoothActivityContext);
                inventoryConnector = new InventoryConnector(createBoothActivityContext, merchantAccount, null);
                inventoryConnector.connect();
                ///////////////CREATE A NEW ITEM OBJECT AND POPULATE DATA
                newBooth.setName(createBoothNameFieldData);
                newBooth.setSku(createBoothNumberFieldData); ////in effect, booth no.
                //// TODO: 9/2/2016 price is still effed grab from edit booth
                newBooth.setPrice(GlobalUtils.getLongFromFormattedPriceString(createBoothPriceFieldData));


                //// TODO: 9/3/2016 virtually last thing is to add the booth item to the category prior to updating in clovewr
                for (Category theCategory : inventoryConnector.getCategories()) {
                    if (theCategory.getId().equalsIgnoreCase(showID)) {
                        newBooth = inventoryConnector.createItem(newBooth);
                        inventoryConnector.addItemToCategory(newBooth.getId(), theCategory.getId());
                        newBoothReference = new Reference();
                        /////sfd
                        newBoothReference.setId(newBooth.getId());
                        ////                        GlobalUtils.valuesTester("invconnitemgrpid", inventoryConnector.getItems());
                        GlobalUtils.valuesTester("invconnitemgrpid", inventoryConnector.getItems().toString());
                        GlobalUtils.valuesTester("NorthPoleShID:", showID);
                        GlobalUtils.valuesTester("NewBoothID:", newBooth.getId());

                        /////////////////////////HANDLE THE ITEM'S
                        newBoothCategories = new ArrayList<>();
                        newBoothCategories.add(theCategory);
                        newBooth.setCategories(newBoothCategories);

                        List<Reference> showCategoryItemList;
                        if (null != theCategory.getItems() && theCategory.getItems().size() > 0) {
                            showCategoryItemList = new ArrayList<>();
                            for (Reference reference : theCategory.getItems()) {
                                showCategoryItemList.add(reference);
                            }
                            showCategoryItemList.add(newBoothReference);
//                          ////todo IF CATEGORY ALREADY HAS THE NEW BOOTH REFERENCE, SKIP OTHERWISE DO THIS
                            theCategory.setItems(showCategoryItemList);
                        } else {
                            showCategoryItemList = new ArrayList<>();
                            showCategoryItemList.add(newBoothReference);
                            theCategory.setItems(showCategoryItemList);
                        }
                        inventoryConnector.updateCategory(theCategory);


                    }
                }


                List<Reference> boothReferenceInList = new ArrayList<>();
                boothReferenceInList.add(newBoothReference);
                Tag boothSizeTagWithID = inventoryConnector.createTag(new Tag().setName("Size - " + createBoothSizeFieldData));
                boothSizeTagWithID.setItems(boothReferenceInList);
                Tag boothAreaTagWithID = inventoryConnector.createTag(new Tag().setName("Area - " + createBoothAreaFieldData));
                boothAreaTagWithID.setItems(boothReferenceInList);
                Tag boothCategoryTagWithID = inventoryConnector.createTag(new Tag().setName("Category - " + createBoothCategoryFieldData));
                boothCategoryTagWithID.setItems(boothReferenceInList);

                newBoothTags = new ArrayList<>();
                newBoothTags.add(boothSizeTagWithID);
                newBoothTags.add(boothAreaTagWithID);
                newBoothTags.add(boothCategoryTagWithID);
                newBooth.setTags(newBoothTags);

                inventoryConnector.updateTag(boothSizeTagWithID);
                inventoryConnector.updateTag(boothAreaTagWithID);
                inventoryConnector.updateTag(boothCategoryTagWithID);
                inventoryConnector.updateItem(newBooth);
                inventoryConnector.updateItemStock(newBooth.getId(), 1);

                GlobalUtils.valuesTester("SizeTagID: ", boothSizeTagWithID.getId());
                GlobalUtils.valuesTester("AreaTagID: ", boothAreaTagWithID.getId());
                GlobalUtils.valuesTester("CatTagID: ", boothCategoryTagWithID.getId());

            } catch (RemoteException | BindingException | ServiceException | ClientException e1) {
                Log.e("Clover Excptn; ", e1.getClass().getName() + " : " + e1.getMessage());
            }

            /////DO LOCAL DATABASE INSERTS USING THE COMPLETE CLOVER OBJECT
            TradeShowDB tradeShowDB = new TradeShowDB(createBoothActivityContext);

            boolean successfulBoothCreationLOCAL = tradeShowDB.createBoothItem(
                    newBooth.getId(),
                    newBooth.getName(),
                    newBooth.getSku(),
                    newBooth.getPrice(),
                    createBoothSizeFieldData,
                    createBoothAreaFieldData,
                    createBoothCategoryFieldData);

            if (!successfulBoothCreationLOCAL)
                Log.e("Add Local Booth: ", "CREATE BOOTH W ID: " + newBooth.getId() + " , <<FAILED>>");
            inventoryConnector.disconnect();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            finish();
            progressDialog.dismiss();
        }
    }

    public void finalizeBoothCreation(View view) {
        CreateBoothTask createBoothTask = new CreateBoothTask();
        createBoothTask.execute();
    }
}
