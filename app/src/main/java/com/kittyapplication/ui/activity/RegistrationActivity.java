package com.kittyapplication.ui.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.kittyapplication.AppApplication;
import com.kittyapplication.R;
import com.kittyapplication.adapter.CityAutoComepleteAdapter;
import com.kittyapplication.chat.ui.activity.QBSignUpSingInActivity;
import com.kittyapplication.chat.utils.Consts;
import com.kittyapplication.chat.utils.SharedPreferencesUtil;
import com.kittyapplication.chat.utils.chat.ChatHelper;
import com.kittyapplication.chat.utils.qb.QbAuthUtils;
import com.kittyapplication.core.utils.SharedPrefsHelper;
import com.kittyapplication.core.utils.SpannableUtils;
import com.kittyapplication.custom.AutoCompleteTextView;
import com.kittyapplication.model.CityDao;
import com.kittyapplication.model.RegisterRequestDao;
import com.kittyapplication.model.RegisterResponseDao;
import com.kittyapplication.ui.view.SignUpView;
import com.kittyapplication.ui.viewmodel.RegistrationViewModel;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.ImageUtils;
import com.kittyapplication.utils.PreferanceUtils;
import com.kittyapplication.utils.Utils;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dhaval Riontech on 8/8/16.
 */
public class RegistrationActivity extends QBSignUpSingInActivity implements SignUpView, View.OnClickListener {
    private static final String TAG = RegistrationActivity.class.getSimpleName();
    public static final int GOOGLE_REQUEST_CODE = 1;
    private RegistrationViewModel mViewModel;
    private ProgressDialog mDialog;
    private String mMobileNumber;
    private EditText mEdtName, mEdtEmail, mEdtCity;
    private RadioGroup mRgGender;
    private TextView mTxtTermCondition;
    private ProgressBar mPbLoader;
    private AutoCompleteTextView mAetCity;
    private CityAutoComepleteAdapter mAdapter;
    private List<String> mCityList = new ArrayList<>();
    private String mSelectedCity = null;

    private static final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    public boolean isAppSessionActive;
    private int retry = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_new);
        Intent intent = getIntent();
        mMobileNumber = intent.getStringExtra(AppConstant.MOBILE);
        mPbLoader = (ProgressBar) findViewById(R.id.pbLoaderRegisterActivity);
        mViewModel = new RegistrationViewModel(RegistrationActivity.this);

        ImageView imgBackground = (ImageView) findViewById(R.id.imgRegisterBackGround);
        ImageUtils.getImageLoader(this).displayImage("drawable://" + R.drawable.login_screen_bg, imgBackground);

        String termCondition = getResources().getString(R.string.term_condition_new_register);
        String[] str1 = {getResources().getString(R.string.terms_of_use)
                , getResources().getString(R.string.privacy_data_policy)};
        mTxtTermCondition = (TextView) findViewById(R.id.txtTermCondition);

        SpannableUtils spannableUtils = new SpannableUtils(mTxtTermCondition);
        spannableUtils.setString(termCondition);
        spannableUtils.setIsBold(true);
        spannableUtils.setIsColored(true);
        spannableUtils.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
        spannableUtils.setHighlightColor(ContextCompat.getColor(this, R.color.colorAccent));
        spannableUtils.setSpannableArray(str1);
        spannableUtils.buildSpannable();

        findViewById(R.id.btnRegister).setOnClickListener(this);
        findViewById(R.id.btnFacebook).setOnClickListener(this);
        findViewById(R.id.btnGoogle).setOnClickListener(this);

        mEdtName = (EditText) findViewById(R.id.edtName);
        Utils.setImeiOption(mEdtName, true, this);
        mEdtEmail = (EditText) findViewById(R.id.edtEmail);
        Utils.setImeiOption(mEdtEmail, false, this);
        mRgGender = (RadioGroup) findViewById(R.id.rgGender);
        mAetCity = (AutoCompleteTextView) findViewById(R.id.edtCity);

