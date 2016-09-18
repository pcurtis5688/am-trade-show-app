package com.ashtonmansion.tradeshowmanagement.activity;

import android.accounts.Account;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.ashtonmansion.amtradeshowmanagement.R;
import com.ashtonmansion.tradeshowmanagement.util.GlobalUtils;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.Intents;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v3.inventory.InventoryConnector;
import com.clover.sdk.v3.inventory.Item;
import com.clover.sdk.v3.inventory.Tag;
import com.clover.sdk.v3.order.LineItem;
import com.clover.sdk.v3.order.Order;
import com.clover.sdk.v3.order.OrderConnector;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class SelectBooth extends Activity {
    private Context selectBoothActivityContext;
    ///// DATA PASSED FROM CLOVER
    private String orderID;
    ///// NECESSARY GLOBAL DATA
    private List<Item> boothList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_booth);
        setResult(RESULT_CANCELED);
        // REMEMBER THERE ARE SOME FURTHER EXAMPLES OF CODE I REMOVED
        selectBoothActivityContext = this;
        Intent intent = getIntent();
        Bundle extrasBundle = intent.getExtras();

        orderID = getIntent().getStringExtra(Intents.EXTRA_ORDER_ID);
        selectShowActivity();
    }

    public void selectShowActivity() {

        Intent reserveBoothShowSelectionActivityIntent = new Intent(selectBoothActivityContext, BoothReservationShowSelection.class);
        reserveBoothShowSelectionActivityIntent.putExtra("orderID", orderID);
        startActivity(reserveBoothShowSelectionActivityIntent);
//        Button approveButton = (Button) findViewById(R.id.select_booth_accept_button);
//        approveButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent data = new Intent();
//                // data.putExtra(Intents.EXTRA_AMOUNT, amount);
//                //    data.putExtra(Intents.EXTRA_CLIENT_ID, Utils.nextRandomId());
//                //    data.putExtra(Intents.EXTRA_NOTE, "Transaction Id: " + Utils.nextRandomId());
//                Toast.makeText(selectBoothActivityContext, "Working...", Toast.LENGTH_LONG).show();
//
//                setResult(RESULT_OK, data);
//                finish();
//            }
//        });
//
//        Button declineButton = (Button) findViewById(R.id.select_booth_decline_button);
//        declineButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent data = new Intent();
//                data.putExtra(Intents.EXTRA_DECLINE_REASON, "You pressed the decline button");
//                Toast.makeText(selectBoothActivityContext, "Working...", Toast.LENGTH_LONG).show();
//                setResult(RESULT_CANCELED, data);
//                finish();
//            }
//        });
//
//        Button swapGenericForSelectedBtn = (Button) findViewById(R.id.swap_generic_for_reserved_btn);
//        swapGenericForSelectedBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                swapGenericForSelected();
//            }
//        });
    }
