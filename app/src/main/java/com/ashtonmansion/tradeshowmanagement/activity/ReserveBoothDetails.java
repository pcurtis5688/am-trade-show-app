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
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

import com.ashtonmansion.amtradeshowmanagement.R;
import com.ashtonmansion.tradeshowmanagement.util.GlobalUtils;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v1.customer.CustomerConnector;
import com.clover.sdk.v1.customer.Customer;
import com.clover.sdk.v3.inventory.Category;
import com.clover.sdk.v3.inventory.Item;
import com.clover.sdk.v3.inventory.Tag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReserveBoothDetails extends AppCompatActivity {
    private Context reserveBoothDetailsActivityContext;
    ///////CLOVER DATA
    private Account merchantAccount;
    private CustomerConnector customerConnector;
    ///////SHOW DATA
    private Category show;
    private String showID;
    private String showName;
    private String showDate;
    private String showLocation;
    private String showNotes;
    ///////BOOTH DATA
    private Item booth;
    private List<Tag> boothTags;
    private Tag sizeTag;
    private Tag areaTag;
    private Tag categoryTag;
    ///////CUSTOMER DATA & UI FIELDS
    private List<Customer> existingCustomers;
    private EditText newCustomerFirstNameField;
    private EditText newCustomerLastNameField;
    private EditText newCustomerPhoneNumberField;
    private EditText newCustomerEmailAddressField;
    private EditText newCustomerAddressLine1Field;
    private EditText newCustomerAddressLine2Field;
    private EditText newCustomerAddressLine3Field;
    private EditText newCustomerCityField;
    private Spinner newCustomerStateSpinner;
    private EditText newCustomerZipCodeField;
    private CheckBox newCustomerIsMarketingAllowedChkbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserve_booth_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /////GET DATA PASSED FROM BOOTH SELECTION
        reserveBoothDetailsActivityContext = this;
        Bundle extrasBundle = getIntent().getExtras();
        if (extrasBundle != null) {
            show = (Category) extrasBundle.get("show");
            booth = (Item) extrasBundle.get("booth");
            boothTags = booth.getTags();
            decoupleShowName(show);
            populateTagObjects();

            /////POPULATE BOOTH DATA IN UI
            TextView boothReservationHeader = (TextView) findViewById(R.id.booth_reservation_header);
            TextView boothReservationPriceTV = (TextView) findViewById(R.id.booth_reservation_details_price);
            boothReservationHeader.setText(getResources().getString(R.string.booth_reservation_details_header_text, showName, booth.getSku()));
            boothReservationPriceTV.setText(GlobalUtils.getFormattedPriceStringFromLong(booth.getPrice()));
            populateTagFields();
        }
        //////////SET UP CUSTOMER-RELATED FIELDS
        GetCustomerListTask getCustomerListTask = new GetCustomerListTask();
        getCustomerListTask.execute();
        setupCustomerFields();
    }

    private void setupCustomerFields() {
        final TableLayout newCustomerTableLayout = (TableLayout) findViewById(R.id.newCustomerTableLayout);
        final TableLayout existingCustomerTableLayout = (TableLayout) findViewById(R.id.existingCustomerTableLayout);

        RadioGroup newOrExistingRadioGrp = (RadioGroup) findViewById(R.id.booth_reservation_new_or_existing_radiogrp);
        newOrExistingRadioGrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if (checkedId == R.id.booth_reservation_new_customer_btn) {
                    existingCustomerTableLayout.setVisibility(View.GONE);
                    newCustomerTableLayout.setVisibility(View.VISIBLE);
                } else if (checkedId == R.id.booth_reservation_existing_customer_btn) {
                    newCustomerTableLayout.setVisibility(View.GONE);
                    existingCustomerTableLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void decoupleShowName(Category show) {
        List<String> splitShowNameArray = Arrays.asList(show.getName().split(","));
        showName = splitShowNameArray.get(0);
        showDate = splitShowNameArray.get(1);
        showLocation = splitShowNameArray.get(2);
        showNotes = splitShowNameArray.get(3);
    }

    private void populateTagObjects() {
        for (Tag currentTag : boothTags) {
            if (currentTag.getName().substring(0, 4).equalsIgnoreCase("size")) {
                sizeTag = currentTag;
            } else if (currentTag.getName().substring(0, 4).equalsIgnoreCase("area")) {
                areaTag = currentTag;
            } else if (currentTag.getName().substring(0, 8).equalsIgnoreCase("category")) {
                categoryTag = currentTag;
            }
        }
    }

    private void populateTagFields() {
        TextView boothReservationSizeTV = (TextView) findViewById(R.id.booth_reservation_details_size);
        TextView boothReservationAreaTV = (TextView) findViewById(R.id.booth_reservation_details_area);
        TextView boothReservationCategoryTV = (TextView) findViewById(R.id.booth_reservation_details_category);

        if (sizeTag != null) {
            String unformattedSizeTagName = GlobalUtils.getUnformattedTagName(sizeTag.getName(), "Size");
            if (unformattedSizeTagName.length() > 0) {
                boothReservationSizeTV.setText(unformattedSizeTagName);
            } else {
                boothReservationSizeTV.setText(getResources().getString(R.string.booth_reservation_no_size_data));
            }
        } else {
            boothReservationSizeTV.setText(getResources().getString(R.string.booth_reservation_no_size_data));
        }
        if (areaTag != null) {
            String unformattedAreaTagName = GlobalUtils.getUnformattedTagName(areaTag.getName(), "Area");
            if (unformattedAreaTagName.length() > 0) {
                boothReservationAreaTV.setText(unformattedAreaTagName);
            } else {
                boothReservationAreaTV.setText(getResources().getString(R.string.booth_reservation_no_area_data));
            }
        } else {
            boothReservationAreaTV.setText(getResources().getString(R.string.booth_reservation_no_area_data));
        }
        if (categoryTag != null) {
            String unformattedCategoryTagName = GlobalUtils.getUnformattedTagName(categoryTag.getName(), "Category");
            if (unformattedCategoryTagName.length() > 0) {
                boothReservationCategoryTV.setText(unformattedCategoryTagName);
            } else {
                boothReservationCategoryTV.setText(getResources().getString(R.string.booth_reservation_no_category_data));
            }
        } else {
            boothReservationCategoryTV.setText(getResources().getString(R.string.booth_reservation_no_category_data));
        }
    }

    private class GetCustomerListTask extends AsyncTask<Void, Void, Void> {
        //////////PRIVATELY NECESSARY OBJECTS & UTILITY LISTS ONLY
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(reserveBoothDetailsActivityContext);
            progressDialog.setMessage(getResources().getString(R.string.booth_reservation_loading_existing_customers_text));
            progressDialog.show();
            existingCustomers = new ArrayList<>();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                //////CONNECT CLOVER ACCT & CUSTOMER CONNECTOR
                merchantAccount = CloverAccount.getAccount(reserveBoothDetailsActivityContext);
                customerConnector = new CustomerConnector(reserveBoothDetailsActivityContext, merchantAccount, null);
                customerConnector.connect();

                existingCustomers = customerConnector.getCustomers();
            } catch (RemoteException | BindingException | ServiceException | ClientException e1) {
                Log.e("Clover Excptn; ", e1.getClass().getName() + " : " + e1.getMessage());
            } finally {
                customerConnector.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            populateExistingCustomersListview();
            progressDialog.dismiss();
        }
    }

    private void populateExistingCustomersListview() {
        ////////ITERATE OVER CUSTOMER LIST, CREATE STRING LIST
        List<String> existingCustomerStrings = new ArrayList<>();
        for (Customer currentCustomer : existingCustomers) {
            String customerString = currentCustomer.getLastName() + ", " + currentCustomer.getFirstName() + " (" + currentCustomer.getId() + ")";
            existingCustomerStrings.add(customerString);
        }

        ////////SET ARRAYADAPTER TO LISTVIEW
        ListView existingCustomersListview = (ListView) findViewById(R.id.existing_customers_lv);
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                existingCustomerStrings);
        existingCustomersListview.setAdapter(arrayAdapter);

        ///////GET SEARCH HANDLER AND ADD LISTENER
        EditText searchExistingCustomersField = (EditText) findViewById(R.id.booth_reservation_search_existing_customers_field);
        searchExistingCustomersField.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                arrayAdapter.getFilter().filter(cs);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });
    }

    public void cancelBoothReservation(View view) {
        finish();
    }

    public void finalizeBoothReservation(View view) {
        RadioButton newCustomerRB = (RadioButton) findViewById(R.id.booth_reservation_new_customer_btn);
        RadioButton existingCustomerRB = (RadioButton) findViewById(R.id.booth_reservation_existing_customer_btn);

        if (newCustomerRB.isChecked()) {
            createNewCustomerFromReserveBoothFields();
        } else if (existingCustomerRB.isChecked()) {
            continueBoothReservation();
        }
    }

    private void createNewCustomerFromReserveBoothFields() {
        newCustomerFirstNameField = (EditText) findViewById(R.id.br_new_customer_first_name_field);
        newCustomerLastNameField = (EditText) findViewById(R.id.br_new_customer_last_name_field);
        newCustomerPhoneNumberField = (EditText) findViewById(R.id.br_new_customer_phone_field);
        newCustomerEmailAddressField = (EditText) findViewById(R.id.br_new_customer_email_field);
        newCustomerAddressLine1Field = (EditText) findViewById(R.id.br_new_customer_address_line_1_field);
        newCustomerAddressLine2Field = (EditText) findViewById(R.id.br_new_customer_address_line_2_field);
        newCustomerAddressLine3Field = (EditText) findViewById(R.id.br_new_customer_address_line_3_field);
        newCustomerCityField = (EditText) findViewById(R.id.br_new_customer_city_field);
        newCustomerStateSpinner = (Spinner) findViewById(R.id.br_new_customer_state_spinner);
        newCustomerZipCodeField = (EditText) findViewById(R.id.br_new_customer_zip_code_field);
        newCustomerIsMarketingAllowedChkbox = (CheckBox) findViewById(R.id.br_new_customer_is_marketing_allowed_chkbox);

        CreateNewCustomerTask createNewCustomerTask = new CreateNewCustomerTask();
        createNewCustomerTask.execute();
    }

    private class CreateNewCustomerTask extends AsyncTask<Void, Void, Void> {
        //////////PRIVATELY NECESSARY OBJECTS & UTILITY LISTS ONLY
        private ProgressDialog progressDialog;
        private String newCustomerID;
        private String newCustomerFirstName;
        private String newCustomerLastName;
        private String newCustomerPhoneNumber;
        private String newCustomerEmailAddress;
        private String newCustomerAddressLine1;
        private String newCustomerAddressLine2;
        private String newCustomerAddressLine3;
        private String newCustomerCity;
        private String newCustomerState;
        private String newCustomerZipCode;
        private boolean newCustomerIsMarketingAllowed;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(reserveBoothDetailsActivityContext);
            progressDialog.setMessage(getResources().getString(R.string.br_new_customer_creating_new_customer_text));
            progressDialog.show();
            newCustomerFirstName = newCustomerFirstNameField.getText().toString();
            newCustomerLastName = newCustomerLastNameField.getText().toString();
            newCustomerPhoneNumber = newCustomerPhoneNumberField.getText().toString();
            newCustomerEmailAddress = newCustomerEmailAddressField.getText().toString();
            newCustomerAddressLine1 = newCustomerAddressLine1Field.getText().toString();
            newCustomerAddressLine2 = newCustomerAddressLine2Field.getText().toString();
            newCustomerAddressLine3 = newCustomerAddressLine3Field.getText().toString();
            newCustomerCity = newCustomerCityField.getText().toString();
            newCustomerState = newCustomerStateSpinner.getSelectedItem().toString();
            newCustomerZipCode = newCustomerZipCodeField.getText().toString();
            newCustomerIsMarketingAllowed = newCustomerIsMarketingAllowedChkbox.isChecked();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                //////CONNECT CLOVER ACCT & CUSTOMER CONNECTOR
                merchantAccount = CloverAccount.getAccount(reserveBoothDetailsActivityContext);
                customerConnector = new CustomerConnector(reserveBoothDetailsActivityContext, merchantAccount, null);
                customerConnector.connect();

                Customer returnedCustomerObject = customerConnector.createCustomer(newCustomerFirstName, newCustomerLastName, newCustomerIsMarketingAllowed);
                newCustomerID = returnedCustomerObject.getId();
                customerConnector.addPhoneNumber(newCustomerID, newCustomerPhoneNumber);
                customerConnector.addEmailAddress(newCustomerID, newCustomerEmailAddress);
                customerConnector.addAddress(newCustomerID, newCustomerAddressLine1, newCustomerAddressLine2, newCustomerAddressLine3, newCustomerCity, newCustomerState, newCustomerZipCode);
            } catch (RemoteException | BindingException | ServiceException | ClientException e1) {
                Log.e("Clover Excptn; ", e1.getClass().getName() + " : " + e1.getMessage());
            } finally {
                customerConnector.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            continueBoothReservation();
            progressDialog.dismiss();
        }
    }

    private void continueBoothReservation() {
        //// TODO: 9/13/2016 finish this after all customer handling
    }
}
