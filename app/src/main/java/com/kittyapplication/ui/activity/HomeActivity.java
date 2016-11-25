package com.kittyapplication.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.kittyapplication.AppApplication;
import com.kittyapplication.R;
import com.kittyapplication.chat.utils.chat.ChatHelper;
import com.kittyapplication.chat.utils.qb.QbDialogHolder;
import com.kittyapplication.chat.utils.qb.VerboseQbChatConnectionListener;
import com.kittyapplication.core.utils.constant.GcmConsts;
import com.kittyapplication.listener.SearchListener;
import com.kittyapplication.ui.fragment.BeeChatFragment;
import com.kittyapplication.ui.fragment.KittiesFragment;
import com.kittyapplication.ui.viewmodel.HomeViewModel;
import com.kittyapplication.utils.AlertDialogUtils;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.KittyPrefHolder;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBGroupChat;
import com.quickblox.chat.QBGroupChatManager;
import com.quickblox.chat.QBPrivateChat;
import com.quickblox.chat.QBPrivateChatManager;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBGroupChatManagerListener;
import com.quickblox.chat.listeners.QBMessageListener;
import com.quickblox.chat.listeners.QBPrivateChatManagerListener;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.core.request.QBRequestGetBuilder;

import org.jivesoftware.smack.ConnectionListener;

import java.util.ArrayList;

/**
 * Created by Pintu Riontech on 7/8/16.
 */
public class HomeActivity extends ChatBaseActivity implements SearchListener {
    private static final String TAG = HomeActivity.class.getSimpleName();
    private TabLayout mTabViewHome;
    private ViewPager mVpHome;
    private HomeViewModel mViewModel;

    private QBRequestGetBuilder requestBuilder;
    public boolean isActivityForeground;
    private QBPrivateChatManagerListener privateChatManagerListener;
    private QBGroupChatManagerListener groupChatManagerListener;
    private ConnectionListener chatConnectionListener;
    private BroadcastReceiver pushBroadcastReceiver;
    private BeeChatFragment chatsFragment;
    private KittiesFragment mKittiesFragment;
    private BeeChatFragment mBeeChatFragment;
    private static final String QUICK_BLOX_PASSWORD = "KittyBeeArun";
    private boolean isLoading = false;
    private int skip = 0;
    private UpdateGroupReceiver updateGroupReceiver;
    private boolean isUpdatedData = false;

    private WorkerThread mQbDialogWorkerThread;

    private ArrayList<String> chatMessageIds;
    private QBDialog updateDialog;
    private ActionMode currentActionMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(HomeActivity.this).inflate(
                R.layout.activity_home, null);
        addLayoutToContainer(view);
        AppLog.d(TAG, "GCM" + AppApplication.getGCMId());

        pushBroadcastReceiver = new PushBroadcastReceiver();

        privateChatManagerListener = new QBPrivateChatManagerListener() {
            @Override
            public void chatCreated(QBPrivateChat qbPrivateChat, boolean createdLocally) {
                Log.d(TAG, "privateChatManagerListener$chatCreated");
//                loadDialogsFromQb();
                if (!createdLocally) {
                    qbPrivateChat.addMessageListener(privateChatMessageListener);
                }
            }
        };

        groupChatManagerListener = new QBGroupChatManagerListener() {
            @Override
            public void chatCreated(QBGroupChat qbGroupChat) {
                requestBuilder.setSkip(0);
                Log.d(TAG, "groupChatManagerListener$chatCreated");
//                loadDialogsFromQb();
                qbGroupChat.addMessageListener(new QBMessageListener<QBGroupChat>() {
                    @Override
                    public void processMessage(QBGroupChat qbGroupChat, QBChatMessage qbChatMessage) {
                        AppLog.d(TAG, "Message Received :: >" + qbChatMessage.getBody());
                        mViewModel.updatedMessage(qbChatMessage);
                    }

                    @Override
                    public void processError(QBGroupChat qbGroupChat, QBChatException e, QBChatMessage qbChatMessage) {

                    }
                });
            }
        };

