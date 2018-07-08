package com.bluebirdaward.joinin.net;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.bluebirdaward.joinin.JoininApplication;
import com.bluebirdaward.joinin.event.BroadcastMeEvent;
import com.bluebirdaward.joinin.event.FinishUpdateStatusActivityEvent;
import com.bluebirdaward.joinin.event.NotifyAdminPostSttEvent;
import com.bluebirdaward.joinin.event.NotifyAdminUpdatedEvent;
import com.bluebirdaward.joinin.event.UpdateAdapterEvent;
import com.bluebirdaward.joinin.pojo.Status;
import com.bluebirdaward.joinin.pojo.User;
import com.bluebirdaward.joinin.vc.activity.WalkthrActivity;
import com.google.android.gms.maps.model.LatLng;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Created by duyvu on 4/12/16.
 */
public class UserClient {

    public interface OnAllUsersFetchedListener {
        public void onAllUsersFetched(ArrayList<ParseUser> users);
    }

    public interface OnFavoritesFetchedListener {
        public void onFavoritesFetched(Boolean isSucceeded);
    }

    public interface OnUserFavoritedListener {
        public void onSuccess(Boolean isSucceeded);
    }

    /* For current user get data from Facebook (FIRST TIME LOGIN) */
    public static void updateUserFbInfo(JSONObject jsonObject) {
        if (jsonObject != null) {
            final String name = jsonObject.optString("name");
            UserClient.setName(name);

            final String imgAvaUrl = jsonObject.optJSONObject("picture").optJSONObject("data").optString("url");
            UserClient.setImgAvaUrl(imgAvaUrl);

            try {
                final String imgCoverUrl = jsonObject.optJSONObject("cover").optString("source");
                UserClient.setImgCoverUrl(imgCoverUrl);
            } catch (NullPointerException e) {
                UserClient.setImgCoverUrl("");
            }

            final String email = jsonObject.optString("email");
            UserClient.setEmail(email);

            final String genderString = jsonObject.optString("gender");
            if (genderString.equals("male")) UserClient.setGender(true);
            else UserClient.setGender(false);

            final String birthDateString = jsonObject.optString("birthday");
            final int age = returnAge(birthDateString);
            UserClient.setAge(age);

            UserClient.setIsOnline(true);
        }
        UserClient.saveAndLaunchDashboard();
    }

