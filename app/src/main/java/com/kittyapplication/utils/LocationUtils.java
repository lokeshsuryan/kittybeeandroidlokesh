package com.kittyapplication.utils;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.kittyapplication.AppApplication;

public class LocationUtils implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = LocationUtils.class.getSimpleName();

    private Context mContext;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private double mLatitude;
    private double mLongitude;
    private LocationUpdateListener mListener;
    private final int REQUEST_LOCATION_PERMISSION = 101;

    public LocationUtils() {
        mLatitude = 0.0;
        mLongitude = 0.0;
    }

    /**
     * Initialise google api
     */
    public void initGoogleApi() {
        try {
            mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();

            if (mGoogleApiClient != null) {
                mGoogleApiClient.connect();
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        createLocationRequest();
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();

    }

    @Override
    public void onLocationChanged(Location location) {
        try {
            if (location != null) {
                mLatitude = location
                        .getLatitude();
                mLongitude = location.getLongitude();
                AppLog.d(TAG, "onLocation changed Lat = " + mLatitude);
                AppLog.d(TAG, "onLocation changed Lng = " + mLongitude);

                LocationServices.FusedLocationApi.
                        removeLocationUpdates(mGoogleApiClient, this);

            } else {
                AppLog.d(TAG, "Location is null");
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }


    /**
     * On connect application will create location request for specific interval
     */
    public void createLocationRequest() {
        final long locationInterval = AppConstant.LOCATION_INTERVAL;
        int distance = AppConstant.MIN_DISTANCE;
        int priority = AppConstant.LOCATION_PRIORITY;

        mLocationRequest = LocationRequest.create();
//        mLocationRequest.setInterval(locationInterval);
//        mLocationRequest.setNumUpdates(1);
        mLocationRequest.setPriority(priority);
        mLocationRequest.setSmallestDisplacement(distance);
//        mLocationRequest.setExpirationDuration(AppConstant.EXPIRE_TIME_LOCATION);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        startLocationUpdates(locationInterval);
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().


                            if (mContext instanceof Activity)
                                status.startResolutionForResult((Activity) mContext, 1000);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    /**
     */
    @TargetApi(Build.VERSION_CODES.M)
    public void startLocationUpdates(final long locationInterval) {
        try {
            if (checkPermission()) {
//                LocationServices.FusedLocationApi.
//                        requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

                Location lastKnownLocation = LocationServices.FusedLocationApi
                        .getLastLocation(mGoogleApiClient);

                if (lastKnownLocation != null) {
                    mLatitude = lastKnownLocation
                            .getLatitude();
                    mLongitude = lastKnownLocation.getLongitude();

                    AppLog.d(TAG, "Last Known Lat = " + lastKnownLocation.getLatitude());
                    AppLog.d(TAG, "Last Known Lng = " + lastKnownLocation.getLongitude());

                    Location location = new Location("current location");
                    location.setLongitude(lastKnownLocation.getLongitude());
                    location.setLatitude(lastKnownLocation.getLatitude());
                    AppApplication.getInstance().setLocation(location);

                    if (mListener != null) {
                        mListener.onLocationUpdate();
                        turnOFFLocation();
                    }

                } else {
                    AppLog.d(TAG, "Last known location is not available...");
                }
                turnOFFLocation();
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }


    /**
     * @return
     */
    private boolean checkPermission() {
        boolean result = false;
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(mContext,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(mContext,
                            Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                requestAccountPermission();
            } else {
                result = true;
            }
        } else {
            result = true;
        }
        return result;
    }


    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        AppLog.d(getClass().getSimpleName(), "onRequestPermissionsResult  " + requestCode);
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                // If request is cancelled, the result arrays are empty.
                try {
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED
                            && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                        AppLog.d(getClass().getSimpleName(), "PERMISSION GRANTED BY USER... GO AHEAD..");
                    } else {
                        AppLog.d(getClass().getSimpleName(), "PERMISSION NOT GRANTED... BACK TO APP..");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }


    /**
     * Requests the {@link Manifest.permission#CAMERA} permission.
     * If an additional rationale should be displayed, the user has to launch the request from
     * a SnackBar that includes additional information.
     */
    private void requestAccountPermission() {
        // Permission has not been granted and must be requested.
        AppLog.d(getClass().getSimpleName(), "requestAccountPermission  ");
        if (mContext instanceof Activity) {
            AppLog.d(getClass().getSimpleName(), "requestAccountPermission  >>>>>>");

            ActivityCompat.requestPermissions((Activity) mContext,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
    }

    /**
     * @param fromLat
     * @param fromLng
     * @param toLat
     * @param toLng
     * @return
     */
    public static float getDistanceInKm(double fromLat, double fromLng,
                                        double toLat, double toLng) {
        float distance;
        Location sourceLocation = new Location("From");
        sourceLocation.setLatitude(fromLat);
        sourceLocation.setLongitude(fromLng);
        Location destinationLocation = new Location("To");
        destinationLocation.setLatitude(toLat);
        destinationLocation.setLongitude(toLng);
        distance = sourceLocation.distanceTo(destinationLocation);
        distance = distance / 1000;
        return distance;
    }

    /**
     * Check location is enable or not
     *
     * @param context
     * @return true/false
     */
    public boolean isLocationEnabled(Context context) {
        int locationMode = 0;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(),
                        Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                AppLog.e(TAG, e.getMessage(), e);
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        } else {
            final LocationManager manager = (LocationManager) context
                    .getSystemService(Context.LOCATION_SERVICE);
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    && !manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                return false;
            }
            return true;
        }
    }

    public void setActivity(Context mActivity) {
        this.mContext = mActivity;
    }

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    public void setListener(LocationUpdateListener Listener) {
        this.mListener = Listener;
    }

    private void turnOFFLocation() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }


    public interface LocationUpdateListener {
        void onLocationUpdate();
    }
}