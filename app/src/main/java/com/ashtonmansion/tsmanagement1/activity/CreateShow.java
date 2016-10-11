package com.ashtonmansion.tsmanagement1.activity;

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
import android.widget.Toast;

import com.ashtonmansion.tsmanagement1.R;
import com.ashtonmansion.tsmanagement1.util.GlobalUtils;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v3.inventory.InventoryConnector;
import com.clover.sdk.v3.inventory.Tag;

public class CreateShow extends AppCompatActivity {
    /////CONTEXT AND SHOW NAME
    private Context createShowActivityContext;
    private String formattedFullShowName;
    /////UI FIELDS
    private int headerFontResId;
    private int promptFontResId;
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
        createShowActivityContext = this;
        handleSizing();
        TextView headerView = (TextView) findViewById(R.id.create_show_header);
        headerView.setTextAppearance(createShowActivityContext, headerFontResId);
        newShowNameField = (EditText) findViewById(R.id.add_show_name_field);
        newShowDateField = (EditText) findViewById(R.id.add_show_date_field);
        newShowLocationField = (EditText) findViewById(R.id.add_show_location_field);
        newShowNotesField = (EditText) findViewById(R.id.add_show_notes_field);
        stylePromptsAndFields();
    }

    private void handleSizing() {
        String platform = GlobalUtils.determinePlatform(getApplicationContext());
        if (platform.equalsIgnoreCase("station")) {
            headerFontResId = R.style.activity_header_style_station;
            promptFontResId = R.style.prompt_text_font_style_station;
        } else {
            headerFontResId = R.style.activity_header_style_mobile;
            promptFontResId = R.style.prompt_text_font_style_mobile;
        }
    }

    private void stylePromptsAndFields() {
        ////// GET PROMPT HANDLERS PRIVATELY
        TextView createShowNamePrompt = (TextView) findViewById(R.id.create_show_name_prompt);
        TextView createShowDatePrompt = (TextView) findViewById(R.id.create_show_date_prompt);
        TextView createShowLocationPrompt = (TextView) findViewById(R.id.create_show_location_prompt);
        TextView createShowNotesPrompt = (TextView) findViewById(R.id.create_show_notes_prompt);
        ////// SET PROMPT FONT
        createShowNamePrompt.setTextAppearance(createShowActivityContext, promptFontResId);
        createShowDatePrompt.setTextAppearance(createShowActivityContext, promptFontResId);
        createShowLocationPrompt.setTextAppearance(createShowActivityContext, promptFontResId);
        createShowNotesPrompt.setTextAppearance(createShowActivityContext, promptFontResId);
        ////// SET FIELD FONTS
        newShowNameField.setTextAppearance(createShowActivityContext, promptFontResId);
        newShowDateField.setTextAppearance(createShowActivityContext, promptFontResId);
        newShowLocationField.setTextAppearance(createShowActivityContext, promptFontResId);
        newShowNotesField.setTextAppearance(createShowActivityContext, promptFontResId);

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
        Toast.makeText(createShowActivityContext, getResources().getString(R.string.create_show_show_added_toast_string), Toast.LENGTH_SHORT).show();
        finish();
    }

    private class AddShowLabelTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;
        //CLOVER ACCESS VARS
        private Account merchantAccount;
        private InventoryConnector inventoryConnector;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(createShowActivityContext);
            progressDialog.setMessage("Adding New Show...");
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                //ACCESS CLOVER
                merchantAccount = CloverAccount.getAccount(createShowActivityContext);
                inventoryConnector = new InventoryConnector(createShowActivityContext, merchantAccount, null);
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
