package com.ashtonmansion.tsmanagement2.activity;

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

import com.ashtonmansion.tsmanagement2.R;

import com.ashtonmansion.tsmanagement2.util.GlobalUtils;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v3.inventory.InventoryConnector;
import com.clover.sdk.v3.inventory.Item;
import com.clover.sdk.v3.inventory.Tag;
import com.clover.sdk.v3.order.OrderConnector;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EditBooth extends AppCompatActivity {
    //////ACTIVITY VARS
    private Context editBoothActivityContext;
    private int headerFontResId;
    private int promptFontResId;
    ////// CLOVER VARS
    private Item booth;
    private List<Tag> boothTags;
    private Tag sizeTag;
    private Tag areaTag;
    private Tag typeTag;
    ////// UI VARS
    private EditText editBoothNumberField;
    private EditText editBoothPriceField;
    private EditText editBoothSizeField;
    private EditText editBoothAreaField;
    private EditText editBoothTypeField;
    ////// FLAG FOR SETTING AVAILABLE OPTION
    private String SET_AVAILABLE_ACTION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_booth);
        Toolbar toolbar = (Toolbar) findViewById(R.id.edit_booth_toolbar);
        setSupportActionBar(toolbar);

        //////////////////NAVIGATION AND UI WORK//////////////
        editBoothActivityContext = this;
        handleSizing();
        TextView editBoothHeaderTv = (TextView) findViewById(R.id.edit_booth_header);
        editBoothNumberField = (EditText) findViewById(R.id.edit_booth_no_field);
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
        editBoothSizeField = (EditText) findViewById(R.id.edit_boothsize_field);
        editBoothAreaField = (EditText) findViewById(R.id.edit_booth_area_field);
        editBoothTypeField = (EditText) findViewById(R.id.edit_booth_type_field);
        handlePromptAndFieldStyles();

        //////////////////DATA WORK///////////////////////////
        Bundle extrasBundle = getIntent().getExtras();
        if (extrasBundle != null) {
            booth = (Item) extrasBundle.get("booth");
            editBoothHeaderTv.setText(getApplicationContext().getString(R.string.edit_booth_header_string, booth.getName()));
            editBoothHeaderTv.setTextAppearance(editBoothActivityContext, headerFontResId);
            editBoothNumberField.setText(booth.getSku());
            editBoothPriceField.setText(GlobalUtils.getFormattedPriceStringFromLong(booth.getPrice()));
            boothTags = new ArrayList<>();
            /////SHOW TAGS ARE FILTERED IN POPULATE TAG FIELDS METHOD
            boothTags = booth.getTags();
            populateTagFields();
        }
        ////// GRAB BUTTON HANDLERS
        Button deleteBoothBtn = (Button) findViewById(R.id.edit_booth_delete_booth_btn);
        Button cancelEditBoothBtn = (Button) findViewById(R.id.edit_booth_cancel_btn);
        Button saveBoothChangesBtn = (Button) findViewById(R.id.edit_booth_save_changes_btn);
        final Button setBoothAvailableBtn = (Button) findViewById(R.id.set_booth_available_action_btn);
        ////// ATTACH BUTTON LISTENERS
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
            public void onClick(View v) {
                setBoothAvailableAction();
            }
        });
        ////// STYLE THE BUTTONS
        deleteBoothBtn.setTextAppearance(editBoothActivityContext, R.style.standard_button_style_mobile);
        cancelEditBoothBtn.setTextAppearance(editBoothActivityContext, R.style.standard_button_style_mobile);
        saveBoothChangesBtn.setTextAppearance(editBoothActivityContext, R.style.standard_button_style_mobile);
        setBoothAvailableBtn.setTextAppearance(editBoothActivityContext, R.style.standard_button_style_mobile);
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
        new AsyncTask<Void, Void, Void>() {
            private ProgressDialog progressDialog;
            private InventoryConnector inventoryConnector;
            private OrderConnector orderConnector;
            private String boothIdToMakeAvailable;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog = new ProgressDialog(editBoothActivityContext);
                progressDialog.setMessage(getResources().getString(R.string.edit_booth_updating_booth_msg));
                progressDialog.show();
                ///// CLOVER CONNECTION INIT AND DATA
                boothIdToMakeAvailable = booth.getId();
                inventoryConnector = new InventoryConnector(editBoothActivityContext, CloverAccount.getAccount(editBoothActivityContext), null);
                orderConnector = new OrderConnector(editBoothActivityContext, CloverAccount.getAccount(editBoothActivityContext), null);
                inventoryConnector.connect();
                orderConnector.connect();
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    inventoryConnector.updateItem(inventoryConnector.getItem(boothIdToMakeAvailable).setCode(getResources().getString(R.string.available_keyword)));

//                    if (SET_AVAILABLE_ACTION.equalsIgnoreCase("REFUNDORDER")) {
//                        List<Refund> refunds = new ArrayList<>();
//                        Order orderToRefund = orderConnector.getOrder(booth.getCode());
//                        // TODO: 9/20/2016 ask if this is needed
//                        orderConnector.getOrder(booth.getCode()).setRefunds(refunds);
//                    } else if (SET_AVAILABLE_ACTION.equalsIgnoreCase("SWAPANDREOPEN")) {
//                        ////// FETCH ORDER, SWAP BOOTHS, AND UPDATE THE ORDER
//                        Order orderToRefund = orderConnector.getOrder(booth.getCode());
//                        List<LineItem> lineItems = orderToRefund.getLineItems();
//                        List<LineItem> newLineItems = new ArrayList<>();
//                        for (LineItem lineItem : lineItems) {
//                            if (lineItem.getItem().getId().equalsIgnoreCase(boothIdToMakeAvailable)) {
//
//                                ////// CREATE A NEW LINE ITEM FOR GENERIC BOOTH
//                                LineItem newGenericBoothLineItem = new LineItem();
//                                newGenericBoothLineItem.setName("GENERIC BOOTH");
//
//                                ////// ADD GENERIC
//                                lineItems.add(newGenericBoothLineItem);
//                            } else {
//                                newLineItems.add(lineItem);
//                            }
//                        }
//                        orderToRefund.clearLineItems();
//                        orderConnector.updateOrder(orderConnector.getOrder(booth.getCode()).setLineItems(newLineItems));
//                    } else if (SET_AVAILABLE_ACTION.equalsIgnoreCase("DELETEORDER")) {
//                        Toast.makeText(editBoothActivityContext, booth.getCode(), Toast.LENGTH_LONG).show();
//
//                    }
                } catch (Exception e) {
                    Log.d("Clover excptn:", e.getMessage(), e.getCause());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                inventoryConnector.disconnect();
                orderConnector.disconnect();
                inventoryConnector = null;
                orderConnector = null;
                progressDialog.dismiss();
                finish();

                Toast.makeText(editBoothActivityContext, getResources().getString(R.string.booth_successfully_made_available_msg), Toast.LENGTH_LONG).show();
            }
        }.execute();
    }

    private void handleSizing() {
        String platform = GlobalUtils.determinePlatform(getApplicationContext());
        if (platform.equalsIgnoreCase("station")) {
            headerFontResId = R.style.activity_header_style_station;
            promptFontResId = R.style.prompt_text_font_style_station;
        } else {
            headerFontResId = R.style.activity_header_style_mobile;
            promptFontResId = R.style.prompt_text_font_style_mobile;
        }
    }

    private void handlePromptAndFieldStyles() {
        ////// GET PRIVATE HANDLERS TO PROMPTS
        TextView boothNoPrompt = (TextView) findViewById(R.id.eb_booth_no_prompt);
        TextView boothPricePrompt = (TextView) findViewById(R.id.eb_booth_price_prompt);
        TextView boothSizePrompt = (TextView) findViewById(R.id.eb_booth_size_prompt);
        TextView boothAreaPrompt = (TextView) findViewById(R.id.eb_booth_area_prompt);
        TextView boothTypePrompt = (TextView) findViewById(R.id.eb_booth_type_prompt);
        ////// SET PROMPT APPEARANCE
        boothNoPrompt.setTextAppearance(editBoothActivityContext, promptFontResId);
        boothPricePrompt.setTextAppearance(editBoothActivityContext, promptFontResId);
        boothSizePrompt.setTextAppearance(editBoothActivityContext, promptFontResId);
        boothAreaPrompt.setTextAppearance(editBoothActivityContext, promptFontResId);
        boothTypePrompt.setTextAppearance(editBoothActivityContext, promptFontResId);
        ////// SET TEXT ALIGNMENT POST POP
        boothNoPrompt.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        boothPricePrompt.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        boothSizePrompt.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        boothAreaPrompt.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        boothTypePrompt.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        ////// SET FIELD APPEARANCES
        editBoothNumberField.setTextAppearance(editBoothActivityContext, promptFontResId);
        editBoothPriceField.setTextAppearance(editBoothActivityContext, promptFontResId);
        editBoothSizeField.setTextAppearance(editBoothActivityContext, promptFontResId);
        editBoothAreaField.setTextAppearance(editBoothActivityContext, promptFontResId);
        editBoothTypeField.setTextAppearance(editBoothActivityContext, promptFontResId);
        ////// SET FIELD ALIGNMENT
        editBoothNumberField.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        editBoothPriceField.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        editBoothSizeField.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        editBoothAreaField.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        editBoothTypeField.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
    }

    private class UpdateBoothTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;
        /////// CLOVER CONNECTION
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
            ////// INIT CLOVER CONNECTIONS
            inventoryConnector = new InventoryConnector(editBoothActivityContext, CloverAccount.getAccount(editBoothActivityContext), null);
            inventoryConnector.connect();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
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
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            inventoryConnector.disconnect();
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
            Toast.makeText(editBoothActivityContext, getResources().getString(R.string.booth_deleted_successfully_msg), Toast.LENGTH_LONG).show();
        }
    }
}
