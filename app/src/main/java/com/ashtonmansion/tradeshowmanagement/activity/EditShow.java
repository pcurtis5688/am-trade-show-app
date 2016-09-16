package com.ashtonmansion.tradeshowmanagement.activity;

import android.accounts.Account;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.ashtonmansion.amtradeshowmanagement.R;
import com.ashtonmansion.tradeshowmanagement.util.GlobalUtils;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v3.inventory.Category;
import com.clover.sdk.v3.inventory.InventoryConnector;
import com.clover.sdk.v3.inventory.Tag;

import java.util.Arrays;
import java.util.List;

public class EditShow extends AppCompatActivity {
    private Context editShowActivityContext;
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
            Tag showTag = (Tag) extrasBundle.get("show");
            showID = showTag.getId();
            formattedFullShowName = showTag.getName();
            decoupleShowName();
            populateFields();
            String editShowHeader = getResources().getString(R.string.edit_show_header_string, showName, showDate, showLocation);
            showNameAndIDHeaderTV.setText(editShowHeader);
        }
    }

    private void decoupleShowName() {
        List<String> splitShowNameArray = GlobalUtils.decoupleShowName(formattedFullShowName);
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
        String editedShowName = showNameEditText.getText().toString();
        String editedShowDate = showDateEditText.getText().toString();
        String editedShowLocation = showLocationEditText.getText().toString();
        String editedShowNotes = showNotesEditText.getText().toString();
        formattedFullShowName = editedShowName + "," + editedShowDate + "," + editedShowLocation + "," + editedShowNotes + " [Show]";

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
        //CLOVER ACCESS VARS
        private Account merchantAccount;
        private InventoryConnector inventoryConnector;

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
                Tag updatedShowCategory = new Tag();
                updatedShowCategory.setId(showID);
                updatedShowCategory.setName(formattedFullShowName);
                inventoryConnector.updateTag(updatedShowCategory);
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
        //CLOVER ACCESS VARS
        private Account merchantAccount;
        private InventoryConnector inventoryConnector;

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
                inventoryConnector.deleteTag(showID);
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
