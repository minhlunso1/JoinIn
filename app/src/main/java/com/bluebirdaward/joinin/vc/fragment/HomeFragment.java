package com.bluebirdaward.joinin.vc.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
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
import android.widget.RelativeLayout;

import com.bluebirdaward.joinin.adapter.StatusRecyclerViewAdapter;
import com.bluebirdaward.joinin.event.UpdateAdapterEvent;
import com.bluebirdaward.joinin.pojo.User;
import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import com.bluebirdaward.joinin.net.UserClient;
import butterknife.Bind;
import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;
import com.bluebirdaward.joinin.JoininApplication;
import com.bluebirdaward.joinin.R;

import com.bluebirdaward.joinin.event.NotifyAdminPostSttEvent;
import com.bluebirdaward.joinin.event.NotifyAdminUpdatedEvent;
import com.bluebirdaward.joinin.vc.activity.DashboardActivity;

/**
 * Created by duyvu on 4/10/16.
 */
public class HomeFragment extends Fragment {

    @Bind(R.id.rvStatus)
    RecyclerView recyclerView;
    @Bind(R.id.swipeRefeshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.no_one_content)
    RelativeLayout rlNoOne;
    @Bind(R.id.iv_no_one)
    ImageView ivNoOne;

    public ArrayList<User> users = new ArrayList<>();
    private StatusRecyclerViewAdapter statusAdapter;
    private DashboardActivity activity;
    private ActionBar actionBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (DashboardActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);

        setHasOptionsMenu(true);
        activity.setSupportActionBar(toolbar);
        actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(getContext().getString(R.string.Timeline));
        }
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
        setupRecyclerView();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void updateAdapter(UpdateAdapterEvent event) {
        if (users.size()==0) {
            rlNoOne.setVisibility(View.VISIBLE);
            Glide.with(activity)
                    .load(R.drawable.noone)
                    .asGif()
                    .fitCenter()
                    .into(ivNoOne);
            swipeRefreshLayout.setVisibility(View.GONE);
        } else {
            rlNoOne.setVisibility(View.GONE);
            swipeRefreshLayout.setVisibility(View.VISIBLE);
        }
        statusAdapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
    }

    @Subscribe
    public void notifyAdminUpdated(NotifyAdminUpdatedEvent event) {
        fetchNearbyUsers();
    }

    @Subscribe
    public void notifyAdminPostStt(NotifyAdminPostSttEvent event) {
        users.add(0, JoininApplication.me);
        if (users.get(1).getId().equals(JoininApplication.me.getId())) {
            users.remove(1);
            statusAdapter.notifyItemChanged(0);
        } else {
            statusAdapter.notifyItemInserted(0);
        }
        recyclerView.scrollToPosition(0);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    swipeRefreshLayout.setRefreshing(true);
                    UserClient.fetchNearbyUsers(users);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary_1, R.color.colorPrimary_2);
        if (!swipeRefreshLayout.isRefreshing() && !JoininApplication.me.getId().equals("")) {
            fetchNearbyUsers();
        }
    }

    private void fetchNearbyUsers() {
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                UserClient.fetchNearbyUsers(users);
                swipeRefreshLayout.setRefreshing(true);
            }
        });
    }

    private void setupRecyclerView() {
        statusAdapter = new StatusRecyclerViewAdapter(activity, users);
        ScaleInAnimationAdapter adapter = new ScaleInAnimationAdapter(statusAdapter);
        adapter.setFirstOnly(false);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_dashboard, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_filter:
                showSearchFilterFragment();
                return true;
            case R.id.action_logout:
                activity.destroyAll();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSearchFilterFragment() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        SearchFilterFragment editNameDialog = SearchFilterFragment.newInstance();
        editNameDialog.show(fm, "fragment_seach_filter");
    }
}
