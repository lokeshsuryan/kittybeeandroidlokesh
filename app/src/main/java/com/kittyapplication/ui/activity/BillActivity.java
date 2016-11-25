package com.kittyapplication.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.kittyapplication.R;
import com.kittyapplication.custom.ImageLoaderListener;
import com.kittyapplication.model.BillDao;
import com.kittyapplication.ui.viewmodel.BillViewModel;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.ImageUtils;
import com.kittyapplication.utils.Utils;

import java.net.URL;

/**
 * Created by Dhaval Riontech on 10/8/16.
 */
public class BillActivity extends BaseActivity {
    private static final String TAG = BillActivity.class.getSimpleName();
    private BillViewModel mViewModel;
    private EditText mEdtPresentMembers;
    private EditText mEdtTotCollection;
    private EditText mEdtAdvancePaid;
    private EditText mEdtGameGift;
    private EditText mEdtBillAmount;
    private EditText mEdtPrevBalance;
    private EditText mEdtCarryForward;
    private EditText mEdtBalanceWith;
    private ImageView mImgBillImage;
    private Button mBtnUploadBill;
    private Button mBtnSumitBill;
    private ProgressBar mProgressBillImage;
    private String mStrPicProfile = "";
    private boolean mBillExist = false;
    private String mBillId = "0";
    private String mGroudId;
    private String mKittyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(BillActivity.this).inflate(
                R.layout.activity_bill, null);

        mGroudId = getIntent().getStringExtra(AppConstant.GROUP_ID);
        mKittyId = getIntent().getStringExtra(AppConstant.KITTY_ID);

        mImgBillImage = (ImageView) view.findViewById(R.id.imgBillImage);
        mProgressBillImage = (ProgressBar) view.findViewById(R.id.progressBillImage);
        mEdtPresentMembers = (EditText) view.findViewById(R.id.edtPresentMembers);
        mEdtTotCollection = (EditText) view.findViewById(R.id.edtTotCollection);
        mEdtAdvancePaid = (EditText) view.findViewById(R.id.edtAdvancePaid);
        mEdtGameGift = (EditText) view.findViewById(R.id.edtGameGift);
        mEdtBillAmount = (EditText) view.findViewById(R.id.edtBillAmount);
        mEdtPrevBalance = (EditText) view.findViewById(R.id.edtPrevBalance);
        mEdtCarryForward = (EditText) view.findViewById(R.id.edtCarryForward);
        mEdtBalanceWith = (EditText) view.findViewById(R.id.edtBalanceWith);
        mBtnUploadBill = (Button) view.findViewById(R.id.btnUploadBill);
        mBtnSumitBill = (Button) view.findViewById(R.id.btnSumitBill);

        mBtnUploadBill.setOnClickListener(this);
        mBtnSumitBill.setOnClickListener(this);
        mImgBillImage.setOnClickListener(this);

        mViewModel = new BillViewModel(BillActivity.this, mGroudId, mKittyId);

