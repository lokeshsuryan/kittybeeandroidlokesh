package com.kittyapplication.services;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.kittyapplication.R;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.PreferanceUtils;
import com.kittyapplication.utils.Utils;

import java.io.IOException;

/**
 * Created by riontech4 on 27/4/16.
 */
public class GCMRegistrationService extends IntentService {

    private static final String TAG = GCMRegistrationService.class.getSimpleName();
    public static final String REGISTRATION_SUCCESS = "RegistrationSuccess";
    public static final String REGISTRATION_ERROR = "RegistrationError";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public GCMRegistrationService() {
        super(GCMRegistrationService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        AppLog.d(TAG, "onHandleIntent");
        try {
            if (Utils.checkInternetConnection(getApplicationContext())) {
                InstanceID instanceID = InstanceID.getInstance(this);

                String token = instanceID.getToken(getResources().getString(R.string.gcm_project_id),
                        GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                //AppApplication.setGCMID(this, token);
                AppLog.d(TAG, "GCM ID = " + token);
                PreferanceUtils.setGCMID(this, token);

            } else {
                AppLog.e(TAG, getResources().getString(R.string.no_internet_available));
            }
        } catch (IOException e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }
}
