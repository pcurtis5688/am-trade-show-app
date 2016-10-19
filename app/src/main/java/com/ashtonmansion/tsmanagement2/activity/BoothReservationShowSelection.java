package com.ashtonmansion.tsmanagement2.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.ashtonmansion.tsmanagement2.R;
import com.ashtonmansion.tsmanagement2.util.GlobalUtils;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v3.inventory.InventoryConnector;
import com.clover.sdk.v3.inventory.Tag;

import java.util.ArrayList;
import java.util.List;

public class BoothReservationShowSelection extends Activity
        implements NavigationView.OnNavigationItemSelectedListener {
    /////ACTIVITY AND UI VARS
    private Context boothReservationShowSelectionActivityContext;
    private TableLayout showSelectionTable;
    private int pageHeaderFontResId;
    private int showSelectionButtonResId;
    /////DATA VARS
    private List<Tag> showList;
    ///// ORDER INFO BEING PASSED TO BOOTH RESERVATION, WILL GRAB CUSTOMER
    private String orderID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ///////////UI WORK/////////////////////////////////////
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booth_reservation_show_selection);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_make_reservation);
        navigationView.setNavigationItemSelectedListener(this);

        ////// OTHER INITIALIZATION
        Intent intent = getIntent();
        Bundle extrasBundle = intent.getExtras();
        if (null != extrasBundle) {
            orderID = (String) extrasBundle.get("orderid");
        }

        ////// SET CONTEXT, ATTACH TO SHOW TABLE, AND POPULATE
        boothReservationShowSelectionActivityContext = this;
        ////// TOAST TO SELECT SHOW IF A PROMPT
        if (orderID != null && !orderID.equalsIgnoreCase("")) {
            Toast.makeText(boothReservationShowSelectionActivityContext, "Please select a show", Toast.LENGTH_SHORT).show();
        }

        handleSizing();
        TextView showSelectionHeaderTv = (TextView) findViewById(R.id.booth_reservation_show_selection_header);
        showSelectionHeaderTv.setTextAppearance(boothReservationShowSelectionActivityContext, pageHeaderFontResId);
        showSelectionTable = (TableLayout) findViewById(R.id.booth_reservation_show_select_table);
        GetShowsForBoothSelection getShowsForBoothSelection = new GetShowsForBoothSelection();
        getShowsForBoothSelection.execute();
    }

    private void populateShowSelectionTable() {
        ///// NON-SHOW TAGS HAVE ALREADY BEEN FILTERED
        if (showList.size() > 0) {
            for (Tag show : showList) {
                final Tag finalizedShowObject = show;

                ///// HANDLE THE SHOW NAME
                List<String> decoupledShowNameArr = GlobalUtils.decoupleShowName(show.getName());
                String showName = decoupledShowNameArr.get(0);
                String showDate = decoupledShowNameArr.get(1);
                String showLocation = decoupledShowNameArr.get(2);
                String showNameForUser = getResources().getString(R.string.show_name_for_user_string, showName, showDate, showLocation);

                ///// TEXT VIEW AND BUTTON CREATION
                TableRow newShowSelectionRow = new TableRow(boothReservationShowSelectionActivityContext);
                Button showSelectButton = new Button(boothReservationShowSelectionActivityContext);

                ///// HANDLE FONTS
                showSelectButton.setTextAppearance(boothReservationShowSelectionActivityContext, R.style.trade_show_row_select_buttons_leftalign);
                TableLayout.LayoutParams tableRowParams =
                        new TableLayout.LayoutParams
                                (TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
                int leftMargin = 20;
                int topMargin = 6;
                int rightMargin = 20;
                int bottomMargin = 6;
                tableRowParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin);
                newShowSelectionRow.setLayoutParams(tableRowParams);
                ///// SHOW NAME BUTTON TEXT AND ACTION
                showSelectButton.setText(showNameForUser);
                showSelectButton.setTextAppearance(boothReservationShowSelectionActivityContext, showSelectionButtonResId);
                showSelectButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectShowForReservation(finalizedShowObject);
                    }
                });

                ///// ADD TO TABLE
                newShowSelectionRow.addView(showSelectButton);
                showSelectionTable.addView(newShowSelectionRow);
            }
        } else {
            ///// HANDLE CASE - NO EXISTING SHOWS
            TableRow noShowsPleaseCreateRow = new TableRow(boothReservationShowSelectionActivityContext);
            TextView noShowsPleaseCreateTv = new TextView(boothReservationShowSelectionActivityContext);
            noShowsPleaseCreateTv.setText(getResources().getString(R.string.no_trade_shows_available_string));
            noShowsPleaseCreateTv.setTextAppearance(boothReservationShowSelectionActivityContext, R.style.no_shows_style);
            noShowsPleaseCreateTv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            TableRow.LayoutParams params = new TableRow.LayoutParams();
            params.span = 4;
            params.topMargin = 32;
            noShowsPleaseCreateRow.addView(noShowsPleaseCreateTv, params);
            showSelectionTable.addView(noShowsPleaseCreateRow);
        }
    }

    private void selectShowForReservation(Tag showTag) {
        Intent boothSelectionIntent = new Intent(boothReservationShowSelectionActivityContext, BoothReservation.class);
        boothSelectionIntent.putExtra("show", showTag);
        boothSelectionIntent.putExtra("orderid", orderID);
        startActivity(boothSelectionIntent);
    }

    private void handleSizing() {
        String platform = GlobalUtils.determinePlatform(getApplicationContext());
        if (platform.equalsIgnoreCase("station")) {
            pageHeaderFontResId = R.style.activity_header_style_station;
            showSelectionButtonResId = R.style.show_selection_button_style;
        } else {
            pageHeaderFontResId = R.style.activity_header_style_mobile;
            showSelectionButtonResId = R.style.show_selection_button_style;
        }
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
            Intent homeIntent = new Intent(boothReservationShowSelectionActivityContext, HomeActivity.class);
            startActivity(homeIntent);
        } else if (id == R.id.nav_show_setup_btn) {
            Intent showSetupIntent = new Intent(boothReservationShowSelectionActivityContext, TradeShows.class);
            startActivity(showSetupIntent);
        } else if (id == R.id.nav_config_booths_btn) {
            Intent configureBoothsIntent = new Intent(boothReservationShowSelectionActivityContext, ConfigureBoothsShowSelection.class);
            startActivity(configureBoothsIntent);
        } else if (id == R.id.nav_make_reservation_btn) {
            //// nothing; already in activity
        } else if (id == R.id.nav_app_settings_btn) {
            Intent applicationSettingsIntent = new Intent(boothReservationShowSelectionActivityContext, ApplicationSettings.class);
            startActivity(applicationSettingsIntent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.make_reservation_drawerlayout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class GetShowsForBoothSelection extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;
        private InventoryConnector inventoryConnector;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(boothReservationShowSelectionActivityContext);
            progressDialog.setMessage("Loading Shows...");
            progressDialog.show();
            showList = new ArrayList<>();
            ////// INIT CLOVER CONNECTIONS
            inventoryConnector = new InventoryConnector(boothReservationShowSelectionActivityContext, CloverAccount.getAccount(boothReservationShowSelectionActivityContext), null);
            inventoryConnector.connect();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                ///////////GET SHOW LIST (TAG LIST) FOR BOOTH SELECTION
                if (inventoryConnector.getTags().size() > 0) {
                    for (Tag currentTag : inventoryConnector.getTags()) {
                        if (currentTag.getName().contains(" [Show]")) {
                            showList.add(currentTag);
                        }
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


