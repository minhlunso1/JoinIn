package com.bluebirdaward.joinin;

import android.app.Application;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.multidex.MultiDexApplication;
import android.support.v4.BuildConfig;
import android.support.v4.app.ActivityCompat;

import com.bluebirdaward.joinin.net.MyAuthenticationListener;
import com.bluebirdaward.joinin.net.UserClient;
import com.bluebirdaward.joinin.pojo.DemoParticipantProvider;
import com.bluebirdaward.joinin.pojo.Filter;
import com.bluebirdaward.joinin.pojo.User;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.maps.model.LatLng;
import com.layer.atlas.messagetypes.text.TextCellFactory;
import com.layer.atlas.messagetypes.threepartimage.ThreePartImageUtils;
import com.layer.atlas.provider.ParticipantProvider;
import com.layer.atlas.util.picasso.requesthandlers.MessagePartRequestHandler;
import com.layer.sdk.LayerClient;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.squareup.picasso.Picasso;

import java.util.Arrays;

import com.bluebirdaward.joinin.net.MyConnectionListener;

/**
 * Created by Minh on 4/10/2016.
 */

public class JoininApplication extends MultiDexApplication {

    public static User me;
    public static boolean isLogout;
    public static Filter filter;
    private static Application sInstance;
    //Global variables used to manage the Layer Client and the conversations in this app
    public static LayerClient layerClient;
    public static final String GCM_PROJECT_NUMBER = "338490063792";
    public static final String LAYER_APP_ID = "layer:///apps/staging/a64e0d8c-fee0-11e5-81fd-eecb0000384a";

    private static LocationListener mLocationListener;
    private static LocationManager locationManager;
    private static Picasso sPicasso;

    public static Application getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        me = new User();
        filter = new Filter();
        sInstance = this;
        initializeParseAndFacebook();
        setupDeviceLocation();
        setupLayer();
        LayerClient client = getLayerClient();
        MobileAds.initialize(getApplicationContext(), getString(R.string.banner_ad_unit_id));
    }

    private void setupDeviceLocation(){
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {
                //Log.d("debug", "change "+location.getLatitude());
                me.setCurrentLocation(new LatLng(location.getLatitude(), location.getLongitude()));
                UserClient.updateUserLocation();
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        updateLocation();
    }

    public static void updateLocation() {
        if (ActivityCompat.checkSelfPermission(sInstance, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(sInstance, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 8000, Criteria.ACCURACY_FINE, mLocationListener);
            else
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 8000, Criteria.ACCURACY_FINE, mLocationListener);
        }
    }

    private void initializeParseAndFacebook() {
        Parse.enableLocalDatastore(this);
        Parse.initialize(this,
                getResources().getString(com.bluebirdaward.joinin.R.string.parse_app_id),
                getResources().getString(com.bluebirdaward.joinin.R.string.parse_client_key));
        ParseFacebookUtils.initialize(this);
        AppEventsLogger.activateApp(this);
    }

    private void setupLayer() {
        // Enable verbose logging in debug builds
        if (BuildConfig.DEBUG) {
            com.layer.atlas.util.Log.setAlwaysLoggable(true);
            LayerClient.setLoggingEnabled(this, true);
        }

        // Allow the LayerClient to track app state
        LayerClient.applicationCreated(this);
    }

    //==============================================================================================
    // Getters / Setters
    //==============================================================================================

    /**
     * Gets or creates a LayerClient, using a default set of LayerClient.Options and flavor-specific
     * App ID and Options from the `generateLayerClient` method.  Returns `null` if the flavor was
     * unable to create a LayerClient (due to no App ID, etc.).
     *
     * @return New or existing LayerClient, or `null` if a LayerClient could not be constructed.
     * @see
     */
    public static LayerClient getLayerClient() {
        if (layerClient == null) {
            // Custom options for constructing a LayerClient
            LayerClient.Options options = new LayerClient.Options();

                    /* Fetch the minimum amount per conversation when first authenticated */
            options.historicSyncPolicy(LayerClient.Options.HistoricSyncPolicy.ALL_MESSAGES);

                    /* Automatically download text and ThreePartImage info/preview */
            options.autoDownloadMimeTypes(Arrays.asList(
                    TextCellFactory.MIME_TYPE,
                    ThreePartImageUtils.MIME_TYPE_INFO,
                    ThreePartImageUtils.MIME_TYPE_PREVIEW));

            options.googleCloudMessagingSenderId(GCM_PROJECT_NUMBER);
            layerClient = LayerClient.newInstance(sInstance, LAYER_APP_ID, options);

            /* Register AuthenticationProvider for handling authentication challenges */
            layerClient.registerConnectionListener(new MyConnectionListener());
            layerClient.registerAuthenticationListener(new MyAuthenticationListener());
        }
        if (!layerClient.isConnected()) {
            //If Layer is not connected, make sure we connect in order to send/receive messages.
            // MyConnectionListener.java handles the callbacks associated with Connection, and
            // will start the Authentication process once the connection is established
            layerClient.connect();

        } else if (!layerClient.isAuthenticated()) {
            //If the client is already connected, try to authenticate a user on this device.
            // MyAuthenticationListener.java handles the callbacks associated with Authentication
            // and will start the Conversation View once the user is authenticated
            layerClient.authenticate();

        }
        return layerClient;
    }

    public static Picasso getPicasso() {
        if (sPicasso == null) {
            // Picasso with custom RequestHandler for loading from Layer MessageParts.
            sPicasso = new Picasso.Builder(sInstance)
                    .addRequestHandler(new MessagePartRequestHandler(getLayerClient()))
                    .build();
        }
        return sPicasso;
    }

    public static ParticipantProvider getParticipantProvider() {
        return new DemoParticipantProvider(sInstance).setLayerAppId(LAYER_APP_ID);
    }

}
