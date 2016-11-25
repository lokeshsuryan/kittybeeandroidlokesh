package com.kittyapplication.ui.viewmodel;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;

import com.google.gson.Gson;
import com.kittyapplication.AppApplication;
import com.kittyapplication.R;
import com.kittyapplication.listener.DataInsertedListener;
import com.kittyapplication.model.DiaryHostSelectionDao;
import com.kittyapplication.model.OfflineDao;
import com.kittyapplication.model.ServerResponse;
import com.kittyapplication.model.SummaryListDao;
import com.kittyapplication.rest.Singleton;
import com.kittyapplication.sqlitedb.SqlDataSetTask;
import com.kittyapplication.ui.activity.KittyDiaryActivity;
import com.kittyapplication.ui.activity.SelectDairyHostManuallyActivity;
import com.kittyapplication.utils.AlertDialogUtils;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.PreferanceUtils;
import com.kittyapplication.utils.Utils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Dhaval Riontech on 16/8/16.
 */
public class SelectDairyHostManuallyViewModel {

    private static final String TAG = SelectDairyHostManuallyViewModel.class.getSimpleName();
    private SelectDairyHostManuallyActivity mActivity;
    private String mGroupId;
    private String mNextKittyDate;

    public SelectDairyHostManuallyViewModel(SelectDairyHostManuallyActivity activity, String groupId) {
        mActivity = activity;
        mGroupId = groupId;
        getApiCall(mGroupId);
    }

    public void onSwipeRefreshReloadData(final SwipeRefreshLayout refreshLayout) {
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (refreshLayout.isRefreshing())
                    refreshLayout.setRefreshing(false);
            }
        });
    }

    private void getApiCall(String groupId) {
        if (Utils.checkInternetConnection(mActivity)) {
            mActivity.showProgressDialog();

            Call<ServerResponse<List<SummaryListDao>>> call = Singleton.getInstance().getRestOkClient().hostList(groupId);
            call.enqueue(getHostListCallback);
        } else {
            AlertDialogUtils.showServerError(mActivity.getResources().getString(R.string.no_internet_available), mActivity);
        }
    }

    Callback<ServerResponse<List<SummaryListDao>>> getHostListCallback = new Callback<ServerResponse<List<SummaryListDao>>>() {
        @Override
        public void onResponse(Call<ServerResponse<List<SummaryListDao>>> call, Response<ServerResponse<List<SummaryListDao>>> response) {
            mActivity.hideProgressDialog();
            if (response.code() == 200) {
                ServerResponse<List<SummaryListDao>> serverResponse = response.body();
                if (serverResponse.getData() != null && !serverResponse.getData().isEmpty()) {
                    setmNextKittyDate(serverResponse.getKittyNext());
                    mActivity.setHostList(serverResponse.getData());
                    mActivity.findViewById(R.id.txtParticipant).setVisibility(View.GONE);
                } else {
                    AlertDialogUtils.showServerError(mActivity.getResources().getString(R.string.zero_host_found), mActivity);
                }
            } else {
                AlertDialogUtils.showServerError(mActivity.getResources().getString(R.string.server_error), mActivity);
            }
        }

        @Override
        public void onFailure(Call<ServerResponse<List<SummaryListDao>>> call, Throwable t) {
            mActivity.hideProgressDialog();
        }
    };

    public void submitApiCall(List<SummaryListDao> memberList, String nextKittyDate, String kittyId, String noOfHost, String groupId) {
        if (Utils.checkInternetConnection(mActivity)) {
            AppApplication.getInstance().setRefresh(true);
            mActivity.showProgressDialog();

            DiaryHostSelectionDao req = new DiaryHostSelectionDao();

            req.setUserId(PreferanceUtils.getLoginUserObject(mActivity).getUserID());
            req.setKittyDate(nextKittyDate);
            req.setKittyId(kittyId);
            req.setNoOfHosts(noOfHost);
            req.setMember(memberList);
            req.setGroupId(groupId);

            Call<ServerResponse<OfflineDao>> call = Singleton.getInstance().getRestOkClient().selectHost(
                    groupId, req);
            call.enqueue(selectHostCallBack);
        } else {
            AlertDialogUtils.showServerError(mActivity.getResources().getString(R.string.no_internet_available), mActivity);
        }
    }

    Callback<ServerResponse<OfflineDao>> selectHostCallBack = new Callback<ServerResponse<OfflineDao>>() {
        @Override
        public void onResponse(Call<ServerResponse<OfflineDao>> call, Response<ServerResponse<OfflineDao>> response) {
            if (response.code() == 200) {
                if (response.body().getStatus().equalsIgnoreCase(AppConstant.SUCCESS)) {


                    final String msg = response.body().getMessage();
                    new SqlDataSetTask(mActivity, response.body().getData(),
                            new DataInsertedListener() {
                                @Override
                                public void insertedSuccess() {
                                    mActivity.hideProgressDialog();
                                    AlertDialogUtils.showServerError(msg, mActivity);
                                    redirectToDairy();
                                }
                            });
//                    Singleton.getInstance().getRestOkClient().
//                            getDairies(mGroupId).enqueue(getDairyDataCallBack);
                } else {
                    AppApplication.getInstance().setRefresh(false);
                    mActivity.hideProgressDialog();
                    AlertDialogUtils.showServerError(mActivity.getResources()
                            .getString(R.string.server_error), mActivity);
                }
            } else {
                AppApplication.getInstance().setRefresh(false);
                mActivity.hideProgressDialog();
                AlertDialogUtils.showServerError(mActivity.getResources()
                        .getString(R.string.server_error), mActivity);
            }
        }

        @Override
        public void onFailure(Call<ServerResponse<OfflineDao>> call, Throwable t) {
            AppApplication.getInstance().setRefresh(false);
            mActivity.hideProgressDialog();
            AlertDialogUtils.showServerError(mActivity.getResources().getString(R.string.server_error), mActivity);
        }
    };

    public String getmNextKittyDate() {
        return mNextKittyDate;
    }

    public void setmNextKittyDate(String mNextKittyDate) {
        this.mNextKittyDate = mNextKittyDate;
    }

    public void redirectToDairy() {
        Intent intent = new Intent(mActivity, KittyDiaryActivity.class);
        intent.putExtra(AppConstant.INTENT_DIARY_DATA,
                new Gson().toJson(AppApplication.getDairyData()).toString());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        mActivity.finish();
        mActivity.startActivity(intent);
    }
}