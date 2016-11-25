package com.kittyapplication.ui.viewmodel;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.kittyapplication.AppApplication;
import com.kittyapplication.R;
import com.kittyapplication.adapter.AddGroupContactAdapter;
import com.kittyapplication.adapter.AddMemberInGroupAdapter;
import com.kittyapplication.chat.utils.ImageLoaderUtils;
import com.kittyapplication.chat.utils.qb.QbDialogUtils;
import com.kittyapplication.chat.utils.qb.callback.QBGetGroupID;
import com.kittyapplication.custom.AddKittyRulesListener;
import com.kittyapplication.custom.CustomEditTextNormal;
import com.kittyapplication.custom.ImagePickerDialog;
import com.kittyapplication.custom.MiddleOfKittyListener;
import com.kittyapplication.listener.GetImageFromListener;
import com.kittyapplication.listener.ImagePickerListener;
import com.kittyapplication.model.ChatData;
import com.kittyapplication.model.ContactDao;
import com.kittyapplication.model.CreateGroup;
import com.kittyapplication.model.OfflineDao;
import com.kittyapplication.model.ServerResponse;
import com.kittyapplication.rest.Singleton;
import com.kittyapplication.sqlitedb.SqlDataSetTask;
import com.kittyapplication.ui.activity.AddGroupActivity;
import com.kittyapplication.ui.activity.HomeActivity;
import com.kittyapplication.ui.activity.KittyWithKidsActivity;
import com.kittyapplication.ui.activity.RuleActivity;
import com.kittyapplication.ui.activity.SelectCoupleActivity;
import com.kittyapplication.ui.activity.SelectHostActivity;
import com.kittyapplication.utils.AlertDialogUtils;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.GroupPrefHolder;
import com.kittyapplication.utils.PreferanceUtils;
import com.kittyapplication.utils.Utils;
import com.quickblox.chat.QBGroupChat;
import com.quickblox.chat.QBGroupChatManager;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smackx.muc.DiscussionHistory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Pintu Riontech on 10/8/16.
 * vaghela.pintu31@gmail.com
 */
