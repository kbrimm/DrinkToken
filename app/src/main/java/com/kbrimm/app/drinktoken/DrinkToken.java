/*
 * DrinkToken
 *     Copyright (c) 2016 Katy Brimm
 *     This source file is licensed under the BSD 2-Clause License.
 *     Please see the file LICENSE in this distribution for license terms.
 * Contact: katy.brimm@gmail.com
 */

package com.kbrimm.app.drinktoken;

import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class DrinkToken extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Very exciting view setup things
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drink_logger);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get the database, initialize the counts
        final DrinkTokenDbHelper db = DrinkTokenDbHelper.getInstance(this);
        setCounts(db);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if present.
        getMenuInflater().inflate(R.menu.menu_drink_logger, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_clear:
                clearData();
                return true;
            case R.id.action_help: return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void addDrink(View view) {
        animateFAB();
        DrinkTokenDbHelper db = DrinkTokenDbHelper.getInstance(getApplicationContext());
        incrementDrinks(db);
        setCounts(db);
    }

    private void clearData() {
        AlertDialog alert = new AlertDialog.Builder(this)
                .setTitle("Clear Data")
                .setMessage("Do you really want to clear all data? This action cannot be undone.")
                .setIcon(android.R.drawable.ic_menu_delete)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DrinkTokenDbHelper db = DrinkTokenDbHelper.getInstance(getApplicationContext());
                        db.clearData();
                        setCounts(db);
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    private void animateFAB() {
        FloatingActionButton fab = (FloatingActionButton)
                findViewById(R.id.beer_icon);
        assert fab != null;
        Animation spin = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.spin);
        fab.startAnimation(spin);
    }

    private void incrementDrinks(DrinkTokenDbHelper db) {
        db.incrementCount();
    }

    private void setCounts(DrinkTokenDbHelper db) {
        // Get updated strings.
        String dailyCountString = "Today: " + db.getDailyCount();
        String weeklyCountString = "This week: " + db.getWeeklyCount();
        String dailyAvgString = "Daily average: " +
                String.format("%.2f", db.getDailyAvg());
        String weeklyAvgString = "Weekly average: " +
                String.format("%.2f", db.getWeeklyAvg());

        // Get and set text elements
        TextView dailyCount = (TextView) findViewById(R.id.count_strings);
        dailyCount.setText(dailyCountString + "\n" + weeklyCountString);
        TextView dailyAvg = (TextView) findViewById(R.id.avg_strings);
        dailyAvg.setText(dailyAvgString + "\n" + weeklyAvgString);
    }


}
