/*
 * DrinkToken
 *     Copyright (c) 2016 Katy Brimm
 *     This source file is licensed under the BSD 2-Clause License.
 *     Please see the file LICENSE in this distribution for license terms.
 * Contact: katy.brimm@gmail.com
 */

package com.kbrimm.app.drinktoken;

import android.provider.BaseColumns;
import java.util.Date;

public final class DrinkContract {
    public DrinkContract() { }

    public static abstract class DrinkEntry implements BaseColumns {
        public static final String TABLE_NAME = "drink_log";
        public static final String COLUMN_NAME_DATE = "log_date";
        public static final String COLUMN_NAME_COUNT = "drink_count";
    }

    public class DrinkLogItem {
        public Date logDate;
        public int drinkCount;
    }
}
