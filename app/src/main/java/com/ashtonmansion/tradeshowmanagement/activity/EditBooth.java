package com.ashtonmansion.tradeshowmanagement.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.ashtonmansion.amtradeshowmanagement.R;

import com.clover.sdk.v3.inventory.Item;

public class EditBooth extends AppCompatActivity {
    //////ACTIVITY VARS
    private Context editBoothActivityContext;
    //////UI VARS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //////////////////NAVIGATION AND UI WORK//////////////
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_booth);
        Toolbar toolbar = (Toolbar) findViewById(R.id.edit_booth_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //////////////////DATA WORK///////////////////////////
        editBoothActivityContext = this;
        Bundle extrasBundle = getIntent().getExtras();
        if (extrasBundle != null) {
            Item booth = (Item) extrasBundle.get("booth");
            TextView editBoothHeaderTv = (TextView) findViewById(R.id.edit_booth_header);
            editBoothHeaderTv.setText("Edit Booth - " + booth.getName());
        }
    }

    public void saveBoothChangesAction(View view) {
        //// TODO: 8/31/2016 
    } //// TODO: 8/31/2016  

}
