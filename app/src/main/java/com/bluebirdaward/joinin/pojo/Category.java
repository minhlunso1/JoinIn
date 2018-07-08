package com.bluebirdaward.joinin.pojo;

import android.os.Parcel;
import android.os.Parcelable;

import com.parse.ParseObject;

import java.util.ArrayList;

/**
 * Created by duyvu on 4/26/16.
 */
public class Category implements Parcelable {
    private String name;
    private ArrayList<Action> actions;

    public Category() {
        name = "";
        actions = new ArrayList<>();
    }

    protected Category(Parcel in) {
        name = in.readString();
        actions = in.createTypedArrayList(Action.CREATOR);
    }

    public static final Creator<Category> CREATOR = new Creator<Category>() {
        @Override
        public Category createFromParcel(Parcel in) {
            return new Category(in);
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeTypedList(actions);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Action> getActions() {
        return actions;
    }

    public void setActions(ArrayList<Action> actions) {
        this.actions = actions;
    }

    public static ArrayList<Category> fromParseObjects(ArrayList<ParseObject> categoriesObject) {
        ArrayList<Category> categories = new ArrayList<>();
        int categoryIndex = 0;
        if (categoriesObject != null && categoriesObject.size() > 0) {
            Category firstItem = new Category();
            firstItem.setName(categoriesObject.get(0).getString("category"));
            firstItem.getActions().add(new Action(categoriesObject.get(0).getString("action"), categoriesObject.get(0).getString("action_emo")));
            categories.add(firstItem);
            for (int i = 1; i < categoriesObject.size(); i++) {
                if (categoriesObject.get(i-1).getString("category").equals(categoriesObject.get(i).getString("category"))) {
                    categories.get(categoryIndex).setName(categoriesObject.get(i).getString("category"));
                    categories.get(categoryIndex).getActions().add(new Action(categoriesObject.get(i).getString("action"), categoriesObject.get(i).getString("action_emo")));
                } else {
                    Category category = new Category();
                    category.setName(categoriesObject.get(i).getString("category"));
                    category.getActions().add(new Action(categoriesObject.get(i).getString("action"), categoriesObject.get(i).getString("action_emo")));
                    categories.add(category);
                    categoryIndex++;
                }
            }
        }
        return categories;
    }
}
