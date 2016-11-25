package com.kittyapplication.core.gcm;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.kittyapplication.core.utils.StringUtils;
import com.kittyapplication.core.utils.constant.GcmConsts;


public abstract class CoreGcmPushListenerService extends GcmListenerService {
    private static final String TAG = CoreGcmPushListenerService.class.getSimpleName();

    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString(GcmConsts.EXTRA_GCM_MESSAGE);
        Log.v(TAG, "From: " + from);
        Log.v(TAG, "Message: " + message);
        StringUtils.printBundle(data);
        showNotification(message, data);
        sendPushMessageBroadcast(message, data);
    }

    protected abstract void showNotification(String message, Bundle bundle);

    protected abstract void sendPushMessageBroadcast(String message, Bundle bundle);
}