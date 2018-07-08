package com.bluebirdaward.joinin.utils;

import android.text.format.DateUtils;

/**
 * Created by Minh on 4/20/2016.
 */
public class StatusValue {

    public static String getRelativeTimeAgo(long dateMillis) {
        if (dateMillis == 0)
            return "Just now";
        String relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();

        return relativeDate;
    }

}
