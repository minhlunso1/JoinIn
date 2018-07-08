package com.bluebirdaward.joinin.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import java.util.ArrayList;

import com.bluebirdaward.joinin.pojo.Status;
import com.bluebirdaward.joinin.utils.StatusValue;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.bluebirdaward.joinin.R;

/**
 * Created by duyvu on 4/22/16.
 */
public class RecentActivityAdapter extends RecyclerView.Adapter<RecentActivityAdapter.ViewHolder> {

    private ArrayList<Status> mStatuses;
    private Context context;

    public RecentActivityAdapter(ArrayList<Status> statuses) {
        this.mStatuses = statuses;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tvBody)
        TextView tvBody;
        @Bind(R.id.tvTime)
        TextView tvTime;
        @Bind(R.id.ivPhoto)0
        ImageView ivPhoto;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View contactView = inflater.inflate(R.layout.item_recent_activity, parent, false);

        return new ViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Status status = mStatuses.get(position);
        if (!status.getBody().equals("")) {
            holder.tvBody.setVisibility(View.VISIBLE);
            holder.tvBody.setText(status.getBody());
        } else {
            holder.tvBody.setVisibility(View.GONE);
        }

        if (status.getPicture().length() > 0) {
            holder.ivPhoto.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(status.getPicture())
                    .asBitmap().centerCrop().into(new BitmapImageViewTarget(holder.ivPhoto) {
                @Override
                protected void setResource(Bitmap resource) {
                    RoundedBitmapDrawable circularBitmapDrawable =
                            RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                    circularBitmapDrawable.setCornerRadius(8);
                    holder.ivPhoto.setImageDrawable(circularBitmapDrawable);
                }
            });
        } else {
            holder.ivPhoto.setVisibility(View.GONE);
        }

        // Time, action and place
        String s = "";
        s += StatusValue.getRelativeTimeAgo(status.getCreatedAt());
        if (!status.getAction().equals("") || !status.getPlace().equals(""))
            s += " -";
        if (!status.getAction().equals("")) {
            s += " <b>" + status.getAction() + "</b>";
            if (!status.getPlace().equals(""))
                s += " at <b>" + status.getPlace() + "</b>";
        } else if (!status.getPlace().equals(""))
            s += " At <b>" + status.getPlace() + "</b>";
        holder.tvTime.setText(Html.fromHtml(s));
    }

    @Override
    public int getItemCount() {
        return mStatuses == null ? 0 : mStatuses.size();
    }
}
