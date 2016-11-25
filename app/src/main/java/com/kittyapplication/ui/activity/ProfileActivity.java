package com.kittyapplication.ui.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.kittyapplication.R;
import com.kittyapplication.adapter.CityAutoComepleteAdapter;
import com.kittyapplication.custom.AutoCompleteTextView;
import com.kittyapplication.custom.DialogDateTimePicker;
import com.kittyapplication.listener.DateListener;
import com.kittyapplication.model.CityDao;
import com.kittyapplication.model.MyProfileRequestDao;
import com.kittyapplication.model.MyProfileResponseDao;
import com.kittyapplication.model.RegisterResponseDao;
import com.kittyapplication.model.ServerResponse;
import com.kittyapplication.services.ProfileUpdateService;
import com.kittyapplication.ui.viewmodel.MyProfileViewModel;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.ImageUtils;
import com.kittyapplication.utils.PreferanceUtils;
import com.kittyapplication.utils.Utils;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dhaval Riontech on 9/8/16.
 */
public class ProfileActivity extends BaseActivity implements DateListener, AdapterView.OnItemSelectedListener {
    private static final String TAG = ProfileActivity.class.getSimpleName();
    private MyProfileViewModel mViewModel;
    private RegisterResponseDao mResponseDao;
    private int dateDialogFor = 0;
    private DialogDateTimePicker mDateTimePicker;
    private ProgressDialog mDialog;

    private EditText mEdtAddress, mEdtEmail, mEdtStatus, mEdtName, mEdtPhone;
    private TextView mTxtBirthday, mTxtAnniversary, mBtnUpdate;
    private LinearLayout mLlBirthday, mLlAnniversary;
    private RadioGroup mRgSex;

    private ImageView mImgUserLeft, mImgUserCenter, mImgUserRight, mImgCall, mImgSms;
    private Bitmap mBmpUserProfile, mBmpUserFamily, mBmpUserCouple;
    private String mStrPicProfile, mStrPicFamily, mStrPicCouple;

    private ProgressBar mProgressLeft, mProgressCenter, mProgressRight;
    private boolean mIsLoginUser = false;
    private MenuItem mEditMenuIcon;
    private String profileFilePath;

    private AutoCompleteTextView mAetCity;
    private String mSelectedCity = null;
    private CityAutoComepleteAdapter mAdapter;
    private List<String> mCityList = new ArrayList<>();


    private boolean isProfile = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(ProfileActivity.this).inflate(
                R.layout.activity_profile, null);
        hideLeftIcon();
        isStoragePermissionGranted();

        mViewModel = new MyProfileViewModel(ProfileActivity.this);
        mResponseDao = PreferanceUtils.getLoginUserObject(ProfileActivity.this);

        mEdtName = (EditText) view.findViewById(R.id.edtProfileName);
        mImgUserLeft = (ImageView) view.findViewById(R.id.imgMyProfileLeftUserIcon);
        mImgUserCenter = (ImageView) view.findViewById(R.id.imgMyProfileCenterUserIcon);
        mImgUserRight = (ImageView) view.findViewById(R.id.imgMyProfileRightUserIcon);
        mProgressLeft = (ProgressBar) view.findViewById(R.id.progressLeft);
        mProgressCenter = (ProgressBar) view.findViewById(R.id.progressCenter);
        mProgressRight = (ProgressBar) view.findViewById(R.id.progressRight);
        mEdtPhone = (EditText) view.findViewById(R.id.edtProfilePhone);
        mImgCall = (ImageView) view.findViewById(R.id.imgActionCall);
        mImgSms = (ImageView) view.findViewById(R.id.imgActionChat);

        mEdtStatus = (EditText) view.findViewById(R.id.edtProfileStatus);

        mEdtEmail = (EditText) view.findViewById(R.id.edtProfileEmail);
        mRgSex = (RadioGroup) view.findViewById(R.id.rgGender);
        mRgSex.setEnabled(false);
//        mEdtCity = (EditText) view.findViewById(R.id.edtProfileCity);
        mAetCity = (AutoCompleteTextView) view.findViewById(R.id.edtProfileCity);
        mEdtAddress = (EditText) view.findViewById(R.id.edtProfileAddress);

