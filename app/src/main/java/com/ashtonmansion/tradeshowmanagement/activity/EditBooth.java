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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ashtonmansion.amtradeshowmanagement.R;

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
    private Tag typeTag;
    //////UI VARS
    private EditText editBoothNumberField;
    private EditText editBoothPriceField;
    private EditText editBoothSizeField;
    private EditText editBoothAreaField;
    private EditText editBoothTypeField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        editBoothActivityContext = this;
        //////////////////NAVIGATION AND UI WORK//////////////
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_booth);
        Toolbar toolbar = (Toolbar) findViewById(R.id.edit_booth_toolbar);
        setSupportActionBar(toolbar);

        TextView editBoothHeaderTv = (TextView) findViewById(R.id.edit_booth_header);
        editBoothNumberField = (EditText) findViewById(R.id.edit_booth_number_field);
        editBoothPriceField = (EditText) findViewById(R.id.edit_booth_price_field);
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
        editBoothSizeField = (EditText) findViewById(R.id.edit_booth_size_field);
        editBoothAreaField = (EditText) findViewById(R.id.edit_booth_area_field);
        editBoothTypeField = (EditText) findViewById(R.id.edit_booth_type_field);

        //////////////////DATA WORK///////////////////////////
        Bundle extrasBundle = getIntent().getExtras();
        if (extrasBundle != null) {
            booth = (Item) extrasBundle.get("booth");
            editBoothHeaderTv.setText(getApplicationContext().getString(R.string.edit_booth_header_string, booth.getName()));
            editBoothNumberField.setText(booth.getSku());
            editBoothPriceField.setText(GlobalUtils.getFormattedPriceStringFromLong(booth.getPrice()));
            boothTags = new ArrayList<>();
            /////SHOW TAGS ARE FILTERED IN POPULATE TAG FIELDS METHOD
            boothTags = booth.getTags();
            populateTagFields();
        }

        ///// ATTACH BUTTON LISTENERS
        Button deleteBoothBtn = (Button) findViewById(R.id.edit_booth_delete_booth_btn);
        Button cancelEditBoothBtn = (Button) findViewById(R.id.edit_booth_cancel_btn);
        Button saveBoothChangesBtn = (Button) findViewById(R.id.edit_booth_save_changes_btn);
        Button setBoothAvailableBtn = (Button) findViewById(R.id.set_booth_available_action_btn);
        deleteBoothBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteBoothAction();
            }
        });
        cancelEditBoothBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelEditBooth();
            }
        });
        saveBoothChangesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveBoothChangesAction();
            }
        });
        setBoothAvailableBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setBoothAvailableAction();
            }
        });
    }

    private void populateTagFields() {
        for (Tag currentTag : boothTags) {
            if (currentTag.getName().substring(0, 4).equalsIgnoreCase("size")) {
                editBoothSizeField.setText(GlobalUtils.getUnformattedTagName(currentTag.getName(), "Size"));
                sizeTag = currentTag;
            } else if (currentTag.getName().substring(0, 4).equalsIgnoreCase("area")) {
                editBoothAreaField.setText(GlobalUtils.getUnformattedTagName(currentTag.getName(), "Area"));
                areaTag = currentTag;
            } else if (currentTag.getName().substring(0, 4).equalsIgnoreCase("type")) {
                editBoothTypeField.setText(GlobalUtils.getUnformattedTagName(currentTag.getName(), "Type"));
                typeTag = currentTag;
            }
        }
    }

    public void cancelEditBooth() {
        finish();
    }

    public void deleteBoothAction() {
        DeleteBoothTask deleteBoothTask = new DeleteBoothTask();
        deleteBoothTask.execute();
    }

    public void saveBoothChangesAction() {
        UpdateBoothTask updateBoothTask = new UpdateBoothTask();
        updateBoothTask.execute();
    }

    public void setBoothAvailableAction() {

    }

    private class UpdateBoothTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;
        /////// CLOVER CONNECTION
        private Account merchantAccount;
        private InventoryConnector inventoryConnector;
        /////// SELECTED BOOTH DATA
        private Item boothToUpdate;
        private List<String> boothIdInStringList;
        private String editBoothNumberFieldData;
        private String editBoothPriceFieldData;
        private String editBoothSizeFieldData;
        private String editBoothAreaFieldData;
        private String editBoothTypeFieldData;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(editBoothActivityContext);
            progressDialog.setMessage("Saving Booth...");
            progressDialog.show();

            ////GRAB FIELD DATA UPON UPDATE START
            String boothIdString = booth.getId();
            boothIdInStringList = new ArrayList<>();
            boothIdInStringList.add(boothIdString);
            editBoothNumberFieldData = editBoothNumberField.getText().toString();
            //// TODO: 9/8/2016 price handling here
            editBoothPriceFieldData = editBoothPriceField.getText().toString();
            editBoothSizeFieldData = editBoothSizeField.getText().toString();
            editBoothAreaFieldData = editBoothAreaField.getText().toString();
            editBoothTypeFieldData = editBoothTypeField.getText().toString();
            if (null == sizeTag) {
                sizeTag = new Tag();
            }
            if (null == areaTag) {
                areaTag = new Tag();
            }
            if (null == typeTag) {
                typeTag = new Tag();
            }
            sizeTag.setName(GlobalUtils.getFormattedTagName(editBoothSizeFieldData, "Size"));
            areaTag.setName(GlobalUtils.getFormattedTagName(editBoothAreaFieldData, "Area"));
            typeTag.setName(GlobalUtils.getFormattedTagName(editBoothTypeFieldData, "Type"));
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                merchantAccount = CloverAccount.getAccount(editBoothActivityContext);
                inventoryConnector = new InventoryConnector(editBoothActivityContext, merchantAccount, null);
                inventoryConnector.connect();

                //////////BOOTH UPDATE PROCESS
                boothToUpdate = inventoryConnector.getItem(booth.getId());
                boothToUpdate.setName(getResources().getString(R.string.booth_name_string, editBoothNumberFieldData, editBoothSizeFieldData, editBoothAreaFieldData, editBoothTypeFieldData));
                boothToUpdate.setSku(editBoothNumberFieldData);
                boothToUpdate.setPrice(GlobalUtils.getLongFromFormattedPriceString(editBoothPriceFieldData));
                inventoryConnector.updateItem(boothToUpdate);

                //////////TAG UPDATES
                if (sizeTag.hasId()) {
                    inventoryConnector.updateTag(sizeTag);
                } else {
                    inventoryConnector.createTag(sizeTag);
                    inventoryConnector.assignItemsToTag(sizeTag.getId(), boothIdInStringList);
                }
                if (areaTag.hasId()) {
                    inventoryConnector.updateTag(areaTag);
                } else {
                    inventoryConnector.createTag(areaTag);
                    inventoryConnector.assignItemsToTag(areaTag.getId(), boothIdInStringList);
                }
                if (typeTag.hasId()) {
                    inventoryConnector.updateTag(typeTag);
                } else {
                    inventoryConnector.createTag(typeTag);
                    inventoryConnector.assignItemsToTag(typeTag.getId(), boothIdInStringList);
                }
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
            Toast.makeText(editBoothActivityContext, "Booth Updated!", Toast.LENGTH_LONG).show();
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
}
