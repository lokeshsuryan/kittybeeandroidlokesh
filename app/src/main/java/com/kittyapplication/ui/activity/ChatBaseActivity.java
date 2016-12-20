package com.kittyapplication.ui.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;

import com.kittyapplication.AppApplication;
import com.kittyapplication.chat.utils.Consts;
import com.kittyapplication.chat.utils.SharedPreferencesUtil;
import com.kittyapplication.chat.utils.chat.ChatHelper;
import com.kittyapplication.chat.utils.qb.QbAuthUtils;
import com.kittyapplication.chat.utils.qb.QbSessionStateCallback;
import com.kittyapplication.core.utils.ErrorUtils;
import com.kittyapplication.core.utils.SharedPrefsHelper;
import com.kittyapplication.rest.Singleton;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.model.QBUser;

/**
 * Created by MIT on 8/18/2016.
 */
public abstract class ChatBaseActivity extends BaseActivity implements QbSessionStateCallback {
    private static final String TAG = ChatBaseActivity.class.getSimpleName();
    private static final int REQUEST_TELEPHONY_SERVICE_PERMISSION = 200;
    private static final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    protected ActionBar actionBar;
    public boolean isAppSessionActive;
    private int retry = 0;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionBar = getSupportActionBar();

        boolean wasAppRestored = savedInstanceState != null;
        boolean isQbSessionActive = QbAuthUtils.isSessionActive();
        final boolean needToRestoreSession = wasAppRestored || !isQbSessionActive;
        Log.v(TAG, "wasAppRestored = " + wasAppRestored);
        Log.v(TAG, "isQbSessionActive = " + isQbSessionActive);

        // Triggering callback via Handler#post() method
        // to let child's code in onCreate() to execute first
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (needToRestoreSession) {
                    recreateChatSession();
                    isAppSessionActive = false;
                } else {
                    onSessionCreated(true);
                    subscribeForPushNotification();
                    isAppSessionActive = true;
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putInt("dummy_value", 0);
        super.onSaveInstanceState(outState, outPersistentState);
    }

    public abstract View getSnackbarAnchorView();

    protected Snackbar showErrorSnackbar(@StringRes int resId, Exception e,
                                         View.OnClickListener clickListener) {
        return ErrorUtils.showSnackbar(getSnackbarAnchorView(), resId, e,
                com.kittyapplication.chat.R.string.dlg_retry, clickListener);
    }

    private void recreateChatSession() {
        Log.d(TAG, "Need to recreate chat session");

        QBUser user = SharedPreferencesUtil.getQbUser();
        if (user != null) {
            reloginToChat(user);
        }
    }

    private void reloginToChat(final QBUser user) {
        ChatHelper.getInstance().login(user, new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void result, Bundle bundle) {
                Log.v(TAG, "Chat login onSuccess()");
                isAppSessionActive = true;
                onSessionCreated(true);
                subscribeForPushNotification();
            }

            @Override
            public void onError(QBResponseException e) {
                isAppSessionActive = false;
                if (retry < 3) {
                    retry++;
                    reloginToChat(user);
                    onSessionCreated(false);
                } else {
                    retry = 0;
                }
            }
        });
    }

    private void subscribeForPushNotification() {
        try {
            if (checkCallPermission()) {
                if (!SharedPrefsHelper.getInstance().<Boolean>get(Consts.QB_SUBSCRIPTION, false)) {
                    QbAuthUtils.subscribeWithQBPushNotification(AppApplication.getGCMId());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*private boolean checkCallPermission() {
        try {
            if (Build.VERSION.SDK_INT >= 23) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(
                            new String[]{
                                    Manifest.permission.READ_PHONE_STATE},
                            REQUEST_TELEPHONY_SERVICE_PERMISSION);
                    return false;
                } else {
                    return true;
                }
            } else {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }*/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try {
            Log.d(TAG, "onRequestPermissionsResult: ");
            if (grantResults != null && grantResults.length > 0)
                for (int i = 0; i < grantResults.length; i++) {
                    if (i == grantResults.length) {
                        subscribeForPushNotification();
                        Singleton.getInstance().getLocationUtils().initGoogleApi();
                    }
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checkCallPermission() {
        try {
            if (Build.VERSION.SDK_INT >= 23) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                        != PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(this,
                                Manifest.permission.ACCESS_COARSE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(
                            new String[]{
                                    Manifest.permission.READ_PHONE_STATE,
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION},
                            REQUEST_TELEPHONY_SERVICE_PERMISSION);
                    return false;
                } else {
                    return true;
                }
            } else {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
