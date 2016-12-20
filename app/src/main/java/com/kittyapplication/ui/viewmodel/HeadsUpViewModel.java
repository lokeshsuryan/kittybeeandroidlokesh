package com.kittyapplication.ui.viewmodel;

import android.content.res.Resources;

import com.kittyapplication.R;
import com.kittyapplication.model.HedsUpData;
import com.kittyapplication.model.ServerResponse;
import com.kittyapplication.rest.Singleton;
import com.kittyapplication.ui.activity.HeadsUpActivity;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.Utils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Pintu Riontech on 8/9/16.
 * vaghela.pintu31@gmail.com
 */
public class HeadsUpViewModel {
    private static final String TAG = HeadsUpViewModel.class.getSimpleName();
    private HeadsUpActivity mActivity;

    public HeadsUpViewModel(HeadsUpActivity activity) {
        mActivity = activity;
        callAPI();
    }


    private void callAPI() {
        if (Utils.checkInternetConnection(mActivity)) {
            mActivity.showProgressDialog();
            Call<ServerResponse<List<HedsUpData>>> call =
                    Singleton.getInstance().getRestOkClient().
                            getHedsUpData();
            call.enqueue(headsUpCallback);

        } else {
            mActivity.showSnackbar(mActivity.getResources()
                    .getString(R.string.no_internet_available));
        }
    }


    private Callback<ServerResponse<List<HedsUpData>>> headsUpCallback = new Callback<ServerResponse<List<HedsUpData>>>() {
        @Override
        public void onResponse(Call<ServerResponse<List<HedsUpData>>> call, Response<ServerResponse<List<HedsUpData>>> response) {
            mActivity.hideProgressDialog();
            try {
                if (response.code() == 200) {
                    List<HedsUpData> data = response.body().getData();
                    if (response.body().getResponse() == AppConstant.RESPONSE_SUCCESS) {
                        if (data != null && !data.isEmpty()) {
                            mActivity.showLayout();
                            mActivity.getDataList(data);
                        } else {
                            mActivity.hideLayout();
                            if (Utils.isValidString(response.body().getMessage())) {
                                mActivity.showSnackbar(response.body().getMessage());
                            } else {
                                mActivity.showSnackbar(mActivity.getResources()
                                        .getString(R.string.server_error));
                            }
                        }
                    } else {
                        mActivity.hideLayout();
                        if (Utils.isValidString(response.body().getMessage())) {
                            mActivity.showSnackbar(response.body().getMessage());
                        } else {
                            mActivity.showSnackbar(mActivity.getResources()
                                    .getString(R.string.server_error));
                        }
                    }
                } else {
                    mActivity.hideLayout();
                    if (Utils.isValidString(response.body().getMessage())) {
                        mActivity.showSnackbar(response.body().getMessage());
                    } else {
                        mActivity.showSnackbar(mActivity.getResources()
                                .getString(R.string.server_error));
                    }
                    AppLog.e(TAG, "HTTP Status code is not 200");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(Call<ServerResponse<List<HedsUpData>>> call, Throwable t) {
            mActivity.hideProgressDialog();
            mActivity.hideLayout();
        }
    };
}
