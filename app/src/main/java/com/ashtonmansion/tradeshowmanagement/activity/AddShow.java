package com.ashtonmansion.tradeshowmanagement.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.ashtonmansion.amtradeshowmanagement.R;

public class AddShow extends AppCompatActivity {
    private Context addShowActivityContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ///////NAVIGATION WORK ///////////////////////////////////
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_show);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ///////ACTIVITY CONTEXT AND UI FIELD WORK //////////////////
        addShowActivityContext = this;

    }

    ////////DATA METHODS AND BUTTON ACTIONS////////////////////
    public void cancelAddNewShowAction(View view) {
        finish();
    }

    public void addNewShowAction(View view) {
    }
}
