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
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v1.tender.TenderConnector;
import com.clover.sdk.v3.base.Tender;
import com.clover.sdk.v3.inventory.InventoryConnector;
import com.clover.sdk.v3.inventory.Item;
import com.clover.sdk.v3.inventory.Tag;

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

        Button deleteAllItemsBtn = (Button) findViewById(R.id.quick_ops_delete_all);
        deleteAllItemsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteAllItems();
            }
        });

        Button deleteAllTagsBtn = (Button) findViewById(R.id.quick_ops_delete_all_tags);
        deleteAllTagsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteAllTags();
            }
        });

        Button createCustomTenderBtn = (Button) findViewById(R.id.create_custom_tender_btn);
        createCustomTenderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createCustomTender(view.getContext());
            }
        });

        Button deleteCustomTenderBtn = (Button) findViewById(R.id.delete_custom_tender_btn);
        deleteCustomTenderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteCustomTender(view.getContext());
            }
        });
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

    private void deleteAllItems() {
        DeleteAllItemsTask deleteAllItemsTask = new DeleteAllItemsTask();
        deleteAllItemsTask.execute();
    }

    private void deleteAllTags() {
        DeleteAllTagsTask deleteAllTagsTask = new DeleteAllTagsTask();
        deleteAllTagsTask.execute();
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


}
