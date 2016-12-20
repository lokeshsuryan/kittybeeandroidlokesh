package com.kittyapplication.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;

import com.kittyapplication.model.OfflineDao;
import com.kittyapplication.model.OfflineSummeryMembers;
import com.kittyapplication.model.ServerResponse;
import com.kittyapplication.rest.Singleton;
import com.kittyapplication.sqlitedb.Operations;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.PreferanceUtils;
import com.kittyapplication.utils.Utils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by HalfBloodPrince(RIONTECH)
 * Riontech on 28/9/16.
 */
public class OfflineSupportIntentService extends IntentService {
    private static final String TAG = OfflineSupportIntentService.class.getSimpleName();

    private Context mContext;
    private List<OfflineDao> list;

    public OfflineSupportIntentService() {
        super("OfflineSupportIntentService");
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public OfflineSupportIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        AppLog.d(TAG, "Inside onHandleIntent of service");
        mContext = this;
        offLineTask();
    }

    /**
     *
     */
    private void offLineTask() {
        try {
            if (Utils.checkInternetConnection(mContext)) {
                String userId = PreferanceUtils.getLoginUserObject(mContext).getUserID();

                Call<ServerResponse<List<OfflineDao>>> call =
                        Singleton.getInstance().getRestOkClient().
                                getOfflineSupportData(userId);
                call.enqueue(offlineCallBack);
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    /**
     *
     */
    private Callback<ServerResponse<List<OfflineDao>>> offlineCallBack =
            new Callback<ServerResponse<List<OfflineDao>>>() {
                @Override
                public void onResponse(Call<ServerResponse<List<OfflineDao>>> call, Response<ServerResponse<List<OfflineDao>>> response) {
                    try {
                        if (response.code() == 200) {
                            if (response.body().getStatus().equalsIgnoreCase(AppConstant.SUCCESS)) {
                                if (Utils.isValidList(response.body().getData())) {
                                    // doInBackground(response.body().getData());
                                    list = response.body().getData();
                                    WorkerThread mQbDialogWorkerThread = new WorkerThread("myWorkerThread");
                                    mQbDialogWorkerThread.start();
                                    mQbDialogWorkerThread.prepareHandler();
                                    mQbDialogWorkerThread.postTask(task);
                                }
                            }
                        }
                    } catch (Exception e) {
                        AppLog.e(TAG, e.getMessage(), e);
                    }
                }

                @Override
                public void onFailure(Call<ServerResponse<List<OfflineDao>>> call, Throwable t) {
                    try {
                        AppLog.e(TAG, "Offline data onFailure.");
                        AppLog.e(TAG, t.getMessage());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

    /**
     *
     */
    private void doInBackground(List<OfflineDao> params) {
        AppLog.e(TAG, "##### Offline data started #####");
        try {
            List<OfflineDao> list = params;
            if (Utils.isValidList(list)) {
                for (int i = 0; i < list.size(); i++) {
                    String groupId = list.get(i).getGroupID();
                    Operations.insertKittyRules(mContext, list.get(i).getKittyRules(), groupId);

                    if (Utils.isValidList(list.get(i).getSummaryMembers().getData())) {
                        OfflineSummeryMembers members = new OfflineSummeryMembers();
                        members.setData(list.get(i).getSummaryMembers().getData());
                        members.setKittynext(list.get(i).getSummaryMembers().getKittynext());
                        members.setCount(String.valueOf(list.get(i).getSummaryMembers().getCount()));
                        Operations.insertIntoSummary(mContext, members, groupId);
                    }
                    if (list.get(i).getVenueData() != null)
                        Operations.insertVenue(mContext, list.get(i).getVenueData(), false);

                    if (list.get(i).getDairyData() != null)
                        Operations.insertIntoDiary(mContext, list.get(i).getDairyData(), groupId);

                    if (Utils.isValidList(list.get(i).getPersonalNotes()))
                        Operations.insertPersonalNotes(mContext, list.get(i).getPersonalNotes(), false);

                    if (list.get(i).getGroupSetting() != null)
                        Operations.insertSetting(mContext, list.get(i).getGroupSetting());
                }
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        } finally {
            PreferanceUtils.setIsOfflineDataAvailable(true, mContext);
        }

        AppLog.e(TAG, "***** Offline data ends *****");
    }

    /**
     *
     */
    private class WorkerThread extends HandlerThread {
        private Handler mWorkerHandler;

        public WorkerThread(String name) {
            super(name);
        }

        public void postTask(Runnable task) {
            mWorkerHandler.post(task);
        }

        public void prepareHandler() {
            mWorkerHandler = new Handler(getLooper());
        }

    }

    /**
     *
     */
    Runnable task = new Runnable() {
        @Override
        public void run() {
            doInBackground(list);
        }
    };
}
