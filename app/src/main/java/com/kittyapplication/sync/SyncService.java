package com.kittyapplication.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.kittyapplication.utils.AppLog;

/**
 * Created by Scorpion on 04-08-2015.
 */
public class SyncService extends Service {

    private static final Object LOCK = new Object();
    private volatile static SynchAdapter sSynchAdapter = null;

    /*
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        AppLog.e("SyncService", "-----onCreate SyncService-----");
        if (sSynchAdapter == null) {
            synchronized (LOCK) {
                if (sSynchAdapter == null) {
                    sSynchAdapter = new SynchAdapter(getApplicationContext(), true);
                }
            }
        }
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public IBinder onBind(Intent intent) {
        return sSynchAdapter.getSyncAdapterBinder();
    }
}