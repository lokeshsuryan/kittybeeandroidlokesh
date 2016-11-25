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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
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
import com.kittyapplication.chat.utils.chat.Chat;
import com.kittyapplication.chat.utils.chat.ChatHelper;
import com.kittyapplication.chat.utils.chat.MessageStatusListener;
import com.kittyapplication.chat.utils.chat.GroupChatImpl;
import com.kittyapplication.chat.utils.chat.PrivateChatImpl;
import com.kittyapplication.chat.utils.chat.QBChatMessageListener;
import com.kittyapplication.chat.utils.qb.FileUploadUtils;
import com.kittyapplication.chat.utils.qb.PaginationHistoryListener;
import com.kittyapplication.chat.utils.qb.QBUserUtils;
import com.kittyapplication.chat.utils.qb.QbDialogUtils;
import com.kittyapplication.chat.utils.qb.VerboseQbChatConnectionListener;
import com.kittyapplication.chat.utils.qb.callback.OnFileUploadListener;
import com.kittyapplication.core.async.BaseAsyncTask;
import com.kittyapplication.core.utils.ConnectivityUtils;
import com.kittyapplication.core.utils.ImageUtils;
import com.kittyapplication.core.utils.ResourceUtils;
import com.kittyapplication.core.utils.StringUtils;
import com.kittyapplication.core.utils.Toaster;
import com.kittyapplication.core.utils.imagepick.GetFilepathFromUriTask;
import com.kittyapplication.core.utils.imagepick.OnImagePickedListener;
import com.kittyapplication.model.Kitty;
import com.kittyapplication.model.RegisterResponseDao;
import com.kittyapplication.model.storeg.ChatGroupMember;
import com.kittyapplication.providers.KittyBeeContract;
import com.kittyapplication.sqlitedb.DBQueryHandler;
import com.kittyapplication.ui.executor.BackgroundExecutorThread;
import com.kittyapplication.ui.executor.ExecutorThread;
import com.kittyapplication.ui.executor.Interactor;
import com.kittyapplication.ui.viewmodel.MainChatViewModel;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.PreferanceUtils;
import com.kittyapplication.utils.Utils;
import com.quickblox.chat.QBChat;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
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

/**
 * Created by MIT on 8/20/2016.
 */
