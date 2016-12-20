package com.kittyapplication.ui.viewmodel;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.kittyapplication.AppApplication;
import com.kittyapplication.R;
import com.kittyapplication.listener.DataInsertedListener;
import com.kittyapplication.model.ChatData;
import com.kittyapplication.model.ServerResponse;
import com.kittyapplication.model.VenueResponseDao;
import com.kittyapplication.providers.KittyBeeContract;
import com.kittyapplication.rest.Singleton;
import com.kittyapplication.sqlitedb.SQLConstants;
import com.kittyapplication.sqlitedb.SqlDataSetTask;
import com.kittyapplication.ui.activity.VenueActivity;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.PreferanceUtils;
import com.kittyapplication.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Pintu Riontech on 9/8/16.
 * vaghela.pintu31@gmail.com
 */
public class SetVenueViewModel {
    private static final String TAG = SetVenueViewModel.class.getSimpleName();
    private VenueActivity mActivity;
    public boolean isVenueEdit = false;
    private boolean flag = false;
    private String mKiityId;

    public SetVenueViewModel(VenueActivity activity, String kittyId) {
        mActivity = activity;
        mKiityId = kittyId;
    }

    public void init() {
        if (!PreferanceUtils.getIsOfflineDataAvailable(mActivity)) {
            callAPI();
        } else {
            initRequest(mKiityId);
        }
    }

    public void callAPI() {
        if (Utils.checkInternetConnection(mActivity)) {
            mActivity.showProgressDialog();
            Call<ServerResponse<List<VenueResponseDao>>> call =
                    Singleton.getInstance().getRestOkClient().getVenue(mKiityId);
            call.enqueue(getVenueCallBack);
        } else {
            mActivity.hideProgressDialog();
            mActivity.showSnackbar(mActivity.getResources().
                    getString(R.string.no_internet_available));
        }
    }

    private void initRequest(String kittyId) {
        new SyncTask().execute(kittyId);
    }

    private Callback<ServerResponse<List<VenueResponseDao>>> getVenueCallBack
            = new Callback<ServerResponse<List<VenueResponseDao>>>() {
        @Override
        public void onResponse(Call<ServerResponse<List<VenueResponseDao>>> call,
                               Response<ServerResponse<List<VenueResponseDao>>> response) {
            mActivity.hideProgressDialog();

            if (response.code() == 200) {
                if (response.body().getStatus().equals(AppConstant.SUCCESS)) {
                    ServerResponse<List<VenueResponseDao>> serverResponse = response.body();
                    if (serverResponse.getData() != null && !serverResponse.getData().isEmpty()) {
                        insertVenue(serverResponse.getData().get(0), false);
                        isVenueEdit = true;
                        mActivity.setVenue(serverResponse.getData().get(0), false);
                    }
                }
            } else {
                mActivity.showSnackbar(mActivity.getResources()
                        .getString(R.string.server_error));
                AppLog.e(TAG, "HTTP Status code is not 200");
            }
        }

        @Override
        public void onFailure
                (Call<ServerResponse<List<VenueResponseDao>>> call, Throwable t) {
            mActivity.hideProgressDialog();
            mActivity.showSnackbar(mActivity.getResources()
                    .getString(R.string.server_error));
            AppLog.e(TAG, "," + t.getMessage());
        }
    };

    public void addVenue(VenueResponseDao dao) {
        if (Utils.checkInternetConnection(mActivity)) {
            mActivity.showProgressDialog();
            AppApplication.getInstance().setRefresh(true);
            if (!isVenueEdit) {
                Call<ServerResponse<List<VenueResponseDao>>> call
                        = Singleton.getInstance().getRestOkClient().addVenue(dao);
                call.enqueue(addVenueCallBack);
            } else {
                Call<ServerResponse<List<VenueResponseDao>>> call
                        = Singleton.getInstance().getRestOkClient().editVenue(dao.getId(),
                        dao);
                call.enqueue(addVenueCallBack);
            }
        } else {
            insertVenue(dao, true);
            mActivity.showSnackbar(mActivity.getResources().getString(R.string.venue_created_success_new));
//            mActivity.showSnackbar(mActivity.getResources().getString(R.string.no_internet_connection));
        }
    }

