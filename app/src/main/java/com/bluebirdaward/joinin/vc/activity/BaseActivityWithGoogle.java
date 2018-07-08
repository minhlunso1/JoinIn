package com.bluebirdaward.joinin.vc.activity;

import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.bluebirdaward.joinin.R;

/**
 * Created by Minh on 4/21/2016.
 */
public class BaseActivityWithGoogle extends BaseActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    protected GoogleApiClient mGoogleApiClient;

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient!=null)
            mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.getErrorCode() == ConnectionResult.API_UNAVAILABLE) {
            Toast.makeText(this, getString(R.string.google_play_services_issue), Toast.LENGTH_SHORT).show();
        }
    }
}