        mTxtBirthday = (TextView) view.findViewById(R.id.txtBirthday);
        mLlBirthday = (LinearLayout) view.findViewById(R.id.llBirthday);
        mTxtAnniversary = (TextView) view.findViewById(R.id.txtAnniversary);
        mLlAnniversary = (LinearLayout) view.findViewById(R.id.llAnniversary);
        mBtnUpdate = (TextView) view.findViewById(R.id.txtProfileUpdate);

        mBtnUpdate.setOnClickListener(this);
        mImgCall.setOnClickListener(this);
        mImgSms.setOnClickListener(this);

        mDateTimePicker = new DialogDateTimePicker(ProfileActivity.this);

        Intent intent = getIntent();
        if (intent.hasExtra(AppConstant.USER_PROFILE_ID)) {
            mIsLoginUser = false;
            showProgressDialog();
            QBUsers.getUser(intent.getIntExtra(AppConstant.USER_PROFILE_ID, 0),
                    new QBEntityCallback<QBUser>() {
                        @Override
                        public void onSuccess(QBUser qbUser, Bundle bundle) {
                            mViewModel.setData(qbUser.getLogin().substring(1));
                        }

                        @Override
                        public void onError(QBResponseException e) {
                            AppLog.d(TAG, "onError: " + e.getMessage());
                        }
                    });

        } else {
            showProgressDialog();
            mIsLoginUser = true;
            mViewModel.setData(PreferanceUtils.getLoginUserObject(ProfileActivity.this)
                    .getUserID());
        }

        disableData();

        addLayoutToContainer(view);

        Utils.setImeiOption(mEdtName, false, this);
        mEdtStatus.setTag(mEdtEmail);
        Utils.setImeiOption(mEdtStatus, true, this);
        Utils.setImeiOption(mEdtEmail, false, this);

