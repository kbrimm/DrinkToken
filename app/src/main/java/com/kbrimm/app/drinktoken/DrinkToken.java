/*
 * DrinkToken
 *     Copyright (c) 2016 Katy Brimm
 *     This source file is licensed under the BSD 2-Clause License.
 *     Please see the file LICENSE in this distribution for license terms.
 * Contact: katy.brimm@gmail.com
 */

package com.kbrimm.app.drinktoken;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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

        // On beer_icon click, do this stuff.
        FloatingActionButton fab = (FloatingActionButton)
                findViewById(R.id.beer_icon);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addDrink(db);
                // Whee!
                animateFAB();
            }
        });
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
            case R.id.action_clear: return true;
            case R.id.action_help: return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /** Called when user clicks the Drink! button */
    public void addDrink(DrinkTokenDbHelper db) {
        // To do: Add drink
        incrementDrinks(db);
        // To do: Retrieve new values
        setCounts(db);
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
