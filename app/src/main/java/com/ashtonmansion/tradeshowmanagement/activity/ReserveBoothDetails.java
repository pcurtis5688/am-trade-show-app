package com.ashtonmansion.tradeshowmanagement.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ashtonmansion.amtradeshowmanagement.R;
import com.ashtonmansion.tradeshowmanagement.util.GlobalUtils;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v1.customer.CustomerConnector;
import com.clover.sdk.v3.customers.Customer;
import com.clover.sdk.v3.inventory.InventoryConnector;
import com.clover.sdk.v3.inventory.Item;
import com.clover.sdk.v3.inventory.Tag;
import com.clover.sdk.v3.order.LineItem;
import com.clover.sdk.v3.order.Order;
import com.clover.sdk.v3.order.OrderConnector;

import java.util.ArrayList;
import java.util.List;

public class ReserveBoothDetails extends AppCompatActivity {
    private Context reserveBoothDetailsActivityContext;
    ///////CLOVER DATA
    private OrderConnector orderConnector;
    private InventoryConnector inventoryConnector;
    ///////SHOW DATA
    private String showName;
    ///// BOOTH DATA
    private Item booth;
    private List<Tag> boothTags;
    private Tag sizeTag;
    private Tag areaTag;
    private Tag typeTag;
    ///// CUSTOMER DATA
    private List<Customer> existingCustomers;
    private Customer customerAttachedToOrder;
    ///// ORDER ID BEING PASSED FROM CLOVER for GENERIC SWAP
    private String orderID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserve_booth_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ///// GET DATA PASSED FROM BOOTH SELECTION
        reserveBoothDetailsActivityContext = this;
        Bundle extrasBundle = getIntent().getExtras();
        if (extrasBundle != null) {
            orderID = (String) extrasBundle.get("orderid");
            Tag show = (Tag) extrasBundle.get("show");
            booth = (Item) extrasBundle.get("booth");
            boothTags = booth.getTags();
            decoupleShowName(show);
            populateTagObjects();

            ///// ATTEMPT TO GET CUSTOMER SELECTED OR HANDLE
            getOrderCustomer();
        }
        /////POPULATE BOOTH DATA IN UI
        TextView boothReservationHeader = (TextView) findViewById(R.id.booth_reservation_header);
        TextView boothReservationPriceTV = (TextView) findViewById(R.id.booth_reservation_details_price);
        ///// SET FONTS
        boothReservationHeader.setTextAppearance(reserveBoothDetailsActivityContext, R.style.large_table_row_font_station);
        boothReservationPriceTV.setTextAppearance(reserveBoothDetailsActivityContext, R.style.large_table_row_font_station);
        ///// POPULATE FIELDS
        boothReservationHeader.setText(getResources().getString(R.string.booth_reservation_details_header_text, showName, booth.getSku()));
        boothReservationPriceTV.setText(GlobalUtils.getFormattedPriceStringFromLong(booth.getPrice()));
        populateTagFields();

