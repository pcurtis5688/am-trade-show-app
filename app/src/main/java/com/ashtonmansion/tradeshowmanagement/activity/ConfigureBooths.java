package com.ashtonmansion.tradeshowmanagement.activity;

import android.accounts.Account;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.ashtonmansion.amtradeshowmanagement.R;
import com.ashtonmansion.tradeshowmanagement.HomeActivity;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v3.base.Reference;
import com.clover.sdk.v3.inventory.Category;
import com.clover.sdk.v3.inventory.InventoryConnector;
import com.clover.sdk.v3.inventory.Item;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Filter;

public class ConfigureBooths extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    //CONTEXT AND UI FIELDS
    private Context configureBoothsActivityContext;
    private String configureBoothsShowNameHeader;
    private TableLayout boothsForShowTable;
    private TextView configureBoothsForShowHeader;
    //CLOVER VARS
    private Account merchantAccount;
    private InventoryConnector inventoryConnector;
    private List<String> refIdStringList;
    private List<Item> boothList;
    //DATA VARS
    private String showID;
    private String showName;

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
        boothsForShowTable = (TableLayout) findViewById(R.id.booths_for_show_table);

        Bundle extrasBundle = getIntent().getExtras();
        if (extrasBundle != null) {
            showID = (String) extrasBundle.get("showid");
            showName = (String) extrasBundle.get("showname");
            configureBoothsShowNameHeader = "Configure Booths - " + showName;
            configureBoothsForShowHeader = (TextView) findViewById(R.id.show_booths_header);
            configureBoothsForShowHeader.setText(configureBoothsShowNameHeader);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        GetShowBoothsTask getShowBoothsTask = new GetShowBoothsTask();
        getShowBoothsTask.execute();
    }

    private void populateBoothsForShowTable() {
        boothsForShowTable.removeAllViews();
        populateBoothHeaderRow();
        if (boothList != null && boothList.size() > 0) {
            for (Item booth : boothList) {
                TableRow newBoothRow = new TableRow(configureBoothsActivityContext);
                final Item finalizedBoothItem = booth;
                TextView boothNumberTv = new TextView(configureBoothsActivityContext);
                TextView boothSizeTv = new TextView(configureBoothsActivityContext);
                TextView boothCustomerTv = new TextView(configureBoothsActivityContext);
                TextView boothPriceTv = new TextView(configureBoothsActivityContext);
                Button editBoothButton = new Button(configureBoothsActivityContext);

                boothNumberTv.setText(booth.getSku());
                boothSizeTv.setText("SET SIZES");
                boothCustomerTv.setText("CUSTOMER OR AVAIL");

                long boothPriceLong = booth.getPrice();
                String priceLongString = Long.toString(boothPriceLong);
                String cleanString = priceLongString.replaceAll("[$,.]", "");
                double parsedBoothPriceDouble = Double.parseDouble(cleanString);

                NumberFormat numberFormatter = NumberFormat.getCurrencyInstance(Locale.US);
                String formattedPrice = numberFormatter.format(parsedBoothPriceDouble / 100.0);
                boothPriceTv.setText(formattedPrice);

                editBoothButton.setText(getResources().getString(R.string.configure_show_booths_edit_booth_btn_text));
                editBoothButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        editBoothAction(finalizedBoothItem);
                    }
                });

                newBoothRow.addView(boothNumberTv);
                newBoothRow.addView(boothSizeTv);
                newBoothRow.addView(boothCustomerTv);
                newBoothRow.addView(boothPriceTv);
                newBoothRow.addView(editBoothButton);

                boothsForShowTable.addView(newBoothRow);
            }
        }
        TableRow addBoothButtonRow = new TableRow(configureBoothsActivityContext);
        Button addBoothButton = new Button(configureBoothsActivityContext);
        addBoothButton.setText(getResources().getString(R.string.action_create_new_booth_string));
        addBoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent createBoothIntent = new Intent(configureBoothsActivityContext, CreateBooth.class);
                startActivity(createBoothIntent);
            }
        });
        addBoothButton.setLayoutParams(new TableRow.LayoutParams(4));
        addBoothButtonRow.addView(addBoothButton);
        boothsForShowTable.addView(addBoothButtonRow);
    }

    private void populateBoothHeaderRow() {
        TableRow boothsForShowTableHeaderRow = new TableRow(configureBoothsActivityContext);
        TextView boothNumberHeaderTv = new TextView(configureBoothsActivityContext);
        TextView boothSizeHeaderTv = new TextView(configureBoothsActivityContext);
        TextView boothCustomerHeaderTv = new TextView(configureBoothsActivityContext);
        TextView boothPriceHeaderTv = new TextView(configureBoothsActivityContext);
        boothNumberHeaderTv.setText(getResources().getString(R.string.configure_show_booths_number_header));
        boothSizeHeaderTv.setText(getResources().getString(R.string.configure_show_booths_size_header));
        boothCustomerHeaderTv.setText(getResources().getString(R.string.configure_show_booths_customer_header));
        boothPriceHeaderTv.setText(getResources().getString(R.string.configure_show_booths_price_header));

        Button boothFilterButton = new Button(configureBoothsActivityContext);
        boothFilterButton.setText(getResources().getString(R.string.configure_show_booths_filter_btn_text));
        boothFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filterBoothsAction();
            }
        });

        boothsForShowTableHeaderRow.addView(boothNumberHeaderTv);
        boothsForShowTableHeaderRow.addView(boothSizeHeaderTv);
        boothsForShowTableHeaderRow.addView(boothCustomerHeaderTv);
        boothsForShowTableHeaderRow.addView(boothPriceHeaderTv);
        boothsForShowTableHeaderRow.addView(boothFilterButton);
        boothsForShowTable.addView(boothsForShowTableHeaderRow);
    }

    private void editBoothAction(Item booth) {
        Intent editBoothIntent = new Intent(configureBoothsActivityContext, EditBooth.class);
        editBoothIntent.putExtra("booth", booth);
        startActivity(editBoothIntent);
    }

    private void filterBoothsAction() {
        Intent filterBoothsIntent = new Intent(configureBoothsActivityContext, FilterBooths.class);
        filterBoothsIntent.putExtra("showid", showID);
        startActivity(filterBoothsIntent);
    }

    ////////////////NAVIGATION METHODS//////////////////////////
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
            startActivity(createBoothIntent);
            return true;
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
        } else if (id == R.id.nav_reports_queries_btn) {
            Intent reportsIntent = new Intent(configureBoothsActivityContext, Reports.class);
            startActivity(reportsIntent);
        } else if (id == R.id.nav_make_reservation_btn) {
            Intent makeReservationIntent = new Intent(configureBoothsActivityContext, MakeReservation.class);
            startActivity(makeReservationIntent);
        } else if (id == R.id.nav_email_reservation_confirmation_btn) {
            Intent emailConfirmationIntent = new Intent(configureBoothsActivityContext, EmailConfirmation.class);
            startActivity(emailConfirmationIntent);
        } else if (id == R.id.nav_advertising_sales_btn) {
            Intent advertisingSalesIntent = new Intent(configureBoothsActivityContext, AdvertisingSales.class);
            startActivity(advertisingSalesIntent);
        } else if (id == R.id.nav_general_tix_sales_btn) {
            Intent generalTicketSalesIntent = new Intent(configureBoothsActivityContext, GeneralTicketSales.class);
            startActivity(generalTicketSalesIntent);
        } else if (id == R.id.nav_special_events_sales_btn) {
            Intent specialEventSalesIntent = new Intent(configureBoothsActivityContext, SpecialEventSales.class);
            startActivity(specialEventSalesIntent);
        } else if (id == R.id.nav_merchandise_sales_btn) {
            Intent merchandiseSalesIntent = new Intent(configureBoothsActivityContext, MerchandiseSales.class);
            startActivity(merchandiseSalesIntent);
        } else if (id == R.id.nav_app_settings_btn) {
            Intent appSettingsIntent = new Intent(configureBoothsActivityContext, AppSettings.class);
            startActivity(appSettingsIntent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.configure_booths_drawerlayout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class GetShowBoothsTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog = new ProgressDialog(configureBoothsActivityContext);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Loading Booths...");
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                merchantAccount = CloverAccount.getAccount(configureBoothsActivityContext);
                inventoryConnector = new InventoryConnector(configureBoothsActivityContext, merchantAccount, null);
                inventoryConnector.connect();
                refIdStringList = new ArrayList<>();
                List<Category> categoryList = inventoryConnector.getCategories();
                Category showCat = null;
                for (Category category : categoryList) {
                    if (category.getName().equalsIgnoreCase(showName)) {
                        showCat = category;
                    }
                }
                List<Reference> itemReferenceList = showCat.getItems();
                if (itemReferenceList != null && itemReferenceList.size() > 0) {
                    for (Reference reference : itemReferenceList) {
                        refIdStringList.add(reference.getId());
                        boothList = new ArrayList<>();
                        boothList.add(inventoryConnector.getItem(reference.getId()));
                    }
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
            progressDialog.dismiss();
            populateBoothsForShowTable();
        }
    }
}