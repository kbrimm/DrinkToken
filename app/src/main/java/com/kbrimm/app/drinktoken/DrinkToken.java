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
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class DrinkToken extends AppCompatActivity {
    private static final String APP_VERSION = "1.0";
    // private static final String TAG = "DrinkToken";

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
            case R.id.action_about:
                showLicense();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void addDrink(View view) {
        DrinkTokenDbHelper db = DrinkTokenDbHelper.getInstance(getApplicationContext());
        db.incrementCount();
        setCounts(db);
        // Whee!
        animateDrinkButton();
    }

    public void undoDrink(View view) {
        // Grab the Db
        DrinkTokenDbHelper db = DrinkTokenDbHelper.getInstance(getApplicationContext());
        // Try the decrement
        if(db.decrementCount())
        {
            // If it works, icon spins and counts reset
            animateUndoButton();
            setCounts(db);
        } else {
            // Otherwise, sassy toast
            Context context = getApplicationContext();
            CharSequence cannot = "Cannot undo yesterday's mistakes.";
            int duration = Toast.LENGTH_SHORT;
            Toast.makeText(context, cannot, duration).show();
        }
    }

    private void clearData() {
        // Alert the user to the destruction they're about to enact
        AlertDialog alert = new AlertDialog.Builder(this)
                .setTitle("Clear Data")
                .setMessage("Do you really want to clear all data? " +
                        "This action cannot be undone.")
                .setIcon(android.R.drawable.ic_menu_delete)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Get the Db
                        DrinkTokenDbHelper db =
                                DrinkTokenDbHelper.getInstance(getApplicationContext());
                        // Clear the data
                        db.clearData();
                        // Reset the counts
                        setCounts(db);
                        // Confirmation toast.
                        Context context = getApplicationContext();
                        CharSequence clean = "You are a clean slate.";
                        int duration = Toast.LENGTH_SHORT;
                        Toast.makeText(context, clean, duration).show();
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    private void sendReport() {
        // Set bug report strings
        String[] recipient = {"info@drinktokenapp.com"};
        String subject = "DrinkToken Bug Report";
        String body = "Please tell us a little bit about the bug you wish to "+
                "report.\n\nWhat happened:\n\nWhat I expected:";
        // Set intent values
        Intent message = new Intent(Intent.ACTION_SEND);
        message.setType("message/rfc822");
        message.putExtra(Intent.EXTRA_EMAIL, recipient);
        message.putExtra(Intent.EXTRA_SUBJECT, subject);
        message.putExtra(Intent.EXTRA_TEXT, body);
        // Execute intent
        try {
            startActivity(Intent.createChooser(message,
                    "Choose email application..."));
        } catch (ActivityNotFoundException nope) {
            Context context = getApplicationContext();
            CharSequence noneFound = "No email client found.";
            int duration = Toast.LENGTH_SHORT;
            Toast.makeText(context, noneFound, duration).show();
        }
    }

    private void showLicense() {
        // Set license strings
        String version = "Drink Token (v " + APP_VERSION + ")";
        String licenseText = "Copyright (c) 2016 Katy Brimm.\n\n" +
                "This application is provided 'as-is' and without any " +
                "express or implied warranties, including, without " +
                "limitation, the implied warranties of merchantability " +
                "and fitness for a particular purpose.\n\nThe source code " +
                "for DrinkToken is available as an open source project, " +
                "and is licensed under the BSD 2-clause license. The source " +
                "is available in its entirety on GitHub:\n\n" +
                "http://github.com/kbrimm/DrinkToken";
        // Build text view
        TextView license = new TextView(this);
        license.setText(licenseText);
        license.setPadding(75, 25, 75, 15);
        license.setGravity(Gravity.FILL_HORIZONTAL);
        // Create dialogue
        AlertDialog alert = new AlertDialog.Builder(this)
                .setTitle(version)
                .setView(license)
                .setPositiveButton(android.R.string.ok, null).show();
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
