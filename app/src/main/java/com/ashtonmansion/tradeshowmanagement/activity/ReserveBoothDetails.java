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

            decoupleShowName(show);

            TextView boothReservationHeader = (TextView) findViewById(R.id.booth_reservation_header);
            TextView boothReservationPriceTV = (TextView) findViewById(R.id.booth_reservation_details_price);
            //TextView boothReservationSizeTV = (TextView) findViewById(R.id.booth_reservation_details_size);
            //TextView boothReservationAreaTV = (TextView) findViewById(R.id.booth_reservation_details_area);
            //TextView boothReservationCategoryTV = (TextView) findViewById(R.id.booth_reservation_details_category);

            boothReservationHeader.setText(showName + " - Booth Number: " + booth.getSku());
            boothReservationPriceTV.setText(GlobalUtils.getFormattedPriceStringFromLong(booth.getPrice()));
            //boothReservationSizeTV.setText();
        }
    }

    private void decoupleShowName(Category show) {
        List<String> splitShowNameArray = Arrays.asList(show.getName().split(","));
        showName = splitShowNameArray.get(0);
        showDate = splitShowNameArray.get(1);
        showLocation = splitShowNameArray.get(2);
        showNotes = splitShowNameArray.get(3);
    }
}
