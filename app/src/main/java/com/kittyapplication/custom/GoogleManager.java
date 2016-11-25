package com.kittyapplication.custom;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;
import com.kittyapplication.utils.AppLog;
import com.riontech.socialconnection.listeners.SocialConnectionListener;
import com.riontech.socialconnection.model.SocialUserResponse;

/**
 * Created by Dhaval Soneji Riontech on 5/9/16.
 */
public class GoogleManager implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = GoogleManager.class.getSimpleName();
    private FragmentActivity mActivity;
    private final int RC_SIGN_IN = 9001;
    private GoogleApiClient mGoogleApiClient;
    private SocialConnectionListener mListener;


    public GoogleManager(FragmentActivity activity, SocialConnectionListener listener) {
        mActivity = activity;
        mListener = listener;
        stopGoogle();
        initGoogleApi();
    }


    private void initGoogleApi() {
        try {
            GoogleSignInOptions gso =
                    new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestEmail()
                            .requestProfile()
                            .build();

            mGoogleApiClient = new GoogleApiClient.Builder(mActivity)
                    .enableAutoManage(mActivity, this)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();
            signIn();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "connectionResultFail" + connectionResult.getErrorMessage());
    }

    private void handleSignInResult(GoogleSignInResult result) {
        try {
            Log.d(TAG, "handleSignInResult:" + result.isSuccess());
            if (result.isSuccess()) {
                // Signed in successfully, show authenticated UI.
                GoogleSignInAccount acct = result.getSignInAccount();
                AppLog.d(TAG, "Google JSON " + new Gson().toJson(acct).toString());
                SocialUserResponse user = new SocialUserResponse();
                try {
                    if (acct.getPhotoUrl() != null && acct.getPhotoUrl().toString().length() > 0) {
                        Uri uri = acct.getPhotoUrl();
                        user.setImgUrl(uri.toString());
                    } else {
                        user.setImgUrl("");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    user.setImgUrl("");
                }
                user.setUserName(acct.getDisplayName());
                user.setUserEmail(acct.getEmail());
                user.setUserId(acct.getId());
                mListener.onSuccess(user);
            } else {
                // Signed out, show unauthenticated UI.
                mListener.onFailure(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        mActivity.startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    public void stopGoogle() {
        try {
            if (mGoogleApiClient != null) {
                mGoogleApiClient.stopAutoManage(mActivity);
                mGoogleApiClient.disconnect();
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }
}