//        Utils.setImeiOption(mEdtCity, false, this);
//        mEdtCity.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if (actionId == EditorInfo.IME_ACTION_DONE) {
//                    Utils.hideKeyboard(RegistrationActivity.this, v);
//                    submitRegisterForm();
//                    return true;
//                }
//                return false;
//            }
//        });

        mAetCity.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSelectedCity = mAdapter.getItem(position);
            }
        });

        mAetCity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mAdapter != null) {
                    mSelectedCity = null;
                    mAdapter.getFilter().filter(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAetCity.setText("");
        mEdtName.setText("");
        mEdtEmail.setText("");
    }

    @Override
    protected View getRootView() {
        return findViewById(R.id.rlRegRoot);
    }

    @Override
    public void gotoNextPage(String message, String mobile) {
    }

    @Override
    public void gotoHomePage(RegisterResponseDao dao) {
//        hideProgressDialog();
        PreferanceUtils.setIsRegistered(RegistrationActivity.this, true);
//        LoginUserPrefHolder.getInstance().saveUser(dao);
        PreferanceUtils.setLoginUserObject(RegistrationActivity.this, new Gson().toJson(dao).toString());

        //Start Offline Support Service
//        startService(new Intent(this, OfflineSupportIntentService.class));

        startActivity(new Intent(RegistrationActivity.this, HomeActivity.class));
        finish();
//        mainThreadHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                recreateChatSession();
//                isAppSessionActive = false;
//            }
//        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnRegister:
                submitRegisterForm();
                break;
            case R.id.btnFacebook:
                mViewModel.socialLogin(AppConstant.SOCIAL_TYPE_FACEBOOK, mMobileNumber);
                break;
            case R.id.btnGoogle:
                mViewModel.socialLogin(AppConstant.SOCIAL_TYPE_GOOGLE, mMobileNumber);
                break;
        }
    }

    /**
     *
     */
    private void submitRegisterForm() {
        if (validateRegisterForm()) {
            int radioButtonId = mRgGender.getCheckedRadioButtonId();
            RadioButton radioButton = (RadioButton) mRgGender.findViewById(radioButtonId);

            RegisterRequestDao requestDao = new RegisterRequestDao();
            requestDao.setName(mEdtName.getText().toString().trim());
            requestDao.setCity(mAetCity.getText().toString().trim());
            requestDao.setEmail(mEdtEmail.getText().toString().trim());
            requestDao.setGender(radioButton.getText().toString());
            requestDao.setPhone(mMobileNumber);
            requestDao.setDeviceID(AppApplication.getGCMId());
            requestDao.setDeviceType(getResources().getString(R.string.device_type_android));
            requestDao.setAddress("");
            requestDao.setDob("");
            requestDao.setDoa("");
            requestDao.setProfile("");
            requestDao.setFamily("");
            requestDao.setCouple("");

            mViewModel.register(requestDao);
        }
    }

    private boolean validateRegisterForm() {
        if (Utils.isValidText(mEdtName, "Name")) {
            if (Utils.isValidName(mEdtName, "Name")) {
                if (Utils.isValidEmail(mEdtEmail, "Email")) {
                    if (mSelectedCity != null) {
                        return true;
                    } else {
                        mAetCity.setError(getResources().getString(R.string.select_city));
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public void showProgressDialog() {
       /* mDialog = new ProgressDialog(RegistrationActivity.this);
        mDialog.setMessage(getResources().getString(R.string.loading_text));
        mDialog.show();*/
        mPbLoader.setVisibility(View.VISIBLE);
    }

    public void hideProgressDialog() {
        mPbLoader.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        AppLog.i(TAG, "requestCode: " + requestCode);
        if (mViewModel.mSocialType == 0) {
            AppLog.i(TAG, "facebook");
            mViewModel.mFaceBookManager.mCallbackManager.onActivityResult(requestCode, resultCode, data);
        } else if (mViewModel.mSocialType == 1) {
            if (mViewModel.mGoogleManager != null) {
                AppLog.i(TAG, "google");
                mViewModel.mGoogleManager.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public void onSignUp(QBUser qbUser) {
        super.onSignUp(qbUser);
//        mViewModel.addQBUser(qbUser);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GOOGLE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mViewModel.callGoogleManager();
            }
        }
    }


    public void enableDisableRegisterButtons(boolean flag) {
        findViewById(R.id.btnRegister).setEnabled(flag);
        findViewById(R.id.btnFacebook).setEnabled(flag);
        findViewById(R.id.btnGoogle).setEnabled(flag);
    }

    public void setCityAdapter(List<CityDao> list) {
        mCityList.clear();
        for (CityDao dao : list) {
            mCityList.add(dao.getCityName());
        }
        mAdapter = new CityAutoComepleteAdapter(this, mCityList);
        mAetCity.setAdapter(mAdapter);
    }


    private void recreateChatSession() {
        QBUser user = SharedPreferencesUtil.getQbUser();
        if (user != null) {
            reloginToChat(user);
        } else {
            startActivity(new Intent(RegistrationActivity.this, HomeActivity.class));
            finish();
        }
    }

    /**
     * @param user
     */
    private void reloginToChat(final QBUser user) {
        ChatHelper.getInstance().login(user, new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void result, Bundle bundle) {
                isAppSessionActive = true;
                subscribeForPushNotification();
                startActivity(new Intent(RegistrationActivity.this, HomeActivity.class));
                finish();
            }

            @Override
            public void onError(QBResponseException e) {
                isAppSessionActive = false;
                if (retry < 3) {
                    retry++;
                    reloginToChat(user);
                } else {
                    startActivity(new Intent(RegistrationActivity.this, HomeActivity.class));
                    finish();
                }
            }
        });
    }

    private void subscribeForPushNotification() {
        if (!SharedPrefsHelper.getInstance().<Boolean>get(Consts.QB_SUBSCRIPTION, false)) {
            QbAuthUtils.subscribeWithQBPushNotification(AppApplication.getGCMId());
        }

        // Load QBDialog after login immediately
//        loadDialogsFromQb();
    }

}