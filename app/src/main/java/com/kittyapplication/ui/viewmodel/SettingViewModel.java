package com.kittyapplication.ui.viewmodel;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.kittyapplication.R;
import com.kittyapplication.chat.utils.chat.QbUpdateDialogListener;
import com.kittyapplication.chat.utils.qb.QbDialogUtils;
import com.kittyapplication.custom.ImagePickerDialog;
import com.kittyapplication.listener.ChangeGroupNameListener;
import com.kittyapplication.listener.GetImageFromListener;
import com.kittyapplication.listener.ImagePickerListener;
import com.kittyapplication.model.ChatData;
import com.kittyapplication.model.ParticipantDao;
import com.kittyapplication.model.ReqChangeGroupName;
import com.kittyapplication.model.ReqRefreshGroup;
import com.kittyapplication.model.ServerResponse;
import com.kittyapplication.providers.KittyBeeContract;
import com.kittyapplication.rest.Singleton;
import com.kittyapplication.sqlitedb.SQLConstants;
import com.kittyapplication.ui.activity.AddMemberActivity;
import com.kittyapplication.ui.activity.ChangeHostActivity;
import com.kittyapplication.ui.activity.DeleteMemberActivity;
import com.kittyapplication.ui.activity.GiveRightToEditActivity;
import com.kittyapplication.ui.activity.RuleActivity;
import com.kittyapplication.ui.activity.SettingActivity;
import com.kittyapplication.utils.AlertDialogUtils;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Pintu Riontech on 17/8/16.
 * vaghela.pintu31@gmail.com
 */
public class SettingViewModel implements ChangeGroupNameListener, GetImageFromListener, ImagePickerListener {
    private static final String TAG = SettingViewModel.class.getSimpleName();
    private SettingActivity mActivity;
    private ProgressDialog mDialog;
    private ChatData mChatData;
    private ParticipantDao mParticipantDao;
    private Call<ServerResponse<ParticipantDao>> mSettingApiCall;


    //Type == 0 FOR REFRESH GROUP
    //Type == 1 FOR CHANGE GROUP NAME
    //Type == 2 FOR CHANGE GROUP IMAGE
    private int mType;
    public ImagePickerDialog mImagePickerDialog;
    private Bitmap mBitMap;
    private String mGroupName;
    private ProgressBar mPbLoader;
    private boolean flag = false;

    public SettingViewModel(SettingActivity activity) {
        mActivity = activity;
    }


