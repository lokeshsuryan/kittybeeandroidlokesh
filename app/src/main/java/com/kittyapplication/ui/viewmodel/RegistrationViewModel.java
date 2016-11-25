package com.kittyapplication.ui.viewmodel;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;

import com.google.gson.Gson;
import com.kittyapplication.AppApplication;
import com.kittyapplication.R;
import com.kittyapplication.chat.utils.SharedPreferencesUtil;
import com.kittyapplication.custom.GoogleManager;
import com.kittyapplication.model.CityDao;
import com.kittyapplication.model.RegisterRequestDao;
import com.kittyapplication.model.RegisterResponseDao;
import com.kittyapplication.model.ServerResponse;
import com.kittyapplication.rest.Singleton;
import com.kittyapplication.ui.activity.RegistrationActivity;
import com.kittyapplication.utils.AlertDialogUtils;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.Utils;
import com.quickblox.users.model.QBUser;
import com.riontech.socialconnection.listeners.SocialConnectionListener;
import com.riontech.socialconnection.model.SocialUserResponse;
import com.riontech.socialconnection.social.FacebookManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Dhaval Soneji Riontech on 6/9/16.
 */
public class RegistrationViewModel extends QBUserViewModel {
    private static final String TAG = RegistrationViewModel.class.getSimpleName();
    private RegistrationActivity mActivity;
    public int mSocialType;
    public GoogleManager mGoogleManager;
    public FacebookManager mFaceBookManager;
    private String mMobileNumber;
    private RegisterResponseDao responseDao;
    private String filePath;

    public RegistrationViewModel(RegistrationActivity activity) {
        mActivity = activity;
        getCityDao();
    }

    /**
     * @param socialType
     * @param mobileNumber
     */
    public void socialLogin(int socialType, String mobileNumber) {
        if (Utils.checkInternetConnection(mActivity)) {
            mMobileNumber = mobileNumber;
            mSocialType = socialType;
            switch (socialType) {
                case AppConstant.SOCIAL_TYPE_FACEBOOK:
                    mFaceBookManager = new FacebookManager(mActivity, socialLoginCallBack);
                    break;
                case AppConstant.SOCIAL_TYPE_GOOGLE:
                    if (isAccountPermissionGranted()) {
                        mGoogleManager = new GoogleManager(mActivity, socialLoginCallBack);
                    }
                    break;
            }
        } else {
            AlertDialogUtils.showSnackToast(mActivity.getResources().getString(R.string.no_internet_connection), mActivity);
        }
    }

