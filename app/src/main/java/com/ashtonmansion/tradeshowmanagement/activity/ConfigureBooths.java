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
import com.clover.sdk.v3.inventory.Tag;
import com.clover.sdk.v3.order.Order;
import com.clover.sdk.v3.order.OrderConnector;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    private CustomerConnector customerConnector;
    private List<Item> boothList;
    //DATA VARS
    private String showID;
    private String showName;
    private Category showCategory;

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
            Category passedCategoryObject = (Category) extrasBundle.get("showcat");
            if (null != passedCategoryObject) {
                showID = passedCategoryObject.getId();
                showName = passedCategoryObject.getName();
            }
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
                TextView boothNameTv = new TextView(configureBoothsActivityContext);
                TextView boothNumberTv = new TextView(configureBoothsActivityContext);
                TextView boothPriceTv = new TextView(configureBoothsActivityContext);
                TextView boothSizeTv = new TextView(configureBoothsActivityContext);
                TextView boothAreaTv = new TextView(configureBoothsActivityContext);
                TextView boothCategoryTv = new TextView(configureBoothsActivityContext);
                TextView boothCustomerTv = new TextView(configureBoothsActivityContext);
                Button editBoothButton = new Button(configureBoothsActivityContext);
                Tag sizeTag = null;
                Tag areaTag = null;
                Tag categoryTag = null;
                /////////////////////
                for (Tag currentTag : booth.getTags()) {
                    if (currentTag.getName().substring(0, 4).equalsIgnoreCase("size")) {
                        sizeTag = currentTag;
                        boothSizeTv.setText(sizeTag.getName());
                    } else if (currentTag.getName().substring(0, 4).equalsIgnoreCase("area")) {
                        areaTag = currentTag;
                        boothAreaTv.setText(areaTag.getName());
                    } else if (currentTag.getName().substring(0, 8).equalsIgnoreCase("category")) {
                        categoryTag = currentTag;
                        boothCategoryTv.setText(categoryTag.getName());
                    }
                }
                if (null != sizeTag) boothSizeTv.setText(sizeTag.getName());
                else Log.d("booth size tag :", "null");
                if (null != areaTag) boothAreaTv.setText(areaTag.getName());
                else Log.d("booth area tag :", "null");
                if (null != categoryTag) boothCategoryTv.setText(categoryTag.getName());
                else Log.d("booth cat tag :", "null");
                //////////////////////
                boothNumberTv.setText(booth.getSku());
                //// TODO: 9/5/2016 fix custmoer
                boothCustomerTv.setText("customerhere");
                ////////////////////PRICE BELOW
                long boothPriceLong = booth.getPrice();
                String priceLongString = Long.toString(boothPriceLong);
                String cleanString = priceLongString.replaceAll("[$,.]", "");
                double parsedBoothPriceDouble = Double.parseDouble(cleanString);
                NumberFormat numberFormatter = NumberFormat.getCurrencyInstance(Locale.US);
                String formattedPrice = numberFormatter.format(parsedBoothPriceDouble / 100.0);
                boothPriceTv.setText(formattedPrice);
                /////////SET UP EDIT BOOTH BUTTON ON END OF TABLE
                final Item finalizedBoothItem = booth;
                editBoothButton.setText(getResources().getString(R.string.configure_show_booths_edit_booth_btn_text));
                editBoothButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        editBoothAction(finalizedBoothItem);
                    }
                });
                ////////ADD NEW TEXTVIEW TO ROW AND ROW TO TABLE
                newBoothRow.addView(boothNumberTv);
                newBoothRow.addView(boothPriceTv);
                newBoothRow.addView(boothSizeTv);
                newBoothRow.addView(boothAreaTv);
                newBoothRow.addView(boothCategoryTv);
                newBoothRow.addView(boothCustomerTv);
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
                createBoothIntent.putExtra("showid", showID);
                createBoothIntent.putExtra("showname", showName);
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
        TextView boothPriceHeaderTv = new TextView(configureBoothsActivityContext);
        TextView boothSizeHeaderTv = new TextView(configureBoothsActivityContext);
        TextView boothAreaHeaderTv = new TextView(configureBoothsActivityContext);
        TextView boothCategoryHeaderTv = new TextView(configureBoothsActivityContext);
        TextView boothCustomerHeaderTv = new TextView(configureBoothsActivityContext);

        boothNumberHeaderTv.setText(getResources().getString(R.string.configure_show_booths_number_header));
        boothPriceHeaderTv.setText(getResources().getString(R.string.configure_show_booths_price_header));
        boothSizeHeaderTv.setText(getResources().getString(R.string.configure_show_booths_size_header));
        boothAreaHeaderTv.setText(getResources().getString(R.string.configure_show_booths_area_header));
        boothCategoryHeaderTv.setText(getResources().getString(R.string.configure_show_booths_category_header));
        boothCustomerHeaderTv.setText(getResources().getString(R.string.configure_show_booths_customer_header));

        Button boothFilterButton = new Button(configureBoothsActivityContext);
        boothFilterButton.setText(getResources().getString(R.string.configure_show_booths_filter_btn_text));
        boothFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filterBoothsAction();
            }
        });

        boothsForShowTableHeaderRow.addView(boothNumberHeaderTv);
        boothsForShowTableHeaderRow.addView(boothPriceHeaderTv);
        boothsForShowTableHeaderRow.addView(boothSizeHeaderTv);
        boothsForShowTableHeaderRow.addView(boothAreaHeaderTv);
        boothsForShowTableHeaderRow.addView(boothCategoryHeaderTv);
        boothsForShowTableHeaderRow.addView(boothCustomerHeaderTv);
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
            createBoothIntent.putExtra("showid", showID);
            createBoothIntent.putExtra("showname", showName);
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
            Intent makeReservationIntent = new Intent(configureBoothsActivityContext, BoothReservation.class);
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
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.configure_booths_drawerlayout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class GetShowBoothsTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog = new ProgressDialog(configureBoothsActivityContext);
        //////////PRIVATELY NECESSARY OBJECTS & UTILITY LISTS ONLY
        private List<Reference> boothReferenceList;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Loading Booths...");
            progressDialog.show();
            boothList = new ArrayList<>();
            showCategory = null;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                merchantAccount = CloverAccount.getAccount(configureBoothsActivityContext);
                inventoryConnector = new InventoryConnector(configureBoothsActivityContext, merchantAccount, null);
                inventoryConnector.connect();
                ///////////////////possibly do this in one transaction later on


                //////////////FIND CATEGORY IN CLOVER & POPULATE BOOTH REFERENCE LIST
                List<Category> categoryList = inventoryConnector.getCategories();
                List<Reference> boothReferenceList = new ArrayList<>();
                for (Category category : categoryList) {
                    if (category.getId().equalsIgnoreCase(showID)) {
                        showCategory = category;
                        if (showCategory.hasItems()) boothReferenceList = showCategory.getItems();
                    }
                }

                //////////////FIND CATEGORY IN CLOVER & POPULATE BOOTH REFERENCE LIST
                for (Reference boothRef : boothReferenceList) {
                    Item currentBooth = inventoryConnector.getItem(boothRef.getId());
                    currentBooth.setTags(inventoryConnector.getTagsForItem(currentBooth.getId()));
                    boothList.add(currentBooth);
                }

                //// TODO: 9/6/2016 find customer for each booth
                //customerConnector = new CustomerConnector(configureBoothsActivityContext, merchantAccount, null);
                //  customerConnector.getCustomer().getOrders()
                //getBoothCustomer(boothRef.getId())??
                //OrderConnector orderConnector = new OrderConnector(configureBoothsActivityContext, merchantAccount, null);
                //Item itemtest = new Item();
                //Order orderTest = new Order();
                // orderTest
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
            populateBoothsForShowTable();
            progressDialog.dismiss();
        }
    }
}