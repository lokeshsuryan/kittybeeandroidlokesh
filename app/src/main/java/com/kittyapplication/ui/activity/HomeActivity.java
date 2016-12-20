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
import com.kittyapplication.chat.managers.DialogsManager;
import com.kittyapplication.chat.utils.chat.ChatHelper;
import com.kittyapplication.chat.utils.chat.QbChatDialogMessageListenerImp;
import com.kittyapplication.chat.utils.qb.VerboseQbChatConnectionListener;
import com.kittyapplication.core.utils.constant.GcmConsts;
import com.kittyapplication.listener.SearchListener;
import com.kittyapplication.rest.Singleton;
import com.kittyapplication.ui.fragment.BeeChatFragment;
import com.kittyapplication.ui.fragment.KittiesFragment;
import com.kittyapplication.ui.viewmodel.HomeViewModel;
import com.kittyapplication.utils.AlertDialogUtils;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.LocationUtils;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBGroupChat;
import com.quickblox.chat.QBGroupChatManager;
import com.quickblox.chat.QBIncomingMessagesManager;
import com.quickblox.chat.QBPrivateChat;
import com.quickblox.chat.QBPrivateChatManager;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.QBSystemMessagesManager;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBChatDialogMessageListener;
import com.quickblox.chat.listeners.QBGroupChatManagerListener;
import com.quickblox.chat.listeners.QBMessageListener;
import com.quickblox.chat.listeners.QBPrivateChatManagerListener;
import com.quickblox.chat.listeners.QBSystemMessageListener;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.core.request.QBRequestGetBuilder;

import org.jivesoftware.smack.ConnectionListener;

import java.util.ArrayList;

/**
 * Created by Pintu Riontech on 7/8/16.
 */
public class HomeActivity extends ChatBaseActivity implements SearchListener, DialogsManager.ManagingDialogsCallbacks {
    private static final String TAG = HomeActivity.class.getSimpleName();

    private QBChatDialogMessageListener allDialogsMessagesListener;
    private SystemMessagesListener systemMessagesListener;
    private QBSystemMessagesManager systemMessagesManager;
    private QBIncomingMessagesManager incomingMessagesManager;
    private DialogsManager dialogsManager;


    private TabLayout mTabViewHome;
    private ViewPager mVpHome;
    private HomeViewModel mViewModel;

    private QBRequestGetBuilder requestBuilder;
    public boolean isActivityForeground;
    private BroadcastReceiver pushBroadcastReceiver;
    private UpdateGroupReceiver updateGroupReceiver;
    private ActionMode currentActionMode;

    private LocationUtils mLocationUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(HomeActivity.this).inflate(
                R.layout.activity_home, null);
        addLayoutToContainer(view);
        AppLog.d(TAG, "GCM" + AppApplication.getGCMId());

        pushBroadcastReceiver = new PushBroadcastReceiver();
        requestBuilder = new QBRequestGetBuilder();

        /**
         * Upgraded version code
         */

        if (isAppSessionActive) {
            allDialogsMessagesListener = new AllDialogsMessageListener();
            systemMessagesListener = new SystemMessagesListener();
        }

        dialogsManager = new DialogsManager();

