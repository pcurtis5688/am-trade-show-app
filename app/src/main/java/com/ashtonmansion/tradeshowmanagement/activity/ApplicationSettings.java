package com.ashtonmansion.tradeshowmanagement.activity;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ashtonmansion.amtradeshowmanagement.R;
import com.ashtonmansion.tradeshowmanagement.util.GlobalUtils;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.tender.TenderConnector;
import com.clover.sdk.v3.inventory.InventoryConnector;
import com.clover.sdk.v3.inventory.Item;
import com.clover.sdk.v3.inventory.Tag;
import com.clover.sdk.v3.order.OrderConnector;

import java.util.ArrayList;
import java.util.List;

public class ApplicationSettings extends AppCompatActivity {
    private Context applicationSettingsActivityContext;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_settings_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        applicationSettingsActivityContext = this;

        final Button checkBoothIntegrityBtn = (Button) findViewById(R.id.check_booth_integrity_btn);
        final Button validateBoothNamesBtn = (Button) findViewById(R.id.validate_booth_names_btn);
        final Button deleteAllDetectedBoothsBtn = (Button) findViewById(R.id.delete_detected_booths_btn);

        checkBoothIntegrityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkBoothIntegrityAndCorrect();
                checkBoothIntegrityBtn.setEnabled(false);
            }
        });
        validateBoothNamesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateBoothNamesAndCorrect();
                validateBoothNamesBtn.setEnabled(false);
            }
        });
        deleteAllDetectedBoothsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAllDetectedBooths();
                deleteAllDetectedBoothsBtn.setEnabled(false);
            }
        });
    }

    private boolean checkBoothIntegrityAndCorrect() {
        new AsyncTask<Void, Void, Void>() {
            private InventoryConnector inventoryConnector;
            private OrderConnector orderConnector;
            private List<Item> reservedBoothsWithInvalidatedOrders;
            private int invalidBoothNo;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                ////// INITIALIZE LISTS
                reservedBoothsWithInvalidatedOrders = new ArrayList<>();
                invalidBoothNo = 0;
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
                        if (null != boothToCheck.getCode() && boothToCheck.getCode().contains("Order #")) {
                            String orderNumber = boothToCheck.getCode().substring(7);
                            if (null == orderConnector.getOrder(orderNumber)) {
                                reservedBoothsWithInvalidatedOrders.add(boothToCheck);
                                inventoryConnector.updateItem(boothToCheck.setCode("Available"));
                                invalidBoothNo++;
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.d("Excptn: ", e.getMessage(), e.getCause());
                    e.printStackTrace();
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
                    Toast.makeText(applicationSettingsActivityContext, getResources().getString(R.string.invalid_booths_found_and_corrected, invalidBoothNo), Toast.LENGTH_LONG).show();
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
            private int boothNamesFormatted;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                ////// INIT LISTS
                boothNamesFormatted = 0;
                ////// INITIALIZE CLOVER CONNECTIONS
                inventoryConnector = new InventoryConnector(applicationSettingsActivityContext, CloverAccount.getAccount(applicationSettingsActivityContext), null);
                inventoryConnector.connect();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    for (Item boothToCheck : inventoryConnector.getItems()) {
                        if (boothToCheck.getName().toLowerCase().contains("booth name")) {
                            String[] tagValues = fetchTagValues(boothToCheck.getSku(), boothToCheck.getTags());
                            String formattedBoothName = "Booth #" + boothToCheck.getSku() + " (" + tagValues[0] + ", " + tagValues[1] + ", " + tagValues[2] + ")";
                            inventoryConnector.updateItem(boothToCheck.setName(formattedBoothName));
                            boothNamesFormatted++;
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
                if (boothNamesFormatted == 0) {
                    Toast.makeText(applicationSettingsActivityContext, getResources().getString(R.string.no_unformatted_names_found), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(applicationSettingsActivityContext, (getResources().getString(R.string.booth_names_corrected_msg, boothNamesFormatted)), Toast.LENGTH_LONG).show();
                }
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
                if (tagValues[0] == null) {
                    tagValues[0] = "[No Size]";
                }
                if (tagValues[1] == null) {
                    tagValues[1] = "[No Area]";
                }
                if (tagValues[2] == null) {
                    tagValues[2] = "[No Type]";
                }
                return tagValues;
            }
        }.execute();
    }

    private void deleteAllDetectedBooths() {
        new AsyncTask<Void, Void, Void>() {
            private InventoryConnector inventoryConnector;
            private int boothsDeletedNo;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                ////// INIT BOOTH DELETED COUNT
                boothsDeletedNo = 0;
                ////// INITIALIZE CLOVER CONNECTIONS
                inventoryConnector = new InventoryConnector(applicationSettingsActivityContext, CloverAccount.getAccount(applicationSettingsActivityContext), null);
                inventoryConnector.connect();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    for (Item boothToCheck : inventoryConnector.getItems()) {
                        if (boothToCheck.getName().contains("booth") ||
                                boothToCheck.getName().contains("Booth") ||
                                boothToCheck.getName().contains("Booth #") ||
                                boothToCheck.getName().contains("booth #")) {
                            inventoryConnector.deleteItem(boothToCheck.getId());
                            boothsDeletedNo++;
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
                if (boothsDeletedNo == 0) {
                    Toast.makeText(applicationSettingsActivityContext, getResources().getString(R.string.no_booths_detected_zero_deleted_msg), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(applicationSettingsActivityContext, getResources().getString(R.string.all_detected_booths_deleted, boothsDeletedNo), Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    ////// INACTIVE METHODS

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
