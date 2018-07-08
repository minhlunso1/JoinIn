package com.bluebirdaward.joinin.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bluebirdaward.joinin.R;

/**
 * Created by duyvu on 4/13/16.
 */
public class PhotoStatusViewHolder extends RecyclerView.ViewHolder {
    ImageView imgAva, imgStatus;
    TextView name, distance, place, status, chatNow;
    RelativeLayout content;

    public PhotoStatusViewHolder(View v) {
        super(v);
        imgAva = (ImageView) v.findViewById(R.id.img_ava);
        imgStatus = (ImageView) v.findViewById(R.id.img_status);
        name = (TextView) v.findViewById(R.id.tv_name);
        distance = (TextView) v.findViewById(R.id.tv_distance);
        place = (TextView) v.findViewById(R.id.tv_place);
        status = (TextView) v.findViewById(R.id.tv_status);
        chatNow = (TextView) v.findViewById(R.id.tv_elapsedTime);
        content = (RelativeLayout) v.findViewById(R.id.content);
    }

    public ImageView getImgAva() {
        return imgAva;
    }

    public void setImgAva(ImageView imgAva) {
        this.imgAva = imgAva;
    }

    public ImageView getImgStatus() {
        return imgStatus;
    }

    public void setImgStatus(ImageView imgStatus) {
        this.imgStatus = imgStatus;
    }

    public TextView getName() {
        return name;
    }

    public void setName(TextView name) {
        this.name = name;
    }

    public TextView getDistance() {
        return distance;
    }

    public TextView getPlace() {
        return place;
    }

    public void setPlace(TextView place) {
        this.place = place;
    }

    public TextView getStatus() {
        return status;
    }

    public void setStatus(TextView status) {
        this.status = status;
    }

    public void setDistance(TextView distance) {
        this.distance = distance;
    }

    public RelativeLayout getContent() {
        return content;
    }

    public void setContent(RelativeLayout content) {
        this.content = content;
    }
}