public class ChatActivity extends BaseActivity implements OnImagePickedListener,
        OnFileUploadListener, MessageStatusListener {

    private static final String TAG = ChatActivity.class.getSimpleName();
    private static final int REQUEST_CODE_SELECT_PEOPLE = 752;
    protected static final String EXTRA_QBUSER = "qbuser";
    protected static final String EXTRA_DIALOG = "qbdialog";
    private static final String PROPERTY_SAVE_TO_HISTORY = "save_to_history";
    private static final String SENT_MESSAGE_INDEX = "sent_msg_index";
    public static final String EXTRA_MARK_READ = "markRead";
    public static final String EXTRA_KITTY_ID = "kittyId";

    private ProgressBar progressBar;
    private StickyListHeadersListView messagesListView;
    private EditText messageEditText;


    private ChatMessageAdapter chatAdapter;
    private ConnectionListener chatConnectionListener;

    private Chat chat;
    private QBDialog qbDialog;
    private ArrayList<String> chatMessageIds;
    private int skipPagination = ChatHelper.CHAT_HISTORY_ITEMS_PER_PAGE;
    private String userId, mStrData;

    private ArrayList<String> mediaList;
    private MainChatViewModel viewModel;
    private MenuItem mMenuItemDiary, mMenuItemProfile, mMenuItemSetting, mMenuItemMedia, mMenuItemRules;
    private NetworkStateUpdateReceiver networkStateUpdateReceiver;
    private int action_trial = 0;
    private int kittyId;
    private HashMap<Integer, ChatGroupMember> members;
    private int profileLoadedCounter = 0;
    public static void startForResult(Activity activity, int code,
                                      int kittyId, QBUser qbUser) {
        try {
            Intent intent = new Intent(activity, ChatActivity.class);
            intent.putExtra(ChatActivity.EXTRA_KITTY_ID, kittyId);
            intent.putExtra(ChatActivity.EXTRA_QBUSER, qbUser);
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

            bindData();
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }

        //getMediaListFromServer();
    }

    private void bindData() {
        if (kittyId > 0) {
            viewModel.fetchKittyById(kittyId, new DBQueryHandler.OnQueryHandlerListener<ArrayList<Kitty>>() {
                @Override
                public void onResult(ArrayList<Kitty> result) {
                    System.out.print(result.get(0).toString());
                    if (result.size() > 0) {
                        Kitty kitty = result.get(0);
                        qbDialog = kitty.getQbDialog();
                        setBasicSetting();
                    } else {
                        setBasicSetting();
                    }
                }
            });
        } else {
            startViewBinding();
        }
    }

    private void setBasicSetting() {
        setChatNameToActionBar();
        loadMemberProfile();
        startViewBinding();
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

    private void startViewBinding() {
        bindAdapter();
        initQBSetting();
    }

    private void registerNetworkStateReceiver() {
        networkStateUpdateReceiver = new NetworkStateUpdateReceiver();
        registerReceiver(networkStateUpdateReceiver, new IntentFilter(AppConstant.NETWORK_STAT_ACTION));
    }

    private void initQBSetting() {
        System.out.println("initQBSetting");
        if (QBChatService.getInstance().getGroupChatManager() != null) {
            init();
        } else {
            loginOnQb();
        }
    }

    private void bindAdapter() {
        if (qbDialog != null) {
            int offset = 0;

            if (chatAdapter != null)
                offset = chatAdapter.getCount();

            viewModel.loadMessages(kittyId, offset, skipPagination,
                    new DBQueryHandler.OnQueryHandlerListener<ArrayList<QBMessage>>() {
                        @Override
                        public void onResult(ArrayList<QBMessage> result) {
                            if (result != null && result.size() > 0) {
                                progressBar.setVisibility(View.GONE);
                                bindAdapter(result);
                            } else {
                                bindAdapter(new ArrayList<QBMessage>());
                            }
                        }
                    });
        } else {
            findViewById(R.id.btnSend).setEnabled(false);
            bindAdapter(new ArrayList<QBMessage>());
        }
    }


    private void loginOnQb() {
        if (ConnectivityUtils.checkInternetConnection(this)) {
            ChatHelper.getInstance().login(SharedPreferencesUtil.getQbUser(), new QBEntityCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid, Bundle bundle) {
                    init();
                }

                @Override
                public void onError(QBResponseException e) {
//                    progressBar.setVisibility(View.GONE);
//                    View rootView = findViewById(R.id.layout_chat_container);
//                ErrorUtils.showSnackbar(rootView, R.string.error_recreate_session, R.string.dlg_retry, new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        initChat();
//                    }
//                });
                }
            });
        } else {
            init();
        }
    }

    private void init() {
        initChatConnectionListener();

        if (qbDialog == null) {
            createDialog();
        } else {
            qbDialog.setUnreadMessageCount(0);
            initChat();
        }
    }

    private void createDialog() {
        try {
            QBUser user = (QBUser) getIntent().getSerializableExtra(EXTRA_QBUSER);
            final List<QBUser> users = new ArrayList<>();
            users.add(user);
            users.add(SharedPreferencesUtil.getQbUser());
            if (user.getId() != null) {
                new ExecutorThread().startExecutor().postTask(new Runnable() {
                    @Override
                    public void run() {
                        createPrivateChatDialog(users);
                    }
                });

            } else {
                progressBar.setVisibility(View.GONE);
                Toaster.longToast("User not found");
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    private void createPrivateChatDialog(List<QBUser> users) {
        ChatHelper.getInstance().createDialogWithSelectedUsers(users,
                new QBEntityCallback<QBDialog>() {
                    @Override
                    public void onSuccess(QBDialog qbDialog, Bundle bundle) {
                        try {
                            String dialog = new Gson().toJson(qbDialog);
                            AppLog.d(TAG, "dialog::" + dialog);
                            qbDialog.setLastMessageDateSent(System.currentTimeMillis() / 1000);
                            ChatActivity.this.qbDialog = qbDialog;

                            if (qbDialog.getDialogId().equals("0")) {
                                kittyId = viewModel.getLastKittyId() + 1;
                                qbDialog.setDialogId(viewModel.getRawGeneratedDialogId(kittyId));
                                qbDialog.setCustomData(viewModel.getCustomData(kittyId));
                            }
                            progressBar.setVisibility(View.GONE);
                            findViewById(R.id.btnSend).setEnabled(true);
                            viewModel.saveQBDialog(qbDialog);
                            initChat();
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
            qbDialog = (QBDialog) savedInstanceState.getSerializable(EXTRA_DIALOG);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ConnectivityUtils.checkInternetConnection(this))
            ChatHelper.getInstance().addConnectionListener(chatConnectionListener);
        registerNetworkStateReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ConnectivityUtils.checkInternetConnection(this))
            ChatHelper.getInstance().removeConnectionListener(chatConnectionListener);

        unregisterReceiver(networkStateUpdateReceiver);
    }

    @Override
    public void onBackPressed() {
        releaseChat();
//        sendReadMessageId();
        sendResult();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        try {
            mMenuItemDiary = menu.findItem(R.id.menu_action_diary);
            mMenuItemMedia = menu.findItem(R.id.menu_action_media);
            mMenuItemProfile = menu.findItem(R.id.menu_action_profile);
            mMenuItemSetting = menu.findItem(R.id.menu_action_setting);
            mMenuItemRules = menu.findItem(R.id.menu_action_kitty_rule);

            mMenuItemDiary.setVisible(false);
            mMenuItemMedia.setVisible(false);
            mMenuItemProfile.setVisible(false);
            mMenuItemSetting.setVisible(false);
            mMenuItemRules.setVisible(false);

           /* if (qbDialog != null) {
                if (qbDialog.getType() == QBDialogType.GROUP) {
                    mMenuItemSetting.setVisible(true);
                    if (Utils.isValidString(mStrData) && !mStrData.equalsIgnoreCase("null")) {
                        ChatData data = new Gson().fromJson(mStrData, ChatData.class);
                        if (data.getRule().equalsIgnoreCase("0")) {
                            mMenuItemRules.setVisible(true);
                            mMenuItemDiary.setVisible(false);
                        } else {
                            mMenuItemRules.setVisible(false);
                            mMenuItemDiary.setVisible(true);
                        }
                    } else {
                        mMenuItemRules.setVisible(false);
                        mMenuItemDiary.setVisible(false);
                    }
                } else {
                    mMenuItemMedia.setVisible(true);
                    mMenuItemProfile.setVisible(true);
                }
            }*/
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
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

        return super.

                onOptionsItemSelected(item);

    }

    private void sendResult() {
        try {
            if (qbDialog != null) {
                Intent result = new Intent();
                qbDialog.setUnreadMessageCount(0);
                result.putExtra(AppConstant.UPDATED_DIALOG, qbDialog);
                result.putExtra(EXTRA_MARK_READ, chatMessageIds);
                AppLog.d(TAG, "Dialog::" + qbDialog);
                viewModel.updateDialog(qbDialog);
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
            ((GroupChatImpl) chat).leaveChatRoom();
            ChatHelper.getInstance().leaveDialog(qbDialog, new QBEntityCallback<QBDialog>() {
                @Override
                public void onSuccess(QBDialog qbDialog, Bundle bundle) {
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
            });
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
                showMessage(message);
                sendMessage(message);
                messageEditText.setText("");
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
            if (chatAdapter != null) {
                qbDialog.setLastMessage(message.getMessage().getBody());
                qbDialog.setLastMessageDateSent(message.getMessage().getDateSent());
                chatAdapter.add(message);
                scrollMessageListDown();
            } else {
                //            if (unShownMessages == null) {
                //                unShownMessages = new ArrayList<>();
                //            }
                //            unShownMessages.add(message);
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
            try {
                chat.sendMessage(message.getMessage());
            } catch (XMPPException | SmackException.NotConnectedException e) {
                Log.e(TAG, "Failed to send a message", e);
                Toaster.shortToast(R.string.chat_send_message_error);
            }
        }
//        message.setId("0");
    }

    private void initChat() {
        try {

            invalidateOptionsMenu();
            if (ConnectivityUtils.checkInternetConnection(this)) {
                switch (qbDialog.getType()) {
                    case GROUP:
                    case PUBLIC_GROUP:
                        chat = new GroupChatImpl(chatMessageListener, this);
                        joinGroupChat();
                        break;

                    case PRIVATE:
                        chat = new PrivateChatImpl(chatMessageListener,
                                QbDialogUtils.getOpponentIdForPrivateDialog(qbDialog)
                                , this);
                        setChatNameToActionBar();
                        setSenderInfo();
                        break;

                    default:
                        Toaster.shortToast(String.format("%s %s", getString(R.string.chat_unsupported_type),
                                qbDialog.getType().name()));
                        finish();
                        break;
                }
            } else {
                setChatNameToActionBar();
                progressBar.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    protected void joinGroupChat() {
        System.out.println("joinGroupChat");
        try {
            if (chat != null) {
                ((GroupChatImpl) chat).joinGroupChat(qbDialog, new QBEntityCallback<Void>() {
                    @Override
                    public void onSuccess(Void result, Bundle b) {
                        setChatNameToActionBar();
                        setSenderInfo();
                    }

                    @Override
                    public void onError(QBResponseException e) {
//                        progressBar.setVisibility(View.GONE);
//                        snackbar = showErrorSnackbar(R.string.connection_error, e, null);
                    }
                });
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    protected void leaveGroupChatRoom() {
        if (chat != null) {
            ((GroupChatImpl) chat).leaveChatRoom();
        }
    }

    protected void releaseChat() {
        try {
            if (chat != null) {
                chat.release();
            }
        } catch (XMPPException e) {
            Log.e(TAG, "Failed to release chat", e);
        }
    }

    protected void updateDialog(final ArrayList<QBUser> selectedUsers) {
        try {
            ChatHelper.getInstance().updateDialogUsers(qbDialog, selectedUsers,
                    new QBEntityCallback<QBDialog>() {
                        @Override
                        public void onSuccess(QBDialog dialog, Bundle args) {
                            qbDialog = dialog;
//                            loadDialogUsers();
                        }

                        @Override
                        public void onError(QBResponseException e) {
//                            showErrorSnackbar(R.string.chat_info_add_people_error, e,
//                                    new View.OnClickListener() {
//                                        @Override
//                                        public void onClick(View v) {
//                                            updateDialog(selectedUsers);
//                                        }
//                                    });
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

    private void bindAdapter(final ArrayList<QBMessage> messages) {
        System.out.println("bindAdapter(ArrayList<QBChatMessage> messages)");
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

                        Log.e(TAG, "bindAdapter: messages :>" + qbMessages.toString());
                        if (chatAdapter == null) {
                            initListView(qbMessages);
                        } else {
                            chatAdapter.addList(qbMessages);
                            messagesListView.setSelection(qbMessages.size() - 1);
                            chatAdapter.notifyDataSetChanged();
                            if (qbMessages.size() < ChatHelper.CHAT_HISTORY_ITEMS_PER_PAGE) {
                                chatAdapter.setPaginationHistoryListener(null);
                            }
                        }
                        skipPagination += ChatHelper.CHAT_HISTORY_ITEMS_PER_PAGE;
                    } catch (Exception e) {
                        progressBar.setVisibility(View.GONE);
                        AppLog.e(TAG, e.getMessage(), e);
                    }
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, messages);
        } else if (chatAdapter == null) {
            initListView(messages);
        }
    }

    private void initListView(ArrayList<QBMessage> messages) {
        chatAdapter = new ChatMessageAdapter(ChatActivity.this, messages);
        chatAdapter.setOutGoingMessageBgColor(R.color.colorAccent);
        if (messages.size() >= ChatHelper.CHAT_HISTORY_ITEMS_PER_PAGE) {
            chatAdapter.setPaginationHistoryListener(new PaginationHistoryListener() {
                @Override
                public void downloadMore() {
                    System.out.println("downloadMore");
                    loadChatHistory();
                }
            });
        }
        messagesListView.setAdapter(chatAdapter);
        messagesListView.setAreHeadersSticky(false);
        messagesListView.setDivider(null);
    }

    private ArrayList<QBMessage> reconstructArrayWithMemberProfile(ArrayList<QBMessage> messages) {
        for (QBMessage message : messages) {
            bindMemberInfo(message);
        }
        return messages;
    }

    protected void loadChatHistory() {
        System.out.println("loadChatHistory");
        if (ConnectivityUtils.checkInternetConnection(this) && !viewModel.isMessageCleared(qbDialog.getDialogId())) {
            if (chatAdapter == null || chatAdapter.getCount() < skipPagination)
                skipPagination = 0;
//            new ExecutorThread().startExecutor().postTask(new Runnable() {
//                @Override
//                public void run() {
            ChatHelper.getInstance().loadChatHistory(qbDialog, skipPagination, new QBEntityCallback<ArrayList<QBChatMessage>>() {
                @Override
                public void onSuccess(ArrayList<QBChatMessage> messages, Bundle args) {
                    // The newest messages should be in the end of list,
                    // so we need to reverse list to show messages in the right order
                    addServerMessages(messages);
                }

                @Override
                public void onError(QBResponseException e) {
                    showNetworkError(e);
                }
            });
//                }
//            });
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void addServerMessages(final ArrayList<QBChatMessage> messages) {
        if (messages.size() > 0) {
            Collections.reverse(messages);
            new BaseAsyncTask<ArrayList<QBChatMessage>, Void, ArrayList<QBMessage>>() {

                @SafeVarargs
                @Override
                public final ArrayList<QBMessage> performInBackground(ArrayList<QBChatMessage>... params) throws Exception {
                    ArrayList<QBChatMessage> list = params[0];
                    ArrayList<QBMessage> messages = new ArrayList<QBMessage>();
                    for (QBChatMessage chatMessage : list) {
                        QBMessage msg = getGeneratedQBMessage(chatMessage, 1);
                        messages.add(msg);
                    }
                    System.out.println("list size::>" + list.size());
                    if (chatAdapter == null)
                        return messages;
                    ArrayList<QBMessage> old = (ArrayList<QBMessage>) chatAdapter.getList();
                    return viewModel.getUnSavedFilteredMessage(old, messages);
                }

                @Override
                public void onResult(ArrayList<QBMessage> msgs) {
                    System.out.println("Result size::>" + msgs.size());
                    bindAdapter(msgs);
                    viewModel.saveMessages(msgs, kittyId);
                    progressBar.setVisibility(View.GONE);
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, messages);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

//    private void startMemberInfoBindingTask(final ArrayList<QBMessage> msgs) {
//        BackgroundExecutorThread backgroundExecutorThread = new BackgroundExecutorThread();
//        backgroundExecutorThread.execute(new Interactor() {
//            @Override
//            public void execute() {
//                for (QBMessage msg : msgs) {
//                    if (members.size() > 0) {
//                        for (ChatGroupMember member : members) {
//                            if (member != null && member.getMemberId() != null) {
//                                if (member.getMemberId().equals(msg.getMessage().getSenderId())) {
//                                    msg.setSenderName(member.getMemberName());
//                                    Bitmap bitmap = ImageUtils.getImage(member.getImage());
//                                    RoundedBitmapDrawable drawable = ImageLoaderUtils.getCircleBitmapDrawable(bitmap);
//                                    msg.setSenderImage(drawable);
//                                } else {
//                                    AppLog.e(TAG, member.getMemberId() + " =====NOT MATCH===== " + msg.getMessage().getSenderId());
//                                }
//                            } else {
//                                AppLog.e(TAG, "No member is null ");
//                            }
//                        }
//                    } else {
//                        AppLog.e(TAG, "No member found size is " + members.size());
//                    }
//                }
//            }
//        }, new Interactor.OnExecutionFinishListener() {
//            @Override
//            public void onFinish() {
//                bindAdapter(msgs);
//                viewModel.saveMessages(msgs, kittyId);
//                progressBar.setVisibility(View.GONE);
//            }
//        });
//    }

    private void showNetworkError(Exception e) {
//        progressBar.setVisibility(View.GONE);
        skipPagination -= ChatHelper.CHAT_HISTORY_ITEMS_PER_PAGE;
//        snackbar = showErrorSnackbar(R.string.connection_error, e, null);
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

    private void initChatConnectionListener() {
        System.out.println("initChatConnectionListener");
        if (ConnectivityUtils.checkInternetConnection(this)) {
            chatConnectionListener = new VerboseQbChatConnectionListener(getSnackbarAnchorView()) {
                @Override
                public void connectionClosedOnError(final Exception e) {
                    super.connectionClosedOnError(e);

                    try {
                        // Leave active room if we're in Group Chat
                        if (qbDialog.getType() == QBDialogType.GROUP) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    leaveGroupChatRoom();
                                }
                            });
                        }
                    } catch (Exception e1) {
                        AppLog.e(TAG, e.getMessage(), e);
                    }
                }

                @Override
                public void reconnectionSuccessful() {
                    super.reconnectionSuccessful();
                    try {
                        skipPagination = 0;
                        chatAdapter = null;
                        switch (qbDialog.getType()) {
                            case PRIVATE:
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setSenderInfo();
                                    }
                                });
                                break;
                            case GROUP:
                                // Join active room if we're in Group Chat
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        joinGroupChat();
                                    }
                                });
                                break;
                        }
                    } catch (Exception e) {
                        AppLog.e(TAG, e.getMessage(), e);
                    }
                }
            };
            ChatHelper.getInstance().addConnectionListener(chatConnectionListener);
        }
    }

    private QBChatMessageListener chatMessageListener = new QBChatMessageListener() {
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
    };

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

    private Bitmap getDefaultBitmap() {
        return BitmapFactory.decodeResource(getResources(), R.drawable.ic_user);
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
                QBUsers.getUsersByIDs(qbDialog.getOccupants(), null, new QBEntityCallback<ArrayList<QBUser>>() {
                    @Override
                    public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                        AppLog.d(TAG, "QBUser List::::::  " + new Gson().toJson(users));
                        for (final QBUser user : users) {
                            if (user.getFileId() != null) {
                                QBUserUtils.downloadUserProfilePicture(user.getFileId(), new QBEntityCallback<InputStream>() {
                                    @Override
                                    public void onSuccess(InputStream inputStream, Bundle bundle) {
                                        AppLog.d(TAG, "downloadUserProfilePicture" + inputStream);
                                        AppLog.d(TAG, "user.getFullName()" + user.getFullName());
                                        StringUtils.printBundle(bundle);
                                        loadUserAvatarBitmap(user, inputStream);
                                    }

                                    @Override
                                    public void onError(QBResponseException e) {
                                        AppLog.e(TAG, e.getMessage());
                                    }
                                });
                            } else {
                                loadUserDisplayName(user);
                                AppLog.d(TAG, user.getFullName() + "::profile image id is " + user.getFileId());
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

    private void loadUserDisplayName(final QBUser user) {
        try {
            new BaseAsyncTask<Void, Void, Object[]>() {

                @Override
                public Object[] performInBackground(Void... params) throws Exception {
                    Object[] objects = new Object[2];
                    try {
                        if (user.getPhone() != null && user.getPhone().length() > 0) {
                            objects[0] = Utils.getDisplayNameByPhoneNumber(ChatActivity.this, user.getPhone());
                        }

                        objects[1] = BitmapFactory.decodeResource(getResources(), R.drawable.ic_user);
                    } catch (Exception e) {
                        AppLog.e(TAG, e.getMessage(), e);
                    }
                    return objects;
                }

                @Override
                public void onResult(Object[] objects) {
                    profileLoadedCounter++;
                    if (objects != null) {
                        saveMember(user, objects);
                    }
                }
            }.execute();
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }


    private void loadUserAvatarBitmap(final QBUser user, final InputStream is) {
        try {
            new BaseAsyncTask<Void, Void, Object[]>() {

                @Override
                public Object[] performInBackground(Void... params) throws Exception {
                    Object[] objects = new Object[2];
                    try {
                        if (is == null)
                            return null;
                        if (user.getPhone() != null && user.getPhone().length() > 0) {
                            objects[0] = Utils.getDisplayNameByPhoneNumber(ChatActivity.this, user.getPhone());
                        }

                        if (objects[0] == null)
                            objects[0] = user.getFullName() != null ? user.getFullName() : user.getId();

                        objects[1] = BitmapFactory.decodeStream(is);
                    } catch (Exception e) {
                        AppLog.e(TAG, e.getMessage(), e);
                    }
                    return objects;
                }

                @Override
                public void onResult(Object[] objects) {
                    if (objects != null) {
                        saveMember(user, objects);
                    }
                }
            }.execute();
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private void saveMember(QBUser user, Object[] objects) {
        saveMemberUser(user, ImageUtils.getBytes((Bitmap) objects[1]));
    }

    private void saveMemberUser(QBUser user, byte[] image) {
        try {
            ChatGroupMember member = new ChatGroupMember();
            member.setMemberId(user.getId());
            member.setMemberNumber(user.getPhone());
            member.setMemberName(user.getFullName());
            member.setGroupId(qbDialog.getDialogId());
            member.setImage(image);
            AppLog.e(TAG, member.toString());
            viewModel.addMember(member);

            if (members == null)
                members = new HashMap<>();

            members.put(user.getId(), member);

            AppLog.e(TAG, "Member size ::" + members.size() + "= QBDialog Member size ::" + qbDialog.getOccupants().size());
            if (members.size() == qbDialog.getOccupants().size()) {
                loadChatHistory();
            }
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
            builder.setTitle(ResourceUtils.getString(R.string.app_name));
            String msg = getResources().getString(R.string.delete_alert_msg, "Delete");
            builder.setMessage(msg);
            builder.setCancelable(false);
            builder.setPositiveButton(ResourceUtils.getString(R.string.proceed), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    clearChat();
                    dialog.dismiss();
                }
            }).setNegativeButton(ResourceUtils.getString(R.string.dismiss), new DialogInterface.OnClickListener() {
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

                    QBChatService.deleteMessages(getMessageIdSet(), new QBEntityCallback<Void>() {
                        @Override
                        public void onSuccess(Void aVoid, Bundle bundle) {
                            viewModel.clearHistory(kittyId);
                        }

                        @Override
                        public void onError(QBResponseException errors) {
//                            progressBar.setVisibility(View.GONE);
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

    private class NetworkStateUpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("NetworkStateUpdateReceiver$onReceive");
            int limit = ChatHelper.CHAT_HISTORY_ITEMS_PER_PAGE;
            if (chatAdapter != null && chatAdapter.getCount() > 0) {
                limit = chatAdapter.getCount();
            }
            viewModel.loadMessages(kittyId, 0, limit,
                    new DBQueryHandler.OnQueryHandlerListener<ArrayList<QBMessage>>() {
                        @Override
                        public void onResult(ArrayList<QBMessage> result) {
                            if (result != null && result.size() > 0) {
                                if (chatAdapter != null)
                                    chatAdapter.getList().clear();
                                bindAdapter(result);
                            }
                        }
                    });
            initQBSetting();
        }
    }

    @Override
    public void onMessageFail(QBChatMessage chatMessage) {

        try {
            int index = Integer.parseInt((String) chatMessage.getProperty(SENT_MESSAGE_INDEX));
            QBMessage message = chatAdapter.getItem(index);
            message.setFail(1);
            chatAdapter.updateList(message, index);
            viewModel.updateFail(message.getMessage(), kittyId);
            Log.i(TAG, "onMessageFail: ");
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }

    }

    @Override
    public void onMessageSent(QBChatMessage chatMessage) {
        try {
            int index = Integer.parseInt((String) chatMessage.getProperty(SENT_MESSAGE_INDEX));
            int lastInsertedId = Integer.parseInt((String) chatMessage.getProperty(AppConstant.LAST_INSERTED_ID));
            QBMessage message = chatAdapter.getItem(index);
            message.setSent(1);
            message.setMessage(chatMessage);
            chatAdapter.updateList(message, index);
            viewModel.updateSent(message.getMessage(), lastInsertedId);
            Log.i(TAG, "onMessageSent: ");
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public void onMessageDelivered(String messageID, String dialogID, Integer occupantsID) {
        try {
            AppLog.e(TAG, "onMessageDelivered: " + messageID);
            QBMessage message = chatAdapter.getMessageById(messageID);
            if (message != null) {
                message.setDelivered(1);
                if (message.getMessage().getDeliveredIds() == null) {
                    message.getMessage().setDeliveredIds(new ArrayList<Integer>());
                }
                message.getMessage().getDeliveredIds().add(occupantsID);
                chatAdapter.updateList(message, message.getUpdatedIndex());
                viewModel.updateDelivered(message.getMessage(), messageID);
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public void onMessageRead(String messageID, String dialogID, Integer occupantsID) {
        try {
            AppLog.e(TAG, "onMessageRead: " + messageID);
            QBMessage message = chatAdapter.getMessageById(messageID);
            if (message != null) {
                message.setRead(1);
                if (message.getMessage().getReadIds() == null) {
                    message.getMessage().setReadIds(new ArrayList<Integer>());
                }
                message.getMessage().getReadIds().add(occupantsID);
                chatAdapter.updateList(message, message.getUpdatedIndex());
                viewModel.updateRead(message.getMessage(), messageID);
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }
}
