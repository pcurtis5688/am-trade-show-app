package com.ashtonmansion.tradeshowmanagement.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.ashtonmansion.amtradeshowmanagement.R;
import com.ashtonmansion.tradeshowmanagement.util.GlobalUtils;
import com.clover.sdk.v3.inventory.Category;
import com.clover.sdk.v3.inventory.Item;
import com.clover.sdk.v3.inventory.Tag;

import java.util.Arrays;
import java.util.List;

public class ReserveBoothDetails extends AppCompatActivity {
    private Context reserveBoothDetailsActivityContext;
    ///////SHOW DATA
    private Category show;
    private String showID;
    private String showName;
    private String showDate;
    private String showLocation;
    private String showNotes;
    ///////BOOTH DATA
    private Item booth;
    private List<Tag> boothTags;
    private Tag sizeTag;
    private Tag areaTag;
    private Tag categoryTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserve_booth_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /////GET DATA PASSED FROM BOOTH SELECTION
        reserveBoothDetailsActivityContext = this;
        Bundle extrasBundle = getIntent().getExtras();
        if (extrasBundle != null) {
            show = (Category) extrasBundle.get("show");
            booth = (Item) extrasBundle.get("booth");
            boothTags = booth.getTags();
            decoupleShowName(show);
            populateTagObjects();

            /////POPULATE BOOTH DATA IN UI
            TextView boothReservationHeader = (TextView) findViewById(R.id.booth_reservation_header);
            TextView boothReservationPriceTV = (TextView) findViewById(R.id.booth_reservation_details_price);
            boothReservationHeader.setText(getResources().getString(R.string.booth_reservation_details_header_text, showName, booth.getSku()));
            boothReservationPriceTV.setText(GlobalUtils.getFormattedPriceStringFromLong(booth.getPrice()));
            populateTagFields();
        }
    }

    private void decoupleShowName(Category show) {
        List<String> splitShowNameArray = Arrays.asList(show.getName().split(","));
        showName = splitShowNameArray.get(0);
        showDate = splitShowNameArray.get(1);
        showLocation = splitShowNameArray.get(2);
        showNotes = splitShowNameArray.get(3);
    }

    private void populateTagObjects() {
        for (Tag currentTag : boothTags) {
            if (currentTag.getName().substring(0, 4).equalsIgnoreCase("size")) {
                sizeTag = currentTag;
            } else if (currentTag.getName().substring(0, 4).equalsIgnoreCase("area")) {
                areaTag = currentTag;
            } else if (currentTag.getName().substring(0, 8).equalsIgnoreCase("category")) {
                categoryTag = currentTag;
            }
        }
    }

    private void populateTagFields() {
        TextView boothReservationSizeTV = (TextView) findViewById(R.id.booth_reservation_details_size);
        TextView boothReservationAreaTV = (TextView) findViewById(R.id.booth_reservation_details_area);
        TextView boothReservationCategoryTV = (TextView) findViewById(R.id.booth_reservation_details_category);

        if (sizeTag != null) {
            String unformattedSizeTagName = GlobalUtils.getUnformattedTagName(sizeTag.getName(), "Size");
            if (unformattedSizeTagName.length() > 0) {
                boothReservationSizeTV.setText(unformattedSizeTagName);
            } else {
                boothReservationSizeTV.setText(getResources().getString(R.string.booth_reservation_no_size_data));
            }
        } else {
            boothReservationSizeTV.setText(getResources().getString(R.string.booth_reservation_no_size_data));
        }
        if (areaTag != null) {
            String unformattedAreaTagName = GlobalUtils.getUnformattedTagName(areaTag.getName(), "Area");
            if (unformattedAreaTagName.length() > 0) {
                boothReservationAreaTV.setText(unformattedAreaTagName);
            } else {
                boothReservationAreaTV.setText(getResources().getString(R.string.booth_reservation_no_area_data));
            }
        } else {
            boothReservationAreaTV.setText(getResources().getString(R.string.booth_reservation_no_area_data));
        }
        if (categoryTag != null) {
            String unformattedCategoryTagName = GlobalUtils.getUnformattedTagName(categoryTag.getName(), "Category");
            if (unformattedCategoryTagName.length() > 0) {
                boothReservationCategoryTV.setText(unformattedCategoryTagName);
            } else {
                boothReservationCategoryTV.setText(getResources().getString(R.string.booth_reservation_no_category_data));
            }
        } else {
            boothReservationCategoryTV.setText(getResources().getString(R.string.booth_reservation_no_category_data));
        }
    }
}
