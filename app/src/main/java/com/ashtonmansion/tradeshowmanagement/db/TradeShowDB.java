package com.ashtonmansion.tradeshowmanagement.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by paul on 8/29/2016.
 */
public class TradeShowDB {
    ///////////////////SHOW TABLE
    public static final String SHOW_TABLE = "Shows";
    public static final String SHOW_ID = "_id";
    public static final String SHOW_CLOVERID = "cloverid";
    public static final String SHOW_NAME = "showname";
    public static final String SHOW_DATE = "showdate";
    public static final String SHOW_LOCATION = "showlocation";
    public static final String SHOW_NOTES = "shownotes";
    public static final String SHOW_LOCATION_AND_DATE_COLUMN = "showlocationanddate";
    ///////////////////BOOTH TABLE
    public static final String BOOTH_TABLE = "Booths";
    public static final String BOOTH_CLOVERID = "cloverid";
    public static final String BOOTH_NAME = "boothname";
    public static final String BOOTH_SKU_NUMBER = "boothskunumber";
    public static final String BOOTH_PRICE = "boothprice";
    public static final String BOOTH_SIZE = "boothsize";
    public static final String BOOTH_AREA = "bootharea";
    public static final String BOOTH_CATEGORY = "boothcategory";
    public static final String checkTableExistsSQL = "SELECT name FROM sqlite_master WHERE type='table' AND name = ?;";
    ///////////////////DB ITEMS
    private TradeShowDBHelper dbHelper;
    private SQLiteDatabase tradeShowDatabase;

    public TradeShowDB(Context context) {
        dbHelper = new TradeShowDBHelper(context);
        tradeShowDatabase = dbHelper.getWritableDatabase();
    }

    public boolean isTablePresent(String tableName) {
        Cursor cursor = tradeShowDatabase.rawQuery(checkTableExistsSQL, new String[]{tableName});
        return cursor.moveToFirst();
    }


    ////////////// BOOTH DATABASE METHODS//////////////////////////
    public boolean createBoothItem(String cloverId, String boothName, String boothSKUNumber,
                                   long boothPrice, String boothSize, String boothArea, String boothCategory) {
        ContentValues boothValues = new ContentValues();
        boothValues.put(BOOTH_CLOVERID, cloverId);
        boothValues.put(BOOTH_NAME, boothName);
        boothValues.put(BOOTH_SKU_NUMBER, boothSKUNumber);
        boothValues.put(BOOTH_PRICE, boothPrice);
        boothValues.put(BOOTH_SIZE, boothSize);
        boothValues.put(BOOTH_AREA, boothArea);
        boothValues.put(BOOTH_CATEGORY, boothCategory);
        return tradeShowDatabase.insert(BOOTH_TABLE, null, boothValues) > 0;
    }

    public boolean updateSingleBoothByCloverId(String cloverId, String boothName, String boothSKUNumber,
                                               long boothPrice, String boothSize, String boothArea, String boothCategory) {
        boolean updateSuccess = false;
        ContentValues boothValues = new ContentValues();
        boothValues.put(BOOTH_CLOVERID, cloverId);
        boothValues.put(BOOTH_NAME, boothName);
        boothValues.put(BOOTH_SKU_NUMBER, boothSKUNumber);
        boothValues.put(BOOTH_PRICE, boothPrice);
        boothValues.put(BOOTH_SIZE, boothSize);
        boothValues.put(BOOTH_AREA, boothArea);
        boothValues.put(BOOTH_CATEGORY, boothCategory);

        String cloverIdWithTicks = "'" + cloverId + "'";
        int updateResultInt = tradeShowDatabase.update(BOOTH_TABLE, boothValues, BOOTH_CLOVERID + "=" + cloverIdWithTicks, null);
        if (updateResultInt == 0) {
            updateSuccess = true;
        }
        return updateSuccess;
    }


    ////////////// SHOW DATABASE METHODS///////////////////////////
    public long createShowRecord(String cloverId, String showName, String showDate,
                                 String showLocation, String showNotes, String newShowLocationAndDateString) {
        ContentValues values = new ContentValues();
        values.put(SHOW_CLOVERID, cloverId);
        values.put(SHOW_NAME, showName);
        values.put(SHOW_DATE, showDate);
        values.put(SHOW_LOCATION, showLocation);
        values.put(SHOW_NOTES, showNotes);
        values.put(SHOW_LOCATION_AND_DATE_COLUMN, newShowLocationAndDateString);
        return tradeShowDatabase.insert(SHOW_TABLE, null, values);
    }


    public boolean updateSingleShowByCloverID(String cloverId, String showName, String showDate,
                                              String showLocation, String showNotes,
                                              String editShowLocationAndDateString) {
        ContentValues values = new ContentValues();
        values.put(SHOW_CLOVERID, cloverId);
        values.put(SHOW_NAME, showName);
        values.put(SHOW_DATE, showDate);
        values.put(SHOW_LOCATION, showLocation);
        values.put(SHOW_NOTES, showNotes);
        values.put(SHOW_LOCATION_AND_DATE_COLUMN, editShowLocationAndDateString);
        String cloverIdWithTicks = "'" + cloverId + "'";
        return tradeShowDatabase.update(SHOW_TABLE, values, SHOW_CLOVERID + "=" + cloverIdWithTicks, null) > 0;
    }

    public boolean deleteSingleShowByCloverID(String showID) {
        String showIDwithTicks = "'" + showID + "'";
        return tradeShowDatabase.delete(SHOW_TABLE, SHOW_CLOVERID + "=" + showIDwithTicks, null) > 0;
    }

    public Cursor selectSingleShowByCloverID(String showID) {
        Cursor mCursor = tradeShowDatabase.rawQuery("SELECT _id, cloverid, showname, showdate, showlocation, shownotes, showlocationanddate FROM Shows WHERE cloverid = ?", new String[]{showID});
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public Cursor selectShowRecords() {
        String[] cols = new String[]{SHOW_ID, SHOW_CLOVERID, SHOW_NAME, SHOW_DATE, SHOW_LOCATION, SHOW_NOTES, SHOW_LOCATION_AND_DATE_COLUMN};
        Cursor mCursor = tradeShowDatabase.query(true, SHOW_TABLE, cols, null, null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    //////////////////////MANUAL DATABASE MAINTENANCE METHODS/////////
    public void recreateShowsTable() {
        dbHelper.recreateShowsTable();
    }

    public void recreateBoothsTable() {
        dbHelper.recreateBoothsTable();
    }
}
