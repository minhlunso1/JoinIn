package com.bluebirdaward.joinin.net;

import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.bluebirdaward.joinin.JoininApplication;
import com.bluebirdaward.joinin.pojo.User;

/**
 * Created by duyvu on 4/15/16.
 */
public class StatusClient {

    private OnStatusesFetchedListener onStatusesFetchedListener;

    public interface OnStatusesFetchedListener {
        void onSuccess(ArrayList<ParseObject> statuses);
    }

    /* Use to fetch all statuses of a particular user */
    public static ArrayList<ParseObject> fetchStatusesByUser(User user, final OnStatusesFetchedListener listener) {
        final ArrayList<ParseObject> statuses = new ArrayList<>();
        ParseQuery<ParseObject> statusQuery = ParseQuery.getQuery("Status");
        statusQuery.whereEqualTo("owner_id", user.getId());
        statusQuery.orderByDescending("postDate");
        statusQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, com.parse.ParseException e) {
                if (e != null) {
                    Log.d("DEBUG", e.toString());
                } else {
                    statuses.addAll(objects);
                    listener.onSuccess(statuses);
                }
            }
        });
        return statuses;
    }

    public static void pushStatusToParse(final String body, final String action, final String place, byte[] data) {
        final ParseObject parseStatus = new ParseObject("Status");
        /* Get fileName based on user ID and time stamp */
        if (data!=null) {
            final String fileName = ParseUser.getCurrentUser().getObjectId()
                    + String.valueOf(System.currentTimeMillis()) + ".jpg";
            final ParseFile file = new ParseFile(fileName, data);
            file.saveInBackground();
            StatusClient.setImageForStatus(parseStatus, file);
        }
        StatusClient.setPostDateForStatus(parseStatus);
        StatusClient.setOwnerForStatus(parseStatus, JoininApplication.me);
        StatusClient.setBodyForStatus(parseStatus, body);
        StatusClient.setActionForStatus(parseStatus, action);
        StatusClient.setPlaceForStatus(parseStatus, place);
        parseStatus.saveInBackground();
        UserClient.setCurrentStatusOnParse(parseStatus);
    }

    private static void setActionForStatus(ParseObject parseStatus, String action) {
        if (action != null) {
            parseStatus.put("action", action);
        }
    }

    private static void setPostDateForStatus(ParseObject parseStatus) {
        parseStatus.put("postDate", new Date(System.currentTimeMillis()));
    }

    private static void setOwnerForStatus(ParseObject parseStatus, User me) {
        parseStatus.put("owner_id", me.getId());
    }

    public static void setBodyForStatus(ParseObject parseStatus, String body) {
        if (body != null) {
            parseStatus.put("body", body);
        }
    }

    public static void setPlaceForStatus(ParseObject parseStatus, String place) {
        if (place != null) {
            parseStatus.put("place", place);
        }
    }

    public static void setImageForStatus(ParseObject parseStatus, ParseFile file) {
        if (file != null) {
            parseStatus.put("image", file);
        }
    }
}
