/*
 * DrinkToken
 *     Copyright (c) 2016 Katy Brimm
 *     This source file is licensed under the BSD 2-Clause License.
 *     Please see the file LICENSE in this distribution for license terms.
 * Contact: katy.brimm@gmail.com
 */

package com.kbrimm.app.drinktoken;

import android.provider.BaseColumns;

public final class MetaContract {
    public MetaContract() { }

    public static abstract class MetaEntry implements BaseColumns {
        public static final String TABLE_NAME = "meta_data";
        public static final String COLUMN_NAME_DATE = "created_date";
    }
}
