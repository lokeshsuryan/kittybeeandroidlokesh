package com.kittyapplication.ui.viewmodel;

import android.content.Intent;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.kittyapplication.AppApplication;
import com.kittyapplication.R;
import com.kittyapplication.chat.utils.SharedPreferencesUtil;
import com.kittyapplication.model.OTPResponseDao;
import com.kittyapplication.model.RegisterResponseDao;
import com.kittyapplication.model.ServerRequest;
import com.kittyapplication.model.SignUpDao;
import com.kittyapplication.rest.Singleton;
import com.kittyapplication.ui.activity.HomeActivity;
import com.kittyapplication.ui.activity.OTPActivity;
import com.kittyapplication.utils.AlertDialogUtils;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.PreferanceUtils;
import com.kittyapplication.utils.Utils;
import com.quickblox.users.model.QBUser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Dhaval Riontech on 8/8/16.
 */
public class OTPViewModel extends QBUserViewModel {
    private static final String TAG = OTPViewModel.class.getSimpleName();
    private OTPActivity mActivity;
    private TextView mTxtSumbmit;

    public OTPViewModel(OTPActivity activity) {
        mActivity = activity;
    }

    public void login(EditText editText, String mobileNumber) {
        if (Utils.isValidOtpNumber(editText, mActivity.getResources().getString(R.string.lbl_otp))) {
            mActivity.showProgressDialog();
            mActivity.enableDisableLoginButton(false);

            ServerRequest obj = new ServerRequest();
            obj.setMobile(mobileNumber);
            obj.setOtp(editText.getText().toString().trim());

            if (Utils.isValidString(AppApplication.getGCMId())) {
                PreferanceUtils.setDeviceID(mActivity, true);
            } else {
                PreferanceUtils.setDeviceID(mActivity, false);
            }
            obj.setDeviceID(AppApplication.getGCMId());

            Call<OTPResponseDao> call = Singleton.getInstance().getRestOkClient().otp(obj);
            call.enqueue(loginCallback);
        }
    }

    private Callback<OTPResponseDao> loginCallback = new Callback<OTPResponseDao>() {
        @Override
        public void onResponse(Call<OTPResponseDao> call, Response<OTPResponseDao> response) {
            if (response.code() == 200) {
                OTPResponseDao object = response.body();
                if (object.getResponse() == AppConstant.RESPONSE_SUCCESS) {
                    if (object.getUserID().equalsIgnoreCase("")) {
                        mActivity.gotoNextPage(object.getMessage(), "");
                    } else {
                        RegisterResponseDao responseDao = new RegisterResponseDao();
                        responseDao.setUserID(object.getUserID());
                        responseDao.setProfilePic(object.getProfilePic());
                        responseDao.setName(object.getName());
                        responseDao.setFullName(object.getFullname());
                        responseDao.setQuickID(object.getQbId());
                        responseDao.setQuickLogin(object.getQbLogin());
                        responseDao.setPhone(object.getPhone());
//                        LoginUserPrefHolder.getInstance().saveUser(responseDao);
                        PreferanceUtils.setLoginUserObject(mActivity, new Gson().toJson(responseDao));
                        PreferanceUtils.setIsRegistered(mActivity, true);

                        saveQBUser(object);

                        if (object.getQbId() != null && !object.getQbId().equalsIgnoreCase("")) {
                            mActivity.signIn(object.getQbLogin());
                        } else {
                            mActivity.gotoHomePage(responseDao);
                        }

                        /*if (object.getQbId() == null || object.getQbId().equalsIgnoreCase("")) {
                            QBUser user = new QBUser();
                            user.setLogin(object.getUserID());
                            user.setPhone(object.getPhone());
                            user.setFullName(object.getName() == null ? object.getFullname() : object.getName());
                            mActivity.signUp(user, object.getProfilePic());
                        } else {
                            mActivity.signIn(object.getQbLogin());
                        }*/
                    }
                } else {
                    mActivity.hideProgressDialog();
//                    if (object.getMessage() != null && object.getMessage().length() > 0) {
//                        AlertDialogUtils.showSnackToast(object.getMessage(), mActivity);
//                    }
                    mActivity.enableDisableLoginButton(true);
                    AlertDialogUtils.showSnackToast(
                            mActivity.getResources().getString(R.string.enter_valid_otp_sent_by_us_new), mActivity);
                }
            } else {
                mActivity.enableDisableLoginButton(true);
                mActivity.hideProgressDialog();
                AlertDialogUtils.showSnackToast(mActivity.getResources()
                        .getString(R.string.server_error), mActivity);
            }
        }

        @Override
        public void onFailure(Call<OTPResponseDao> call, Throwable t) {
            mActivity.hideProgressDialog();
            mActivity.enableDisableLoginButton(true);
            AlertDialogUtils.showSnackToast(mActivity.getResources()
                    .getString(R.string.server_error), mActivity);
        }
    };

    public void resend(String mobileNumber) {
        mActivity.showProgressDialog();

        ServerRequest obj = new ServerRequest();
        obj.setMobile(mobileNumber);
        obj.setDeviceID(AppApplication.getGCMId());
        Call<SignUpDao> call = Singleton.getInstance().getRestOkClient().login(obj);
        call.enqueue(resendOTPCallback);
    }

    private Callback<SignUpDao> resendOTPCallback = new Callback<SignUpDao>() {
        @Override
        public void onResponse(Call<SignUpDao> call, Response<SignUpDao> response) {
            mActivity.hideProgressDialog();
            if (response.code() == 200) {
                SignUpDao object = response.body();
                if (object.getResponse() == AppConstant.RESPONSE_SUCCESS) {
                    if (object.getMessage() != null && object.getMessage().length() > 0) {
                        AlertDialogUtils.showSnackToast(object.getMessage(), mActivity);
                    }
                } else {
                    if (object.getMessage() != null && object.getMessage().length() > 0) {
                        AlertDialogUtils.showSnackToast(object.getMessage(), mActivity);
                    }
                    AppLog.e(TAG, "Response code = 0");
                }
            } else {
                AlertDialogUtils.showSnackToast(mActivity.getResources()
                        .getString(R.string.server_error), mActivity);
                AppLog.e(TAG, "HTTP Status code is not 200");
            }
        }

        @Override
        public void onFailure(Call<SignUpDao> call, Throwable t) {
            mActivity.hideProgressDialog();
            AlertDialogUtils.showSnackToast(mActivity.getResources()
                    .getString(R.string.server_error), mActivity);
        }
    };

    @Override
    protected void onQBAdded() {
        mActivity.hideProgressDialog();
        mActivity.startActivity(new Intent(mActivity, HomeActivity.class));
        mActivity.finish();
    }

    /**
     * @param responseDao
     */
    private void saveQBUser(OTPResponseDao responseDao) {
        try {
            QBUser user = new QBUser();
            user.setId(Integer.parseInt(responseDao.getQbId()));
            user.setLogin(responseDao.getQbLogin());
            user.setPhone(responseDao.getPhone());
            if (Utils.isValidString(responseDao.getQbId()))
                user.setId(Integer.parseInt(responseDao.getQbId()));
            else
                user.setId(0);
            String name = responseDao.getName() == null ? responseDao.getFullname()
                    : responseDao.getName();
            user.setFullName(name);
            SharedPreferencesUtil.saveQbUser(user);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }

    }
}