    public void getData(String data) {
        try {
            showDialog();
            mChatData = new Gson().fromJson(data, ChatData.class);
            if (mChatData.getName() != null) {
                mActivity.setName(mChatData.getName());
            }
            mActivity.setRefreshMenuButton(mChatData.getKittyDate());
            if (mChatData.getIsAdmin().equalsIgnoreCase("1")) {
                mActivity.changeHostButtonVisibility(true);
            } else {
                mActivity.changeHostButtonVisibility(false);
            }
            AppLog.d(TAG, "DATA = " + data);
            new SyncTask().execute(mChatData);


        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    private Callback<ServerResponse<ParticipantDao>> getParticipantCallBack =
            new Callback<ServerResponse<ParticipantDao>>() {
                @Override
                public void onResponse(Call<ServerResponse<ParticipantDao>> call, Response<ServerResponse<ParticipantDao>> response) {
                    hideDialog();
                    if (response.code() == 200) {
                        ParticipantDao data = response.body().getData();
                        if (response.body().getResponse() == AppConstant.RESPONSE_SUCCESS) {
                            if (data != null) {
                                mActivity.hideEmptyView();
                                mParticipantDao = data;
                                insertSetting(data);
                                mActivity.setDataIntoList(data);
                            } else {
                                mActivity.showEmptyView();
                                showServerError(mActivity.getResources().getString(R.string.server_error));
                            }
                        } else {
                            mActivity.showEmptyView();
                            showServerError(mActivity.getResources().getString(R.string.server_error));
                        }
                    } else {
                        mActivity.showEmptyView();
                        showServerError(mActivity.getResources().getString(R.string.server_error));
                    }
                }

                @Override
                public void onFailure(Call<ServerResponse<ParticipantDao>> call, Throwable t) {
                    hideDialog();
                    mActivity.hideEmptyView();
//                    mActivity.showSnackBar(mActivity.getResources().getString(R.string.server_error));
                }
            };

    public void showDialog() {
        mDialog = new ProgressDialog(mActivity);
        mDialog.setMessage(mActivity.getResources().getString(R.string.loading_text));
        mDialog.show();
    }

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

    private void showServerError(String error) {
        if (Utils.isValidString(error)) {
            mActivity.showSnackBar(error);
        } else {
            mActivity.showSnackBar(mActivity.getResources().getString(R.string.server_error));
        }
    }

    public void actionAddMember() {
        try {
            if (mParticipantDao != null
                    && mParticipantDao.getParticipant() != null
                    && !mParticipantDao.getParticipant().isEmpty()) {

                if (mChatData.getIsAdmin().equalsIgnoreCase("1")) {
                    Intent addMemberIntent = new Intent(mActivity, AddMemberActivity.class);
                    addMemberIntent.putExtra(AppConstant.INTENT_ADD_MEMBER,
                            new Gson().toJson(mParticipantDao));

                    addMemberIntent.putExtra(AppConstant.INTENT_ADD_MEMBER_GROUP_ID,
                            mChatData.getGroupID());

                    addMemberIntent.putExtra(AppConstant.INTENT_KITTY_TYPE,
                            mChatData.getCategory());

                    addMemberIntent.putExtra(AppConstant.INTENT_ADD_MEMBER_KITTY_DATE,
                            mChatData.getKittyDate());

                    addMemberIntent.putExtra(AppConstant.INTENT_DIALOG_ID,
                            mChatData.getQuickId());

                    addMemberIntent.putExtra(AppConstant.INTENT_GET_KITTY_RULE,
                            mChatData.getRule());

                    mActivity.startActivity(addMemberIntent);
                } else {
                    mActivity.showSnackBar(mActivity.getResources()
                            .getString(R.string.add_member_warning));
                }
            } else {
                mActivity.showSnackBar(mActivity.getResources()
                        .getString(R.string.server_error));
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public void actionDeleteMember() {
        try {
            if (mParticipantDao != null
                    && mParticipantDao.getParticipant() != null
                    && !mParticipantDao.getParticipant().isEmpty()) {

                if (mChatData.getIsAdmin().equalsIgnoreCase("1")) {
                    Intent deleteMember = new Intent(mActivity, DeleteMemberActivity.class);
                    deleteMember.putExtra(AppConstant.INTENT_ADD_MEMBER,
                            new Gson().toJson(mParticipantDao));
                    deleteMember.putExtra(AppConstant.INTENT_ADD_MEMBER_KITTY_DATE, mChatData.getKittyDate());
                    deleteMember.putExtra(AppConstant.INTENT_ADD_MEMBER_GROUP_ID, mChatData.getGroupID());
                    deleteMember.putExtra(AppConstant.INTENT_KITTY_ID, mChatData.getKittyId());
                    deleteMember.putExtra(AppConstant.INTENT_DIALOG_ID, mChatData.getQuickId());
                    mActivity.startActivity(deleteMember);
                } else {
                    mActivity.showSnackBar(mActivity.getResources()
                            .getString(R.string.delete_member_warning));
                }
            } else {
                mActivity.showSnackBar(mActivity.getResources()
                        .getString(R.string.server_error));
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public void actionKittyRule() {
        try {
            if (mChatData != null
                    && mChatData.getGroupID() != null) {
                if (mChatData.getRule().equalsIgnoreCase("1")) {
                    Intent ruleIntent = new Intent(mActivity, RuleActivity.class);
                    ruleIntent.putExtra(AppConstant.SETTING_DATA,
                            new Gson().toJson(mChatData));
                    mActivity.startActivity(ruleIntent);
                } else {
                    mActivity.showSnackBar(mActivity.getResources()
                            .getString(R.string.please_create_kitty_first));
                }
            } else {
                mActivity.showSnackBar(mActivity.getResources()
                        .getString(R.string.server_error));
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public void actionRefreshGroup() {
        try {
            if (mChatData != null &&
                    mChatData.getIsAdmin() != null &&
                    mChatData.getIsAdmin().equalsIgnoreCase("1")) {

                if (Utils.checkInternetConnection(mActivity)) {
                    mType = 0;
                    ReqRefreshGroup reqRefreshGroup = new ReqRefreshGroup();
                    reqRefreshGroup.setDelete("0");
                    Call<ServerResponse> call = Singleton.getInstance()
                            .getRestOkClient().refershGroup(mChatData.getGroupID(),
                                    reqRefreshGroup);
                    call.enqueue(reqRefreshGroupCallBack);
                } else {
                    mActivity.showSnackBar(mActivity.getResources()
                            .getString(R.string.no_internet_available));
                }
            } else {
                mActivity.showSnackBar(mActivity.getResources()
                        .getString(R.string.refresh_group_warning));
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    private Callback<ServerResponse> reqRefreshGroupCallBack =
            new Callback<ServerResponse>() {
                @Override
                public void onResponse(Call<ServerResponse> call,
                                       Response<ServerResponse> response) {
                    hideDialog();
                    if (response.code() == 200) {
                        if (response.body().getResponse() == AppConstant.RESPONSE_SUCCESS) {
                            if (mType == 0) {

                                mActivity.showSnackBar(mActivity.getResources()
                                        .getString(R.string.refresh_group_sucess));
                            } else if (mType == 1) {
                                mActivity.setName(mGroupName);
                                mActivity.showSnackBar(mActivity.getResources()
                                        .getString(R.string.change_name_sucess));
                            } else if (mType == 2) {
                                mPbLoader.setVisibility(View.GONE);
                                mActivity.showSnackBar(mActivity.getResources()
                                        .getString(R.string.change_img_sucess));
                                mActivity.setImage(mBitMap);
                            }
                        } else {
                            if (mPbLoader != null) {
                                mPbLoader.setVisibility(View.GONE);
                            }
                            showServerError(response.body().getMessage());
                        }
                    } else {
                        if (mPbLoader != null) {
                            mPbLoader.setVisibility(View.GONE);
                        }
                        showServerError(response.body().getMessage());
                    }
                }

                @Override
                public void onFailure(Call<ServerResponse> call, Throwable t) {
                    hideDialog();
                    if (mPbLoader != null) {
                        mPbLoader.setVisibility(View.GONE);
                    }
                    AlertDialogUtils.showSnackToast(mActivity.getResources()
                            .getString(R.string.server_error), mActivity);
                }
            };

    public void changeGroupName() {
        AlertDialogUtils.showChnageGroupNameDialog(mActivity, this);
    }

    @Override
    public void getChangedGroupName(final String name) {
        mGroupName = name;
        final String groupName = name;
        if (Utils.checkInternetConnection(mActivity)) {

            QbDialogUtils.updateQbDialogById(mActivity.getmDialogId(), 0, null,
                    mGroupName, new QbUpdateDialogListener() {
                        @Override
                        public void onSuccessResponce() {
                            mType = 1;
                            ReqChangeGroupName changeGroupNameObj = new ReqChangeGroupName();
                            changeGroupNameObj.setName(groupName);

                            Call<ServerResponse> call = Singleton.getInstance()
                                    .getRestOkClient().chnageGroupName(mChatData.getGroupID(),
                                            changeGroupNameObj);

                            call.enqueue(reqRefreshGroupCallBack);
                        }

                        @Override
                        public void onError() {
                            mActivity.showSnackBar(mActivity.getResources()
                                    .getString(R.string.quick_blox_error_found));
                        }
                    }, null);
        } else {
            mActivity.showSnackBar(mActivity.getResources()
                    .getString(R.string.no_internet_available));
        }
    }

    public void changeGroupImage() {
        mImagePickerDialog = new ImagePickerDialog(mActivity, this);
        AlertDialogUtils.showImagePickerDialog(mActivity, this);
    }

    @Override
    public void getImageFrom(int type) {
        mImagePickerDialog.getImagesFrom(type);
    }

    @Override
    public void getBitmapImageFromPhone(Bitmap image) {
        mPbLoader = (ProgressBar) mActivity.findViewById(R.id.pbLoaderGroupImage);
        mPbLoader.setVisibility(View.VISIBLE);
        AppLog.d(TAG, Utils.getImageInString(image));
        mBitMap = image;
        final Bitmap imgBitMap = image;
        if (Utils.checkInternetConnection(mActivity)) {

           /* QbDialogUtils.updateQbDialogById(mActivity.getmDialogId(), 4, null,
                    null, new QbUpdateDialogListener() {
                        @Override
                        public void onSuccessResponce() {*/
            mType = 2;
            ReqChangeGroupName changeGroupNameObj = new ReqChangeGroupName();
            changeGroupNameObj.setGroupIMG(Utils.getImageInString(imgBitMap));
            Call<ServerResponse> call = Singleton.getInstance()
                    .getRestOkClient().chnageGroupName(mChatData.getGroupID(),
                            changeGroupNameObj);
            call.enqueue(reqRefreshGroupCallBack);
                       /* }

                        @Override
                        public void onError() {
                            mPbLoader.setVisibility(View.GONE);
                            mActivity.showSnackBar(mActivity.getResources()
                                    .getString(R.string.quick_blox_error_found));
                        }
                    }, Utils.getImageInString(imgBitMap));*/
        } else {
            mPbLoader.setVisibility(View.GONE);
            mActivity.showSnackBar(mActivity.getResources()
                    .getString(R.string.no_internet_available));
        }
    }

    public void actionGiveRightToEdit() {

        try {
            if (mChatData != null) {
                boolean isHostEmpty = getHostDetail(mChatData);
                if (isHostEmpty && mChatData.getIsAdmin().equalsIgnoreCase("1")) {
                    openRightsActivity();
                } else if (mChatData.getIsHost().equalsIgnoreCase("1")) {
                    openRightsActivity();
                } else {
                    mActivity.showSnackBar(mActivity.getResources()
                            .getString(R.string.right_to_edit_group_warning));
                }
            } else {
                mActivity.showSnackBar(mActivity.getResources()
                        .getString(R.string.server_error));
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    private void openRightsActivity() {
        try {
            if (mParticipantDao != null
                    && mParticipantDao.getParticipant() != null
                    && !mParticipantDao.getParticipant().isEmpty()) {

                Intent giveRightsIntent = new Intent(mActivity, GiveRightToEditActivity.class);
                giveRightsIntent.putExtra(AppConstant.INTENT_GIVE_RIGHTS_DATA,
                        new Gson().toJson(mParticipantDao).toString());
                giveRightsIntent.putExtra(AppConstant.INTENT_ADD_MEMBER_GROUP_ID,
                        mChatData.getKittyId());
                giveRightsIntent.putStringArrayListExtra(AppConstant.INTENT_GIVE_RIGHTS_ARRYA,
                        (ArrayList<String>) mChatData.getRights());
                mActivity.startActivity(giveRightsIntent);
            } else {
                mActivity.showSnackBar(mActivity.getResources()
                        .getString(R.string.server_error));
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    private boolean getHostDetail(ChatData obj) {
        List<String> hostList = obj.getHostId();
        if (hostList != null && !hostList.isEmpty()) {
            for (int i = 0; i < hostList.size(); i++) {
                if (hostList.get(i).equalsIgnoreCase("")) {
                    return true;
                }
            }
        }
        return false;
    }


    public void actionChangeHost() {
        if (mParticipantDao != null
                && mParticipantDao.getParticipant() != null
                && !mParticipantDao.getParticipant().isEmpty()) {

            Intent changeHostIntent = new Intent(mActivity, ChangeHostActivity.class);
            changeHostIntent.putExtra(AppConstant.INTENT_CHAT_DATA,
                    mChatData.getNoOfHost());
            changeHostIntent.putExtra(AppConstant.INTENT_CHANGE_HOST,
                    new Gson().toJson(mParticipantDao).toString());

            changeHostIntent.putExtra(AppConstant.GROUP_ID,
                    mChatData.getGroupID());

            mActivity.startActivity(changeHostIntent);

        } else {
            mActivity.showSnackBar(mActivity.getResources().getString(R.string.server_error));
        }
    }


    public void insertSetting(ParticipantDao dao) {
        ContentValues values = new ContentValues();
        values.put(SQLConstants.KEY_ID, dao.getId());
        values.put(SQLConstants.KEY_DATA, new Gson().toJson(dao));
        mActivity.getContentResolver().insert(KittyBeeContract.Setting.CONTENT_URI, values);
    }

    /**
     *
     */
    private class SyncTask extends AsyncTask<ChatData, Void, ParticipantDao> {

        @Override
        protected ParticipantDao doInBackground(ChatData... params) {
            ParticipantDao responseDao = null;
            Uri uri = ContentUris.withAppendedId(KittyBeeContract.Setting.CONTENT_URI,
                    Long.valueOf(params[0].getGroupID()));
            ContentResolver resolver =
                    mActivity.getContentResolver();
            Cursor cursor = resolver.query(
                    uri,                      // the URI to query
                    KittyBeeContract.Groups.PROJECTION_ALL,   // the projection to use
                    null,                           // the where clause without the WHERE keyword
                    null,                           // any wildcard substitutions
                    null);                          // the sort order without the SORT BY keyword

            if (cursor != null && cursor.getCount() > 0) {
                // show data from db
                Gson gson = new Gson();
                while (cursor.moveToNext()) {
                    responseDao = gson.fromJson(cursor.getString(1), ParticipantDao.class);
                }

                flag = true;
                cursor.close();
            }
            return responseDao;
        }

        @Override
        protected void onPostExecute(ParticipantDao aVoid) {
            super.onPostExecute(aVoid);
//            MySQLiteHelper.getInstance(mActivity).close();
            if (aVoid != null && Utils.isValidString(aVoid.getId())) {
                mParticipantDao = aVoid;
                mActivity.setDataIntoList(aVoid);
                hideDialog();
            }

            if (Utils.checkInternetConnection(mActivity)) {

                if (mChatData != null && mChatData.getGroupID() != null && Utils.isValidString(mChatData.getGroupID())) {
                    mSettingApiCall = Singleton.getInstance()
                            .getRestOkClient().getParticipant(mChatData.getGroupID());
                    mSettingApiCall.enqueue(getParticipantCallBack);
                }
            } else {
                if (!flag) {
                    hideDialog();
                    mActivity.showEmptyView();
//                    mActivity.showSnackBar(mActivity.getResources().getString(R.string.no_internet_available));
                }
            }
        }
    }

    /**
     * cancel api call if activity has been destroy
     */
    public void destroyApiCall() {
        if (mSettingApiCall != null && mSettingApiCall.isExecuted())
            mSettingApiCall.cancel();
    }
}