public class AddGroupViewModel implements AddKittyRulesListener,
        MiddleOfKittyListener, ImagePickerListener {
    private static final String TAG = AddGroupViewModel.class.getSimpleName();

    private AddGroupActivity mActivity;
    private AddGroupContactAdapter mContactAdapter;
    private CreateGroup mGroupObject;
    public ImagePickerDialog mImagePickerDialog;
    private List<ContactDao> mList;


    public AddGroupViewModel(AddGroupActivity activity) {
        mActivity = activity;
        mGroupObject = new CreateGroup();
        getContactList();
        addUserObjectIntoList();
    }


    private void addUserObjectIntoList() {
        ContactDao userObeject = new ContactDao();
        userObeject.setID(PreferanceUtils.getLoginUserObject(mActivity).getQuickID());
        userObeject.setName(PreferanceUtils.getLoginUserObject(mActivity).getName());
        userObeject.setUserId(PreferanceUtils.getLoginUserObject(mActivity).getUserID());
        userObeject.setImage(PreferanceUtils.getLoginUserObject(mActivity).getProfilePic());
        userObeject.setFullName(PreferanceUtils.getLoginUserObject(mActivity).getFullName());
        userObeject.setPhone(PreferanceUtils.getLoginUserObject(mActivity).getPhone());
        mGroupObject.getGroupMember().add(userObeject);
    }

    private void getContactList() {
        try {
            if (checkCallPermission()) {
                mList = PreferanceUtils.getContactListFromPreferance(mActivity).getContactList();
                if (mList != null && !mList.isEmpty()) {
                    mActivity.setDataIntoList(mList);

                } else {
                    mActivity.findViewById(R.id.rvAddGroupMember).setVisibility(View.GONE);
                    mActivity.findViewById(R.id.txtNoContactFoundAddGroup).setVisibility(View.VISIBLE);
                }
            } else {
                mActivity.findViewById(R.id.rvAddGroupMember).setVisibility(View.GONE);
                mActivity.findViewById(R.id.txtNoContactFoundAddGroup).setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            mActivity.findViewById(R.id.rvAddGroupMember).setVisibility(View.GONE);
            mActivity.findViewById(R.id.txtNoContactFoundAddGroup).setVisibility(View.VISIBLE);

        }
    }

    public void setAdapter(AddGroupContactAdapter adapter) {
        mContactAdapter = adapter;
    }

    public void clickOnNextButton(AddMemberInGroupAdapter adapter,
                                  String kittyType,
                                  EditText edtGroupName) {
        try {
            List<ContactDao> list = adapter.getSelectedList();
            if (Utils.isValidText(edtGroupName,
                    mActivity.getResources().getString(R.string.group_name))) {
                if (!list.isEmpty()) {
                    mGroupObject.setCategory(kittyType);
                    mGroupObject.setName(Utils.getText(edtGroupName));
                    mGroupObject.setSetRule("0");
                    mGroupObject.getGroupMember().clear();
                    addUserObjectIntoList();
                    mGroupObject.getGroupMember().addAll(list);
                    //                if (!mGroupObject.getGroupMember().containsAll(list)) {
                    //                    addMember(list);
                    //                }


//                    AlertDialogUtils.showAddKittyRulesDialog(mActivity, this);

                    AlertDialogUtils.showMiddleOfKittyDialog(mActivity, this);
                } else {
                    mActivity.showSnackbar(mActivity.getResources().getString(R.string.select_min_members));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void clickOnLater() {
        if (Utils.checkInternetConnection(mActivity)) {
            mGroupObject.setGroupIMG(mActivity.getStringFromURIPath(mGroupObject.getGroupIMG()));
            mActivity.showProgressDialog();
            WorkerThread mQbDialogWorkerThread = new WorkerThread("myWorkerThread");
            mQbDialogWorkerThread.start();
            mQbDialogWorkerThread.prepareHandler();
            mQbDialogWorkerThread.postTask(task);

        } else {
            mActivity.showSnackbar(mActivity.getResources().getString(R.string.no_internet_available));
        }
    }

    private void addGroupApiCall() {
        HashMap<String, String> map = new HashMap<>();
        for (int i = 0; i < mGroupObject.getGroupMember().size(); i++) {
            if (Utils.isValidString(mGroupObject.getGroupMember().get(i).getID())) {
                map.put(mGroupObject.getGroupMember().get(i).getID(),
                        mGroupObject.getGroupMember().get(i).getFullName());
            }
        }

        QbDialogUtils.createGroupChatDialog(map,
                mGroupObject.getName()
                , ""
                , new QBGetGroupID() {
                    @Override
                    public void getQuickBloxGroupID(QBDialog dialog, String message, QBGroupChatManager groupChatManager) {
                        sendMessage(dialog.getRoomJid(), message, groupChatManager);
                        AppApplication.getInstance().setRefresh(true);
                        mGroupObject.setQuickGroupId(dialog.getDialogId());
                        Call<ServerResponse<OfflineDao>> call = Singleton.getInstance()
                                .getRestOkClient().addGroup(
                                        PreferanceUtils.getLoginUserObject(mActivity)
                                                .getUserID(), mGroupObject);
                        call.enqueue(getGroupCallback);
                    }

                    @Override
                    public void getError(Exception e) {
                        mActivity.hideProgressDialog();
                        mActivity.showSnackbar(mActivity.getString(R.string.quick_blox_error_found));
                    }
                });
    }

    /**
     * @param roomID
     * @param message
     * @param groupChatManager
     */
    private void sendMessage(String roomID, String message, QBGroupChatManager groupChatManager) {
        // TODO Join group and send message
        DiscussionHistory history = new DiscussionHistory();
        history.setMaxStanzas(0);

        final QBGroupChat currentChatRoom = groupChatManager.
                createGroupChat(roomID);
        final String finalMessage = message;
        currentChatRoom.join(history, new QBEntityCallback() {
            @Override
            public void onSuccess(Object o, Bundle bundle) {
                QBChatMessage chatMessage = new QBChatMessage();
                chatMessage.setBody(finalMessage);
                chatMessage.setProperty("save_to_history", "1"); // Save to Chat 2.0 history
                try {
                    currentChatRoom.sendMessage(chatMessage);
                } catch (SmackException.NotConnectedException | IllegalStateException e) {
                    AppLog.e(TAG, e.getMessage(), e);
                }
            }

            @Override
            public void onError(QBResponseException errors) {
            }
        });
    }

    @Override
    public void clickOnNow() {
        AlertDialogUtils.showMiddleOfKittyDialog(mActivity, this);
    }

    //kitty TYPE 0 (COUPLE KITTY)
    //kitty TYPE 1 (KITTY WITH KIDS)
    //kitty TYPE 2 (NORMAL KITTY)
    //kitty TYPE 4 (COMING KITTY FROM ADD MEMBER WHICH IS KITTY WITH KIDS WITHOUT PAID)
    //kitty TYPE 5 (COMING KITTY FROM ADD MEMBER WHICH IS KITTY WITH KIDS WITH PAID)
    //kitty TYPE 6 (COMING KITTY FROM ADD MEMBER WHICH IS COUPLE KITTY)


    @Override
    public void clickOnYes() {

        if (mGroupObject.getCategory().equalsIgnoreCase(mActivity.getResources()
                .getStringArray(R.array.kitty)[0])) {
            if (mGroupObject.getGroupMember().size() > 0) {
                Intent i = new Intent(mActivity, SelectCoupleActivity.class);
                i.putExtra(AppConstant.INTENT_KITTY_DATA, new Gson().toJson(mGroupObject).toString());
                i.putExtra(AppConstant.INTENT_KITTY_TYPE, 0);
                i.putExtra(AppConstant.INTENT_KITTY_IS_COUPLE_HOST, 0);
                mActivity.startActivity(i);
            } else {
                mActivity.showSnackbar(mActivity.getResources().getString(R.string.kitty_couple_one_warning));
            }

        } else if (mGroupObject.getCategory().equalsIgnoreCase(mActivity.getResources()
                .getStringArray(R.array.kitty)[1])) {
            Intent i = new Intent(mActivity, SelectHostActivity.class);
            i.putExtra(AppConstant.INTENT_KITTY_DATA, new Gson().toJson(mGroupObject).toString());
            i.putExtra(AppConstant.INTENT_KITTY_TYPE, 1);
            mActivity.startActivity(i);
        } else {
            Intent i = new Intent(mActivity, SelectHostActivity.class);
            i.putExtra(AppConstant.INTENT_KITTY_DATA, new Gson().toJson(mGroupObject).toString());
            i.putExtra(AppConstant.INTENT_KITTY_TYPE, 2);
            mActivity.startActivity(i);
        }


    }

    @Override
    public void clickOnNo() {
        if (mGroupObject.getCategory().equalsIgnoreCase(mActivity.getResources().getStringArray(R.array.kitty)[0])) {
            Intent i = new Intent(mActivity, SelectCoupleActivity.class);
            i.putExtra(AppConstant.INTENT_KITTY_DATA, new Gson().toJson(mGroupObject).toString());
            i.putExtra(AppConstant.INTENT_KITTY_TYPE, 0);
            mActivity.startActivity(i);
            AppLog.d(TAG, new Gson().toJson(mGroupObject).toString());

        } else if (mGroupObject.getCategory().equalsIgnoreCase(mActivity.getResources().getStringArray(R.array.kitty)[1])) {

            Intent i = new Intent(mActivity, KittyWithKidsActivity.class);
            i.putExtra(AppConstant.INTENT_KITTY_DATA, new Gson().toJson(mGroupObject).toString());
            i.putExtra(AppConstant.INTENT_KITTY_TYPE, 1);
            mActivity.startActivity(i);

        } else {
            Intent i = new Intent(mActivity, RuleActivity.class);
            i.putExtra(AppConstant.INTENT_KITTY_DATA, new Gson().toJson(mGroupObject).toString());
            i.putExtra(AppConstant.INTENT_KITTY_TYPE, 2);
            mActivity.startActivity(i);
        }
    }

    public void getSearchView(final AddMemberInGroupAdapter adapter, boolean isVisibleSearch) {
        CustomEditTextNormal edtSearch = (CustomEditTextNormal)
                mActivity.findViewById(R.id.edtSearchGroupMember);
        if (isVisibleSearch) {
            edtSearch.setVisibility(View.VISIBLE);
            mActivity.hideBottomLayout();
            mActivity.findViewById(R.id.txtAddGroupNext).setVisibility(View.GONE);

            setImeiOption(edtSearch);
            edtSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    //adapter.getFilter().filter(s);
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    adapter.getFilter().filter(s);
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        } else {
            edtSearch.setVisibility(View.GONE);
            mActivity.showBottomLayout();
            mActivity.findViewById(R.id.txtAddGroupNext).setVisibility(View.VISIBLE);
            adapter.reloadData();
        }
    }


    public void setImeiOption(final EditText edt) {
        edt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    //((View) v.getTag()).setFocusable(true);
                    Utils.hideKeyboard(mActivity, edt);
                    v.setVisibility(View.GONE);
                    mActivity.showBottomLayout();
                    mActivity.findViewById(R.id.txtAddGroupNext).setVisibility(View.VISIBLE);
                    if (mActivity.getAdapter() != null)
                        getSearchView(mActivity.getAdapter(), false);
                }
                return false;
            }
        });
    }


    private Callback<ServerResponse<OfflineDao>> getGroupCallback = new Callback<ServerResponse<OfflineDao>>() {
        @Override
        public void onResponse(Call<ServerResponse<OfflineDao>> call, Response<ServerResponse<OfflineDao>> response) {
            mActivity.hideProgressDialog();
            AppLog.d(TAG, "response code " + response.code());
            if (response.code() == 200) {
                if (response.body().getResponse() == AppConstant.RESPONSE_SUCCESS) {

                    addChatData(response.body().getGroup());

                    // insert data into db
                    new SqlDataSetTask(mActivity, response.body().getGroup());


                    mActivity.showSnackbar(response.body().getMessage());
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
            AppLog.e(TAG, t.getMessage());
            finishActivity();
            mActivity.showSnackbar(mActivity.getResources().getString(R.string.server_error));
        }
    };

    private void finishActivity() {
        Intent intent = new Intent(mActivity, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        AppLog.d(TAG, "Group created on server");
        mActivity.startActivity(intent);
        mActivity.finish();
    }

    @Override
    public void getBitmapImageFromPhone(Bitmap image) {
        ImageView imgGroupIcon = (ImageView) mActivity.findViewById(R.id.imgAddGroupUser);
        RoundedBitmapDrawable drawable = ImageLoaderUtils.getCircleBitmapDrawable(image);
//        imgGroupIcon.setImageBitmap(image);
        imgGroupIcon.setImageDrawable(drawable);
        AppLog.d(TAG, Utils.getImageInString(image));
        mGroupObject = new CreateGroup();
        mGroupObject.setGroupIMG(mImagePickerDialog.getPath());
        mImagePickerDialog = null;
    }

    public void setGroupImageIcon() {
        mImagePickerDialog = new ImagePickerDialog(mActivity, this);
        AlertDialogUtils.showImagePickerDialog(mActivity, new GetImageFromListener() {
            @Override
            public void getImageFrom(int type) {
                mImagePickerDialog.getImagesFrom(type);
            }
        });
    }

    private void addMember(List<ContactDao> list) {
        for (int i = 0; i < list.size(); i++) {
            if (!mGroupObject.getGroupMember().contains(list.get(i))) {
                mGroupObject.getGroupMember().add(list.get(i));
            }
        }
    }

    private List<String> getContactId(CreateGroup group) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < group.getGroupMember().size(); i++) {
            if (Utils.isValidString(group.getGroupMember().get(i).getID())) {
                list.add(group.getGroupMember().get(i).getID());
            }
        }
        return list;
    }


    private boolean checkCallPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(mActivity,
                    Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }


    private void addChatData(OfflineDao offlineDao) {
//        ArrayList<ChatData> groupList = GroupPrefHolder.getInstance().getGroupList(); //PreferanceUtils.getChatDataFromPreferance(mActivity);
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

        GroupPrefHolder.getInstance().addAt(data, 0);

        /*Kitty groupChat = new Kitty();
        groupChat.setGroup(data);
        groupChat.setQbDialog(QbDialogHolder.getInstance().getQBDialogByDialogId(data.getQuickId()));

        KittyPrefHolder.getInstance().addAt(groupChat, 0);*/
    }


    private class SendMessageThread extends HandlerThread {

        private Handler mWorkerHandler;


        public SendMessageThread(String name) {
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
    private class Task implements Runnable {

        final QBDialog dialog;
        final String message;
        final QBGroupChatManager groupChatManager;

        Task(QBDialog dg, String msg, QBGroupChatManager gManager) {
            dialog = dg;
            message = msg;
            groupChatManager = gManager;
        }

        @Override
        public void run() {
            DiscussionHistory history = new DiscussionHistory();
            history.setMaxStanzas(0);

            final QBGroupChat currentChatRoom = groupChatManager.
                    createGroupChat(dialog.getRoomJid());
            final String finalMessage = message;
            currentChatRoom.join(history, new QBEntityCallback() {
                @Override
                public void onSuccess(Object o, Bundle bundle) {
                    QBChatMessage chatMessage = new QBChatMessage();
                    chatMessage.setBody(finalMessage);
                    chatMessage.setProperty("save_to_history", "1");
                    try {
                        currentChatRoom.sendMessage(chatMessage);
                    } catch (SmackException.NotConnectedException | IllegalStateException e) {
                        AppLog.e(TAG, e.getMessage(), e);
                    }
                }

                @Override
                public void onError(QBResponseException errors) {
                }
            });
        }
    }


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
            addGroupApiCall();
        }
    };
}
