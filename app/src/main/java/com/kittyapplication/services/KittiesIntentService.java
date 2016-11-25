package com.kittyapplication.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.content.LocalBroadcastManager;

import com.kittyapplication.AppApplication;
import com.kittyapplication.chat.utils.chat.ChatHelper;
import com.kittyapplication.chat.utils.qb.QbDialogHolder;
import com.kittyapplication.model.ChatData;
import com.kittyapplication.model.Kitty;
import com.kittyapplication.model.ServerResponse;
import com.kittyapplication.rest.Singleton;
import com.kittyapplication.sync.SyncGroupOperation;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.GroupPrefHolder;
import com.kittyapplication.utils.KittyPrefHolder;
import com.kittyapplication.utils.PreferanceUtils;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
        syncGroupOperation.syncGroups(new SyncGroupOperation.OnSyncComplete() {
            @Override
            public void onCompleted(boolean hasData) {
                syncGroupOperation.syncDialogs(new SyncGroupOperation.OnSyncComplete() {
                    @Override
                    public void onCompleted(boolean hasData) {
                    }
                });
            }
        });
    }
}