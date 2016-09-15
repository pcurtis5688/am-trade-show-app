package com.ashtonmansion.tradeshowmanagement.activity;

import android.accounts.Account;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BoothReservationShowSelection extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    /////////////ACTIVITY AND UI VARS
    private Context boothReservationActivityContext;
    private TableRow rowContainerForTables;
    private TableLayout showSelectionTable;
    private TableLayout boothAvailabilityTable;
    /////////////DATA VARS
    private List<Category> showList;
    private List<String> referenceIdStringList;
    private String chosenShowId;
    private String chosenShowName;
    private Category chosenShowCategoryObj;
    private List<Item> boothListWithCategories;
    private List<Item> filteredBoothList;
    private Item selectedBooth;
    /////////////CLOVER VARS
    private Account merchantAccount;
    private InventoryConnector inventoryConnector;

    ////////////////////////////UI/INITIATION WORK////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ///////////UI WORK/////////////////////////////////////
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booth_reservation_show_selection);
        Toolbar toolbar = (Toolbar) findViewById(R.id.booth_selection_show_selection_toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.make_reservation_drawerlayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_make_reservation);
        navigationView.setNavigationItemSelectedListener(this);

        ///////////DATA WORK////////////////////////////////////
        boothReservationActivityContext = this;
        showSelectionTable = (TableLayout) findViewById(R.id.booth_reservation_show_select_table);

        GetShowsForBoothSelection getShowsForBoothSelection = new GetShowsForBoothSelection();
        getShowsForBoothSelection.execute();
    }

    private class GetShowsForBoothSelection extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(boothReservationActivityContext);
            progressDialog.setMessage("Loading Shows...");
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                ///////////CLOVER CONNECT
                merchantAccount = CloverAccount.getAccount(boothReservationActivityContext);
                inventoryConnector = new InventoryConnector(boothReservationActivityContext, merchantAccount, null);
                inventoryConnector.connect();
                ///////////GET SHOW LIST (CATEGORY LIST) FOR BOOTH SELECTION
                showList = inventoryConnector.getCategories();

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
            populateShowSelectionTable();
            progressDialog.dismiss();
        }
    }

    private void populateShowSelectionTable() {
        for (Category show : showList) {
            final String finalizedShowID = show.getId();
            final Category finalizedShowObject = show;
            List<String> decoupledShowNameArr = Arrays.asList(show.getName().split(","));
            String showName = decoupledShowNameArr.get(0);
            String showDate = decoupledShowNameArr.get(1);
            String showLocation = decoupledShowNameArr.get(2);
            String showNameForUser = showName + " (" + showDate + " - " + showLocation + ")";

            TableRow newShowSelectionRow = new TableRow(boothReservationActivityContext);

            TextView showSelectionNameTv = new TextView(boothReservationActivityContext);
            showSelectionNameTv.setText(showNameForUser);
            showSelectionNameTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);

            Button showSelectButton = new Button(boothReservationActivityContext);
            showSelectButton.setText(getResources().getString(R.string.select_show_button_text));
            showSelectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectShowForReservation(finalizedShowObject);
                }
            });

            newShowSelectionRow.addView(showSelectionNameTv);
            newShowSelectionRow.addView(showSelectButton);

            showSelectionTable.addView(newShowSelectionRow);
        }
    }

    private void selectShowForReservation(Category showObj) {
        Intent boothSelectionIntent = new Intent(boothReservationActivityContext, BoothReservation.class);
        boothSelectionIntent.putExtra("show", showObj);
        startActivity(boothSelectionIntent);
    }

    ////////////////NAVIGATION METHODS//////////////////////////
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.make_reservation_drawerlayout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
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
            Intent homeIntent = new Intent(boothReservationActivityContext, HomeActivity.class);
            startActivity(homeIntent);
        } else if (id == R.id.nav_show_setup_btn) {
            Intent showSetupIntent = new Intent(boothReservationActivityContext, TradeShows.class);
            startActivity(showSetupIntent);
        } else if (id == R.id.nav_config_booths_btn) {
            Intent configureBoothsIntent = new Intent(boothReservationActivityContext, ConfigureBoothsShowSelection.class);
            startActivity(configureBoothsIntent);
        } else if (id == R.id.nav_make_reservation_btn) {
            //// nothing; already in activity
        } else if (id == R.id.nav_app_settings_btn){
            Intent applicationSettingsIntent = new Intent(boothReservationActivityContext, ApplicationSettings.class);
            startActivity(applicationSettingsIntent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.make_reservation_drawerlayout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}


