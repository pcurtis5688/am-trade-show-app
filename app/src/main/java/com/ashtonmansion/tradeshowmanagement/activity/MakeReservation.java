package com.ashtonmansion.tradeshowmanagement.activity;

import android.accounts.Account;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.ashtonmansion.amtradeshowmanagement.R;
import com.ashtonmansion.tradeshowmanagement.HomeActivity;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v3.inventory.InventoryConnector;
import com.clover.sdk.v3.inventory.Item;

import java.util.List;

public class MakeReservation extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private Context makeReservationActivityContext;
    private List<Item> boothList;
    private InventoryConnector inventoryConnector;
    private Account merchantAccount;
    private TableLayout boothTable;

    public void fetchData() {
        GetAvailableBoothsTask getAvailableBoothsTask = new GetAvailableBoothsTask();
        getAvailableBoothsTask.execute();
    }

    private void viewBoothDetail(Item booth) {
        Intent viewBoothDetailIntent = new Intent(makeReservationActivityContext, ViewBoothDetail.class);
        viewBoothDetailIntent.putExtra("booth", booth);
        startActivity(viewBoothDetailIntent);
    }

    ////////////////////////////UI/INITIATION WORK////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ///////////UI WORK/////////////////////////////////////
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_reservation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.make_reservation_toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.make_reservation_drawerlayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_make_reservation);
        navigationView.setNavigationItemSelectedListener(this);

        ///////////DATA WORK////////////////////////////////////
        makeReservationActivityContext = this;
        merchantAccount = CloverAccount.getAccount(makeReservationActivityContext);
        inventoryConnector = new InventoryConnector(makeReservationActivityContext, merchantAccount, null);
        boothTable = (TableLayout) findViewById(R.id.make_reservation_booth_table);
        fetchData();
    }

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
            Intent homeIntent = new Intent(makeReservationActivityContext, HomeActivity.class);
            startActivity(homeIntent);
        } else if (id == R.id.nav_show_setup_btn) {
            Intent showSetupIntent = new Intent(makeReservationActivityContext, ShowSetup.class);
            startActivity(showSetupIntent);
        } else if (id == R.id.nav_config_booths_btn) {
            Intent configureBoothsIntent = new Intent(makeReservationActivityContext, ConfigureBooths.class);
            startActivity(configureBoothsIntent);
        } else if (id == R.id.nav_reports_queries_btn) {
            Intent reportsIntent = new Intent(makeReservationActivityContext, Reports.class);
            startActivity(reportsIntent);
        } else if (id == R.id.nav_make_reservation_btn) {
            //// nothing; already in activity
        } else if (id == R.id.nav_email_reservation_confirmation_btn) {
            Intent emailConfirmationIntent = new Intent(makeReservationActivityContext, EmailConfirmation.class);
            startActivity(emailConfirmationIntent);
        } else if (id == R.id.nav_advertising_sales_btn) {
            Intent advertisingSalesIntent = new Intent(makeReservationActivityContext, AdvertisingSales.class);
            startActivity(advertisingSalesIntent);
        } else if (id == R.id.nav_general_tix_sales_btn) {
            Intent generalTicketSalesIntent = new Intent(makeReservationActivityContext, GeneralTicketSales.class);
            startActivity(generalTicketSalesIntent);
        } else if (id == R.id.nav_special_events_sales_btn) {
            Intent specialEventSalesIntent = new Intent(makeReservationActivityContext, SpecialEventSales.class);
            startActivity(specialEventSalesIntent);
        } else if (id == R.id.nav_merchandise_sales_btn) {
            Intent merchandiseSalesIntent = new Intent(makeReservationActivityContext, MerchandiseSales.class);
            startActivity(merchandiseSalesIntent);
        } else if (id == R.id.nav_app_settings_btn) {
            Intent appSettingsIntent = new Intent(makeReservationActivityContext, AppSettings.class);
            startActivity(appSettingsIntent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.make_reservation_drawerlayout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class GetAvailableBoothsTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                boothList = inventoryConnector.getItems();
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
            for (final Item booth : boothList) {
                TableRow boothRow = new TableRow(makeReservationActivityContext);
                TextView boothIDTV = new TextView(makeReservationActivityContext);
                TextView boothName = new TextView(makeReservationActivityContext);
                boothIDTV.setText(booth.getId());
                boothName.setText(booth.getName());
                boothRow.addView(boothIDTV);
                boothRow.addView(boothName);
                boothRow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        viewBoothDetail(booth);
                    }
                });
                boothTable.addView(boothRow);
            }
        }
    }
}
