package com.kittyapplication.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.kittyapplication.sync.SyncGroupOperation;
import com.kittyapplication.sync.callback.DataSyncListener;

/**
 * Created by HalfBloodPrince(RIONTECH)
 * Riontech on 28/10/16.
 */

public class KittiesIntentService extends IntentService {
    private static final String TAG = KittiesIntentService.class.getSimpleName();
    private Context mContext;

    public KittiesIntentService() {
        super("KittiesIntentService");
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public KittiesIntentService(String name) {
        super(name);

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mContext = this;
        final SyncGroupOperation syncGroupOperation = new SyncGroupOperation();
        syncGroupOperation.syncGroups(new DataSyncListener() {
            @Override
            public void onCompleted(int itemCount) {
                syncGroupOperation.syncDialogs(0, 0, new DataSyncListener() {
                    @Override
                    public void onCompleted(int itemCount) {
                    }
                });
            }
        });
    }
}