        addLayoutToContainer(view);
        hideLeftIcon();
        hideBottomLayout();
    }

    @Override
    String getActionTitle() {
        return getResources().getString(R.string.bill);
    }

    @Override
    boolean isDisplayHomeAsUpEnabled() {
        return true;
    }

    @Override
    boolean hasDrawer() {
        return false;
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.btnUploadBill:
                openImagePicker();
                break;

            case R.id.btnSumitBill:
                if (mBillExist) {
                    mViewModel.editBill(getFormData());
                } else {
                    mViewModel.addBill(getFormData());
                }
                break;

            case R.id.imgBillImage:
                openImagePicker();
                break;
        }
    }

    /**
     * @param data
     */
    public void displayBillData(BillDao data) {
        mEdtPresentMembers.setText(data.getMemberPresent());
        mEdtTotCollection.setText(data.getCollection());
        mEdtAdvancePaid.setText(data.getAdvancedPaid());
        mEdtGameGift.setText(data.getGiftGame());
        mEdtBillAmount.setText(data.getAmount());
        mEdtPrevBalance.setText(data.getPreviousBalance());
        mEdtCarryForward.setText(data.getCarryForword());
        mEdtBalanceWith.setText(data.getBalanceWith());
        if (Utils.isValidString(data.getImage())) {
            //for updated diary from Server
            if (Utils.isValidURL(data.getImage())) {
                ImageUtils.getImageLoader(BillActivity.this)
                        .displayImage(data.getImage(), mImgBillImage,
                                new ImageLoaderListener(mProgressBillImage));
            } else {
                Bitmap bitmap = Utils.getBitmapFromString(data.getImage());
                if (bitmap != null) {
                    mImgBillImage.setImageBitmap(bitmap);
                }
            }
        } else {
            // if not updated in server
            Bitmap bitmap = Utils.getBitmapFromString(data.getImageBill());
            if (bitmap != null) {
                mImgBillImage.setImageBitmap(bitmap);
            }
        }
        setmBillId(data.getId());
        new ImageDownload().execute(data);
    }

    private BillDao getFormData() {
        BillDao dao = new BillDao();
        dao.setGroupId(mGroudId);
        dao.setKittyId(mKittyId);
        dao.setMemberPresent(getText(mEdtPresentMembers));
        dao.setCollection(getText(mEdtTotCollection));
        dao.setAdvancedPaid(getText(mEdtAdvancePaid));
        dao.setGiftGame(getText(mEdtGameGift));
        dao.setAmount(getText(mEdtBillAmount));
        dao.setPreviousBalance(getText(mEdtPrevBalance));
        dao.setCarryForword(getText(mEdtCarryForward));
        dao.setBalanceWith(mEdtBalanceWith.getText().toString());
        dao.setImageBill(mStrPicProfile);
        return dao;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mViewModel.mImagePickerDialog.onActivityResult(requestCode, resultCode, data);
        /*if (resultCode == RESULT_OK) {
            try {
                final InputStream imageStream = getContentResolver().openInputStream(data.getData());
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                selectedImage.compress(Bitmap.CompressFormat.PNG, 50, baos); //bm is the bitmap object
                byte[] byteArrayImage = baos.toByteArray();

                mStrPicProfile = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
                mImgBillImage.setImageBitmap(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }*/
    }

    private String getText(EditText edt) {
        return Utils.getText(edt);

    }

    public boolean ismBillExist() {
        return mBillExist;
    }

    public void setmBillExist(boolean mBillExist) {
        this.mBillExist = mBillExist;
    }

    public String getmBillId() {
        return mBillId;
    }

    public void setmBillId(String mBillId) {
        this.mBillId = mBillId;
    }

    public void setmStrPicProfile(String mStrPicProfile) {
        this.mStrPicProfile = mStrPicProfile;
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

    private void openImagePicker() {
        if (isStoragePermissionGranted())
            mViewModel.getImagePicker();
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                AppLog.d(TAG, "Permission is granted");
                return true;
            } else {

                AppLog.d(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.CAMERA}, 1);
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
        try {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                AppLog.d(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
                //resume tasks needing this permission
                if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    AppLog.d(TAG, "Permission: " + permissions[1] + "was " + grantResults[1]);
                    mViewModel.getImagePicker();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getmStrPicProfile() {
        return mStrPicProfile;
    }

    public class ImageDownload extends AsyncTask<BillDao, Void, Bitmap> {
        private BillDao mBillDao;

        @Override
        protected Bitmap doInBackground(BillDao... params) {
            Bitmap image = null;
            mBillDao = params[0];
            try {
                if (Utils.checkInternetConnection(BillActivity.this)) {
                    if (Utils.isValidString(mBillDao.getImage())
                            && Utils.isValidURL(mBillDao.getImage())) {
                        try {
                            URL url = new URL(mBillDao.getImage());
                            image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                        } catch (Exception e) {
                            e.printStackTrace();
                            AppLog.e(TAG, e.getMessage(), e);
                        }

                    }
                }
            } catch (Exception e) {
                AppLog.e(TAG, e.getMessage(), e);
            }
            return image;
        }

        @Override
        protected void onPostExecute(Bitmap aVoid) {
            super.onPostExecute(aVoid);
            if (aVoid != null) {
                mBillDao.setImage(Utils.getImageInString(aVoid));
                mViewModel.insertBill(mBillDao, false);
            }
        }
    }
}
