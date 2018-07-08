package com.bluebirdaward.joinin.pojo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by duyvu on 4/26/16.
 */
public class Action implements Parcelable {

    private String name;
    private String emoticon;

    public Action () {
        name = "";
        emoticon = "";
    }

    public Action (String name, String emoticon) {
        this.name = name;
        this.emoticon = emoticon;
    }

    protected Action(Parcel in) {
        name = in.readString();
        emoticon = in.readString();
    }

    public static final Creator<Action> CREATOR = new Creator<Action>() {
        @Override
        public Action createFromParcel(Parcel in) {
            return new Action(in);
        }

        @Override
        public Action[] newArray(int size) {
            return new Action[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(emoticon);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmoticon() {
        return emoticon;
    }

    public void setEmoticon(String emoticon) {
        this.emoticon = emoticon;
    }

}
