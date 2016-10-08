package com.ashtonmansion.amtradeshowmanagement.activity;

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
import com.ashtonmansion.amtradeshowmanagement.util.GlobalUtils;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v3.inventory.InventoryConnector;
import com.clover.sdk.v3.inventory.Tag;

import java.util.ArrayList;
import java.util.List;

public class TradeShows extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    //ACTIVITY AND UI HANDLING
    private Context tradeShowsActivityContext;
    private int tableRowFontResId;
    private TableLayout showSelectionTable;
    // DATA HANDLING
    private List<Tag> showList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ///////////UI WORK/////////////////////////////////////
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trade_shows);
        Toolbar showSetupToolbar = (Toolbar) findViewById(R.id.show_setup_toolbar);
        setSupportActionBar(showSetupToolbar);
        DrawerLayout navDrawer = (DrawerLayout) findViewById(R.id.show_setup_drawerlayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, navDrawer, showSetupToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        navDrawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_show_setup);
        navigationView.setNavigationItemSelectedListener(this);

        ///// DATA WORK
        tradeShowsActivityContext = this;
        handleSizing();
        showSelectionTable = (TableLayout) findViewById(R.id.trade_show_selection_table);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ///// REFRESH PAGE
        GetShowListTask getShowListTask = new GetShowListTask();
        getShowListTask.execute();
    }

    private void populateShowTable() {
        showSelectionTable.removeAllViews();
        if (showList != null && showList.size() > 0) {
            for (Tag show : showList) {
                final Tag finalizedShow = show;
                ///// HANDLE SHOW NAME
                List<String> decoupledShowArray = GlobalUtils.decoupleShowName(show.getName());
                String showName = decoupledShowArray.get(0);
                String showDate = decoupledShowArray.get(1);
                String showLocation = decoupledShowArray.get(2);
                String showNameForUser = getResources().getString(R.string.show_name_for_user_string, showName, showDate, showLocation);

                ///// CREATE TEXT VIEWS AND BUTTONS
                TableRow newShowRow = new TableRow(tradeShowsActivityContext);
                TextView newShowTV = new TextView(tradeShowsActivityContext);
                Button editShowButton = new Button(tradeShowsActivityContext);

                ///// HANDLE FONTS
                newShowTV.setTextAppearance(tradeShowsActivityContext, tableRowFontResId);

                ///// SHOW NAME- BUTTON TXT- BUTTON ACTION
                newShowTV.setText(showNameForUser);
                editShowButton.setText(getResources().getString(R.string.trade_shows_edit_btn_string));
                editShowButton.setTextAppearance(tradeShowsActivityContext, R.style.row_item_button_style_both_platforms_currently);
                editShowButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        editShowAction(finalizedShow);
                    }
                });

                ///// ADD NEW ROW
                newShowRow.addView(newShowTV);
                newShowRow.addView(editShowButton);
                showSelectionTable.addView(newShowRow);
            }
        } else {
            ///// HANDLE CASE - NO SHOWS CREATED
            TextView noShowsCreatedTV = new TextView(tradeShowsActivityContext);
            noShowsCreatedTV.setText(getResources().getString(R.string.no_trade_shows_available_string));
            noShowsCreatedTV.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            noShowsCreatedTV.setTextAppearance(tradeShowsActivityContext, R.style.no_shows_style);
            TableRow noShowsCreatedRow = new TableRow(tradeShowsActivityContext);
            TableRow.LayoutParams params = new TableRow.LayoutParams();
            params.span = 4;
            params.topMargin = 32;
            params.bottomMargin = 20;
            noShowsCreatedRow.addView(noShowsCreatedTV, params);
            showSelectionTable.addView(noShowsCreatedRow);
        }

        //// ADD SHOW BUTTON TO TABLE
        Button addShowButton = new Button(tradeShowsActivityContext);
        addShowButton.setText(getResources().getString(R.string.add_show_string));
        addShowButton.setTextAppearance(tradeShowsActivityContext, R.style.standard_button_style_mobile);
        addShowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewShowAction();
            }
        });
        showSelectionTable.addView(addShowButton);
    }

    private void addNewShowAction() {
        Intent addShowIntent = new Intent(tradeShowsActivityContext, CreateShow.class);
        startActivity(addShowIntent);
    }

    private void editShowAction(Tag showToPass) {
        Intent editShowIntent = new Intent(tradeShowsActivityContext, EditShow.class);
        editShowIntent.putExtra("show", showToPass);
        startActivity(editShowIntent);
    }

    private void handleSizing() {
        String platform = GlobalUtils.determinePlatform(getApplicationContext());
        if (platform.equalsIgnoreCase("station"))
            tableRowFontResId = R.style.trade_show_row_style_station;
        else tableRowFontResId = R.style.trade_show_row_style_mobile;
    }

    ////////////NAVIGATION HANDLING METHODS ////////////////////
    @Override
    public void onBackPressed() {
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.app_settings_drawerlayout);
//        if (drawer.isDrawerOpen(GravityCompat.START)) {
//            drawer.closeDrawer(GravityCompat.START);
//        } else {
        super.onBackPressed();
        //}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_trade_shows, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_create_new_show_option) {
            addNewShowAction();
            return true;
        } else if (id == R.id.action_app_settings) {
            Intent applicationSettingsIntent = new Intent(tradeShowsActivityContext, ApplicationSettings.class);
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
            Intent homeIntent = new Intent(tradeShowsActivityContext, HomeActivity.class);
            startActivity(homeIntent);
        } else if (id == R.id.nav_show_setup_btn) {
            //// nothing; already in activity
        } else if (id == R.id.nav_config_booths_btn) {
            Intent configureBoothsIntent = new Intent(tradeShowsActivityContext, ConfigureBoothsShowSelection.class);
            startActivity(configureBoothsIntent);
        } else if (id == R.id.nav_make_reservation_btn) {
            Intent makeReservationIntent = new Intent(tradeShowsActivityContext, BoothReservationShowSelection.class);
            startActivity(makeReservationIntent);
        } else if (id == R.id.nav_app_settings_btn) {
            Intent applicationSettingsIntent = new Intent(tradeShowsActivityContext, ApplicationSettings.class);
            startActivity(applicationSettingsIntent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.show_setup_drawerlayout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class GetShowListTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;
        private InventoryConnector inventoryConnector;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(tradeShowsActivityContext);
            progressDialog.setMessage(getResources().getString(R.string.loading_trade_shows_text));
            progressDialog.show();
            ///// INITIALIZE CLOVER CONNECTIONS AND LIST                showList = new ArrayList<>();
            showList = new ArrayList<>();
            inventoryConnector = new InventoryConnector(tradeShowsActivityContext, CloverAccount.getAccount(tradeShowsActivityContext), null);
            inventoryConnector.connect();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                /////ONLY RETURN SHOW TAGS.
                for (Tag currentTag : inventoryConnector.getTags()) {
                    if (currentTag.getName().contains("[Show]")) {
                        showList.add(currentTag);
                    }
                }
            } catch (RemoteException | BindingException | ServiceException | ClientException e1) {
                Log.e("Clover Excptn; ", e1.getClass().getName() + " : " + e1.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            inventoryConnector.disconnect();
            populateShowTable();
            progressDialog.dismiss();
        }
    }
}
