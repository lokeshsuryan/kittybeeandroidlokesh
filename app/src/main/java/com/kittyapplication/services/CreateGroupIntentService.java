package com.kittyapplication.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;

import com.kittyapplication.AppApplication;
import com.kittyapplication.R;
import com.kittyapplication.chat.utils.chat.ChatHelper;
import com.kittyapplication.chat.utils.qb.QbDialogUtils;
import com.kittyapplication.chat.utils.qb.callback.QBGetGroupID;
import com.kittyapplication.model.CreateGroup;
import com.kittyapplication.model.OfflineDao;
import com.kittyapplication.model.ServerResponse;
import com.kittyapplication.rest.Singleton;
import com.kittyapplication.sqlitedb.SqlDataSetTask;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.PreferanceUtils;
import com.kittyapplication.utils.Utils;
import com.quickblox.chat.QBGroupChat;
import com.quickblox.chat.QBGroupChatManager;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smackx.muc.DiscussionHistory;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by HalfBloodPrince(RIONTECH)
 * Riontech on 11/10/16.
 */

public class CreateGroupIntentService extends IntentService {
    private static final String TAG = CreateGroupIntentService.class.getSimpleName();
    private Context mContext;
    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotifyManager;
    private CreateGroup mGroup;
    private int mNotificationId = 1;
    private QBChatDialog mDialog;
    private String mMessage;

    public CreateGroupIntentService() {
        super("CreateGroupIntentService");
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public CreateGroupIntentService(String name) {
        super(name);
        mContext = this;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        AppLog.d("", "Inside onHandleIntent of service");
        mContext = this;
        mGroup = AppApplication.getInstance().getGroup();
        if (mGroup != null)
            createGroupToServer();
    }

    /**
     * display progressbar notification
     */
    private void showNotification() {
        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle(mContext.getResources().getString(R.string.app_name))
                .setContentText(mContext.getResources().getString(R.string.notification_create_group_txt, mGroup.getName())).setSmallIcon(R.drawable.ic_noti_small);
        mBuilder.setProgress(0, 0, true);
        mNotifyManager.notify(mNotificationId, mBuilder.build());
        mBuilder.setAutoCancel(true);

    }

    /**
     * call create group task
     */
    private void createGroupToServer() {
        if (Utils.checkInternetConnection(mContext)) {
            showNotification();
            new CreateGroupAsyncTask().execute();
        }
    }

    /**
     * send empty message in group
     *
     * @param message
     */
    private void sendMessage(final QBChatDialog dialog, String message) {
        // TODO Join group and send message
        DiscussionHistory history = new DiscussionHistory();
        history.setMaxStanzas(0);

        final String finalMessage = message;

        ChatHelper.getInstance().join(dialog, new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                QBChatMessage chatMessage = new QBChatMessage();
                chatMessage.setBody(finalMessage);
                chatMessage.setProperty("save_to_history", "1"); // Save to Chat 2.0 history
                try {
                    dialog.sendMessage(chatMessage);
                } catch (SmackException.NotConnectedException | IllegalStateException e) {
                    AppLog.e(TAG, e.getMessage(), e);
                }

                sendBroadCast();
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });
    }

    /**
     * callback server
     */
    private Callback<ServerResponse<OfflineDao>> getGroupCallback = new Callback<ServerResponse<OfflineDao>>() {
        @Override
        public void onResponse(Call<ServerResponse<OfflineDao>> call, Response<ServerResponse<OfflineDao>> response) {
            if (response.code() == 200) {
                if (response.body().getResponse() == AppConstant.RESPONSE_SUCCESS) {

                    // insert data into db
                    new SqlDataSetTask(mContext, response.body().getData());

                    // remove progress bar notification
                    mNotifyManager.cancel(mNotificationId);

                    // show create group success notification
//                    NotificationUtils.showNotification(mContext,
//                            HomeActivity.class,
//                            mContext.getResources().getString(R.string.app_name),
//                            mContext.getResources().getString(R.string.group_created_success_new_two, mGroup.getName()),
//                            R.drawable.ic_noti_small, (int) System.currentTimeMillis());

                    sendMessage(mDialog, mMessage);

                } else {
                    AppLog.d(TAG, response.body().getMessage());
                }
            } else {
                AppLog.d(TAG, response.body().getMessage());
            }
        }

        @Override
        public void onFailure(Call<ServerResponse<OfflineDao>> call, Throwable t) {


            try {
                AppLog.e(TAG, t.getMessage());
            } catch (Exception e) {
                AppLog.e(TAG, e.getMessage(), e);
            }
        }
    };


    /**
     * Create Group Async Task
     */
    public class CreateGroupAsyncTask
            extends AsyncTask<Void, Void, HashMap<String, String>> {

        public CreateGroupAsyncTask() {
        }

        @Override
        protected HashMap<String, String> doInBackground(Void... params) {
            //get id nd name of user from group
            HashMap<String, String> map = new HashMap<>();
            for (int i = 0; i < mGroup.getGroupMember().size(); i++) {
                if (Utils.isValidString(mGroup.getGroupMember().get(i).getID())) {
                    String ids = mGroup.getGroupMember().get(i).getID();
                    String names = mGroup.getGroupMember().get(i).getFullName();


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
                        map.put(mGroup.getGroupMember().get(i).getID(),
                                mGroup.getGroupMember().get(i).getFullName());
                    }

                }
            }
            return map;
        }

        @Override
        protected void onPostExecute(HashMap<String, String> avoid) {
            super.onPostExecute(avoid);
            // create group in quick blox

            QbDialogUtils.createGroupChatDialog(avoid,
                    mGroup.getName()
                    , ""
                    , new QBGetGroupID() {
                        @Override
                        public void getQuickBloxGroupID(QBChatDialog dialog, String message) {

                            // create group in server
                            mGroup.setQuickGroupId(dialog.getDialogId());
                            Call<ServerResponse<OfflineDao>> call = Singleton.getInstance()
                                    .getRestOkClient().addGroup(
                                            PreferanceUtils.getLoginUserObject(mContext)
                                                    .getUserID(), mGroup);
                            call.enqueue(getGroupCallback);

                            mDialog = dialog;
                            mMessage = message;
                        }

                        @Override
                        public void getError(Exception e) {
                            AppLog.e(TAG, e.getMessage(), e);
                        }
                    });
        }
    }


    /**
     * send broadCast
     */
    private void sendBroadCast() {
        // start broad cast receiver for create group
        AppApplication.getInstance().setIsCreateGroup(false);
        Intent intent = new Intent(AppConstant.CREATE_GROUP_ACTION);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);


    }
}
