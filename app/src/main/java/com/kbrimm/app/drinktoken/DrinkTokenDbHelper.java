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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
    // Table name strings
    public static final String META_TABLE = "meta_data";
    public static final String LOG_TABLE = "drink_log";
    // Column name strings
    public static final String META_DATE_COLUMN = "created_date";
    public static final String LOG_DATE_COLUMN = "log_date";
    public static final String LOG_COUNT_COLUMN = "drink_count";
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
        String createMeta = "CREATE TABLE " + META_TABLE + " (" +
                META_DATE_COLUMN + " DATE);";
        String createCounts = "CREATE TABLE " + LOG_TABLE +
                " (" + LOG_DATE_COLUMN + " DATE, " + LOG_COUNT_COLUMN +
                " INTEGER);";
        String addCreatedDate = "INSERT INTO " + META_TABLE + " VALUES (" +
                getToday() + ");";

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

    protected void clearData() {
        String createMeta = "DELETE FROM " + META_TABLE;
        String createCounts = "DELETE FROM " + LOG_TABLE;
        String addCreatedDate = "INSERT INTO " + META_TABLE + " VALUES (" +
                getToday() + ");";

        SQLiteDatabase db = getWritableDatabase();
        // Create tables
        db.execSQL(createMeta);
        db.execSQL(createCounts);
        // Store created_date in meta_data
        db.execSQL(addCreatedDate);
    }

    // Ensures today is in log, increments drink count
    protected void incrementCount() {
        /* INSERT INTO drink_log
         *   VALUES ({getToday()}, 1);
         */

        String selectQuery = "SELECT * FROM " + LOG_TABLE + " WHERE " +
                LOG_DATE_COLUMN + " = ?";
        String[] selectQueryArgs = {getToday()};
        String insertQuery = "INSERT INTO " + LOG_TABLE + " VALUES ('" +
                getToday() + "', 1);";
        String updateQuery = "UPDATE " + LOG_TABLE + " SET " + LOG_COUNT_COLUMN +
                " = " + LOG_COUNT_COLUMN + " + 1 WHERE " + LOG_DATE_COLUMN +
                " = '" + getToday() + "'";

        // Get database, begin transaction
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, selectQueryArgs);
        // First, attempt a select
        try {
            if(cursor.getCount() > 0) {
                // If results are returned, update
                db.execSQL(updateQuery);
                Log.d(TAG, "incrementCount: Updated");
            } else {
                // Otherwise insert
                db.execSQL(insertQuery);
                Log.d(TAG, "incrementCount: Inserted");
            }
        } catch (Exception oops) {
            Log.d(TAG, "incrementCount: Unexpected error.");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    // Returns today's total drinks
    protected int getDailyCount() {
        /*
         * SELECT SUM(drink_count)
         *   FROM drink_log
         *   WHERE date = date('now', 'localtime');
         */
        String[] projection = {"SUM(" + LOG_COUNT_COLUMN + ")", LOG_DATE_COLUMN};
        String selection = LOG_DATE_COLUMN + " = ?";
        String[] selectionArgs = {getToday()};
        String groupBy = null;
        String having = null;
        String orderBy = null;
        String limit = null;
        int result = -1;
        String dateString = "";
        // Get database and cursor
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(LOG_TABLE, projection, selection, selectionArgs,
                groupBy, having, orderBy, limit);
        try {
            if (cursor.moveToFirst()) {
                result = cursor.getInt(0);
                dateString = cursor.getString(1);
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
            Log.d(TAG, "getDailyCount: Returning " + result + ", " + dateString);
        }

        return result;
    }

    protected int getWeeklyCount() {
        /*
         * SELECT SUM(drink_count)
         *   FROM drink_log
         *   WHERE date BETWEEN oneWeekAgo() AND getToday();
         */
        String query = "SELECT SUM("+ LOG_COUNT_COLUMN + ") FROM "
                + LOG_TABLE + " WHERE " + LOG_DATE_COLUMN + " BETWEEN ? AND ?";
        String[] queryArgs = {getOneWeekAgo(), getToday()};
        int result = -1;
        // Get database and cursor
        SQLiteDatabase db = this.getReadableDatabase();
        //Cursor cursor = db.query(table, projection, selection, selectionArgs,
        //        groupBy, having, orderBy, limit);
        Cursor cursor = db.rawQuery(query, queryArgs);
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
        double result = 1.0 * totalDrinks/totalDays;
        Log.d(TAG, "getDailyAvg: Returning: " + result);
        return result;
    }

    protected double getWeeklyAvg() {
        int totalDrinks = getTotalDrinks();
        int totalWeeks = getElapsedWeeks();
        double result = 1.0 * totalDrinks/totalWeeks;
        Log.d(TAG, "getWeeklyAvg: Returning " + result);
        return result;
    }

    private String getToday() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date calDate = Calendar.getInstance().getTime();
        String today = df.format(calDate);
        Log.d(TAG, "getToday: " + today);
        return today;
    }

    private String getOneWeekAgo() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date calDate = Calendar.getInstance().getTime();
        calDate = new Date(calDate.getTime() - 6 * 24 * 3600 * 1000l);
        String oneWeekAgo = df.format(calDate);
        Log.d(TAG, "getOneWeekAgo: " + oneWeekAgo);
        return oneWeekAgo;
    }

    private int getTotalDrinks() {
         /*
         * SELECT SUM(drink_count)
         *   FROM drink_log;
         */
        String table = LOG_TABLE;
        String[] projection = {"SUM(" +
                LOG_COUNT_COLUMN + ")"};
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
            Log.d(TAG, "getTotalDrinks: Returning " + result);
        }
        return result;
    }

    private int getElapsedDays() {
        /*
         * SELECT 1 + CAST(strftime('%J', date('now', 'localtime')) AS INT) -
         *   (SELECT CAST(strftime('%J', created_date) AS INT) FROM meta_data);
         */
        String table = META_TABLE;
        String[] projection = {"1 + CAST(strftime('%J', " + getToday() +
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
            Log.d(TAG, "getElapsedDays: Returning " + result);
        }
        return result;
    }

    private int getElapsedWeeks() {
        int result = 1+((getElapsedDays()-1)/7);
        Log.d(TAG, "getElapsedWeeks: Returning " + result);
        return result;
    }
}
