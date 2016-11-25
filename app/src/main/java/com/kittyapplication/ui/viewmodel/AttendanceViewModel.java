package com.kittyapplication.ui.viewmodel;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kittyapplication.R;
import com.kittyapplication.model.AttendanceDataDao;
import com.kittyapplication.model.NotificationDao;
import com.kittyapplication.model.ServerResponse;
import com.kittyapplication.providers.KittyBeeContract;
import com.kittyapplication.rest.Singleton;
import com.kittyapplication.sqlitedb.SQLConstants;
import com.kittyapplication.ui.activity.AttendanceActivity;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.Utils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Dhaval Riontech on 11/8/16.
 */
public class AttendanceViewModel {
    private static final String TAG = AttendanceViewModel.class.getSimpleName();
    private AttendanceActivity mActivity;
    private NotificationDao mNotificationDao;
    boolean flag = false;

    public AttendanceViewModel(AttendanceActivity activity, NotificationDao dao) {
        mActivity = activity;
        mNotificationDao = dao;
        initRequest();
    }

    private void initRequest() {
        mActivity.showProgressDialog();
        new SyncTask().execute();
    }

    /**
     *
     */
    private Callback<ServerResponse<List<AttendanceDataDao>>> getAttendanceDataCallBack = new Callback<ServerResponse<List<AttendanceDataDao>>>() {
        @Override
        public void onResponse(Call<ServerResponse<List<AttendanceDataDao>>> call, Response<ServerResponse<List<AttendanceDataDao>>> response) {
            mActivity.hideProgressDialog();
            if (response.code() == 200) {
                ServerResponse<List<AttendanceDataDao>> serverResponse = response.body();
                if (serverResponse.getData() != null && !serverResponse.getData().isEmpty()) {
                    insertAttendance(serverResponse.getData());
                    mActivity.setAttendanceList(serverResponse.getData());
                } else {
                    mActivity.showSnackbar(mActivity.getResources().getString(R.string.empty_attendance));
                }
            } else {
                mActivity.showSnackbar(mActivity.getResources()
                        .getString(R.string.server_error));
            }
        }

        @Override
        public void onFailure(Call<ServerResponse<List<AttendanceDataDao>>> call, Throwable t) {
            mActivity.hideProgressDialog();
            mActivity.showSnackbar(mActivity.getResources()
                    .getString(R.string.server_error));
            AppLog.e(TAG, "-" + t.getMessage());
        }
    };

    public void onSwipeRefreshReloadData(final SwipeRefreshLayout refreshLayout) {
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (refreshLayout.isRefreshing())
                    refreshLayout.setRefreshing(false);
            }
        });
    }

    /**
     * @param list
     */
    private void insertAttendance(List<AttendanceDataDao> list) {
        ContentValues values = new ContentValues();
        values.put(SQLConstants.KEY_GROUP_ID, mNotificationDao.getGroupId());
        values.put(SQLConstants.KEY_KITTY_ID, mNotificationDao.getKittyId());
        values.put(SQLConstants.KEY_DATA, new Gson().toJson(list));
        mActivity.getContentResolver().insert(KittyBeeContract.Attendance.CONTENT_URI, values);
    }


    /**
     *
     */
    private class SyncTask extends AsyncTask<Void, Void, List<AttendanceDataDao>> {

        @Override
        protected List<AttendanceDataDao> doInBackground(Void... params) {
            List<AttendanceDataDao> attendanceList = new ArrayList<>();
            String selection = SQLConstants.KEY_GROUP_ID + "=? AND " +
                    SQLConstants.KEY_KITTY_ID + "=?";
            String[] selectionArgs = {mNotificationDao.getGroupId(),
                    mNotificationDao.getKittyId()};

            ContentResolver resolver =
                    mActivity.getContentResolver();
            Cursor cursor = resolver.query(
                    KittyBeeContract.Attendance.CONTENT_URI,                      // the URI to query
                    KittyBeeContract.Attendance.PROJECTION_ALL,   // the projection to use
                    selection,                           // the where clause without the WHERE keyword
                    selectionArgs,                           // any wildcard substitutions
                    null);                          // the sort order without the SORT BY keyword

            if (cursor != null && cursor.getCount() > 0) {
                // show data from db
                Gson gson = new Gson();
                Type listType = new TypeToken<List<AttendanceDataDao>>() {
                }.getType();
                while (cursor.moveToNext()) {
                    attendanceList = gson.fromJson(cursor.getString(
                            cursor.getColumnIndex(SQLConstants.KEY_DATA)), listType);
                }
                cursor.close();
            }
            return attendanceList;
        }

        @Override
        protected void onPostExecute(List<AttendanceDataDao> aVoid) {
            super.onPostExecute(aVoid);
            if (Utils.isValidList(aVoid)) {
                mActivity.setAttendanceList(aVoid);
                mActivity.hideProgressDialog();
                flag = true;
            }

            if (Utils.checkInternetConnection(mActivity)) {
                Call<ServerResponse<List<AttendanceDataDao>>> call =
                        Singleton.getInstance().getRestOkClient()
                                .getAttendance(mNotificationDao);
                call.enqueue(getAttendanceDataCallBack);
            } else {
                if (!flag) {
                    mActivity.hideProgressDialog();
                    mActivity.showSnackbar(mActivity.getResources().
                            getString(R.string.no_internet_available));
                }
            }

        }
    }
}
