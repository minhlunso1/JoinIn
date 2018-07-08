package com.bluebirdaward.joinin.vc.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.bluebirdaward.joinin.JoininApplication;
import com.bluebirdaward.joinin.R;
import com.bluebirdaward.joinin.aconst.Code;
import com.bluebirdaward.joinin.anim.ExplosionM;
import com.bluebirdaward.joinin.event.FinishDashboardAcvitityEvent;
import com.bluebirdaward.joinin.event.ShowAdminProfileEvent;
import com.bluebirdaward.joinin.net.UserClient;
import com.bluebirdaward.joinin.utils.SmartFragmentStatePagerAdapter;
import com.bluebirdaward.joinin.vc.fragment.ChatHistoryFragment;
import com.bluebirdaward.joinin.vc.fragment.FavoriteFragment;
import com.bluebirdaward.joinin.vc.fragment.HomeFragment;
import com.bluebirdaward.joinin.vc.fragment.ProfileFragment;
import com.bluebirdaward.joinin.vc.fragment.SearchFilterFragment;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DashboardActivity extends BaseActivity implements SearchFilterFragment.SearchFilterFragmentListener {

    public static final String LAYER_APP_ID = "layer:///apps/staging/a64e0d8c-fee0-11e5-81fd-eecb0000384a";
    public static final String GCM_PROJECT_NUMBER = "338490063792";

    private SmartFragmentStatePagerAdapter adapterViewPager;
    private ExplosionM explosionM;

    @Bind(R.id.viewPager)
    ViewPager viewPager;
    @Bind(R.id.tabs)
    PagerSlidingTabStrip tabStrip;
    @Bind(R.id.fab)
    FloatingActionButton fab;
    @Bind(R.id.adView)
    AdView adView;
    @Bind(R.id.fab_border)
    FloatingActionButton fabBorder;
    @Bind(R.id.ads_area)
    View adsArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        ButterKnife.bind(this);

        initialViewSetup();
        UserClient.refreshCurrentUserData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        JoininApplication.updateLocation();
        JoininApplication.getLayerClient();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void finishActivity(FinishDashboardAcvitityEvent event) {
        finish();
    }

    @Subscribe
    public void showAdminProfile(ShowAdminProfileEvent event) {
        if (viewPager != null)
            viewPager.setCurrentItem(3, true);
    }

    private void initialViewSetup() {
        explosionM = ExplosionM.attach2Window(this);
        setupViewPager();
        if (Build.VERSION.SDK_INT<21)
            fab.bringToFront();

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                closeAds();
            }
            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                closeAds();
            }
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                adsArea.setVisibility(View.VISIBLE);
            }
        });
    }


    private void setupViewPager() {
        adapterViewPager = new DashboardPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapterViewPager);
        tabStrip.setViewPager(viewPager);
        int pos = getIntent().getIntExtra(Code.TAB_POS, 0);
        viewPager.setCurrentItem(pos);
    }

    @Override
    public void onFilter() {
        Toast.makeText(DashboardActivity.this, getString(R.string.filter_changed), Toast.LENGTH_SHORT).show();
        HomeFragment fragment = (HomeFragment) adapterViewPager.getRegisteredFragment(0);
        UserClient.fetchNearbyUsers(fragment.users);
    }

    public class DashboardPagerAdapter extends SmartFragmentStatePagerAdapter
            implements PagerSlidingTabStrip.IconTabProvider {

        private final int[] ICONS = {R.drawable.ic_home,
                R.drawable.ic_favorite_filled_large, R.drawable.ic_comment, R.drawable.ic_profile,};

        public DashboardPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new HomeFragment();
                case 1:
                    return new FavoriteFragment();
                case 2:
                    return new ChatHistoryFragment();
                case 3:
                    return ProfileFragment.newInstance(true, JoininApplication.me);
                default:
                    return new HomeFragment();
            }
        }

        @Override
        public int getPageIconResId(int position) {
            return ICONS[position];
        }

        @Override
        public int getCount() {
            return ICONS.length;
        }


    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @OnClick(R.id.fab)
    public void goToUpdateStatus() {
        startActivity(new Intent(DashboardActivity.this, PostStatusActivity.class));
    }

    @OnClick(R.id.img_close_ads)
    public void closeAds() {
        adsArea.setVisibility(View.GONE);
    }

    public void destroyAll(){
        fabBorder.setVisibility(View.INVISIBLE);
        explosionM.explodeWithLogout(viewPager, fab, tabStrip);
    }
}
