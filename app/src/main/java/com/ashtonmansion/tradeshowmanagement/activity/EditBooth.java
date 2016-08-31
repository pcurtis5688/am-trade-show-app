package com.ashtonmansion.tradeshowmanagement.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.ashtonmansion.amtradeshowmanagement.R;

import com.clover.sdk.v3.inventory.Item;

public class EditBooth extends AppCompatActivity {
    //////ACTIVITY VARS
    //private Context editBoothActivityContext;
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
        //editBoothActivityContext = this;
        Bundle extrasBundle = getIntent().getExtras();
        if (extrasBundle != null) {
            Item booth = (Item) extrasBundle.get("booth");
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
        //// TODO: 8/31/2016 
    } //// TODO: 8/31/2016

}
