package com.ashtonmansion.tradeshowmanagement.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;
import android.widget.Spinner;

import com.ashtonmansion.amtradeshowmanagement.R;

public class FilterBooths extends AppCompatActivity {
    ///////////////ACTIVITY AND UI VARS////////////
    private Context filterBoothsActivityContext;
    ///////////////CLOVER VARS/////////////////////
    private String showID;
    ///////////////UI VARS/////////////////////////
    private EditText boothNumberFilterField;
    private EditText boothMinPriceFilterField;
    private EditText boothMaxPriceFilterField;
    private Spinner boothSizeFilterDropdown;
    private Spinner boothAreaFilterDropdown;
    private Spinner boothCategoryFilterDropdown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /////////UI WORK AND NAVIGATION
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_booths);
        Toolbar toolbar = (Toolbar) findViewById(R.id.filter_booth_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /////////GRAB SHOW ID FROM CONFIG BOOTHS ACTIVITY
        Bundle extrasBundle = getIntent().getExtras();
        if (extrasBundle != null) {
            showID = (String) extrasBundle.get("showid");
        }

        /////////GRAB FIELDS
        filterBoothsActivityContext = this;
        boothNumberFilterField = (EditText) findViewById(R.id.booth_filter_boothnumber_field);
        boothMinPriceFilterField = (EditText) findViewById(R.id.booth_filter_min_price_field);
        boothMaxPriceFilterField = (EditText) findViewById(R.id.booth_filter_max_price_field);
        boothSizeFilterDropdown = (Spinner) findViewById(R.id.booth_filter_size_dropdown);
        boothAreaFilterDropdown = (Spinner) findViewById(R.id.booth_filter_area_dropdown);
        boothCategoryFilterDropdown = (Spinner) findViewById(R.id.booth_filter_category_dropdown);

    }

}
