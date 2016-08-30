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
import android.widget.TextView;

import com.ashtonmansion.amtradeshowmanagement.R;
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
    //LOCAL SHOW DATA VARS
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

        ////////////DATA HANDLING//////////////////////////////
        Bundle extrasBundle = getIntent().getExtras();
        if (extrasBundle != null) {
            showName = (String) extrasBundle.get("showname");
            showID = (String) extrasBundle.get("showid");
            String showNameAndIDHeader = showName + " (" + showID + ")";
            showNameAndIDHeaderTV.setText(showNameAndIDHeader);
            showNameEditText.setText(showName);
        }

        //FETCH LOCAL SHOW DATA
        GetLocalShowDataTask getLocalDataAndPopulateTask = new GetLocalShowDataTask();
        getLocalDataAndPopulateTask.execute();
    }

    private void getLocalShowDataAndPopulateFields(String showID) {
        //// TODO: 8/30/2016
    }

    /////////////DATA AND BUTTON ACTION HANDLING
    public void cancelEditShow(View view) {
        finish();
    }

    private class GetLocalShowDataTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(editShowActivityContext);
            progressDialog.setMessage("Loading Show Data...");
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            getLocalShowDataAndPopulateFields(showID);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
        }
    }
}
