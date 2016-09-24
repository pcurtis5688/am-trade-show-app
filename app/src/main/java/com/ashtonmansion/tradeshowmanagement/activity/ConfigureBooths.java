package com.ashtonmansion.tradeshowmanagement.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.ashtonmansion.amtradeshowmanagement.R;
import com.ashtonmansion.tradeshowmanagement.HomeActivity;
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

public class ConfigureBooths extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    /////CONTEXT AND UI FIELDS
    private Context configureBoothsActivityContext;
    private TableLayout showTable;
    private String lastSortedBy;
    /////DATA VARS
    private Tag show;
    private String showNameForUser;
    ////// TRYING SOMETHING NEW
    private List<BoothWithTags> boothWithTagsList;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_booths);
        Toolbar toolbar = (Toolbar) findViewById(R.id.configure_booths_toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.configure_booths_drawerlayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_configure_booths);
        navigationView.setNavigationItemSelectedListener(this);

        //DATA AND ACTIVITY WORK
        configureBoothsActivityContext = this;
        showTable = (TableLayout) findViewById(R.id.booths_for_show_table);
        lastSortedBy = "none";

        Bundle extrasBundle = getIntent().getExtras();
        if (extrasBundle != null) {
            if (null != extrasBundle.get("show")) {
                show = (Tag) extrasBundle.get("show");
                List<String> decoupledShowArray = GlobalUtils.decoupleShowName(show.getName());
                String showName = decoupledShowArray.get(0);
                String showDate = decoupledShowArray.get(1);
                String showLocation = decoupledShowArray.get(2);
                showNameForUser = getResources().getString(R.string.show_name_for_user_string, showName, showDate, showLocation);
            }
            TextView showNameHeaderTV = (TextView) findViewById(R.id.show_booths_header);
            showNameHeaderTV.setText(showNameForUser);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        GetShowBoothsTask getShowBoothsTask = new GetShowBoothsTask();
        getShowBoothsTask.execute();
    }

    private void populateBoothHeaderRow() {
        ////// CREATE ALL ELEMENTS FOR HEADER ROW
        TableRow boothsForShowTableHeaderRow = new TableRow(configureBoothsActivityContext);
        TextView boothNumberHeaderTv = new TextView(configureBoothsActivityContext);
        TextView boothPriceHeaderTv = new TextView(configureBoothsActivityContext);
        TextView boothSizeHeaderTv = new TextView(configureBoothsActivityContext);
        TextView boothAreaHeaderTv = new TextView(configureBoothsActivityContext);
        TextView boothTypeHeaderTv = new TextView(configureBoothsActivityContext);
        TextView boothAvailabilityTv = new TextView(configureBoothsActivityContext);

        ////// SET TEXT AND APPEARANCES
        boothNumberHeaderTv.setText(getResources().getString(R.string.configure_show_booths_number_header));
        boothNumberHeaderTv.setTextAppearance(configureBoothsActivityContext, R.style.table_header_text_style_sortable);
        boothPriceHeaderTv.setText(getResources().getString(R.string.configure_show_booths_price_header));
        boothPriceHeaderTv.setTextAppearance(configureBoothsActivityContext, R.style.table_header_text_style_sortable);
        boothSizeHeaderTv.setText(getResources().getString(R.string.configure_show_booths_size_header));
        boothSizeHeaderTv.setTextAppearance(configureBoothsActivityContext, R.style.table_header_text_style_sortable);
        boothAreaHeaderTv.setText(getResources().getString(R.string.configure_show_booths_area_header));
        boothAreaHeaderTv.setTextAppearance(configureBoothsActivityContext, R.style.table_header_text_style_sortable);
        boothTypeHeaderTv.setText(getResources().getString(R.string.configure_show_booths_type_header));
        boothTypeHeaderTv.setTextAppearance(configureBoothsActivityContext, R.style.table_header_text_style_sortable);
        boothAvailabilityTv.setText(getResources().getString(R.string.booth_selection_booth_availability_header));
        boothAvailabilityTv.setTextAppearance(configureBoothsActivityContext, R.style.span_2_and_table_header_style);

        ////// ATTACH LISTENERS FOR SORTING PURPOSES
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

        ////// not really sure.
        Button boothFilterButton = new Button(configureBoothsActivityContext);
        boothFilterButton.setText(getResources().getString(R.string.configure_show_booths_filter_btn_text));
        boothFilterButton.setTextAppearance(configureBoothsActivityContext, R.style.button_font_style);
        boothFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filterBoothsAction();
            }
        });

        ////// ADD VIEWS TO ROW AND ROW TO TABLE
        boothsForShowTableHeaderRow.addView(boothNumberHeaderTv);
        boothsForShowTableHeaderRow.addView(boothPriceHeaderTv);
        boothsForShowTableHeaderRow.addView(boothSizeHeaderTv);
        boothsForShowTableHeaderRow.addView(boothAreaHeaderTv);
        boothsForShowTableHeaderRow.addView(boothTypeHeaderTv);
        boothsForShowTableHeaderRow.addView(boothAvailabilityTv);
        showTable.addView(boothsForShowTableHeaderRow);
    }

    private void populateBoothsForShowTable() {
        showTable.removeAllViews();
        populateBoothHeaderRow();

        if (boothWithTagsList != null && boothWithTagsList.size() > 0) {
            for (final BoothWithTags boothWithTags : boothWithTagsList) {
                final BoothWithTags finalizedBoothWithTagsItem = boothWithTags;

                ///// CREATE TEXT VIEWS
                TableRow newBoothRow = new TableRow(configureBoothsActivityContext);
                TextView boothNumberTv = new TextView(configureBoothsActivityContext);
                TextView boothPriceTv = new TextView(configureBoothsActivityContext);
                TextView boothSizeTv = new TextView(configureBoothsActivityContext);
                TextView boothAreaTv = new TextView(configureBoothsActivityContext);
                TextView boothTypeTv = new TextView(configureBoothsActivityContext);
                TextView boothAvailabilityTv = new TextView(configureBoothsActivityContext);

                ///// SET FONTS
                boothNumberTv.setTextAppearance(configureBoothsActivityContext, R.style.large_table_row_font_station);
                boothPriceTv.setTextAppearance(configureBoothsActivityContext, R.style.large_table_row_font_station);
                boothSizeTv.setTextAppearance(configureBoothsActivityContext, R.style.large_table_row_font_station);
                boothAreaTv.setTextAppearance(configureBoothsActivityContext, R.style.large_table_row_font_station);
                boothTypeTv.setTextAppearance(configureBoothsActivityContext, R.style.large_table_row_font_station);

                ///// SET ROW DATA
                boothNumberTv.setText(boothWithTags.booth.getSku());
                boothPriceTv.setText(GlobalUtils.getFormattedPriceStringFromLong(boothWithTags.booth.getPrice()));
                boothSizeTv.setText(boothWithTags.getSizeTag());
                boothAreaTv.setText(boothWithTags.getAreaTag());
                boothTypeTv.setText(boothWithTags.getTypeTag());

                if (boothWithTags.booth.getCode().equalsIgnoreCase("AVAILABLE")) {
                    boothAvailabilityTv.setText(getResources().getString(R.string.booth_reservation_available_string));
                    boothAvailabilityTv.setTextAppearance(configureBoothsActivityContext, R.style.available_booth_style);
                } else {
                    boothAvailabilityTv.setText(boothWithTags.booth.getCode());
                    boothAvailabilityTv.setTextAppearance(configureBoothsActivityContext, R.style.reserved_booth_style);
                }

                Button editBoothButton = new Button(configureBoothsActivityContext);
                editBoothButton.setText(getResources().getString(R.string.configure_show_booths_edit_booth_btn_text));
                editBoothButton.setTextAppearance(configureBoothsActivityContext, R.style.row_item_button_style);
                editBoothButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        editBoothAction(boothWithTags.booth);
                    }
                });

                ///// ADD NEW ROW TO BOOTH CONFIG TABLE
                newBoothRow.addView(boothNumberTv);
                newBoothRow.addView(boothPriceTv);
                newBoothRow.addView(boothSizeTv);
                newBoothRow.addView(boothAreaTv);
                newBoothRow.addView(boothTypeTv);
                newBoothRow.addView(boothAvailabilityTv);
                newBoothRow.addView(editBoothButton);
                showTable.addView(newBoothRow);
            }
        } else {
            ///// HANDLE CASE - NO BOOTHS TO CONFIGURE
            TableRow noBoothsForShowRow = new TableRow(configureBoothsActivityContext);
            TextView noBoothsForShowTV = new TextView(configureBoothsActivityContext);
            noBoothsForShowTV.setText(getResources().getString(R.string.booth_configuration_no_booths_message));
            noBoothsForShowTV.setTextAppearance(configureBoothsActivityContext, R.style.no_rows_style);
            noBoothsForShowTV.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

            TableRow.LayoutParams params = new TableRow.LayoutParams();
            params.span = 6;
            params.topMargin = 25;
            params.bottomMargin = 25;
            noBoothsForShowRow.addView(noBoothsForShowTV, params);
            showTable.addView(noBoothsForShowRow);
        }

        TableRow addBoothButtonRow = new TableRow(configureBoothsActivityContext);
        Button addBoothButton = new Button(configureBoothsActivityContext);
        addBoothButton.setText(getResources()

                .

                        getString(R.string.action_create_new_booth_string));
        addBoothButton.setTextAppearance(configureBoothsActivityContext, R.style.button_font_style);
        addBoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent createBoothIntent = new Intent(configureBoothsActivityContext, CreateBooth.class);
                createBoothIntent.putExtra("show", show);
                startActivity(createBoothIntent);
            }
        });
        TableRow.LayoutParams params = new TableRow.LayoutParams();
        params.span = 7;
        addBoothButton.setLayoutParams(params);
        addBoothButtonRow.addView(addBoothButton);
        showTable.addView(addBoothButtonRow);
    }

    private void editBoothAction(Item booth) {
        Intent editBoothIntent = new Intent(configureBoothsActivityContext, EditBooth.class);
        editBoothIntent.putExtra("booth", booth);
        startActivity(editBoothIntent);
    }

    private void filterBoothsAction() {
        Intent filterBoothsIntent = new Intent(configureBoothsActivityContext, FilterBooths.class);
        filterBoothsIntent.putExtra("show", show);
        startActivity(filterBoothsIntent);
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
        populateBoothsForShowTable();
    }

    private void sortBoothListByPrice() {
        if (lastSortedBy.equalsIgnoreCase("boothPrice")) {
            Collections.reverse(boothWithTagsList);
        } else {
            lastSortedBy = "boothPrice";
            Collections.sort(boothWithTagsList, new Comparator<BoothWithTags>() {
                public int compare(BoothWithTags boothWithTags1, BoothWithTags boothWithTags2) {
                    return (boothWithTags1.booth.getPrice().compareTo(boothWithTags2.booth.getPrice()));
                }
            });
        }
        populateBoothsForShowTable();
    }

    private void sortBoothListByAvailability() {
        if (lastSortedBy.equalsIgnoreCase("boothAvailability")) {
            Collections.reverse(boothWithTagsList);
        } else {
            lastSortedBy = "boothAvailability";
            Collections.sort(boothWithTagsList, new Comparator<BoothWithTags>() {
                public int compare(BoothWithTags boothWithTags1, BoothWithTags boothWithTags2) {
                    return (boothWithTags1.booth.getCode().compareTo(boothWithTags2.booth.getCode()));
                }
            });
        }
        populateBoothsForShowTable();
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
        populateBoothsForShowTable();
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
        populateBoothsForShowTable();
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
        populateBoothsForShowTable();
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
            if (null == boothWithTags.getSizeTag() || boothWithTags.getSizeTag().equalsIgnoreCase("")){
                boothWithTags.setSizeTag("N/A");
            }
            if (null == boothWithTags.getAreaTag() || boothWithTags.getAreaTag().equalsIgnoreCase("")){
                boothWithTags.setAreaTag("N/A");
            }
            if (null == boothWithTags.getTypeTag() || boothWithTags.getTypeTag().equalsIgnoreCase("")){
                boothWithTags.setTypeTag("N/A");
            }
        }
    }

    private class GetShowBoothsTask extends AsyncTask<Void, Void, Void> {
        //////////PRIVATELY NECESSARY OBJECTS & UTILITY LISTS ONLY
        private ProgressDialog progressDialog;
        private InventoryConnector inventoryConnector;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(configureBoothsActivityContext);
            progressDialog.setMessage("Loading Booths...");
            progressDialog.show();
            ///// CLOVER CONNECTIONS and LIST INIT
            boothWithTagsList = new ArrayList<>();
            inventoryConnector = new InventoryConnector(configureBoothsActivityContext, CloverAccount.getAccount(configureBoothsActivityContext), null);
            inventoryConnector.connect();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                ///// FETCH BOOTHS FOR SHOW
                if (inventoryConnector.getItems().size() > 0) {
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
                }
            } catch (RemoteException |
                    BindingException |
                    ServiceException |
                    ClientException e1) {
                Log.e("Clover Excptn; ", e1.getClass().getName() + " : " + e1.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            inventoryConnector.disconnect();
            processBoothWithTagsList();
            populateBoothsForShowTable();
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

    ////// NAVIGATION METHODS
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.configure_booths_drawerlayout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_configure_booths, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_create_new_booth_option) {
            Intent createBoothIntent = new Intent(configureBoothsActivityContext, CreateBooth.class);
            createBoothIntent.putExtra("show", show);
            startActivity(createBoothIntent);
            return true;
        } else if (id == R.id.action_app_settings) {
            Intent applicationSettingsIntent = new Intent(configureBoothsActivityContext, ApplicationSettings.class);
            startActivity(applicationSettingsIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home_btn) {
            Intent homeActivityIntent = new Intent(configureBoothsActivityContext, HomeActivity.class);
            startActivity(homeActivityIntent);
        } else if (id == R.id.nav_show_setup_btn) {
            Intent showSetupIntent = new Intent(configureBoothsActivityContext, TradeShows.class);
            startActivity(showSetupIntent);
        } else if (id == R.id.nav_config_booths_btn) {
            // nothing ; already in activity
        } else if (id == R.id.nav_make_reservation_btn) {
            Intent makeReservationIntent = new Intent(configureBoothsActivityContext, BoothReservationShowSelection.class);
            startActivity(makeReservationIntent);
        } else if (id == R.id.nav_app_settings_btn) {
            Intent applicationSettingsIntent = new Intent(configureBoothsActivityContext, ApplicationSettings.class);
            startActivity(applicationSettingsIntent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.configure_booths_drawerlayout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}