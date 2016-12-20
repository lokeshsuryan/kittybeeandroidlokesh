package com.kittyapplication.ui.viewmodel;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import com.kittyapplication.R;
import com.kittyapplication.chat.utils.qb.QBUserUtils;
import com.kittyapplication.custom.ImagePickerDialog;
import com.kittyapplication.listener.GetImageFromListener;
import com.kittyapplication.listener.ImagePickerListener;
import com.kittyapplication.model.CityDao;
import com.kittyapplication.model.MyProfileResponseDao;
import com.kittyapplication.model.RegisterResponseDao;
import com.kittyapplication.model.ServerResponse;
import com.kittyapplication.rest.Singleton;
import com.kittyapplication.ui.activity.ProfileActivity;
import com.kittyapplication.utils.AlertDialogUtils;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.Utils;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.model.QBUser;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Dhaval Riontech on 9/8/16.
 */
public class MyProfileViewModel implements ImagePickerListener {
    private static final String TAG = MyProfileViewModel.class.getSimpleName();
    private ProfileActivity mActivity;
    private int mType;
    public ImagePickerDialog mImagePickerDialog;

    public MyProfileViewModel(ProfileActivity activity) {
        mActivity = activity;
    }

    public void setData(String id) {
        Call<ServerResponse<MyProfileResponseDao>> call =
                Singleton.getInstance().getRestOkClient().profile(id);
        call.enqueue(getMyProfileCallBack);
    }

    private Callback<ServerResponse<MyProfileResponseDao>> getMyProfileCallBack = new Callback<ServerResponse<MyProfileResponseDao>>() {
        @Override
        public void onResponse(Call<ServerResponse<MyProfileResponseDao>> call, Response<ServerResponse<MyProfileResponseDao>> response) {
            try {
                mActivity.hideProgressDialog();
                if (response.code() == 200) {
                    ServerResponse<MyProfileResponseDao> object = response.body();
                    if (object.getResponse() == AppConstant.RESPONSE_SUCCESS) {
                        mActivity.setData(object);
//                        getCityDao();
                    } else {
                        AlertDialogUtils.showServerError(object.getMessage(), mActivity);
                        AppLog.e(TAG, mActivity.getResources().getString(R.string.response_code_fail));
                    }
                } else {
                    mActivity.showSnackbar(mActivity.getResources().getString(R.string.server_error));
                }
            } catch (Exception e) {
            }
        }

        @Override
        public void onFailure(Call<ServerResponse<MyProfileResponseDao>> call, Throwable t) {
            mActivity.hideProgressDialog();
            mActivity.showSnackbar(mActivity.getResources().getString(R.string.server_error));
            AppLog.e(TAG, "," + t.getMessage());
        }
    };

    public void imagePicker(int id) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType(mActivity.getResources().getString(R.string.file_type_image));
        mActivity.startActivityForResult(photoPickerIntent, id);
    }

    public void updateQBUserProfilePicture(RegisterResponseDao mResponseDao, String profileFilePath) {
        try {
            if (mResponseDao.getQuickLogin() != null && !mResponseDao.getQuickLogin().equals("")) {
                QBUser user = new QBUser();
                user.setId(Integer.parseInt(mResponseDao.getQuickID()));
                user.setLogin(mResponseDao.getQuickLogin());
                QBUserUtils.updateProfilePicture(user, profileFilePath, new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {
                        AppLog.d(TAG, "QBUser :: " + qbUser.toString());
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        AppLog.e(TAG, e.getMessage(), e);
                    }
                });
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public void setUserImageIcon(int type) {
        //type == 0 mean left image
        //type == 1 mean center image
        //type == 2 mean right image
        mType = type;
        mImagePickerDialog = new ImagePickerDialog(mActivity, this);
        AlertDialogUtils.showImagePickerDialog(mActivity, new GetImageFromListener() {
            @Override
            public void getImageFrom(int type) {
                mImagePickerDialog.getImagesFrom(type);
            }
        });
    }

    @Override
    public void getBitmapImageFromPhone(Bitmap image) {
        //type == 0 mean left image
        //type == 1 mean center image
        //type == 2 mean right image
        AppLog.d(TAG, Utils.getImageInString(image));
        ImageView imgView;
        if (mType == 0) {
            imgView = (ImageView) mActivity.findViewById(R.id.imgMyProfileLeftUserIcon);
        } else if (mType == 1) {
            imgView = (ImageView) mActivity.findViewById(R.id.imgMyProfileCenterUserIcon);
        } else {
            imgView = (ImageView) mActivity.findViewById(R.id.imgMyProfileRightUserIcon);
        }
        imgView.setImageBitmap(image);


        mActivity.setImageString(mType, Utils.getImageInString(image), mImagePickerDialog.getPath());

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