    private boolean isAccountPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (mActivity.checkSelfPermission(Manifest.permission.GET_ACCOUNTS)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(mActivity,
                        new String[]{Manifest.permission.GET_ACCOUNTS},
                        mActivity.GOOGLE_REQUEST_CODE);
                return false;
            }
        } else {
            return true;
        }
    }

    public void callGoogleManager() {
        mActivity.showProgressDialog();
        mGoogleManager = new GoogleManager(mActivity, socialLoginCallBack);
    }

    /**
     * Social login Callback
     */
    private SocialConnectionListener socialLoginCallBack = new SocialConnectionListener() {
        @Override
        public void onSuccess(SocialUserResponse response) {
            AppLog.e(TAG, "socialLoginCallBack: onSuccess: " + new Gson().toJson(response));
            response.setSocialType(mSocialType);

            mActivity.showProgressDialog();
            RegisterRequestDao obj = new RegisterRequestDao();
            obj.setDeviceID(AppApplication.getGCMId());
            obj.setDeviceType(mActivity.getResources().getString(R.string.device_type_android));
            obj.setEmail(response.getUserEmail());
            obj.setName(response.getUserName());
            obj.setPhone(mMobileNumber);
            obj.setProfilePic(response.getmImgUrl());
            obj.setGender("");

            mActivity.enableDisableRegisterButtons(false);
            if (mSocialType == AppConstant.SOCIAL_TYPE_FACEBOOK) {
                obj.setFbID(response.getUserId());
                AppLog.e(TAG, "Request(FB)\n" + new Gson().toJson(obj));
                Call<RegisterResponseDao> call = Singleton.getInstance().getRestOkClient().fbLogin(obj);
                call.enqueue(fbLoginCallback);
            } else if (mSocialType == AppConstant.SOCIAL_TYPE_GOOGLE) {
                obj.setGmailID(response.getUserId());
                AppLog.e(TAG, "Request(GOOGLE)\n" + new Gson().toJson(obj));
                Call<RegisterResponseDao> call = Singleton.getInstance().getRestOkClient().gmailLogin(obj);
                call.enqueue(gLoginCallback);
            }
        }

        @Override
        public void onFailure(Exception e) {
            AppLog.e(TAG, "socialLoginCallBack: onFailure");
            mActivity.hideProgressDialog();
        }
    };

    public void register(RegisterRequestDao requestDao) {
        if (Utils.checkInternetConnection(mActivity)) {
            mActivity.showProgressDialog();
            mActivity.enableDisableRegisterButtons(false);
            Call<RegisterResponseDao> call = Singleton.getInstance().getRestOkClient().register(requestDao);
            call.enqueue(registerCallback);
        } else {
            AlertDialogUtils.showSnackToast(mActivity.getResources().getString(R.string.no_internet_connection), mActivity);
        }
    }

    /**
     *
     */
    private Callback<RegisterResponseDao> registerCallback = new Callback<RegisterResponseDao>() {
        @Override
        public void onResponse(Call<RegisterResponseDao> call, Response<RegisterResponseDao> response) {
            if (response.code() == 200) {
                responseDao = response.body();
                if (responseDao.getResponse() == AppConstant.RESPONSE_SUCCESS) {
                    saveQBUser();
                    mActivity.gotoHomePage(responseDao);

                    /*if (responseDao.getQuickID() == null || responseDao.getQuickID().equals("")) {
                        mActivity.signIn(responseDao.getQuickLogin());
                    } else {
                        mActivity.gotoHomePage(responseDao);
                    }*/
                } else {
                    mActivity.enableDisableRegisterButtons(true);
                    if (responseDao.getMessage() != null && responseDao.getMessage().length() > 0) {
                        AlertDialogUtils.showSnackToast(responseDao.getMessage(), mActivity);
                    }
                    AppLog.e(TAG, "Response code = 0");
                }
            } else {
                mActivity.enableDisableRegisterButtons(true);
                AlertDialogUtils.showSnackToast(mActivity.getResources()
                        .getString(R.string.server_error), mActivity);
                AppLog.e(TAG, "HTTP Status code is not 200");
            }
        }

        @Override
        public void onFailure(Call<RegisterResponseDao> call, Throwable t) {
            mActivity.enableDisableRegisterButtons(true);
            mActivity.hideProgressDialog();
            AlertDialogUtils.showSnackToast(mActivity.getResources()
                    .getString(R.string.server_error), mActivity);
        }
    };

    /**
     *
     */
    private void signUpWithQb() {
        try {
            if (responseDao.getQuickID() == null || responseDao.getQuickID().equals("")) {
                QBUser user = new QBUser();
                user.setLogin(responseDao.getUserID());
                user.setPhone(responseDao.getPhone());
                String name = responseDao.getName() == null ? responseDao.getFullName() : responseDao.getName();
                user.setFullName(name);
                mActivity.signUp(user, filePath);
                mActivity.gotoHomePage(responseDao);
            } else {
                mActivity.gotoHomePage(responseDao);
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    private Callback<RegisterResponseDao> fbLoginCallback = new Callback<RegisterResponseDao>() {
        @Override
        public void onResponse(Call<RegisterResponseDao> call, Response<RegisterResponseDao> response) {
            if (response.code() == 200) {
                responseDao = response.body();
                if (responseDao.getResponse() == AppConstant.RESPONSE_SUCCESS) {
                    saveQBUser();
                    mActivity.gotoHomePage(responseDao);

                    /*signUpWithQb();
                    if (responseDao.getQuickID() == null || responseDao.getQuickID().equals(""))
                        mActivity.signUp(responseDao.getUserID(), responseDao.getPhone(), responseDao.getName());
                    else
                        mActivity.gotoHomePage(responseDao);*/
                } else {
                    mActivity.enableDisableRegisterButtons(true);
                    if (responseDao.getMessage() != null && responseDao.getMessage().length() > 0) {
                        AlertDialogUtils.showSnackToast(responseDao.getMessage(), mActivity);
                    }
                    AppLog.e(TAG, "Response code = 0");
                }
            } else {
                mActivity.enableDisableRegisterButtons(true);
                AlertDialogUtils.showSnackToast(mActivity.getResources()
                        .getString(R.string.server_error), mActivity);
                AppLog.e(TAG, "HTTP Status code is not 200");
            }
        }

        @Override
        public void onFailure(Call<RegisterResponseDao> call, Throwable t) {
            mActivity.hideProgressDialog();
            mActivity.enableDisableRegisterButtons(true);
            AlertDialogUtils.showSnackToast(mActivity.getResources()
                    .getString(R.string.server_error), mActivity);
        }
    };

    private Callback<RegisterResponseDao> gLoginCallback = new Callback<RegisterResponseDao>() {
        @Override
        public void onResponse(Call<RegisterResponseDao> call, Response<RegisterResponseDao> response) {
            if (response.code() == 200) {
                try {
                    responseDao = response.body();
                    if (responseDao.getResponse() == AppConstant.RESPONSE_SUCCESS) {
                        saveQBUser();
                        mActivity.gotoHomePage(responseDao);

                        /*signUpWithQb();
                        if (responseDao.getQuickID() == null || responseDao.getQuickID().equals(""))
                            mActivity.signUp(responseDao.getUserID(), responseDao.getPhone(), responseDao.getName());
                        else
                            mActivity.gotoHomePage(responseDao)*/
                        ;
                    } else {
                        mActivity.enableDisableRegisterButtons(true);
                        if (responseDao.getMessage() != null && responseDao.getMessage().length() > 0) {
                            AlertDialogUtils.showSnackToast(responseDao.getMessage(), mActivity);
                        }
                    }
                } catch (Exception e) {
                    mActivity.enableDisableRegisterButtons(true);
                    AppLog.e(TAG, e.getMessage(), e);
                }
            } else {
                mActivity.enableDisableRegisterButtons(true);
                AlertDialogUtils.showSnackToast(mActivity.getResources()
                        .getString(R.string.server_error), mActivity);
                AppLog.e(TAG, "HTTP Status code is not 200");
            }
        }

        @Override
        public void onFailure(Call<RegisterResponseDao> call, Throwable t) {
            mActivity.hideProgressDialog();
            mActivity.enableDisableRegisterButtons(true);
            AlertDialogUtils.showSnackToast(mActivity.getResources()
                    .getString(R.string.server_error), mActivity);
        }
    };

    @Override
    protected void onQBAdded() {
        mActivity.gotoHomePage(responseDao);
    }

    private void saveQBUser() {
        try {
            QBUser user = new QBUser();
            user.setId(Integer.parseInt(responseDao.getQuickID()));
            user.setLogin(responseDao.getQuickLogin());
            user.setPhone(responseDao.getPhone());
            if (Utils.isValidString(responseDao.getQuickID()))
                user.setId(Integer.parseInt(responseDao.getQuickID()));
            else
                user.setId(0);
            String name = responseDao.getName() == null ? responseDao.getFullName()
                    : responseDao.getName();
            user.setFullName(name);
            SharedPreferencesUtil.saveQbUser(user);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }

    }


    public void getCityDao() {
        if (Utils.checkInternetConnection(mActivity)) {
            mActivity.showProgressDialog();
            Call<ServerResponse<List<CityDao>>> call = Singleton.getInstance().getRestOkClient().getCity();
            call.enqueue(new Callback<ServerResponse<List<CityDao>>>() {
                @Override
                public void onResponse(Call<ServerResponse<List<CityDao>>> call, Response<ServerResponse<List<CityDao>>> response) {
                    mActivity.hideProgressDialog();
                    if (response.code() == 200) {
                        try {
                            if (response.body().getResponse() == AppConstant.RESPONSE_SUCCESS) {
                                if (Utils.isValidList(response.body().getData())) {
                                    mActivity.setCityAdapter(response.body().getData());
                                }
                            }
                        } catch (Exception e) {
                        }
                    }
                }

                @Override
                public void onFailure(Call<ServerResponse<List<CityDao>>> call, Throwable t) {
                    mActivity.hideProgressDialog();
                }
            });
        } else {
            AlertDialogUtils.showSnackToast(mActivity.getResources().getString(R.string.no_internet_available), mActivity);
        }
    }
}
