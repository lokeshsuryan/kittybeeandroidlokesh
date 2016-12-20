package com.kittyapplication.ui.viewmodel;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.EditText;

import com.kittyapplication.AppApplication;
import com.kittyapplication.R;
import com.kittyapplication.model.ServerRequest;
import com.kittyapplication.model.SignUpDao;
import com.kittyapplication.rest.Singleton;
import com.kittyapplication.ui.activity.SignUpActivity;
import com.kittyapplication.utils.AlertDialogUtils;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.PreferanceUtils;
import com.kittyapplication.utils.Utils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Dhaval Riontech on 8/8/16.
 */
public class SignUpViewModel implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String TAG = SignUpActivity.class.getSimpleName();
    private SignUpActivity mActivity;
    private ProgressDialog mDialog;
    private final int REQUEST_PHONE_STATE_PERMISSION = 100;

    public SignUpViewModel(SignUpActivity activity) {
        mActivity = activity;
//        checkPermission();
    }

    public void signup(EditText editText) {
        if (Utils.isValidMobileNumber(editText)) {
//                if (mDeviceID != "") {
            mActivity.showProgressDialog();

            ServerRequest obj = new ServerRequest();
            obj.setMobile(editText.getText().toString().trim());
            AppLog.d(TAG, "--" + PreferanceUtils.getRegGCMID(mActivity) + "--");
//            obj.setDeviceID(PreferanceUtils.getRegGCMID(mActivity));
            obj.setDeviceID(AppApplication.getInstance().getGCMId());


            Call<SignUpDao> call = Singleton.getInstance().getRestOkClient().login(obj);
            call.enqueue(signupCallback);
//                }else{
//                    checkPermission();
//                }
        }
    }

    private Callback<SignUpDao> signupCallback = new Callback<SignUpDao>() {
        @Override
        public void onResponse(Call<SignUpDao> call, Response<SignUpDao> response) {
            mActivity.hideProgressDialog();
            if (response.code() == 200) {
                SignUpDao object = response.body();
                if (object.getResponse() == AppConstant.RESPONSE_SUCCESS) {
                    if (object.getMessage() != null && object.getMessage().length() > 0) {
                        mActivity.gotoNextPage(object.getMessage(), object.getMobile());
                    }
                } else {
                    if (object.getMessage() != null && object.getMessage().length() > 0) {
                        AlertDialogUtils.showSnackToast(object.getMessage(), mActivity);
                    }
                    AppLog.e(TAG, "Response code = 0");
                }
            } else {

                System.out.println("Response code => " + response.code());
                AlertDialogUtils.showSnackToast(mActivity.getResources()
                        .getString(R.string.server_error), mActivity);
                AppLog.e(TAG, "HTTP Status code is not 200");
            }
        }

        @Override
        public void onFailure(Call<SignUpDao> call, Throwable t) {
            mActivity.hideProgressDialog();
            System.out.println("error code => " + t.getMessage());
            AlertDialogUtils.showSnackToast(mActivity.getResources()
                    .getString(R.string.server_error), mActivity);
        }
    };

    private boolean checkPermission() {
        boolean result = false;
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(mActivity,
                    Manifest.permission.READ_PHONE_STATE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPhoneStatePermission();
                return false;
            } else {
                result = true;
            }
        } else {
            result = true;
        }
        return result;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        AppLog.d(getClass().getSimpleName(), "onRequestPermissionsResult  " + requestCode);
        switch (requestCode) {
            case REQUEST_PHONE_STATE_PERMISSION:
                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                AppLog.d(getClass().getSimpleName(), "PERMISSION GRANTED BY USER... GO AHEAD..");
//                mDeviceID = SocialConnectionUtils.getIMEI(mActivity);
//                } else {
//                    AppLog.d(getClass().getSimpleName(), "PERMISSION NOT GRANTED... BACK TO APP..");
//                }
                break;
        }
    }

    /**
     * Requests the {@link Manifest.permission#CAMERA} permission.
     * If an additional rationale should be displayed, the user has to launch the request from
     * a SnackBar that includes additional information.
     */
    private void requestPhoneStatePermission() {
        // Permission has not been granted and must be requested.
        AppLog.d(getClass().getSimpleName(), "requestPhoneStatePermission  ");
        AppLog.d(getClass().getSimpleName(), "requestPhoneStatePermission  >>>>>>");
        ActivityCompat.requestPermissions(mActivity,
                new String[]{Manifest.permission.READ_PHONE_STATE},
                REQUEST_PHONE_STATE_PERMISSION);

    }
}
