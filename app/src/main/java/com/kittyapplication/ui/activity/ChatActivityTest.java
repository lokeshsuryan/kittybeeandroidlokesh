/*
 * Copyright (c) 2016 riontech-xten
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kittyapplication.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.kittyapplication.R;
import com.kittyapplication.chat.ui.adapter.ChatMessageAdapter;
import com.kittyapplication.chat.ui.models.QBMessage;
import com.kittyapplication.chat.utils.Consts;
import com.kittyapplication.chat.utils.ImageLoaderUtils;
import com.kittyapplication.chat.utils.SharedPreferencesUtil;
import com.kittyapplication.chat.utils.chat.ChatHelper;
import com.kittyapplication.chat.utils.chat.QbChatDialogMessageListenerImp;
import com.kittyapplication.chat.utils.qb.FileUploadUtils;
import com.kittyapplication.chat.utils.qb.PaginationHistoryListener;
import com.kittyapplication.chat.utils.qb.QBUserUtils;
import com.kittyapplication.chat.utils.qb.VerboseQbChatConnectionListener;
import com.kittyapplication.chat.utils.qb.callback.OnFileUploadListener;
import com.kittyapplication.core.async.BaseAsyncTask;
import com.kittyapplication.core.utils.ConnectivityUtils;
import com.kittyapplication.core.utils.ImageUtils;
import com.kittyapplication.core.utils.ResourceUtils;
import com.kittyapplication.core.utils.Toaster;
import com.kittyapplication.core.utils.imagepick.GetFilepathFromUriTask;
import com.kittyapplication.core.utils.imagepick.OnImagePickedListener;
import com.kittyapplication.listener.QBChatMessageServiceListener;
import com.kittyapplication.model.Kitty;
import com.kittyapplication.model.RegisterResponseDao;
import com.kittyapplication.model.storeg.ChatGroupMember;
import com.kittyapplication.sqlitedb.DBQueryHandler;
import com.kittyapplication.sync.ChatSyncOperation;
import com.kittyapplication.sync.callback.DataSyncListener;
import com.kittyapplication.ui.viewmodel.MainChatViewModel;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.PreferanceUtils;
import com.kittyapplication.utils.QBRestMessageService;
import com.kittyapplication.utils.Utils;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBMessageStatusesManager;
import com.quickblox.chat.listeners.QBChatDialogMessageSentListener;
import com.quickblox.chat.listeners.QBMessageStatusListener;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import static com.kittyapplication.utils.AppConstant.EXTRA_CLICKED_ITEM_INDEX;

/**
 * Created by MIT on 8/20/2016.
 */
