package com.bluebirdaward.joinin.vc.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bluebirdaward.joinin.JoininApplication;
import com.bluebirdaward.joinin.R;
import com.bluebirdaward.joinin.adapter.RecentActivityAdapter;
import com.bluebirdaward.joinin.event.NotifyAdminPostSttEvent;
import com.bluebirdaward.joinin.event.NotifyAdminUpdatedEvent;
import com.bluebirdaward.joinin.net.StatusClient;
import com.bluebirdaward.joinin.net.UserClient;
import com.bluebirdaward.joinin.pojo.Status;
import com.bluebirdaward.joinin.pojo.User;
import com.bluebirdaward.joinin.utils.UserValue;
import com.bluebirdaward.joinin.vc.activity.MessagesListActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.parse.ParseObject;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 11-Apr-16.
 */
public class ProfileFragment extends Fragment {

    @Bind(R.id.tv_favorite)
    TextView tvFav;
    @Bind(R.id.tv_message)
    TextView tvMessage;
    @Bind(R.id.img_ava)
    ImageView imgAva;
    @Bind(R.id.htab_header)
    ImageView imgCover;
    @Bind(R.id.tv_name)
    TextView tvName;
    @Bind(R.id.tv_age)
    TextView tvAge;
    @Bind(R.id.tv_status)
    TextView tvStatus;
    @Bind(R.id.tv_intro)
    TextView tvIntro;
    @Bind(R.id.ic_gender)
    ImageView icGender;
    @Bind(R.id.tv_interests)
    TextView tvInterests;
    @Bind(R.id.tv_edit)
    TextView tvEdit;
    @Bind(R.id.rvRecentStatuses)
    RecyclerView rvStatuses;
    @Bind(R.id.toolBar)
    Toolbar toolbar;
    @Bind(R.id.htab_appbar)
    AppBarLayout appBarLayout;
    @Bind(R.id.htab_collapse_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;

    private Context context;
    private boolean isMe;
    private User user;
    private RecentActivityAdapter statusAdapter;

    public static ProfileFragment newInstance(boolean isMe, User user) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("isMe", isMe);
        bundle.putParcelable("user", user);
        ProfileFragment fragment = new ProfileFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Subscribe
    public void notifyAdminUpdated(NotifyAdminUpdatedEvent event) {
        if (isMe) {
            user = JoininApplication.me;
            attachDataToView();
            statusAdapter.notifyDataSetChanged();
        }
    }

    @Subscribe
    public void notifyAdminPostStt(NotifyAdminPostSttEvent event) {
        if (isMe) {
            user.getStatusList().add(0, JoininApplication.me.getCurrentStatus());
            attachDataToView();
            statusAdapter.notifyItemInserted(0);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        Bundle bundle = getArguments();
        user = bundle.getParcelable("user");
        isMe = bundle.getBoolean("isMe", false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, view);
        setHasOptionsMenu(true);
        setupRecyclerView();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (isMe)
            inflater.inflate(R.menu.menu_profile_me, menu);
        else {
            inflater.inflate(R.menu.menu_profile_them, menu);
            if (userPositionInList(user) == -1) {
                // User is not in favorite list
                toolbar.getMenu().getItem(0).setIcon(R.drawable.ic_favorite_unfilled_large);
            } else {
                // User is in favorite list
                toolbar.getMenu().getItem(0).setIcon(R.drawable.ic_favorite_filled_large);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (isMe) {
            switch (item.getItemId()) {
                case R.id.action_edit:
                    launchEditProfileFragment();
                    return true;
            }
        } else {
            switch (item.getItemId()) {
                case R.id.action_message:
                    launchMessageListActivity();
                    return true;
                case R.id.action_favorite:
                    addToFavorite();
                    return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        StatusClient.fetchStatusesByUser(user, new StatusClient.OnStatusesFetchedListener() {
            @Override
            public void onSuccess(ArrayList<ParseObject> fetchedStatuses) {
                user.getStatusList().clear();
                user.setStatusList(Status.fromParseStatuses(fetchedStatuses));
                statusAdapter.notifyDataSetChanged();
            }
        });
        setupToolbar();
        attachDataToView();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }

    private void setupToolbar() {
        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        appCompatActivity.setSupportActionBar(toolbar);
        if (appCompatActivity.getSupportActionBar() != null) {
            appCompatActivity.getSupportActionBar().setTitle("");
        }
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    if (user == null) collapsingToolbarLayout.setTitle("Profile");
                    else collapsingToolbarLayout.setTitle(user.getName());
                    collapsingToolbarLayout.setCollapsedTitleTextColor(getResources().getColor(android.R.color.white));
                    if (toolbar.getMenu().size() > 0) {
                        toolbar.getMenu().getItem(0).setVisible(true);
                        if (!isMe)
                            toolbar.getMenu().getItem(1).setVisible(true);
                    }
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbarLayout.setTitle("");
                    toolbar.setBackgroundColor(Color.TRANSPARENT);
                    if (toolbar.getMenu().size() > 0) {
                        toolbar.getMenu().getItem(0).setVisible(false);
                        if (!isMe)
                            toolbar.getMenu().getItem(1).setVisible(false);
                    }
                    isShow = false;
                }
            }
        });
    }

    private void attachDataToView() {
        if (isMe) {
            tvFav.setVisibility(View.GONE);
            tvMessage.setVisibility(View.GONE);
            tvEdit.setVisibility(View.VISIBLE);
        } else {
            tvMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getActivity(), MessagesListActivity.class);
                    ArrayList<String> participantIds = new ArrayList<>(2);
                    participantIds.add(user.getId());
                    participantIds.add(JoininApplication.me.getId());
                    i.putStringArrayListExtra("participantIds", participantIds);
                    startActivity(i);
                    getActivity().overridePendingTransition(R.anim.enter, R.anim.exit);
                }
            });
            tvFav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addToFavorite();
                }
            });
            if (userPositionInList(user) == -1) {
                // User is not in favorite list
                tvFav.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_unfilled, 0, 0, 0);
                tvFav.setText(context.getString(R.string.Favourite));
            } else {
                // User is in favorite list
                tvFav.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_filled, 0, 0, 0);
                tvFav.setText(context.getString(R.string.Unfavourite));
            }
        }
        set1(user.getName(), user.getAboutMe(), user.getHobby());

        Glide.with(context)
                .load(user.getImgAva())
                .asBitmap().into(new BitmapImageViewTarget(imgAva) {
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                circularBitmapDrawable.setCircular(true);
                imgAva.setImageDrawable(circularBitmapDrawable);
            }
        });

        Glide.with(context)
                .load(user.getImgCover())
                .asBitmap()
                .error(R.drawable.bluebird_logo)
                .into(imgCover);

        tvAge.setText(UserValue.getAge(getContext(), user.getAge()));
        tvStatus.setText(UserValue.getInfo(user.getCurrentStatus().getBody()));
    }

    private int userPositionInList(User user) {
        for (int i = 0; i < JoininApplication.me.getFavoriteIds().size(); i++) {
            if (user.getId().equals(JoininApplication.me.getFavoriteIds().get(i))) {
                return i;
            }
        }
        return -1;
    }

    private void setupRecyclerView() {
        statusAdapter = new RecentActivityAdapter(user.getStatusList());
        rvStatuses.setAdapter(statusAdapter);
        rvStatuses.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @OnClick(R.id.tv_favorite)
    public void addToFavorite() {
        tvFav.setClickable(false);
        final int i = userPositionInList(user);
        if (i == -1) {
            // If not in list, add favorite user
            UserClient.favoriteUser(user, new UserClient.OnUserFavoritedListener() {
                @Override
                public void onSuccess(Boolean isSucceeded) {
                    tvFav.setClickable(true);
                    if (isSucceeded) {
                        JoininApplication.me.getFavoriteIds().add(user.getId());
                        tvFav.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_filled, 0, 0, 0);
                        tvFav.setText(context.getString(R.string.Unfavourite));
                        if (toolbar.getMenu().size() > 0) {
                            toolbar.getMenu().getItem(0).setIcon(R.drawable.ic_favorite_filled_large);
                        }
                    } else {
                        Toast.makeText(context, "An error has occured! Please try again later", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            // If already favorited, remove favorite user
            UserClient.unfavoriteUser(user, new UserClient.OnUserFavoritedListener() {
                @Override
                public void onSuccess(Boolean isSucceeded) {
                    tvFav.setClickable(true);
                    if (isSucceeded) {
                        JoininApplication.me.getFavoriteIds().remove(i);
                        //TODO: Customize UI Favorite Btn
                        tvFav.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_unfilled, 0, 0, 0);
                        tvFav.setText(context.getString(R.string.Favorite));
                        if (toolbar.getMenu().size() > 0) {
                            toolbar.getMenu().getItem(0).setIcon(R.drawable.ic_favorite_unfilled_large);
                        }
                    } else {
                        Toast.makeText(context, "An error has occured! Please try again later", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @OnClick(R.id.tv_edit)
    public void editProfile() {
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                //TODO fix this
                .setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
                .add(android.R.id.content, EditProfileFragment.newInstance(this))
                .addToBackStack("profile")
                .commit();
    }

    public void set1(String name, String about, String hobby) {
        tvName.setText(name);
        tvIntro.setText(about);
        tvInterests.setText(hobby);
    }

    public void launchEditProfileFragment() {
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .add(android.R.id.content, EditProfileFragment.newInstance(this))
                .addToBackStack("profile")
                .commit();
    }

    private void launchMessageListActivity() {
        Intent i = new Intent(getActivity(), MessagesListActivity.class);
        ArrayList<String> participantIds = new ArrayList<>(2);
        participantIds.add(user.getId());
        participantIds.add(JoininApplication.me.getId());
        i.putStringArrayListExtra("participantIds", participantIds);
        startActivity(i);
    }
}
