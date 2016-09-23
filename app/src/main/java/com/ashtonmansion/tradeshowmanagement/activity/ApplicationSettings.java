package com.ashtonmansion.tradeshowmanagement.activity;

import android.accounts.Account;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ashtonmansion.amtradeshowmanagement.R;
import com.ashtonmansion.tradeshowmanagement.util.GlobalUtils;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v1.tender.TenderConnector;
import com.clover.sdk.v3.base.Tender;
import com.clover.sdk.v3.inventory.InventoryConnector;
import com.clover.sdk.v3.inventory.Item;
import com.clover.sdk.v3.inventory.Tag;
import com.clover.sdk.v3.order.OrderConnector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ApplicationSettings extends AppCompatActivity {
    private Context applicationSettingsActivityContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_settings_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        applicationSettingsActivityContext = this;

        final Button checkBoothIntegrityBtn = (Button) findViewById(R.id.check_booth_integrity_btn);
        checkBoothIntegrityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkBoothIntegrityAndCorrect();
                checkBoothIntegrityBtn.setEnabled(false);
            }
        });
        final Button validateBoothNamesBtn = (Button) findViewById(R.id.validate_booth_names_btn);
        validateBoothNamesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateBoothNamesAndCorrect();
            }
        });
    }

    private class DeleteAllItemsTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;
        /////// CLOVER CONNECTION
        private Account merchantAccount;
        private InventoryConnector inventoryConnector;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(applicationSettingsActivityContext);
            progressDialog.setMessage(getResources().getString(R.string.quick_ops_delete_all_progress_message));
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                merchantAccount = CloverAccount.getAccount(applicationSettingsActivityContext);
                inventoryConnector = new InventoryConnector(applicationSettingsActivityContext, merchantAccount, null);
                inventoryConnector.connect();
                for (Item currentItem : inventoryConnector.getItems()) {
                    inventoryConnector.deleteItem(currentItem.getId());
                }
            } catch (RemoteException | BindingException | ServiceException | ClientException e1) {
                Log.e("Clover Excptn; ", e1.getClass().getName() + " : " + e1.getMessage());
            } catch (SQLiteException e2) {
                Log.e("SQLiteExcptn: ", e2.getClass().getName() + " - " + e2.getMessage());
            } finally {
                inventoryConnector.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            Toast toast = Toast.makeText(applicationSettingsActivityContext, "Done Deleting All", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private class DeleteAllTagsTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;
        /////// CLOVER CONNECTION
        private Account merchantAccount;
        private InventoryConnector inventoryConnector;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(applicationSettingsActivityContext);
            progressDialog.setMessage(getResources().getString(R.string.quick_ops_delete_all_tags_progress_message));
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                merchantAccount = CloverAccount.getAccount(applicationSettingsActivityContext);
                inventoryConnector = new InventoryConnector(applicationSettingsActivityContext, merchantAccount, null);
                inventoryConnector.connect();

                Iterator tagKiller = inventoryConnector.getTags().iterator();
                do {
                    Tag dieTag = (Tag) tagKiller.next();
                    inventoryConnector.deleteTag(dieTag.getId());
                } while (tagKiller.hasNext());

            } catch (RemoteException | BindingException | ServiceException | ClientException e1) {
                Log.e("Clover Excptn; ", e1.getClass().getName() + " : " + e1.getMessage());
            } catch (SQLiteException e2) {
                Log.e("SQLiteExcptn: ", e2.getClass().getName() + " - " + e2.getMessage());
            } finally {
                inventoryConnector.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            Toast toast = Toast.makeText(applicationSettingsActivityContext, "Done Deleting All Tags", Toast.LENGTH_SHORT);
            toast.show();
        }
    }


    private boolean checkBoothIntegrityAndCorrect() {
        new AsyncTask<Void, Void, Void>() {
            private InventoryConnector inventoryConnector;
            private OrderConnector orderConnector;
            private List<Item> reservedBoothsWithInvalidatedOrders;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                ////// INITIALIZE LISTS
                reservedBoothsWithInvalidatedOrders = new ArrayList<>();

                ////// INITIALIZE CLOVER CONNECTIONS
                inventoryConnector = new InventoryConnector(applicationSettingsActivityContext, CloverAccount.getAccount(applicationSettingsActivityContext), null);
                orderConnector = new OrderConnector(applicationSettingsActivityContext, CloverAccount.getAccount(applicationSettingsActivityContext), null);
                inventoryConnector.connect();
                orderConnector.connect();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    for (Item boothToCheck : inventoryConnector.getItems()) {
                        if (boothToCheck.getCode().contains("Order #")) {
                            String orderNumber = boothToCheck.getCode().substring(7);
                            if (null == orderConnector.getOrder(orderNumber)) {
                                reservedBoothsWithInvalidatedOrders.add(boothToCheck);
                                inventoryConnector.updateItem(boothToCheck.setCode("Available"));
                                Log.d("Invalid Booth: ", boothToCheck.getId() + "code: " + boothToCheck.getCode() + " name: " + boothToCheck.getName());
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.d("Excptn: ", e.getMessage(), e.getCause());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                inventoryConnector.disconnect();
                orderConnector.disconnect();
                inventoryConnector = null;
                orderConnector = null;
                if (reservedBoothsWithInvalidatedOrders.size() > 0) {
                    Toast.makeText(applicationSettingsActivityContext, getResources().getString(R.string.invalid_booths_found_and_corrected), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(applicationSettingsActivityContext, getResources().getString(R.string.no_invalid_booths_found), Toast.LENGTH_LONG).show();
                }
            }
        }.execute();

        return false;
    }

    private void validateBoothNamesAndCorrect() {
        new AsyncTask<Void, Void, Void>() {
            private InventoryConnector inventoryConnector;
            private List<String> boothSkusMissingSATs;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                ////// INIT LISTS
                boothSkusMissingSATs = new ArrayList<>();
                ////// INITIALIZE CLOVER CONNECTIONS
                inventoryConnector = new InventoryConnector(applicationSettingsActivityContext, CloverAccount.getAccount(applicationSettingsActivityContext), null);
                inventoryConnector.connect();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    for (Item boothToCheck : inventoryConnector.getItems()) {
                        if (boothToCheck.getName().contains("booth name")) {
                            String[] tagValues = fetchTagValues(boothToCheck.getSku(), boothToCheck.getTags());
                            String formattedBoothName = "Booth #" + boothToCheck.getSku() + "(" + tagValues[0] + ", " + tagValues[1] + ", " + tagValues[2] + ")";
                            inventoryConnector.updateItem(boothToCheck.setName(formattedBoothName));
                        }
                    }
                } catch (Exception e) {
                    Log.d("Excptn: ", e.getMessage(), e.getCause());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                inventoryConnector.disconnect();
                inventoryConnector = null;
                Toast.makeText(applicationSettingsActivityContext, getResources().getString(R.string.invalid_booths_found_and_corrected), Toast.LENGTH_LONG).show();
            }

            protected String[] fetchTagValues(String boothSku, List<Tag> tagList) {
                String[] tagValues = new String[3];
                for (Tag currentTag : tagList) {
                    if (currentTag.getName().substring(0, 4).equalsIgnoreCase("size")) {
                        tagValues[0] = GlobalUtils.getUnformattedTagName(currentTag.getName(), "Size");
                    } else if (currentTag.getName().substring(0, 4).equalsIgnoreCase("area")) {
                        tagValues[1] = GlobalUtils.getUnformattedTagName(currentTag.getName(), "Area");
                    } else if (currentTag.getName().substring(0, 4).equalsIgnoreCase("type")) {
                        tagValues[2] = GlobalUtils.getUnformattedTagName(currentTag.getName(), "Type");
                    }
                }
                if (tagValues[0] == null){
                    tagValues[0] = "[No Size]";
                    boothSkusMissingSATs.add(boothSku);
                }
                if (tagValues[1] == null){
                    tagValues[1] = "[No Area]";
                    boothSkusMissingSATs.add(boothSku);
                }
                if (tagValues[2] == null){
                    tagValues[2] = "[No Type]";
                    boothSkusMissingSATs.add(boothSku);
                }
                return tagValues;
            }
        }.execute();
    }

    ////// INACTIVE METHODS

    private void deleteAllItems() {
        DeleteAllItemsTask deleteAllItemsTask = new DeleteAllItemsTask();
        deleteAllItemsTask.execute();
    }

    private void deleteAllTags() {
        DeleteAllTagsTask deleteAllTagsTask = new DeleteAllTagsTask();
        deleteAllTagsTask.execute();
    }

    private void createCustomTender(final Context context) {
        new AsyncTask<Void, Void, Exception>() {
            private TenderConnector tenderConnector;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                tenderConnector = new TenderConnector(context, CloverAccount.getAccount(context), null);
                tenderConnector.connect();
            }

            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    tenderConnector.checkAndCreateTender(getString(R.string.custom_tender_name), getPackageName(), true, false);
                } catch (Exception e) {
                    Log.e("Clover excpt:", e.getMessage(), e.getCause());
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception e) {
                tenderConnector.disconnect();
                tenderConnector = null;
            }
        }.execute();
    }

    private void deleteCustomTender(final Context context) {
        new AsyncTask<Void, Void, Exception>() {
            private TenderConnector tenderConnector;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                tenderConnector = new TenderConnector(context, CloverAccount.getAccount(context), null);
                tenderConnector.connect();
            }

            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    tenderConnector.deleteTender("GZ4WX32MMBY9G");
                } catch (Exception e) {
                    Log.e("Clover excpt:", e.getMessage(), e.getCause());
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception e) {
                tenderConnector.disconnect();
                tenderConnector = null;
            }
        }.execute();
    }
}
