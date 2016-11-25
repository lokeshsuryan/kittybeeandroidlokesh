package com.kittyapplication.ui.viewmodel;

import android.content.res.Resources;
import android.widget.ImageView;

import com.kittyapplication.R;
import com.kittyapplication.model.NotificationDao;
import com.kittyapplication.model.ServerResponse;
import com.kittyapplication.rest.Singleton;
import com.kittyapplication.ui.activity.NotificationActivity;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.PreferanceUtils;
import com.kittyapplication.utils.Utils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Dhaval Riontech on 10/8/16.
 */
public class NotificationViewModel {
    private static final String TAG = NotificationViewModel.class.getSimpleName();
    private NotificationActivity mActivity;

    public NotificationViewModel(NotificationActivity activity) {
        mActivity = activity;
        initRequest();
    }

    private void initRequest() {
        if (Utils.checkInternetConnection(mActivity)) {
            mActivity.showProgressDialog();
            Call<ServerResponse<List<NotificationDao>>> call = Singleton.getInstance().getRestOkClient().getNotification(PreferanceUtils.getLoginUserObject(mActivity).getUserID());
            call.enqueue(getNotificationDataCallBack);
        } else {
            mActivity.showSnackbar(mActivity.getResources().getString(R.string.no_internet_available));
        }
    }

    private Callback<ServerResponse<List<NotificationDao>>> getNotificationDataCallBack = new Callback<ServerResponse<List<NotificationDao>>>() {
        @Override
        public void onResponse(Call<ServerResponse<List<NotificationDao>>> call, Response<ServerResponse<List<NotificationDao>>> response) {
            try {
                mActivity.hideProgressDialog();

                if (response.code() == 200) {
                    ServerResponse<List<NotificationDao>> serverResponse = response.body();

                    if (serverResponse.getData() != null && !serverResponse.getData().isEmpty()) {

                        mActivity.setNotificationList(serverResponse.getData());

                    } else {
                        mActivity.showEmptyLayout(mActivity.getResources().getString(R.string.empty_notification_list));
                    }
                } else {
                    mActivity.showSnackbar(mActivity.getResources()
                            .getString(R.string.server_error));
                    AppLog.e(TAG, String.format(mActivity.getResources().getString(R.string.response_code_not_200),
                            "Response Code", response.code()));
                }
            } catch (Resources.NotFoundException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(Call<ServerResponse<List<NotificationDao>>> call, Throwable t) {
            mActivity.hideProgressDialog();
            mActivity.showSnackbar(mActivity.getResources()
                    .getString(R.string.server_error));
            AppLog.e(TAG, "-" + t.getMessage());
        }
    };

    public void deleteNotification(int position, String notiId) {
        NotificationDao request = new NotificationDao();
        request.setUserId(PreferanceUtils.getLoginUserObject(mActivity).getUserID());
        request.setId(notiId);
        Call<ServerResponse<List<NotificationDao>>> call = Singleton.getInstance().getRestOkClient().deleteNotification(request);
        call.enqueue(deleteNotificationCallBack);
    }

    public void deleteAllNotification() {
        NotificationDao request = new NotificationDao();
        request.setUserId(PreferanceUtils.getLoginUserObject(mActivity).getUserID());
        mActivity.showProgressDialog();
        Call<ServerResponse<List<NotificationDao>>> call = Singleton.getInstance().getRestOkClient().deleteNotification(request);
        call.enqueue(deleteAllNotificationCallBack);
    }

    private Callback<ServerResponse<List<NotificationDao>>> deleteNotificationCallBack = new Callback<ServerResponse<List<NotificationDao>>>() {
        @Override
        public void onResponse(Call<ServerResponse<List<NotificationDao>>> call, Response<ServerResponse<List<NotificationDao>>> response) {
            try {
                mActivity.hideProgressDialog();

                if (response.code() == 200) {
                    ServerResponse<List<NotificationDao>> serverResponse = response.body();
                    if (serverResponse.getResponse() == AppConstant.RESPONSE_SUCCESS
                            && serverResponse.getStatus().equalsIgnoreCase(AppConstant.SUCCESS)) {
                        if (serverResponse.getData() != null && !serverResponse.getData().isEmpty()) {
                            mActivity.showSnackbar(serverResponse.getMessage());
                        } else {
                            mActivity.showEmptyLayout(mActivity.getResources().getString(R.string.empty_notification_list));
                        }

                    } else {
                        mActivity.showSnackbar(serverResponse.getMessage());
                        AppLog.e(TAG, "Response code = 0");
                    }
                } else {
                    mActivity.showSnackbar(mActivity.getResources()
                            .getString(R.string.server_error));
                    AppLog.e(TAG, String.format(mActivity.getResources().getString(R.string.response_code_not_200),
                            "Response Code", response.code()));
                }
            } catch (Resources.NotFoundException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(Call<ServerResponse<List<NotificationDao>>> call, Throwable t) {
            mActivity.hideProgressDialog();
            mActivity.showSnackbar(mActivity.getResources()
                    .getString(R.string.server_error));
            AppLog.e(TAG, "-" + t.getMessage());
        }
    };

    private Callback<ServerResponse<List<NotificationDao>>> deleteAllNotificationCallBack = new Callback<ServerResponse<List<NotificationDao>>>() {
        @Override
        public void onResponse(Call<ServerResponse<List<NotificationDao>>> call, Response<ServerResponse<List<NotificationDao>>> response) {
            try {
                mActivity.hideProgressDialog();

                if (response.code() == 200) {
                    ServerResponse<List<NotificationDao>> serverResponse = response.body();
                    if (serverResponse.getResponse() == AppConstant.RESPONSE_SUCCESS
                            && serverResponse.getStatus().equalsIgnoreCase(AppConstant.SUCCESS)) {

                        mActivity.showEmptyLayout(mActivity.getResources().getString(R.string.empty_notification_list));
                        mActivity.showSnackbar(serverResponse.getMessage());

                    } else {
                        mActivity.showSnackbar(serverResponse.getMessage());
                        AppLog.e(TAG, "Response code = 0");
                    }
                } else {
                    mActivity.showSnackbar(mActivity.getResources()
                            .getString(R.string.server_error));
                    AppLog.e(TAG, String.format(mActivity.getResources().getString(R.string.response_code_not_200),
                            "Response Code", response.code()));
                }
            } catch (Resources.NotFoundException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(Call<ServerResponse<List<NotificationDao>>> call, Throwable t) {
            mActivity.hideProgressDialog();
            mActivity.showSnackbar(mActivity.getResources()
                    .getString(R.string.server_error));
            AppLog.e(TAG, "-" + t.getMessage());
        }
    };

    public void setBanner(ImageView img) {
        Utils.setBannerItem(mActivity,
                mActivity.getResources().getStringArray(R.array.adv_banner_name)[0],
                img);
    }
}
