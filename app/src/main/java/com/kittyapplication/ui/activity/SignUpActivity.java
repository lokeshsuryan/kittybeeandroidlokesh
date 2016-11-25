package com.kittyapplication.ui.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.kittyapplication.R;
import com.kittyapplication.model.RegisterResponseDao;
import com.kittyapplication.services.GCMRegistrationService;
import com.kittyapplication.ui.view.SignUpView;
import com.kittyapplication.ui.viewmodel.SignUpViewModel;
import com.kittyapplication.utils.AlertDialogUtils;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.ImageUtils;
import com.kittyapplication.utils.Utils;

/**
 * Created by Dhaval Riontech on 7/8/16.
 */
public class SignUpActivity extends AppCompatActivity implements View.OnClickListener, SignUpView {

    private static final String TAG = SignUpActivity.class.getSimpleName();
    private SignUpViewModel mViewModel;
    private EditText mEdtMobile;
    private ProgressDialog mDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        mViewModel = new SignUpViewModel(SignUpActivity.this);
        mEdtMobile = (EditText) findViewById(R.id.edtMobileNumber);
        Utils.setImeiOption(mEdtMobile, false, this);
        mEdtMobile.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Utils.hideKeyboard(SignUpActivity.this, v);
                    mViewModel.signup(mEdtMobile);
                    return true;
                }
                return false;
            }
        });

        ImageView imgbg = (ImageView) findViewById(R.id.imgSignUpBackGround);
        ImageUtils.getImageLoader(this).displayImage("drawable://" + R.drawable.login_screen_bg, imgbg);
        findViewById(R.id.btnSignup).setOnClickListener(this);
        registerGCM();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSignup:
                mViewModel.signup(mEdtMobile);
                break;
        }
    }

    @Override
    public void gotoNextPage(String message, String mobile) {
        AlertDialogUtils.showSnackToast(message, this);
        Intent intent = new Intent(SignUpActivity.this, OTPActivity.class);
        intent.putExtra(AppConstant.MOBILE, mobile);
        startActivity(intent);
        finish();
    }

    @Override
    public void gotoHomePage(RegisterResponseDao dao) {

    }

    public void showProgressDialog() {
        mDialog = new ProgressDialog(SignUpActivity.this);
        mDialog.setMessage(getResources().getString(R.string.loading_text));
        mDialog.show();
    }

    public void hideProgressDialog() {
        try {
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss();
                mDialog = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void registerGCM() {
        //Checking play service is available or not
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS != result) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        1).show();
            } else {
                AlertDialogUtils.showSnackToast(
                        "GCM ERROR FOUND"
                        , this);
            }
        } else {
            Intent intent = new Intent(this, GCMRegistrationService.class);
            startService(intent);
        }
    }
}
