package com.bluebirdaward.joinin.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bluebirdaward.joinin.pojo.User;
import com.bluebirdaward.joinin.utils.UserValue;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import com.bluebirdaward.joinin.JoininApplication;
import com.bluebirdaward.joinin.event.ShowAdminProfileEvent;
import com.bluebirdaward.joinin.pojo.Status;
import com.bluebirdaward.joinin.utils.StatusValue;
import com.bluebirdaward.joinin.vc.activity.BaseActivity;
import com.bluebirdaward.joinin.vc.activity.MessagesListActivity;
import com.bluebirdaward.joinin.vc.activity.ProfileActivity;
import com.bluebirdaward.joinin.R;

/**
 * Created by duyvu on 4/13/16.
 */
public class StatusRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<User> users;
    private final int NOIMAGE = 0, IMAGE = 1;
    private Context context;
    private BaseActivity activity;

    public StatusRecyclerViewAdapter(Context context, ArrayList<User> users) {
        this.users = users;
        activity = (BaseActivity) context;
    }

    @Override
    public int getItemViewType(int position) {
        if (users.get(position).getCurrentStatus().getPicture().equals(""))
            return NOIMAGE;
        return IMAGE;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        context = parent.getContext();
        switch (viewType) {
            case IMAGE:
                View v1 = inflater.inflate(R.layout.viewholder_with_photo, parent, false);
                return new PhotoStatusViewHolder(v1);
            default:
                View v2 = inflater.inflate(R.layout.viewholder_no_photo, parent, false);
                return new NoPhotoStatusViewHolder(v2);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case IMAGE:
                PhotoStatusViewHolder vh1 = (PhotoStatusViewHolder) holder;
                configurePhotoStatusVH(vh1, position);
                break;
            default:
                NoPhotoStatusViewHolder vh2 = (NoPhotoStatusViewHolder) holder;
                configureNoPhotoStatusVH(vh2, position);
                break;
        }
    }

    private void configurePhotoStatusVH(final PhotoStatusViewHolder vh1, int position) {
        final User user = users.get(position);
        if (user != null) {
            // Ava
            Glide.with(context)
                    .load(user.getImgAva())
                    .asBitmap().into(new BitmapImageViewTarget(vh1.getImgAva()) {
                @Override
                protected void setResource(Bitmap resource) {
                    RoundedBitmapDrawable circularBitmapDrawable =
                            RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                    circularBitmapDrawable.setCircular(true);
                    vh1.getImgAva().setImageDrawable(circularBitmapDrawable);
                }
            });

            // Name
            vh1.getName().setText(user.getName());

            // Place, action and time
            Status status = user.getCurrentStatus();
            String actionString = "";
            actionString += StatusValue.getRelativeTimeAgo(status.getCreatedAt());
            if (!status.getPlace().equals("") || !status.getAction().equals("")) {
                actionString += " -";
            }
            if (!status.getAction().equals("")) {
                actionString += " <b>" + status.getAction() + " </b>";
                if (!status.getPlace().equals(""))
                    actionString += " at <b>" + status.getPlace() + " </b>";
            } else if (!status.getPlace().equals(""))
                actionString += " At <b>" + status.getPlace() + " </b>";
            vh1.place.setText(Html.fromHtml(actionString));

            // Distance
            int distance = user.getDistanceFrom();
            if (user.getId().equals(JoininApplication.me.getId()))
                vh1.getDistance().setText(R.string.Me);
            else if (distance == 0)
                vh1.getDistance().setText(R.string.here);
            else
                vh1.getDistance().setText(UserValue.getDistance(distance));

            // Stt img
            Glide.with(context)
                    .load(status.getPicture())
                    .centerCrop()
                    .into(vh1.getImgStatus());

            // Stt body
            if (!status.getBody().equals(""))
                vh1.status.setText(status.getBody());
            else
                vh1.status.setVisibility(View.GONE);

            // Chat now
            if (user.getId().equals(JoininApplication.me.getId())) {
                vh1.chatNow.setVisibility(View.GONE);
            } else {
                vh1.chatNow.setVisibility(View.VISIBLE);
                vh1.chatNow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(context, MessagesListActivity.class);
                        ArrayList<String> participantIds = new ArrayList<>(2);
                        participantIds.add(user.getId());
                        participantIds.add(JoininApplication.me.getId());
                        i.putStringArrayListExtra("participantIds", participantIds);
                        context.startActivity(i);
                        activity.overridePendingTransition(R.anim.enter, R.anim.exit);
                    }
                });
            }

            vh1.getContent().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (user.getId().equals(JoininApplication.me.getId()))
                        EventBus.getDefault().post(new ShowAdminProfileEvent());
                    else
                        viewProfile(user, vh1.getImgAva(), vh1.getName());
                }
            });
        }
    }

    private void configureNoPhotoStatusVH(final NoPhotoStatusViewHolder vh2, int position) {
        final User user = users.get(position);
        if (user != null) {
            // Ava
            Glide.with(context)
                    .load(user.getImgAva())
                    .asBitmap().into(new BitmapImageViewTarget(vh2.getImgAva()) {
                @Override
                protected void setResource(Bitmap resource) {
                    RoundedBitmapDrawable circularBitmapDrawable =
                            RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                    circularBitmapDrawable.setCircular(true);
                    vh2.getImgAva().setImageDrawable(circularBitmapDrawable);
                }
            });

            // Name
            vh2.getName().setText(user.getName());

            // Place, action and time
            Status status = user.getCurrentStatus();
            String actionString = "";
            actionString += StatusValue.getRelativeTimeAgo(status.getCreatedAt());
            if (!status.getPlace().equals("") || !status.getAction().equals("")) {
                actionString += " -";
            }
            if (!status.getAction().equals("")) {
                actionString += " <b>" + status.getAction() + "</b>";
                if (!status.getPlace().equals(""))
                    actionString += " at <b>" + status.getPlace() + "</b>";
            } else if (!status.getPlace().equals(""))
                actionString += " At <b>" + status.getPlace() + "</b>";
            vh2.place.setText(Html.fromHtml(actionString));

            // Distance
            int distance = user.getDistanceFrom();
            if (user.getId().equals(JoininApplication.me.getId()))
                vh2.getDistance().setText(R.string.Me);
            else if (distance == 0)
                vh2.getDistance().setText(R.string.here);
            else
                vh2.getDistance().setText(UserValue.getDistance(distance));

            // Stt body
            if (!status.getBody().equals(""))
                vh2.status.setText(status.getBody());
            else
                vh2.status.setVisibility(View.GONE);

            // Chat now
            if (user.getId().equals(JoininApplication.me.getId())) {
                vh2.chatNow.setVisibility(View.GONE);
            } else {
                vh2.chatNow.setVisibility(View.VISIBLE);
                vh2.chatNow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(context, MessagesListActivity.class);
                        ArrayList<String> participantIds = new ArrayList<>(2);
                        participantIds.add(user.getId());
                        participantIds.add(JoininApplication.me.getId());
                        i.putStringArrayListExtra("participantIds", participantIds);
                        context.startActivity(i);
                        activity.overridePendingTransition(R.anim.enter, R.anim.exit);
                    }
                });
            }

            vh2.getContent().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (user.getId().equals(JoininApplication.me.getId()))
                        EventBus.getDefault().post(new ShowAdminProfileEvent());
                    else
                        viewProfile(user, vh2.getImgAva(), vh2.getName());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if (users != null) return users.size();
        return 0;
    }

    public void viewProfile(User user, View ivProfile, View tvName) {
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.putExtra("user", user);
        Pair<View, String> p1 = Pair.create(ivProfile, "profile");
        Pair<View, String> p3 = Pair.create(tvName, "name");
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation((Activity) context, p1, p3);
        context.startActivity(intent, options.toBundle());
    }
}
