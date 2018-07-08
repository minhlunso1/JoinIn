package com.bluebirdaward.joinin.pojo;

/**
 * Created by Minh on 4/10/2016.
 */

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.bluebirdaward.joinin.JoininApplication;
import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class User implements Parcelable, Comparable<User> {

    private String id;
    private String name;
    private String imgAvaUrl;
    private String imgCoverUrl;
    private String email;
    private boolean isMale;
    private int age;
    private boolean isOnline;
    private LatLng currentLocation;
    private String hobby;
    private String aboutMe;
    private boolean isStatusPosted;
    private Status currentStatus;
    private ArrayList<Status> statusList;
    private int distanceFrom;
    private ArrayList<String> favoriteIds;
    private ArrayList<User> favoriteUsers;

    protected User(Parcel in) {
        id = in.readString();
        name = in.readString();
        imgAvaUrl = in.readString();
        imgCoverUrl = in.readString();
        email = in.readString();
        isMale = in.readByte() != 0;
        age = in.readInt();
        isOnline = in.readByte() != 0;
        currentLocation = in.readParcelable(LatLng.class.getClassLoader());
        hobby = in.readString();
        aboutMe = in.readString();
        currentStatus = in.readParcelable(Status.class.getClassLoader());
        statusList = in.createTypedArrayList(Status.CREATOR);
        distanceFrom = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(imgAvaUrl);
        dest.writeString(imgCoverUrl);
        dest.writeString(email);
        dest.writeByte((byte) (isMale ? 1 : 0));
        dest.writeInt(age);
        dest.writeByte((byte) (isOnline ? 1 : 0));
        dest.writeParcelable(currentLocation, flags);
        dest.writeString(hobby);
        dest.writeString(aboutMe);
        dest.writeParcelable(currentStatus, flags);
        dest.writeTypedList(statusList);
        dest.writeInt(distanceFrom);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public void setCurrentStatus(Status currentStatus) {
        this.currentStatus = currentStatus;
    }

    public int getDistanceFrom() {
        return distanceFrom;
    }

    public void setDistanceFrom(int distanceFrom) {
        this.distanceFrom = distanceFrom;
    }

    public User() {
        id = "";
        name = "";
        imgAvaUrl = "";
        imgCoverUrl = "";
        email = "";
        isMale = true;
        age = 0;
        isOnline = true;
        currentLocation = new LatLng(0, 0);
        hobby = "";
        aboutMe = "";
        distanceFrom = 0;
        isStatusPosted = false;
        currentStatus = new Status();
        statusList = new ArrayList<>();
        favoriteUsers = new ArrayList<>();
        favoriteIds = new ArrayList<>();
    }

    /* For other users get data from Parse */
    public static User fromJSONObject(JSONObject jsonObject) {
        User user = new User();
        user.name = jsonObject.optString("name");
        user.imgAvaUrl = jsonObject.optString("img_avatar_url");
        user.imgCoverUrl = jsonObject.optString("img_cover_url");
        user.email = jsonObject.optString("email");
        user.isMale = jsonObject.optBoolean("is_male");
        user.age = jsonObject.optInt("age");
        return user;
    }

    public ArrayList<User> fromJSONArray(JSONArray jsonArray) {
        ArrayList<User> users = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {

            try {
                JSONObject userJson = jsonArray.getJSONObject(i);
                User user = User.fromJSONObject(userJson);
                if (user != null) {
                    users.add(user);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
        }
        return users;
    }

    /* For other users get data from Parse */
    public void fromParseUser(ParseUser parseUser) {
        if (parseUser != null) {
            this.setId(parseUser.getObjectId());
            this.setAge(parseUser.getInt("age"));
            this.setName(parseUser.getString("name"));
            this.setImgAva(parseUser.getString("img_avatar_url"));
            this.setImgCover(parseUser.getString("img_cover_url"));
            this.setMale(parseUser.getBoolean("is_male"));
            this.setOnline(parseUser.getBoolean("is_online"));
            this.setCurrentLocation(parseUser.getParseGeoPoint("current_location"));
            this.setHobby(parseUser.getString("hobbies"));
            this.setAboutMe(parseUser.getString("about"));
            this.setStatusPosted(parseUser.getBoolean("is_status_posted"));
            if (isStatusPosted())
                this.setCurrentStatus(parseUser.getParseObject("current_status"));

            LatLng currentLocation = JoininApplication.me.getCurrentLocation();
            LatLng pCurrentLocation = this.getCurrentLocation();
            float distanceResults[] = new float[1];
            Location.distanceBetween(currentLocation.latitude, currentLocation.longitude, pCurrentLocation.latitude, pCurrentLocation.longitude, distanceResults);
            this.setDistanceFrom((int) distanceResults[0]);
        }
    }

    /* For current user only */
    public static void returnCurrentUser(User currentUser, ParseUser parseUser) {
        if (parseUser != null) {
            currentUser.setId(parseUser.getObjectId());
            currentUser.setAge(parseUser.getInt("age"));
            currentUser.setName(parseUser.getString("name"));
            currentUser.setImgAva(parseUser.getString("img_avatar_url"));
            currentUser.setImgCover(parseUser.getString("img_cover_url"));
            currentUser.setMale(parseUser.getBoolean("is_male"));
            currentUser.setOnline(parseUser.getBoolean("is_online"));
            currentUser.setCurrentLocation(parseUser.getParseGeoPoint("current_location"));
            currentUser.setHobby(parseUser.getString("hobbies"));
            currentUser.setAboutMe(parseUser.getString("about"));
            currentUser.setStatusPosted(parseUser.getBoolean("is_status_posted"));
            if (currentUser.isStatusPosted())
                currentUser.setCurrentStatus(parseUser.getParseObject("current_status"));
//            ArrayList<ParseObject> parseStatuses = StatusClient.fetchStatusesByUser(parseUser);
//            currentUser.setStatusList(Status.fromParseStatuses(parseStatuses));
            List<String> ids = parseUser.getList("favorite_ids");
            if (ids != null)
                currentUser.favoriteIds.addAll(ids);
            currentUser.distanceFrom = 0;
        }
    }

    private void setUserFavorites(ArrayList<User> users) {
        if (users != null)
            this.favoriteUsers.addAll(users);
    }

    public static ArrayList<User> fromParseUsers(List<ParseUser> parseUsers) {
        ArrayList<User> users = new ArrayList<>();
        if (parseUsers != null) {
            for (int i = 0; i < parseUsers.size(); i++) {
                User user = new User();
                user.fromParseUser(parseUsers.get(i));
                users.add(user);
            }
        }
        return users;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImgAva() {
        return imgAvaUrl;
    }

    public void setImgAva(String imgAvaUrl) {
        this.imgAvaUrl = imgAvaUrl;
    }

    public String getImgCover() {
        return imgCoverUrl;
    }

    public void setImgCover(String imgCover) {
        this.imgCoverUrl = imgCover;
    }

    public boolean isMale() {
        return isMale;
    }

    public void setMale(boolean male) {
        isMale = male;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public LatLng getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(LatLng currentLocation) {
        this.currentLocation = currentLocation;
    }

    public void setCurrentLocation(ParseGeoPoint currentLocation) {
        if (currentLocation != null) {
            this.currentLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        }
    }

    public String getHobby() {
        return hobby;
    }

    public void setHobby(String hobby) {
        this.hobby = hobby;
    }

    public String getAboutMe() {
        return aboutMe;
    }

    public void setAboutMe(String aboutMe) {
        this.aboutMe = aboutMe;
    }

    public Status getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(ParseObject currentStatus) {
        this.currentStatus = Status.fromParseStatus(currentStatus);
    }

    public ArrayList<Status> getStatusList() {
        return statusList;
    }

    public void setStatusList(ArrayList<Status> statusList) {
        this.statusList.addAll(statusList);
    }

    public void setStatusPosted(boolean isStatusPosted) {
        this.isStatusPosted = isStatusPosted;
    }

    public boolean isStatusPosted() {
        return isStatusPosted;
    }


    public ArrayList<String> getFavoriteIds() {
        return favoriteIds;
    }

    public void setFavoriteIds(ArrayList<String> favoriteIds) {
        this.favoriteIds = favoriteIds;
    }

    public ArrayList<User> getFavoriteUsers() {
        return favoriteUsers;
    }

    public void setFavoriteUsers(ArrayList<User> favoriteUsers) {
        this.favoriteUsers = favoriteUsers;
    }

    @Override
    public int compareTo(@NonNull User another) {
        int anotherDistanceFrom = another.getDistanceFrom();
        return this.distanceFrom - anotherDistanceFrom;//descedants
    }
}
