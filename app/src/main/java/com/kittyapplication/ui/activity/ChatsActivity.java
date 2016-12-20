package com.kittyapplication.ui.activity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.kittyapplication.R;
import com.kittyapplication.chat.ui.adapter.AttachmentPreviewAdapter;
import com.kittyapplication.chat.ui.adapter.ChatMessageAdapter;
import com.kittyapplication.chat.ui.models.QBMessage;
import com.kittyapplication.chat.utils.Consts;
import com.kittyapplication.chat.utils.ImageLoaderUtils;
import com.kittyapplication.chat.utils.SharedPreferencesUtil;
import com.kittyapplication.chat.utils.chat.ChatHelper;
import com.kittyapplication.chat.utils.chat.QbChatDialogMessageListenerImp;
import com.kittyapplication.chat.utils.qb.PaginationHistoryListener;
import com.kittyapplication.chat.utils.qb.QBUserUtils;
import com.kittyapplication.chat.utils.qb.QbAuthUtils;
import com.kittyapplication.chat.utils.qb.QbDialogUtils;
import com.kittyapplication.chat.utils.qb.VerboseQbChatConnectionListener;
import com.kittyapplication.core.async.BaseAsyncTask;
import com.kittyapplication.core.ui.dialog.ProgressDialogFragment;
import com.kittyapplication.core.utils.ConnectivityUtils;
import com.kittyapplication.core.utils.ImageUtils;
import com.kittyapplication.core.utils.Toaster;
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
import com.kittyapplication.utils.Utils;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBMessageStatusesManager;
import com.quickblox.chat.QBRestChatService;
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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import static com.crashlytics.android.Crashlytics.log;
import static com.kittyapplication.utils.AppConstant.EXTRA_CLICKED_ITEM_INDEX;
import static io.fabric.sdk.android.services.concurrency.AsyncTask.init;

/**
 * Created by HalfBloodPrince(RIONTECH)
 * Riontech on 26/11/16.
 */