public class ChatActivityTest extends ChatBaseActivity implements OnImagePickedListener,
        OnFileUploadListener, QBChatMessageServiceListener {

    private static final String TAG = ChatActivityTest.class.getSimpleName();
    private static final int REQUEST_CODE_SELECT_PEOPLE = 752;
    protected static final String EXTRA_QBUSER = "qbuser";
    private static final String PROPERTY_SAVE_TO_HISTORY = "save_to_history";
    private static final String SENT_MESSAGE_INDEX = "sent_msg_index";
    public static final String EXTRA_MARK_READ = "markRead";
    public static final String EXTRA_KITTY_ID = "kittyId";
    private static final String EXTRA_DIALOG = "dialog";

    private ProgressBar progressBar;
    private StickyListHeadersListView messagesListView;
    private EditText messageEditText;


    private ChatMessageAdapter chatAdapter;
    private ConnectionListener chatConnectionListener;

    //    private Chat chat;
    private QBChatDialog qbDialog;
    private ArrayList<String> chatMessageIds;

    private ArrayList<String> mediaList;
    private MainChatViewModel viewModel;
    //    private MenuItem mMenuItemDiary, mMenuItemProfile, mMenuItemSetting, mMenuItemMedia, mMenuItemRules;
    private NetworkStateUpdateReceiver networkStateUpdateReceiver;
    private int kittyId;
    private HashMap<Integer, ChatGroupMember> members;
    private ArrayList<String> offlineMsgList;
    private int skipPagination = ChatHelper.CHAT_HISTORY_ITEMS_PER_PAGE;
    private QBRestMessageService messageService;

    public static void startForResult(Activity activity, int code,
                                      int kittyId, QBUser qbUser, int position) {
        try {
            Intent intent = new Intent(activity, ChatActivityTest.class);
            intent.putExtra(EXTRA_KITTY_ID, kittyId);
            intent.putExtra(EXTRA_QBUSER, qbUser);
            intent.putExtra(EXTRA_CLICKED_ITEM_INDEX, position);
            activity.startActivityForResult(intent, code);
        } catch (ActivityNotFoundException e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            View view = LayoutInflater.from(this).inflate(R.layout.activity_chat, null);
            addLayoutToContainer(view);
            hideBottomLayout();
            hideLeftIcon();

            view.findViewById(R.id.layout_chat_container).requestFocus();

            offlineMsgList = new ArrayList<>();
            viewModel = new MainChatViewModel(this);
            mediaList = new ArrayList<>();
            kittyId = getIntent().getIntExtra(EXTRA_KITTY_ID, 0);
            chatMessageIds = new ArrayList<>();

            messagesListView = _findViewById(R.id.list_chat_messages);
            messageEditText = _findViewById(R.id.edit_chat_message);
            progressBar = _findViewById(R.id.progress_chat);
            progressBar.setVisibility(View.VISIBLE);
            ImageView imgbg = (ImageView) view.findViewById(R.id.imgChatBg);
            com.kittyapplication.utils.ImageUtils.getImageLoader(this).displayImage("drawable://" + R.drawable.login_screen_bg, imgbg);

            messageService = QBRestMessageService.getMessageService();

            bindView(new ArrayList<QBMessage>());
            loadData();
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }

        //getMediaListFromServer();
    }

    private void loadData() {
        if (kittyId > 0) {
            viewModel.fetchKittyById(kittyId, new DBQueryHandler.OnQueryHandlerListener<ArrayList<Kitty>>() {
                @Override
                public void onResult(ArrayList<Kitty> result) {
                    System.out.print(result.get(0).toString());
                    if (result.size() > 0) {
                        Kitty kitty = result.get(0);
                        qbDialog = kitty.getQbDialog();
//                        addChatListener();
                        initDialogForChat();
                        initChatView();
//                        qbDialog.initForChat(QBChatService.getInstance());
                    }
                }
            });
        } else {
            createDialog();
        }
    }

    private void loadMemberProfile() {
        members = new HashMap<>();
        if (qbDialog != null) {
            for (Integer integer : qbDialog.getOccupants()) {
                if (!integer.equals(SharedPreferencesUtil.getQbUser().getId()))
                    setMessageSenderInfo(integer);
            }
        } else {
            QBUser user = (QBUser) getIntent().getSerializableExtra(EXTRA_QBUSER);
            setMessageSenderInfo(user.getId());
        }
    }

    private void initChatView() {
//        initChat();
        setChatNameToActionBar();
        loadMemberProfile();
        bindData();
    }

    private void registerNetworkStateReceiver() {
        networkStateUpdateReceiver = new NetworkStateUpdateReceiver();
        registerReceiver(networkStateUpdateReceiver, new IntentFilter(AppConstant.NETWORK_STAT_ACTION));
    }

//    private void initQBSetting() {
//        System.out.println("initQBSetting");
//        if (QBChatService.getInstance().getGroupChatManager() != null) {
//            init();
//        } else {
//            loginOnQb();
//        }
//    }

    private void bindData() {
        if (qbDialog != null) {
            viewModel.loadMessages(kittyId, chatAdapter.getCount(),
                    new DBQueryHandler.OnQueryHandlerListener<ArrayList<QBMessage>>() {
                        @Override
                        public void onResult(ArrayList<QBMessage> result) {
                            if (result != null && result.size() > 0) {
                                bindData(result);
                            } else {
                                syncMessages();
                            }
                        }
                    });
        }
    }

    private void syncMessages() {
        if (Utils.checkInternetConnection(this)) {
            AppLog.e(TAG, "syncMessages: start");
            ChatSyncOperation operation = new ChatSyncOperation(this);
            operation.syncDialogMessages(kittyId, qbDialog, chatAdapter.getCount(), new DataSyncListener() {
                @Override
                public void onCompleted(int itemCount) {
                    AppLog.e(TAG, "syncMessages: onCompleted(" + itemCount + ")");
                    if (itemCount > 0)
                        bindData();
                }
            });
        } else {
            AppLog.e(TAG, "syncMessages: chat is null");
        }
    }

//    private void loginOnQb() {
//        if (ConnectivityUtils.checkInternetConnection(this)) {
//            ChatHelper.getInstance().login(SharedPreferencesUtil.getQbUser(), new QBEntityCallback<Void>() {
//                @Override
//                public void onSuccess(Void aVoid, Bundle bundle) {
//                    init();
//                }
//
//                @Override
//                public void onError(QBResponseException e) {
//                }
//            });
//        } else {
//            init();
//        }
//    }

//    private void init() {
////        initChatConnectionListener();
//
//        if (qbDialog == null) {
//            createDialog();
//        } else {
//            qbDialog.setUnreadMessageCount(0);
//            initChat();
//        }
//    }

    private void createDialog() {
        try {
            QBUser user = (QBUser) getIntent().getSerializableExtra(EXTRA_QBUSER);
            if (user.getId() != null) {
                createPrivateChatDialog(user);
            } else {
                progressBar.setVisibility(View.GONE);
                Toaster.longToast("User not found");
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    private void createPrivateChatDialog(QBUser user) {
        final List<QBUser> users = new ArrayList<>();
        users.add(user);
        users.add(SharedPreferencesUtil.getQbUser());

        viewModel.getPrivateDialogWithUser(user.getId(), new DBQueryHandler.OnQueryHandlerListener<ArrayList<Kitty>>() {
            @Override
            public void onResult(ArrayList<Kitty> result) {
                if (!result.isEmpty()) {
                    Kitty kitty = result.get(0);
                    qbDialog = kitty.getQbDialog();
//                    addChatListener();
                    initDialogForChat();
                    kittyId = kitty.getId();
                    findViewById(R.id.btnSend).setEnabled(true);
                    initChatView();
                } else {
                    createPrivateDialog(users);
                }
            }
        });
    }

    private void createPrivateDialog(List<QBUser> users) {
        ChatHelper.getInstance().createDialogWithSelectedUsers(users,
                new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog qbDialog, Bundle bundle) {
                        try {
                            String dialog = new Gson().toJson(qbDialog);
                            AppLog.d(TAG, "dialog::" + dialog);
                            qbDialog.setLastMessageDateSent(System.currentTimeMillis() / 1000);
                            ChatActivityTest.this.qbDialog = qbDialog;
//                            addChatListener();
                            initDialogForChat();
                            if (qbDialog.getDialogId().equals("0")) {
                                kittyId = viewModel.getLastKittyId() + 1;
                                qbDialog.setDialogId(viewModel.getRawGeneratedDialogId(kittyId));
                            }
                            progressBar.setVisibility(View.GONE);
                            findViewById(R.id.btnSend).setEnabled(true);
                            viewModel.saveQBDialog(qbDialog);
                            initChatView();
                        } catch (Exception e) {
                            AppLog.e(TAG, e.getMessage(), e);
                        }
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        progressBar.setVisibility(View.GONE);
//                            View rootView = findViewById(R.id.layout_chat_container);
//                            ErrorUtils.showSnackbar(rootView, R.string.error_recreate_session, R.string.dlg_retry, new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    createDialog();
//                                }
//                            });
                    }
                });
    }


    @Override
    String getActionTitle() {
        return "";
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
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        if (qbDialog != null) {
            outState.putSerializable(EXTRA_DIALOG, qbDialog);
        }
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (qbDialog == null) {
            qbDialog = (QBChatDialog) savedInstanceState.getSerializable(EXTRA_DIALOG);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        addConnectionListener();
        registerNetworkStateReceiver();
    }

    private void addConnectionListener() {
        if (ConnectivityUtils.checkInternetConnection(this)) {
            messageService.addConnectionListener();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ConnectivityUtils.checkInternetConnection(this))
            messageService.removeConnectionListener();

        unregisterReceiver(networkStateUpdateReceiver);
    }

    @Override
    public void onBackPressed() {
        messageService.releaseChat();
//        sendReadMessageId();
        sendResult();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.menu_action_clear_chat:
                clearChatData();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return super.onOptionsItemSelected(item);

    }

    private void sendResult() {
        try {
            if (qbDialog != null) {
                Intent result = new Intent();
                qbDialog.setUnreadMessageCount(0);
                result.putExtra(AppConstant.UPDATED_DIALOG, qbDialog);
                result.putExtra(EXTRA_MARK_READ, chatMessageIds);
                AppLog.d(TAG, "Dialog::" + qbDialog);
                if (!ConnectivityUtils.checkInternetConnection(this) && offlineMsgList.size() > 0)
                    viewModel.updateWithSyncableDialog(qbDialog);
                else
                    viewModel.updateDialog(qbDialog);
                int index = getIntent().getIntExtra(EXTRA_CLICKED_ITEM_INDEX, -1);
                result.putExtra(EXTRA_CLICKED_ITEM_INDEX, index);
                setResult(RESULT_OK, result);
            }

        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        } finally {
            finish();
        }
    }

    private void leaveGroupChat() {
        try {
//            ((GroupChatImpl) chat).leaveChatRoom();
            /*ChatHelper.getInstance().leaveDialog(qbDialog, new QBEntityCallback<QBChatDialog>() {
                @Override
                public void onSuccess(QBChatDialog qbDialog, Bundle bundle) {
                    finish();
                }

                @Override
                public void onError(QBResponseException e) {
//                    showErrorSnackbar(R.string.error_leave_chat, e, new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            leaveGroupChat();
//                        }
//                    });
                }
            });*/
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            try {
                if (requestCode == REQUEST_CODE_SELECT_PEOPLE) {
                    //                ArrayList<QBUser> selectedUsers = (ArrayList<QBUser>) data.getSerializableExtra(
                    //                        SelectUsersActivity.EXTRA_QB_USERS);
                    //
                    //                updateDialog(selectedUsers);
                } else if (requestCode == ImageUtils.CAMERA_REQUEST_CODE && (data == null || data.getData() == null)) {
                    // Hacky way to get EXTRA_OUTPUT param to work.
                    // When setting EXTRA_OUTPUT param in the camera intent there is a chance that data will return as null
                    // So we just pass temporary camera file as a data, because RESULT_OK means that photo was written in the file.
                    data = new Intent();
                    data.setData(Uri.fromFile(ImageUtils.getLastUsedCameraFile()));
                    new GetFilepathFromUriTask(getSupportFragmentManager(), this, requestCode).execute(data);
                } else {
                    new GetFilepathFromUriTask(getSupportFragmentManager(), this, requestCode).execute(data);
                }
            } catch (Exception e) {
                AppLog.e(TAG, e.getMessage(), e);
            }

        }
    }

    @Override
    public void onImagePicked(int requestCode, File file) {
        switch (requestCode) {
            case ImageUtils.CAMERA_REQUEST_CODE:
            case ImageUtils.GALLERY_REQUEST_CODE:
                try {
                    AppLog.d(TAG, "File =>" + file.getAbsolutePath());
                    QBAttachment attachment = new QBAttachment(QBAttachment.PHOTO_TYPE);
                    attachment.setUrl(file.getAbsolutePath());
                    attachment.setId("0");
                    QBMessage message = getMessage(null, attachment);
                    showMessage(message);
                    FileUploadUtils.uploadFile(file, this);
                } catch (Exception e) {
                    AppLog.e(TAG, e.getMessage(), e);
                }
                break;

        }
    }

    @Override
    public void onImagePickError(int requestCode, Exception e) {
//        showErrorSnackbar(0, e, null);
    }

    @Override
    public void onImagePickClosed(int requestCode) {
        // ignore
    }

    public View getSnackbarAnchorView() {
        return findViewById(R.id.list_chat_messages);
    }

    public void onSendChatClick(View view) {

        try {
            String text = messageEditText.getText().toString().trim();
            if (!TextUtils.isEmpty(text)) {
                QBMessage message = getMessage(text, null);
                messageEditText.setText("");
                showMessage(message);
                sendMessage(message);
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public void onAttachmentsClick(View view) {
        if (isStoragePermissionGranted())
            ImageUtils.startImagePicker(this);
    }

    public void onCameraClick(View view) {
        if (isStoragePermissionGranted())
            ImageUtils.startCameraForResult(this);
    }

    public void showMessage(QBMessage message) {
        try {
            if (messagesListView.getAdapter() != null) {
                qbDialog.setLastMessage(message.getMessage().getBody());
                qbDialog.setLastMessageDateSent(message.getMessage().getDateSent());
                ((ChatMessageAdapter) messagesListView.getAdapter()).add(message);
                scrollMessageListDown();
            } else {
                AppLog.d(TAG, "null adapter");
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    private QBMessage getMessage(String text, QBAttachment attachment) {
        QBMessage message = new QBMessage();
        try {
            QBChatMessage chatMessage = new QBChatMessage();
            RegisterResponseDao user = PreferanceUtils.getLoginUserObject(this);
            chatMessage.setProperty(Consts.KEY_USERNAME, user.getName());
            chatMessage.setProperty(Consts.KEY_IMAGE, user.getProfilePic());
            chatMessage.setDialogId(qbDialog.getDialogId());
            chatMessage.setSaveToHistory(true);
            if (chatAdapter == null)
                chatMessage.setProperty(SENT_MESSAGE_INDEX, "" + 0);
            else
                chatMessage.setProperty(SENT_MESSAGE_INDEX, "" + chatAdapter.getCount());
            chatMessage.setProperty(AppConstant.LAST_INSERTED_ID, String.valueOf(viewModel.getLastMessageId() + 1));
            if (attachment != null) {
                chatMessage.setBody("Attachment");
                chatMessage.addAttachment(attachment);
                qbDialog.setLastMessage("Attachment");
            } else {
                chatMessage.setBody(text);
                qbDialog.setLastMessage(text);
            }
            chatMessage.setMarkable(true);
            chatMessage.setRecipientId(qbDialog.getRecipientId());
            chatMessage.setSenderId(SharedPreferencesUtil.getQbUser().getId());
            chatMessage.setProperty(PROPERTY_SAVE_TO_HISTORY, "1");
            chatMessage.setDateSent(System.currentTimeMillis() / 1000);

            qbDialog.setLastMessageDateSent(System.currentTimeMillis() / 1000);

            message.setKittyId(kittyId);
            message.setMessage(chatMessage);

            AppLog.d(TAG, "sent Message =>" + chatMessage);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
        return message;
    }

    private void sendMessage(QBMessage message) {
        viewModel.saveMessage(message, kittyId);
        if (ConnectivityUtils.checkInternetConnection(this)) {
//            try {
            messageService.sendMessage(message.getMessage());
//                if (qbDialog.getType().equals(QBDialogType.PRIVATE) || qbDialog.isJoined())
//                    qbDialog.sendMessage(message.getMessage());
//            } catch (SmackException.NotConnectedException e) {
////                Toaster.shortToast(R.string.chat_send_message_error);
//            }
        } else {
            offlineMsgList.add(message.getMessage().getBody());
        }
//        message.setId("0");
    }

//    private void initChat() {
//        try {
////            initDialogForChat();
////            chatMessageListener = new ChatMessageListener();
////            initChatMessageListener();
////            qbDialog.addMessageListener(chatMessageListener);
//            invalidateOptionsMenu();
//            if (ConnectivityUtils.checkInternetConnection(this) && qbDialog != null) {
//                addChatListener();
//                qbDialog.setUnreadMessageCount(0);
//                switch (qbDialog.getType()) {
//                    case GROUP:
//                    case PUBLIC_GROUP:
//                        joinGroupChat();
//                        break;
//
//                    case PRIVATE:
//                        setChatNameToActionBar();
//                        loadDialogUsers();
//                        setSenderInfo();
//                        break;
//
//                    default:
//                        Toaster.shortToast(String.format("%s %s", getString(R.string.chat_unsupported_type),
//                                qbDialog.getType().name()));
//                        finish();
//                        break;
//                }
//            } else {
//                setChatNameToActionBar();
//                progressBar.setVisibility(View.GONE);
//            }
//        } catch (Exception e) {
//            AppLog.e(TAG, e.getMessage(), e);
//        }
//    }

//    protected void joinGroupChat() {
//        System.out.println("joinGroupChat");
//        try {
//            ChatHelper.getInstance().join(qbDialog, new QBEntityCallback<Void>() {
//                @Override
//                public void onSuccess(Void result, Bundle b) {
//                    setChatNameToActionBar();
//                    setSenderInfo();
//                }
//
//                @Override
//                public void onError(QBResponseException e) {
//                    progressBar.setVisibility(View.GONE);
//                }
//            });
//        } catch (Exception e) {
//            AppLog.e(TAG, e.getMessage(), e);
//        }
//    }

//    private void leaveGroupDialog() {
//        try {
//            ChatHelper.getInstance().leaveChatDialog(qbDialog);
//        } catch (XMPPException | SmackException.NotConnectedException e) {
//            Log.w(TAG, e);
//        }
//    }
//
//    protected void releaseChat() {
//        try {
//            qbDialog.removeMessageListrener(chatMessageListener);
//            if (!QBDialogType.PRIVATE.equals(qbDialog.getType())) {
//                leaveGroupDialog();
//            }
//        } catch (Exception e) {
//            Log.e(TAG, "Failed to release chat", e);
//        }
//    }

    protected void updateDialog(final ArrayList<QBUser> selectedUsers) {
        try {
            ChatHelper.getInstance().updateDialogUsers(qbDialog, selectedUsers,
                    new QBEntityCallback<QBChatDialog>() {
                        @Override
                        public void onSuccess(QBChatDialog dialog, Bundle args) {
                            qbDialog = dialog;
                        }

                        @Override
                        public void onError(QBResponseException e) {
                        }
                    }
            );
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    private void setChatNameToActionBar() {
        try {
            ActionBar ab = getSupportActionBar();
            if (ab != null) {
                ab.setTitle(qbDialog.getName());
                ab.setDisplayHomeAsUpEnabled(true);
                ab.setHomeButtonEnabled(true);
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    private void bindData(final ArrayList<QBMessage> messages) {
        System.out.println("bindData(ArrayList<QBChatMessage> messages)");
        if (members != null && members.size() > 0) {
            new BaseAsyncTask<ArrayList<QBMessage>, Void, ArrayList<QBMessage>>() {

                @Override
                public ArrayList<QBMessage> performInBackground(ArrayList<QBMessage>... params) throws Exception {
                    ArrayList<QBMessage> list = params[0];
                    return reconstructArrayWithMemberProfile(list);
                }

                @Override
                public void onResult(ArrayList<QBMessage> qbMessages) {
                    try {
                        Log.e(TAG, "bindData: messages :>" + qbMessages.toString());
                        Collections.reverse(qbMessages);
                        chatAdapter.addList(qbMessages);
                        messagesListView.setSelection(qbMessages.size() - 1);
                        chatAdapter.notifyDataSetChanged();
                        skipPagination += ChatHelper.CHAT_HISTORY_ITEMS_PER_PAGE;
                        progressBar.setVisibility(View.GONE);
                    } catch (Exception e) {
                        progressBar.setVisibility(View.GONE);
                        AppLog.e(TAG, e.getMessage(), e);
                    }
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, messages);
        }
    }

    private void bindView(ArrayList<QBMessage> messages) {
        chatAdapter = new ChatMessageAdapter(ChatActivityTest.this, messages);
        chatAdapter.setOutGoingMessageBgColor(R.color.colorAccent);
        setPaginationListener();
        messagesListView.setAdapter(chatAdapter);
        messagesListView.setAreHeadersSticky(false);
        messagesListView.setDivider(null);
    }

    private void setPaginationListener() {
        chatAdapter.setPaginationHistoryListener(new PaginationHistoryListener() {
            @Override
            public void downloadMore() {
                System.out.println("downloadMore");
                if (chatAdapter.getCount() > 0)
                    bindData();
            }
        });
    }

    private ArrayList<QBMessage> reconstructArrayWithMemberProfile(ArrayList<QBMessage> messages) {
        for (QBMessage message : messages) {
            bindMemberInfo(message);
        }
        return messages;
    }

    private void scrollMessageListDown() {
        messagesListView.smoothScrollToPosition(messagesListView.getCount() - 1);
//        messagesListView.setSelection(messagesListView.getCount() - 1);
    }

    protected void deleteChat() {
        try {
            ChatHelper.getInstance().deleteDialog(qbDialog, new QBEntityCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid, Bundle bundle) {
                    setResult(RESULT_OK);
                    finish();
                }

                @Override
                public void onError(QBResponseException e) {
//                    showErrorSnackbar(R.string.dialogs_deletion_error, e,
//                            new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    deleteChat();
//                                }
//                            });
                }
            });
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

//    private void initChatConnectionListener() {
//        if (ConnectivityUtils.checkInternetConnection(this)) {
//            chatConnectionListener = new VerboseQbChatConnectionListener(getSnackbarAnchorView()) {
//                @Override
//                public void reconnectionSuccessful() {
//                    super.reconnectionSuccessful();
//                    skipPagination = 0;
//                    chatAdapter = null;
//                    switch (qbDialog.getType()) {
//                        case PRIVATE:
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    setSenderInfo();
//                                }
//                            });
//                            break;
//                        case GROUP:
//                            // Join active room if we're in Group Chat
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    joinGroupChat();
//                                }
//                            });
//                            break;
//                    }
//                }
//            };
//            addConnectionListener();
//        }
//    }

    /*private QBChatMessageListener chatMessageListener = new QBChatMessageListener() {
        @Override
        public void onQBChatMessageReceived(QBChat chat, QBChatMessage message) {
            try {
                System.out.println("onQBChatMessageReceived");
                Integer userId = SharedPreferencesUtil.getQbUser().getId();
                if (!userId.equals(message.getSenderId())) {
                    setMessageSenderInfo(message.getSenderId());
                    AppLog.d(TAG, "SenderId" + message.getSenderId());
                    AppLog.d(TAG, "UserId" + userId);
                    chatMessageIds.add(message.getId());

                    QBMessage qbMessage = new QBMessage();
                    qbMessage.setMessage(message);
                    qbMessage.setKittyId(kittyId);
                    bindMemberInfo(qbMessage);
                    showMessage(qbMessage);
                    // save locally
                    viewModel.saveMessage(qbMessage, kittyId);
                } else {
                    viewModel.updateMessage(message);
                }

            } catch (Exception e) {
                AppLog.e(TAG, e.getMessage(), e);
            }
        }
    };*/

    @Override
    public void onUploaded(File file, QBAttachment attachment) {
        try {
            chatAdapter.stopProgress(file);
            chatAdapter.replaceFileIntoAttachment(file, attachment);
            sendMessage(getMessage(null, attachment));
            if (mediaList != null && !mediaList.contains(attachment.getUrl())) {
                if (Utils.isValidString(attachment.getUrl()))
                    mediaList.add(attachment.getUrl());
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }

    }

    @Override
    public void onUploadError(File file, QBResponseException ex) {
        try {
            Log.e(TAG, "Failed to send a message", ex);
            chatAdapter.stopProgress(file);
            Toaster.shortToast(R.string.chat_send_message_error);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public void onUploading(File file, int progress) {
        chatAdapter.updateFileUploadProgress(file, progress);
    }

    private void createMediaList(ArrayList<QBChatMessage> messages) {
        try {
            for (QBChatMessage message : messages) {
                for (QBAttachment attachment : message.getAttachments()) {
                    if (mediaList != null && !mediaList.contains(attachment.getUrl()))
                        mediaList.add(attachment.getUrl());
                }
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    private void setMessageSenderInfo(int senderId) {
        ChatGroupMember member = viewModel.getMember(senderId);
        Log.e(TAG, "setMessageSenderInfo: " + member.toString());
        if (member != null) {
            members.put(senderId, member);
        }
    }

    private void setSenderInfo() {
        try {

            if (ConnectivityUtils.checkInternetConnection(this)) {
               /* QBUsers.getUsersByIDs(getWithoutLoginUserMemberList(), null, new QBEntityCallback<ArrayList<QBUser>>() {
                    @Override
                    public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                        for (final QBUser user : users) {
                            if (user.getFileId() != null) {
                                QBUserUtils.downloadUserProfilePicture(user.getFileId(), new QBEntityCallback<InputStream>() {
                                    @Override
                                    public void onSuccess(InputStream inputStream, Bundle bundle) {
                                        saveUserProfileName(user, inputStream);
                                    }

                                    @Override
                                    public void onError(QBResponseException e) {
                                        AppLog.e(TAG, e.getMessage());
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onError(QBResponseException errors) {
                        AppLog.e(TAG, errors.getMessage());
                    }
                });*/

                QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
                pagedRequestBuilder.setPage(1);
                pagedRequestBuilder.setPerPage(10);
                Bundle bundle = new Bundle();

                QBUsers.getUsersByIDs(getWithoutLoginUserMemberList(),
                        pagedRequestBuilder, bundle)
                        .performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
                            @Override
                            public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                                for (final QBUser user : users) {
                                    if (user.getFileId() != null) {
                                        QBUserUtils.downloadUserProfilePicture(user.getFileId(),
                                                new QBEntityCallback<InputStream>() {
                                                    @Override
                                                    public void onSuccess(InputStream inputStream,
                                                                          Bundle bundle) {
                                                        loadUserAvatarBitmap(user, inputStream);
                                                    }

                                                    @Override
                                                    public void onError(QBResponseException e) {
                                                        AppLog.e(TAG, e.getMessage());
                                                    }
                                                });
                                    } else {
                                        saveUserProfileName(user);
                                    }
                                }
                            }

                            @Override
                            public void onError(QBResponseException errors) {
                                AppLog.e(TAG, errors.getMessage());
                            }

                        });
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    private void loadUserAvatarBitmap(final QBUser user, final InputStream is) {
        try {
            new BaseAsyncTask<Void, Void, Bitmap>() {

                @Override
                public Bitmap performInBackground(Void... params) throws Exception {
                    try {
                        if (is == null)
                            return null;
                        if (user.getPhone() != null && user.getPhone().length() > 0) {
                            user.setFullName(Utils.getDisplayNameByPhoneNumber(ChatActivityTest.this, user.getPhone()));
                        }

                        return BitmapFactory.decodeStream(is);

                    } catch (Exception e) {
                        AppLog.e(TAG, e.getMessage(), e);
                    }
                    return null;
                }

                @Override
                public void onResult(Bitmap bitmap) {
                    if (bitmap != null)
                        saveMemberUser(user, ImageUtils.getBytes(bitmap));
                    else
                        saveMemberUser(user, null);
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    private void saveUserProfileName(final QBUser user) {
        try {
            new BaseAsyncTask<Void, Void, QBUser>() {

                @Override
                public QBUser performInBackground(Void... params) throws Exception {
                    try {
                        if (user.getPhone() != null && user.getPhone().length() > 0) {
                            user.setFullName(Utils.getDisplayNameByPhoneNumber(ChatActivityTest.this, user.getPhone()));
                        }
                    } catch (Exception e) {
                        AppLog.e(TAG, e.getMessage(), e);
                    }
                    return user;
                }

                @Override
                public void onResult(QBUser qbUser) {
                    saveMemberUser(qbUser, null);
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void saveMemberUser(QBUser user, byte[] image) {
        try {
            ChatGroupMember member = viewModel.getMember(user, image);
            viewModel.addMember(member);

            if (members == null)
                members = new HashMap<>();

            members.put(user.getId(), member);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    private void clearAdapter() {
        try {
            if (chatAdapter != null)
                chatAdapter.clearChatData();
            qbDialog.setLastMessage("");
            qbDialog.setUnreadMessageCount(0);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    private void clearChatData() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(ResourceUtils.getString(this, R.string.app_name));
            String msg = getResources().getString(R.string.delete_alert_msg, "Delete");
            builder.setMessage(msg);
            builder.setCancelable(false);
            builder.setPositiveButton(ResourceUtils.getString(this, R.string.proceed), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    clearChat();
                    dialog.dismiss();
                }
            }).setNegativeButton(ResourceUtils.getString(this, R.string.dismiss), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.create().show();
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    private void clearChat() {
        try {

            if (ConnectivityUtils.checkInternetConnection(this)) {
                if (getMessageIdSet() != null && !getMessageIdSet().isEmpty()) {
                    ChatHelper.getInstance().deleteMessages(getMessageIdSet(), new QBEntityCallback<Void>() {
                        @Override
                        public void onSuccess(Void o, Bundle bundle) {
                            viewModel.clearHistory(kittyId);
                        }

                        @Override
                        public void onError(QBResponseException e) {

                        }
                    });
                }
            } else {
                viewModel.updateAllMessageDeleted(kittyId);
            }
            clearAdapter();
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    private Set<String> getMessageIdSet() {
        Set<String> messageId = new HashSet<>();
        try {
            List<QBMessage> chatMessageList = chatAdapter.getList();
            if (chatMessageList != null && !chatMessageList.isEmpty())
                for (int i = 0; i < chatMessageList.size(); i++) {
                    if (Utils.isValidString(chatMessageList.get(i).getMessage().getId()))
                        messageId.add(chatMessageList.get(i).getMessage().getId());
                }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
        return messageId;
    }

    /**
     * @return
     */
    public boolean isStoragePermissionGranted() {
        try {
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
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
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
                ImageUtils.startImagePicker(this);
                if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    AppLog.d(TAG, "Permission: " + permissions[1] + "was " + grantResults[1]);
                    ImageUtils.startCameraForResult(this);
                }
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    private QBMessage getGeneratedQBMessage(QBChatMessage message, int sent) {
        QBMessage msg = new QBMessage();
        try {
            msg.setMessage(message);
            msg.setKittyId(kittyId);
            msg.generateMessageStatus((ArrayList<Integer>) qbDialog.getOccupants());
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
        return msg;
    }

    private void bindMemberInfo(QBMessage message) {
        try {
            if (members.size() > 0) {
//                for (ChatGroupMember member : members) {
                if (members != null) {
                    ChatGroupMember member = members.get(message.getMessage().getSenderId());
                    if (member != null) {
                        message.setSenderName(member.getMemberName());
                        if (member.getImage() != null) {
                            Bitmap bitmap = ImageUtils.getImage(member.getImage());
                            RoundedBitmapDrawable drawable = ImageLoaderUtils.getCircleBitmapDrawable(bitmap);
                            message.setSenderImage(drawable);
                        }
                    }
                }
//                }
            } else {
                AppLog.e(TAG, "No member found size is " + members.size());
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public void onSessionCreated(boolean success) {
        if (success) {
//            initChat();
        }
    }

    @Override
    public void onConnected() {
        try {
            setChatNameToActionBar();
            loadDialogUsers();
            setSenderInfo();
            progressBar.setVisibility(View.GONE);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public void onMessageReceived(QBMessage qbMessage, QBChatMessage message, Integer senderId) {
        setMessageSenderInfo(message.getSenderId());
        chatMessageIds.add(message.getId());
        bindMemberInfo(qbMessage);
        int index = ((ChatMessageAdapter) messagesListView.getAdapter()).getList().size();
        QBMessage lastMessage =
                ((ChatMessageAdapter) messagesListView.getAdapter()).getList().get(index - 1);
        if ((lastMessage.getMessage().getDateSent()) != message.getDateSent())
            showMessage(qbMessage);
        else
            AppLog.d(TAG, "match found");
    }

    private class NetworkStateUpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("NetworkStateUpdateReceiver$onReceive");
            initDialogForChat();
        }
    }

    private List<Integer> getWithoutLoginUserMemberList() {
        List<Integer> members = new ArrayList<>();
        members.addAll(qbDialog.getOccupants());
        return members;
    }

//    public class ChatMessageListener extends QbChatDialogMessageListenerImp {
//        @Override
//        public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {
////            showMessage(qbChatMessage);
//            AppLog.d(TAG, "recive message " + new Gson().toJson(qbChatMessage));
//            try {
//                QBMessage qbMessage = new QBMessage();
//                qbMessage.setMessage(qbChatMessage);
//                qbMessage.setKittyId(kittyId);
//                Integer userId = SharedPreferencesUtil.getQbUser().getId();
//
//                if (!userId.equals(qbChatMessage.getSenderId())) {
//                    setMessageSenderInfo(qbChatMessage.getSenderId());
//                    chatMessageIds.add(qbChatMessage.getId());
//                    bindMemberInfo(qbMessage);
//                    // save locally
//                    viewModel.saveMessage(qbMessage, kittyId);
//                    int index = ((ChatMessageAdapter) messagesListView.getAdapter()).getList().size();
//                    QBMessage lastMessage =
//                            ((ChatMessageAdapter) messagesListView.getAdapter()).getList().get(index - 1);
//                    if ((lastMessage.getMessage().getDateSent()) != qbChatMessage.getDateSent())
//                        showMessage(qbMessage);
//                    else
//                        AppLog.d(TAG, "match found");
//
//                    markAsRead(qbChatMessage);
//                } else {
//                    //  TODO
//                    int lastInsertedId = Integer.parseInt((String) qbChatMessage.getProperty(AppConstant.LAST_INSERTED_ID));
//                    viewModel.updateMessage(qbChatMessage, lastInsertedId);
//                }
//
//            } catch (Exception e) {
//                AppLog.e(TAG, e.getMessage(), e);
//            }
//        }
//    }

//    private void markAsRead(QBChatMessage chatMessage) {
//        if (chatMessage.isMarkable()) {
//            try {
//                qbDialog.readMessage(chatMessage);
//            } catch (XMPPException | SmackException.NotConnectedException e) {
//
//            }
//        }
//    }
//    /*TODO Message Listener Start*/
//
//    private void initChatMessageListener() {
//        try {
//            QBMessageStatusesManager manager = QBChatService.getInstance().getMessageStatusesManager();
//            manager.addMessageStatusListener(ChatActivityTest.this);
//            qbDialog.addMessageSentListener(ChatActivityTest.this);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//    @Override
//    public void processMessageDelivered(String messageID, String dialogID, Integer occupantsID) {
//        try {
//            AppLog.e(TAG, "onMessageDelivered: " + messageID);
////            QBMessage message = chatAdapter.getMessageById(messageID);
//            QBMessage message = ((ChatMessageAdapter) messagesListView.getAdapter()).getMessageById(messageID);
//            if (message != null) {
//                message.setDelivered(1);
//                if (message.getMessage().getDeliveredIds() == null) {
//                    message.getMessage().setDeliveredIds(new ArrayList<Integer>());
//                }
//                message.getMessage().getDeliveredIds().add(occupantsID);
//                chatAdapter.updateList(message, message.getUpdatedIndex());
//                viewModel.updateDelivered(message.getMessage(), messageID);
//            }
//        } catch (Exception e) {
//            AppLog.e(TAG, e.getMessage(), e);
//        }
//    }
//
//    @Override
//    public void processMessageRead(String messageID, String dialogID, Integer occupantsID) {
//        try {
//            AppLog.e(TAG, "onMessageRead: " + messageID);
//            QBMessage message = chatAdapter.getMessageById(messageID);
//            if (message != null) {
//                message.setRead(1);
//                if (message.getMessage().getReadIds() == null) {
//                    message.getMessage().setReadIds(new ArrayList<Integer>());
//                }
//                message.getMessage().getReadIds().add(occupantsID);
//                chatAdapter.updateList(message, message.getUpdatedIndex());
//                viewModel.updateRead(message.getMessage(), messageID);
//            }
//        } catch (Exception e) {
//            AppLog.e(TAG, e.getMessage(), e);
//        }
//    }
//
//    @Override
//    public void processMessageSent(String s, QBChatMessage qbChatMessage) {
//        Log.d(TAG, "processMessageSent: ");
//        try {
//            AppLog.d(TAG, new Gson().toJson(qbChatMessage).toString());
//            int index = Integer.parseInt((String) qbChatMessage.getProperty(SENT_MESSAGE_INDEX));
//            int lastInsertedId = Integer.parseInt((String) qbChatMessage.getProperty(AppConstant.LAST_INSERTED_ID));
//            QBMessage message = chatAdapter.getItem(index);
//            message.setSent(1);
//            message.setMessage(qbChatMessage);
//            chatAdapter.updateList(message, index);
//            viewModel.updateSent(message.getMessage(), lastInsertedId);
//            Log.i(TAG, "onMessageSent: " + qbChatMessage.getBody());
//        } catch (Exception e) {
//            AppLog.e(TAG, e.getMessage(), e);
//        }
//
//    }
//
//    @Override
//    public void processMessageFailed(String s,
//                                     QBChatMessage qbChatMessage) {
//        Log.d(TAG, "processMessageFailed: ");
//        try {
//            int index = Integer.parseInt((String) qbChatMessage.getProperty(SENT_MESSAGE_INDEX));
//            QBMessage message = chatAdapter.getItem(index);
//            message.setFail(1);
//            chatAdapter.updateList(message, index);
//            viewModel.updateFail(message.getMessage(), kittyId);
//            Log.i(TAG, "onMessageFail: ");
//        } catch (Exception ex) {
//            AppLog.e(TAG, ex.getMessage(), ex);
//        }
//
//    }

    private void loadDialogUsers() {
        ChatHelper.getInstance().getUsersFromDialog(qbDialog, new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> users, Bundle bundle) {
                setChatNameToActionBar();
            }

            @Override
            public void onError(QBResponseException e) {
            }
        });
    }

    private void initDialogForChat() {
        try {
            if(ConnectivityUtils.checkInternetConnection(this)) {
                messageService.initDialogForChat(qbDialog, this);
            } else {
                setChatNameToActionBar();
                progressBar.setVisibility(View.GONE);
            }
//            if (qbDialog != null) {
//                qbDialog.initForChat(QBChatService.getInstance());
//            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

//    private void addChatListener() {
////        initDialogForChat();
//        chatMessageListener = new ChatMessageListener();
//        initChatMessageListener();
//        qbDialog.addMessageListener(chatMessageListener);
//        initChatConnectionListener();
//    }

    public class MessageStatusObserver extends ContentObserver {
        public MessageStatusObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            this.onChange(selfChange,null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            //Write your code here
            //Whatever is written here will be
            //executed whenever a change is made
        }

    }
}
