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
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ashtonmansion.amtradeshowmanagement.R;
import com.ashtonmansion.tradeshowmanagement.util.CustomersAdapter;
import com.ashtonmansion.tradeshowmanagement.util.GlobalUtils;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v1.customer.CustomerConnector;
import com.clover.sdk.v3.base.Reference;
import com.clover.sdk.v3.customers.Customer;
import com.clover.sdk.v3.inventory.Category;
import com.clover.sdk.v3.inventory.InventoryConnector;
import com.clover.sdk.v3.inventory.Item;
import com.clover.sdk.v3.inventory.PriceType;
import com.clover.sdk.v3.inventory.Tag;
import com.clover.sdk.v3.order.LineItem;
import com.clover.sdk.v3.order.Order;
import com.clover.sdk.v3.order.OrderConnector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReserveBoothDetails extends AppCompatActivity {
    private Context reserveBoothDetailsActivityContext;
    ///////CLOVER DATA
    private Account merchantAccount;
    private CustomerConnector customerConnector;
    private OrderConnector orderConnector;
    private InventoryConnector inventoryConnector;
    ///////SHOW DATA
    private Category show;
    private String showName;
    ///////BOOTH DATA
    private Item booth;
    private List<Tag> boothTags;
    private Tag sizeTag;
    private Tag areaTag;
    private Tag typeTag;
    /////// CUSTOMER DATA
    private List<Customer> existingCustomers;
    private Customer customerForOrder;

    private void populateExistingCustomersListview() {
        ////////UTILIZE CUSTOM ADAPTER
        final CustomersAdapter customersAdapter = new CustomersAdapter(reserveBoothDetailsActivityContext, existingCustomers);

        ////////ATTACH ADAPTER TO THE EXISTING CUSTOMER LISTVIEW
        ListView existingCustomersListview = (ListView) findViewById(R.id.existing_customers_lv);
        existingCustomersListview.setAdapter(customersAdapter);
        existingCustomersListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Customer clickedCustomer = customersAdapter.getItem(position);
                setExistingCustomerInformation(clickedCustomer);
                view.setSelected(true);
            }
        });
        ///////ATTACH TO SEARCH FIELD AND ADD LISTENER
        EditText searchExistingCustomersField = (EditText) findViewById(R.id.booth_reservation_search_existing_customers_field);
        //// TODO: 9/13/2016 fix this filter
        searchExistingCustomersField.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                customersAdapter.getFilter().filter(cs);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });
    }

    private void setNewCustomerObject(com.clover.sdk.v1.customer.Customer customer) {
        customerForOrder = GlobalUtils.getv3CustomerFromv1Customer(customer, customer.getPhoneNumbers(), customer.getEmailAddresses(), customer.getAddresses());
        continueBoothReservation();
    }

    private void setExistingCustomerInformation(Customer customer) {
        customerForOrder = customer;
    }

    private void continueBoothReservation() {
        CreateNewOrderTask createNewOrderTask = new CreateNewOrderTask();
        createNewOrderTask.execute();
    }

    private class CreateNewOrderTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;
        private Order boothOrder;
        ///BOOTH AND REFERENCE
        private String boothId;
        private Item boothItem;
        private Reference boothReference;
        ///LINE ITEMS
        private LineItem boothLineItem;
        private List<LineItem> orderLineItems;
        ///CUSTOMER
        private Customer orderCustomer;
        private List<Customer> customerInListForOrder;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(reserveBoothDetailsActivityContext);
            progressDialog.setMessage(getResources().getString(R.string.booth_reservation_reserving_booth_pd_text));
            progressDialog.show();
            /////MAKE LOCAL COPIES OF OBJECTS FOR ASYNC TASK
            boothId = booth.getId();
            orderCustomer = customerForOrder;
            /////MAKE CUSTOMER LIST
            customerInListForOrder = new ArrayList<>();
            customerInListForOrder.add(orderCustomer);
            /////MAKE REFERENCE TO THE RESERVED BOOTH AND ADD TO LINE ITEM LIST
            boothReference = new Reference();
            boothReference.setId(boothId);
            boothLineItem = new LineItem();
            boothLineItem.setItem(boothReference);
            orderLineItems = new ArrayList<>();
            orderLineItems.add(boothLineItem);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                //////CONNECT ORDER AND INVENTORY CONNECTIONS
                merchantAccount = CloverAccount.getAccount(reserveBoothDetailsActivityContext);
                orderConnector = new OrderConnector(reserveBoothDetailsActivityContext, merchantAccount, null);
                inventoryConnector = new InventoryConnector(reserveBoothDetailsActivityContext, merchantAccount, null);
                orderConnector.connect();
                inventoryConnector.connect();

                ///// CREATE A NEW ORDER AND FETCH ID
                boothOrder = orderConnector.createOrder(new Order());
                String newOrderID = boothOrder.getId();
                ///////////////////////
                ///// GET BOOTH FROM INVENTORY CONNECTOR
                boothItem = inventoryConnector.getItem(boothId);
                if (boothItem.getPriceType() == PriceType.FIXED) {
                    orderConnector.addFixedPriceLineItem(newOrderID, boothId, null, null);
                }

                //// TODO: 9/14/2016 other price types
                boothOrder.setCustomers(customerInListForOrder);
                orderConnector.updateOrder(boothOrder);
            } catch (RemoteException | BindingException | ServiceException | ClientException e1) {
                Log.e("Clover Excptn; ", e1.getClass().getName() + " : " + e1.getMessage());
            } finally {
                orderConnector.disconnect();
                inventoryConnector.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            closeOutBoothReservationActivity();
        }
    }

    /////////////////////////ignore below for now

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

    private class GetCustomerListTask extends AsyncTask<Void, Void, Void> {
        //////////PRIVATELY NECESSARY OBJECTS & UTILITY LISTS ONLY
        private ProgressDialog progressDialog;
        private List<com.clover.sdk.v1.customer.Customer> existingv1Customers;
        private List<Customer> v3CustomerList;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(reserveBoothDetailsActivityContext);
            progressDialog.setMessage(getResources().getString(R.string.booth_reservation_loading_existing_customers_text));
            progressDialog.show();
            existingCustomers = new ArrayList<>();
            existingv1Customers = new ArrayList<>();
            v3CustomerList = new ArrayList<>();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                //////CONNECT CLOVER ACCT & CUSTOMER CONNECTOR
                merchantAccount = CloverAccount.getAccount(reserveBoothDetailsActivityContext);
                customerConnector = new CustomerConnector(reserveBoothDetailsActivityContext, merchantAccount, null);
                customerConnector.connect();

                existingv1Customers = customerConnector.getCustomers();

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
            for (com.clover.sdk.v1.customer.Customer v1Customer : existingv1Customers) {
                v3CustomerList.add(GlobalUtils.getv3CustomerFromv1Customer(v1Customer, v1Customer.getPhoneNumbers(), v1Customer.getEmailAddresses(), v1Customer.getAddresses()));
            }
            existingCustomers = v3CustomerList;
            populateExistingCustomersListview();
            progressDialog.dismiss();
        }
    }

    public void cancelBoothReservation(View view) {
        finish();
    }

    public void finalizeBoothReservation(View view) {
        RadioButton newCustomerRB = (RadioButton) findViewById(R.id.booth_reservation_new_customer_btn);
        RadioButton existingCustomerRB = (RadioButton) findViewById(R.id.booth_reservation_existing_customer_btn);

        if (newCustomerRB.isChecked()) {
            //IF NEW CUSTOMER, CREATE (POST-EXE HANDLES CONTINUATION)
            CreateNewCustomerTask createNewCustomerTask = new CreateNewCustomerTask();
            createNewCustomerTask.execute();
        } else if (existingCustomerRB.isChecked()) {
            //IF EXISTING CUSTOMER, ENSURE ONE HAS BEEN SELECTED
            if (customerForOrder != null && customerForOrder.hasId()) {
                continueBoothReservation();
            } else {
                Toast selectACustomerWarning = Toast.makeText(reserveBoothDetailsActivityContext, getResources().getString(R.string.booth_reservation_select_customer_warning), Toast.LENGTH_SHORT);
                selectACustomerWarning.show();
            }
        }
    }

    private class CreateNewCustomerTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;
        private com.clover.sdk.v1.customer.Customer v1CustomerCreated;
        private Customer newV3Customer;
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
            EditText newCustomerFirstNameField = (EditText) findViewById(R.id.br_new_customer_first_name_field);
            EditText newCustomerLastNameField = (EditText) findViewById(R.id.br_new_customer_last_name_field);
            EditText newCustomerPhoneNumberField = (EditText) findViewById(R.id.br_new_customer_phone_field);
            EditText newCustomerEmailAddressField = (EditText) findViewById(R.id.br_new_customer_email_field);
            EditText newCustomerAddressLine1Field = (EditText) findViewById(R.id.br_new_customer_address_line_1_field);
            EditText newCustomerAddressLine2Field = (EditText) findViewById(R.id.br_new_customer_address_line_2_field);
            EditText newCustomerAddressLine3Field = (EditText) findViewById(R.id.br_new_customer_address_line_3_field);
            EditText newCustomerCityField = (EditText) findViewById(R.id.br_new_customer_city_field);
            Spinner newCustomerStateSpinner = (Spinner) findViewById(R.id.br_new_customer_state_spinner);
            EditText newCustomerZipCodeField = (EditText) findViewById(R.id.br_new_customer_zip_code_field);
            CheckBox newCustomerIsMarketingAllowedChkbox = (CheckBox) findViewById(R.id.br_new_customer_is_marketing_allowed_chkbox);
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
                v1CustomerCreated = customerConnector.createCustomer(newCustomerFirstName, newCustomerLastName, newCustomerIsMarketingAllowed);
                newCustomerID = v1CustomerCreated.getId();
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
            setNewCustomerObject(v1CustomerCreated);
            progressDialog.dismiss();
        }
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
        String showDate = splitShowNameArray.get(1);
        String showLocation = splitShowNameArray.get(2);
        String showNotes = splitShowNameArray.get(3);
    }

    private void populateTagObjects() {
        for (Tag currentTag : boothTags) {
            if (currentTag.getName().substring(0, 4).equalsIgnoreCase("size")) {
                sizeTag = currentTag;
            } else if (currentTag.getName().substring(0, 4).equalsIgnoreCase("area")) {
                areaTag = currentTag;
            } else if (currentTag.getName().substring(0, 4).equalsIgnoreCase("type")) {
                typeTag = currentTag;
            }
        }
    }

    private void populateTagFields() {
        TextView boothReservationSizeTV = (TextView) findViewById(R.id.booth_reservation_details_size);
        TextView boothReservationAreaTV = (TextView) findViewById(R.id.booth_reservation_details_area);
        TextView boothReservationTypeTV = (TextView) findViewById(R.id.booth_reservation_details_type);

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
        if (typeTag != null) {
            String unformattedTypeTagName = GlobalUtils.getUnformattedTagName(typeTag.getName(), "Type");
            if (unformattedTypeTagName.length() > 0) {
                boothReservationTypeTV.setText(unformattedTypeTagName);
            } else {
                boothReservationTypeTV.setText(getResources().getString(R.string.booth_reservation_no_type_data));
            }
        } else {
            boothReservationTypeTV.setText(getResources().getString(R.string.booth_reservation_no_type_data));
        }
    }

    private void closeOutBoothReservationActivity() {
        finish();
    }

}
