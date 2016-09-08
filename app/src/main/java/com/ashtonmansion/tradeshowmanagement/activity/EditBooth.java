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
import com.ashtonmansion.tradeshowmanagement.util.GlobalUtils;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v3.inventory.InventoryConnector;
import com.clover.sdk.v3.inventory.Item;
import com.clover.sdk.v3.inventory.Tag;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EditBooth extends AppCompatActivity {
    //////ACTIVITY VARS
    private Context editBoothActivityContext;
    //////CLOVER VARS
    private Item booth;
    private List<Tag> boothTags;
    private Tag sizeTag;
    private Tag areaTag;
    private Tag categoryTag;
    private int sizeTagInx;
    private int areaTagInx;
    private int categoryTagInx;
    //////UI VARS
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

        TextView editBoothHeaderTv = (TextView) findViewById(R.id.edit_booth_header);
        editBoothNumberField = (EditText) findViewById(R.id.edit_booth_number_field);
        editBoothPriceField = (EditText) findViewById(R.id.edit_booth_price_field);
        editBoothSizeField = (EditText) findViewById(R.id.edit_booth_size_field);
        editBoothAreaField = (EditText) findViewById(R.id.edit_booth_area_field);
        editBoothCategoryField = (EditText) findViewById(R.id.edit_booth_category_field);
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

        //////////////////DATA WORK///////////////////////////
        editBoothActivityContext = this;

        Bundle extrasBundle = getIntent().getExtras();
        if (extrasBundle != null) {
            booth = (Item) extrasBundle.get("booth");
            //// TODO: 8/31/2016 string resources
            editBoothHeaderTv.setText(getApplicationContext().getString(R.string.edit_booth_header_string, booth.getName()));
            editBoothNumberField.setText(booth.getSku());
            editBoothPriceField.setText(booth.getPrice().toString());
            ////
            GetBoothDetailsTask getBoothDetailsTask = new GetBoothDetailsTask();
            getBoothDetailsTask.execute();
        } else {
            finish();
        }
    }

    private class GetBoothDetailsTask extends AsyncTask<Void, Void, Void> {
        /////// CLOVER CONNECTION
        private Account merchantAccount;
        private InventoryConnector inventoryConnector;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            merchantAccount = CloverAccount.getAccount(editBoothActivityContext);
            inventoryConnector = new InventoryConnector(editBoothActivityContext, merchantAccount, null);
            inventoryConnector.connect();
            try {
                if (inventoryConnector.getItem(booth.getId()).hasTags()) {
                    boothTags = inventoryConnector.getItem(booth.getId()).getTags();
                } else {
                    boothTags = new ArrayList<>();
                }
            } catch (RemoteException | BindingException | ServiceException | ClientException e1) {
                Log.d("Clover Excptn; ", e1.getClass().getName() + " : " + e1.getMessage());
            } finally {
                inventoryConnector.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            populateExtraData();
        }
    }

    private void populateExtraData() {
        Log.d("Booth ID: ", booth.getId() + "Fetching tags...");
        int tagInx = 0;
        for (Tag currentTag : boothTags) {
            Log.d("currenttagid: ", currentTag.getId() + " currenttagname: " + currentTag.getName());
            String currentTagSubstring1 = currentTag.getName().substring(0, 3);
            String currentTagSubstring2 = currentTag.getName().substring(0, 7);
            Log.d("Substring1: ", currentTagSubstring1);
            Log.d("Substring2: ", currentTagSubstring2);
            if (currentTag.getName().substring(0, 4).equalsIgnoreCase("size")) {
                editBoothSizeField.setText(currentTag.getName());
                sizeTag = currentTag;
                sizeTagInx = tagInx;
                Log.d("Booth ID: ", booth.getId() + "Size tag: " + sizeTag.toString());
            } else if (currentTag.getName().substring(0, 4).equalsIgnoreCase("area")) {
                editBoothAreaField.setText(currentTag.getName());
                areaTag = currentTag;
                areaTagInx = tagInx;
                Log.d("Booth ID: ", booth.getId() + "Area tag: " + areaTag.toString());
            } else if (currentTag.getName().substring(0, 8).equalsIgnoreCase("category")) {
                editBoothCategoryField.setText(currentTag.getName());
                categoryTag = currentTag;
                categoryTagInx = tagInx;
                Log.d("Booth ID: ", booth.getId() + "Category tag: " + categoryTag.toString());
            } else {
                Log.d("Booth ID: ", booth.getId() + " - Tag'" + currentTag.getName() + "' found and skipped.");
            }
        }
        tagInx++;
        Log.d("Booth ID: ", booth.getId() + " - Tag Scan Complete.");
    }

    private class UpdateBoothTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;
        /////// CLOVER CONNECTION
        private Account merchantAccount;
        private InventoryConnector inventoryConnector;
        /////// SELECTED BOOTH DATA
        private Item boothToUpdate;
        private String editBoothNumberFieldData;
        private String editBoothPriceFieldData;
        private long priceLongFormat;
        private String editBoothSizeFieldData;
        private String editBoothAreaFieldData;
        private String editBoothCategoryFieldData;
        /////// LOCAL DATABASE INFO
        private boolean sqliteUpdateBoothSuccess;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(editBoothActivityContext);
            progressDialog.setMessage("Saving Booth...");
            progressDialog.show();

            editBoothNumberFieldData = editBoothNumberField.getText().toString();
            //PRICE HANDLING BELOW
            editBoothPriceFieldData = editBoothPriceField.getText().toString();
            String cleanString = editBoothPriceFieldData.replaceAll("[$,.]", "");
            priceLongFormat = Long.parseLong(cleanString);

            ///'TAG' FIELD HANDLING
            editBoothSizeFieldData = editBoothSizeField.getText().toString();
            editBoothAreaFieldData = editBoothAreaField.getText().toString();
            editBoothCategoryFieldData = editBoothCategoryField.getText().toString();
            sizeTag.setName(GlobalUtils.getFormattedTagName(editBoothSizeFieldData, "Size"));
            areaTag.setName(GlobalUtils.getFormattedTagName(editBoothAreaFieldData, "Area"));
            categoryTag.setName(GlobalUtils.getFormattedTagName(editBoothCategoryFieldData, "Category"));
            boothTags.set(sizeTagInx, sizeTag);
            boothTags.set(areaTagInx, areaTag);
            boothTags.set(categoryTagInx, categoryTag);
            //////
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                merchantAccount = CloverAccount.getAccount(editBoothActivityContext);
                inventoryConnector = new InventoryConnector(editBoothActivityContext, merchantAccount, null);
                inventoryConnector.connect();
                //////////TAG UPDATES
                // TODO: 9/7/2016 following 3 necessary since we already are going to update the list etc
                inventoryConnector.updateTag(sizeTag);
                inventoryConnector.updateTag(areaTag);
                inventoryConnector.updateTag(categoryTag);
                //////////BOOTH UPDATES
                boothToUpdate = inventoryConnector.getItem(booth.getId());
                boothToUpdate.setSku(editBoothNumberFieldData);
                boothToUpdate.setPrice(priceLongFormat);
                boothToUpdate.setTags(boothTags);
                inventoryConnector.updateItem(boothToUpdate);
                //////////LOCAL UPDATES
                TradeShowDB tradeShowDatabase = new TradeShowDB(editBoothActivityContext);
                sqliteUpdateBoothSuccess = tradeShowDatabase.updateSingleBoothByCloverId(boothToUpdate.getId(), boothToUpdate.getName(),
                        boothToUpdate.getSku(), boothToUpdate.getPrice(), editBoothSizeFieldData,
                        editBoothAreaFieldData, editBoothCategoryFieldData);
                if (!sqliteUpdateBoothSuccess)
                    Log.e("Err Edit Booth: ", "Booth w/ ID: " + boothToUpdate.getId());
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
            progressDialog.dismiss();
            finish();
        }
    }

    private class DeleteBoothTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;
        /////// CLOVER CONNECTION
        private Account merchantAccount;
        private InventoryConnector inventoryConnector;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(editBoothActivityContext);
            progressDialog.setMessage("Deleting Booth...");
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                merchantAccount = CloverAccount.getAccount(editBoothActivityContext);
                inventoryConnector = new InventoryConnector(editBoothActivityContext, merchantAccount, null);
                inventoryConnector.connect();
                inventoryConnector.deleteItem(booth.getId());
                TradeShowDB tradeShowDatabase = new TradeShowDB(editBoothActivityContext);
                if (!tradeShowDatabase.deleteSingleBoothByBoothID(booth.getId()))
                    Log.e("Deletion Error: ", "Local Booth Deletion, ID: " + booth.getId());
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
            progressDialog.dismiss();
            finish();
        }
    }

    public void cancelEditBooth(View view) {
        finish();
    }

    public void deleteBoothAction(View view) {
        DeleteBoothTask deleteBoothTask = new DeleteBoothTask();
        deleteBoothTask.execute();
    }

    public void saveBoothChangesAction(View view) {
        UpdateBoothTask updateBoothTask = new UpdateBoothTask();
        updateBoothTask.execute();
    }
}
