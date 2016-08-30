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
    private TradeShowDBHelper dbHelper;
    private SQLiteDatabase tradeShowDatabase;

    public TradeShowDB(Context context) {
        dbHelper = new TradeShowDBHelper(context);
        tradeShowDatabase = dbHelper.getWritableDatabase();
    }

    public long createShowRecord(String cloverId, String showName) {
        ContentValues values = new ContentValues();
        values.put(SHOW_CLOVERID, cloverId);
        values.put(SHOW_NAME, showName);
        return tradeShowDatabase.insert(SHOW_TABLE, null, values);
    }

    public Cursor selectShowRecords() {
        String[] cols = new String[]{SHOW_ID, SHOW_CLOVERID, SHOW_NAME};
        Cursor mCursor = tradeShowDatabase.query(true, SHOW_TABLE, cols, null, null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
}