        mAetCity.setTag(mEdtAddress);
        Utils.setImeiOption(mAetCity, true, this);
        Utils.setImeiOption(mEdtAddress, false, this);
        mEdtAddress.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Utils.hideKeyboard(ProfileActivity.this, v);
                    /*if (validateMyProfileForm()) {
                        MyProfileRequestDao requestDao = getFormData();
                        Intent profileUpdateService = new Intent(ProfileActivity.this, ProfileUpdateService.class);
                        com.kittyapplication.rest.Singleton.getInstance().setProfileRequestDao(requestDao);
                        startService(profileUpdateService);
                        disableData();
                        AlertDialogUtils.showSnackToast(getResources().getString(R.string.profile_update_successfully), ProfileActivity.this);
                        mViewModel.updateQBUserProfilePicture(mResponseDao, profileFilePath);
                    }*/
                    return true;
                }
                return false;
            }
        });

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
    String getActionTitle() {
        return getString(R.string.my_profile_app_title);
    }

    @Override
    boolean isDisplayHomeAsUpEnabled() {
        return true;
    }

    @Override
    boolean hasDrawer() {
        if (getIntent().hasExtra(AppConstant.USER_PROFILE_ID)) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mInflater = getMenuInflater();
        mInflater.inflate(R.menu.menu_myprofile, menu);
        mEditMenuIcon = menu.findItem(R.id.action_edit_myprofile);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit_myprofile:
                enableData();
                break;
            case android.R.id.home:
                if (hasDrawer()) {
                    toggle();
                } else {
                    onBackPressed();
                }
                break;

            case R.id.menu_home_jump:
                Utils.openActivity(this, HomeActivity.class);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.llBirthday:
                Utils.hideKeyboard(ProfileActivity.this, mTxtBirthday);
                dateDialogFor = 1;
                mDateTimePicker.showDatePickerDialog(ProfileActivity.this);
                break;
            case R.id.llAnniversary:
                Utils.hideKeyboard(ProfileActivity.this, mTxtAnniversary);
                dateDialogFor = 2;
                mDateTimePicker.showDatePickerDialog(ProfileActivity.this);
                break;
            case R.id.txtProfileUpdate:
                if (validateMyProfileForm()) {
                    MyProfileRequestDao requestDao = getFormData();
                    Intent profileUpdateService = new Intent(ProfileActivity.this, ProfileUpdateService.class);
                    com.kittyapplication.rest.Singleton.getInstance().setProfileRequestDao(requestDao);
                    startService(profileUpdateService);
                    disableData();
                    showSnackbar(getResources().getString(R.string.profile_update_successfully));
                    mViewModel.updateQBUserProfilePicture(mResponseDao, profileFilePath);
                }
                break;
            case R.id.imgMyProfileLeftUserIcon:
        /*        mPickerDilaog = new ImagePickerDialog(ProfileActivity.this, this);
                mPickerDialogFor = AppConstant.ACTION_REQUEST_PROFILE;
                openDialog();
                mViewModel.imagePicker(AppConstant.ACTION_REQUEST_PROFILE);*/

                //type == 0 mean left image
                //type == 1 mean center image
                //type == 2 mean right image
                mViewModel.setUserImageIcon(0);
                isProfile = true;

                break;
            case R.id.imgMyProfileCenterUserIcon:
                /*mPickerDilaog = new ImagePickerDialog(ProfileActivity.this, this);
                mPickerDialogFor = AppConstant.ACTION_REQUEST_FAMILY;
                openDialog();
                mViewModel.imagePicker(AppConstant.ACTION_REQUEST_FAMILY);*/

                //type == 0 mean left image
                //type == 1 mean center image
                //type == 2 mean right image
                mViewModel.setUserImageIcon(1);
                isProfile = false;
                break;
            case R.id.imgMyProfileRightUserIcon:
                /*mPickerDilaog = new ImagePickerDialog(ProfileActivity.this, this);
                mPickerDialogFor = AppConstant.ACTION_REQUEST_COUPLE;
                openDialog();
                mViewModel.imagePicker(AppConstant.ACTION_REQUEST_COUPLE);*/


                //type == 0 mean left image
                //type == 1 mean center image
                //type == 2 mean right image
                mViewModel.setUserImageIcon(2);
                isProfile = false;
                break;
            case R.id.imgActionChat:
                Utils.makeSmsCall(mEdtPhone.getText().toString().trim(), ProfileActivity.this);
                break;
            case R.id.imgActionCall:
                Utils.makePhoneCall(mEdtPhone.getText().toString().trim(), ProfileActivity.this);
                break;
        }
    }

    /**
     * @return MyProfileRequestDao
     * <p/>
     * fetch form fields information and store into MyProfileRequestDao object
     */

    private MyProfileRequestDao getFormData() {
        MyProfileRequestDao requestDao = new MyProfileRequestDao();

        requestDao.setAddress(mEdtAddress.getText().toString().trim());
        requestDao.setCity(mAetCity.getText().toString().trim());
        requestDao.setCouple(mStrPicCouple);
        requestDao.setDoa(mTxtAnniversary.getText().toString().trim());
        requestDao.setDob(mTxtBirthday.getText().toString().trim());
        requestDao.setEmail(mEdtEmail.getText().toString().trim());
        requestDao.setFamily(mStrPicFamily);
        requestDao.setGender(getGender());
        requestDao.setName(mEdtName.getText().toString().trim());
        requestDao.setProfile(mStrPicProfile);
        requestDao.setStatus(StringEscapeUtils.escapeJava(mEdtStatus.getText().toString().trim()));
        return requestDao;
    }

    /**
     * @return String
     * return selected radio button value
     */
    private String getGender() {
        switch (mRgSex.getCheckedRadioButtonId()) {
            case R.id.rbMale:
                return getResources().getString(R.string.male);
            case R.id.rbFemale:
                return getResources().getString(R.string.female);
            default:
                return getResources().getString(R.string.not_now);
        }
    }

    /**
     * @return boolean
     * validate email,name form field
     */
    private boolean validateMyProfileForm() {
        if (Utils.isValidEmail(mEdtEmail, "Email")) {
            if (Utils.isValidName(mEdtName, "Name")) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case AppConstant.ACTION_REQUEST_PROFILE:
                    profileFilePath = ImageLoaderUtils.getPath(data.getData());
                    compress(AppConstant.ACTION_REQUEST_PROFILE, data.getData());
                    break;
                case AppConstant.ACTION_REQUEST_FAMILY:
                    compress(AppConstant.ACTION_REQUEST_FAMILY, data.getData());
                    break;
                case AppConstant.ACTION_REQUEST_COUPLE:
                    compress(AppConstant.ACTION_REQUEST_COUPLE, data.getData());
                    break;
            }
        }*/
        if (mViewModel.mImagePickerDialog != null) {
            mViewModel.mImagePickerDialog.onActivityResult(requestCode, resultCode, data);
//            profileFilePath = ImageLoaderUtils.getPath(data.getData());
        }
    }

    /**
     * @param what     - type of pic (couple/family/profile)
     * @param imageUri - selected image Uri from onActivityResult
     *                 <p/>
     *                 convert image into byteArray & store base64 image string into string variable, display to imageView
     */
    private void compress(int what, Uri imageUri) {
        try {
            final InputStream imageStream = getContentResolver().openInputStream(imageUri);
            final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            selectedImage.compress(Bitmap.CompressFormat.PNG, 50, baos); //bm is the bitmap object
            byte[] byteArrayImage = baos.toByteArray();

            switch (what) {
                case 1:
                    mStrPicProfile = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
                    mImgUserLeft.setImageBitmap(selectedImage);
                    break;

                case 2:
                    mStrPicFamily = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
                    mImgUserCenter.setImageBitmap(selectedImage);
                    break;

                case 3:
                    mStrPicCouple = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
                    mImgUserRight.setImageBitmap(selectedImage);
                    break;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param what          - type of pic (couple/family/profile)
     * @param selectedImage - selected image Bitmap
     *                      <p/>
     *                      display image to imageView & convert and store base64 image string
     */
    private void compress(int what, Bitmap selectedImage) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            selectedImage.compress(Bitmap.CompressFormat.PNG, 50, baos); //bm is the bitmap object
            byte[] byteArrayImage = baos.toByteArray();

            switch (what) {
                case 1:
                    mStrPicProfile = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
                    mImgUserLeft.setImageBitmap(selectedImage);
                    break;

                case 2:
                    mStrPicFamily = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
                    mImgUserCenter.setImageBitmap(selectedImage);
                    break;

                case 3:
                    mStrPicCouple = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
                    mImgUserRight.setImageBitmap(selectedImage);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getDate(String date) {
        switch (dateDialogFor) {
            case 1:
                mTxtBirthday.setText(date);
                break;
            case 2:
                mTxtAnniversary.setText(date);
                break;
        }
    }

    /**
     * show ProgressDialog
     */
    public void showDialog() {
        mDialog = new ProgressDialog(ProfileActivity.this);
        mDialog.setMessage(getResources().getString(R.string.loading_text));
        mDialog.show();
    }

    /**
     * hide ProgressDialog
     */
    public void hideDialog() {
        try {
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss();
                mDialog = null;
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    /**
     * disable profile form
     */
    public void disableData() {
        mEdtStatus.setEnabled(false);
        mEdtEmail.setEnabled(false);
        mRgSex.setEnabled(false);
        mLlBirthday.setOnClickListener(null);
        mLlAnniversary.setOnClickListener(null);
        mTxtBirthday.setEnabled(false);
        mTxtAnniversary.setEnabled(false);
        mEdtAddress.setEnabled(false);
//        mEdtCity.setEnabled(false);
        mAetCity.setEnabled(false);
        mEdtName.setEnabled(false);
        mImgUserLeft.setClickable(false);
        mImgUserCenter.setClickable(false);
        mImgUserRight.setClickable(false);

        mImgUserLeft.setOnClickListener(null);
        mImgUserCenter.setOnClickListener(null);
        mImgUserRight.setOnClickListener(null);

        mBtnUpdate.setVisibility(View.GONE);
    }

    /**
     * enable profile form
     */
    public void enableData() {
        mEdtStatus.setEnabled(true);
        mEdtEmail.setEnabled(true);
        mRgSex.setEnabled(true);
        mLlBirthday.setOnClickListener(this);
        mLlAnniversary.setOnClickListener(this);
        mTxtBirthday.setEnabled(true);
        mTxtAnniversary.setEnabled(true);
        mEdtAddress.setEnabled(true);
//        mEdtCity.setEnabled(true);
        mAetCity.setEnabled(true);
        mEdtName.setEnabled(true);

        mImgUserLeft.setOnClickListener(this);
        mImgUserCenter.setOnClickListener(this);
        mImgUserRight.setOnClickListener(this);

        mBtnUpdate.setVisibility(View.VISIBLE);
    }

    /**
     * @param object - response object from get profile api calls
     *               <p/>
     *               display values into form
     */
    public void setData(ServerResponse<MyProfileResponseDao> object) {
        try {
            mEdtPhone.setText(object.getData().getPhone());
            mEdtStatus.setText(StringEscapeUtils.unescapeJava(object.getData().getStatus()));
            mEdtEmail.setText(object.getData().getEmail());

            if (object.getData().getGender().equalsIgnoreCase(getResources().getString(R.string.male)))
                ((RadioButton) findViewById(R.id.rbMale)).setChecked(true);
            if (object.getData().getGender().equalsIgnoreCase(getResources().getString(R.string.female)))
                ((RadioButton) findViewById(R.id.rbFemale)).setChecked(true);
            if (object.getData().getGender().equalsIgnoreCase(getResources().getString(R.string.not_now)))
                ((RadioButton) findViewById(R.id.rbNotnow)).setChecked(true);

            mTxtBirthday.setText(object.getData().getDob());
            mTxtAnniversary.setText(object.getData().getDoa());
            mEdtAddress.setText(object.getData().getAddress());
//            mEdtCity.setText(object.getData().getCity());
            mAetCity.setText(object.getData().getCity());
            mEdtName.setText(object.getData().getName());
            if (!mIsLoginUser) {
                mImgCall.setVisibility(View.VISIBLE);
                mImgSms.setVisibility(View.VISIBLE);
            }

            setImageUsingUniversalImageLoader(object.getData().getProfilePic(), mImgUserLeft, mProgressLeft, 1, object.getData().getGender());
            setImageUsingUniversalImageLoader(object.getData().getFamilyIMG(), mImgUserCenter, mProgressCenter, 2, object.getData().getGender());
            setImageUsingUniversalImageLoader(object.getData().getCoupleIMG(), mImgUserRight, mProgressRight, 3, object.getData().getGender());

            mViewModel.getCityDao();

        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    /**
     * @param profilePic
     * @param imageView
     * @param progressBar
     * @param i
     * @param gender      display image using UniversalImageLoader
     */
    private void setImageUsingUniversalImageLoader(String profilePic, final ImageView imageView,
                                                   final ProgressBar progressBar, final int i, final String gender) {
        ImageUtils.getImageLoader(ProfileActivity.this).displayImage(profilePic, imageView,
                new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        super.onLoadingStarted(imageUri, view);
                        progressBar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        super.onLoadingComplete(imageUri, view, loadedImage);
                        progressBar.setVisibility(View.GONE);
                        switch (i) {
                            case 1:
                                mBmpUserProfile = loadedImage;
                                compress(1, mBmpUserProfile);
                                break;
                            case 2:
                                mBmpUserFamily = loadedImage;
                                compress(2, mBmpUserFamily);
                                break;
                            case 3:
                                mBmpUserCouple = loadedImage;
                                compress(3, mBmpUserCouple);
                                break;
                        }
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {
                        super.onLoadingCancelled(imageUri, view);
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        super.onLoadingFailed(imageUri, view, failReason);
                        progressBar.setVisibility(View.GONE);
                        if (gender.equalsIgnoreCase(getResources().getString(R.string.female))) {
                            imageView.setImageDrawable(ContextCompat.getDrawable(ProfileActivity.this, R.drawable.ic_female));
                        } else {
                            imageView.setImageDrawable(ContextCompat.getDrawable(ProfileActivity.this, R.drawable.ic_male));
                        }
                    }
                });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mIsLoginUser) {
            mEditMenuIcon.setVisible(true);
        } else {
            mEditMenuIcon.setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                AppLog.d(TAG, "Permission is granted");
                return true;
            } else {

                AppLog.d(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.CAMERA,
                                Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            AppLog.d(TAG, "Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    public void setImageString(int type, String data, String path) {
        //type == 0 mean left image
        //type == 1 mean center image
        //type == 2 mean right image
        switch (type) {
            case 0:
                mStrPicProfile = data;
                break;
            case 1:
                mStrPicFamily = data;
                break;
            case 2:
                mStrPicCouple = data;
                break;
        }

        if (isProfile) {
            profileFilePath = path;
            isProfile = false;
        }
    }

    public void setCityAdapter(List<CityDao> list) {
        mCityList.clear();
        for (CityDao dao : list) {
            mCityList.add(dao.getCityName());
        }
        mAdapter = new CityAutoComepleteAdapter(this, mCityList);
        mAetCity.setAdapter(mAdapter);
        hideProgressDialog();
    }
}
