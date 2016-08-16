/*
 * DrinkToken
 *     Copyright (c) 2016 Katy Brimm
 *     This source file is licensed under the BSD 2-Clause License.
 *     Please see the file LICENSE in this distribution for license terms.
 * Contact: info@drinktokenapp.com
 */

package com.kbrimm.app.drinktoken;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.Toast;

public class DrinkToken extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Very exciting view setup things
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drink_logger);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get the database, initialize the counts
        DrinkTokenDbHelper db = DrinkTokenDbHelper.getInstance(this);
        setCounts(db);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if present.
        getMenuInflater().inflate(R.menu.menu_drink_logger, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        DrinkTokenDbHelper db = DrinkTokenDbHelper.getInstance(getApplicationContext());
        setCounts(db);
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
            case R.id.action_report:
                sendReport();
                return true;
            case R.id.action_about: return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void addDrink(View view) {
        animateDrinkButton();
        DrinkTokenDbHelper db = DrinkTokenDbHelper.getInstance(getApplicationContext());
        db.incrementCount();
        setCounts(db);
    }

    public void undoDrink(View view) {

        DrinkTokenDbHelper db = DrinkTokenDbHelper.getInstance(getApplicationContext());
        if(db.decrementCount())
        {
            animateUndoButton();
            setCounts(db);
        } else {
            Context context = getApplicationContext();
            CharSequence cannot = "Cannot undo yesterday's mistakes.";
            int duration = Toast.LENGTH_SHORT;
            Toast cannotMessage = Toast.makeText(context, cannot, duration);
            cannotMessage.show();
        }
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

                        // Confirmation toast.
                        Context context = getApplicationContext();
                        CharSequence clean = "You are a clean slate.";
                        int duration = Toast.LENGTH_SHORT;
                        Toast cleanMessage = Toast.makeText(context, clean, duration);
                        cleanMessage.show();
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    private void sendReport() {
        // Set bug report strings
        String[] recipient = {"report@drinktokenapp.com"};
        String subject = "DrinkToken Bug Report";
        String body = "Please tell us a little bit about the bug you wish to report.\n" +
                "What happened:\n\nWhat I expected:";
        // Set intent values
        Intent message = new Intent(Intent.ACTION_SEND);
        message.setType("message/rfc822");
        message.putExtra(Intent.EXTRA_EMAIL, recipient);
        message.putExtra(Intent.EXTRA_SUBJECT, subject);
        message.putExtra(Intent.EXTRA_TEXT, body);
        // Execute intent
        try {
            startActivity(Intent.createChooser(message, "Choose Application to Send Report"));
        } catch (ActivityNotFoundException nope) {
            Toast.makeText(this, "No email client found.", Toast.LENGTH_SHORT).show();
        }
    }

    private void animateDrinkButton() {
        FloatingActionButton fab = (FloatingActionButton)
                findViewById(R.id.beer_icon);
        assert fab != null;
        Animation spin = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.spin);
        fab.startAnimation(spin);
    }

    private void animateUndoButton() {
        FloatingActionButton fab = (FloatingActionButton)
                findViewById(R.id.undo_icon);
        assert fab != null;
        Animation spin = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.spin);
        fab.startAnimation(spin);
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
