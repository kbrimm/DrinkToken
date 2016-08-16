/*
 * DrinkToken
 *     Copyright (c) 2016 Katy Brimm
 *     This source file is licensed under the BSD 2-Clause License.
 *     Please see the file LICENSE in this distribution for license terms.
 * Contact: info@drinktokenapp.com
 */

package com.kbrimm.app.drinktoken;

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
    // Singleton database
    private static DrinkTokenDbHelper INSTANCE;

    /**
     * Checks to see if an instance of the database already exists.
     * If so, returns that. Else creates a new instance.
     *
     * @param context context from which method is called
     * @return        the single instance of the application database
     */
    public static synchronized DrinkTokenDbHelper getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new DrinkTokenDbHelper(context.getApplicationContext());
        }
        return INSTANCE;
    }

    /**
     * Database instantiator. Only ever called through
     * DrinkTokenDbHelper.getInstance().
     *
     * @param context context from which method is called
     */
    private DrinkTokenDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Run only on first launch of app. Creates tables meta_data and
     * drink_count. Inserts today's date into meta_data.created_date.
     *
     * @param db a writable instance of the DrinkToken database
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createMeta = "CREATE TABLE " + META_TABLE + " (" +
                META_DATE_COLUMN + " DATE);";
        String createCounts = "CREATE TABLE " + LOG_TABLE +
                " (" + LOG_DATE_COLUMN + " DATE, " + LOG_COUNT_COLUMN +
                " INTEGER);";
        String addCreatedDate = "INSERT INTO " + META_TABLE + " VALUES ('" +
                getToday() + "');";

        // Create tables
        db.execSQL(createMeta);
        db.execSQL(createCounts);
        // Store created_date in meta_data
        db.execSQL(addCreatedDate);
    }

    /**
     * Not implemented at this time.
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         // To do: Implement upgrade policy
    }

    /**
     * Not implemented at this time.
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    public void onDowngrade(SQLiteDatabase db, int oldVersion,
                            int newVersion) {
        // To do: Implement downgrade policy
    }

    /**
     * Drops all data from application database. Repopulates
     * meta_data.created_date with today's date.
     *
     * No values accepted or returned.
     */
    protected void clearData() {
        String createMeta = "DELETE FROM " + META_TABLE;
        String createCounts = "DELETE FROM " + LOG_TABLE;
        String insertCreatedDate = "INSERT INTO " + META_TABLE + " VALUES ('" +
                getToday() + "');";

        SQLiteDatabase db = getWritableDatabase();
        // Create tables
        db.execSQL(createMeta);
        db.execSQL(createCounts);
        // Store created_date in meta_data
        db.execSQL(insertCreatedDate);
    }

    /**
     * Queries database, checks to see if today exists in
     * drink_log.created_date.
     * If not, creates today's entry with a drink_count of 1.
     * Else increments today's entry for drink_count by 1.
     *
     * No values accepted or returned.
     */
    protected void incrementCount() {
        /*
         * SELECT * FROM drink_log
         *   WHERE log_date = 'getToday()';
         */
        String selectQuery = "SELECT * FROM " + LOG_TABLE + " WHERE " +
                LOG_DATE_COLUMN + " = ?";
        String[] selectQueryArgs = {getToday()};
        /*
         * INSERT INTO drink_log
         *   VALUES ('getToday()', 1);
         */
        String insertQuery = "INSERT INTO " + LOG_TABLE + " VALUES ('" +
                getToday() + "', 1);";
        /*
         * UPDATE drink_log
         *   SET drink_count = drink_count + 1
         *   WHERE log_date = 'getToday()';
         */
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
                Log.d(TAG, "incrementCount: Updated.");
            } else {
                // Otherwise insert
                db.execSQL(insertQuery);
                Log.d(TAG, "incrementCount: Inserted.");
            }
        } catch (Exception oops) {
            Log.d(TAG, "incrementCount: Unexpected error.");
        } finally {
            if (cursor != null && !cursor.isClosed()) { cursor.close(); }
        }
    }

    /**
     * Queries database, checks to see if today exists in
     * drink_log.created_date and has a drink count of > 0.
     * If yes, decrements today's entry for drink_count by 1.
     *
     * @return boolean indicating whether or not a drink was removed
     */
    protected boolean decrementCount() {
        /*
         * SELECT * FROM drink_log
         *   WHERE log_date = 'getToday()' AND drink_count > 0;
         */
        String selectQuery = "SELECT * FROM " + LOG_TABLE + " WHERE " +
                LOG_DATE_COLUMN + " = ? AND " + LOG_COUNT_COLUMN + " > 0";
        String[] selectQueryArgs = {getToday()};
        /*
         * UPDATE drink_log
         *   SET drink_count = drink_count - 1
         *   WHERE log_date = 'getToday()';
         */
        String updateQuery = "UPDATE " + LOG_TABLE + " SET " + LOG_COUNT_COLUMN +
                " = " + LOG_COUNT_COLUMN + " - 1 WHERE " + LOG_DATE_COLUMN +
                " = '" + getToday() + "'";

        // Get database, begin transaction
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, selectQueryArgs);
        boolean drinkRemoved;
        // First, attempt a select
        try {
            if(cursor.getCount() > 0) {
                // If results are returned, update
                db.execSQL(updateQuery);
                Log.d(TAG, "decrementCount: Decremented.");
                drinkRemoved = true;
            } else {
                // Otherwise nothing happens
                Log.d(TAG, "decrementCount: No drinks found for today.");
                drinkRemoved = false;
            }
        } catch (Exception oops) {
            Log.d(TAG, "decrementCount: Unexpected error.");
            drinkRemoved = false;
        } finally {
            if (cursor != null && !cursor.isClosed()) { cursor.close(); }
        }
        return drinkRemoved;
    }

    /**
     * Queries database, returns total drinks for today's date.
     *
     * @return the integer count of total drinks for today's date
     */
    protected int getDailyCount() {
        /*
         * SELECT SUM(drink_count)
         *   FROM drink_log
         *   WHERE date = 'getToday()';
         */
        String[] projection = {"SUM(" + LOG_COUNT_COLUMN + ")", LOG_DATE_COLUMN};
        String selection = LOG_DATE_COLUMN + " = ?";
        String[] selectionArgs = {getToday()};
        String groupBy = null;
        String having = null;
        String orderBy = null;
        String limit = null;

        // Get database and cursor
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(LOG_TABLE, projection, selection, selectionArgs,
                groupBy, having, orderBy, limit);
        int result = -1;
        String dateString = "";
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
            if (cursor != null && !cursor.isClosed()) { cursor.close(); }
            Log.d(TAG, "getDailyCount: Returning " + result + ", " + dateString);
        }
        return result;
    }

    /**
     * Queries database, returns total drinks for a rolling 7 day window.
     *
     * @return the integer count of total drinks for the last 7 days
     */
    protected int getWeeklyCount() {
        /*
         * SELECT SUM(drink_count)
         *   FROM drink_log
         *   WHERE date BETWEEN 'oneWeekAgo()' AND 'getToday()';
         */
        String query = "SELECT SUM(" + LOG_COUNT_COLUMN + ") FROM "
                + LOG_TABLE + " WHERE " + LOG_DATE_COLUMN + " BETWEEN ? AND ?";
        String[] queryArgs = {getOneWeekAgo(), getToday()};

        // Get database and cursor
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, queryArgs);
        int result = -1;
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
            if (cursor != null && !cursor.isClosed()) { cursor.close(); }
            Log.d(TAG, "getWeeklyCount: Returning " + result);
        }
        return result;
    }

    /**
     * Returns a double value for the average number of drinks per day.
     * Calls getTotalDrinks() and getElapsedDays(), returns the divided result.
     *
     * @return a double value representing the averages drinks per day
     */
    protected double getDailyAvg() {
        int totalDrinks = getTotalDrinks();
        int totalDays = getElapsedDays();
        double result = 1.0 * totalDrinks/totalDays;
        Log.d(TAG, "getDailyAvg: Returning: " + result);
        return result;
    }

    /**
     * Returns a double value for the average number of drinks per week.
     * Calls getTotalDrinks() and getElapsedWeeks(), returns the divided
     * result.
     *
     * @return a double value representing the averages drinks per week
     */
    protected double getWeeklyAvg() {
        int totalDrinks = getTotalDrinks();
        double totalWeeks = getElapsedWeeks();
        double result = totalDrinks/totalWeeks;
        Log.d(TAG, "getWeeklyAvg: Returning " + result);
        return result;
    }

    /**
     * Gets todays date, formats appropriately for insertion into SQLite.
     *
     * @return today's date formatted as yyyy-MM-dd
     */
    private String getToday() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date calDate = Calendar.getInstance().getTime();
        String today = df.format(calDate);
        Log.d(TAG, "getToday: " + today);
        return today;
    }

    /**
     * Gets the date for 6 days before today, formats appropriately for
     * insertion into SQLite.
     *
     * @return date for 6 days ago formatted as yyyy-MM-dd
     */
    private String getOneWeekAgo() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date calDate = Calendar.getInstance().getTime();
        calDate = new Date(calDate.getTime() - 6 * 24 * 3600 * 1000l);
        String oneWeekAgo = df.format(calDate);
        Log.d(TAG, "getOneWeekAgo: " + oneWeekAgo);
        return oneWeekAgo;
    }

    /**
     * Queries database, returns total number of drinks logged.
     *
     * @return integer value for total number of drinks logged
     */
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

        // Get database and cursor
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(table, projection, selection, selectionArgs,
                groupBy, having, orderBy, limit);
        int result = -1;
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
            if (cursor != null && !cursor.isClosed()) { cursor.close(); }
            Log.d(TAG, "getTotalDrinks: Returning " + result);
        }
        return result;
    }

    /**
     * Queries database, returns total number of days elapsed since database
     * creation date, adds 1.
     *
     * @return integer value for total number of days since database creation
     */
    private int getElapsedDays() {
        /*
         * SELECT 1 + CAST(strftime('%J', 'getToday()') AS INT) -
         *   (SELECT CAST(strftime('%J', created_date) AS INT) FROM meta_data);
         */
        String table = META_TABLE;
        String[] projection = {"1 + CAST(strftime('%J', '" + getToday() +
                "') AS INT) - (SELECT CAST(strftime('%J', created_date) " +
                "AS INT) FROM " + META_TABLE + ")"};
        String selection = null;
        String[] selectionArgs = null;
        String groupBy = null;
        String having = null;
        String orderBy = null;
        String limit = null;

        // Get database and cursor
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(table, projection, selection,
                selectionArgs, groupBy, having, orderBy, limit);
        int result = -1;
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
            if (cursor != null && !cursor.isClosed()) { cursor.close(); }
            Log.d(TAG, "getElapsedDays: Returning " + result);
        }
        return result;
    }

    /**
     * Returns total number of seven-day periods since database creation date.
     * Calls getElapsedDays(), divides that value by 7, adds 1.
     *
     * @return integer value for total number of weeks since database creation
     */
    private double getElapsedWeeks() {
        double result = ((getElapsedDays()-1.00)/7.00);
        Log.d(TAG, "getElapsedWeeks: Returning " + result);
        return result;
    }
}
