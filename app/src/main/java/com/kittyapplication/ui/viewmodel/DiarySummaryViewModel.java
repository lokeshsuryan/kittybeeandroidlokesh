package com.kittyapplication.ui.viewmodel;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.kittyapplication.R;
import com.kittyapplication.model.OfflineSummeryMembers;
import com.kittyapplication.model.ServerResponse;
import com.kittyapplication.model.SummaryListDao;
import com.kittyapplication.providers.KittyBeeContract;
import com.kittyapplication.rest.Singleton;
import com.kittyapplication.sqlitedb.SQLConstants;
import com.kittyapplication.ui.activity.DiarySummaryActivity;
import com.kittyapplication.utils.AlertDialogUtils;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.Utils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Dhaval Riontech on 15/8/16.
 */
public class DiarySummaryViewModel {
    private static final String TAG = DiarySummaryViewModel.class.getSimpleName();
    private final DiarySummaryActivity mActivity;
    private final String mGroupId;
    private boolean flag = false;
    public int mCount = 0;

    public DiarySummaryViewModel(DiarySummaryActivity activity, String groupId) {
        mActivity = activity;
        mGroupId = groupId;
        initRequest(mGroupId);
    }

    private void initRequest(String groupId) {
        mActivity.showProgressDialog();
        new SyncTask().execute(groupId);
    }

    /**
     *
     */
    Callback<ServerResponse<List<SummaryListDao>>> getHostListCallback = new Callback<ServerResponse<List<SummaryListDao>>>() {
        @Override
        public void onResponse(Call<ServerResponse<List<SummaryListDao>>> call, Response<ServerResponse<List<SummaryListDao>>> response) {
            mActivity.hideProgressDialog();
            if (response.code() == 200) {
                ServerResponse<List<SummaryListDao>> serverResponse = response.body();
                if (serverResponse.getData() != null && !serverResponse.getData().isEmpty()) {
                    OfflineSummeryMembers members = new OfflineSummeryMembers();
                    members.setData(serverResponse.getData());
                    members.setKittynext(serverResponse.getKittyNext());
                    members.setCount(String.valueOf(serverResponse.getCount()));
                    insertIntoSummary(members);
                    mCount = serverResponse.getCount();
                    mActivity.setHostList(members);
                } else {
//                    AlertDialogUtils.showServerError(mActivity.getResources().getString(R.string.zero_host_found), mActivity);
                }
            } else {
                AlertDialogUtils.showServerError(mActivity.getResources()
                        .getString(R.string.server_error), mActivity);
            }
        }

        @Override
        public void onFailure(Call<ServerResponse<List<SummaryListDao>>> call, Throwable t) {
            mActivity.hideProgressDialog();
            AlertDialogUtils.showServerError(mActivity.getResources().getString(R.string.server_error), mActivity);
        }
    };

    /**
     * @param diaryResponseDao
     */
    private void insertIntoSummary(OfflineSummeryMembers diaryResponseDao) {
        ContentValues values = new ContentValues();
        values.put(SQLConstants.KEY_ID, mGroupId);
        values.put(SQLConstants.KEY_DATA, new Gson().toJson(diaryResponseDao));
        mActivity.getContentResolver().insert(KittyBeeContract.Summary.CONTENT_URI, values);
    }


    /**
     *
     */
    private class SyncTask extends AsyncTask<String, Void, OfflineSummeryMembers> {
        @Override
        protected OfflineSummeryMembers doInBackground(String... params) {
//            List<SummaryListDao> summaryList = new ArrayList<>();
            OfflineSummeryMembers members = new OfflineSummeryMembers();
            Uri uri = ContentUris.withAppendedId(KittyBeeContract.Summary.CONTENT_URI,
                    Long.valueOf(params[0]));
            ContentResolver resolver =
                    mActivity.getContentResolver();
            Cursor cursor = resolver.query(
                    uri,                      // the URI to query
                    KittyBeeContract.Groups.PROJECTION_ALL,   // the projection to use
                    null,                           // the where clause without the WHERE keyword
                    null,                           // any wildcard substitutions
                    null);                          // the sort order without the SORT BY keyword

            if (cursor != null && cursor.getCount() != -1 && cursor.getCount() > 0) {
                // show data from db
//                AppLog.d(TAG, cursor.getString(1));
//                AppLog.d(TAG, cursor.getString(0));
//                AppLog.d(TAG, cursor.getString(2));

                /*Type listType = new TypeToken<List<SummaryListDao>>() {
                }.getType();*/
                while (cursor.moveToNext()) {
                    Gson gson = new Gson();
                    members = gson.fromJson(cursor.getString(1), OfflineSummeryMembers.class);
//                    summaryList = gson.fromJson(cursor.getString(1), listType);
                }
                cursor.close();
            }
            return members;
        }

        @Override
        protected void onPostExecute(OfflineSummeryMembers members) {
            super.onPostExecute(members);
            try {
                AppLog.d(TAG, new Gson().toJson(members));
                if (members != null && Utils.isValidList(members.getData())) {
                    mActivity.setHostList(members);
                    flag = true;
                    mActivity.hideProgressDialog();
                }

                if (Utils.checkInternetConnection(mActivity)) {
                    Call<ServerResponse<List<SummaryListDao>>> call = Singleton.getInstance().
                            getRestOkClient().hostList(mGroupId);
                    call.enqueue(getHostListCallback);
                } else {
                    if (!flag) {
                        mActivity.hideProgressDialog();
                        AlertDialogUtils.showServerError(mActivity.getResources().
                                getString(R.string.no_internet_available), mActivity);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public int getCount() {
        return mCount;
    }
}
