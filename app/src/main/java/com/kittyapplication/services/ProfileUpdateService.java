package com.kittyapplication.services;

import android.app.IntentService;
import android.content.Intent;
import android.widget.Toast;

import com.google.gson.Gson;
import com.kittyapplication.R;
import com.kittyapplication.model.MyProfileRequestDao;
import com.kittyapplication.model.MyProfileResponseDao;
import com.kittyapplication.model.RegisterResponseDao;
import com.kittyapplication.model.ServerResponse;
import com.kittyapplication.rest.Singleton;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.LoginUserPrefHolder;
import com.kittyapplication.utils.PreferanceUtils;
import com.kittyapplication.utils.Utils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Dhaval Riontech on 19/8/16.
 */
public class ProfileUpdateService extends IntentService {
    private static final String TAG = ProfileUpdateService.class.getSimpleName();
    private int counter = 0;

    public ProfileUpdateService() {
        super(ProfileUpdateService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        AppLog.d(TAG, "onHandleIntent");
        callProfileUpdateApi();
    }

    private void callProfileUpdateApi() {
        if (Utils.checkInternetConnection(getApplicationContext())) {
//            MyProfileRequestDao requestDao = (MyProfileRequestDao) intent.getExtras().getSerializable(AppConstant.PROFILE_UPDATE);
            MyProfileRequestDao requestDao = Singleton.getInstance().getProfileRequestDao();
            Call<ServerResponse<MyProfileResponseDao>> call = Singleton.getInstance().getRestOkClient().editProfile(
                    PreferanceUtils.getLoginUserObject(this).getUserID(), requestDao);
            call.enqueue(updateMyProfileCallBack);
        } else {
            AppLog.e(TAG, getResources().getString(R.string.no_internet_available));
        }
    }

    private Callback<ServerResponse<MyProfileResponseDao>> updateMyProfileCallBack = new Callback<ServerResponse<MyProfileResponseDao>>() {
        @Override
        public void onResponse(Call<ServerResponse<MyProfileResponseDao>> call, Response<ServerResponse<MyProfileResponseDao>> response) {
            if (response.code() == 200) {
                ServerResponse<MyProfileResponseDao> object = response.body();
                if (object.getResponse() == AppConstant.RESPONSE_SUCCESS) {
                    if (object.getMessage() != null && object.getMessage().length() > 0) {

                        RegisterResponseDao responseDao = PreferanceUtils.getLoginUserObject(ProfileUpdateService.this);
                        responseDao.setProfilePic(object.getData().getProfilePic());
                        responseDao.setName(object.getData().getName());
                        responseDao.setPhone(object.getData().getPhone());
                        responseDao.setStatus(object.getData().getStatus());
//                        LoginUserPrefHolder.getInstance().saveUser(responseDao);
                        PreferanceUtils.setLoginUserObject(ProfileUpdateService.this, new Gson().toJson(responseDao));
                        PreferanceUtils.setIsRegistered(ProfileUpdateService.this, true);

//                        setData(PreferanceUtils.getLoginUserObject(mActivity));
                    }
                } else {
                    showSnackbar(object.getMessage());
                    AppLog.e(TAG, "Response code = 0");
                }

            } else {
                showSnackbar(getResources()
                        .getString(R.string.server_error));
                AppLog.e(TAG, "HTTP Status code is not 200");
            }
        }

        @Override
        public void onFailure(Call<ServerResponse<MyProfileResponseDao>> call, Throwable t) {
            AppLog.d(TAG, "Retry Count = " + counter);
            if (counter >= 3) {
                showSnackbar(getResources()
                        .getString(R.string.server_error));
                AppLog.e(TAG, "," + t.getMessage());
                stopSelf();
            } else {
                callProfileUpdateApi();
            }
            counter++;
        }
    };

    public void showSnackbar(String str) {
        Toast.makeText(ProfileUpdateService.this, str, Toast.LENGTH_SHORT).show();
    }
}
