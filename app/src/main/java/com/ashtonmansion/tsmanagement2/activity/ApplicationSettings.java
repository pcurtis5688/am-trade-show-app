package com.ashtonmansion.tsmanagement2.activity;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ashtonmansion.tsmanagement2.R;
import com.ashtonmansion.tsmanagement2.util.GlobalUtils;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v1.tender.TenderConnector;
import com.clover.sdk.v3.base.Reference;
import com.clover.sdk.v3.inventory.Category;
import com.clover.sdk.v3.inventory.InventoryConnector;
import com.clover.sdk.v3.inventory.Item;
import com.clover.sdk.v3.inventory.PriceType;
import com.clover.sdk.v3.inventory.Tag;
import com.clover.sdk.v3.order.OrderConnector;

import java.util.ArrayList;
import java.util.List;

public class ApplicationSettings extends AppCompatActivity {
    private Context applicationSettingsActivityContext;
    private int activityHeaderResId;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_settings_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ////// SET LOCAL VARS
        handleSizing();
        applicationSettingsActivityContext = this;
        TextView activityHeaderTv = (TextView) findViewById(R.id.application_settings_header);
        activityHeaderTv.setTextAppearance(applicationSettingsActivityContext, activityHeaderResId);

        ////// FETCH BUTTONS
        final Button validateBoothNamesBtn = (Button) findViewById(R.id.validate_booth_names_btn);
        final Button deleteAllDetectedBoothsBtn = (Button) findViewById(R.id.delete_detected_booths_btn);

        ////// ADD BUTTON LISTENERS
        validateBoothNamesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateBoothNamesAndRecreateTags();
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

