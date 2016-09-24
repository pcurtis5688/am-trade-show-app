package com.ashtonmansion.tradeshowmanagement.activity;

import android.accounts.Account;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
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
import com.clover.sdk.v3.inventory.InventoryConnector;
import com.clover.sdk.v3.inventory.Item;
import com.clover.sdk.v3.inventory.Tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

public class BoothReservation extends AppCompatActivity {
    ///// CONTEXT AND UI OBJECTS
    private Context boothReservationActivityContext;
    private TableLayout boothListTable;
    private boolean startedFromApp;
    private String lastSortedBy;
    ///// DATA VARS
    private Tag show;
    private String showNameForUser;
    private List<BoothWithTags> boothWithTagsList;
    ///// ORDER DATA BEING PASSED FROM CLOVER CUSTOM TENDER
    private String orderID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booth_reservation);

        //////////////FIELD DEFINITIONS & DATA HANDLING
        boothReservationActivityContext = this;
        boothListTable = (TableLayout) findViewById(R.id.booth_selection_booth_table);
        lastSortedBy = "none";

        Bundle extrasBundle = getIntent().getExtras();
        if (extrasBundle != null) {
            orderID = (String) extrasBundle.get("orderid");
            Tag passedShowTag = (Tag) extrasBundle.get("show");
            if (null != passedShowTag) {
                show = passedShowTag;
                List<String> decoupledShowArray = GlobalUtils.decoupleShowName(show.getName());
                String showName = decoupledShowArray.get(0);
                String showDate = decoupledShowArray.get(1);
                String showLocation = decoupledShowArray.get(2);
                showNameForUser = getResources().getString(R.string.show_name_for_user_string, showName, showDate, showLocation);
            }
            TextView boothSelectionHeaderTV = (TextView) findViewById(R.id.booth_selection_header);
            boothSelectionHeaderTV.setText(showNameForUser);

            ////// TO DETERMINE BUTTON TEXT
            if (orderID == null) startedFromApp = true;
            else startedFromApp = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        GetShowBoothsTask getShowBoothsTask = new GetShowBoothsTask();
        getShowBoothsTask.execute();
    }

    private void populateBoothSelectionHeaderRow() {
        TableRow boothSelectionTableHeaderRow = new TableRow(boothReservationActivityContext);
        TextView boothNumberHeaderTv = new TextView(boothReservationActivityContext);
        TextView boothPriceHeaderTv = new TextView(boothReservationActivityContext);
        TextView boothSizeHeaderTv = new TextView(boothReservationActivityContext);
        TextView boothAreaHeaderTv = new TextView(boothReservationActivityContext);
        TextView boothTypeHeaderTv = new TextView(boothReservationActivityContext);
        TextView boothAvailabilityTv = new TextView(boothReservationActivityContext);

        boothNumberHeaderTv.setText(getResources().getString(R.string.booth_selection_booth_number_header));
        boothNumberHeaderTv.setTextAppearance(boothReservationActivityContext, R.style.table_header_text_style_sortable);
        boothPriceHeaderTv.setText(getResources().getString(R.string.booth_selection_booth_price_header));
        boothPriceHeaderTv.setTextAppearance(boothReservationActivityContext, R.style.table_header_text_style_sortable);
        boothSizeHeaderTv.setText(getResources().getString(R.string.booth_selection_booth_size_header));
        boothSizeHeaderTv.setTextAppearance(boothReservationActivityContext, R.style.table_header_text_style_sortable);
        boothAreaHeaderTv.setText(getResources().getString(R.string.booth_selection_booth_area_header));
        boothAreaHeaderTv.setTextAppearance(boothReservationActivityContext, R.style.table_header_text_style_sortable);
        boothTypeHeaderTv.setText(getResources().getString(R.string.booth_selection_booth_type_header));
        boothTypeHeaderTv.setTextAppearance(boothReservationActivityContext, R.style.table_header_text_style_sortable);
        boothAvailabilityTv.setText(getResources().getString(R.string.booth_selection_booth_availability_header));
        boothAvailabilityTv.setTextAppearance(boothReservationActivityContext, R.style.span_2_and_table_header_style);

        ////// ADD ACTION LISTENERS TO SORT BY THE CLICKED HEADER
        boothNumberHeaderTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortBoothListByBoothNo();
            }
        });
        boothPriceHeaderTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortBoothListByPrice();
            }
        });
        boothSizeHeaderTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortBoothListBySizeTag();
            }
        });
        boothAreaHeaderTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortBoothListByAreaTag();
            }
        });
        boothTypeHeaderTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortBoothListByTypeTag();
            }
        });
        boothAvailabilityTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortBoothListByAvailability();
            }
        });

        boothSelectionTableHeaderRow.addView(boothNumberHeaderTv);
        boothSelectionTableHeaderRow.addView(boothPriceHeaderTv);
        boothSelectionTableHeaderRow.addView(boothSizeHeaderTv);
        boothSelectionTableHeaderRow.addView(boothAreaHeaderTv);
        boothSelectionTableHeaderRow.addView(boothTypeHeaderTv);
        boothSelectionTableHeaderRow.addView(boothAvailabilityTv);
        boothListTable.addView(boothSelectionTableHeaderRow);
    }

    private void createBoothSelectionTable() {
        boothListTable.removeAllViews();
        populateBoothSelectionHeaderRow();

        if (boothWithTagsList.size() > 0) {
            for (BoothWithTags boothWithTags : boothWithTagsList) {
                final Item finalizedBooth = boothWithTags.booth;
                /////////CREATE NEW ROW AND NECESSARY TEXTVIEWS
                TableRow newBoothRow = new TableRow(boothReservationActivityContext);
                TextView boothNumberTv = new TextView(boothReservationActivityContext);
                TextView boothPriceTv = new TextView(boothReservationActivityContext);
                TextView boothSizeTv = new TextView(boothReservationActivityContext);
                TextView boothAreaTv = new TextView(boothReservationActivityContext);
                TextView boothTypeTv = new TextView(boothReservationActivityContext);
                TextView boothAvailabilityTv = new TextView(boothReservationActivityContext);

                ///// FONT HANDLING
                boothNumberTv.setTextAppearance(boothReservationActivityContext, R.style.large_table_row_font_station);
                boothPriceTv.setTextAppearance(boothReservationActivityContext, R.style.large_table_row_font_station);
                boothSizeTv.setTextAppearance(boothReservationActivityContext, R.style.large_table_row_font_station);
                boothAreaTv.setTextAppearance(boothReservationActivityContext, R.style.large_table_row_font_station);
                boothTypeTv.setTextAppearance(boothReservationActivityContext, R.style.large_table_row_font_station);

                /////////POPULATE TVS / HANDLE ANY PROCESSING
                boothNumberTv.setText(boothWithTags.booth.getSku());
                boothPriceTv.setText(GlobalUtils.getFormattedPriceStringFromLong(boothWithTags.booth.getPrice()));
                boothSizeTv.setText(boothWithTags.getSizeTag());
                boothAreaTv.setText(boothWithTags.getAreaTag());
                boothTypeTv.setText(boothWithTags.getTypeTag());
                if (boothWithTags.booth.getCode().equalsIgnoreCase("AVAILABLE")) {
                    boothAvailabilityTv.setText(getResources().getString(R.string.booth_reservation_available_string));
                    boothAvailabilityTv.setTextAppearance(boothReservationActivityContext, R.style.available_booth_style);
                } else {
                    boothAvailabilityTv.setText(boothWithTags.booth.getCode());
                    boothAvailabilityTv.setTextAppearance(boothReservationActivityContext, R.style.reserved_booth_style);
                }

                Button reserveBoothButton = new Button(boothReservationActivityContext);
                if (startedFromApp)
                    reserveBoothButton.setText(getResources().getString(R.string.view_booth_details_string));
                else
                    reserveBoothButton.setText(getResources().getString(R.string.reserve_booth_button_text));
                reserveBoothButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        reserveBoothAction(show, finalizedBooth);
                    }
                });
                reserveBoothButton.setTextAppearance(boothReservationActivityContext, R.style.row_item_button_style);
                if (!startedFromApp) {
                    if (!finalizedBooth.getCode().equalsIgnoreCase("AVAILABLE")) {
                        reserveBoothButton.setEnabled(false);
                    }
                }

                ///////////POPULATE THE NEW ROW AND ADD TO TABLE
                newBoothRow.addView(boothNumberTv);
                newBoothRow.addView(boothPriceTv);
                newBoothRow.addView(boothSizeTv);
                newBoothRow.addView(boothAreaTv);
                newBoothRow.addView(boothTypeTv);
                newBoothRow.addView(boothAvailabilityTv);
                newBoothRow.addView(reserveBoothButton);
                boothListTable.addView(newBoothRow);
            }
        } else {
            ///// HANDLE CASE - NO AVAILABLE BOOTHS FOR SHOW
            TextView noBoothsForShowTV = new TextView(boothReservationActivityContext);
            noBoothsForShowTV.setText(getResources().getString(R.string.booth_reservation_no_booths_for_show_text));
            noBoothsForShowTV.setTextAppearance(boothReservationActivityContext, R.style.large_table_row_font_station);
            noBoothsForShowTV.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

            TableRow noBoothsForShowRow = new TableRow(boothReservationActivityContext);
            TableRow.LayoutParams params = new TableRow.LayoutParams();
            params.span = 6;
            params.topMargin = 50;

            noBoothsForShowRow.addView(noBoothsForShowTV, params);
            boothListTable.addView(noBoothsForShowRow);
        }
    }

    private void reserveBoothAction(Tag show, Item boothToReserve) {
        Intent reserveBoothIntent = new Intent(boothReservationActivityContext, ReserveBoothDetails.class);
        reserveBoothIntent.putExtra("show", show);
        reserveBoothIntent.putExtra("booth", boothToReserve);
        reserveBoothIntent.putExtra("orderid", orderID);
        startActivity(reserveBoothIntent);
    }

    private void sortBoothListByBoothNo() {
        if (lastSortedBy.equalsIgnoreCase("boothNumber")) {
            Collections.reverse(boothWithTagsList);
        } else {
            lastSortedBy = "boothNumber";
            Collections.sort(boothWithTagsList, new Comparator<BoothWithTags>() {
                public int compare(BoothWithTags boothWithTags1, BoothWithTags boothWithTags2) {
                    return (boothWithTags1.booth.getSku().compareTo(boothWithTags2.booth.getSku()));
                }
            });
        }
        createBoothSelectionTable();
    }

    private void sortBoothListByPrice() {
        if (lastSortedBy.equalsIgnoreCase("boothPrice")) {
            Collections.reverse(boothWithTagsList);
        } else {
            lastSortedBy = "boothPrice";
            Collections.sort(boothWithTagsList, new Comparator<BoothWithTags>() {
                public int compare(BoothWithTags boothWithTags1, BoothWithTags boothWithTags2) {
                    return boothWithTags1.booth.getPrice().compareTo(boothWithTags2.booth.getPrice());
                }
            });
        }
        createBoothSelectionTable();
    }

    private void sortBoothListByAvailability() {
        if (lastSortedBy.equalsIgnoreCase("boothAvailability")) {
            Collections.reverse(boothWithTagsList);
        } else {
            lastSortedBy = "boothAvailability";
            Collections.sort(boothWithTagsList, new Comparator<BoothWithTags>() {
                public int compare(BoothWithTags boothWithTags1, BoothWithTags boothWithTags2) {
                    return boothWithTags1.booth.getCode().compareTo(boothWithTags2.booth.getCode());
                }
            });
        }
        createBoothSelectionTable();
    }

    private void sortBoothListBySizeTag() {
        if (lastSortedBy.equalsIgnoreCase("sizeTag")) {
            Collections.reverse(boothWithTagsList);
        } else {
            lastSortedBy = "sizeTag";
            Collections.sort(boothWithTagsList, new Comparator<BoothWithTags>() {
                public int compare(BoothWithTags boothWithTags1, BoothWithTags boothWithTags2) {
                    return (boothWithTags1.getSizeTag().compareTo(boothWithTags2.getSizeTag()));
                }
            });
        }
        createBoothSelectionTable();
    }

    private void sortBoothListByAreaTag() {
        if (lastSortedBy.equalsIgnoreCase("areaTag")) {
            Collections.reverse(boothWithTagsList);
        } else {
            lastSortedBy = "areaTag";
            Collections.sort(boothWithTagsList, new Comparator<BoothWithTags>() {
                public int compare(BoothWithTags boothWithTags1, BoothWithTags boothWithTags2) {
                    return (boothWithTags1.getAreaTag().compareTo(boothWithTags2.getAreaTag()));
                }
            });
        }
        createBoothSelectionTable();
    }

    private void sortBoothListByTypeTag() {
        if (lastSortedBy.equalsIgnoreCase("typeTag")) {
            Collections.reverse(boothWithTagsList);
        } else {
            lastSortedBy = "typeTag";
            Collections.sort(boothWithTagsList, new Comparator<BoothWithTags>() {
                public int compare(BoothWithTags boothWithTags1, BoothWithTags boothWithTags2) {
                    return (boothWithTags1.getTypeTag().compareTo(boothWithTags2.getTypeTag()));
                }
            });
        }
        createBoothSelectionTable();
    }

    private void processBoothWithTagsList() {
        for (BoothWithTags boothWithTags : boothWithTagsList) {
            for (Tag currentTag : boothWithTags.booth.getTags()) {
                if (!currentTag.getName().contains(" [Show]")) {
                    if (currentTag.getName().substring(0, 4).equalsIgnoreCase("size")) {
                        boothWithTags.setSizeTag(GlobalUtils.getUnformattedTagName(currentTag.getName(), "Size"));
                    } else if (currentTag.getName().substring(0, 4).equalsIgnoreCase("area")) {
                        boothWithTags.setAreaTag(GlobalUtils.getUnformattedTagName(currentTag.getName(), "Area"));
                    } else if (currentTag.getName().substring(0, 4).equalsIgnoreCase("type")) {
                        boothWithTags.setTypeTag(GlobalUtils.getUnformattedTagName(currentTag.getName(), "Type"));
                    }
                }
            }
        }
    }

    private class GetShowBoothsTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;
        private InventoryConnector inventoryConnector;
        /////UTILITY LISTS

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(boothReservationActivityContext);
            progressDialog.setMessage("Loading Booths...");
            progressDialog.show();
            boothWithTagsList = new ArrayList<>();
            ////// INITIALIZE CLOVER CONNECTIONS
            inventoryConnector = new InventoryConnector(boothReservationActivityContext, CloverAccount.getAccount(boothReservationActivityContext), null);
            inventoryConnector.connect();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                ////// FETCH BOOTHS FOR SHOW, CREATE BOOTH W TAGS OBJECT, ADD TO LIST
                ListIterator<Item> iterator = inventoryConnector.getItems().listIterator();
                do {
                    Item boothTest = iterator.next();
                    for (Tag boothTestTag : boothTest.getTags()) {
                        if (boothTestTag.getId().equalsIgnoreCase(show.getId())) {
                            BoothWithTags boothWithTags = new BoothWithTags(boothTest);
                            boothWithTagsList.add(boothWithTags);
                        }
                    }
                } while (iterator.hasNext());
            } catch (RemoteException | BindingException | ServiceException | ClientException e1) {
                Log.e("Clover Excptn; ", e1.getClass().getName() + " : " + e1.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            inventoryConnector.disconnect();
            processBoothWithTagsList();
            createBoothSelectionTable();
            progressDialog.dismiss();
        }
    }

    private class BoothWithTags {
        private Item booth;
        private String sizeTag;
        private String areaTag;
        private String typeTag;

        private BoothWithTags(Item booth) {
            this.booth = booth;
        }

        private BoothWithTags(Item booth, String sizeTag, String areaTag, String typeTag) {
            this.booth = booth;
            this.sizeTag = sizeTag;
            this.areaTag = areaTag;
            this.typeTag = typeTag;
        }

        public String getSizeTag() {
            return sizeTag;
        }

        public void setSizeTag(String sizeTag) {
            this.sizeTag = sizeTag;
        }

        public String getAreaTag() {
            return areaTag;
        }

        public void setAreaTag(String areaTag) {
            this.areaTag = areaTag;
        }

        public String getTypeTag() {
            return typeTag;
        }

        public void setTypeTag(String typeTag) {
            this.typeTag = typeTag;
        }
    }
}