        requestBuilder = new QBRequestGetBuilder();
        chatConnectionListener = new VerboseQbChatConnectionListener(getSnackbarAnchorView()) {

            @Override
            public void reconnectionSuccessful() {
                super.reconnectionSuccessful();
                Log.i(TAG, "reconnectionSuccessful: ");

//                requestBuilder.setSkip(0);
            }
        };
        initUI(view);
        setUpSearchBar(getSearchbar(), this);

    }


    private void registerUpdateGroupReceiver() {
        updateGroupReceiver = new UpdateGroupReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(AppConstant.ACTION_UPDATE_GROUP);
        LocalBroadcastManager.getInstance(this).
                registerReceiver(updateGroupReceiver, filter);
    }

    private void unregisterUpdateGroupReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateGroupReceiver);
    }

    private void initUI(View view) {
        mTabViewHome = (TabLayout) view.findViewById(R.id.tabHomeScreen);
        mVpHome = (ViewPager) view.findViewById(R.id.vpHomeScreen);
        mViewModel = new HomeViewModel(this);
        visibleLeftIcon();
        mViewModel.setUpTabView(mTabViewHome, mVpHome);

        /*boolean isSync = AccountUtils.isSyncEnabled(this);
        if (!isSync) {
            AlertDialogUtils.showYesNoAlert(HomeActivity.this,
                    getResources().getString(R.string.sync_disable_msg),
                    new MiddleOfKittyListener() {
                        @Override
                        public void clickOnYes() {
                            if (isReadStatusForSync())
                                enableSync();
                        }

                        @Override
                        public void clickOnNo() {

                        }
                    });
        }*/
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // added patch for create group to avoid two time
        // reload data call in create group with rule & without rules(later)
        /*if (!AppApplication.getInstance().isCreateGroup()) {
            mViewModel.refreshChatData();
        }*/
    }

    @Override
    protected void onStart() {
        super.onStart();
        /*if (!AccountUtils.isAccountExist(this)) {
            AccountUtils.addAccount(this,
                    PreferanceUtils.getLoginUserObject(this).getUserID(), QUICK_BLOX_PASSWORD);
        }*/
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
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerUpdateGroupReceiver();

        ChatHelper.getInstance().addConnectionListener(chatConnectionListener);
        isActivityForeground = true;

        LocalBroadcastManager.getInstance(this).registerReceiver(pushBroadcastReceiver,
                new IntentFilter(GcmConsts.ACTION_NEW_GCM_EVENT));
/*
        if (AppApplication.getInstance().isRefresh()) {
            AppApplication.getInstance().setRefresh(false);
            try {
//                if (chatsFragment != null)
//                    chatsFragment.refreshChatsAsync();
//                if (mKittiesFragment != null)
//                    mKittiesFragment.refreshChatsAsync();
            } catch (Exception e) {
                AppLog.e(TAG, e.getMessage(), e);
            }
        }*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        ChatHelper.getInstance().removeConnectionListener(chatConnectionListener);
        isActivityForeground = false;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(pushBroadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterQbChatListeners();
        unregisterUpdateGroupReceiver();
    }

    public View getSnackbarAnchorView() {
        return findViewById(R.id.layout_draw);
    }


    @Override
    public void onSessionCreated(boolean success) {
        if (success) {
//            loadDialogsFromQb();
            registerQbChatListeners();
        }
    }

    /**
     *
     */
    QBMessageListener<QBPrivateChat> privateChatMessageListener = new QBMessageListener<QBPrivateChat>() {
        @Override
        public void processMessage(QBPrivateChat privateChat, final QBChatMessage chatMessage) {
            requestBuilder.setSkip(0);
            if (isActivityForeground) {
                AppLog.d(TAG, "Message Received :: >" + chatMessage.getBody());
                mViewModel.updatedMessage(chatMessage);
            }
        }

        @Override
        public void processError(QBPrivateChat privateChat, QBChatException error, QBChatMessage originMessage) {
        }
    };

    private void registerQbChatListeners() {
        QBPrivateChatManager privateChatManager = QBChatService.getInstance().getPrivateChatManager();
        QBGroupChatManager groupChatManager = QBChatService.getInstance().getGroupChatManager();

        if (privateChatManager != null) {
            privateChatManager.addPrivateChatManagerListener(privateChatManagerListener);
        }

        if (groupChatManager != null) {
            groupChatManager.addGroupChatManagerListener(groupChatManagerListener);
        }
    }

    private void unregisterQbChatListeners() {
        QBPrivateChatManager privateChatManager = QBChatService.getInstance().getPrivateChatManager();
        QBGroupChatManager groupChatManager = QBChatService.getInstance().getGroupChatManager();
        if (privateChatManager != null) {
            privateChatManager.removePrivateChatManagerListener(privateChatManagerListener);
        }

        if (groupChatManager != null) {
            groupChatManager.removeGroupChatManagerListener(groupChatManagerListener);
        }
    }

    public void loadDialogsFromQb() {
        mQbDialogWorkerThread = new WorkerThread("QBWorkerThread");
        mQbDialogWorkerThread.start();
        mQbDialogWorkerThread.prepareHandler();
        mQbDialogWorkerThread.postTask(task);
    }


    public void setChatsFragment(BeeChatFragment chatsFragment) {
        this.chatsFragment = chatsFragment;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            AppLog.d(TAG, "onActivityResult");
            if (resultCode == RESULT_OK) {
                switch (requestCode) {
                    case AppConstant.REQUEST_UPDATE_DIALOG:
                        DialogWorkerThread workerThread = new
                                DialogWorkerThread("DialogWorkerThread", data);
                        workerThread.start();
                        workerThread.prepareHandler();
                        workerThread.postTask(dialogTask);
                        break;
                }
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }


    private class PushBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            Bundle data = intent.getBundleExtra(GcmConsts.EXTRA_GCM_MESSAGE);
            String message = data.getString(GcmConsts.EXTRA_GCM_MESSAGE);
            String dialogId = data.getString("dialog_id");

            QBChatMessage chatMessage = new QBChatMessage();
            chatMessage.setBody(message);
            chatMessage.setDialogId(dialogId);
            mViewModel.updatedMessage(chatMessage);
//            String message = intent.getStringExtra(GcmConsts.EXTRA_GCM_MESSAGE);
//            Log.i(TAG, "Received broadcast " + intent.getAction() + " with data: " + message);
//            loadDialogsFromQb(true, true);
        }
    }

    private void markMessagesRead(StringifyArrayList<String> messagesIds, String dialogId) {
        if (messagesIds.size() > 0) {
            QBChatService.markMessagesAsRead(dialogId, messagesIds, new QBEntityCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid, Bundle bundle) {
//                    loadDialogsFromQb();
                }

                @Override
                public void onError(QBResponseException e) {

                }
            });
        } else {
//            loadDialogsFromQb(true, true);
        }
    }

    private class UpdateGroupReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            AppLog.d(TAG, "On Receiver UPDATE");
