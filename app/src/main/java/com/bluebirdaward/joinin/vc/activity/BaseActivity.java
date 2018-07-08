package com.bluebirdaward.joinin.vc.activity;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

import com.github.ybq.android.spinkit.style.FadingCircle;

import com.bluebirdaward.joinin.R;

/**
 * Created by Minh on 4/10/2016.
 */
public class BaseActivity extends AppCompatActivity {
    private ProgressDialog progressDialog;

    public void showProgressDialog() {
        progressDialog = new ProgressDialog(this, R.style.Theme_MyDialog2);
        progressDialog.setIndeterminateDrawable(new FadingCircle());
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        Window window = progressDialog.getWindow();
        window.setLayout(200, 200);
    }

    public void closeProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

}