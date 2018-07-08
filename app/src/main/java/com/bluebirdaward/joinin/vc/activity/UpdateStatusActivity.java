package com.bluebirdaward.joinin.vc.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

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

import com.bluebirdaward.joinin.pojo.Status;
import com.bluebirdaward.joinin.utils.ImageHelper;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import com.bluebirdaward.joinin.R;
import com.bluebirdaward.joinin.aconst.Code;
import com.bluebirdaward.joinin.event.FinishUpdateStatusActivityEvent;

/**
 * Created by Administrator on 13-Apr-16.
 */
public class UpdateStatusActivity extends BaseActivityWithGoogle {

    @Bind(R.id.edt_status)
    EditText edtStatus;
    @Bind(R.id.edt_place)
    EditText edtPlace;
    @Bind(R.id.img_status)
    ImageView imgStatus;
    @Bind(R.id.toolbar)
    Toolbar toolbar;

    private ImageHelper imageHelper;
    private byte[] photo;
    private Status currentStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose_status);
        ButterKnife.bind(this);
        imageHelper = new ImageHelper(this);

        if (Build.VERSION.SDK_INT > 20)
            setSupportActionBar(toolbar);
        else
            toolbar.setVisibility(View.GONE);

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        setupView();

        imgStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(imageHelper.getTakePictureIntent(), Code.REQUEST_TAKE_PHOTO);
            }
        });
    }

    private void setupView() {
        if (currentStatus!=null)
            edtStatus.setText(currentStatus.getBody());
        else
            currentStatus = new Status();
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
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onClickCompose() {
        if (edtStatus.getText().length() != 0) {
            postStatus();
        } else {
            Toast.makeText(this, "Something missing!", Toast.LENGTH_LONG).show();
            closeProgressDialog();
        }
    }

    private void postStatus() {
        String body = edtStatus.getText().toString();
        String place = edtPlace.getText().toString();
//        StatusClient.pushStatusToParse(body, place, photo);
    }

    @Subscribe
    public void finishUpdateStatusActivity(FinishUpdateStatusActivityEvent event) {
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        closeProgressDialog();
        if (requestCode == Code.PLACE_PICKER_REQUEST && data!=null) {
            Place place = PlacePicker.getPlace(data, this);
            if (!place.getAddress().equals(""))
                edtPlace.setText(place.getName());
            else
                Toast.makeText(this, getString(R.string.Please_get_another_location), Toast.LENGTH_LONG).show();
        } else if (requestCode == Code.REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            try {
                String url = "";
                if (data!=null)
                    url = imageHelper.getPhotoPath(data);
                else
                    url = imageHelper.getmCurrentPhotoPath();
                Bitmap imageBitmap = imageHelper.decodeSampledBitmapFromResource(url, 320, 150);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                photo = stream.toByteArray();
                Glide.with(this)
                        .load(photo)
                        .bitmapTransform(new RoundedCornersTransformation(this, 13, 5))
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .into(imgStatus);
            } catch (OutOfMemoryError e) {
                Toast.makeText(this, R.string.Try_again, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @OnClick(R.id.img_map)
    public void goToPlaceActivity() {
        currentStatus.setBody(edtStatus.getText().toString());
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.pop_enter, R.anim.pop_exit);
    }
}
