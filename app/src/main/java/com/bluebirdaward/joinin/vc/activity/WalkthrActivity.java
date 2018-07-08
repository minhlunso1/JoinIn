package com.bluebirdaward.joinin.vc.activity;

import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bluebirdaward.joinin.JoininApplication;
import com.bluebirdaward.joinin.R;
import com.bluebirdaward.joinin.adapter.WalkthrPagerAdapter;
import com.bluebirdaward.joinin.event.BroadcastMeEvent;
import com.bluebirdaward.joinin.event.LaunchDashBoardEvent;
import com.bluebirdaward.joinin.net.UserClient;
import com.bluebirdaward.joinin.utils.PermissionNewUtils;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.pixelcan.inkpageindicator.InkPageIndicator;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class WalkthrActivity extends BaseActivity implements LogInCallback {

    @Bind(R.id.walkthr_pager)
    ViewPager mPager;
    @Bind(R.id.loginBtn)
    TextView FbLoginBtn;
    @Bind(R.id.ink_indicator)
    InkPageIndicator inkPageIndicator;
    @Bind(R.id.logo)
    ImageView logo;

    private WalkthrPagerAdapter mPagerAdapter;
    private PermissionNewUtils permissionNewUtils;
    PermissionNewUtils.IDo iDo = new PermissionNewUtils.IDo() {
        @Override
        public void doWhat() {
            List<String> permissions = Arrays.asList("user_birthday", "email", "public_profile");
            ParseFacebookUtils.logInWithReadPermissionsInBackground(
                    WalkthrActivity.this,
                    permissions,
                    WalkthrActivity.this);
        }
    };
    private WalkthrActivity activity;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionNewUtils.onRequestPermissionsResult(this, requestCode, permissions, grantResults, iDo);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walkthr);
        ButterKnife.bind(this);
        permissionNewUtils = new PermissionNewUtils();
        activity = this;

        setupPager();
        setupFbBtn();
    }

    @Override
    protected void onResume() {
        super.onResume();
        runAnimation();
    }

    private void runAnimation() {
        Animation animation1 = AnimationUtils.loadAnimation(this, R.anim.slide_down_in);
        logo.startAnimation(animation1);
        animation1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_up_reverse);
                logo.startAnimation(animation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
        if (JoininApplication.isLogout)
            JoininApplication.isLogout=false;
        else
            launchDashboardActivity();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }

    private void setupPager() {
        //Set the pager with an adapter
        mPagerAdapter = new WalkthrPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        setupPageIndicator();
    }

    private void setupPageIndicator() {
        inkPageIndicator.setViewPager(mPager);
    }

    private void setupFbBtn() {
        FbLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permissionNewUtils.checkPermission(activity, iDo);
            }
        });
    }

    private void launchDashboardActivity() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && ParseUser.getCurrentUser() != null)
            startActivity(new Intent(WalkthrActivity.this, BroadcastMeActivity.class));
        else if (ParseUser.getCurrentUser() != null) {
            Intent i = new Intent(WalkthrActivity.this, DashboardActivity.class);
            startActivity(i);
        }
    }

    private void launchBroadcastMe() {
        if (ParseUser.getCurrentUser() != null) {
            Intent i = new Intent(WalkthrActivity.this, BroadcastMeActivity.class);
            startActivity(i);
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public void done(ParseUser user, ParseException e) {
        if (e != null) {
            Log.d("DEBUG", "Uh oh. Error occurred " + e.toString());
        } else if (user == null) {
            Log.d("DEBUG", "Uh oh. The user cancelled the Facebook login.");
        } else if (user.isNew()) {
            Log.d("DEBUG", "User signed up and logged in through Facebook!");
            getFacebookResult();
        } else {
            Log.d("DEBUG", "User logged in through Facebook!");
            launchDashboardActivity();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }

    private void getFacebookResult() {
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,first_name,last_name,email,gender,birthday,picture.type(large),cover");
        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        UserClient.updateUserFbInfo(object);
                        Log.d("DEBUG", response.toString());
                    }
                });
        request.setParameters(parameters);
        request.executeAsync();
    }

    @Subscribe
    public void launchDashBoard(LaunchDashBoardEvent event) {
        launchDashboardActivity();
    }

    @Subscribe
    public void launchBroadcast(BroadcastMeEvent event) {
        launchDashboardActivity();
    }
}
