package com.kittyapplication.ui.viewmodel;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.kittyapplication.AppApplication;
import com.kittyapplication.R;
import com.kittyapplication.listener.DataInsertedListener;
import com.kittyapplication.model.DiaryHostSelectionDao;
import com.kittyapplication.model.DiaryResponseDao;
import com.kittyapplication.model.OfflineDao;
import com.kittyapplication.model.ServerResponse;
import com.kittyapplication.model.SummaryListDao;
import com.kittyapplication.rest.Singleton;
import com.kittyapplication.sqlitedb.Operations;
import com.kittyapplication.sqlitedb.SqlDataSetTask;
import com.kittyapplication.ui.activity.KittyDiaryActivity;
import com.kittyapplication.ui.activity.SelectDairyHostActivity;
import com.kittyapplication.utils.AlertDialogUtils;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.PreferanceUtils;
import com.kittyapplication.utils.Utils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Dhaval Riontech on 16/8/16.
 */
public class SelectDairyHostViewModel {
    private static final String TAG = SelectDairyHostViewModel.class.getSimpleName();
    private SelectDairyHostActivity mActivity;
    private String mGroupId;
    private String mNextKittyDate;
    private List<SummaryListDao> mRandomHostedMember;

    public SelectDairyHostViewModel(SelectDairyHostActivity activity, String groupId) {
        mActivity = activity;
        mGroupId = groupId;
        initRequest(mGroupId);
    }

    private void initRequest(String groupId) {
        if (Utils.checkInternetConnection(mActivity)) {
            mActivity.showProgressDialog();
            mActivity.enableDisableButtons(false);
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
            mActivity.enableDisableButtons(true);
            if (response.code() == 200) {
                ServerResponse<List<SummaryListDao>> serverResponse = response.body();
                if (serverResponse.getData() != null && !serverResponse.getData().isEmpty()) {
                    mNextKittyDate = serverResponse.getKittyNext();
                    mActivity.setHostList(serverResponse.getData());
                    ((TextView) mActivity.findViewById(R.id.txtParticipant)).setText("Participants: " + serverResponse.getData().size());
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
            mActivity.enableDisableButtons(true);
            AlertDialogUtils.showServerError(mActivity.getResources().getString(R.string.server_error), mActivity);
            AppLog.e(TAG, "," + t.getMessage());
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

    public String getmNextKittyDate() {
        return mNextKittyDate;
    }

    public void setmNextKittyDate(String mNextKittyDate) {
        this.mNextKittyDate = mNextKittyDate;
    }

    public void setSelectHost(List<SummaryListDao> dao, String nextKittyDate, String kittyId, String noOfHost, String groupId) {
        AppApplication.getInstance().setRefresh(true);
        mActivity.showProgressDialog();
        mActivity.enableDisableButtons(false);

        DiaryHostSelectionDao req = new DiaryHostSelectionDao();
        List<SummaryListDao> memberList = dao;
        req.setUserId(PreferanceUtils.getLoginUserObject(mActivity).getUserID());
        req.setKittyDate(nextKittyDate);
        req.setKittyId(kittyId);
        req.setNoOfHosts(noOfHost);
        req.setMember(memberList);
        req.setGroupId(groupId);

        Call<ServerResponse<OfflineDao>> call = Singleton.getInstance().getRestOkClient().selectHost(groupId, req);
        call.enqueue(selectHostCallBack);
    }

    Callback<ServerResponse<OfflineDao>> selectHostCallBack = new Callback<ServerResponse<OfflineDao>>() {
        @Override
        public void onResponse(Call<ServerResponse<OfflineDao>> call, Response<ServerResponse<OfflineDao>> response) {
            mActivity.enableDisableButtons(true);
            if (response.code() == 200) {
                if (response.body().getStatus().equalsIgnoreCase(AppConstant.SUCCESS)) {

//                    redirectToDairy();

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

                } else {
                    AppApplication.getInstance().setRefresh(false);
                    mActivity.hideProgressDialog();
                    AlertDialogUtils.showServerError(mActivity.getResources().getString(R.string.server_error), mActivity);
                }
            } else {
                AppApplication.getInstance().setRefresh(false);
                mActivity.hideProgressDialog();
                AlertDialogUtils.showServerError(mActivity.getResources().getString(R.string.server_error), mActivity);
            }
        }

        @Override
        public void onFailure(Call<ServerResponse<OfflineDao>> call, Throwable t) {
            AppApplication.getInstance().setRefresh(false);
            mActivity.hideProgressDialog();
            mActivity.enableDisableButtons(true);
            AlertDialogUtils.showServerError(mActivity.getResources().getString(R.string.server_error), mActivity);
        }
    };

    public void setmRandomHostedMember(List<SummaryListDao> mRandomHostedMember) {
        this.mRandomHostedMember = mRandomHostedMember;
    }

    public List<SummaryListDao> getmRandomHostedMember() {
        return mRandomHostedMember;
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


    /**
     * Diary Response Callback
     */
    public Callback<ServerResponse<DiaryResponseDao>> getDairyDataCallBack = new Callback<ServerResponse<DiaryResponseDao>>() {
        @Override
        public void onResponse(Call<ServerResponse<DiaryResponseDao>> call, Response<ServerResponse<DiaryResponseDao>> response) {
            mActivity.hideProgressDialog();
            if (response.code() == 200) {
                ServerResponse<DiaryResponseDao> serverResponse = response.body();
                if (serverResponse.getData() != null) {
                    // Insert data into local database
                    Operations.insertIntoDiary(mActivity, serverResponse.getData(), mGroupId);
                    redirectToDairy();
                }
            }
        }

        @Override
        public void onFailure(Call<ServerResponse<DiaryResponseDao>> call, Throwable t) {
            mActivity.hideProgressDialog();
        }
    };
}
