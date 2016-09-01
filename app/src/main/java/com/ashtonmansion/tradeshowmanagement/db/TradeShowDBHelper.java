package com.ashtonmansion.tradeshowmanagement.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by paul on 8/29/2016.
 */
public class TradeShowDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "AMTradeShowDB";
    private static final int DATABASE_VERSION = 1;

    // SHOW_TABLE creation sql statement
    private static final String CREATE_SHOW_TABLE = "CREATE TABLE " +
            "Shows(_id integer primary key, " +
            "cloverid text not null, " +
            "showname text not null, " +
            "showdate text not null, " +
            "showlocation text not null, " +
            "shownotes text not null, " +
            "showlocationanddate text not null);";

    // BOOTH_TABLE creation sql statement
    private static final String CREATE_BOOTH_TABLE = "CREATE TABLE " +
            "Booths(_id integer primary key, " +
            "cloverid text not null, " +
            "boothname text not null, " +
            "boothskunumber text not null, " +
            "boothprice text not null, " +
            "boothsize text not null, " +
            "bootharea text not null, " +
            "boothcategory text not null);";

    public TradeShowDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Method is called during creation of the database
    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(CREATE_SHOW_TABLE);
        database.execSQL(CREATE_BOOTH_TABLE);
    }

    // Method is called during an upgrade of the database,
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        /////DROP AND RECREATE SHOW TABLE
        Log.w(TradeShowDBHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", dropping and recreating SHOW TABLE");
        database.execSQL("DROP TABLE IF EXISTS Shows");

        /////DROP AND RECREATE BOOTH TABLE
        Log.w(TradeShowDBHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", dropping and recreating BOOTH TABLE");
        database.execSQL("DROP TABLE IF EXISTS Booths");

        //RECREATE DATABASE AND TABLES
        onCreate(database);
    }

    //////////////////////MANUAL DATABASE MAINTENANCE METHODS/////////
    public void recreateShowsTable() {
        getWritableDatabase().execSQL("DROP TABLE IF EXISTS Shows");
        getWritableDatabase().execSQL(CREATE_SHOW_TABLE);
    }

    public void recreateBoothsTable() {
        getWritableDatabase().execSQL("DROP TABLE IF EXISTS Booths");
        getWritableDatabase().execSQL(CREATE_BOOTH_TABLE);
    }
}
