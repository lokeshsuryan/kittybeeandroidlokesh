package com.kittyapplication.sqlitedb.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.kittyapplication.core.utils.ConnectivityUtils;
import com.kittyapplication.core.utils.StringUtils;
import com.kittyapplication.sync.ChatSyncOperation;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;

/**
 * Created by MIT on 10/12/2016.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    private static final String TAG = NetworkChangeReceiver.class.getSimpleName();

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.e(TAG, "onReceive: ");
        StringUtils.printBundle(intent.getExtras());
        try {
            if (ConnectivityUtils.checkInternetConnection(context)) {
                ChatSyncOperation chatSyncOperation = new ChatSyncOperation(context);
                chatSyncOperation.syncDeletedDialog();
                chatSyncOperation.syncCreatedDialog();
                chatSyncOperation.syncMessages();

                // send broadcast to update network setting on screen
                chatSyncOperation.sendBroadCast(context);
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }
}
