package com.kittyapplication.services;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.content.LocalBroadcastManager;

import com.kittyapplication.AppApplication;
import com.kittyapplication.R;
import com.kittyapplication.core.gcm.CoreGcmPushListenerService;
import com.kittyapplication.core.utils.ActivityLifecycle;
import com.kittyapplication.core.utils.NotificationUtils;
import com.kittyapplication.core.utils.ResourceUtils;
import com.kittyapplication.core.utils.constant.GcmConsts;
import com.kittyapplication.ui.activity.HomeActivity;
import com.kittyapplication.ui.activity.NotificationActivity;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.PreferanceUtils;

/**
 * Created by riontech4 on 27/4/16.
 */
public class GCMIntentService extends CoreGcmPushListenerService {
    private static final String TAG = GCMIntentService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 1;
    private static final String QB_DIALOG_ID = "dialog_id";

    @Override
    protected void showNotification(String message, Bundle data) {
        try {
            if (PreferanceUtils.getLoginUserObject(this) != null) {
                if (data.containsKey(QB_DIALOG_ID)) {
                    if (ActivityLifecycle.getInstance().isBackground()) {
                        NotificationUtils.showNotification(this, HomeActivity.class,
                                ResourceUtils.getString(AppApplication.getInstance(),
                                        R.string.app_name), message,
                                R.drawable.ic_noti_small, (int) System.currentTimeMillis());
                    }
                } else {
                    PreferanceUtils.setHasNotification(this, true);
                    NotificationUtils.showNotification(this, NotificationActivity.class,
                            ResourceUtils.getString(AppApplication.getInstance(),R.string.app_name), message,
                            R.drawable.ic_noti_small, (int) System.currentTimeMillis());

                    // Start intent service to get group data
                    WorkerThread mQbDialogWorkerThread = new WorkerThread("BackgroundKittyThread");
                    mQbDialogWorkerThread.start();
                    mQbDialogWorkerThread.prepareHandler();
                    mQbDialogWorkerThread.postTask(task);
                }
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    protected void sendPushMessageBroadcast(String message, Bundle data) {
        Intent intentBroadcast = new Intent(AppConstant.NOTIFICAION_ATTENTION);
        intentBroadcast.putExtra(AppConstant.NOTIFICAION_ATTENTION, true);

        if (data.containsKey(QB_DIALOG_ID)) {
            intentBroadcast = new Intent(GcmConsts.ACTION_NEW_GCM_EVENT);
            intentBroadcast.putExtra(GcmConsts.EXTRA_GCM_MESSAGE, data);
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(intentBroadcast);
    }

    /**
     *
     */
    private class WorkerThread extends HandlerThread {
        private Handler mWorkerHandler;

        WorkerThread(String name) {
            super(name);
        }

        void postTask(Runnable task) {
            mWorkerHandler.post(task);
        }

        void prepareHandler() {
            mWorkerHandler = new Handler(getLooper());
        }

    }

    /**
     *
     */
    Runnable task = new Runnable() {
        @Override
        public void run() {
            if (!AppApplication.getInstance().isRefresh()) {
                startService(new Intent(GCMIntentService.this, KittiesIntentService.class));
            }
        }
    };
}
