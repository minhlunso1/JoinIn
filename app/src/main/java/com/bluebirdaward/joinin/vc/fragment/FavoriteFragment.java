package com.bluebirdaward.joinin.vc.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bluebirdaward.joinin.JoininApplication;
import com.bluebirdaward.joinin.adapter.FavAdapter;
import com.bluebirdaward.joinin.event.NotifyAdminUpdatedEvent;
import com.bluebirdaward.joinin.net.UserClient;
import com.bluebirdaward.joinin.pojo.User;
import com.bluebirdaward.joinin.vc.activity.DashboardActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;
import com.bluebirdaward.joinin.R;

/**
 * Created by duyvu on 4/11/16.
 */
public class FavoriteFragment extends Fragment {

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.rv)
    RecyclerView rv;
    @Bind(R.id.swipeRefeshLayout)
    SwipeRefreshLayout swipeRefreshLayout;

    private FavAdapter adapter;
    private ArrayList<User> users = new ArrayList<>();
    private DashboardActivity activity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (DashboardActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);
        ButterKnife.bind(this, view);
		if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);

        setHasOptionsMenu(true);
        activity.setSupportActionBar(toolbar);
        toolbar.setTitle(activity.getString(R.string.Favourite));
        toolbar.inflateMenu(R.menu.menu_dashboard2);
        toolbar.setOnMenuItemClickListener(
                new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_logout:
                                activity.destroyAll();
                                return true;
                        }
                        return true;
                    }
                });

        setupRecyclerView();
        return view;
    }

    @Subscribe
    public void notifyAdminUpdated(NotifyAdminUpdatedEvent event) {
        updateRecycleView();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    swipeRefreshLayout.setRefreshing(true);
                    updateRecycleView();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary_1, R.color.colorPrimary_2);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateRecycleView();
        activity.closeProgressDialog();
    }

    private void setupRecyclerView() {
        rv.setHasFixedSize(true);
        final GridLayoutManager layout = new GridLayoutManager(getActivity(), 2);
        rv.setLayoutManager(layout);
        adapter = new FavAdapter(getActivity(), users);
        ScaleInAnimationAdapter scaleAdapter = new ScaleInAnimationAdapter(adapter);
        scaleAdapter.setFirstOnly(false);
        rv.setAdapter(scaleAdapter);
    }

    private void updateRecycleView() {
        UserClient.fetchUserFavorites(JoininApplication.me.getFavoriteIds(), new UserClient.OnFavoritesFetchedListener() {
            @Override
            public void onFavoritesFetched(Boolean isSuceeded) {
                if (isSuceeded) {
                    users.clear();
                }
                users.addAll(JoininApplication.me.getFavoriteUsers());
                adapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
}
