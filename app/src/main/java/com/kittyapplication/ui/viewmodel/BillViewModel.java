package com.kittyapplication.ui.viewmodel;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.kittyapplication.R;
import com.kittyapplication.custom.ImagePickerDialog;
import com.kittyapplication.listener.GetImageFromListener;
import com.kittyapplication.listener.ImagePickerListener;
import com.kittyapplication.model.BillDao;
import com.kittyapplication.model.ServerResponse;
import com.kittyapplication.providers.KittyBeeContract;
import com.kittyapplication.rest.Singleton;
import com.kittyapplication.sqlitedb.SQLConstants;
import com.kittyapplication.ui.activity.BillActivity;
import com.kittyapplication.utils.AlertDialogUtils;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.Utils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Dhaval Riontech on 10/8/16.
 */
public class BillViewModel implements ImagePickerListener {
    private static final String TAG = BillViewModel.class.getSimpleName();
    private BillActivity mActivity;
    private final String mGroudId;
    private final String mKittyId;
    private boolean flag = false;
    public ImagePickerDialog mImagePickerDialog;

    public BillViewModel(BillActivity activity, String groudId, String kittyId) {
        mActivity = activity;
        mGroudId = groudId;
        mKittyId = kittyId;
        initRequest();
    }

    /**
     *
     */
    private void initRequest() {
        mActivity.showProgressDialog();
        new SyncTask().execute();
    }

    /**
     *
     */
    private Callback<ServerResponse<BillDao>> getBillCallBack = new Callback<ServerResponse<BillDao>>() {
        @Override
        public void onResponse(Call<ServerResponse<BillDao>> call, Response<ServerResponse<BillDao>> response) {
            mActivity.hideProgressDialog();
            ServerResponse<BillDao> object = response.body();
            if (response.code() == 200) {
                if (object.getResponse() == AppConstant.RESPONSE_SUCCESS) {
                    if (object.getData() != null) {
                        insertBill(object.getData(), false);
                        mActivity.displayBillData(object.getData());
                        mActivity.setmBillExist(true);
                    } else {
                        AlertDialogUtils.showServerError(object.getMessage(), mActivity);
                        mActivity.setmBillExist(false);
                    }
                } else {
                    AlertDialogUtils.showServerError(object.getMessage(), mActivity);
                    AppLog.e(TAG, mActivity.getResources().getString(R.string.response_code_fail));
                }
            } else {
                AlertDialogUtils.showServerError(mActivity.getResources()
                        .getString(R.string.server_error), mActivity);
            }
        }

        @Override
        public void onFailure(Call<ServerResponse<BillDao>> call, Throwable t) {
            mActivity.hideProgressDialog();
            AlertDialogUtils.showServerError(mActivity.getResources()
                    .getString(R.string.server_error), mActivity);
        }
    };

    public void getImagePicker() {
        mImagePickerDialog = new ImagePickerDialog(mActivity, this);
        AlertDialogUtils.showImagePickerDialog(mActivity, new GetImageFromListener() {
            @Override
            public void getImageFrom(int type) {
                mImagePickerDialog.getImagesFrom(type);
            }
        });
    }

    /**
     * @param dao
     */
    public void addBill(BillDao dao) {
        if (Utils.checkInternetConnection(mActivity)) {
            mActivity.showProgressDialog();
            Call<ServerResponse<BillDao>> call = Singleton.getInstance().getRestOkClient()
                    .addBill(dao);
            call.enqueue(addBillCallBack);
        } else {
            insertBill(dao, true);
            AlertDialogUtils.showServerError(mActivity.getResources().getString(R.string.bill_added_success), mActivity);
            mActivity.finish();
        }
    }

    /**
     * @param dao
     */
    public void editBill(BillDao dao) {
        if (Utils.checkInternetConnection(mActivity)) {
            dao.setId(mActivity.getmBillId());
            mActivity.showProgressDialog();

            Call<ServerResponse<BillDao>> call = Singleton.getInstance().getRestOkClient()
                    .editBill(dao);
            call.enqueue(addBillCallBack);
        } else {
            insertBill(dao, true);
            AlertDialogUtils.showServerError(mActivity.getResources().getString(R.string.update_bill_success), mActivity);
        }
    }

