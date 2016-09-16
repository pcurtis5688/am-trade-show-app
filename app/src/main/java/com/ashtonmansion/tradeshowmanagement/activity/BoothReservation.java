package com.ashtonmansion.tradeshowmanagement.activity;

import android.accounts.Account;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.ashtonmansion.amtradeshowmanagement.R;
import com.ashtonmansion.tradeshowmanagement.util.GlobalUtils;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v3.base.Reference;
import com.clover.sdk.v3.inventory.InventoryConnector;
import com.clover.sdk.v3.inventory.Item;
import com.clover.sdk.v3.inventory.Tag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

public class BoothReservation extends AppCompatActivity {
    ////////CONTEXT AND UI OBJECTS
    private Context boothReservationActivityContext;
    private TableLayout boothListTable;
    ////////DATA VARS
    private Tag show;
    private String showNameForUser;
    private List<Item> boothList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booth_reservation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.booth_reservation_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //////////////FIELD DEFINITIONS & DATA HANDLING
        boothReservationActivityContext = this;
        boothListTable = (TableLayout) findViewById(R.id.booth_selection_booth_table);

        Bundle extrasBundle = getIntent().getExtras();
        if (extrasBundle != null) {
            Tag passedShowTag = (Tag) extrasBundle.get("show");
            if (null != passedShowTag) {
                show = passedShowTag;
                List<String> decoupledShowArray = Arrays.asList(show.getName().split(","));
                String showName = decoupledShowArray.get(1);
                String showDate = decoupledShowArray.get(2);
                String showLocation = decoupledShowArray.get(3);
                showNameForUser = getResources().getString(R.string.show_name_for_user_string, showName, showDate, showLocation);
            }
            TextView boothSelectionHeaderTV = (TextView) findViewById(R.id.booth_selection_header);
            boothSelectionHeaderTV.setText(showNameForUser);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        GetShowBoothsTask getShowBoothsTask = new GetShowBoothsTask();
        getShowBoothsTask.execute();
    }

    private class GetShowBoothsTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;
        private Account merchantAccount;
        private InventoryConnector inventoryConnector;
        /////UTILITY LISTS
        private List<Reference> boothReferenceList = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(boothReservationActivityContext);
            progressDialog.setMessage("Loading Booths...");
            progressDialog.show();
            boothList = new ArrayList<>();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                merchantAccount = CloverAccount.getAccount(boothReservationActivityContext);
                inventoryConnector = new InventoryConnector(boothReservationActivityContext, merchantAccount, null);
                inventoryConnector.connect();

