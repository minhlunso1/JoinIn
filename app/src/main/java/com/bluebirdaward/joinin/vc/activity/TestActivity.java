package com.bluebirdaward.joinin.vc.activity;

import android.os.Bundle;
import android.widget.FrameLayout;

import com.bluebirdaward.joinin.vc.fragment.ProfileFragment;

/**
 * Created by Administrator on 11-Apr-16.
 */

//u guys can do anything with this activity
public class TestActivity extends BaseActivity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frame = new FrameLayout(this);
        if (savedInstanceState == null) {
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(android.R.id.content, new ProfileFragment()).commit();
        }
        setContentView(frame);
    }

}
