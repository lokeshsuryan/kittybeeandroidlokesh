package com.kittyapplication.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;

import com.google.gson.Gson;
import com.kittyapplication.R;
import com.kittyapplication.custom.CustomTextViewBold;
import com.kittyapplication.custom.CustomTextViewNormal;
import com.kittyapplication.custom.ImageLoaderListener;
import com.kittyapplication.model.PromotionalDao;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.ImageUtils;
import com.kittyapplication.utils.Utils;

/**
 * Created by Pintu Riontech on 8/8/16.
 * vaghela.pintu31@gmail.com
 */
public class PromotionalDetailsActivity extends BaseActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String TAG = PromotionalDetailsActivity.class.getSimpleName();
    public CustomTextViewNormal mTxtAddress, mTxtContact;
    public CustomTextViewBold mTxtName;
    public ImageView mImgPromotionalDetail;
    public RatingBar mRbPromotionalDetail;
    public ProgressBar mPbLoaderImage;
    private final int REQUEST_PHONE_STATE_PERMISSION = 100;
    private String mPhoneNumer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(PromotionalDetailsActivity.this).inflate(
                R.layout.activity_promotional_details, null);
        addLayoutToContainer(view);
        hideLeftIcon();
        String str = getIntent().getStringExtra("object");
        PromotionalDao dataObject = new Gson().fromJson(str, PromotionalDao.class);
        mTxtName = (CustomTextViewBold) view.findViewById(R.id.txtPromotionalDetailsTitle);
        mTxtAddress = (CustomTextViewNormal) view.findViewById(R.id.txtPromotionalDetailsAddress);
        mRbPromotionalDetail = (RatingBar) view.findViewById(R.id.rbPromotionalDetails);
        mTxtContact = (CustomTextViewNormal) view.findViewById(R.id.txtPromotionalDetailContacts);
        mImgPromotionalDetail = (ImageView) view.findViewById(R.id.imgBannerPromotionalDetails);
        mPbLoaderImage = (ProgressBar) view.findViewById(R.id.pbLoaderPromotionalDetail);
        mPhoneNumer = dataObject.getPhone();
        setDataOnView(dataObject);
    }

    @Override
    protected String getActionTitle() {
        return "";
    }

    @Override
    protected boolean isDisplayHomeAsUpEnabled() {
        return true;
    }

    @Override
    boolean hasDrawer() {
        return false;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }


    private void setDataOnView(PromotionalDao data) {
        ImageUtils.getImageLoader(this).displayImage(data.getBanner(),
                mImgPromotionalDetail,
                new ImageLoaderListener(mPbLoaderImage));
        mTxtName.setText(data.getName());
        mTxtAddress.setText(data.getAddress1());
        mRbPromotionalDetail.setRating(Float.valueOf(data.getRating()));
        setActionbarTitle(data.getName());
        if (Utils.isValidString(data.getPhone()) && data.getPhone().length() >= 10) {
            mTxtContact.setVisibility(View.VISIBLE);
            mTxtContact.setTag(data.getPhone());
            mTxtContact.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String number = (String) v.getTag();
                    if (isCallPhone()) {
                        callToPerson(number);
                    }
                }
            });
        } else {
            mTxtContact.setVisibility(View.GONE);
        }
    }

    /**
     * @return
     */
    public boolean isCallPhone() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.CALL_PHONE)
                    == PackageManager.PERMISSION_GRANTED) {
                AppLog.d(TAG, "Permission is granted");
                return true;
            } else {

                AppLog.d(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CALL_PHONE}, 1);
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
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            AppLog.d(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
        }
    }


    private void callToPerson(String str) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + str));
        if (isCallPhone())
            startActivity(callIntent);
    }

}
