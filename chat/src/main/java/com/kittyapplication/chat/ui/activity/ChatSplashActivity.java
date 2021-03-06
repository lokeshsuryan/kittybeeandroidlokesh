package com.kittyapplication.chat.ui.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.kittyapplication.chat.R;
import com.kittyapplication.chat.utils.Consts;
import com.kittyapplication.chat.utils.SharedPreferencesUtil;
import com.kittyapplication.core.gcm.GooglePlayServicesHelper;
import com.kittyapplication.core.ui.activity.CoreSplashActivity;
import com.kittyapplication.core.utils.ConnectivityUtils;
import com.kittyapplication.core.utils.ErrorUtils;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;

public abstract class ChatSplashActivity extends CoreSplashActivity {

    private static final String TAG = ChatSplashActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "QBUser" + SharedPreferencesUtil.hasQbUser());
        if (SharedPreferencesUtil.hasQbUser()) {
            proceedToTheNextActivity();
            Log.d(TAG, "QBUser already registered");
            return;
        }
        createSession();
    }

    private void createSession() {
        if (ConnectivityUtils.isNetworkConnected()) {
            QBAuth.createSession().performAsync(new QBEntityCallback<QBSession>() {
                @Override
                public void onSuccess(QBSession qbSession, Bundle bundle) {
                    GooglePlayServicesHelper helper = new GooglePlayServicesHelper();
                    if (helper.checkPlayServicesAvailable()) {
                        helper.registerForGcm(Consts.GCM_SENDER_ID);
                    }
                    proceedToTheNextActivity();
                }

                @Override
                public void onError(QBResponseException e) {
                    proceedToTheNextActivity();
                    // TODO Keyur Ashra Commented this code on 13-10-2016 because Gaurav faced issue of bad timestamp
                    // TODO and he don't wants to show error, instead directly create session in background again.
                    /*showSnackbarError(getRootView(), R.string.splash_create_session_error, e, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                        }
                    });*/
                }
            });
        } else {
            ErrorUtils.showSnackbar(getRootView(), R.string.no_internet_connection,
                    R.string.dlg_retry, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            createSession();
                        }
                    });
        }
    }
}