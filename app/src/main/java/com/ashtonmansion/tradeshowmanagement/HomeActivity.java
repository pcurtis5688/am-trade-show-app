package com.ashtonmansion.tradeshowmanagement;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.ashtonmansion.amtradeshowmanagement.R;
import com.ashtonmansion.tradeshowmanagement.activity.AppSettings;
import com.ashtonmansion.tradeshowmanagement.activity.EmailConfirmation;
import com.ashtonmansion.tradeshowmanagement.activity.MakeReservation;
import com.ashtonmansion.tradeshowmanagement.activity.ShowSetup;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private Context homeActivityContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeActivityContext = this;
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
        int id = item.getItemId();

        if (id == R.id.nav_show_setup_btn) {
            Intent showSetupIntent = new Intent(homeActivityContext, ShowSetup.class);
            startActivity(showSetupIntent);
        } else if (id == R.id.nav_config_booths_btn) {
            //// TODO: 8/27/2016
        } else if (id == R.id.nav_reports_queries_btn) {
            //// TODO: 8/27/2016  
        } else if (id == R.id.nav_make_reservation_btn) {
            Intent makeReservationIntent = new Intent(homeActivityContext, MakeReservation.class);
            startActivity(makeReservationIntent);
        } else if (id == R.id.nav_email_reservation_confirmation_btn) {
            Intent emailConfirmationIntent = new Intent(homeActivityContext, EmailConfirmation.class);
            startActivity(emailConfirmationIntent);
        } else if (id == R.id.nav_advertising_sales_btn) {
            //// TODO: 8/27/2016  
        } else if (id == R.id.nav_general_tix_sales_btn) {
            //// TODO: 8/27/2016  
        } else if (id == R.id.nav_special_events_sales_btn) {
            //// TODO: 8/27/2016  
        } else if (id == R.id.nav_merchandise_sales_btn) {
            //// TODO: 8/27/2016  
        } else if (id == R.id.nav_app_settings_btn) {
            Intent appSettingsIntent = new Intent(homeActivityContext, AppSettings.class);
            startActivity(appSettingsIntent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void makeReservation(View view) {
        Intent makeReservationIntent = new Intent(this, MakeReservation.class);
        startActivity(makeReservationIntent);
    }
}
