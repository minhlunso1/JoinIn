package com.bluebirdaward.joinin.vc.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.bluebirdaward.joinin.R;

public class BlueBirdActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blue_bird);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                launchWalkThrActivity();
            }
        }, 3000);
    }

    private void launchWalkThrActivity() {
        Intent i = new Intent(BlueBirdActivity.this, WalkthrActivity.class);
        startActivity(i);
    }
}
