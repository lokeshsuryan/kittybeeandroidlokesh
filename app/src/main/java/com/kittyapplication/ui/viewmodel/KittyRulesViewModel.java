package com.kittyapplication.ui.viewmodel;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.gson.Gson;
import com.kittyapplication.AppApplication;
import com.kittyapplication.R;
import com.kittyapplication.chat.utils.chat.ChatHelper;
import com.kittyapplication.chat.utils.qb.QbDialogUtils;
import com.kittyapplication.chat.utils.qb.callback.QBGetGroupID;
import com.kittyapplication.custom.DialogDateTimePicker;
import com.kittyapplication.listener.DateListener;
import com.kittyapplication.listener.TimePickerListener;
import com.kittyapplication.model.ChatData;
import com.kittyapplication.model.CreateGroup;
import com.kittyapplication.model.OfflineDao;
import com.kittyapplication.model.ServerResponse;
import com.kittyapplication.providers.KittyBeeContract;
import com.kittyapplication.rest.Singleton;
import com.kittyapplication.sqlitedb.SQLConstants;
import com.kittyapplication.sqlitedb.SqlDataSetTask;
import com.kittyapplication.ui.activity.HomeActivity;
import com.kittyapplication.ui.activity.KittyDiaryActivity;
import com.kittyapplication.ui.activity.RuleActivity;
import com.kittyapplication.ui.activity.SettingActivity;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.DateTimeUtils;
import com.kittyapplication.utils.PreferanceUtils;
import com.kittyapplication.utils.Utils;
import com.quickblox.chat.QBGroupChat;
import com.quickblox.chat.QBGroupChatManager;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smackx.muc.DiscussionHistory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Pintu Riontech on 11/8/16.
 * vaghela.pintu31@gmail.com
 */
public class KittyRulesViewModel implements View.OnTouchListener, DateListener, TimePickerListener {
    private static final String TAG = KittyRulesViewModel.class.getSimpleName();
    private RuleActivity mActivity;
    private CreateGroup mGroupObject;
    private DialogDateTimePicker mDataPicker;
    private int mTimeSelection = 0;
    public boolean isSetRule = false;
    private String mGroupId;
    private boolean flag = false;
    private boolean isUpdate = false;
    private ChatData mChatData;
    private QBChatDialog mQbDialog;

    public KittyRulesViewModel(RuleActivity activity) {
        mActivity = activity;
    }

    public void getData(String str) {
        mGroupObject = new Gson().fromJson(str, CreateGroup.class);
        mActivity.setActionbarTitle(mGroupObject.getName());
        mActivity.setImageViewData(mGroupObject.getCategory());
    }

    /**
     *
     */
    public void submit() {
        WorkerThread mQbDialogWorkerThread = new WorkerThread("myWorkerThread");
        mQbDialogWorkerThread.start();
        mQbDialogWorkerThread.prepareHandler();
        mQbDialogWorkerThread.postTask(task);
    }

