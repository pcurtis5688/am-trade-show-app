package com.ashtonmansion.tradeshowmanagement.activity;

import android.accounts.Account;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.ashtonmansion.amtradeshowmanagement.R;
import com.ashtonmansion.tradeshowmanagement.db.TradeShowDB;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v3.base.Reference;
import com.clover.sdk.v3.inventory.Category;
import com.clover.sdk.v3.inventory.InventoryConnector;
import com.clover.sdk.v3.inventory.Item;

import java.util.ArrayList;
import java.util.List;

public class AddShow extends AppCompatActivity {
    private Context addShowActivityContext;
    //CLOVER ACCESS VARS
    private Account merchantAccount;
    private InventoryConnector inventoryConnector;
    //UI VARS
    private String newShowLocationAndDateStringForCategory;
    private EditText newShowNameField;
    private EditText newShowDateField;
    private EditText newShowLocationField;
    private EditText newShowNotesField;
    private String newShowNameString;
    private String newShowDateString;
    private String newShowLocationString;
    private String newShowNotesString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ///////NAVIGATION WORK ///////////////////////////////////
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_show);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ///////ACTIVITY CONTEXT AND UI FIELD WORK //////////////////
        addShowActivityContext = this;
        newShowNameField = (EditText) findViewById(R.id.add_show_name_field);
        newShowDateField = (EditText) findViewById(R.id.add_show_date_field);
        newShowLocationField = (EditText) findViewById(R.id.add_show_location_field);
        newShowNotesField = (EditText) findViewById(R.id.add_show_notes_field);
    }

    ////////DATA METHODS AND BUTTON ACTIONS////////////////////
    public void addNewShowAction(View view) {
        //GRAB NEW SHOW DATA
        newShowNameString = newShowNameField.getText().toString();
        newShowDateString = newShowDateField.getText().toString();
        newShowLocationString = newShowLocationField.getText().toString();
        newShowNotesString = newShowNotesField.getText().toString();
        newShowLocationAndDateStringForCategory = newShowLocationString + " - " + newShowDateString;
        AddShowCategoryTask addShowTask = new AddShowCategoryTask();
        addShowTask.execute();
    }
    private void doLocalInsert(Category returnedCategory) {
        //GET THE CLOVER ID FOR LOCAL INSERT
        String cloverCategoryID = returnedCategory.getId();
        //DO THE LOCAL INSERT
        TradeShowDB database = new TradeShowDB(addShowActivityContext);
        database.createShowRecord(cloverCategoryID, newShowNameString, newShowDateString,
                newShowLocationString, newShowNotesString, newShowLocationAndDateStringForCategory);
    }

    private void closeOutActivity() {
        Toast showAddedToast = Toast.makeText(addShowActivityContext, "Show Added!", Toast.LENGTH_SHORT);
        showAddedToast.show();
        finish();
    }

    public void cancelAddNewShowAction(View view) {
        finish();
    }

    private class AddShowCategoryTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;
        private Category returnedCategory;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(addShowActivityContext);
            progressDialog.setMessage("Adding New Show...");
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                //ACCESS CLOVER
                merchantAccount = CloverAccount.getAccount(addShowActivityContext);
                inventoryConnector = new InventoryConnector(addShowActivityContext, merchantAccount, null);
                inventoryConnector.connect();
                Category newShowCategory = new Category();
                newShowCategory.setSortOrder(1);
                newShowCategory.setName(newShowLocationAndDateStringForCategory);
                List<Reference> fauxItemRefList = new ArrayList<>();

                newShowCategory.setItems(fauxItemRefList);
                returnedCategory = inventoryConnector.createCategory(newShowCategory);

                doLocalInsert(returnedCategory);
            } catch (RemoteException | BindingException | ServiceException | ClientException e1) {
                Log.e("Clover Excptn; ", e1.getClass().getName() + " : " + e1.getMessage());
                e1.printStackTrace();
            } finally {
                inventoryConnector.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            closeOutActivity();
        }
    }
}
