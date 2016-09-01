package com.ashtonmansion.tradeshowmanagement.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import com.ashtonmansion.amtradeshowmanagement.R;

import java.util.ArrayList;
import java.util.List;

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
    ///////////////DATA VARS///////////////////////
    private List<String> boothSizeDropdownList;
    private List<String> boothAreaDropdownList;
    private List<String> boothCategoryDropdownList;

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

        /////////POPULATE DROPDOWN FILTERS
        populateDropdownLists();
    }

    private void populateDropdownLists() {
        boothSizeDropdownList = new ArrayList<>();
        boothAreaDropdownList = new ArrayList<>();
        boothCategoryDropdownList = new ArrayList<>();

        //// TODO: 8/31/2016
    }

    public void applyBoothFiltersAction(View view) {
        //// TODO: 8/31/2016 filter booths
    }

}
