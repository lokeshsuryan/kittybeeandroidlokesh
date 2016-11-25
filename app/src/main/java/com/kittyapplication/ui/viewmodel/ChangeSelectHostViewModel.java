package com.kittyapplication.ui.viewmodel;

import android.content.Intent;

import com.google.gson.Gson;
import com.kittyapplication.AppApplication;
import com.kittyapplication.R;
import com.kittyapplication.adapter.ChangeHostAdapter;
import com.kittyapplication.model.ChangeHostDao;
import com.kittyapplication.model.OfflineDao;
import com.kittyapplication.model.ServerResponse;
import com.kittyapplication.rest.Singleton;
import com.kittyapplication.sqlitedb.SqlDataSetTask;
import com.kittyapplication.ui.activity.SelectChangeHostActivity;
import com.kittyapplication.ui.activity.SettingActivity;
import com.kittyapplication.utils.AlertDialogUtils;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.Utils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by HalfBloodPrince(RIONTECH)
 * Riontech on 19/9/16.
 */
public class ChangeSelectHostViewModel {
    private static final String TAG = ChangeSelectHostViewModel.class.getSimpleName();
    private SelectChangeHostActivity mActivity;
    private ChangeHostDao mChangeHostDao;
    private String mGroupId;

    public ChangeSelectHostViewModel(SelectChangeHostActivity activity) {
        mActivity = activity;
    }

    public void getData(String participantData, String id) {
        if (Utils.isValidString(participantData)) {
            mGroupId = id;
            AppLog.d(TAG, "Group Id " + mGroupId);
            mChangeHostDao = new Gson().fromJson(participantData, ChangeHostDao.class);
            mActivity.setListData(mChangeHostDao.getNewHostList(), mChangeHostDao.getCurrentHostList().size());
        } else {
            mActivity.showSnackbar(mActivity.getResources().getString(R.string.server_error));
            mActivity.hideLayout();
        }
    }

    public void sumbitData(ChangeHostAdapter adapter) {
        if (Utils.isValidList(adapter.getSelectedList())
                && adapter.getSelectedList().size() > 0) {

            if (Utils.checkInternetConnection(mActivity)) {
                AppApplication.getInstance().setRefresh(true);
                mActivity.showProgressDialog();
                mChangeHostDao.getNewHostList().clear();
                mChangeHostDao.setNewHostList(adapter.getSelectedList());
                AppLog.d(TAG, "GroupId =" + mGroupId);

                Call<ServerResponse<OfflineDao>> call = Singleton.getInstance()
                        .getRestOkClient().changeHost(
                                mGroupId, mChangeHostDao);

                call.enqueue(changeHostCallback);

            } else {
                mActivity.showSnackbar(mActivity.getResources().getString(R.string.no_internet_available));
            }
        } else {
            mActivity.showSnackbar(mActivity.getResources().getString(R.string.change_host_no_member));
        }
    }

    private Callback<ServerResponse<OfflineDao>> changeHostCallback =
            new Callback<ServerResponse<OfflineDao>>() {
                @Override
                public void onResponse(Call<ServerResponse<OfflineDao>> call,
                                       Response<ServerResponse<OfflineDao>> response) {
                    mActivity.hideProgressDialog();
                    if (response.code() == 200) {
                        if (response.body().getResponse() == AppConstant.RESPONSE_SUCCESS) {
                            showServerError(response.body().getMessage());
                            // insert data into db
                            new SqlDataSetTask(mActivity, response.body().getData());
                            mActivity.startActivity(new Intent(mActivity, SettingActivity.class));
                            mActivity.finish();
                        } else {
                            AppApplication.getInstance().setRefresh(false);
                            showServerError(response.body().getMessage());
                        }
                    } else {
                        AppApplication.getInstance().setRefresh(false);
                        showServerError(response.body().getMessage());
                    }
                }

                @Override
                public void onFailure(Call<ServerResponse<OfflineDao>> call, Throwable t) {
                    AppApplication.getInstance().setRefresh(false);
                    mActivity.hideProgressDialog();
                    AlertDialogUtils.showSnackToast(mActivity.getResources()
                            .getString(R.string.server_error), mActivity);
                }
            };

    private void showServerError(String error) {
        if (Utils.isValidString(error)) {
            mActivity.showSnackbar(error);
        } else {
            mActivity.showSnackbar(mActivity.getResources().getString(R.string.server_error));
        }
    }
}