    /**
     *
     */
    public void submitDataToServer() {
        if (Utils.checkInternetConnection(mActivity)) {
            mActivity.showProgressDialog();

            final CreateGroup group = mActivity.getData();
            group.setGroupIMG(mActivity.getStringFromURIPath(mGroupObject.getGroupIMG()));
            group.setName(mGroupObject.getName());
            group.setCategory(mGroupObject.getCategory());
            group.setSetRule("1");
            group.setGroupMember(mGroupObject.getGroupMember());


            if (!mActivity.isFromChat()) {
                HashMap<String, String> map = new HashMap<>();
                for (int i = 0; i < group.getGroupMember().size(); i++) {
                    if (Utils.isValidString(group.getGroupMember().get(i).getID())) {
                        String ids = group.getGroupMember().get(i).getID();
                        String names = group.getGroupMember().get(i).getFullName();

                        if (ids.contains(AppConstant.SEPERATOR_STRING)) {
                            String[] idArray = ids.split(AppConstant.SEPERATOR_STRING);
                            String[] nameArray = names.split(AppConstant.SEPERATOR_STRING);
                            if (idArray != null && idArray.length > 0
                                    && nameArray != null && nameArray.length > 0) {
                                for (int j = 0; j < idArray.length; j++) {
                                    map.put(idArray[j], nameArray[j]);
                                }
                            }
                        } else {
                            map.put(group.getGroupMember().get(i).getID(),
                                    group.getGroupMember().get(i).getFullName());
                        }

                    }
                }

                QbDialogUtils.createGroupChatDialog(map,
                        group.getName()
                        , ""
                        , new QBGetGroupID() {
                            @Override
                            public void getQuickBloxGroupID(QBChatDialog dialog, String message) {
                                AppApplication.getInstance().setRefresh(true);
                                sendMessage(dialog, message);
                                mQbDialog = dialog;
                                group.setQuickGroupId(dialog.getDialogId());
                                Call<ServerResponse<OfflineDao>> call = Singleton.getInstance()
                                        .getRestOkClient().addGroup(
                                                PreferanceUtils.getLoginUserObject(mActivity)
                                                        .getUserID(), group);
                                call.enqueue(createGroupCallBack);
                            }

                            @Override
                            public void getError(Exception e) {
                                mActivity.hideProgressDialog();
                                mActivity.showSnackbar(mActivity.getString
                                        (R.string.quick_blox_error_found));
                            }
                        });
            } else {
                AppApplication.getInstance().setRefresh(true);
                group.setGroupID(mGroupObject.getGroupID());
                Call<ServerResponse<OfflineDao>> call = Singleton.getInstance()
                        .getRestOkClient().createKitty(group);
                call.enqueue(createKittyCallBack);
            }
        } else {
            mActivity.showSnackbar(mActivity.getResources().getString(R.string.no_internet_available));
        }
    }

