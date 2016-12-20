package com.kittyapplication.ui.viewmodel;

import com.kittyapplication.R;
import com.kittyapplication.model.AddAttendanceDao;
import com.kittyapplication.model.ServerResponse;
import com.kittyapplication.rest.Singleton;
import com.kittyapplication.ui.activity.NotificationCardActivity;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.Utils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Dhaval Soneji : sonejidhavalm@gmail.com on 24/8/16.
 */
public class NotificationCardViewModel {
    private static final String TAG = NotificationCardViewModel.class.getSimpleName();
    private NotificationCardActivity mActivity;

    public NotificationCardViewModel(NotificationCardActivity activity) {
        mActivity = activity;
    }

    public void addAttendance(AddAttendanceDao dao) {
        if (Utils.checkInternetConnection(mActivity)) {
            mActivity.showProgressDialog();
            Call<ServerResponse> call = Singleton.getInstance().getRestOkClient().addAttendance(dao);
            call.enqueue(addAttendanceDataCallBack);
        } else {
            mActivity.showSnackbar(mActivity.getResources().getString(R.string.no_internet_available));
        }
    }

    Callback<ServerResponse> addAttendanceDataCallBack = new Callback<ServerResponse>() {
        @Override
        public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
            mActivity.hideProgressDialog();

            if (response.code() == 200) {
                ServerResponse serverResponse = response.body();
                if (serverResponse.getResponse() == AppConstant.RESPONSE_SUCCESS
                        && serverResponse.getMessage().equalsIgnoreCase(AppConstant.SUCCESS)) {

                    mActivity.showSnackbar(serverResponse.getMessage());

                } else {
                    mActivity.showSnackbar(serverResponse.getMessage());
                    AppLog.e(TAG, "Response code = 0");
                }

            } else {
                mActivity.showSnackbar(mActivity.getResources().getString(R.string.server_error));
                AppLog.e(TAG, "HTTP Status code is not 200");
            }
        }

        @Override
        public void onFailure(Call<ServerResponse> call, Throwable t) {
            mActivity.hideProgressDialog();
            mActivity.showSnackbar(mActivity.getResources().getString(R.string.server_error));
            AppLog.e(TAG, "-" + t.getMessage());
        }
    };
}