    private static void saveAndLaunchDashboard() {
        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
            @Override
            public void done(com.parse.ParseException e) {
                if (e != null) {
                    Log.d("DEBUG", "Save user error: " + e.toString());
                    ParseUser.getCurrentUser().saveEventually();
                } else {
                    Log.d("DEBUG", "Current user saved");
                    User.returnCurrentUser(JoininApplication.me, ParseUser.getCurrentUser());
                    EventBus.getDefault().post(new BroadcastMeEvent());
                }
            }
        });
    }

    public static int returnAge(String birthdayString) {
        SimpleDateFormat birthDay = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        SimpleDateFormat birthYear = new SimpleDateFormat("yyyy", Locale.US);
        int age = 0;
        try {
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            age = currentYear - Integer.parseInt(birthYear.format(birthDay.parse(birthdayString)));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return age;
    }

    /* Update user online and current location */
    public static void refreshCurrentUserData() {
        if (ParseUser.getCurrentUser() != null) {
            UserClient.setIsOnline(true);
            UserClient.setCurrentLocation(JoininApplication.me.getCurrentLocation());

            ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                @Override
                public void done(com.parse.ParseException e) {
                    if (e != null) {
                        Log.d("DEBUG", "Save user error: " + e.toString());
                    } else {
                        if (JoininApplication.me.getId().equals(""))
                            fetchCurrentUser();
                    }
                }
            });

        }
    }

    /* Update user current location */
    public static void updateUserLocation() {
        if (ParseUser.getCurrentUser() != null) {
            UserClient.setCurrentLocation(JoininApplication.me.getCurrentLocation());
            ParseUser.getCurrentUser().saveInBackground();
        }
    }

    private static void fetchCurrentUser() {
        ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
        userQuery.whereEqualTo("objectId", ParseUser.getCurrentUser().getObjectId());
        userQuery.include("current_status");
        userQuery.include("favorite_ids");
        userQuery.getFirstInBackground(new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser object, com.parse.ParseException e) {
                User.returnCurrentUser(JoininApplication.me, object);
                EventBus.getDefault().post(new NotifyAdminUpdatedEvent());
            }
        });
    }

    public static void logOut(final Context context) {
        if (ParseUser.getCurrentUser() != null) {
            UserClient.setIsOnline(false);
            UserClient.setCurrentLocation(new LatLng(0, 0));
            UserClient.setIsStatusPosted(false);

            JoininApplication.me = new User();
            JoininApplication.getLayerClient().deauthenticate();
            JoininApplication.isLogout = true;
            Intent i = new Intent(context, WalkthrActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(i);
            ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                @Override
                public void done(com.parse.ParseException e) {
                    ParseUser.logOut();
//                    EventBus.getDefault().post(new FinishDashboardAcvitityEvent());
                }
            });
        }
    }

    public static void setCurrentStatusOnParse(final ParseObject parseStatus) {
        if (parseStatus != null) {
            ParseUser.getCurrentUser().put("current_status", parseStatus);
            ParseUser.getCurrentUser().put("is_status_posted", true);
            ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                @Override
                public void done(com.parse.ParseException e) {
                    if (e != null) {
                        Log.d("DEBUG", e.toString());
                    } else {
                        JoininApplication.me.setStatusPosted(true);
                        Status status = Status.fromParseStatus(parseStatus);
                        JoininApplication.me.setCurrentStatus(status);
                        JoininApplication.me.getStatusList().add(status);
                        EventBus.getDefault().post(new NotifyAdminPostSttEvent());
                    }
                    EventBus.getDefault().post(new FinishUpdateStatusActivityEvent());

                }
            });
        } else {
            Log.d("DEBUG", "Status = null");
        }
    }

    /* Use to fetch all user nearby */
    public static void fetchNearbyUsers(final ArrayList<User> users) {
        // Fetch users nearby only if current user exists
        if (!JoininApplication.me.getId().equals("")) {
            ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
            userQuery.whereEqualTo("is_status_posted", true);
            userQuery.whereEqualTo("is_online", true);
            userQuery.include("current_status");
            userQuery.whereNotEqualTo("objectId", JoininApplication.me.getId());
            Double latitude = JoininApplication.me.getCurrentLocation().latitude;
            Double longitude = JoininApplication.me.getCurrentLocation().longitude;

            //Filter Query
            if (JoininApplication.filter.maxDistance < 2000)
                userQuery.whereWithinKilometers("current_location", new ParseGeoPoint(latitude, longitude), ((double) JoininApplication.filter.maxDistance)/1000);
            userQuery.whereGreaterThan("age", JoininApplication.filter.minAge - 1);
            userQuery.whereLessThan("age", JoininApplication.filter.maxAge + 1);
            if (JoininApplication.filter.gender == 0) {
                userQuery.whereEqualTo("is_male", true);
            } else if (JoininApplication.filter.gender == 1){
                userQuery.whereEqualTo("is_male", false);
            }

            userQuery.findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(List<ParseUser> objects, com.parse.ParseException e) {
                    if (e != null) {
                        Log.d("DEBUG", e.toString());
                        EventBus.getDefault().post(new UpdateAdapterEvent());
                    } else {
                        users.clear();
                        if (JoininApplication.me.isStatusPosted()) {
                            users.add(JoininApplication.me);
                            Log.d("DEBUG", "me added");
                        }
                        users.addAll(User.fromParseUsers(objects));
                        Collections.sort(users);
                        EventBus.getDefault().post(new UpdateAdapterEvent());
                    }
                }
            });
        } else {
            EventBus.getDefault().post(new UpdateAdapterEvent());
        }
    }

    public static void fetchAllUsers(final OnAllUsersFetchedListener listener) {
        final ArrayList<ParseUser> users = new ArrayList<>();
        ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
        userQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, com.parse.ParseException e) {
                if (e != null) {
                    Log.d("DEBUG", e.toString());
                    listener.onAllUsersFetched(users);
                } else {
                    users.addAll(objects);
                    listener.onAllUsersFetched(users);
                }
            }
        });
    }

    public static void fetchUserFavorites(List<String> userIds, final OnFavoritesFetchedListener listener) {
        final ArrayList<User> users = new ArrayList<>();
        ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
        userQuery.whereContainedIn("objectId", userIds);
        userQuery.orderByDescending("is_online");
        userQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, com.parse.ParseException e) {
                if (e == null) {
                    users.addAll(User.fromParseUsers(objects));
                    JoininApplication.me.getFavoriteUsers().clear();
                    JoininApplication.me.getFavoriteUsers().addAll(users);
                    listener.onFavoritesFetched(true);
                } else {
                    listener.onFavoritesFetched(false);
                }
            }
        });
    }

    public static void favoriteUser(User user, final OnUserFavoritedListener listener) {
        ParseUser.getCurrentUser().addUnique("favorite_ids", user.getId());
        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
            @Override
            public void done(com.parse.ParseException e) {
                if (e == null) {
                    listener.onSuccess(true);
                } else {
                    listener.onSuccess(false);
                }
            }
        });
    }

    public static void unfavoriteUser(User user, final OnUserFavoritedListener listener) {
        ArrayList<String> tempIds = new ArrayList<>();
        tempIds.add(user.getId());
        ParseUser.getCurrentUser().removeAll("favorite_ids", tempIds);
        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
            @Override
            public void done(com.parse.ParseException e) {
                if (e == null) {
                    listener.onSuccess(true);
                } else {
                    listener.onSuccess(false);
                    Log.d("DEBUG", e.toString());
                }
            }
        });
    }


    /* Setter */
    public static void setName(String name) {
        if (name != null)
            ParseUser.getCurrentUser().put("name", name);
    }

    public static void setImgAvaUrl(String url) {
        if (url != null)
            ParseUser.getCurrentUser().put("img_avatar_url", url);
    }

    public static void setImgCoverUrl(String url) {
        if (url != null)
            ParseUser.getCurrentUser().put("img_cover_url", url);
    }

    public static void setEmail(String email) {
        if (email != null)
            ParseUser.getCurrentUser().setEmail(email);
    }

    public static void setGender(boolean isMale) {
        ParseUser.getCurrentUser().put("is_male", isMale);
    }

    public static void setAge(int age) {
        ParseUser.getCurrentUser().put("age", age);
    }

    public static void setIsOnline(boolean isOnline) {
        ParseUser.getCurrentUser().put("is_online", isOnline);
    }

    public static void setIsStatusPosted(boolean isPosted) {
        ParseUser.getCurrentUser().put("is_status_posted", isPosted);
    }

    public static void setCurrentLocation(LatLng currentLocation) {
        if (currentLocation != null) {
            ParseGeoPoint point = new ParseGeoPoint(currentLocation.latitude, currentLocation.longitude);
            ParseUser.getCurrentUser().put("current_location", point);
        }
    }

    public static void setHobbies(String hobbies) {
        if (hobbies != null)
            ParseUser.getCurrentUser().put("hobbies", hobbies);
    }

    public static void setAbout(String about) {
        if (about != null)
            ParseUser.getCurrentUser().put("about", about);
    }

    public static void setCurrentStatus(Status status) {
        ParseUser.getCurrentUser().put("status", status);
    }

    /* Getter */
    public static String getId() {
        return ParseUser.getCurrentUser().getObjectId();
    }

    public static ParseObject getCurrentStatus() {
        return ParseUser.getCurrentUser().getParseObject("current_status");
    }

    public static String getName() {
        return ParseUser.getCurrentUser().getString("name");
    }

    public static String getAvaImg() {
        return ParseUser.getCurrentUser().getString("img_avatar_url");
    }

    public static String getCoverImg() {
        return ParseUser.getCurrentUser().getString("img_cover_url");
    }

    public static String getEmail() {
        return ParseUser.getCurrentUser().getEmail();
    }

    public static boolean getGender() {
        return ParseUser.getCurrentUser().getBoolean("is_male");
    }

    public static int getAge() {
        return ParseUser.getCurrentUser().getInt("age");
    }

    public static boolean getIsOnline() {
        return ParseUser.getCurrentUser().getBoolean("is_online");
    }

    public static boolean getIsIsStatusPosted() {
        return ParseUser.getCurrentUser().getBoolean("is_online");
    }

    public static LatLng getCurrentLocation() {
        ParseGeoPoint point = ParseUser.getCurrentUser().getParseGeoPoint("current_location");
        if (point != null)
            return new LatLng(point.getLatitude(), point.getLongitude());
        return new LatLng(0, 0);
    }

    public static String getHobbies() {
        return ParseUser.getCurrentUser().getString("hobbies");
    }

    public static String getAbout() {
        return ParseUser.getCurrentUser().getString("about");
    }

}