        ///// SET UP CANCEL AND FINALIZE BUTTONS
        Button cancelBoothReservationBtn = (Button) findViewById(R.id.cancel_reserve_booth_btn);
        Button finalizeBoothReservationBtn = (Button) findViewById(R.id.finalize_booth_reservation_btn);
        cancelBoothReservationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelBoothReservation();
            }
        });
        finalizeBoothReservationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finalizeBoothReservation();
            }
        });
    }

    private void getOrderCustomer() {
        new AsyncTask<Void, Void, Void>() {
            private OrderConnector orderConnector;
            private Customer tempCustomer;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                orderConnector = new OrderConnector(reserveBoothDetailsActivityContext, CloverAccount.getAccount(reserveBoothDetailsActivityContext), null);
                orderConnector.connect();
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    List<Customer> customerList = orderConnector.getOrder(orderID).getCustomers();
                    if (null != customerList.get(0)) {
                        tempCustomer = customerList.get(0);
                    }
                } catch (Exception e) {
                    Log.d("Exception: ", e.getMessage(), e.getCause());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                customerAttachedToOrder = tempCustomer;
                orderConnector.disconnect();
                orderConnector = null;
                if (null != customerAttachedToOrder) populateExistingCustomer();
                else promptForCustomer();
            }
        }.execute();
    }

    private void populateExistingCustomer() {
        ///// GET TV FOR SELECTED CUSTOMER AND POPULATE
        TextView selectedCustomerFirstAndLastTv = (TextView) findViewById(R.id.selected_customer_first_and_last);
        TextView selectedCustomerPhoneTv = (TextView) findViewById(R.id.selected_customer_phone_number);
        TextView selectedCustomerEmailTv = (TextView) findViewById(R.id.selected_customer_email_address);
        selectedCustomerFirstAndLastTv.setText(getResources().getString(R.string.selected_customer_first_and_last_text, customerAttachedToOrder.getLastName(), customerAttachedToOrder.getFirstName()));
        selectedCustomerPhoneTv.setText(getResources().getString(R.string.selected_customer_phone_number_text, customerAttachedToOrder.getPhoneNumbers().get(0).getPhoneNumber()));
        selectedCustomerEmailTv.setText(getResources().getString(R.string.selected_customer_email_address_text, customerAttachedToOrder.getEmailAddresses().get(0).getEmailAddress()));
    }

    private void promptForCustomer() {
        findViewById(R.id.finalize_booth_reservation_btn).setEnabled(false);
        findViewById(R.id.selected_customer_header_tv).setVisibility(View.GONE);
        findViewById(R.id.selected_customer_first_and_last).setVisibility(View.GONE);
        findViewById(R.id.selected_customer_phone_number).setVisibility(View.GONE);
        findViewById(R.id.selected_customer_email_address).setVisibility(View.GONE);
        TableLayout newCustomerEntryTable = (TableLayout) findViewById(R.id.br_new_customer_table_layout);
        newCustomerEntryTable.setVisibility(View.VISIBLE);
        Button saveCustomerInformationBtn = (Button) findViewById(R.id.save_customer_to_order_btn);
        saveCustomerInformationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveNewCustomerInfo();
            }
        });
    }

    private void saveNewCustomerInfo() {
        new AsyncTask<Void, Void, Void>() {
            private ProgressDialog progressDialog;
            private CustomerConnector customerConnector;
            private OrderConnector orderConnector;
            private com.clover.sdk.v1.customer.Customer v1CustomerCreated;
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
                ///// PREPARE CLOVER CONNECTIONS 1ST
                customerConnector = new CustomerConnector(reserveBoothDetailsActivityContext, CloverAccount.getAccount(reserveBoothDetailsActivityContext), null);
                orderConnector = new OrderConnector(reserveBoothDetailsActivityContext, CloverAccount.getAccount(reserveBoothDetailsActivityContext), null);
                customerConnector.connect();
                orderConnector.connect();
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    v1CustomerCreated = customerConnector.createCustomer(newCustomerFirstName, newCustomerLastName, newCustomerIsMarketingAllowed);
                    Customer v3Customer = GlobalUtils.getv3CustomerFromv1Customer(v1CustomerCreated, v1CustomerCreated.getPhoneNumbers(), v1CustomerCreated.getEmailAddresses(), v1CustomerCreated.getAddresses());
                    newCustomerID = v1CustomerCreated.getId();
                    customerConnector.addPhoneNumber(newCustomerID, newCustomerPhoneNumber);
                    customerConnector.addEmailAddress(newCustomerID, newCustomerEmailAddress);
                    customerConnector.addAddress(newCustomerID, newCustomerAddressLine1, newCustomerAddressLine2, newCustomerAddressLine3, newCustomerCity, newCustomerState, newCustomerZipCode);
                    List<Customer> customerInListForOrder = new ArrayList<Customer>();
                    customerInListForOrder.add(v3Customer);
                    orderConnector.updateOrder(orderConnector.getOrder(orderID).setCustomers(customerInListForOrder));
                } catch (RemoteException
                        | BindingException
                        | ServiceException
                        | ClientException e1) {
                    Log.e("Clover Excptn; ", e1.getClass().getName() + " : " + e1.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                customerConnector.disconnect();
                customerConnector = null;
                ///// DISABLE ALL EDITABLE CUSTOMER FIELDS
                findViewById(R.id.br_new_customer_first_name_field).setEnabled(false);
                findViewById(R.id.br_new_customer_last_name_field).setEnabled(false);
                findViewById(R.id.br_new_customer_phone_field).setEnabled(false);
                findViewById(R.id.br_new_customer_email_field).setEnabled(false);
                findViewById(R.id.br_new_customer_address_line_1_field).setEnabled(false);
                findViewById(R.id.br_new_customer_address_line_2_field).setEnabled(false);
                findViewById(R.id.br_new_customer_address_line_3_field).setEnabled(false);
                findViewById(R.id.br_new_customer_city_field).setEnabled(false);
                findViewById(R.id.br_new_customer_state_spinner).setEnabled(false);
                findViewById(R.id.br_new_customer_zip_code_field).setEnabled(false);
                findViewById(R.id.save_customer_to_order_btn).setEnabled(false);
                ///// ENABLE FINALIZATION OF RESERVATION
                findViewById(R.id.finalize_booth_reservation_btn).setEnabled(true);
                progressDialog.dismiss();
                Toast.makeText(reserveBoothDetailsActivityContext, getResources().getString(R.string.customer_created_message_text), Toast.LENGTH_LONG).show();
            }
        }.execute();
    }

    private void cancelBoothReservation() {
        finish();
    }

    private void finalizeBoothReservation() {
        swapGenericForSelectedTask();
    }

    private void swapGenericForSelectedTask() {
        new AsyncTask<Void, Void, Void>() {
            private InventoryConnector inventoryConnector;
            private OrderConnector orderConnector;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                inventoryConnector = new InventoryConnector(reserveBoothDetailsActivityContext, CloverAccount.getAccount(reserveBoothDetailsActivityContext), null);
                inventoryConnector.connect();
                orderConnector = new OrderConnector(reserveBoothDetailsActivityContext, CloverAccount.getAccount(reserveBoothDetailsActivityContext), null);
                orderConnector.connect();
            }

            @Override
            protected Void doInBackground(Void... params) {
                String genericBoothID = null;
                try {
                    Order utilityOrder = orderConnector.getOrder(orderID);
                    List<LineItem> lineItemList = utilityOrder.getLineItems();
                    List<LineItem> swappedLineItemList = new ArrayList<>();
                    for (LineItem lineItem : lineItemList) {
                        if (lineItem.getName().contains("Booth L")
                                || lineItem.getName().contains("Booth M")
                                || lineItem.getName().contains("Booth S"))
                            genericBoothID = lineItem.getId();
                        else swappedLineItemList.add(lineItem);
                    }
                    if (null != genericBoothID) {
                        List<String> itemsToRemoveIncludingGenericBooth = new ArrayList<>();
                        itemsToRemoveIncludingGenericBooth.add(genericBoothID);
                        orderConnector.deleteLineItems(orderID, itemsToRemoveIncludingGenericBooth);

                        for (Item item : inventoryConnector.getItems()) {
                            if (item.getId().equalsIgnoreCase(booth.getId())) {
                                orderConnector.addFixedPriceLineItem(orderID, item.getId(), null, null);
                                inventoryConnector.updateItem(inventoryConnector.getItem(item.getId()).setCode(orderID));
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.d("Exception: ", e.getMessage(), e.getCause());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                orderConnector.disconnect();
                inventoryConnector.disconnect();
                orderConnector = null;
                inventoryConnector = null;
            }
        }.execute();
    }

    private void decoupleShowName(Tag show) {
        List<String> splitShowNameArray = GlobalUtils.decoupleShowName(show.getName());
        showName = splitShowNameArray.get(0);
    }

    private void populateTagObjects() {
        for (Tag currentTag : boothTags) {
            if (!currentTag.getName().contains(" [Show]")) {
                if (currentTag.getName().substring(0, 4).equalsIgnoreCase("size")) {
                    sizeTag = currentTag;
                } else if (currentTag.getName().substring(0, 4).equalsIgnoreCase("area")) {
                    areaTag = currentTag;
                } else if (currentTag.getName().substring(0, 4).equalsIgnoreCase("type")) {
                    typeTag = currentTag;
                }
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
        // TODO: 9/19/2016 figure out how to toast/close
    }

    /////////////////////////ignore below for now
    //    private class CreateNewOrderTask extends AsyncTask<Void, Void, Void> {
    //        private ProgressDialog progressDialog;
    //        private Order boothOrder;
    //        ///BOOTH AND REFERENCE
    //        private Item boothItem;
    //        private Reference boothReference;
    //        ///LINE ITEMS
    //        private LineItem boothLineItem;
    //        private List<LineItem> orderLineItems;
    //        ///CUSTOMER
    //        private Customer orderCustomer;
    //        private List<Customer> customerInListForOrder;
    //
    //        @Override
    //        protected void onPreExecute() {
    //            super.onPreExecute();
    //            progressDialog = new ProgressDialog(reserveBoothDetailsActivityContext);
    //            progressDialog.setMessage(getResources().getString(R.string.booth_reservation_reserving_booth_pd_text));
    //            progressDialog.show();
    //            /////MAKE LOCAL COPIES OF OBJECTS FOR ASYNC TASK
    //            orderCustomer = customerForOrder;
    //            /////MAKE CUSTOMER LIST
    //            customerInListForOrder = new ArrayList<>();
    //            customerInListForOrder.add(orderCustomer);
    //            /////MAKE REFERENCE TO THE RESERVED BOOTH AND ADD TO LINE ITEM LIST
    //            boothReference = new Reference();
    //            boothReference.setId(booth.getId());
    //            boothLineItem = new LineItem();
    //            boothLineItem.setItem(boothReference);
    //            orderLineItems = new ArrayList<>();
    //            orderLineItems.add(boothLineItem);
    //            ///// SET UP CLOVER CONNECTIONS
    //            orderConnector = new OrderConnector(reserveBoothDetailsActivityContext, CloverAccount.getAccount(reserveBoothDetailsActivityContext), null);
    //            inventoryConnector = new InventoryConnector(reserveBoothDetailsActivityContext, CloverAccount.getAccount(reserveBoothDetailsActivityContext), null);
    //            orderConnector.connect();
    //            inventoryConnector.connect();
    //        }
    //
    //        @Override
    //        protected Void doInBackground(Void... params) {
    //            try {
    //                ///// CREATE A NEW ORDER AND FETCH ID
    //                boothOrder = orderConnector.createOrder(new Order());
    //                boothOrder.setCustomers(customerInListForOrder);
    //                orderConnector.addFixedPriceLineItem(boothOrder.getId(), booth.getId(), null, null);
    //                orderConnector.updateOrder(boothOrder);
    //
    //                ///// GET BOOTH FROM INVENTORY CONNECTOR AND SET TO RESERVED
    //                boothItem = inventoryConnector.getItem(booth.getId());
    //                boothItem.setCode("RESERVED");
    //                inventoryConnector.updateItem(boothItem);
    //            } catch (RemoteException | BindingException | ServiceException | ClientException e1) {
    //                Log.e("Clover Excptn; ", e1.getClass().getName() + " : " + e1.getMessage());
    //            } finally {
    //                orderConnector.disconnect();
    //                inventoryConnector.disconnect();
    //            }
    //            return null;
    //        }
    //
    //        @Override
    //        protected void onPostExecute(Void result) {
    //            super.onPostExecute(result);
    //            progressDialog.dismiss();
    //            Toast.makeText(reserveBoothDetailsActivityContext, getResources().getString(R.string.booth_reservation_booth_reserved_notification), Toast.LENGTH_LONG).show();
    //            closeOutBoothReservationActivity();
    //        }
    //    }

    //    private class GetCustomerListTask extends AsyncTask<Void, Void, Void> {
    //        //////////PRIVATELY NECESSARY OBJECTS & UTILITY LISTS ONLY
    //        private ProgressDialog progressDialog;
    //        private List<com.clover.sdk.v1.customer.Customer> existingv1Customers;
    //        private List<Customer> v3CustomerList;
    //
    //        @Override
    //        protected void onPreExecute() {
    //            super.onPreExecute();
    //            progressDialog = new ProgressDialog(reserveBoothDetailsActivityContext);
    //            progressDialog.setMessage(getResources().getString(R.string.booth_reservation_loading_existing_customers_text));
    //            progressDialog.show();
    //            existingCustomers = new ArrayList<>();
    //            existingv1Customers = new ArrayList<>();
    //            v3CustomerList = new ArrayList<>();
    //            ///// PREP CLOVER CONNECTIONS FIRST
    //            customerConnector = new CustomerConnector(reserveBoothDetailsActivityContext, CloverAccount.getAccount(reserveBoothDetailsActivityContext), null);
    //            customerConnector.connect();
    //        }
    //
    //        @Override
    //        protected Void doInBackground(Void... params) {
    //            try {
    //                existingv1Customers = customerConnector.getCustomers();
    //            } catch (RemoteException | BindingException | ServiceException | ClientException e1) {
    //                Log.e("Clover Excptn; ", e1.getClass().getName() + " : " + e1.getMessage());
    //            }
    //            return null;
    //        }
    //
    //        @Override
    //        protected void onPostExecute(Void result) {
    //            super.onPostExecute(result);
    //            customerConnector.disconnect();
    //            for (com.clover.sdk.v1.customer.Customer v1Customer : existingv1Customers) {
    //                v3CustomerList.add(GlobalUtils.getv3CustomerFromv1Customer(v1Customer, v1Customer.getPhoneNumbers(), v1Customer.getEmailAddresses(), v1Customer.getAddresses()));
    //            }
    //            existingCustomers = v3CustomerList;
    //            progressDialog.dismiss();
    //        }
    //    }

    //private void populateExistingCustomersListview() {
    ////////UTILIZE CUSTOM ADAPTER
    //        final CustomersAdapter customersAdapter = new CustomersAdapter(reserveBoothDetailsActivityContext, existingCustomers);
    //
    //        ////////ATTACH ADAPTER TO THE EXISTING CUSTOMER LISTVIEW
    //        ListView existingCustomersListview = (ListView) findViewById(R.id.existing_customers_lv);
    //        existingCustomersListview.setAdapter(customersAdapter);
    //        existingCustomersListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
    //            @Override
    //            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
    //                Customer clickedCustomer = customersAdapter.getItem(position);
    //                setExistingCustomerInformation(clickedCustomer);
    //                view.setSelected(true);
    //            }
    //        });
    //        ///////ATTACH TO SEARCH FIELD AND ADD LISTENER
    //        // TODO: 9/13/2016 fix this filter
    //        EditText searchExistingCustomersField = (EditText) findViewById(R.id.booth_reservation_search_existing_customers_field);
    //        searchExistingCustomersField.addTextChangedListener(new TextWatcher() {
    //
    //            @Override
    //            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
    //                // When user changed the Text
    //                customersAdapter.getFilter().filter(cs);
    //            }
    //
    //            @Override
    //            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
    //            }
    //
    //            @Override
    //            public void afterTextChanged(Editable arg0) {
    //            }
    //        });
    //}
}
