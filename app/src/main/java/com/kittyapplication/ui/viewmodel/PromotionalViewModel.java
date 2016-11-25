package com.kittyapplication.ui.viewmodel;

import android.app.ProgressDialog;

import com.google.gson.Gson;
import com.kittyapplication.R;
import com.kittyapplication.model.PromotionalDao;
import com.kittyapplication.model.ServerResponse;
import com.kittyapplication.rest.Singleton;
import com.kittyapplication.ui.activity.PromotionalActivity;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.Utils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Pintu Riontech on 8/8/16.
 * vaghela.pintu31@gmail.com
 */
public class PromotionalViewModel {
    private static final String TAG = PromotionalViewModel.class.getSimpleName();
    private PromotionalActivity mActivity;
    private ProgressDialog mDialog;

    public PromotionalViewModel(PromotionalActivity activity) {
        mActivity = activity;
    }


    public void initRequest(int pos) {
        if (Utils.checkInternetConnection(mActivity)) {
            showDialog();
            String id;
            if (pos < 5) {
                id = String.valueOf(pos + 1);
            } else {
                id = mActivity.getResources().getString(R.string.special);
            }
            Call<ServerResponse<List<PromotionalDao>>> call =
                    Singleton.getInstance().getRestOkClient().getPromotionalData(id);
            call.enqueue(getPromotionalDataCallBack);
        } else {
            mActivity.showSnackbar(mActivity.getResources().getString(R.string.no_internet_available));
        }
    }

    private void showDialog() {
//        mDialog = new ProgressDialog(mActivity);
//        mDialog.setMessage(mActivity.getResources().getString(R.string.loading_text));
//        mDialog.show();
        mActivity.showProgressDialog();
    }

    private void hideDialog() {
//        try {
//            if (mDialog != null && mDialog.isShowing()) {
//                mDialog.dismiss();
//                mDialog = null;
//            }
//        } catch (Exception e) {
//            AppLog.e(TAG, e.getMessage(), e);
//        }
        mActivity.hideProgressDialog();
    }

    private Callback<ServerResponse<List<PromotionalDao>>> getPromotionalDataCallBack = new Callback<ServerResponse<List<PromotionalDao>>>() {
        @Override
        public void onResponse(Call<ServerResponse<List<PromotionalDao>>> call, Response<ServerResponse<List<PromotionalDao>>> response) {
            hideDialog();
            if (response.code() == 200) {
                List<PromotionalDao> data = response.body().getData();
                if (response.body().getResponse() == AppConstant.RESPONSE_SUCCESS) {
                    if (data != null && !data.isEmpty()) {
                        hideDialog();
                        AppLog.d(TAG, "List Data" + new Gson().toJson(data).toString());
                        mActivity.getDataList(data);
                    } else {
                        if (Utils.isValidString(response.body().getMessage())) {
                            mActivity.showSnackbar(response.body().getMessage());
                        } else {
                            mActivity.showSnackbar(mActivity.getResources().getString(R.string.server_error));
                        }
                    }
                } else {
                    if (Utils.isValidString(response.body().getMessage())) {
                        mActivity.showSnackbar(response.body().getMessage());
                    } else {
                        mActivity.showSnackbar(mActivity.getResources().getString(R.string.server_error));
                    }
                    mActivity.finish();
                    AppLog.e(TAG, "Response code = 0");
                }
            } else {
                if (Utils.isValidString(response.body().getMessage())) {
                    mActivity.showSnackbar(response.body().getMessage());
                } else {
                    mActivity.showSnackbar(mActivity.getResources().getString(R.string.server_error));
                }
                AppLog.e(TAG, "HTTP Status code is not 200");
            }
        }

        @Override
        public void onFailure(Call<ServerResponse<List<PromotionalDao>>> call, Throwable t) {
            hideDialog();
            mActivity.showSnackbar(mActivity.getResources().getString(R.string.server_error));
        }
    };

}
