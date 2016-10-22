package com.ashtonmansion.tsmanagement2.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.TextView;

import com.ashtonmansion.tsmanagement2.R;
import com.ashtonmansion.tsmanagement2.util.GlobalUtils;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private Context homeActivityContext;
    private boolean appHasValidPermissions;
    private TextView cloverStatusTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ////// UI WORK / INSTANTIATION
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.home_drawerlayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_home);
        navigationView.setNavigationItemSelectedListener(this);

        /////// DATA WORK - SET CONTEXT & CHECK PERMISSIONS
        homeActivityContext = this;
        appHasValidPermissions = GlobalUtils.getPermissionsValid(this, getApplicationContext());
        TextView cloverConnTv = (TextView) findViewById(R.id.clover_connectivity_textview);
        cloverConnTv.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        cloverConnTv.setText(getResources().getString(R.string.clover_connectivity_string));
        cloverStatusTv = (TextView) findViewById(R.id.connectivity_status_tv);
        cloverStatusTv.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        if (appHasValidPermissions) {
            Log.d("HomeActivity", "Invalid Credentials");
            cloverStatusTv.setTextAppearance(homeActivityContext, R.style.clover_connectivity_unvailable_style);
            cloverStatusTv.setText(R.string.clover_connectivity_unavailable_string);
        } else {
            Log.d("HomeActivity", "Valid Credentials");
            cloverStatusTv.setTextAppearance(homeActivityContext, R.style.clover_connectivity_available_style);
            cloverStatusTv.setText(R.string.clover_connectivity_available_string);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        appHasValidPermissions = GlobalUtils.getPermissionsValid(this, getApplicationContext());
        if (!appHasValidPermissions) {
            Log.d("HomeActivity", "Valid Credentials");
            cloverStatusTv.setTextAppearance(homeActivityContext, R.style.clover_connectivity_available_style);
        } else {
            Log.d("HomeActivity", "Invalid Credentials");
            cloverStatusTv.setTextAppearance(homeActivityContext, R.style.clover_connectivity_unvailable_style);
        }
    }

    ////////////////NAVIGATION METHODS
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.home_drawerlayout);
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
            Intent applicationSettingsIntent = new Intent(homeActivityContext, ApplicationSettings.class);
            startActivity(applicationSettingsIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_show_setup_btn) {
            Intent showSetupIntent = new Intent(homeActivityContext, TradeShows.class);
            startActivity(showSetupIntent);
        } else if (id == R.id.nav_config_booths_btn) {
            Intent configBoothsIntent = new Intent(homeActivityContext, ConfigureBoothsShowSelection.class);
            startActivity(configBoothsIntent);
        } else if (id == R.id.nav_make_reservation_btn) {
            Intent makeReservationIntent = new Intent(homeActivityContext, BoothReservationShowSelection.class);
            startActivity(makeReservationIntent);
        } else if (id == R.id.nav_app_settings_btn) {
            Intent applicationSettingsIntent = new Intent(homeActivityContext, ApplicationSettings.class);
            startActivity(applicationSettingsIntent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.home_drawerlayout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