//
//    private void swapGenericForSelected() {
//        new AsyncTask<Void, Void, Void>() {
//            private InventoryConnector inventoryConnector;
//            private OrderConnector orderConnector;
//
//            @Override
//            protected void onPreExecute() {
//                super.onPreExecute();
//                inventoryConnector = new InventoryConnector(selectBoothActivityContext, CloverAccount.getAccount(selectBoothActivityContext), null);
//                inventoryConnector.connect();
//                orderConnector = new OrderConnector(selectBoothActivityContext, CloverAccount.getAccount(selectBoothActivityContext), null);
//                orderConnector.connect();
//            }
//
//            @Override
//            protected Void doInBackground(Void... params) {
//                String genericBoothID = null;
//
//                try {
//                    Order utilityOrder = orderConnector.getOrder(orderID);
//                    List<LineItem> lineItemList = utilityOrder.getLineItems();
//                    List<LineItem> swappedLineItemList = new ArrayList<>();
//                    for (LineItem lineItem : lineItemList) {
//                        if (lineItem.getName().contains("Booth (Generic)"))
//                            genericBoothID = lineItem.getId();
//                        else swappedLineItemList.add(lineItem);
//                    }
//                    if (null != genericBoothID) {
//                        List<String> itemsToRemoveIncludingGenericBooth = new ArrayList<>();
//                        itemsToRemoveIncludingGenericBooth.add(genericBoothID);
//                        orderConnector.deleteLineItems(orderID, itemsToRemoveIncludingGenericBooth);
//
//                        for (Item item : inventoryConnector.getItems()) {
//                            if (item.getSku().equalsIgnoreCase("25"))
//                                orderConnector.addFixedPriceLineItem(orderID, item.getId(), null, null);
//                        }
//                    }
//                } catch (Exception e) {
//                    Log.d("Clover Excpt:", e.getMessage(), e.getCause());
//                }
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(Void result) {
//                orderConnector.disconnect();
//                inventoryConnector.disconnect();
//                orderConnector = null;
//                inventoryConnector = null;
//            }
//        }.execute();
//    }

//
//    private class GetShowBoothsTask extends AsyncTask<Void, Void, Void> {
//        private ProgressDialog progressDialog;
//        private InventoryConnector inventoryConnector;
//        /////UTILITY LISTS
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            progressDialog = new ProgressDialog(selectBoothActivityContext);
//            progressDialog.setMessage("Loading Booths...");
//            progressDialog.show();
//            boothList = new ArrayList<>();
//
//            inventoryConnector = new InventoryConnector(selectBoothActivityContext, CloverAccount.getAccount(selectBoothActivityContext), null);
//            inventoryConnector.connect();
//        }
//
//        @Override
//        protected Void doInBackground(Void... params) {
//            try {
//                ///// FETCH BOOTHS FOR SHOW
//                ListIterator<Item> iterator = inventoryConnector.getItems().listIterator();
//                do {
//                    Item boothTest = iterator.next();
//                    for (Tag boothTestTag : boothTest.getTags()) {
//                        if (boothTestTag.getId().equalsIgnoreCase(show.getId()))
//                            boothList.add(boothTest);
//                    }
//                } while (iterator.hasNext());
//            } catch (RemoteException | BindingException | ServiceException | ClientException e1) {
//                Log.e("Clover Excptn; ", e1.getClass().getName() + " : " + e1.getMessage());
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void result) {
//            super.onPostExecute(result);
//            inventoryConnector.disconnect();
//            createBoothSelectionTable();
//            progressDialog.dismiss();
//        }
//    }
//
//    private void createBoothSelectionTable() {
//        boothListTable.removeAllViews();
//        populateBoothSelectionHeaderRow();
//
//        if (boothList.size() > 0) {
//            for (Item booth : boothList) {
//                final Item finalizedBooth = booth;
//                /////////CREATE NEW ROW AND NECESSARY TEXTVIEWS
//                TableRow newBoothRow = new TableRow(boothReservationActivityContext);
//                TextView boothNumberTv = new TextView(boothReservationActivityContext);
//                TextView boothPriceTv = new TextView(boothReservationActivityContext);
//                TextView boothSizeTv = new TextView(boothReservationActivityContext);
//                TextView boothAreaTv = new TextView(boothReservationActivityContext);
//                TextView boothTypeTv = new TextView(boothReservationActivityContext);
//                TextView boothAvailabilityTv = new TextView(boothReservationActivityContext);
//
//                /////////POPULATE TVS / HANDLE ANY PROCESSING
//                boothNumberTv.setText(booth.getSku());
//                /////////HANDLE PRICE
//                String formattedPrice = GlobalUtils.getFormattedPriceStringFromLong(booth.getPrice());
//                boothPriceTv.setText(formattedPrice);
//                if (booth.getCode().equalsIgnoreCase("AVAILABLE")) {
//                    boothAvailabilityTv.setText(getResources().getString(R.string.booth_reservation_available_string));
//                    boothAvailabilityTv.setTextAppearance(boothReservationActivityContext, R.style.available_booth_style);
//                } else {
//                    if (booth.getCode().equalsIgnoreCase("RESERVED")) {
//                        boothAvailabilityTv.setText(getResources().getString(R.string.booth_reservation_unavailable_string));
//                        boothAvailabilityTv.setTextAppearance(boothReservationActivityContext, R.style.reserved_booth_style);
//                    } else boothAvailabilityTv.setText(booth.getCode());
//                }
//
//                ///// WILL FILTER OUT SHOW TAG FROM BOOTH
//                Tag sizeTag;
//                Tag areaTag;
//                Tag typeTag;
//                for (Tag currentTag : booth.getTags()) {
//                    if (currentTag.getName().substring(0, 4).equalsIgnoreCase("size")) {
//                        sizeTag = currentTag;
//                        String unformattedSizeTagName = GlobalUtils.getUnformattedTagName(sizeTag.getName(), "Size");
//                        if (unformattedSizeTagName.length() > 0) {
//                            boothSizeTv.setText(unformattedSizeTagName);
//                        } else {
//                            boothSizeTv.setText(getResources().getString(R.string.booth_tag_not_available));
//                        }
//                    } else if (currentTag.getName().substring(0, 4).equalsIgnoreCase("area")) {
//                        areaTag = currentTag;
//                        String unformattedAreaTagName = GlobalUtils.getUnformattedTagName(areaTag.getName(), "Area");
//                        if (unformattedAreaTagName.length() > 0) {
//                            boothAreaTv.setText(unformattedAreaTagName);
//                        } else {
//                            boothAreaTv.setText(getResources().getString(R.string.booth_tag_not_available));
//                        }
//                    } else if (currentTag.getName().substring(0, 4).equalsIgnoreCase("type")) {
//                        typeTag = currentTag;
//                        String unformattedTypeTagName = GlobalUtils.getUnformattedTagName(typeTag.getName(), "Type");
//                        if (unformattedTypeTagName.length() > 0) {
//                            boothTypeTv.setText(unformattedTypeTagName);
//                        } else {
//                            boothTypeTv.setText(getResources().getString(R.string.booth_tag_not_available));
//                        }
//                    }
//                }
//
//                Button reserveBoothButton = new Button(boothReservationActivityContext);
//                reserveBoothButton.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        reserveBoothAction(show, finalizedBooth);
//                    }
//                });
//                reserveBoothButton.setText(getResources().getString(R.string.reserve_booth_button_text));
//                if (finalizedBooth.getCode().equalsIgnoreCase("RESERVED")) {
//                    reserveBoothButton.setEnabled(false);
//                }
//
//                ///////////POPULATE THE NEW ROW AND ADD TO TABLE
//                newBoothRow.addView(boothNumberTv);
//                newBoothRow.addView(boothPriceTv);
//                newBoothRow.addView(boothSizeTv);
//                newBoothRow.addView(boothAreaTv);
//                newBoothRow.addView(boothTypeTv);
//                newBoothRow.addView(boothAvailabilityTv);
//                newBoothRow.addView(reserveBoothButton);
//                boothListTable.addView(newBoothRow);
//            }
//        } else {
//            TextView noBoothsForShowTV = new TextView(boothReservationActivityContext);
//            noBoothsForShowTV.setText(getResources().getString(R.string.booth_reservation_no_booths_for_show_text));
//            noBoothsForShowTV.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
//            TableRow noBoothsForShowRow = new TableRow(boothReservationActivityContext);
//            TableRow.LayoutParams params = new TableRow.LayoutParams();
//            params.span = 6;
//            params.topMargin = 50;
//            noBoothsForShowRow.addView(noBoothsForShowTV, params);
//            boothListTable.addView(noBoothsForShowRow);
//        }
//    }
}