    private Callback<ServerResponse<List<VenueResponseDao>>> addVenueCallBack = new Callback<ServerResponse<List<VenueResponseDao>>>() {
        @Override
        public void onResponse(Call<ServerResponse<List<VenueResponseDao>>> call, Response<ServerResponse<List<VenueResponseDao>>> response) {
            mActivity.hideProgressDialog();

            if (response.code() == 200) {
                final ServerResponse<List<VenueResponseDao>> serverResponse = response.body();
                if (serverResponse.getStatus().equals(AppConstant.SUCCESS)) {
                    insertVenue(serverResponse.getData().get(0), false);
                    isVenueEdit = true;
                    // insert data into db
                    final VenueResponseDao dao = serverResponse.getData().get(0);
                    new SqlDataSetTask(mActivity, response.body().getAllData(),
                            new DataInsertedListener() {
                                @Override
                                public void insertedSuccess() {
                                    mActivity.setVenue(dao, false);
                                    mActivity.showSnackbar(mActivity.getResources()
                                            .getString(R.string.venue_created_success_new));
                                    mActivity.finish();
                                }
                            });
//                    mActivity.showSnackbar(serverResponse.getMessage());
                } else {
                    AppApplication.getInstance().setRefresh(false);
                    mActivity.showSnackbar(serverResponse.getMessage());
                }
            } else {
                AppApplication.getInstance().setRefresh(false);
                mActivity.showSnackbar(mActivity.getResources()
                        .getString(R.string.server_error));
                AppLog.e(TAG, "HTTP Status code is not 200");
            }
        }

        @Override
        public void onFailure(Call<ServerResponse<List<VenueResponseDao>>> call, Throwable t) {
            AppApplication.getInstance().setRefresh(false);
            mActivity.hideProgressDialog();
            mActivity.showSnackbar(mActivity.getResources()
                    .getString(R.string.server_error));
            AppLog.e(TAG, "," + t.getMessage());
        }
    };


    /**
     * Insert Venue
     */
    private void insertVenue(VenueResponseDao dao, boolean flag) {
        ContentValues values = new ContentValues();
        values.put(SQLConstants.KEY_ID, dao.getKittyId());
        values.put(SQLConstants.KEY_DATA, new Gson().toJson(dao));
        if (flag) {
            values.put(SQLConstants.KEY_IS_SYNC, 1);
        }
        mActivity.getContentResolver().insert(KittyBeeContract.Venue.CONTENT_URI, values);
    }


    /**
     *
     */
    private class SyncTask extends AsyncTask<String, Void, VenueResponseDao> {
        @Override
        protected VenueResponseDao doInBackground(String... params) {
            VenueResponseDao responseDao = null;
            Uri uri = ContentUris.withAppendedId(KittyBeeContract.Venue.CONTENT_URI,
                    Long.valueOf(params[0]));
            ContentResolver resolver = mActivity.getContentResolver();
            Cursor cursor = resolver.query(uri, null, null, null, null);

            if (cursor != null && cursor.getCount() > 0) {
                // show data from db
                Gson gson = new Gson();
                while (cursor.moveToNext()) {
                    responseDao = gson.fromJson(cursor.getString(1), VenueResponseDao.class);
                }

                flag = true;
                cursor.close();
            }
            return responseDao;
        }

        @Override
        protected void onPostExecute(VenueResponseDao aVoid) {
            super.onPostExecute(aVoid);
//            MySQLiteHelper.getInstance(mActivity).close();
            if (aVoid != null && Utils.isValidString(aVoid.getId())) {
                isVenueEdit = true;
                mActivity.setVenue(aVoid, true);
                mActivity.hideProgressDialog();
            } else {
                callAPI();
            }
        }

    }

    public String getHostedName(ChatData data) {
        List<String> nameList = new ArrayList<>();
        HashMap<String, String> hashMap = data.getHostName();
        if (hashMap != null) {
            for (Map.Entry m : hashMap.entrySet()) {
                //return number
                String number = (String) m.getKey();
                //return name
                String name = (String) m.getValue();
                //System.out.println(m.getKey() + " " + m.getValue());
                nameList.add(Utils.getNameForDiary(mActivity, number, name));
            }
        }
        if (Utils.isValidList(nameList)) {
            return TextUtils.join(mActivity.getResources().getString(R.string.comma), nameList);
        } else {
            return "";
        }
    }
}