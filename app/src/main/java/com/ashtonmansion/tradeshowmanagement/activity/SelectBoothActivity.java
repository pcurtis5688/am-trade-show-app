package com.ashtonmansion.tradeshowmanagement.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ashtonmansion.amtradeshowmanagement.R;
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.Intents;
import com.clover.sdk.v3.inventory.InventoryConnector;
import com.clover.sdk.v3.inventory.Item;
import com.clover.sdk.v3.order.LineItem;
import com.clover.sdk.v3.order.Order;
import com.clover.sdk.v3.order.OrderConnector;

import java.util.ArrayList;
import java.util.List;

public class SelectBoothActivity extends Activity {
    private Context selectBoothActivityContext;
    ///// DATA PASSED FROM CLOVER
    private String orderID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_booth);
        setResult(RESULT_CANCELED);
        // REMEMBER THERE ARE SOME FURTHER EXAMPLES OF CODE I REMOVED
        selectBoothActivityContext = this;
        Intent intent = getIntent();
        Bundle extrasBundle = intent.getExtras();

        orderID = getIntent().getStringExtra(Intents.EXTRA_ORDER_ID);
        setupViews();
    }

    public void setupViews() {

        Button approveButton = (Button) findViewById(R.id.select_booth_accept_button);
        approveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                // data.putExtra(Intents.EXTRA_AMOUNT, amount);
                //    data.putExtra(Intents.EXTRA_CLIENT_ID, Utils.nextRandomId());
                //    data.putExtra(Intents.EXTRA_NOTE, "Transaction Id: " + Utils.nextRandomId());
                Toast.makeText(selectBoothActivityContext, "Working...", Toast.LENGTH_LONG).show();

                setResult(RESULT_OK, data);
                finish();
            }
        });

        Button declineButton = (Button) findViewById(R.id.select_booth_decline_button);
        declineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                data.putExtra(Intents.EXTRA_DECLINE_REASON, "You pressed the decline button");
                Toast.makeText(selectBoothActivityContext, "Working...", Toast.LENGTH_LONG).show();
                setResult(RESULT_CANCELED, data);
                finish();
            }
        });

        Button swapGenericForSelectedBtn = (Button) findViewById(R.id.swap_generic_for_reserved_btn);
        swapGenericForSelectedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                swapGenericForSelected();
            }
        });
    }

    private void swapGenericForSelected() {
        new AsyncTask<Void, Void, Void>() {
            private InventoryConnector inventoryConnector;
            private OrderConnector orderConnector;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                inventoryConnector = new InventoryConnector(selectBoothActivityContext, CloverAccount.getAccount(selectBoothActivityContext), null);
                inventoryConnector.connect();
                orderConnector = new OrderConnector(selectBoothActivityContext, CloverAccount.getAccount(selectBoothActivityContext), null);
                orderConnector.connect();
            }

            @Override
            protected Void doInBackground(Void... params) {
                String genericBoothID = null;

                try {
                    Order utilityOrder = orderConnector.getOrder(orderID);
                    List<LineItem> lineItemList = utilityOrder.getLineItems();
                    List<LineItem> swappedLineItemList = new ArrayList<>();
                    for (LineItem lineItem : lineItemList) {
                        if (lineItem.getName().contains("Booth (Generic)"))
                            genericBoothID = lineItem.getId();
                        else swappedLineItemList.add(lineItem);
                    }
                    if (null != genericBoothID) {
                        List<String> itemsToRemoveIncludingGenericBooth = new ArrayList<>();
                        itemsToRemoveIncludingGenericBooth.add(genericBoothID);
                        orderConnector.deleteLineItems(orderID, itemsToRemoveIncludingGenericBooth);

                        for (Item item : inventoryConnector.getItems()) {
                            if (item.getSku().equalsIgnoreCase("25"))
                                orderConnector.addFixedPriceLineItem(orderID, item.getId(), null, null);
                        }
                    }
                } catch (Exception e) {
                    Log.d("Clover Excpt:", e.getMessage(), e.getCause());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                orderConnector.disconnect();
                inventoryConnector.disconnect();
                orderConnector = null;
                inventoryConnector = null;
            }
        }.execute();
    }
}
