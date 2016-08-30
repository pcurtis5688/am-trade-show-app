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
import java.util.List;

public class EditShow extends AppCompatActivity {
    private Context editShowActivityContext;
    private String showName;
    private String showID;
    //UI FIELDS
    private EditText showNameEditText;
    private EditText showDateEditText;
    private EditText showLocationEditText;
    private EditText showNotesEditText;
    //CLOVER ACCESS VARS
    private Account merchantAccount;
    private InventoryConnector inventoryConnector;
    //UPON SAVING CHANGES VARS
    private String editedShowName;
    private String editedShowDate;
    private String editedShowLocation;
    private String editedShowNotes;
    private String editedShowLocationAndDateString;

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

        ////////////DATA HANDLING//////////////////////////////
        Bundle extrasBundle = getIntent().getExtras();
        if (extrasBundle != null) {
            showName = (String) extrasBundle.get("showname");
            showID = (String) extrasBundle.get("showid");
            String showNameAndIDHeader = showName + " (" + showID + ")";
            showNameAndIDHeaderTV.setText(showNameAndIDHeader);
        }

        getLocalShowDataAndPopulateFields(showID);
    }

    private void getLocalShowDataAndPopulateFields(String showID) {
        TradeShowDB database = new TradeShowDB(editShowActivityContext);
        Cursor dbShowCursor = database.selectSingleShowByCloverID(showID);
        showNameEditText.setText(dbShowCursor.getString(2));
        showDateEditText.setText(dbShowCursor.getString(3));
        showLocationEditText.setText(dbShowCursor.getString(4));
        showNotesEditText.setText(dbShowCursor.getString(5));
    }

    public void saveShowChangesAction(View view) {
        editedShowName = showNameEditText.getText().toString();
        editedShowDate = showDateEditText.getText().toString();
        editedShowLocation = showLocationEditText.getText().toString();
        editedShowNotes = showNotesEditText.getText().toString();
        editedShowLocationAndDateString = editedShowLocation + " - " + editedShowDate;

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
                updatedShowCategory.setName(editedShowLocationAndDateString);

                inventoryConnector.updateCategory(updatedShowCategory);

                TradeShowDB database = new TradeShowDB(editShowActivityContext);
                recordSuccessfullyUpdated = database.updateSingleShowByCloverID(showID, editedShowName,
                        editedShowDate, editedShowLocation, editedShowNotes, editedShowLocationAndDateString);
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
