package com.ashtonmansion.tsmanagement2.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.ashtonmansion.tsmanagement2.R;
import com.ashtonmansion.tsmanagement2.util.CustomersAdapter;
import com.ashtonmansion.tsmanagement2.util.GlobalUtils;
import com.ashtonmansion.tsmanagement2.util.ParcelableListener;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.Intents;
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

import static android.view.View.GONE;

public class ReserveBoothDetails extends AppCompatActivity {
    private Context reserveBoothDetailsActivityContext;
    private boolean orderIDOriginBoothCode;
    private int activityHeaderResId;
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
    private Customer clickedCustomer;
    ///// ORDER ID BEING PASSED FROM CLOVER for GENERIC SWAP
    private String orderID;
    ////// RECEIVE PASSED REFERENCE TO PARCELABLE LISTENER
    private ParcelableListener parcelableListener;
    private String finalBoothID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserve_booth_details);

        ///// GET DATA PASSED FROM BOOTH SELECTION
        reserveBoothDetailsActivityContext = this;
        handleSizing();

        Bundle extrasBundle = getIntent().getExtras();
        if (extrasBundle != null) {
            orderID = (String) extrasBundle.get("orderid");
            parcelableListener = (ParcelableListener) extrasBundle.get("parcelablelistener");
            Tag show = (Tag) extrasBundle.get("show");
            booth = (Item) extrasBundle.get("booth");
            boothTags = booth.getTags();
            decoupleShowName(show);
            populateTagObjects();
        }
        /////POPULATE BOOTH DATA IN UI
        TextView boothReservationHeader = (TextView) findViewById(R.id.booth_reservation_header);
        TextView boothReservationPriceTV = (TextView) findViewById(R.id.booth_reservation_details_price);
        ///// SET FONTS
        boothReservationHeader.setTextAppearance(reserveBoothDetailsActivityContext, activityHeaderResId);
        boothReservationPriceTV.setTextAppearance(reserveBoothDetailsActivityContext, R.style.large_table_row_font_station);
        ///// POPULATE FIELDS
        boothReservationHeader.setText(getResources().getString(R.string.booth_reservation_details_header_text, showName, booth.getSku()));
        boothReservationPriceTV.setText(GlobalUtils.getFormattedPriceStringFromLong(booth.getPrice()));
        populateTagFields();

        ///// ATTEMPT TO GET A CUSTOMER ASSOCIATED WITH ORDER
        if (null != orderID) {
            orderIDOriginBoothCode = false;
            getOrderCustomer();
        } else if (booth.getCode().contains("Order #")) {
            orderID = GlobalUtils.getOrderIDOnlyFromCode(booth.getCode());
            orderIDOriginBoothCode = true;
            getOrderCustomer();
        } else {
            orderIDOriginBoothCode = false;
            handleAppCase();
        }
    }

    private void handleRegisterCase() {
        if (null != customerAttachedToOrder) {
            populateExistingCustomer();
        } else {
            promptForCustomer();
        }

        Button finalizeOrViewOrderBtn = (Button) findViewById(R.id.finalize_or_order_btn);
        finalizeOrViewOrderBtn.setText(getResources().getString(R.string.reserve_booth_button_text));
        finalizeOrViewOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finalizeBoothReservation();
            }
        });
    }

    private void handleAppCase() {
        TextView modifiedSelectedCustomerTv = (TextView) findViewById(R.id.selected_customer_header_tv);
        if (null != customerAttachedToOrder) {
            modifiedSelectedCustomerTv.setText(getResources().getString(R.string.associated_customer_alternate_string));
            populateExistingCustomer();
        } else findViewById(R.id.selected_customer_table).setVisibility(View.GONE);

        Button viewBoothOrderBtn = (Button) findViewById(R.id.finalize_or_order_btn);
        viewBoothOrderBtn.setText(getResources().getString(R.string.view_booth_order));

        if (booth.getCode().contains("Order #")) {
            viewBoothOrderBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intents.ACTION_START_REGISTER);
                    intent.putExtra(Intents.EXTRA_ORDER_ID, "" + GlobalUtils.getOrderIDOnlyFromCode(booth.getCode()));
                    startActivity(intent);
                }
            });
        } else {
            viewBoothOrderBtn.setEnabled(false);
            ////// ADD WARNING TO PAGE
            TableRow reserveThroughRegisterWarningRow = (TableRow) findViewById(R.id.make_reservation_through_register_warning_row);
            TextView reserveThroughRegisterTv = new TextView(reserveBoothDetailsActivityContext);
            reserveThroughRegisterTv.setText(getResources().getString(R.string.reserve_through_register_warning_msg));
            reserveThroughRegisterTv.setTextAppearance(reserveBoothDetailsActivityContext, R.style.reserve_through_register_style);
            ////// SPAN NONSENSE
            TableRow.LayoutParams params = new TableRow.LayoutParams();
            params.span = 2;
            params.gravity = Gravity.CENTER_HORIZONTAL;
            reserveThroughRegisterWarningRow.addView(reserveThroughRegisterTv, params);
            reserveThroughRegisterWarningRow.setVisibility(View.VISIBLE);
        }
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
                if (orderIDOriginBoothCode)
                    handleAppCase();
                else
                    handleRegisterCase();
            }
        }.execute();
    }

    private void populateExistingCustomer() {
        ///// GET TV FOR SELECTED CUSTOMER AND POPULATE
        TextView selectedCustomerFirstAndLastTv = (TextView) findViewById(R.id.selected_customer_first_and_last);
        TextView selectedCustomerPhoneTv = (TextView) findViewById(R.id.selected_customer_phone_number);
        TextView selectedCustomerEmailTv = (TextView) findViewById(R.id.selected_customer_email_address);
        selectedCustomerFirstAndLastTv.setText(getResources().getString(R.string.selected_customer_first_and_last_text, customerAttachedToOrder.getLastName(), customerAttachedToOrder.getFirstName()));
        if (null != customerAttachedToOrder.getPhoneNumbers() && customerAttachedToOrder.getPhoneNumbers().size() > 0) {
            selectedCustomerPhoneTv.setText(getResources().getString(R.string.selected_customer_phone_number_text, customerAttachedToOrder.getPhoneNumbers().get(0).getPhoneNumber()));
        } else {
            selectedCustomerPhoneTv.setText("N/A");
        }
        if (null != customerAttachedToOrder.getEmailAddresses() && customerAttachedToOrder.getEmailAddresses().size() > 0) {
            selectedCustomerEmailTv.setText(getResources().getString(R.string.selected_customer_email_address_text, customerAttachedToOrder.getEmailAddresses().get(0).getEmailAddress()));
        } else {
            selectedCustomerEmailTv.setText("N/A");
        }
    }

    private void promptForCustomer() {
        ////// BEGIN GATHERING EXISTING CUSTOMERS 1ST
        getExistingCustomerList();

        ////// GET TABLE HANDLERS AND SET NEW CUSTOMER TABLE VISIBLE
        final TableLayout no_customer_warnAndRadio_table = (TableLayout) findViewById(R.id.no_customer_selected_warning);
        final LinearLayout newCustomerEntryTable = (LinearLayout) findViewById(R.id.br_new_customer_table_layout);
        final TableLayout existingCustomerSelectionTable = (TableLayout) findViewById(R.id.existing_customer_selection_table);
        no_customer_warnAndRadio_table.setVisibility(View.VISIBLE);
        TextView noCustomerSelectedWarningTv = (TextView) findViewById(R.id.no_customer_selected_warning_text_view);
        noCustomerSelectedWarningTv.setTextAppearance(reserveBoothDetailsActivityContext, R.style.no_customer_selected_warning_style);
        noCustomerSelectedWarningTv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        newCustomerEntryTable.setVisibility(View.VISIBLE);

        ////// DEACTIVATE FINISH RESERVATION BUTTON AND REMOVE SELECTED CUSTOMER FIELDS
        findViewById(R.id.finalize_or_order_btn).setEnabled(false);
        findViewById(R.id.selected_customer_header_tv).setVisibility(GONE);
        findViewById(R.id.selected_customer_first_and_last).setVisibility(GONE);
        findViewById(R.id.selected_customer_phone_number).setVisibility(GONE);
        findViewById(R.id.selected_customer_email_address).setVisibility(GONE);
        findViewById(R.id.existing_customer_selection_table).setVisibility(GONE);

        ////// ADD LISTENERS
        RadioGroup newOrExistingRadiogrp = (RadioGroup) findViewById(R.id.booth_reservation_new_or_existing_radiogrp);
        Button saveCustomerInformationBtn = (Button) findViewById(R.id.save_customer_to_order_btn);
        newOrExistingRadiogrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int selected) {
                if (selected == R.id.booth_reservation_new_customer_btn) {
                    existingCustomerSelectionTable.setVisibility(GONE);
                    newCustomerEntryTable.setVisibility(View.VISIBLE);
                    findViewById(R.id.finalize_or_order_btn).setEnabled(false);
                    clickedCustomer = null;
                } else if (selected == R.id.booth_reservation_existing_customer_btn) {
                    newCustomerEntryTable.setVisibility(GONE);
                    existingCustomerSelectionTable.setVisibility(View.VISIBLE);
                }
            }
        });
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
                orderConnector.disconnect();
                customerConnector.disconnect();
                orderConnector = null;
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
                findViewById(R.id.br_new_customer_is_marketing_allowed_chkbox).setEnabled(false);
                findViewById(R.id.save_customer_to_order_btn).setEnabled(false);
                ///// ENABLE FINALIZATION OF RESERVATION
                if (orderID != null) {
                    findViewById(R.id.finalize_or_order_btn).setEnabled(true);
                }
                progressDialog.dismiss();
                Toast.makeText(reserveBoothDetailsActivityContext, getResources().getString(R.string.customer_created_message_text), Toast.LENGTH_LONG).show();
            }
        }.execute();
    }

    private void finalizeBoothReservation() {
        injectSelectedBoothAndRemoveGeneric();
        Toast.makeText(reserveBoothDetailsActivityContext, getResources().getString(R.string.booth_reservation_booth_reserved_notification), Toast.LENGTH_LONG).show();
    }

    private void injectSelectedBoothAndRemoveGeneric() {
        new AsyncTask<Void, Void, Void>() {
            private InventoryConnector inventoryConnector;
            private OrderConnector orderConnector;
            private Order boothOrder;

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
                try {
                    boothOrder = orderConnector.getOrder(orderID);
                } catch (Exception e) {
                    Log.d("ecspp", e.getMessage(), e.getCause());
                }
                /////// CREATE THE LISTENER FOR BOOTH CHANGES
                OrderConnector.OnOrderUpdateListener2 secondListener = new OrderConnector.OnOrderUpdateListener2() {
                    private Order fetchedOrder;
                    private List<LineItem> initialLineItems;
                    private String listenerFinalBoothID;

                    @Override
                    public void onOrderCreated(String orderId) {
                        Log.d("Listener created", " order total : " + fetchedOrder.getTotal() + "and added to the connector");

                        initialLineItems = new ArrayList<>();
                        listenerFinalBoothID = finalBoothID;
                        fetchedOrder = boothOrder;
                    }

                    @Override
                    public void onOrderUpdated(String orderId, boolean selfChange) {
                        Log.d("Order updated heard", " orderID: " + orderId + ", selfchange: " + selfChange);

                    }

                    @Override
                    public void onOrderDeleted(String orderId) {
                        Log.d("Order deleted heard", " orderID: " + orderId);
                    }

                    @Override
                    public void onOrderDiscountAdded(String orderId, String discountId) {
                        Log.d("Order dscnt added", " orderID: " + orderId);
                    }

                    @Override
                    public void onOrderDiscountsDeleted(String orderId, List<String> discountIds) {

                    }

                    @Override
                    public void onLineItemsAdded(String orderId, List<String> lineItemIds) {
                        Log.d("LI Added - ", " orderID: " + orderId + " - lineitems: " + lineItemIds.toString());
                    }

                    @Override
                    public void onLineItemsUpdated(String orderId, List<String> lineItemIds) {
                        Log.d("LI Updated - ", " orderID: " + orderId + " - lineitems: " + lineItemIds.toString());
                        Log.d("SWAPPED HERE", " onLineItemsUpdated()");
                    }

                    @Override
                    public void onLineItemsDeleted(String orderId, List<String> lineItemIds) {
                        Log.d("SWAPPED HERE", " onLineItemsDeleted()");

                    }

                    private boolean isLineItemSpecificBooth() {

                        return false;
                    }

                    @Override
                    public void onLineItemModificationsAdded(String orderId, List<String> lineItemIds, List<String> modificationIds) {
                        Log.d("LI ModAdded - ", " orderID: " + orderId + " - lineitems: " + lineItemIds.toString() + " mod ids: " + modificationIds.toString());
                    }

                    @Override
                    public void onLineItemDiscountsAdded(String orderId, List<String> lineItemIds, List<String> discountIds) {

                    }

                    @Override
                    public void onLineItemExchanged(String orderId, String oldLineItemId, String newLineItemId) {
                    }

                    @Override
                    public void onPaymentProcessed(String orderId, String paymentId) {
                    }

                    @Override
                    public void onRefundProcessed(String orderId, String refundId) {
                    }

                    @Override
                    public void onCreditProcessed(String orderId, String creditId) {

                    }
                };

                String genericBoothID = null;
                try {
                    Order utilityOrder = orderConnector.getOrder(orderID);
                    List<LineItem> lineItemList = utilityOrder.getLineItems();
                    List<LineItem> swappedLineItemList = new ArrayList<>();
                    for (LineItem lineItem : lineItemList) {
                        if (lineItem.getName().equalsIgnoreCase("Select Booth"))
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
                                inventoryConnector.updateItem(inventoryConnector.getItem(item.getId()).setCode(getResources().getString(R.string.booth_product_code_with_prefix, orderID)));
                                inventoryConnector.updateItemStock(item.getId(), 0);
                                Order boothsOrder = orderConnector.getOrder(orderID);
                                String orderNotes = boothsOrder.getNote();
                                if (null == orderNotes || orderNotes.trim().equals("")) {
                                    orderConnector.updateOrder(orderConnector.getOrder(orderID).setNote("Booth No.: " + item.getSku()));
                                    Log.d("ReserveBoothDetails", "Empty order notes were to set to booth's associate number: " + item.getSku());
                                } else {
                                    String orderHeader = orderConnector.getOrder(orderID).getNote() + " Booth No.: " + item.getSku();
                                    orderConnector.updateOrder(orderConnector.getOrder(orderID).setNote(orderHeader));
                                    Log.d("ReserveBoothDetails", "Order notes were not empty; booth number appended...");
                                }
                            }
                        }

                        ////// CHECK IF THIS IS AN EXISTING CUSTOMER SELECTED FROM LIST & HANDLE
                        if (null != clickedCustomer) {
                            List<Customer> customerInListForOrder = new ArrayList<>();
                            customerInListForOrder.add(clickedCustomer);
                            orderConnector.updateOrder(orderConnector.getOrder(orderID).setCustomers(customerInListForOrder));
                        }
                    }
                } catch (Exception e) {
                    Log.d("Exception: ", e.getMessage(), e.getCause());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                findViewById(R.id.finalize_or_order_btn).setEnabled(false);
                orderConnector.disconnect();
                inventoryConnector.disconnect();
                orderConnector = null;
                inventoryConnector = null;
                finish();
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
        ////// HANDLE FONTS
        boothReservationSizeTV.setTextAppearance(reserveBoothDetailsActivityContext, R.style.large_table_row_font_station);
        boothReservationAreaTV.setTextAppearance(reserveBoothDetailsActivityContext, R.style.large_table_row_font_station);
        boothReservationTypeTV.setTextAppearance(reserveBoothDetailsActivityContext, R.style.large_table_row_font_station);
        ////// POPULATE DATA
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

    public void getExistingCustomerList() {
        new AsyncTask<Void, Void, Void>() {
            private ProgressDialog progressDialog;
            private CustomerConnector customerConnector;
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
                ///// PREP CLOVER CONNECTIONS FIRST
                customerConnector = new CustomerConnector(reserveBoothDetailsActivityContext, CloverAccount.getAccount(reserveBoothDetailsActivityContext), null);
                customerConnector.connect();
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    existingv1Customers = customerConnector.getCustomers();
                    for (com.clover.sdk.v1.customer.Customer v1Customer : existingv1Customers) {
                        v3CustomerList.add(GlobalUtils.getv3CustomerFromv1Customer(v1Customer, customerConnector.getCustomer(v1Customer.getId()).getPhoneNumbers(), customerConnector.getCustomer(v1Customer.getId()).getEmailAddresses(), customerConnector.getCustomer(v1Customer.getId()).getAddresses()));
                    }
                } catch (RemoteException | BindingException | ServiceException | ClientException e1) {
                    Log.e("Clover Excptn; ", e1.getClass().getName() + " : " + e1.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                customerConnector.disconnect();
                existingCustomers = v3CustomerList;
                populateExistingCustomersListview();
                progressDialog.dismiss();
            }
        }.execute();
    }

    private void populateExistingCustomersListview() {
        ////// SORT THE LIST FOR ACCESSIBILITY
        ////// UTILIZE CUSTOM ADAPTER
        final CustomersAdapter customersAdapter = new CustomersAdapter(reserveBoothDetailsActivityContext, existingCustomers);

        ////// ATTACH ADAPTER TO THE EXISTING CUSTOMER LISTVIEW
        ListView existingCustomersListview = (ListView) findViewById(R.id.existing_customers_lv);
        existingCustomersListview.setAdapter(customersAdapter);
        existingCustomersListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                clickedCustomer = customersAdapter.getItem(position);
                findViewById(R.id.finalize_or_order_btn).setEnabled(true);
                view.setSelected(true);
                // setExistingCustomerInformation(clickedCustomer);
            }
        });

        ///////ATTACH TO SEARCH FIELD AND ADD LISTENER
        // TODO: 9/13/2016 fix this filter
        EditText searchExistingCustomersField = (EditText) findViewById(R.id.booth_reservation_search_existing_customers_field);
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

    private void handleSizing() {
        String platform = GlobalUtils.determinePlatform(getApplicationContext());
        if (platform.equalsIgnoreCase("station"))
            activityHeaderResId = R.style.activity_header_style_station;
        else activityHeaderResId = R.style.activity_header_style_mobile;
    }
}