    /**
     * @param dialog
     * @param message
     */
    private void sendMessage(final QBChatDialog dialog, final String message) {
        // TODO Join group and send message
        DiscussionHistory history = new DiscussionHistory();
        history.setMaxStanzas(0);

        ChatHelper.getInstance().join(dialog, new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                QBChatMessage chatMessage = new QBChatMessage();
                chatMessage.setBody(message);
                chatMessage.setProperty("save_to_history", "1"); // Save to Chat 2.0 history
                try {
                    dialog.sendMessage(chatMessage);
                } catch (SmackException.NotConnectedException | IllegalStateException e) {
                    AppLog.e(TAG, e.getMessage(), e);
                }
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });
    }


    /**
     * call back for create group or create kitty
     */
    private Callback<ServerResponse<OfflineDao>> createGroupCallBack = new Callback<ServerResponse<OfflineDao>>() {
        @Override
        public void onResponse(Call<ServerResponse<OfflineDao>> call, Response<ServerResponse<OfflineDao>> response) {
            mActivity.hideProgressDialog();
            if (response.code() == 200) {
                if (response.body().getResponse() == AppConstant.RESPONSE_SUCCESS) {

                    addChatData(response.body().getGroup());
                    // insert data into db
                    new SqlDataSetTask(mActivity, response.body().getGroup());

                    if (mActivity.isFromChat()) {
                        mActivity.showSnackbar(mActivity.getResources().getString(R.string.rule_update_success));
                    } else {
                        mActivity.showSnackbar(mActivity.getResources().getString(R.string.group_created_success_new));
                    }
                    finishActivity();
                } else {
                    AppApplication.getInstance().setRefresh(false);
                    mActivity.showSnackbar(mActivity.getResources().getString(R.string.server_error));
                    AppLog.e(TAG, "Response code = 0");
                    finishActivity();
                }
            } else {
                AppApplication.getInstance().setRefresh(false);
                mActivity.showSnackbar(mActivity.getResources().getString(R.string.server_error));
                AppLog.e(TAG, "HTTP Status code is not 200");
                finishActivity();
            }
        }

        @Override
        public void onFailure(Call<ServerResponse<OfflineDao>> call, Throwable t) {
            AppApplication.getInstance().setRefresh(false);
            mActivity.hideProgressDialog();
            mActivity.showSnackbar(mActivity.getResources().getString(R.string.server_error));
            finishActivity();
        }
    };

    private void addChatData(OfflineDao offlineDao) {
        try {
//            ChatDao dataObject = PreferanceUtils.getChatDataFromPreferance(mActivity);
            ChatData data = new ChatData();
            data.setMemberId(offlineDao.getMemberId());
            data.setHostId(offlineDao.getHostId());
            data.setRights(offlineDao.getRights());
            data.setGroupID(offlineDao.getGroupID());
            data.setQuickId(offlineDao.getQuickId());
            data.setName(offlineDao.getName());

            data.setCategory(offlineDao.getCategory());
            data.setGroupImage(offlineDao.getGroupImage());
            data.setHostName(offlineDao.getHostName());
            data.setHostnumber(offlineDao.getHostnumber());
            data.setIsHost(offlineDao.getIsHost());
            data.setIsAdmin(offlineDao.getIsAdmin());
            data.setIsVenue(offlineDao.getIsVenue());

            data.setNoOfHost(offlineDao.getNoOfHost());
            data.setKittyId(offlineDao.getKittyId());
            data.setKittyDate(offlineDao.getKittyDate());
            data.setKittyTime(offlineDao.getKittyTime());
            data.setPunctuality(offlineDao.getPunctuality());
            data.setPunctualityTime(offlineDao.getPunctualityTime());
            data.setPunctualityTime2(offlineDao.getPunctualityTime2());
            data.setRule(offlineDao.getRule());

            KittyViewModel model = new KittyViewModel(mActivity);
            model.saveGroup(data);
            model.saveQBDialog(mQbDialog);

            AppApplication.getInstance().setQbRefresh(true);
//            GroupPrefHolder.getInstance().addAt(data, 0);

            /*Kitty groupChat = new Kitty();
            groupChat.setGroup(data);
            groupChat.setQbDialog(QbDialogHolder.getInstance().getQBDialogByDialogId(data.getQuickId()));

            KittyPrefHolder.getInstance().addAt(groupChat, 0);*/
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    /**
     *
     */
    private void finishActivity() {
        Intent intent = new Intent(mActivity, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        mActivity.startActivity(intent);
        mActivity.finish();
    }

    public void setOnDatePickerOpenListener(EditText editText) {
        editText.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            switch (v.getId()) {
                case R.id.edtKittyRuleFirstKittyDate:
                    mDataPicker = new DialogDateTimePicker(mActivity);
                    mDataPicker.showDatePickerDialog(this);
                    return true;
                case R.id.edtKittyRuleFirstKittyTime:
                    mTimeSelection = 0;
                    mDataPicker = new DialogDateTimePicker(mActivity);
                    mDataPicker.showTimePickerDialog(this);
                    return true;
                case R.id.edtKittyRuleKittyPunctualityAmountTimeOne:
                    mTimeSelection = 1;
                    mDataPicker = new DialogDateTimePicker(mActivity);
                    mDataPicker.showTimePickerDialog(this);
                    return true;
                case R.id.edtKittyRuleKittyPunctualityAmountTimeTwo:
                    mTimeSelection = 2;
                    mDataPicker = new DialogDateTimePicker(mActivity);
                    mDataPicker.showTimePickerDialog(this);
                    return true;
            }
        }
        return false;
    }

    @Override
    public void getDate(String date) {
        try {
            if (DateTimeUtils.getSelectedDateIsAfterCurrentDate(date, true)) {
                mActivity.getKittyDate(date);
            } else {
                mActivity.showSnackbar(mActivity.getResources().getString(R.string.kitty_date_error_text));
            }
            mDataPicker = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getTime(String time) {
        try {
            Date date = DateTimeUtils.getPickerTimeFormat().parse(time);
            String formateTime = DateTimeUtils.getServerTimeFormat().format(date);

            EditText edtKittyTime = (EditText)
                    mActivity.findViewById(R.id.edtKittyRuleFirstKittyTime);

            EditText edtPunckTime = (EditText)
                    mActivity.findViewById(R.id.edtKittyRuleKittyPunctualityAmountTimeOne);

            EditText edtPunckTimeTwo = (EditText)
                    mActivity.findViewById(R.id.edtKittyRuleKittyPunctualityAmountTimeTwo);

            if (mTimeSelection == 0) {

                edtKittyTime.setText(formateTime);
            } else if (mTimeSelection == 1) {

                // validation for check punctual time is after kitty time
                if (DateTimeUtils.getSelectedTimeIsAfterParticularTime
                        (edtKittyTime.getText().toString(), date)) {
                    edtPunckTime.setText(formateTime);
                } else {
                    mActivity.showSnackbar(mActivity.getResources().getString(R.string.punctual_time_one_warning));
                }
            } else if (mTimeSelection == 2) {
                // validation for check punctual time two is after punctual time time
                if (DateTimeUtils
                        .getSelectedTimeIsAfterParticularTime
                                (edtPunckTime.getText().toString(), date)) {
                    edtPunckTimeTwo.setText(formateTime);
                } else {
                    mActivity.showSnackbar(mActivity.getResources().getString(R.string.punctual_time_two_warning));
                }
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public void getDateFromDayAndWeek(Spinner week, Spinner days) {
        mActivity.getKittyDate(DateTimeUtils.getFirstMonday(Calendar.getInstance().get(Calendar.YEAR),
                (Calendar.getInstance().get(Calendar.MONTH)), days.getSelectedItemPosition() + 1,
                week.getSelectedItemPosition() + 1));
    }

    public void getDiaryData(String data) {
        mChatData = new Gson().fromJson(data, ChatData.class);
        mActivity.setActionbarTitle(mChatData.getName());
        mActivity.hideSubmitButton(mChatData);
        AppLog.d(TAG, "CHAT DATA " + data);
        if (mChatData.getRule().equalsIgnoreCase("1")) {
            //Update req
            isSetRule = true;
            mGroupId = mChatData.getGroupID();
            mActivity.showProgressDialog();

            new SyncTask().execute(mGroupId);

        } else {
            //For No Rule Set
            mGroupObject = new CreateGroup();
            mGroupObject.setGroupIMG(mChatData.getGroupImage());
            mGroupObject.setName(mChatData.getName());
            mGroupObject.setCategory(mChatData.getCategory());
            mGroupObject.setSetRule("1");
            mGroupObject.setGroupID(mChatData.getGroupID());
            mGroupId = mChatData.getGroupID();
            mActivity.setImageViewData(mChatData.getCategory());
        }
    }


    private Callback<ServerResponse<List<CreateGroup>>> getRuleDataCallBack =
            new Callback<ServerResponse<List<CreateGroup>>>() {
                @Override
                public void onResponse(Call<ServerResponse<List<CreateGroup>>> call,
                                       Response<ServerResponse<List<CreateGroup>>> response) {
                    mActivity.hideProgressDialog();
                    if (response.code() == 200) {
                        if (response.body().getResponse() == AppConstant.RESPONSE_SUCCESS) {
                            if (response.body().getData().get(0) != null) {
                                mGroupObject = response.body().getData().get(0);
                                mActivity.setData(response.body().getData().get(0));
                                mActivity.disableHostAndKittyDate();
                                insertGroup(response.body().getData().get(0), false);
                                if (isUpdate) {
                                    mActivity.showSnackbar(mActivity.getResources().getString(R.string.rule_update_success));
                                    //if comes from diary data  than back to diary
                                    // if comes from setting than back to setting
                                    if (mActivity.isDiaryData()) {
                                        Intent intent = new Intent(mActivity, KittyDiaryActivity.class);
                                        intent.putExtra(AppConstant.INTENT_DIARY_DATA,
                                                new Gson().toJson(mChatData).toString());
                                        mActivity.startActivity(intent);
                                        mActivity.finish();
                                    } else if (mActivity.isFromSetting()) {
                                        Intent intent = new Intent(mActivity, SettingActivity.class);
                                        intent.putExtra(AppConstant.SETTING_DATA,
                                                new Gson().toJson(mChatData).toString());
                                        mActivity.startActivity(intent);
//                                        mActivity.startActivity(new Intent(mActivity, SettingActivity.class));
                                        mActivity.finish();
                                    }
                                    isUpdate = false;
                                }
                            }
                        } else {
                            mActivity.showSnackbar(mActivity.getResources().getString(R.string.server_error));
                            AppLog.e(TAG, "Response code = 0");
                        }
                    } else {
                        mActivity.showSnackbar(mActivity.getResources().getString(R.string.server_error));
                        AppLog.e(TAG, "HTTP Status code is not 200");
                    }
                }

                @Override
                public void onFailure(Call<ServerResponse<List<CreateGroup>>> call, Throwable t) {
                    mActivity.hideProgressDialog();
                    mActivity.showSnackbar(mActivity.getResources().getString(R.string.server_error));
                }
            };

    public void submitDiaryData() {
        CreateGroup group = mActivity.getData();
        group.setGroupIMG(mGroupObject.getGroupIMG());
        group.setName(mGroupObject.getName());
        group.setCategory(mGroupObject.getCategory());
        group.setSetRule("1");
        group.setGroupMember(mGroupObject.getGroupMember());
        AppLog.d(TAG, "Group Id " + mGroupObject.getGroupID() + "--" + mGroupId);
        group.setGroupID(mGroupId);
        if (Utils.checkInternetConnection(mActivity)) {
            mActivity.showProgressDialog();
            if (isSetRule) {
                AppApplication.getInstance().setRefresh(true);
                isUpdate = true;
                Call<ServerResponse<List<CreateGroup>>> call = Singleton.getInstance()
                        .getRestOkClient().updateRuleData(
                                mGroupId,
                                group);
                call.enqueue(updateRuleDataCallBack);
            } else {
                AppApplication.getInstance().setRefresh(false);
                Call<ServerResponse<OfflineDao>> call = Singleton.getInstance()
                        .getRestOkClient().createKitty(group);
                call.enqueue(createKittyCallBack);
            }
        } else {
            if (isSetRule) {
                insertGroup(group, true);

                if (mActivity.isFromSetting()) {
                    mActivity.startActivity(new Intent(mActivity, SettingActivity.class));
                } else if (mActivity.isDiaryData()) {
                    mActivity.startActivity(new Intent(mActivity, KittyDiaryActivity.class));
                }

                mActivity.showSnackbar(mActivity.getResources().getString(R.string.rule_update_success));
                mActivity.finish();
            } else {
                mActivity.showSnackbar(mActivity.getResources().getString(R.string.no_internet_available));
            }
        }
    }

    private List<String> getContactId(CreateGroup group) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < group.getGroupMember().size(); i++) {
            if (Utils.isValidString(group.getGroupMember().get(i).getID())) {
                String ids = group.getGroupMember().get(i).getID();
                String[] idArray = null;
                if (ids.contains("-!-")) {
                    idArray = ids.split("-!-");
                    list.addAll(Arrays.asList(idArray));
                } else {
                    list.add(group.getGroupMember().get(i).getID());
                }
            }
        }
        return list;
    }


    /**
     * Insert Venue
     */
    private void insertGroup(CreateGroup dao, boolean isSync) {
        ContentValues values = new ContentValues();
        AppLog.d(TAG, "IDS = " + mGroupId + " data " + new Gson().toJson(dao));
        values.put(SQLConstants.KEY_ID, mGroupId);
        values.put(SQLConstants.KEY_DATA, new Gson().toJson(dao));
        if (isSync) {
            values.put(SQLConstants.KEY_IS_SYNC, 1);
        }
        mActivity.getContentResolver().insert(KittyBeeContract.Rules.CONTENT_URI, values);
    }


    /**
     *
     */
    private class SyncTask extends AsyncTask<String, Void, CreateGroup> {
        @Override
        protected CreateGroup doInBackground(String... params) {
            CreateGroup responseDao = null;
            Uri uri = ContentUris.withAppendedId(KittyBeeContract.Rules.CONTENT_URI,
                    Long.valueOf(params[0]));
            String selection = SQLConstants.KEY_ID + "=?";
            String[] selectionArgs = {params[0]};

            AppLog.d(TAG, "URI " + uri);

            ContentResolver resolver =
                    mActivity.getContentResolver();
            Cursor cursor = resolver.query(
                    KittyBeeContract.Rules.CONTENT_URI,                      // the URI to query
                    KittyBeeContract.Rules.PROJECTION_ALL,// the projection to use
                    selection,                           // the where clause without the WHERE keyword
                    selectionArgs,                           // any wildcard substitutions
                    null);                          // the sort order without the SORT BY keyword

            if (cursor != null && cursor.getCount() > 0) {
                // show data from db
                Gson gson = new Gson();
                while (cursor.moveToNext()) {
                    AppLog.d(TAG, "String Cursour" + cursor.getString(1));
                    responseDao = gson.fromJson(cursor.getString(1), CreateGroup.class);
                }


                cursor.close();
            }
            return responseDao;
        }

        @Override
        protected void onPostExecute(CreateGroup createGroup) {
            super.onPostExecute(createGroup);
            AppLog.d(TAG, new Gson().toJson(createGroup).toString());
//            MySQLiteHelper.getInstance(mActivity).close();
            if (createGroup != null) {
                flag = true;
                mGroupObject = createGroup;
                mActivity.setData(createGroup);
                mActivity.disableHostAndKittyDate();
                mActivity.hideProgressDialog();
            }

            if (Utils.checkInternetConnection(mActivity)) {
                Call<ServerResponse<List<CreateGroup>>> call = Singleton.getInstance()
                        .getRestOkClient().getRuleData(mGroupId);
                call.enqueue(getRuleDataCallBack);
            } else {
                if (!flag) {
                    mActivity.hideProgressDialog();
                    mActivity.showSnackbar(mActivity.getResources().getString(R.string.no_internet_available));
                }
            }
        }
    }


    private Callback<ServerResponse<List<CreateGroup>>> updateRuleDataCallBack =
            new Callback<ServerResponse<List<CreateGroup>>>() {
                @Override
                public void onResponse(Call<ServerResponse<List<CreateGroup>>> call,
                                       Response<ServerResponse<List<CreateGroup>>> response) {
                    mActivity.hideProgressDialog();
                    if (response.code() == 200) {
                        if (response.body().getResponse() == AppConstant.RESPONSE_SUCCESS) {
                            if (response.body().getData() != null) {
                                mGroupObject = response.body().getData().get(0);
                                mActivity.setData(response.body().getData().get(0));
                                mActivity.disableHostAndKittyDate();
                                insertGroup(response.body().getData().get(0), false);

                                AppLog.d(TAG, "Data = " + new Gson().toJson(response.body().getGroupData()));
                                // insert data into db
                                new SqlDataSetTask(mActivity, response.body().getGroupData());

                                if (isUpdate) {
                                    mActivity.showSnackbar(mActivity.getResources().getString(R.string.rule_update_success));
                                    //if comes from diary data  than back to diary
                                    // if comes from setting than back to setting
                                    if (mActivity.isDiaryData()) {
                                        Intent intent = new Intent(mActivity, KittyDiaryActivity.class);
                                        intent.putExtra(AppConstant.INTENT_DIARY_DATA,
                                                new Gson().toJson(mChatData).toString());
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION
                                                | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                        mActivity.startActivity(intent);
                                        mActivity.finish();
                                    } else if (mActivity.isFromSetting()) {
                                        Intent intent = new Intent(mActivity, SettingActivity.class);
                                        intent.putExtra(AppConstant.SETTING_DATA,
                                                new Gson().toJson(mChatData).toString());
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION
                                                | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                        mActivity.startActivity(intent);
//                                        mActivity.startActivity(new Intent(mActivity, SettingActivity.class));
                                        mActivity.finish();
                                    }
                                    isUpdate = false;
                                }
                            }
                        } else {
                            AppApplication.getInstance().setRefresh(false);
                            mActivity.showSnackbar(mActivity.getResources().getString(R.string.server_error));
                            AppLog.e(TAG, "Response code = 0");
                        }
                    } else {
                        AppApplication.getInstance().setRefresh(false);
                        mActivity.showSnackbar(mActivity.getResources().getString(R.string.server_error));
                        AppLog.e(TAG, "HTTP Status code is not 200");
                    }
                }

                @Override
                public void onFailure(Call<ServerResponse<List<CreateGroup>>> call, Throwable t) {
                    AppApplication.getInstance().setRefresh(false);
                    try {
                        mActivity.hideProgressDialog();
                        mActivity.showSnackbar(mActivity.getResources().getString(R.string.server_error));
                        AppLog.d(TAG, t.getMessage());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };


    /**
     * call back for create group or create kitty
     */
    private Callback<ServerResponse<OfflineDao>> createKittyCallBack = new Callback<ServerResponse<OfflineDao>>() {
        @Override
        public void onResponse(Call<ServerResponse<OfflineDao>> call, Response<ServerResponse<OfflineDao>> response) {
            mActivity.hideProgressDialog();
            if (response.code() == 200) {
                if (response.body().getResponse() == AppConstant.RESPONSE_SUCCESS) {

                    // insert data into db
                    new SqlDataSetTask(mActivity, response.body().getData());

                    if (mActivity.isFromChat()) {
                        mActivity.showSnackbar(mActivity.getResources().getString(R.string.rule_update_success));
                    } else {
                        mActivity.showSnackbar(mActivity.getResources().getString(R.string.group_created_success_new));
                    }
                    finishActivity();
                } else {
                    AppApplication.getInstance().setRefresh(false);
                    mActivity.showSnackbar(mActivity.getResources().getString(R.string.server_error));
                    AppLog.e(TAG, "Response code = 0");
                    finishActivity();
                }
            } else {
                AppApplication.getInstance().setRefresh(false);
                mActivity.showSnackbar(mActivity.getResources().getString(R.string.server_error));
                AppLog.e(TAG, "HTTP Status code is not 200");
                finishActivity();
            }
        }

        @Override
        public void onFailure(Call<ServerResponse<OfflineDao>> call, Throwable t) {
            AppApplication.getInstance().setRefresh(false);
            mActivity.hideProgressDialog();
            mActivity.showSnackbar(mActivity.getResources().getString(R.string.server_error));
            finishActivity();
        }
    };

    private class WorkerThread extends HandlerThread {

        private Handler mWorkerHandler;


        public WorkerThread(String name) {
            super(name);
        }

        public void postTask(Runnable task) {
            mWorkerHandler.post(task);
        }

        public void prepareHandler() {
            mWorkerHandler = new Handler(getLooper());
        }

    }

    /**
     *
     */
    Runnable task = new Runnable() {
        @Override
        public void run() {
            submitDataToServer();
        }
    };
}
