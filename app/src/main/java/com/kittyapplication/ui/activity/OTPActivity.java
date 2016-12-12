package com.kittyapplication.ui.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kittyapplication.R;
import com.kittyapplication.chat.ui.activity.QBSignUpSingInActivity;
import com.kittyapplication.model.RegisterResponseDao;
import com.kittyapplication.services.IncomingSmsReceiver;
import com.kittyapplication.ui.view.SignUpView;
import com.kittyapplication.ui.viewmodel.OTPViewModel;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.ImageUtils;
import com.kittyapplication.utils.Utils;
import com.quickblox.users.model.QBUser;

/**
 * Created by Dhaval Riontech on 7/8/16.
 */
public class OTPActivity extends QBSignUpSingInActivity implements SignUpView, View.OnClickListener {
    private static final int REQUEST_PHONE_STATE_PERMISSION = 111;
    private OTPViewModel mViewModel;
    private EditText mEdtOtpNumber;
    private ProgressDialog mDialog;
    private String mMobileNumber;
    private static String TAG = OTPActivity.class.getSimpleName();
    private IncomingSmsReceiver mReceiver;
    private OtpReceiver otpReceiver;
    private ProgressBar mPbLoader;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Intent intent = getIntent();
        mMobileNumber = intent.getStringExtra(AppConstant.MOBILE);

        mViewModel = new OTPViewModel(OTPActivity.this);
        mEdtOtpNumber = (EditText) findViewById(R.id.edtOtpNumber);
        Utils.setImeiOption(mEdtOtpNumber, false, this);
        mEdtOtpNumber.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Utils.hideKeyboard(OTPActivity.this, v);
                    mViewModel.login(mEdtOtpNumber, mMobileNumber);
                    return true;
                }
                return false;
            }
        });
        findViewById(R.id.btnLogin).setOnClickListener(this);
        findViewById(R.id.txtResendOtp).setOnClickListener(this);

        ImageView imgbg = (ImageView) findViewById(R.id.imgSignUpBackGround);
        ImageUtils.getImageLoader(this).displayImage("drawable://" + R.drawable.login_screen_bg, imgbg);

        mPbLoader = (ProgressBar) findViewById(R.id.pbLoaderOTPActivity);
        if (checkSmsPermission()) {
            smsReceiverCall();
        }
    }


    @Override
    protected View getRootView() {
        return findViewById(R.id.rlLoginRootView);
    }

    @Override
    public void onSignIn(QBUser qbUser) {
        super.onSignIn(qbUser);
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }

    @Override
    public void onSignUp(QBUser qbUser) {
        super.onSignUp(qbUser);
    }

    @Override
    public void gotoNextPage(String message, String mobile) {
        Intent intent = new Intent(this, RegistrationActivity.class);
        intent.putExtra(AppConstant.MOBILE, mMobileNumber);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }

    @Override
    public void gotoHomePage(RegisterResponseDao dao) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnLogin:
                mViewModel.login(mEdtOtpNumber, mMobileNumber);
                break;
            case R.id.txtResendOtp:
                mViewModel.resend(mMobileNumber);
                break;
        }
    }

    public void showProgressDialog() {
       /* mDialog = new ProgressDialog(OTPActivity.this);
        mDialog.setMessage(getResources().getString(R.string.loading_text));
        mDialog.show();*/
        mPbLoader.setVisibility(View.VISIBLE);
    }

    public void hideProgressDialog() {
        /*try {
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss();
                mDialog = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        mPbLoader.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        otpReceiver = new OtpReceiver();
        registerReceiver(otpReceiver, new IntentFilter(AppConstant.OTP_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(otpReceiver);
    }

    private boolean checkSmsPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(OTPActivity.this,
                    Manifest.permission.RECEIVE_SMS)
                    != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(OTPActivity.this,
                    Manifest.permission.READ_SMS)
                    != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(OTPActivity.this,
                    Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(OTPActivity.this,
                    Manifest.permission.READ_PHONE_STATE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestSmsStatePermission();
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestSmsStatePermission() {
        requestPermissions(
                new String[]{Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.READ_SMS,
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.READ_PHONE_STATE},
                REQUEST_PHONE_STATE_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //resume tasks needing this permission
                if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    if (grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                        smsReceiverCall();
                        if (grantResults[3] == PackageManager.PERMISSION_DENIED) {
                            requestSmsStatePermission();
                        }
                    } else {
                        requestSmsStatePermission();
                    }
                } else {
                    requestSmsStatePermission();
                }
            } else {
                requestSmsStatePermission();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void smsReceiverCall() {
        IntentFilter mFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        mReceiver = new IncomingSmsReceiver();
        registerReceiver(mReceiver, mFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiver != null)
            unregisterReceiver(mReceiver);
    }

    private class OtpReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            AppLog.e(TAG, "onreceive");
            if (!Utils.isValidString(mEdtOtpNumber.getText().toString())) {
                String otp = intent.getStringExtra(AppConstant.OTP_ACTION);
                if (otp != null && otp.length() > 0) {
                    mEdtOtpNumber.setText(otp);
                    if (mEdtOtpNumber.getText() != null)
                        mViewModel.login(mEdtOtpNumber, mMobileNumber);
                }
            }
        }
    }


    public void enableDisableLoginButton(boolean enable) {
        findViewById(R.id.btnLogin).setEnabled(enable);
    }
}