        initUI(view);
        setUpSearchBar(getSearchbar(), this);
        startLocationService();
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
            registerQbChatListeners();
        }
    }

    private void registerQbChatListeners() {
        incomingMessagesManager = QBChatService.getInstance().getIncomingMessagesManager();
        systemMessagesManager = QBChatService.getInstance().getSystemMessagesManager();

        if (incomingMessagesManager != null) {
            incomingMessagesManager.addDialogMessageListener(allDialogsMessagesListener != null
                    ? allDialogsMessagesListener : new AllDialogsMessageListener());
        }

        if (systemMessagesManager != null) {
            systemMessagesManager.addSystemMessageListener(systemMessagesListener != null
                    ? systemMessagesListener : new SystemMessagesListener());
        }

        dialogsManager.addManagingDialogsCallbackListener(this);
    }

    private void unregisterQbChatListeners() {

        if (incomingMessagesManager != null) {
            incomingMessagesManager.removeDialogMessageListrener(allDialogsMessagesListener);
        }

        if (systemMessagesManager != null) {
            systemMessagesManager.removeSystemMessageListener(systemMessagesListener);
        }

        dialogsManager.removeManagingDialogsCallbackListener(this);
    }

    public void setChatsFragment(BeeChatFragment chatsFragment) {
//        this.chatsFragment = chatsFragment;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            AppLog.d(TAG, "onActivityResult");
            if (resultCode == RESULT_OK) {
                switch (requestCode) {
                    case AppConstant.REQUEST_UPDATE_DIALOG:
                        updateItem(data);
                        break;

                    case 1000:
                        if (mLocationUtils != null)
                            mLocationUtils.initGoogleApi();
                        break;
                }
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    private void updateItem(Intent data) {
        QBChatDialog dialog = (QBChatDialog) data.getExtras()
                .getSerializable(AppConstant.UPDATED_DIALOG);
        ArrayList<String> ids = (ArrayList<String>) data
                .getSerializableExtra(ChatActivity.EXTRA_MARK_READ);

        int index = data.getIntExtra(AppConstant.EXTRA_CLICKED_ITEM_INDEX, -1);
        try {
            if (dialog != null) {
                AppLog.d(TAG, "Dialog::" + dialog);
                mViewModel.updatedDialog(dialog, index);
//                QbDialogHolder.getInstance().changeIndex(0, dialog);
//                KittyPrefHolder.getInstance().updateQBDialog(0, dialog);
                final StringifyArrayList<String> messagesIds = new StringifyArrayList<>();
                messagesIds.addAll(ids);
                markMessagesRead(messagesIds, dialog.getDialogId());
            }
        } catch (Exception e) {
            e.printStackTrace();
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
            QBRestChatService.markMessagesAsRead(dialogId, messagesIds);
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

    public void setKittiesFragment(KittiesFragment kitteisFragment) {
//        mKittiesFragment = kitteisFragment;
    }

    public void setBeeChatFragment(BeeChatFragment beeChatFragment) {
//        mBeeChatFragment = beeChatFragment;
    }

    @Override
    public void getSearchString(String str) {
        if (mViewModel != null)
            mViewModel.filterData(str);
    }

    @Override
    public void onSearchBarVisible() {
        if (mViewModel != null)
            mViewModel.isFilterApply(true);
    }

    @Override
    public void onSearchBarHide() {
        if (mViewModel != null)
            mViewModel.isFilterApply(false);
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

    private void startLocationService() {
        mLocationUtils = Singleton.getInstance().getLocationUtils();
        mLocationUtils.setActivity(this);
        mLocationUtils.initGoogleApi();
       /* if (mLocationUtils.getGoogleApiClient() != null) {
            if (mLocationUtils.getGoogleApiClient().isConnected())
                mLocationUtils.createLocationRequest();
            else
                mLocationUtils.initGoogleApi();
        }*/
    }

    /******************************* Upgrade version code **********************/

    @Override
    public void onDialogCreated(QBChatDialog chatDialog) {

    }

    @Override
    public void onDialogUpdated(String chatDialog) {

    }

    @Override
    public void onNewDialogLoaded(QBChatDialog chatDialog) {

    }

    private class SystemMessagesListener implements QBSystemMessageListener {
        @Override
        public void processMessage(final QBChatMessage qbChatMessage) {
//            dialogsManager.onSystemMessageReceived(qbChatMessage);
            if (isActivityForeground) {
                AppLog.d(TAG, "Message Received :: >" + qbChatMessage.getBody());
                mViewModel.updatedMessage(qbChatMessage);
            }
        }

        @Override
        public void processError(QBChatException e, QBChatMessage qbChatMessage) {

        }
    }

    private class AllDialogsMessageListener extends QbChatDialogMessageListenerImp {
        @Override
        public void processMessage(final String dialogId, final QBChatMessage qbChatMessage, Integer senderId) {
//            if (!senderId.equals(ChatHelper.getCurrentUser().getId())) {
//                dialogsManager.onGlobalMessageReceived(dialogId, qbChatMessage);
//            }

            if (isActivityForeground) {
                AppLog.d(TAG, "Message Received :: >" + qbChatMessage.getBody());
                mViewModel.updatedMessage(qbChatMessage);
            }
        }
    }
}