//            if (chatsFragment != null)
//                chatsFragment.updateGroupList();
//            if (mKittiesFragment != null)
//                mKittiesFragment.updateList();
            AppLog.d(TAG, "On Receiver call");
        }
    }


    /**
     *
     */
    private class WorkerThread extends HandlerThread {
        private Handler mWorkerHandler;

        WorkerThread(String name) {
            super(name);
        }

        void postTask(Runnable task) {
            mWorkerHandler.post(task);
        }

        void prepareHandler() {
            mWorkerHandler = new Handler(getLooper());
        }
    }

    /**
     *
     */
    Runnable task = new Runnable() {
        @Override
        public void run() {
            if (!isLoading) {
                isLoading = true;
                ChatHelper.getInstance().getDialogs(requestBuilder, new QBEntityCallback<ArrayList<QBDialog>>() {
                    @Override
                    public void onSuccess(ArrayList<QBDialog> dialogs, Bundle bundle) {
                        QbDialogHolder.getInstance().addDialogs(dialogs);
                        int total = bundle.getInt("total_entries");

                        isLoading = false;
                        skip = QbDialogHolder.getInstance().getDialogList().size();
                        mQbDialogWorkerThread.quit();

                        if (total > skip) {
                            loadDialogsFromQb();
                        } else {
                            skip = 0;
                            /*if (mBeeChatFragment != null) {
                                mBeeChatFragment.updateList();
                            }*/
                        }
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        isLoading = false;
                        skip = 0;
                        mQbDialogWorkerThread.quit();
                    }
                }, skip);
            }
        }
    };

    public void setKittiesFragment(KittiesFragment kitteisFragment) {
        mKittiesFragment = kitteisFragment;
    }

    public void setBeeChatFragment(BeeChatFragment beeChatFragment) {
        mBeeChatFragment = beeChatFragment;
    }

    /**
     *
     */
    private class DialogWorkerThread extends HandlerThread {
        private Handler mWorkerHandler;

        DialogWorkerThread(String name, Intent data) {
            super(name);

            QBDialog dialog = (QBDialog) data.getExtras()
                    .getSerializable(AppConstant.UPDATED_DIALOG);
            ArrayList<String> ids = (ArrayList<String>) data
                    .getSerializableExtra(ChatActivity.EXTRA_MARK_READ);
            updateDialog = dialog;
            chatMessageIds = ids;
        }

        void postTask(Runnable task) {
            mWorkerHandler.post(task);
        }

        void prepareHandler() {
            mWorkerHandler = new Handler(getLooper());
        }
    }

    /**
     *
     */
    Runnable dialogTask = new Runnable() {
        @Override
        public void run() {
            try {
                if (updateDialog != null) {
                    AppLog.d(TAG, "Dialog::" + updateDialog);
                    mViewModel.updatedDialog(updateDialog);
                    QbDialogHolder.getInstance().changeIndex(0, updateDialog);
                    KittyPrefHolder.getInstance().updateQBDialog(0, updateDialog);
                    final StringifyArrayList<String> messagesIds = new StringifyArrayList<>();
                    messagesIds.addAll(chatMessageIds);
                    markMessagesRead(messagesIds, updateDialog.getDialogId());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void getSearchString(String str) {
        if (mViewModel != null)
            mViewModel.filterData(str);
    }

    @Override
    public void onSearchBarVisible() {

    }

    @Override
    public void onSearchBarHide() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mInflater = getMenuInflater();
        mInflater.inflate(R.menu.home_menu, menu);
        MenuItem item = menu.findItem(R.id.menu_home_search);
        getSearchbar().setMenuItem(item);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                toggle();
                break;
            case R.id.menu_home_add_group:
                AlertDialogUtils.showCreateKittyDialog(this);
                break;
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    public ActionMode getCurrentActionMode() {
        return currentActionMode;
    }

    public void setCurrentActionMode(ActionMode currentActionMode) {
        this.currentActionMode = currentActionMode;
    }
}