package com.sam_chordas.android.stockhawk;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import com.sam_chordas.android.stockhawk.ui.MyStocksActivity;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Vidhya on 14-11-2016.
 */

public class Utility {
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    public static String getFormattedDate(long dateInMillis) {
        Locale localeUS = new Locale("en", "US");
        SimpleDateFormat queryDayFormat = new SimpleDateFormat(Utility.DATE_FORMAT, localeUS);
        return queryDayFormat.format(dateInMillis);
    }

    public static String get1WeekBackDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, -7);
//        Date newDate = cal.getTime();
        return getFormattedDate(cal.getTimeInMillis());
    }

    static public boolean isNetworkAvailable(Context c) {
        ConnectivityManager cm =
                (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }


    @SuppressWarnings("ResourceType")
    static public
    @MyStocksActivity.StockStatus
    int getStockStatus(Context c) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        return sp.getInt(c.getString(R.string.pref_stiock_status_key), MyStocksActivity.STOCK_STATUS_UNKNOWN);
    }
}