                ///// FETCH BOOTHS FOR SHOW
                ListIterator<Item> iterator = inventoryConnector.getItems().listIterator();
                do {
                    Item boothTest = iterator.next();
                    for (Tag boothTestTag : boothTest.getTags()) {
                        if (boothTestTag.getId().equalsIgnoreCase(show.getId()))
                            boothList.add(boothTest);
                    }
                } while (iterator.hasNext());
            } catch (RemoteException | BindingException | ServiceException | ClientException e1) {
                Log.e("Clover Excptn; ", e1.getClass().getName() + " : " + e1.getMessage());
            } finally {
                inventoryConnector.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            createBoothSelectionTable();
            progressDialog.dismiss();
        }
    }

    private void createBoothSelectionTable() {
        boothListTable.removeAllViews();
        populateBoothSelectionHeaderRow();

        if (boothList.size() > 0) {
            for (Item booth : boothList) {
                final Item finalizedBooth = booth;
                /////////CREATE NEW ROW AND NECESSARY TEXTVIEWS
                TableRow newBoothRow = new TableRow(boothReservationActivityContext);
                TextView boothNumberTv = new TextView(boothReservationActivityContext);
                TextView boothPriceTv = new TextView(boothReservationActivityContext);
                TextView boothSizeTv = new TextView(boothReservationActivityContext);
                TextView boothAreaTv = new TextView(boothReservationActivityContext);
                TextView boothTypeTv = new TextView(boothReservationActivityContext);
                TextView boothCustomerTv = new TextView(boothReservationActivityContext);

                /////////POPULATE TVS / HANDLE ANY PROCESSING
                boothNumberTv.setText(booth.getSku());
                /////////HANDLE PRICE
                String formattedPrice = GlobalUtils.getFormattedPriceStringFromLong(booth.getPrice());
                boothPriceTv.setText(formattedPrice);
                boothCustomerTv.setText("customerhere");
                ///// WILL FILTER OUT SHOW TAG FROM BOOTH
                Tag sizeTag;
                Tag areaTag;
                Tag typeTag;
                for (Tag currentTag : booth.getTags()) {
                    if (currentTag.getName().substring(0, 4).equalsIgnoreCase("size")) {
                        sizeTag = currentTag;
                        String unformattedSizeTagName = GlobalUtils.getUnformattedTagName(sizeTag.getName(), "Size");
                        if (unformattedSizeTagName.length() > 0) {
                            boothSizeTv.setText(unformattedSizeTagName);
                        } else {
                            boothSizeTv.setText(getResources().getString(R.string.booth_tag_not_available));
                        }
                    } else if (currentTag.getName().substring(0, 4).equalsIgnoreCase("area")) {
                        areaTag = currentTag;
                        String unformattedAreaTagName = GlobalUtils.getUnformattedTagName(areaTag.getName(), "Area");
                        if (unformattedAreaTagName.length() > 0) {
                            boothAreaTv.setText(unformattedAreaTagName);
                        } else {
                            boothAreaTv.setText(getResources().getString(R.string.booth_tag_not_available));
                        }
                    } else if (currentTag.getName().substring(0, 4).equalsIgnoreCase("type")) {
                        typeTag = currentTag;
                        String unformattedTypeTagName = GlobalUtils.getUnformattedTagName(typeTag.getName(), "Type");
                        if (unformattedTypeTagName.length() > 0) {
                            boothTypeTv.setText(unformattedTypeTagName);
                        } else {
                            boothTypeTv.setText(getResources().getString(R.string.booth_tag_not_available));
                        }
                    }
                }

                Button reserveBoothButton = new Button(boothReservationActivityContext);
                reserveBoothButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        reserveBoothAction(show, finalizedBooth);
                    }
                });
                reserveBoothButton.setText(getResources().getString(R.string.reserve_booth_button_text));

                ///////////POPULATE THE NEW ROW AND ADD TO TABLE
                newBoothRow.addView(boothNumberTv);
                newBoothRow.addView(boothPriceTv);
                newBoothRow.addView(boothSizeTv);
                newBoothRow.addView(boothAreaTv);
                newBoothRow.addView(boothTypeTv);
                newBoothRow.addView(boothCustomerTv);
                newBoothRow.addView(reserveBoothButton);
                boothListTable.addView(newBoothRow);
            }
        } else {
            TextView noBoothsForShowTV = new TextView(boothReservationActivityContext);
            noBoothsForShowTV.setText(getResources().getString(R.string.booth_reservation_no_booths_for_show_text));
            noBoothsForShowTV.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            TableRow noBoothsForShowRow = new TableRow(boothReservationActivityContext);
            TableRow.LayoutParams params = new TableRow.LayoutParams();
            params.span = 6;
            params.topMargin = 50;
            noBoothsForShowRow.addView(noBoothsForShowTV, params);
            boothListTable.addView(noBoothsForShowRow);
        }
    }

    private void populateBoothSelectionHeaderRow() {
        TableRow boothSelectionTableHeaderRow = new TableRow(boothReservationActivityContext);
        TextView boothNumberHeaderTv = new TextView(boothReservationActivityContext);
        TextView boothPriceHeaderTv = new TextView(boothReservationActivityContext);
        TextView boothSizeHeaderTv = new TextView(boothReservationActivityContext);
        TextView boothAreaHeaderTv = new TextView(boothReservationActivityContext);
        TextView boothTypeHeaderTv = new TextView(boothReservationActivityContext);
        TextView boothCustomerHeaderTv = new TextView(boothReservationActivityContext);

        boothNumberHeaderTv.setText(getResources().getString(R.string.booth_selection_booth_number_header));
        boothNumberHeaderTv.setTextAppearance(boothReservationActivityContext, R.style.table_header_text_style);

        boothPriceHeaderTv.setText(getResources().getString(R.string.booth_selection_booth_price_header));
        boothPriceHeaderTv.setTextAppearance(boothReservationActivityContext, R.style.table_header_text_style);

        boothSizeHeaderTv.setText(getResources().getString(R.string.booth_selection_booth_size_header));
        boothSizeHeaderTv.setTextAppearance(boothReservationActivityContext, R.style.table_header_text_style);

        boothAreaHeaderTv.setText(getResources().getString(R.string.booth_selection_booth_area_header));
        boothAreaHeaderTv.setTextAppearance(boothReservationActivityContext, R.style.table_header_text_style);

        boothTypeHeaderTv.setText(getResources().getString(R.string.booth_selection_booth_type_header));
        boothTypeHeaderTv.setTextAppearance(boothReservationActivityContext, R.style.table_header_text_style);

        boothCustomerHeaderTv.setText(getResources().getString(R.string.booth_selection_booth_customer_header));
        boothCustomerHeaderTv.setTextAppearance(boothReservationActivityContext, R.style.table_header_text_style);

        boothSelectionTableHeaderRow.addView(boothNumberHeaderTv);
        boothSelectionTableHeaderRow.addView(boothPriceHeaderTv);
        boothSelectionTableHeaderRow.addView(boothSizeHeaderTv);
        boothSelectionTableHeaderRow.addView(boothAreaHeaderTv);
        boothSelectionTableHeaderRow.addView(boothTypeHeaderTv);
        boothSelectionTableHeaderRow.addView(boothCustomerHeaderTv);
        boothListTable.addView(boothSelectionTableHeaderRow);
    }

    private void reserveBoothAction(Tag show, Item boothToReserve) {
        Intent reserveBoothIntent = new Intent(boothReservationActivityContext, ReserveBoothDetails.class);
        reserveBoothIntent.putExtra("show", show);
        reserveBoothIntent.putExtra("booth", boothToReserve);
        startActivity(reserveBoothIntent);
    }
}
