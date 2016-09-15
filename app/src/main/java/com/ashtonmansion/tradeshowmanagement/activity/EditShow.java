package com.ashtonmansion.tradeshowmanagement.activity;

import android.accounts.Account;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
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
import android.widget.TextView;

import com.ashtonmansion.amtradeshowmanagement.R;
import com.ashtonmansion.tradeshowmanagement.db.TradeShowDB;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v3.base.Reference;
import com.clover.sdk.v3.inventory.Category;
import com.clover.sdk.v3.inventory.InventoryConnector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EditShow extends AppCompatActivity {
    private Context editShowActivityContext;
    //CLOVER ACCESS VARS
    private Account merchantAccount;
    private InventoryConnector inventoryConnector;
    private Category show;
    //UI FIELDS
    private EditText showNameEditText;
    private EditText showDateEditText;
    private EditText showLocationEditText;
    private EditText showNotesEditText;
    //TEMPORARY UTILITY STRINGS
    private String showID;
    private String formattedFullShowName;
    private String showName;
    private String showDate;
    private String showLocation;
    private String showNotes;
    //VARS TO SAVE
    private String editedShowName;
    private String editedShowDate;
    private String editedShowLocation;
    private String editedShowNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ////////UI & ACTIVITY CONTEXT HANDLING//////////////////
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_show);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editShowActivityContext = this;
        TextView showNameAndIDHeaderTV = (TextView) findViewById(R.id.edit_show_name_and_id_header);
        showNameEditText = (EditText) findViewById(R.id.edit_show_name_field);
        showDateEditText = (EditText) findViewById(R.id.edit_show_date_field);
        showLocationEditText = (EditText) findViewById(R.id.edit_show_location_field);
        showNotesEditText = (EditText) findViewById(R.id.edit_show_notes_field);
        ////////////DATA HANDLING/////////////////
        Bundle extrasBundle = getIntent().getExtras();
        if (extrasBundle != null) {
            show = (Category) extrasBundle.get("show");
            showID = show.getId();
            formattedFullShowName = show.getName();
            decoupleShowName();
            populateFields();
            String editShowHeader = getResources().getString(R.string.edit_show_header_string, showName, showDate, showLocation);
            showNameAndIDHeaderTV.setText(editShowHeader);
        }

    }

    private void decoupleShowName() {
        List<String> splitShowNameArray = Arrays.asList(formattedFullShowName.split(","));
        showName = splitShowNameArray.get(0);
        showDate = splitShowNameArray.get(1);
        showLocation = splitShowNameArray.get(2);
        showNotes = splitShowNameArray.get(3);
    }

    private void populateFields() {
        showNameEditText.setText(showName);
        showDateEditText.setText(showDate);
        showLocationEditText.setText(showLocation);
        showNotesEditText.setText(showNotes);
    }

    public void saveShowChangesAction(View view) {
        editedShowName = showNameEditText.getText().toString();
        editedShowDate = showDateEditText.getText().toString();
        editedShowLocation = showLocationEditText.getText().toString();
        editedShowNotes = showNotesEditText.getText().toString();
        formattedFullShowName = editedShowName + "," + editedShowDate + "," + editedShowLocation + "," + editedShowNotes;

        UpdateShowTask updateShowTask = new UpdateShowTask();
        updateShowTask.execute();
    }

    public void deleteShowAction(View view) {
        DeleteShowTask deleteShowTask = new DeleteShowTask();
        deleteShowTask.execute();
    }

    public void cancelEditShow(View view) {
        finish();
    }

    private class UpdateShowTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;
        private boolean recordSuccessfullyUpdated;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(editShowActivityContext);
            progressDialog.setMessage("Updating Show...");
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                merchantAccount = CloverAccount.getAccount(editShowActivityContext);
                inventoryConnector = new InventoryConnector(editShowActivityContext, merchantAccount, null);
                inventoryConnector.connect();
                Category updatedShowCategory = new Category();
                updatedShowCategory.setId(showID);
                updatedShowCategory.setSortOrder(1);
                updatedShowCategory.setName(formattedFullShowName);

                inventoryConnector.updateCategory(updatedShowCategory);

                TradeShowDB database = new TradeShowDB(editShowActivityContext);
                recordSuccessfullyUpdated = database.updateSingleShowByCloverID(showID, editedShowName,
                        editedShowDate, editedShowLocation, editedShowNotes, formattedFullShowName);
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
            finish();
        }
    }

    private class DeleteShowTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;
        private boolean recordSuccessfullyDeleted;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(editShowActivityContext);
            progressDialog.setMessage("Deleting Show...");
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                merchantAccount = CloverAccount.getAccount(editShowActivityContext);
                inventoryConnector = new InventoryConnector(editShowActivityContext, merchantAccount, null);
                inventoryConnector.connect();
                inventoryConnector.deleteCategory(showID);

                TradeShowDB database = new TradeShowDB(editShowActivityContext);
                recordSuccessfullyDeleted = database.deleteSingleShowByCloverID(showID);
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
            finish();
        }
    }
}
