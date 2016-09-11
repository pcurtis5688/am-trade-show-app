package com.ashtonmansion.tradeshowmanagement.activity;

import android.accounts.Account;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import com.clover.sdk.v3.inventory.Category;
import com.clover.sdk.v3.inventory.InventoryConnector;
import com.clover.sdk.v3.inventory.Item;
import com.clover.sdk.v3.inventory.Tag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BoothReservation extends AppCompatActivity {
    private Context boothReservationActivityContext;
    ////////UI OBJECTS
    private TableLayout boothListTable;
    private TextView boothSelectionHeaderTV;
    ////////CLOVER DATA
    private Account merchantAccount;
    private InventoryConnector inventoryConnector;
    private List<Item> boothList;
    ////////SHOW VARS
    private Category show;
    private String showID;
    private String showName;
    private String showDate;
    private String showLocation;
    private String showNotes;
    private String userShowName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booth_reservation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //////////////FIELD DEFINITIONS & DATA HANDLING
        boothReservationActivityContext = this;
        boothListTable = (TableLayout) findViewById(R.id.booth_selection_booth_table);

        Bundle extrasBundle = getIntent().getExtras();
        if (extrasBundle != null) {
            Category passedCategoryObject = (Category) extrasBundle.get("show");
            if (null != passedCategoryObject) {
                showID = passedCategoryObject.getId();
                List<String> decoupledShowArray = Arrays.asList(passedCategoryObject.getName().split(","));
                showName = decoupledShowArray.get(0);
                showDate = decoupledShowArray.get(1);
                showLocation = decoupledShowArray.get(2);
                showNotes = decoupledShowArray.get(3);
                userShowName = showName + " (" + showDate + " - " + showLocation + ")";
            }
            boothSelectionHeaderTV = (TextView) findViewById(R.id.booth_selection_header);
            boothSelectionHeaderTV.setText(userShowName);
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

                ////////FIND CATEGORY IN CLOVER, POPULATE BOOTH REFERENCE LIST
                List<Category> categoryList = inventoryConnector.getCategories();
                List<Reference> boothReferenceList = new ArrayList<>();
                for (Category category : categoryList) {
                    if (category.getId().equalsIgnoreCase(showID)) {
                        show = category;
                        if (show.hasItems()) boothReferenceList = show.getItems();
                    }
                }

                //////////////ITERATE REF LIST AND ADD BOOTHS TO LIST
                for (Reference boothRef : boothReferenceList) {
                    Item currentBooth = inventoryConnector.getItem(boothRef.getId());
                    currentBooth.setTags(inventoryConnector.getTagsForItem(currentBooth.getId()));
                    boothList.add(currentBooth);
                }
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
                TextView boothCategoryTv = new TextView(boothReservationActivityContext);
                TextView boothCustomerTv = new TextView(boothReservationActivityContext);

                /////////POPULATE TVS / HANDLE ANY PROCESSING
                boothNumberTv.setText(booth.getSku());
                /////////HANDLE PRICE
                String formattedPrice = GlobalUtils.getFormattedPriceStringFromLong(booth.getPrice());
                boothPriceTv.setText(formattedPrice);
                boothCustomerTv.setText("customerhere");

                Tag sizeTag = null;
                Tag areaTag = null;
                Tag categoryTag = null;
                for (Tag currentTag : booth.getTags()) {
                    if (currentTag.getName().substring(0, 4).equalsIgnoreCase("size")) {
                        sizeTag = currentTag;
                        boothSizeTv.setText(GlobalUtils.getUnformattedTagName(sizeTag.getName(), "Size"));
                    } else if (currentTag.getName().substring(0, 4).equalsIgnoreCase("area")) {
                        areaTag = currentTag;
                        boothAreaTv.setText(GlobalUtils.getUnformattedTagName(areaTag.getName(), "Area"));
                    } else if (currentTag.getName().substring(0, 8).equalsIgnoreCase("category")) {
                        categoryTag = currentTag;
                        boothCategoryTv.setText(GlobalUtils.getUnformattedTagName(categoryTag.getName(), "Category"));
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
                newBoothRow.addView(boothCategoryTv);
                newBoothRow.addView(boothCustomerTv);
                newBoothRow.addView(reserveBoothButton);
                boothListTable.addView(newBoothRow);
            }
        } else {
            TextView noBoothsForShowTV = new TextView(boothReservationActivityContext);
            noBoothsForShowTV.setText(getResources().getString(R.string.booth_reservation_no_booths_for_show_text));
            boothListTable.addView(noBoothsForShowTV);
        }
    }

    private void populateBoothSelectionHeaderRow() {
        TableRow boothSelectionTableHeaderRow = new TableRow(boothReservationActivityContext);
        TextView boothNumberHeaderTv = new TextView(boothReservationActivityContext);
        TextView boothPriceHeaderTv = new TextView(boothReservationActivityContext);
        TextView boothSizeHeaderTv = new TextView(boothReservationActivityContext);
        TextView boothAreaHeaderTv = new TextView(boothReservationActivityContext);
        TextView boothCategoryHeaderTv = new TextView(boothReservationActivityContext);
        TextView boothCustomerHeaderTv = new TextView(boothReservationActivityContext);

        boothNumberHeaderTv.setText(getResources().getString(R.string.booth_selection_booth_number_header));
        boothPriceHeaderTv.setText(getResources().getString(R.string.booth_selection_booth_price_header));
        boothSizeHeaderTv.setText(getResources().getString(R.string.booth_selection_booth_size_header));
        boothAreaHeaderTv.setText(getResources().getString(R.string.booth_selection_booth_area_header));
        boothCategoryHeaderTv.setText(getResources().getString(R.string.booth_selection_booth_category_header));
        boothCustomerHeaderTv.setText(getResources().getString(R.string.booth_selection_booth_customer_header));

        boothSelectionTableHeaderRow.addView(boothNumberHeaderTv);
        boothSelectionTableHeaderRow.addView(boothPriceHeaderTv);
        boothSelectionTableHeaderRow.addView(boothSizeHeaderTv);
        boothSelectionTableHeaderRow.addView(boothAreaHeaderTv);
        boothSelectionTableHeaderRow.addView(boothCategoryHeaderTv);
        boothSelectionTableHeaderRow.addView(boothCustomerHeaderTv);
        boothListTable.addView(boothSelectionTableHeaderRow);
    }

    private void reserveBoothAction(Category show, Item boothToReserve) {
        Intent reserveBoothIntent = new Intent(boothReservationActivityContext, ReserveBoothDetails.class);
        reserveBoothIntent.putExtra("show", show);
        reserveBoothIntent.putExtra("booth", boothToReserve);
        startActivity(reserveBoothIntent);
    }
}
