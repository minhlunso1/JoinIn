package com.bluebirdaward.joinin.pojo;

import android.os.Parcel;
import android.os.Parcelable;

import com.parse.ParseFile;
import com.parse.ParseObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Minh on 4/10/2016.
 */

public class Status implements Parcelable {

    private String id;
    private long createdAt;
    private long updatedAt;
    private String body;
    private String place;
    private String picture;
    private String action;

    public Status() {
        body = "";
        place = "";
        picture = "";
        action ="";
    }

    public Status(String body, String place, String picture) {
        this.body = body;
        this.place = place;
        this.picture = picture;
    }

    protected Status(Parcel in) {
        id = in.readString();
        createdAt = in.readLong();
        updatedAt = in.readLong();
        body = in.readString();
        place = in.readString();
        picture = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeLong(createdAt);
        dest.writeLong(updatedAt);
        dest.writeString(body);
        dest.writeString(place);
        dest.writeString(picture);
    }

    public static Status fromParseStatus(ParseObject parseStatus) {
        Status status = new Status();
        if (parseStatus != null) {
            if (parseStatus.has("body"))
                status.setBody(parseStatus.getString("body"));
            if (parseStatus.has("place"))
                status.setPlace(parseStatus.getString("place"));
            if (parseStatus.has("action"))
                status.setAction(parseStatus.getString("action"));
            status.setId(parseStatus.getObjectId());
            if (parseStatus.has("postDate"))
                status.setCreatedAt(parseStatus.getDate("postDate").getTime());
            ParseFile file = null;
            if (parseStatus.has("image"))
                file = parseStatus.getParseFile("image");
            if (file != null)
                status.setPicture(file.getUrl());
        }
        return status;
    }

    public static ArrayList<Status> fromParseStatuses(ArrayList<ParseObject> parseStatus) {
        ArrayList<Status> statuses = new ArrayList<>();
        if (parseStatus != null) {
            for (int i = 0; i < parseStatus.size(); i++) {
                Status status = Status.fromParseStatus(parseStatus.get(i));
                if (status != null) {
                    statuses.add(status);
                }
            }
        }
        return statuses;
    }

    public static final Creator<Status> CREATOR = new Creator<Status>() {
        @Override
        public Status createFromParcel(Parcel in) {
            return new Status(in);
        }

        @Override
        public Status[] newArray(int size) {
            return new Status[size];
        }
    };

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getId() {
        return id;
    }

    public static ArrayList<Status> fromJSONArray(JSONArray statusesArray) {
        ArrayList<Status> statuses = new ArrayList<>();
        if (statusesArray != null) {
            for (int i = 0; i < statusesArray.length(); i++) {

                try {
                    JSONObject userJson = statusesArray.getJSONObject(i);
                    Status status = Status.fromJSONObject(userJson);
                    if (status != null) {
                        statuses.add(status);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }
        return statuses;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    private static Status fromJSONObject(JSONObject userJson) {
        Status status = new Status();
        if (userJson != null) {
            status.body = userJson.optString("body");
            status.place = userJson.optString("place");
            status.picture = userJson.optString("img_url");
        }
        return status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
