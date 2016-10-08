package com.ashtonmansion.tsmanagement1.activity;

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

import com.ashtonmansion.tsmanagement1.R;
import com.ashtonmansion.tsmanagement1.util.GlobalUtils;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v3.inventory.InventoryConnector;
import com.clover.sdk.v3.inventory.Tag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigureBoothsShowSelection extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    ///// ACTIVITY CONTEXT AND UI VARS
    private Context configureBoothsShowSelectionActivityContext;
    private TableLayout configureBoothsShowSelectionTable;
    private int platformFontStyle1;
    ///// DATA VARS
    private List<Tag> showList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ///////////UI WORK/////////////////////////////////////
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_booths_show_selection);
        Toolbar toolbar = (Toolbar) findViewById(R.id.configure_booths_show_selection_toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.configure_booths_show_selection_drawerlayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_configure_booths_show_selection);
        navigationView.setNavigationItemSelectedListener(this);

        ///////////DATA WORK////////////////////////////////////
        configureBoothsShowSelectionActivityContext = this;
        configureBoothsShowSelectionTable = (TableLayout) findViewById(R.id.configure_booths_show_selection_table);
        GetShowListTask getShowListTask = new GetShowListTask();
        getShowListTask.execute();
        handleSizing();
    }

    private void populateShowSelectionTable() {
        if (showList.size() > 0) {
            for (Tag showTag : showList) {
                final Tag finalizedShowTag = showTag;

                ///// HANDLE THE SHOW NAME
                List<String> decoupledShowNameArr = Arrays.asList(showTag.getName().split(","));
                String showName = decoupledShowNameArr.get(0);
                String showDate = decoupledShowNameArr.get(1);
                String showLocation = decoupledShowNameArr.get(2);
                String showNameForUser = getResources().getString(R.string.show_name_for_user_string, showName, showDate, showLocation);

                ///// TEXT VIEW AND BUTTON CONFIGURATION
                TableRow newShowSelectionRow = new TableRow(configureBoothsShowSelectionActivityContext);
                Button showSelectionButton = new Button(configureBoothsShowSelectionActivityContext);

                ///// HANDLE FONTS
                showSelectionButton.setTextAppearance(configureBoothsShowSelectionActivityContext, R.style.trade_show_row_select_buttons);

                ///// SET SHOW NAME AND BUTTON TEXT
                showSelectionButton.setText(showNameForUser);
                showSelectionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        configureBoothsSelectShowAction(finalizedShowTag);
                    }
                });

                ///// ADD NEW SHOW ROW TO SHOW TABLE
                newShowSelectionRow.addView(showSelectionButton);
                configureBoothsShowSelectionTable.addView(newShowSelectionRow);
            }
        } else {
            ////// HANDLE CASE - NO EXISTING SHOWS
            TextView showSelectionNoShowsWarningTv = new TextView(configureBoothsShowSelectionActivityContext);
            showSelectionNoShowsWarningTv.setText(getResources().getString(R.string.no_trade_shows_available_string));
            showSelectionNoShowsWarningTv.setTextAppearance(configureBoothsShowSelectionActivityContext, R.style.no_shows_style);
            showSelectionNoShowsWarningTv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            TableRow showSelectionNoShowsWarningRow = new TableRow(configureBoothsShowSelectionActivityContext);
            TableRow.LayoutParams params = new TableRow.LayoutParams();
            params.span = 4;
            params.topMargin = 32;
            showSelectionNoShowsWarningRow.addView(showSelectionNoShowsWarningTv, params);
            configureBoothsShowSelectionTable.addView(showSelectionNoShowsWarningRow);
        }
    }

    private void configureBoothsSelectShowAction(Tag showTag) {
        Intent configureBoothsForShowIntent = new Intent(configureBoothsShowSelectionActivityContext, ConfigureBooths.class);
        configureBoothsForShowIntent.putExtra("show", showTag);
        startActivity(configureBoothsForShowIntent);
    }

    private void handleSizing() {
        if (((GlobalUtils.determinePlatform(getApplicationContext())).equalsIgnoreCase("station"))) {
            platformFontStyle1 = R.style.station_flagship_font;
        } else {
            platformFontStyle1 = R.style.mobile_flagship_font;
        }
    }

    ////////////////NAVIGATION METHODS//////////////////////////
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.configure_booths_show_selection_drawerlayout);
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
            Intent applicationSettingsIntent = new Intent(configureBoothsShowSelectionActivityContext, ApplicationSettings.class);
            startActivity(applicationSettingsIntent);
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
            Intent homeActivityIntent = new Intent(configureBoothsShowSelectionActivityContext, HomeActivity.class);
            startActivity(homeActivityIntent);
        } else if (id == R.id.nav_show_setup_btn) {
            Intent showSetupIntent = new Intent(configureBoothsShowSelectionActivityContext, TradeShows.class);
            startActivity(showSetupIntent);
        } else if (id == R.id.nav_config_booths_btn) {
            // nothing ; already in activity
        } else if (id == R.id.nav_make_reservation_btn) {
            Intent makeReservationIntent = new Intent(configureBoothsShowSelectionActivityContext, BoothReservationShowSelection.class);
            startActivity(makeReservationIntent);
        } else if (id == R.id.nav_app_settings_btn) {
            Intent applicationSettingsIntent = new Intent(configureBoothsShowSelectionActivityContext, ApplicationSettings.class);
            startActivity(applicationSettingsIntent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.configure_booths_show_selection_drawerlayout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class GetShowListTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;
        /////CLOVER CONNECT
        private InventoryConnector inventoryConnector;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(configureBoothsShowSelectionActivityContext);
            progressDialog.setMessage("Loading Shows...");
            progressDialog.show();
            showList = new ArrayList<>();
            ////// INIT CLOVER CONNECTIONS
            inventoryConnector = new InventoryConnector(configureBoothsShowSelectionActivityContext, CloverAccount.getAccount(configureBoothsShowSelectionActivityContext), null);
            inventoryConnector.connect();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                for (Tag currentTag : inventoryConnector.getTags()) {
                    if (currentTag.getName().contains(" [Show]")) {
                        showList.add(currentTag);
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
            populateShowSelectionTable();
            progressDialog.dismiss();
        }
    }
}