    /**
     *
     */
    Callback<ServerResponse<BillDao>> addBillCallBack = new Callback<ServerResponse<BillDao>>() {
        @Override
        public void onResponse(Call<ServerResponse<BillDao>> call, Response<ServerResponse<BillDao>> response) {
            mActivity.hideProgressDialog();
            ServerResponse<BillDao> object = response.body();
            if (response.code() == 200) {
                if (object.getResponse() == AppConstant.RESPONSE_SUCCESS) {
                    if (object.getData() != null) {
                        if (Utils.isValidString(mActivity.getmStrPicProfile())) {
                            object.getData().setImage(mActivity.getmStrPicProfile());
                        }
                        insertBill(object.getData(), false);
                        mActivity.displayBillData(object.getData());
                        AlertDialogUtils.showServerError(object.getMessage(), mActivity);
                        mActivity.finish();
                    } else {
                        AlertDialogUtils.showServerError(object.getMessage(), mActivity);
                    }
                } else {
                    AlertDialogUtils.showServerError(object.getMessage(), mActivity);
                    AppLog.e(TAG, mActivity.getResources().getString(R.string.response_code_fail));
                }
            } else {
                AlertDialogUtils.showServerError(mActivity.getResources()
                        .getString(R.string.server_error), mActivity);
            }
        }

        @Override
        public void onFailure(Call<ServerResponse<BillDao>> call, Throwable t) {
            mActivity.hideProgressDialog();
            AlertDialogUtils.showServerError(mActivity.getResources()
                    .getString(R.string.server_error), mActivity);
        }
    };

    /**
     * Insert Bill
     */
    public void insertBill(BillDao dao, boolean flag) {
        try {
            ContentValues values = new ContentValues();
            values.put(SQLConstants.KEY_GROUP_ID, dao.getGroupId());
            values.put(SQLConstants.KEY_KITTY_ID, dao.getKittyId());
            values.put(SQLConstants.KEY_DATA, new Gson().toJson(dao));
            if (flag) {
                values.put(SQLConstants.KEY_IS_SYNC, 1);
            }
            mActivity.getContentResolver().insert(KittyBeeContract.Bill.CONTENT_URI, values);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public void getBitmapImageFromPhone(Bitmap image) {
        try {
            ImageView imgBill = (ImageView) mActivity.findViewById(R.id.imgBillImage);
            imgBill.setImageBitmap(image);
            mActivity.setmStrPicProfile(Utils.getImageInString(image));
            mImagePickerDialog = null;
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    /**
     *
     */
    private class SyncTask extends AsyncTask<Void, Void, BillDao> {
        @Override
        protected BillDao doInBackground(Void... params) {
            BillDao dao = new BillDao();
            String selection = SQLConstants.KEY_GROUP_ID + "=? AND " + SQLConstants.KEY_KITTY_ID + "=?";
            String[] selectionArgs = {mGroudId, mKittyId};
            ContentResolver resolver =
                    mActivity.getContentResolver();
            Cursor cursor = resolver.query(
                    KittyBeeContract.Bill.CONTENT_URI,
                    KittyBeeContract.Bill.PROJECTION_ALL,
                    selection, selectionArgs, null);

            if (cursor != null && cursor.getCount() > 0) {
                // show data from db
                Gson gson = new Gson();

                while (cursor.moveToNext()) {
                    dao = gson.fromJson(
                            cursor.getString(cursor.getColumnIndex(SQLConstants.KEY_DATA)), BillDao.class);
                }
                flag = true;
                cursor.close();
            }
            return dao;
        }

        @Override
        protected void onPostExecute(BillDao aVoid) {
            super.onPostExecute(aVoid);
//            MySQLiteHelper.getInstance(mActivity).close();

            if (aVoid != null) {
                mActivity.displayBillData(aVoid);
                AppLog.d(TAG, "DATA " + new Gson().toJson(aVoid));
                mActivity.setmBillExist(true);
                mActivity.hideProgressDialog();
            } else {
                mActivity.setmBillExist(false);
            }

            if (Utils.checkInternetConnection(mActivity)) {
                BillDao dao = new BillDao();
                dao.setGroupId(mGroudId);
                dao.setKittyId(mKittyId);

                Call<ServerResponse<BillDao>> call = Singleton.getInstance().getRestOkClient()
                        .getBill(dao);
                call.enqueue(getBillCallBack);
            } else {
                if (!flag) {
                    mActivity.hideProgressDialog();
                    AlertDialogUtils.showServerError(mActivity.getResources().
                            getString(R.string.no_internet_available), mActivity);
                }
            }
        }
    }
}
