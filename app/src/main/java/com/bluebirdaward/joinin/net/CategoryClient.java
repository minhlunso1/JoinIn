package com.bluebirdaward.joinin.net;

import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class CategoryClient {

    public interface OnCategoriesFetchedListener {
        void onSuccess(ArrayList<ParseObject> categories);
    }

    public static ArrayList<ParseObject> fetchAllCategories(final OnCategoriesFetchedListener listener) {
        final ArrayList<ParseObject> categories = new ArrayList<>();
        ParseQuery<ParseObject> categoryQuery = ParseQuery.getQuery("Action");
        categoryQuery.orderByDescending("category");
        categoryQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, com.parse.ParseException e) {
                if (e != null) {
                    Log.d("DEBUG", e.toString());
                } else {
                    categories.addAll(objects);
                    listener.onSuccess(categories);
                }
            }
        });
        return categories;
    }
}