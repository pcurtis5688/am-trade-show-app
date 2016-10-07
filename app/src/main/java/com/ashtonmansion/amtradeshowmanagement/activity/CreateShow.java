package com.ashtonmansion.amtradeshowmanagement.activity;

import android.accounts.Account;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ashtonmansion.amtradeshowmanagement.R;
import com.ashtonmansion.amtradeshowmanagement.util.GlobalUtils;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v3.inventory.InventoryConnector;
import com.clover.sdk.v3.inventory.Tag;

public class CreateShow extends AppCompatActivity {
    /////CONTEXT AND SHOW NAME
    private Context addShowActivityContext;
    private String formattedFullShowName;
    /////UI FIELDS
    private int headerFontResId;
    private EditText newShowNameField;
    private EditText newShowDateField;
    private EditText newShowLocationField;
    private EditText newShowNotesField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ///////NAVIGATION WORK ///////////////////////////////////
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_show);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ///////ACTIVITY CONTEXT AND UI FIELD WORK //////////////////
        addShowActivityContext = this;
        handleSizing();
        TextView headerView = (TextView) findViewById(R.id.create_show_header);
        headerView.setTextAppearance(addShowActivityContext, headerFontResId);
        newShowNameField = (EditText) findViewById(R.id.add_show_name_field);
        newShowDateField = (EditText) findViewById(R.id.add_show_date_field);
        newShowLocationField = (EditText) findViewById(R.id.add_show_location_field);
        newShowNotesField = (EditText) findViewById(R.id.add_show_notes_field);
    }

    private void handleSizing() {
        String platform = GlobalUtils.determinePlatform(getApplicationContext());
        if (platform.equalsIgnoreCase("station"))
            headerFontResId = R.style.activity_header_style_station;
        else headerFontResId = R.style.activity_header_style_mobile;
    }

    ////////DATA METHODS AND BUTTON ACTIONS////////////////////
    public void addNewShowAction(View view) {
        //GRAB NEW SHOW DATA
        String newShowNameString = newShowNameField.getText().toString();
        String newShowDateString = newShowDateField.getText().toString();
        String newShowLocationString = newShowLocationField.getText().toString();
        String newShowNotesString = newShowNotesField.getText().toString();
        formattedFullShowName = newShowNameString + "," + newShowDateString + "," + newShowLocationString + "," + newShowNotesString + " [Show]";
        AddShowLabelTask addShowTask = new AddShowLabelTask();
        addShowTask.execute();
    }

    private void closeOutActivity() {
        Toast showAddedToast = Toast.makeText(addShowActivityContext, "Show Added!", Toast.LENGTH_SHORT);
        showAddedToast.show();
        finish();
    }

    public void cancelAddNewShowAction(View view) {
        finish();
    }

    private class AddShowLabelTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;
        //CLOVER ACCESS VARS
        private Account merchantAccount;
        private InventoryConnector inventoryConnector;

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

                //CREATE NEW LABEL AND SET DATA, ADD TO CLOVER
                Tag newShowTag = new Tag();
                newShowTag.setName(formattedFullShowName);
                inventoryConnector.createTag(newShowTag);
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
