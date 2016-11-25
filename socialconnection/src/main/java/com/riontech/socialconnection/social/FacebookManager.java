package com.riontech.socialconnection.social;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.riontech.socialconnection.R;
import com.riontech.socialconnection.listeners.SocialConnectionListener;
import com.riontech.socialconnection.model.SocialUserResponse;
import com.riontech.socialconnection.utility.SocialConnectionUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Facebook Manager Class for getting facebook Details
 * Implement this class in your click event of button
 * and add FacebookResponse listener to get fb details.
 * it Will give you id , name ,access token, photo,location,gender,email.
 * <p>
 * Vaghela Pintu
 * 9601718963
 *
 * @auther vaghela.pintu31@gmail.com
 */
public class FacebookManager {

    // for display log
    private static final String TAG = FacebookManager.class.getSimpleName();
    //for context
    private Activity mActivity;
    // for access token
    private String mAccessToken;
    // for callback listener
    private SocialConnectionListener mResponse;
    // for facebook callback manager
    public CallbackManager mCallbackManager;

    /**
     * FaceBook Manager Constructor
     *
     * @param activity context
     * @param response listener
     */
    public FacebookManager(Activity activity, SocialConnectionListener response) {
        mActivity = activity;
        mResponse = response;
        getHashKeyOfYourProject();
        initFacebook();
    }


    /**
     * Getting Hash Key For Facebook
     */
    private void getHashKeyOfYourProject() {
        try {
            PackageInfo info = mActivity.getPackageManager().getPackageInfo(
                    mActivity.getPackageName(),
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());

                Log.d(TAG, "Getting Key Hash Issue With Face book put this Key On facebook");
                Log.d(TAG, "Key Hash = " + Base64.encodeToString(md.digest(),
                        Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }


    /**
     * initialization of facebook
     */
    private void initFacebook() {
//        if (SocialConnectionUtils.checkInternetConnection(mActivity)) {
        FacebookSdk.sdkInitialize(mActivity);
        mCallbackManager = CallbackManager.Factory.create();
        try {
            LoginManager.getInstance().logInWithReadPermissions(mActivity,
                    Arrays.asList(new String[]{"public_profile", "email", "user_location"}));
            LoginManager.getInstance().registerCallback(mCallbackManager,
                    callback);
        } catch (Exception e) {
            mResponse.onFailure(e);
            Log.e(TAG, e.getMessage(), e);
        }
//        } else {
//            SocialConnectionUtils.showToast(mActivity.getResources()
//                    .getString(R.string.no_internet_available), mActivity);
//        }
    }

    /**
     * Facebook Call back
     */
    private FacebookCallback<LoginResult> callback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            Log.d(TAG, "onSuccess");
            final AccessToken accessToken = loginResult.getAccessToken();
            mAccessToken = accessToken.getToken();

            GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
                @Override
                public void onCompleted(JSONObject object, GraphResponse response) {
                    if (response.getError() != null) {
                        Log.d(TAG, "Response = " + response.getError().toString());
                    } else {
                        try {
                            Log.d(TAG, "Response = " + object.toString());
                            String mEmail = object.getString("email");
                            String mUid = object.getString("id");
                            String mName = object.getString("name");
                            String mGender = object.getString("gender");
                            String mPhoto = "https://graph.facebook.com/" + mUid + "/picture";

                            String mLocation;
                            if (object.has("location")) {
                                if (object.getJSONObject("location") != null) {
                                    mLocation = object.getJSONObject("location").getString("name");
                                } else {
                                    mLocation = "NA";
                                }
                            } else {
                                mLocation = "NA";
                            }
                            SocialUserResponse obj = new SocialUserResponse();
                            obj.setUserAccessToken(mAccessToken);
                            obj.setUserEmail(mEmail);
                            obj.setUserGender(mGender);
                            obj.setUserId(mUid);
                            obj.setUserName(mName);
                            obj.setUserLocation(mLocation);
                            obj.setImgUrl(mPhoto);
                            mResponse.onSuccess(obj);
                        } catch (JSONException e) {
                            Log.e(TAG, e.getMessage(), e);
                        }
                    }
                }
            });

            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,name,email,gender,location");
            request.setParameters(parameters);
            request.executeAsync();
        }

        @Override
        public void onCancel() {
            Log.d(TAG, "onCancel");
        }

        @Override
        public void onError(FacebookException e) {
            Log.e(TAG, e.getMessage(), e);
            mResponse.onFailure(e);
        }
    };

}
