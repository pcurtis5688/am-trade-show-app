package com.ashtonmansion.tradeshowmanagement.activity;

import android.accounts.Account;
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
import com.clover.sdk.v3.inventory.Category;
import com.clover.sdk.v3.inventory.InventoryConnector;

import java.util.Arrays;
import java.util.List;

public class TradeShows extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    //ACTIVITY AND UI HANDLING
    private Context tradeShowsActivityContext;
    private TableLayout showSelectionTable;
    // DATA HANDLING
    private Account merchantAccount;
    private InventoryConnector inventoryConnector;
    private List<Category> showList;

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

        ///////////DATA WORK////////////////////////////////////
        tradeShowsActivityContext = this;
        merchantAccount = CloverAccount.getAccount(tradeShowsActivityContext);
        inventoryConnector = new InventoryConnector(tradeShowsActivityContext, merchantAccount, null);
        showSelectionTable = (TableLayout) findViewById(R.id.trade_show_selection_table);
    }

    @Override
    protected void onResume() {
        super.onResume();

        fetchData();
    }

    ////////////DATA HANDLING METHODS///////////////////////////
    private void fetchData() {
        GetShowListTask getShowListTask = new GetShowListTask();
        getShowListTask.execute();
    }

    private void populateTable() {
        showSelectionTable.removeAllViews();
        for (Category show : showList) {
            /////////////
            final Category finalizedShow = show;
            String showID = show.getId();
            List<String> decoupledShowArray = Arrays.asList(show.getName().split(","));
            String showName = decoupledShowArray.get(0);
            String showDate = decoupledShowArray.get(1);
            String showLocation = decoupledShowArray.get(2);
            String showNotes = decoupledShowArray.get(3);
            String showNameAndIDString = showName + " (" + showDate + " - " + showLocation + ")";

            TableRow newShowRow = new TableRow(tradeShowsActivityContext);
            TextView newShowTV = new TextView(tradeShowsActivityContext);
            newShowTV.setText(showNameAndIDString);
            Button editShowButton = new Button(tradeShowsActivityContext);
            editShowButton.setText(getResources().getString(R.string.trade_shows_edit_btn_string));
            editShowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    editShowAction(finalizedShow);
                }
            });
            newShowRow.addView(newShowTV);
            newShowRow.addView(editShowButton);
            showSelectionTable.addView(newShowRow);
        }
        ////PUT THE LAST ROW (ADD SHOW BUTTON) IN
        Button addShowButton = new Button(tradeShowsActivityContext);
        addShowButton.setText(getResources().getString(R.string.add_show_string));
        addShowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewShowAction();
            }
        });
        showSelectionTable.addView(addShowButton);
    }

    private void addNewShowAction() {
        Intent addShowIntent = new Intent(tradeShowsActivityContext, AddShow.class);
        startActivity(addShowIntent);
    }

    private void editShowAction(Category showToPass) {
        Intent editShowIntent = new Intent(tradeShowsActivityContext, EditShow.class);
        editShowIntent.putExtra("show", showToPass);
        startActivity(editShowIntent);
    }

    ////////////NAVIGATION HANDLING METHODS ////////////////////
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.app_settings_drawerlayout);
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
            Intent homeIntent = new Intent(tradeShowsActivityContext, HomeActivity.class);
            startActivity(homeIntent);
        } else if (id == R.id.nav_show_setup_btn) {
            //// nothing; already in activity
        } else if (id == R.id.nav_config_booths_btn) {
            Intent configureBoothsIntent = new Intent(tradeShowsActivityContext, ConfigureBoothsShowSelection.class);
            startActivity(configureBoothsIntent);
        } else if (id == R.id.nav_reports_queries_btn) {
            Intent reportsIntent = new Intent(tradeShowsActivityContext, Reports.class);
            startActivity(reportsIntent);
        } else if (id == R.id.nav_make_reservation_btn) {
            Intent makeReservationIntent = new Intent(tradeShowsActivityContext, BoothReservationShowSelection.class);
            startActivity(makeReservationIntent);
        } else if (id == R.id.nav_email_reservation_confirmation_btn) {
            Intent emailConfirmationIntent = new Intent(tradeShowsActivityContext, EmailConfirmation.class);
            startActivity(emailConfirmationIntent);
        } else if (id == R.id.nav_advertising_sales_btn) {
            Intent advertisingSalesIntent = new Intent(tradeShowsActivityContext, AdvertisingSales.class);
            startActivity(advertisingSalesIntent);
        } else if (id == R.id.nav_general_tix_sales_btn) {
            Intent generalTicketSalesIntent = new Intent(tradeShowsActivityContext, GeneralTicketSales.class);
            startActivity(generalTicketSalesIntent);
        } else if (id == R.id.nav_special_events_sales_btn) {
            Intent specialEventSalesIntent = new Intent(tradeShowsActivityContext, SpecialEventSales.class);
            startActivity(specialEventSalesIntent);
        } else if (id == R.id.nav_merchandise_sales_btn) {
            Intent merchandiseSalesIntent = new Intent(tradeShowsActivityContext, MerchandiseSales.class);
            startActivity(merchandiseSalesIntent);
        } else if (id == R.id.nav_app_settings_btn) {


        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.show_setup_drawerlayout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class GetShowListTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
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
            populateTable();
        }
    }
}
