package com.ashtonmansion.tradeshowmanagement.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by paul on 8/29/2016.
 */
public class TradeShowDB {
    public static final String SHOW_TABLE = "Shows";
    public static final String SHOW_ID = "_id";
    public static final String SHOW_CLOVERID = "cloverid";
    public static final String SHOW_NAME = "showname";
    public static final String SHOW_DATE = "showdate";
    public static final String SHOW_LOCATION = "showlocation";
    public static final String SHOW_NOTES = "shownotes";
    public static final String SHOW_LOCATION_AND_DATE_COLUMN = "showlocationanddate";
    private TradeShowDBHelper dbHelper;
    private SQLiteDatabase tradeShowDatabase;

    public TradeShowDB(Context context) {
        dbHelper = new TradeShowDBHelper(context);
        tradeShowDatabase = dbHelper.getWritableDatabase();
    }

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

    public Cursor selectShowRecords() {
        String[] cols = new String[]{SHOW_ID, SHOW_CLOVERID, SHOW_NAME, SHOW_DATE, SHOW_LOCATION, SHOW_NOTES, SHOW_LOCATION_AND_DATE_COLUMN};
        Cursor mCursor = tradeShowDatabase.query(true, SHOW_TABLE, cols, null, null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public Cursor selectSingleShowByCloverID(String showID) {
        Cursor mCursor = tradeShowDatabase.rawQuery("SELECT _id, cloverid, showname, showdate, showlocation, shownotes, showlocationanddate FROM Shows WHERE cloverid = ?", new String[]{showID});
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public boolean deleteSingleShowByCloverID(String showID) {
        return tradeShowDatabase.delete(SHOW_TABLE, SHOW_CLOVERID + "=" + showID, null) > 0;
    }
}
