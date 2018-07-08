package com.bluebirdaward.joinin.utils;

import android.content.Context;

import com.bluebirdaward.joinin.R;

import java.util.Locale;

/**
 * Created by Administrator on 11-Apr-16.
 */
public class UserValue {

    public static String getAge(Context context, int age) {
        return age == 0 ? context.getString(R.string.hidden_age) : age + context.getString(R.string.year_old);
    }

    public static String getInfo(String str) {
        return str == null ? "" : str;
    }

    public static String getDistance(int distance) {
        float km = (float) distance / 1000;
        if (km > 2)
            return "2.0+ km";
        return String.format(Locale.getDefault(), "%.1f", km) + " km";
    }

}