    private void createSelectBoothButtonInInventory() {
        new AsyncTask<Void, Void, Void>() {
            private InventoryConnector inventoryConnector;
            private List<Category> categoryList;
            private boolean hasBoothsCategory;
            private boolean hasBoothItem;
            private String existingSelectBoothItemID;
            private Category boothsCategory;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                ////// INITIALIZE VARS
                categoryList = new ArrayList<>();
                hasBoothsCategory = false;
                ////// INITIALIZE CLOVER CONNECTIONS
                inventoryConnector = new InventoryConnector(applicationSettingsActivityContext, CloverAccount.getAccount(applicationSettingsActivityContext), null);
                inventoryConnector.connect();
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    ////// ITERATE OVER CATEGORY LIST CHECK IF ALREADY HAS CATEGORY
                    categoryList = inventoryConnector.getCategories();
                    if (null != categoryList && categoryList.size() > 0) {
                        for (Category category : categoryList) {
                            if (category.getName().trim().toLowerCase().contains("booths")) {
                                hasBoothsCategory = true;
                            }
                        }
                    }
                    Log.d("AppSettings Debug", "Creating only if similar one was not found..."
                            + "\n Category similar to \'Booths\' found: " + hasBoothsCategory);

                    ////// IF DOES *NOT* HAVE CATEGORY, CREATES IT
                    if (!hasBoothsCategory) {
                        Category newBoothsCategory = new Category();
                        newBoothsCategory.setName("Booths");
                        newBoothsCategory.setSortOrder(0);
                        newBoothsCategory = inventoryConnector.createCategory(newBoothsCategory);
                    }

                    ////// ITERATE INVENTORY ITEMS CHECK FOR SELECT BOOTH
                    for (Item currentItem : inventoryConnector.getItemsWithCategories()) {
                        if (currentItem.getName().trim().toLowerCase().equals("select booth")) {
                            hasBoothItem = true;
                        }
                    }

                    ////// IF IT DOES *NOT* HAVE 'SELECT BOOTH', CREATES IT
                    if (!hasBoothItem) {
                        Item createSelectBoothItem = new Item();
                        createSelectBoothItem.setName("Select Booth");
                        createSelectBoothItem.setPrice(Long.parseLong("100"));
                        createSelectBoothItem.setCost(Long.parseLong("100"));
                        createSelectBoothItem.setPriceType(PriceType.FIXED);
                        inventoryConnector.createItem(createSelectBoothItem);
                        //Returns Item w/ Code if Necessary
                        Log.d("AppSettings", "Select Booth Item Created/ Option Enabled");
                    } else
                        Log.d("AppSettings", "Select Booth option already enabled");
                } catch (BindingException | ServiceException | ClientException | RemoteException e) {
                    Log.d("Clover E: ", e.getMessage(), e.getCause());
                    e.printStackTrace();
                } catch (Exception e2) {
                    Log.e("Non-Clover:", "Exception in Category/Item Creation Method");
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                inventoryConnector.disconnect();
                inventoryConnector = null;
            }
        }.execute();
    }

    private void validateBoothNamesAndRecreateTags() {
        new AsyncTask<Void, Void, Void>() {
            private InventoryConnector inventoryConnector;
            private int boothNamesFormatted;
            private List<String> boothsWithNoSKU;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                ////// INIT LISTS
                boothNamesFormatted = 0;
                boothsWithNoSKU = new ArrayList<>();
                ////// INITIALIZE CLOVER CONNECTIONS
                inventoryConnector = new InventoryConnector(applicationSettingsActivityContext, CloverAccount.getAccount(applicationSettingsActivityContext), null);
                inventoryConnector.connect();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    for (Item boothToCheck : inventoryConnector.getItems()) {
                        if (boothToCheck.getName().toLowerCase().contains("booth name")) {
                            ////// CREATE NEW TAGS SO INDIVIDUAL IDS
                            Tag newSizeTag = new Tag();
                            Tag newAreaTag = new Tag();
                            Tag newTypeTag = new Tag();
                            for (Tag currentTag : boothToCheck.getTags()) {
                                if (currentTag.getName().substring(0, 4).equalsIgnoreCase("size")) {
                                    newSizeTag.setName(currentTag.getName());
                                    inventoryConnector.deleteTag(currentTag.getId());
                                } else if (currentTag.getName().substring(0, 4).equalsIgnoreCase("area")) {
                                    newAreaTag.setName(currentTag.getName());
                                    inventoryConnector.deleteTag(currentTag.getId());
                                } else if (currentTag.getName().substring(0, 4).equalsIgnoreCase("type")) {
                                    newTypeTag.setName(currentTag.getName());
                                    inventoryConnector.deleteTag(currentTag.getId());
                                }
                            }
                            ////// CREATE NECESSARY STRING LIST WITH BOOTH ID
                            List<String> boothIDInStringList = new ArrayList<>();
                            String boothIDString = boothToCheck.getId();
                            boothIDInStringList.add(boothIDString);
                            ////// CLEAR CURRENT TAGS FOR ITEM
                            inventoryConnector.getItem(boothToCheck.getId()).clearTags();
                            ////// CREATE THE THREE NEW TAGS
                            newSizeTag = inventoryConnector.createTag(newSizeTag);
                            newAreaTag = inventoryConnector.createTag(newAreaTag);
                            newTypeTag = inventoryConnector.createTag(newTypeTag);
                            ////// ASSIGN THE BOOTH THE NEW TAGS
                            inventoryConnector.assignItemsToTag(newSizeTag.getId(), boothIDInStringList);
                            inventoryConnector.assignItemsToTag(newAreaTag.getId(), boothIDInStringList);
                            inventoryConnector.assignItemsToTag(newTypeTag.getId(), boothIDInStringList);
                            ////// FORMAT BOOTH NAME WITH USER-FRIENDLY TAG VALUES
                            String formattedBoothName = "Booth #" + boothToCheck.getSku() + " (" + GlobalUtils.getUnformattedTagName(newSizeTag.getName(), "Size") + ", " + GlobalUtils.getUnformattedTagName(newAreaTag.getName(), "Area") + ", " + GlobalUtils.getUnformattedTagName(newTypeTag.getName(), "Type") + ")";
                            ////// UPDATE BOOTH ITEM AND COUNTER
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
                printLog();
                if (boothNamesFormatted == 0) {
                    Toast.makeText(applicationSettingsActivityContext, getResources().getString(R.string.no_unformatted_names_found), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(applicationSettingsActivityContext, (getResources().getString(R.string.booth_names_corrected_msg, boothNamesFormatted)), Toast.LENGTH_LONG).show();
                }
            }

            private void printLog() {
                if (boothsWithNoSKU.size() > 0) {
                    String boothsNoSKUString = getResources().getString(R.string.booths_with_no_skus_detected);
                    for (String boothId : boothsWithNoSKU) {
                        boothsNoSKUString += String.format("\n %s", boothId);
                    }
                }
            }
        }.execute();
    }

    private void deleteAllDetectedBooths() {
        new AsyncTask<Void, Void, Void>() {
            private InventoryConnector inventoryConnector;
            private List<String> boothsDeletedNames;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                ////// INIT BOOTH DELETED COUNT
                boothsDeletedNames = new ArrayList<>();
                ////// INITIALIZE CLOVER CONNECTIONS
                inventoryConnector = new InventoryConnector(applicationSettingsActivityContext, CloverAccount.getAccount(applicationSettingsActivityContext), null);
                inventoryConnector.connect();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    for (Item boothToCheck : inventoryConnector.getItems()) {
                        if (boothToCheck.getName().toLowerCase().contains("booth")) {
                            inventoryConnector.deleteItem(boothToCheck.getId());
                            boothsDeletedNames.add(boothToCheck.getName());
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
                if (boothsDeletedNames.size() == 0) {
                    Toast.makeText(applicationSettingsActivityContext, getResources().getString(R.string.no_booths_detected_zero_deleted_msg), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(applicationSettingsActivityContext, getResources().getString(R.string.all_detected_booths_deleted, boothsDeletedNames.size()), Toast.LENGTH_LONG).show();
                    String boothsDeletedString = getResources().getString(R.string.booths_deleted_log_message);
                    boothsDeletedString += "\r\n Names \r\n -----";
                    for (String boothName : boothsDeletedNames) {
                        boothsDeletedString += String.format("\r\n %s", boothName);
                    }
                }
            }
        }.execute();
    }

    private void deleteAllUnusedBoothLabels() {
        new AsyncTask<Void, Void, Void>() {
            private InventoryConnector inventoryConnector;
            private List<Tag> allTagsList;
            private int numberDeletedUnusedSATTags;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                ////// INIT LISTS
                allTagsList = new ArrayList<>();
                numberDeletedUnusedSATTags = 0;
                ////// INITIALIZE CLOVER CONNECTIONS
                inventoryConnector = new InventoryConnector(applicationSettingsActivityContext, CloverAccount.getAccount(applicationSettingsActivityContext), null);
                inventoryConnector.connect();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    allTagsList = inventoryConnector.getTags();
                    for (Tag currentTag : allTagsList) {
                        if (currentTag.getName().toLowerCase().contains("size -")
                                | currentTag.getName().toLowerCase().contains("area -")
                                | currentTag.getName().toLowerCase().contains("type -")) {
                            Tag tag = currentTag;
                            List<Reference> currentTagItemReferencesList = currentTag.getItems();
                            if (null == currentTagItemReferencesList || currentTagItemReferencesList.size() == 0) {
                                inventoryConnector.deleteTag(currentTag.getId());
                                numberDeletedUnusedSATTags++;
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
                inventoryConnector = null;
                if (numberDeletedUnusedSATTags > 0) {
                    Toast.makeText(applicationSettingsActivityContext, getResources().getString(R.string.deleted_SAT_tags_found_and_number, numberDeletedUnusedSATTags), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(applicationSettingsActivityContext, getResources().getString(R.string.no_unused_SAT_tags_found), Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    private void handleSizing() {
        String platform = GlobalUtils.determinePlatform(getApplicationContext());
        if (platform.equalsIgnoreCase("station"))
            activityHeaderResId = R.style.activity_header_style_station;
        else activityHeaderResId = R.style.activity_header_style_mobile;
    }

    ////// INACTIVE METHODS
    private void checkBoothIntegrityAndCorrect() {
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
