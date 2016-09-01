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

import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v3.inventory.InventoryConnector;
import com.clover.sdk.v3.inventory.Item;

public class EditBooth extends AppCompatActivity {
    //////ACTIVITY VARS
    private Context editBoothActivityContext;
    //////CLOVER VARS
    private Account merchantAccount;
    private InventoryConnector inventoryConnector;
    private Item booth;
    //////UI VARS
    private TextView editBoothHeaderTv;
    private EditText editBoothNumberField;
    private EditText editBoothPriceField;
    private EditText editBoothSizeField;
    private EditText editBoothAreaField;
    private EditText editBoothCategoryField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //////////////////NAVIGATION AND UI WORK//////////////
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_booth);
        Toolbar toolbar = (Toolbar) findViewById(R.id.edit_booth_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //////////////////DATA WORK///////////////////////////
        editBoothActivityContext = this;
        Bundle extrasBundle = getIntent().getExtras();
        if (extrasBundle != null) {
            booth = (Item) extrasBundle.get("booth");
            editBoothHeaderTv = (TextView) findViewById(R.id.edit_booth_header);
            editBoothNumberField = (EditText) findViewById(R.id.edit_booth_number_field);
            editBoothPriceField = (EditText) findViewById(R.id.edit_booth_price_field);
            editBoothSizeField = (EditText) findViewById(R.id.edit_booth_size_field);
            editBoothAreaField = (EditText) findViewById(R.id.edit_booth_area_field);
            editBoothCategoryField = (EditText) findViewById(R.id.edit_booth_category_field);

            editBoothHeaderTv.setText("Edit Booth - " + booth.getName());
            //// TODO: 8/31/2016 string resource
            editBoothNumberField.setText(booth.getSku());
            //// TODO: 8/31/2016 price handling
            editBoothPriceField.setText(booth.getPrice().toString());
            editBoothSizeField.setText("set size");
            editBoothAreaField.setText("set area");
            editBoothCategoryField.setText("category?");
        } else {
            Log.e("Major error: ", "edit booth started without booth obj");
        }
    }

    public void saveBoothChangesAction(View view) {
        UpdateBoothTask updateBoothTask = new UpdateBoothTask();
        updateBoothTask.execute();
    }

    private void closeOutActivity() {
        finish();
    }

    private class UpdateBoothTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;
        private String editBoothNumberFieldData;
        private long editBoothPriceFieldData;
        // private String editBoothSizeFieldData;
        //  private String editBoothAreaFieldData;
        //  private String editBoothCategoryFieldData;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(editBoothActivityContext);
            progressDialog.setMessage("Saving Booth...");
            progressDialog.show();
            editBoothNumberFieldData = editBoothNumberField.getText().toString();
            editBoothPriceFieldData = Long.parseLong(editBoothPriceField.getText().toString());
            //    editBoothSizeFieldData = editBoothSizeField.getText().toString();
            ///     editBoothAreaFieldData = editBoothAreaField.getText().toString();
            //     editBoothCategoryFieldData = editBoothCategoryField.getText().toString();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                merchantAccount = CloverAccount.getAccount(editBoothActivityContext);
                inventoryConnector = new InventoryConnector(editBoothActivityContext, merchantAccount, null);
                inventoryConnector.connect();
                Item fetchedBooth = inventoryConnector.getItem(booth.getId());
                fetchedBooth.setSku(editBoothNumberFieldData);
                fetchedBooth.setPrice(editBoothPriceFieldData);
                //todo change the other 3 here
                inventoryConnector.updateItem(fetchedBooth);
                Item testUpdate = inventoryConnector.getItem(fetchedBooth.getId());

                // showList = inventoryConnector.getCategories();
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
            progressDialog.dismiss();
            closeOutActivity();
        }
    }
}