public class ChatsActivity extends BaseActivity implements
        QBChatDialogMessageSentListener, QBMessageStatusListener {

    private static final String TAG = ChatsActivity.class.getSimpleName();
    private static final int REQUEST_CODE_SELECT_PEOPLE = 752;
    protected static final String EXTRA_QBUSER = "qbuser";
    private static final String PROPERTY_SAVE_TO_HISTORY = "save_to_history";
    private static final String SENT_MESSAGE_INDEX = "sent_msg_index";
    public static final String EXTRA_MARK_READ = "markRead";
    public static final String EXTRA_KITTY_ID = "kittyId";
    private static final String EXTRA_DIALOG = "dialog";
    private static final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    private ProgressBar progressBar;
    private StickyListHeadersListView messagesListView;
    private EditText messageEditText;

    private ArrayList<QBMessage> unShownMessages;


    private ChatMessageAdapter chatAdapter;
    private ConnectionListener chatConnectionListener;

    private QBChatDialog qbDialog;
    private ArrayList<String> chatMessageIds;

    private ArrayList<String> mediaList;
    private MainChatViewModel viewModel;
    private NetworkStateUpdateReceiver networkStateUpdateReceiver;
    private int kittyId;
    private HashMap<Integer, ChatGroupMember> members;
    private ArrayList<String> offlineMsgList;
    private int skipPagination = ChatHelper.CHAT_HISTORY_ITEMS_PER_PAGE;

    private ChatMessageListener chatMessageListener;
    private AttachmentPreviewAdapter attachmentPreviewAdapter;

    public static void startForResult(Activity activity, int code, int kittyID,
                                      QBUser qbUser, int position) {
        try {
            Intent intent = new Intent(activity, ChatsActivity.class);
            intent.putExtra(EXTRA_KITTY_ID, kittyID);
            intent.putExtra(EXTRA_QBUSER, qbUser);
            intent.putExtra(EXTRA_CLICKED_ITEM_INDEX, position);
            activity.startActivityForResult(intent, code);
        } catch (ActivityNotFoundException e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_chat);
        View view = LayoutInflater.from(this).inflate(R.layout.activity_chat, null);
        addLayoutToContainer(view);
        hideBottomLayout();
        hideLeftIcon();
        findViewById(R.id.layout_chat_container).requestFocus();
        offlineMsgList = new ArrayList<>();
        viewModel = new MainChatViewModel(this);
        mediaList = new ArrayList<>();
        kittyId = getIntent().getIntExtra(EXTRA_KITTY_ID, 0);
        viewModel = new MainChatViewModel(this);
        chatMessageIds = new ArrayList<>();

        initView(view);
        attachmentPreviewAdapter = new AttachmentPreviewAdapter(this, null, null);
        chatMessageListener = new ChatMessageListener();
        bindView(new ArrayList<QBMessage>());
        loadData();
    }

    private void initView(View view) {
        messagesListView = (StickyListHeadersListView) view.findViewById(R.id.list_chat_messages);
        messageEditText = (EditText) view.findViewById(R.id.edit_chat_message);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_chat);
        progressBar.setVisibility(View.VISIBLE);
        ImageView imgbg = (ImageView) findViewById(R.id.imgChatBg);
        com.kittyapplication.utils.ImageUtils.getImageLoader(this)
                .displayImage("drawable://" + R.drawable.login_screen_bg, imgbg);

    }

    @Override
    String getActionTitle() {
        return null;
    }

    @Override
    boolean isDisplayHomeAsUpEnabled() {
        return false;
    }

    @Override
    boolean hasDrawer() {
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        ChatHelper.getInstance().addConnectionListener(chatConnectionListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ChatHelper.getInstance().removeConnectionListener(chatConnectionListener);
    }

    @Override
    public void onBackPressed() {
//        releaseChat();
//        sendDialogId();

        super.onBackPressed();
    }

    public void onSessionCreated(boolean success) {
        if (success) {
            initChat();
        }
    }

    protected View getSnackbarAnchorView() {
        return findViewById(R.id.list_chat_messages);
    }

    public void onSendChatClick(View view) {
        try {
            String text = messageEditText.getText().toString().trim();
            if (!TextUtils.isEmpty(text)) {
                QBMessage message = getMessage(text, null);
//                messageEditText.setText("");
//                showMessage(message);
//                sendMessage(message);
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public void showMessage(QBMessage message) {
        try {
            if (chatAdapter != null) {
                qbDialog.setLastMessage(message.getMessage().getBody());
                qbDialog.setLastMessageDateSent(message.getMessage().getDateSent());
                chatAdapter.add(message);
                scrollMessageListDown();
            } else {
                Log.d(TAG, "showMessage: else");
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
                chatMessage.setProperty(SENT_MESSAGE_INDEX, "" +
                        chatAdapter.getCount());

            chatMessage.setProperty(AppConstant.LAST_INSERTED_ID,
                    String.valueOf(viewModel.getLastMessageId() + 1));
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

    private void sendMessage(final QBMessage message) {
        AppLog.d(TAG, "SEND MESSAGE  :: " + new Gson().toJson(message).toString());
        //TODO
        new BaseAsyncTask<Void, Void, Void>() {
            @Override
            public Void performInBackground(Void... params) throws Exception {
                //TODO
                viewModel.saveMessage(message, kittyId);
                if (ConnectivityUtils.checkInternetConnection(ChatsActivity.this)) {
                    try {
                        sendChatMessage(message.getMessage().toString(), null);
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to send a message", e);
//                        Toaster.shortToast(R.string.chat_send_message_error);
                    }
                } else {
                    offlineMsgList.add(message.getMessage().getBody());
                }
                return null;
            }

            @Override
            public void onResult(Void bitmap) {

            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void sendChatMessage(final String text, final QBAttachment attachment) {
        new BaseAsyncTask<Void, Void, Void>() {
            @Override
            public Void performInBackground(Void... params) throws Exception {
                QBChatMessage chatMessage = new QBChatMessage();
                if (attachment != null) {
                    chatMessage.addAttachment(attachment);
                } else {
                    chatMessage.setBody(text);
                }
                chatMessage.setProperty(PROPERTY_SAVE_TO_HISTORY, "1");
                chatMessage.setDateSent(System.currentTimeMillis() / 1000);
                chatMessage.setMarkable(true);

/*if (!QBDialogType.PRIVATE.equals(qbDialog.getType()) && !qbDialog.isJoined()) {
            Toaster.shortToast("You're still joining a group chat, please wait a bit");
            return;
        }*/

                try {
                    qbDialog.sendMessage(chatMessage);
                    if (attachment != null) {
                        attachmentPreviewAdapter.remove(attachment);
                    } else {
                        messageEditText.setText("");
                    }
                } catch (SmackException.NotConnectedException e) {
                    Log.w(TAG, e);
                    Toaster.shortToast("Can't send a message, You are not connected to chat");
                }
                return null;
            }

            @Override
            public void onResult(Void bitmap) {

            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void initChat() {
        switch (qbDialog.getType()) {
            case GROUP:
            case PUBLIC_GROUP:
                joinGroupChat();
                break;

            case PRIVATE:
                initGlobalChatMessageListener();
                loadDialogUsers();
                break;

            default:
                Toaster.shortToast(String.format("%s %s",
                        getString(R.string.chat_unsupported_type), qbDialog.getType().name()));
                finish();
                break;
        }
    }

    private void joinGroupChat() {
        progressBar.setVisibility(View.VISIBLE);
        ChatHelper.getInstance().join(qbDialog, new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void result, Bundle b) {
                initGlobalChatMessageListener();
                loadDialogUsers();
            }

            @Override
            public void onError(QBResponseException e) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void leaveGroupDialog() {
        try {
            ChatHelper.getInstance().leaveChatDialog(qbDialog);
        } catch (XMPPException | SmackException.NotConnectedException e) {
            Log.w(TAG, e);
        }
    }

    private void releaseChat() {
        qbDialog.removeMessageListrener(chatMessageListener);
        if (!QBDialogType.PRIVATE.equals(qbDialog.getType())) {
            leaveGroupDialog();
        }
    }

    private void updateDialog(final ArrayList<QBUser> selectedUsers) {
        ChatHelper.getInstance().updateDialogUsers(qbDialog, selectedUsers,
                new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog dialog, Bundle args) {
                        qbDialog = dialog;
                        loadDialogUsers();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                    }
                }
        );
    }

    private void loadDialogUsers() {
        ChatHelper.getInstance().getUsersFromDialog(qbDialog, new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> users, Bundle bundle) {
                setChatNameToActionBar();
                if (chatAdapter != null && chatAdapter.getList().isEmpty())
                    loadChatHistory(0);
            }

            @Override
            public void onError(QBResponseException e) {
            }
        });
    }

    private void setChatNameToActionBar() {
        String chatName = QbDialogUtils.getDialogName(qbDialog);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(chatName);
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeButtonEnabled(true);
        }
    }

    private void loadChatHistory(int count) {
        ChatHelper.getInstance().loadChatHistory(qbDialog,
                count, new QBEntityCallback<ArrayList<QBChatMessage>>() {
                    @Override
                    public void onSuccess(ArrayList<QBChatMessage> messages, Bundle args) {

                        Log.d(TAG, "onSuccess: " + new Gson().toJson(messages).toString());
                        // The newest messages should be in the end of list,
                        // so we need to reverse list to show messages in the right order
                        List<QBMessage> messageList = new ArrayList<QBMessage>();
                        for (int i = 0; i < messages.size(); i++) {
                            QBMessage qbMessage = new QBMessage();
                            qbMessage.setMessage(messages.get(i));
                            messageList.add(qbMessage);
                        }

                        Log.d(TAG, "onError:98746531264565987");
                        Collections.reverse(messageList);
                        if (chatAdapter == null) {
                            Log.d(TAG, "onError:897987");
                            chatAdapter = new ChatMessageAdapter(ChatsActivity.this,
                                    messageList);
                            if (unShownMessages != null && !unShownMessages.isEmpty()) {
                                List<QBMessage> chatList = chatAdapter.getList();
                                for (QBMessage message : unShownMessages) {
                                    if (!chatList.contains(message)) {
                                        chatAdapter.add(message);
                                    }
                                }
                            }
                            messagesListView.setAdapter(chatAdapter);
                            messagesListView.setAreHeadersSticky(false);
                            messagesListView.setDivider(null);
                            progressBar.setVisibility(View.GONE);
                        } else {
                            Log.d(TAG, "onError:8");
                            messagesListView.setVisibility(View.VISIBLE);
                            chatAdapter.addList(messageList);
                            messagesListView.setSelection(messages.size());
                            chatAdapter.notifyDataSetChanged();
                        }
                        Log.d(TAG, "onError: 888");
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        progressBar.setVisibility(View.GONE);
                        Log.d(TAG, "onError: ");
                        skipPagination -= ChatHelper.CHAT_HISTORY_ITEMS_PER_PAGE;
                    }
                });
        skipPagination += ChatHelper.CHAT_HISTORY_ITEMS_PER_PAGE;
        setPagination();
    }

    private void scrollMessageListDown() {
        messagesListView.setSelection(messagesListView.getCount() - 1);
    }

    private void initChatConnectionListener() {
        chatConnectionListener = new VerboseQbChatConnectionListener(getSnackbarAnchorView()) {
            @Override
            public void reconnectionSuccessful() {
                super.reconnectionSuccessful();
                skipPagination = 0;
                chatAdapter = null;
                switch (qbDialog.getType()) {
                    case PRIVATE:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loadChatHistory(0);
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
            }
        };
    }

    public class ChatMessageListener extends QbChatDialogMessageListenerImp {
        @Override
        public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {
//            showMessage(qbChatMessage);
            AppLog.d(TAG, "recive message " + new Gson().toJson(qbChatMessage));
            try {
                QBMessage qbMessage = new QBMessage();
                qbMessage.setMessage(qbChatMessage);
                qbMessage.setKittyId(kittyId);
                Integer userId = SharedPreferencesUtil.getQbUser().getId();

                if (!userId.equals(qbChatMessage.getSenderId())) {
                    setMessageSenderInfo(qbChatMessage.getSenderId());
                    AppLog.d(TAG, "SenderId" + qbChatMessage.getSenderId());
                    AppLog.d(TAG, "UserId" + userId);
                    chatMessageIds.add(qbChatMessage.getId());
                    bindMemberInfo(qbMessage);
                    // save locally
                    viewModel.saveMessage(qbMessage, kittyId);
                    showMessage(qbMessage);
                } else {
//  TODO
//     viewModel.updateMessage(qbChatMessage, getLastInsertedID(qbChatMessage));
                }

            } catch (Exception e) {
                AppLog.e(TAG, e.getMessage(), e);
            }
        }
    }

    private class NetworkStateUpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("NetworkStateUpdateReceiver$onReceive");
//            initQBSetting();
        }
    }


    private void createSession() {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!QbAuthUtils.isSessionActive()) {
                    recreateChatSession();
                } else {
                    onSessionCreated(true);
                }
            }
        });
    }

    private void recreateChatSession() {
        Log.d(TAG, "Need to recreate chat session");

        QBUser user = SharedPreferencesUtil.getQbUser();
        if (user == null) {
            throw new RuntimeException("User is null, can't restore session");
        }

        reloginToChat(user);
    }

    private void reloginToChat(final QBUser user) {
        ProgressDialogFragment.show(getSupportFragmentManager(), R.string.dlg_restoring_chat_session);

        ChatHelper.getInstance().login(user, new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void result, Bundle bundle) {
                Log.v(TAG, "Chat login onSuccess()");
                onSessionCreated(true);
            }

            @Override
            public void onError(QBResponseException e) {
                onSessionCreated(false);
            }
        });
    }

    private void setPagination() {
        if (chatAdapter != null)
            chatAdapter.setPaginationHistoryListener(new PaginationHistoryListener() {
                @Override
                public void downloadMore() {
                    if (chatAdapter.getCount() != 0)
                        loadChatHistory(chatAdapter.getCount());
                }
            });
    }

    private void bindView(ArrayList<QBMessage> messages) {
        chatAdapter = new ChatMessageAdapter(ChatsActivity.this, messages);
        chatAdapter.setOutGoingMessageBgColor(R.color.colorAccent);
        setPagination();
        messagesListView.setAdapter(chatAdapter);
        messagesListView.setAreHeadersSticky(false);
        messagesListView.setDivider(null);
    }


    private void loadData() {
        if (kittyId > 0) {
            viewModel.fetchKittyById(kittyId,
                    new DBQueryHandler.OnQueryHandlerListener<ArrayList<Kitty>>() {
                        @Override
                        public void onResult(ArrayList<Kitty> result) {
                            System.out.print(result.get(0).toString());
                            if (result.size() > 0) {
                                Kitty kitty = result.get(0);
                                qbDialog = kitty.getQbDialog();
                                AppLog.d(TAG, "DIALOG " + new Gson().toJson(qbDialog).toString());
                            }
                            setBasicSetting();

                        }
                    });
        } else {
            startViewBinding();
        }
    }

    private void setBasicSetting() {
        if (qbDialog != null) {
            initChat();
            qbDialog.addMessageListener(chatMessageListener);
//            initChatConnectionListener();

            setChatNameToActionBar();
            loadMemberProfile();
            startViewBinding();
        } else {
            AppLog.d(TAG, "null object");
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

    private void startViewBinding() {
        bindData();
        initQBSetting();
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
                            user.setFullName(Utils.getDisplayNameByPhoneNumber(ChatsActivity.this, user.getPhone()));
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
        if (ConnectivityUtils.checkInternetConnection(this)) {
            AppLog.e(TAG, "syncMessages: start");
            ChatSyncOperation operation = new ChatSyncOperation(this);
            operation.syncDialogMessages(kittyId, qbDialog,
                    chatAdapter.getCount(), new DataSyncListener() {
                        @Override
                        public void onCompleted(int itemCount) {
                            AppLog.e(TAG, "syncMessages: onCompleted(" + itemCount + ")");
                            if (itemCount > 0)
                                bindData();
                        }
                    });
        } else {
            AppLog.e(TAG, "syncMessages: NO INTER NET");
        }
    }

    private List<Integer> getWithoutLoginUserMemberList() {
        List<Integer> members = new ArrayList<>();
        members.addAll(qbDialog.getOccupants());
        return members;
    }

    private void bindData(final ArrayList<QBMessage> messages) {
        System.out.println("bindData(ArrayList<QBChatMessage> messages)");
        if (members != null && members.size() > 0) {
            new BaseAsyncTask<ArrayList<QBMessage>,
                    Void, ArrayList<QBMessage>>() {

                @Override
                public ArrayList<QBMessage>
                performInBackground(ArrayList<QBMessage>... params) throws Exception {
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

    private ArrayList<QBMessage> reconstructArrayWithMemberProfile(ArrayList<QBMessage> messages) {
        for (QBMessage message : messages) {
            bindMemberInfo(message);
        }
        return messages;
    }

    private void bindMemberInfo(QBMessage message) {
        try {
            if (members.size() > 0) {
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
            } else {
                AppLog.e(TAG, "No member found size is " + members.size());
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    private void initQBSetting() {
        System.out.println("initQBSetting");
        if (QBChatService.getInstance().getGroupChatManager() != null) {
            init();
        } else {
            loginOnQb();
        }
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


    private void loginOnQb() {
        if (ConnectivityUtils.checkInternetConnection(this)) {
            ChatHelper.getInstance().login(SharedPreferencesUtil.getQbUser(),
                    new QBEntityCallback<Void>() {
                        @Override
                        public void onSuccess(Void aVoid, Bundle bundle) {
                            createSession();
                        }

                        @Override
                        public void onError(QBResponseException e) {
                        }
                    });
        } else {
            createSession();
        }
    }

    /*TODO Message Listener Start*/

    private void initChatMessageListener() {
        qbDialog.addMessageSentListener(ChatsActivity.this);
        QBMessageStatusesManager manager =
                QBChatService.getInstance().getMessageStatusesManager();
        manager.addMessageStatusListener(ChatsActivity.this);
    }

    @Override
    public void processMessageDelivered(String messageID, String dialogID, Integer occupantsID) {
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
    public void processMessageRead(String messageID, String dialogID, Integer occupantsID) {
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

    @Override
    public void processMessageSent(String s,
                                   QBChatMessage qbChatMessage) {
        Log.d(TAG, "processMessageSent: ");
        try {
            AppLog.d(TAG, new Gson().toJson(qbChatMessage).toString());
//            int index = Integer.parseInt((String) qbChatMessage.getProperty(SENT_MESSAGE_INDEX));
//            int lastInsertedId = Integer.parseInt((String) qbChatMessage.getProperty(AppConstant.LAST_INSERTED_ID));
            int lastInsertedId = getLastInsertedID(qbChatMessage);
            int index = getSentMessageIndex(qbChatMessage);
            QBMessage message = chatAdapter.getItem(index);
            message.setSent(1);
            message.setMessage(qbChatMessage);
            chatAdapter.updateList(message, index);
            viewModel.updateSent(message.getMessage(), lastInsertedId);
            Log.i(TAG, "onMessageSent: " + qbChatMessage.getBody());
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }

    }

    @Override
    public void processMessageFailed(String s,
                                     QBChatMessage qbChatMessage) {
        Log.d(TAG, "processMessageFailed: ");
        try {
//            int index = Integer.parseInt((String) qbChatMessage.getProperty(SENT_MESSAGE_INDEX));
            int index = getSentMessageIndex(qbChatMessage);
            QBMessage message = chatAdapter.getItem(index);
            message.setFail(1);
            chatAdapter.updateList(message, index);
            viewModel.updateFail(message.getMessage(), kittyId);
            Log.i(TAG, "onMessageFail: ");
        } catch (Exception ex) {
            AppLog.e(TAG, ex.getMessage(), ex);
        }

    }

    private void initGlobalChatMessageListener() {
        QBRestChatService.getChatDialogById(qbDialog.getDialogId())
                .performAsync(new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog chatDialog, Bundle bundle) {
                        initChatMessageListener();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        log("Error loading dialog from global message listener: " + e);
                    }
                });

        QBChatService.getInstance().getIncomingMessagesManager()
                .addDialogMessageListener(chatMessageListener);
    }

    /*TODO Message Listener END*/

    /**
     * Get Last Message Index From Object
     *
     * @param message
     * @return
     */
    private int getLastInsertedID(QBChatMessage message) {
        try {
            String value = null;
            String str = message.getBody();
            String strLastIndex = "properties={last_inserted_id=";
            String[] values = str.split(",");
            for (int i = 0; i < values.length; i++) {
                String currentValue = values[i];
                if (currentValue.contains(strLastIndex)) {
                    value = currentValue.substring(strLastIndex.length() + 1
                            , currentValue.length());
                    AppLog.d(TAG, "last Value = " + value);
                }
            }

            if (value != null) {
                value = value.replace("}", "");
                return Integer.parseInt(value);

            } else {
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Get Sent Message Index From Object
     *
     * @param message
     * @return
     */
    private int getSentMessageIndex(QBChatMessage message) {
        String value = null;
        try {
            String str = message.getBody();
            String strSentMessageIndex = "sent_msg_index=";
            String[] values = str.split(",");
            for (int i = 0; i < values.length; i++) {
                String currentValue = values[i];
                if (currentValue.contains(strSentMessageIndex)) {
                    value = currentValue.substring(strSentMessageIndex.length() + 1
                            , currentValue.length());
                    AppLog.d(TAG, "send Value = " + value);
                    break;
                }
            }
            if (value != null) {
                value = value.replace("}", "");
                return Integer.parseInt(value);

            } else {
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}