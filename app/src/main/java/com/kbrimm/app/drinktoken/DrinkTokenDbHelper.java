/*
 * DrinkToken
 *     Copyright (c) 2016 Katy Brimm
 *     This source file is licensed under the BSD 2-Clause License.
 *     Please see the file LICENSE in this distribution for license terms.
 * Contact: katy.brimm@gmail.com
 */

package com.kbrimm.app.drinktoken;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Date;

/**
 * Use: Handles app interactions with database
 * Access points: incrementCount() - adds one to today's drink count
 *                getDailyCount() - returns int for today's drink count
 *                getWeeklyCount() - returns int for past 7 days' count
 *                getDailyAvg() - returns double for total drinks/total days
 *                getWeeklyAvg() - returns double for total drinks/total weeks
 * Author: K Brimm
 * Date: 7/24/16
 */
public class DrinkTokenDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "DrinkToken.db";
    private static final String TAG = "DrinkToken";
    // Frequently used date query parts
    private static final String TODAYS_DATE = "date('now', 'localtime')";
    private static final String ONE_WEEK_AGO = "date('now', 'localtime', " +
            "'-6 days')";
    private static final String COLUMN_NAME_NULLABLE = null;
    // Singleton database
    private static DrinkTokenDbHelper INSTANCE;


    // Singleton database initialized using getInstance(context)
    public static synchronized DrinkTokenDbHelper getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new DrinkTokenDbHelper(context.getApplicationContext());
        }
        return INSTANCE;
    }

    // Constructor should never be called directly.
    private DrinkTokenDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Sets two tables, meta_data and drink_log. Initializes created_date
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createMeta = "CREATE TABLE " + MetaContract.MetaEntry.TABLE_NAME +
                " (" + MetaContract.MetaEntry.COLUMN_NAME_DATE + " DATE);";
        String createCounts = "CREATE TABLE " + DrinkContract.DrinkEntry.TABLE_NAME +
                " (" + DrinkContract.DrinkEntry.COLUMN_NAME_DATE + " DATE, " +
                DrinkContract.DrinkEntry.COLUMN_NAME_COUNT + " INTEGER);";
        String addCreatedDate = "INSERT INTO " + MetaContract.MetaEntry.TABLE_NAME +
                " VALUES (" + TODAYS_DATE + ");";

        // Create tables
        db.execSQL(createMeta);
        db.execSQL(createCounts);
        // Store created_date in meta_data
        db.execSQL(addCreatedDate);
    }

    // Not implemented
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         // To do: Implement upgrade policy
    }

    // Not implemented
    public void onDowngrade(SQLiteDatabase db, int oldVersion,
                            int newVersion) {
        // To do: Implement downgrade policy
    }

    // Ensures today is in log, increments drink count
    protected void incrementCount() {
        /* INSERT INTO drink_log
         *   VALUES ({TODAYS_DATE}, 1);
         */
        String table = DrinkContract.DrinkEntry.TABLE_NAME;
        String columnDate = DrinkContract.DrinkEntry.COLUMN_NAME_DATE;
        String columnCount = DrinkContract.DrinkEntry.COLUMN_NAME_COUNT;
        ContentValues insertValues = new ContentValues();
        insertValues.put(columnDate, TODAYS_DATE);
        insertValues.put(columnCount, 1);
        // Get database, begin transaction
        SQLiteDatabase db = getWritableDatabase();
        // First, attempt an insert
        try {
            db.insertOrThrow(table, COLUMN_NAME_NULLABLE, insertValues);
        } catch (Exception oops) {
            Log.d(TAG, "incrementCount: Unexpected error.");
        }
    }

    // Returns today's total drinks
    protected int getDailyCount() {
        /*
         * SELECT SUM(drink_count)
         *   FROM drink_log
         *   WHERE date = date('now', 'localtime');
         */
        String table = DrinkContract.DrinkEntry.TABLE_NAME;
        String columnDate = DrinkContract.DrinkEntry.COLUMN_NAME_DATE;
        String columnCount = DrinkContract.DrinkEntry.COLUMN_NAME_COUNT;
        String[] projection = {"SUM(" + columnCount + ")"};
        String selection = columnDate + " = ?";
        String[] selectionArgs = {TODAYS_DATE};
        String groupBy = null;
        String having = null;
        String orderBy = null;
        String limit = null;
        int result = -1;
        // Get database and cursor
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(table, projection, selection, selectionArgs,
                groupBy, having, orderBy, limit);
        try {
            if (cursor.moveToFirst()) {
                result = cursor.getInt(0);
            } else {
                result = 0;
            }
        } catch(Exception oops) {
            // To do: Handle exception
            Log.d(TAG, "getDailyCount: Unexpected error.");
            result = 0;
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return result;
    }

    protected int getWeeklyCount() {
        /*
         * SELECT SUM(drink_count)
         *   FROM drink_log
         *   WHERE date > date(julianday('now', 'localtime') - 7);
         */
        String table = DrinkContract.DrinkEntry.TABLE_NAME;
        String columnDate = DrinkContract.DrinkEntry.COLUMN_NAME_DATE;
        String columnCount = DrinkContract.DrinkEntry.COLUMN_NAME_COUNT;
        String[] projection = {"SUM(" + columnCount + ")"};
        String selection = columnDate + " BETWEEN ? AND ?";
        String[] selectionArgs = {TODAYS_DATE, ONE_WEEK_AGO};
        String groupBy = null;
        String having = null;
        String orderBy = null;
        String limit = null;
        int result = -1;
        // Get database and cursor
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(table, projection, selection, selectionArgs,
                groupBy, having, orderBy, limit);
        try {
            if (cursor.moveToFirst()) {
                do {
                    result = cursor.getInt(0);
                } while (cursor.moveToNext());
            } else {
                result = 0;
            }
        } catch(Exception oops) {
            // To do: Handle exception
            Log.d(TAG, "getWeeklyCount: Unexpected error.");
            result = 0;
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            Log.d(TAG, "getWeeklyCount: Returning " + result);
        }
        return result;
    }

    protected double getDailyAvg() {
        int totalDrinks = getTotalDrinks();
        int totalDays = getElapsedDays();
        return totalDrinks/totalDays;
    }

    protected double getWeeklyAvg() {
        int totalDrinks = getTotalDrinks();
        int totalWeeks = getElapsedWeeks();
        return totalDrinks/totalWeeks;
    }

    private int getTotalDrinks() {
         /*
         * SELECT SUM(drink_count)
         *   FROM drink_log;
         */
        String table = DrinkContract.DrinkEntry.TABLE_NAME;
        String[] projection = {"SUM(" +
                DrinkContract.DrinkEntry.COLUMN_NAME_COUNT + ")"};
        String selection = null;
        String[] selectionArgs = null;
        String groupBy = null;
        String having = null;
        String orderBy = null;
        String limit = null;
        int result = -1;
        // Get database and cursor
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(table, projection, selection, selectionArgs,
                groupBy, having, orderBy, limit);
        try {
            if (cursor.moveToFirst()) {
                result = cursor.getInt(0);
            } else {
                result = 0;
            }
        } catch(Exception oops) {
            // To do: Handle exception
            Log.d(TAG, "getTotalDrinks: Unexpected error.");
            result = 0;
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return result;
    }

    private int getElapsedDays() {
        /*
         * SELECT 1 + CAST(strftime('%J', date('now', 'localtime')) AS INT) -
         *   (SELECT CAST(strftime('%J', created_date) AS INT) FROM meta_data);
         */
        String table = MetaContract.MetaEntry.TABLE_NAME;
        String[] projection = {"1 + CAST(strftime('%J', " + TODAYS_DATE +
                ") AS INT) - (SELECT CAST(strftime('%J', created_date) " +
                "AS INT) FROM meta_data)"};
        String selection = null;
        String[] selectionArgs = null;
        String groupBy = null;
        String having = null;
        String orderBy = null;
        String limit = null;
        int result = -1;
        // Get database and cursor
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(table, projection, selection,
                selectionArgs, groupBy, having, orderBy, limit);
        try {
            if (cursor.moveToFirst()) {
                result = cursor.getInt(0);
            } else {
                result = 0;
            }
        } catch(Exception oops) {
            // To do: Handle exception
            Log.d(TAG, "getElapsedDays: Unexpected error.");
            result = 0;
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return result;
    }

    private int getElapsedWeeks() {
        return 1+((getElapsedDays()-1)/7);
    }
}
