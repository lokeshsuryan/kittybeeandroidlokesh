package com.kittyapplication.ui.viewmodel;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import com.google.gson.Gson;
import com.kittyapplication.R;
import com.kittyapplication.model.ChatData;
import com.kittyapplication.model.DiaryResponseDao;
import com.kittyapplication.model.KittiesDao;
import com.kittyapplication.model.ServerResponse;
import com.kittyapplication.providers.KittyBeeContract;
import com.kittyapplication.rest.Singleton;
import com.kittyapplication.sqlitedb.Operations;
import com.kittyapplication.sqlitedb.SQLConstants;
import com.kittyapplication.ui.activity.DiarySummaryActivity;
import com.kittyapplication.ui.activity.KittyDiaryActivity;
import com.kittyapplication.utils.AlertDialogUtils;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.PreferanceUtils;
import com.kittyapplication.utils.Utils;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by HalfBloodPrince(RIONTECH)
 * Riontech on 18/10/16.
 */

public class KittyDiaryActivityViewModel {
    private static final String TAG = KittyDiaryActivityViewModel.class.getSimpleName();
    private final ChatData mChatData;
    private final KittyDiaryActivity mActivity;
    private boolean flag;
    private Call<ServerResponse<DiaryResponseDao>> mDiaryCall;

    public KittyDiaryActivityViewModel(KittyDiaryActivity activity, ChatData data) {
        mActivity = activity;
        mChatData = data;
        if (!PreferanceUtils.getIsOfflineDataAvailable(mActivity)) {
            callAPI();
        } else {
            initRequest();
        }
    }

    public void callAPI() {
        if (Utils.checkInternetConnection(mActivity)) {
            mDiaryCall = Singleton.getInstance().
                    getRestOkClient().getDairies(mChatData.getGroupID());
            mDiaryCall.enqueue(getDairyDataCallBack);
        } else {
            mActivity.hideProgressBar();
            mActivity.showSnackbar(mActivity.getResources().
                    getString(R.string.no_internet_available));
        }
    }


    public void actionSummeryData() {
        try {
            if (mChatData != null && Utils.isValidString(mChatData.getGroupID())) {
                Intent intent = new Intent(mActivity, DiarySummaryActivity.class);
                intent.putExtra(AppConstant.GROUP_ID,
                        mChatData.getGroupID());
                mActivity.startActivity(intent);
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }


    /**
     * Diary Response Callback
     */
    public Callback<ServerResponse<DiaryResponseDao>> getDairyDataCallBack = new Callback<ServerResponse<DiaryResponseDao>>() {
        @Override
        public void onResponse(Call<ServerResponse<DiaryResponseDao>> call, Response<ServerResponse<DiaryResponseDao>> response) {
            if (response.code() == 200) {
                ServerResponse<DiaryResponseDao> serverResponse = response.body();
                if (serverResponse.getData() != null) {
                    // Insert data into local database
                    Operations.insertIntoDiary(mActivity, serverResponse.getData(), mChatData.getGroupID());

                    // TODO reverse kitty
                    List<KittiesDao> reverseKittyList = serverResponse.getData().getKitties();
                    Collections.reverse(reverseKittyList);
                    serverResponse.getData().setKitties(reverseKittyList);

                    mActivity.setUpViewPager(serverResponse.getData(), false);
                } else {
                    mActivity.hideLayout();
                    AlertDialogUtils.showServerError(mActivity.getResources().getString(R.string.zero_member_found), mActivity);
                }
            } else {
                mActivity.hideLayout();
                AlertDialogUtils.showServerError(mActivity.getResources()
                        .getString(R.string.server_error), mActivity);
            }
        }

        @Override
        public void onFailure(Call<ServerResponse<DiaryResponseDao>> call, Throwable t) {
            mActivity.hideLayout();
            mActivity.hideProgressBar();
            AlertDialogUtils.showServerError(mActivity.getResources().getString(R.string.server_error), mActivity);
        }
    };

    /**
     * @param diaryResponseDao
     */
    private void insertIntoDiary(DiaryResponseDao diaryResponseDao) {
        ContentValues values = new ContentValues();
        values.put(SQLConstants.KEY_ID, mChatData.getGroupID());
        values.put(SQLConstants.KEY_DATA, new Gson().toJson(diaryResponseDao));
        mActivity.getContentResolver().insert(KittyBeeContract.Diaries.CONTENT_URI, values);
    }


    /**
     *
     */
    private void doInBackground(String groupId) {
        Uri uri = ContentUris.withAppendedId(KittyBeeContract.Diaries.CONTENT_URI,
                Long.valueOf(groupId));
        ContentResolver resolver = mActivity.getContentResolver();
        MyQueryHandler queryHandler = new MyQueryHandler(resolver);
        queryHandler.startQuery(0, null, uri, null, null, null, null);
    }

    /**
     *
     */
    private void initRequest() {
        if (Utils.isValidString(mChatData.getGroupID())) {
            doInBackground(mChatData.getGroupID());
        } else {
            AppLog.d(TAG, "group id is missing");
        }
    }

    /**
     *
     */
    private class MyQueryHandler extends AsyncQueryHandler {

        public MyQueryHandler(ContentResolver cr) {
            super(cr);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            super.onQueryComplete(token, cookie, cursor);
            DiaryResponseDao diaryResponseDao = null;
            if (cursor != null) {
                flag = true;
                // show data from db
                Gson gson = new Gson();
                while (cursor.moveToNext()) {
                    diaryResponseDao = gson.fromJson(cursor.getString(1), DiaryResponseDao.class);
                }

                cursor.close();
            }

            if (diaryResponseDao != null) {
                List<KittiesDao> reverseKittyList = diaryResponseDao.getKitties();
                Collections.reverse(reverseKittyList);
                diaryResponseDao.setKitties(reverseKittyList);
                mActivity.setUpViewPager(diaryResponseDao, true);
            } else {
                if (Utils.checkInternetConnection(mActivity)) {
                    callAPI();
                } else {
                    mActivity.hideLayout();
                }
            }

        }
    }

    /**
     * cancel api call if activity has been destroy
     */
    public void destroyApiCall() {
        if (mDiaryCall != null && mDiaryCall.isExecuted())
            mDiaryCall.cancel();
    }
}