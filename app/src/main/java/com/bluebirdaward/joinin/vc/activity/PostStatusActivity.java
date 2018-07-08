package com.bluebirdaward.joinin.vc.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bluebirdaward.joinin.JoininApplication;
import com.bluebirdaward.joinin.R;
import com.bluebirdaward.joinin.aconst.Code;
import com.bluebirdaward.joinin.event.FinishUpdateStatusActivityEvent;
import com.bluebirdaward.joinin.net.StatusClient;
import com.bluebirdaward.joinin.utils.ImageHelper;
import com.bluebirdaward.joinin.vc.fragment.PickActionFragment;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.ByteArrayOutputStream;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PostStatusActivity extends BaseActivityWithGoogle {
    public @Bind(R.id.edt_status)
    EditText edtStatus;
    @Bind(R.id.img_camera)
    ImageView imgCamera;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.img_ava)
    ImageView ivAva;
    @Bind(R.id.tv_name)
    TextView tvName;
    @Bind(R.id.btnClose)
    ImageView btnClose;
    @Bind(R.id.img_status_layout)
    RelativeLayout imgSttLayout;
    @Bind(R.id.img_status)
    ImageView imgStatus;

    private ImageHelper imageHelper;
    private byte[] photo;
    public String place = "";
    public String action = "";
    public String tempAction = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_status);
        ButterKnife.bind(this);
        imageHelper = new ImageHelper(this);

        if (Build.VERSION.SDK_INT > 20) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(R.string.post);
        } else
            toolbar.setVisibility(View.GONE);

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        setupView();

        imgCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(imageHelper.getTakePictureIntent(), Code.REQUEST_TAKE_PHOTO);
            }
        });
    }

    private void setupView() {
        updateStatusInfoUI();
        Glide.with(this).load(JoininApplication.me.getImgAva()).asBitmap().into(ivAva);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unbindImgSttToView();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
        mGoogleApiClient.connect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_update_status, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_done:
                showProgressDialog();
                onClickCompose();
                return true;
            case R.id.action_cancel:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onClickCompose() {
        if (place.length() != 0) {
            postStatus();
        } else {
            Toast.makeText(this, "Please pick up your location!", Toast.LENGTH_LONG).show();
            closeProgressDialog();
        }
    }

    private void postStatus() {
        String body = edtStatus.getText().toString();
        StatusClient.pushStatusToParse(body, action, place, photo);
    }

    @Subscribe
    public void finishUpdateStatusActivity(FinishUpdateStatusActivityEvent event) {
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        closeProgressDialog();
        if (requestCode == Code.PLACE_PICKER_REQUEST && data != null) {
            Place place = PlacePicker.getPlace(data, this);
            if (!place.getAddress().equals("")) {
                this.place = place.getName().toString();
                updateStatusInfoUI();
            }
            else
                Toast.makeText(this, getString(R.string.Please_get_another_location), Toast.LENGTH_LONG).show();
        } else if (requestCode == Code.REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            try {
                String url = "";
                if (data != null)
                    url = imageHelper.getPhotoPath(data);
                else
                    url = imageHelper.getmCurrentPhotoPath();
                Bitmap imageBitmap = imageHelper.decodeSampledBitmapFromResource(url, 320, 150);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                photo = stream.toByteArray();
                bindImgSttToView();
            } catch (OutOfMemoryError e) {
                Toast.makeText(this, R.string.Try_again, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void bindImgSttToView() {
        imgSttLayout.setVisibility(View.VISIBLE);
        Glide.with(this)
                .load(photo)
                .asBitmap().fitCenter().into(imgStatus);
    }

    private void unbindImgSttToView() {
        imgSttLayout.setVisibility(View.GONE);
        photo = null;
    }

    @OnClick(R.id.img_map)
    public void goToPlaceActivity() {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(this), Code.PLACE_PICKER_REQUEST);
            showProgressDialog();
        } catch (GooglePlayServicesRepairableException e) {
            Toast.makeText(this, getString(R.string.google_play_services_issue), Toast.LENGTH_SHORT).show();
        } catch (GooglePlayServicesNotAvailableException e) {
            Toast.makeText(this, getString(R.string.google_play_services_issue), Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.img_action)
    public void launchPickActionFragment() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edtStatus.getWindowToken(), 0);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
        fragmentTransaction.replace(android.R.id.content, PickActionFragment.newInstance(action));
        fragmentTransaction.addToBackStack("action_fragment");
        fragmentTransaction.commit();
    }

    public void updateStatusInfoUI() {
        String s = "";

        //Add name
        s = "<b>" + JoininApplication.me.getName() + " </b>";

        //Add buffer
        if (!action.equals("") || !place.equals(""))
            s += " -";

        //Add action
        if (!action.equals("")) {
            s += " <b>" + action + " </b>";
            //Add place
            if (!place.equals(""))
                s += " at <b>" +  place + " </b>";
        } else if (!place.equals(""))
            s += " At <b>" + place + " </b>";

        //Update textView
        tvName.setText(Html.fromHtml(s));
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setTempAction(String temp) {
        this.tempAction = temp;
    }

    public String getTempAction() {
        return tempAction;
    }
}
