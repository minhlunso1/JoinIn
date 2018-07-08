package com.bluebirdaward.joinin.vc.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;

import com.bluebirdaward.joinin.aconst.Code;
import com.bluebirdaward.joinin.pojo.User;
import com.bluebirdaward.joinin.vc.fragment.ProfileFragment;

/**
 * Created by Minh on 4/17/2016.
 */
public class ProfileActivity extends BaseActivity {

    private User user;
    private int hackInt;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        user = intent.getParcelableExtra("user");
        hackInt = intent.getIntExtra(Code.INT_HACK, -1);

        FrameLayout frame = new FrameLayout(this);
        if (savedInstanceState == null) {
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(android.R.id.content, ProfileFragment.newInstance(false, user)).commit();
        }
        setContentView(frame);
    }
}
