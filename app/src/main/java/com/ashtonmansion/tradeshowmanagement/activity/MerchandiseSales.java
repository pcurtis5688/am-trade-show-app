package com.ashtonmansion.tradeshowmanagement.activity;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.ashtonmansion.amtradeshowmanagement.R;
import com.ashtonmansion.tradeshowmanagement.HomeActivity;

public class MerchandiseSales extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private Context merchandiseSalesActivityContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ///////////UI WORK/////////////////////////////////////
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchandise_sales);
        Toolbar toolbar = (Toolbar) findViewById(R.id.merchandise_sales_toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.merchandise_sales_drawerlayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_merchandise_sales);
        navigationView.setNavigationItemSelectedListener(this);

        ///////////DATA WORK////////////////////////////////////
        merchandiseSalesActivityContext = this;
        //// TODO: 8/28/2016 begin here
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.merchandise_sales_drawerlayout);
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
            Intent homeActivityIntent = new Intent(merchandiseSalesActivityContext, HomeActivity.class);
            startActivity(homeActivityIntent);
        } else if (id == R.id.nav_show_setup_btn) {
            Intent showSetupIntent = new Intent(merchandiseSalesActivityContext, TradeShows.class);
            startActivity(showSetupIntent);
        } else if (id == R.id.nav_config_booths_btn) {
            Intent configureBoothsIntent = new Intent(merchandiseSalesActivityContext, ConfigureBooths.class);
            startActivity(configureBoothsIntent);
        } else if (id == R.id.nav_reports_queries_btn) {
            Intent reportsIntent = new Intent(merchandiseSalesActivityContext, Reports.class);
            startActivity(reportsIntent);
        } else if (id == R.id.nav_make_reservation_btn) {
            Intent makeReservationIntent = new Intent(merchandiseSalesActivityContext, MakeReservation.class);
            startActivity(makeReservationIntent);
        } else if (id == R.id.nav_email_reservation_confirmation_btn) {
            Intent emailConfirmationIntent = new Intent(merchandiseSalesActivityContext, EmailConfirmation.class);
            startActivity(emailConfirmationIntent);
        } else if (id == R.id.nav_advertising_sales_btn) {
            Intent advertisingSalesIntent = new Intent(merchandiseSalesActivityContext, AdvertisingSales.class);
            startActivity(advertisingSalesIntent);
        } else if (id == R.id.nav_general_tix_sales_btn) {
            Intent generalTicketSalesIntent = new Intent(merchandiseSalesActivityContext, GeneralTicketSales.class);
            startActivity(generalTicketSalesIntent);
        } else if (id == R.id.nav_special_events_sales_btn) {
            Intent specialEventSalesIntent = new Intent(merchandiseSalesActivityContext, SpecialEventSales.class);
            startActivity(specialEventSalesIntent);
        } else if (id == R.id.nav_merchandise_sales_btn) {
            //nothing ; already in activity
        } else if (id == R.id.nav_app_settings_btn) {
            Intent appSettingsIntent = new Intent(merchandiseSalesActivityContext, AppSettings.class);
            startActivity(appSettingsIntent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.merchandise_sales_drawerlayout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
