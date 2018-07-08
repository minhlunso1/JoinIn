package com.bluebirdaward.joinin.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import com.bluebirdaward.joinin.JoininApplication;
import com.bluebirdaward.joinin.pojo.User;
import com.bluebirdaward.joinin.vc.activity.MessagesListActivity;
import com.bluebirdaward.joinin.vc.activity.ProfileActivity;
import jp.wasabeef.glide.transformations.BlurTransformation;
import com.bluebirdaward.joinin.R;

public class FavAdapter extends RecyclerView.Adapter<FavAdapter.VH> {
    private Activity mContext;
    private List<User> list;

    public FavAdapter(Activity context, List<User> list) {
        mContext = context;
        this.list = list;
    }

    // Inflate the view based on the viewType provided.
    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fav, parent, false);
        return new VH(itemView, mContext);
    }

    // Display data at the specified position
    @Override
    public void onBindViewHolder(final VH holder, int position) {
        final User user = list.get(position);
        holder.rootView.setTag(user);
        holder.tvName.setText(user.getName());
        if (!user.isOnline()) {
            holder.tvName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_offline, 0);
            Glide.with(mContext).load(user.getImgAva())
                    .bitmapTransform(new BlurTransformation(mContext))
                    .into(holder.ivProfile);
        } else {
            holder.tvName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_online, 0);
            Glide.with(mContext).load(user.getImgAva()).into(holder.ivProfile);
        }

        holder.fab.setBackgroundTintList(ColorStateList.valueOf(mContext.getResources().getColor(R.color.colorPrimary_origin1)));
        holder.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mContext, MessagesListActivity.class);
                ArrayList<String> participantIds = new ArrayList<>(2);
                participantIds.add(user.getId());
                participantIds.add(JoininApplication.me.getId());
                i.putStringArrayListExtra("participantIds", participantIds);
                mContext.startActivity(i);
                mContext.overridePendingTransition(R.anim.enter, R.anim.exit);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // Provide a reference to the views for each contact item
    public final class VH extends RecyclerView.ViewHolder {
        final View rootView;
        final ImageView ivProfile;
        final TextView tvName;
        final FloatingActionButton fab;
        Context context;

        public VH(View itemView, final Context context) {
            super(itemView);
            this.context = context;
            rootView = itemView;
            ivProfile = (ImageView) itemView.findViewById(R.id.ivProfile);
            tvName = (TextView) itemView.findViewById(R.id.tvName);
            fab = (FloatingActionButton) itemView.findViewById(R.id.fab);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final User user = (User) v.getTag();
                    if (user != null) {
                        Intent intent = new Intent(context, ProfileActivity.class);
                        intent.putExtra("user", user);
                        Pair<View, String> p1 = Pair.create((View) ivProfile, "profile");
                        Pair<View, String> p2 = Pair.create((View) fab, "message");
                        Pair<View, String> p3 = Pair.create((View) tvName, "name");
                        ActivityOptionsCompat options = ActivityOptionsCompat.
                                makeSceneTransitionAnimation((Activity) context, p1, p2, p3);
                        context.startActivity(intent, options.toBundle());
                    }
                }
            });
        }

    }

}
