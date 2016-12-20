package com.kittyapplication.chat.utils.chat;

import android.os.Bundle;
import android.util.Log;

import com.kittyapplication.chat.R;
import com.kittyapplication.chat.utils.SharedPreferencesUtil;
import com.kittyapplication.chat.utils.qb.QbDialogUtils;
import com.kittyapplication.chat.utils.qb.QbUsersHolder;
import com.kittyapplication.chat.utils.qb.callback.QbEntityCallbackTwoTypeWrapper;
import com.kittyapplication.chat.utils.qb.callback.QbEntityCallbackWrapper;
import com.kittyapplication.core.CoreApp;
import com.kittyapplication.core.utils.ConnectivityUtils;
import com.kittyapplication.core.utils.Toaster;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.request.QBDialogRequestBuilder;
import com.quickblox.chat.utils.DialogUtils;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.LogLevel;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBProgressCallback;
import com.quickblox.core.QBSettings;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.core.request.QueryRule;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.DiscussionHistory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ChatHelper {
    private static final String TAG = ChatHelper.class.getSimpleName();
    private static final int AUTO_PRESENCE_INTERVAL_IN_SECONDS = 30;
    public static final int ITEMS_PER_PAGE = 100;
    private static final int TIMEOUT = 120000;
    private static final int CHAT_SOCKET_TIMEOUT = 0;
    public static final int DIALOG_ITEMS_PER_PAGE = 100;
    public static final int CHAT_HISTORY_ITEMS_PER_PAGE = 50;
    private static final String CHAT_HISTORY_ITEMS_SORT_FIELD = "date_sent";

    private static ChatHelper instance;

    private QBChatService qbChatService;

    public static synchronized ChatHelper getInstance() {
        if (instance == null) {
            QBSettings.getInstance().setLogLevel(LogLevel.DEBUG);
            QBChatService.setDebugEnabled(true);
            QBChatService.setConfigurationBuilder(buildChatConfigs());
            QBChatService.setDefaultAutoSendPresenceInterval(AUTO_PRESENCE_INTERVAL_IN_SECONDS);
            instance = new ChatHelper();
        }
        return instance;
    }

    public static QBUser getCurrentUser() {
        return QBChatService.getInstance().getUser();
    }

    private ChatHelper() {
        qbChatService = QBChatService.getInstance();
        qbChatService.setUseStreamManagement(true);

//        qbChatService = QBChatService.getInstance();
//        QBUser user = SharedPreferencesUtil.getQbUser();
//        qbChatService.setUseStreamManagement(true);
//        try {
//            qbChatService.login(user);
//        } catch (XMPPException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (SmackException e) {
//            e.printStackTrace();
//        }
    }

    private static QBChatService.ConfigurationBuilder buildChatConfigs() {
        QBChatService.ConfigurationBuilder configurationBuilder = new QBChatService.ConfigurationBuilder();
        configurationBuilder.setKeepAlive(true)
                .setUseTls(true)
                .setSocketTimeout(TIMEOUT)
                .setAutojoinEnabled(true);

        return configurationBuilder;
    }

    public void addConnectionListener(ConnectionListener listener) {
        qbChatService.addConnectionListener(listener);
    }

    public void removeConnectionListener(ConnectionListener listener) {
        qbChatService.removeConnectionListener(listener);
    }

    public void login(final QBUser user, final QBEntityCallback<Void> callback) {
        if (ConnectivityUtils.checkInternetConnection(CoreApp.getInstance())) {
            // Create REST API session on QuickBlox version 3.1.0
            QBAuth.createSession(user).performAsync(new QbEntityCallbackTwoTypeWrapper<QBSession, Void>(callback) {
                @Override
                public void onSuccess(QBSession session, Bundle args) {
                    try {
                        if (session != null && session.getUserId() != null) {
                            user.setId(session.getUserId());
                            loginToChat(user, new QbEntityCallbackWrapper<>(callback));
                        } else {
                            callback.onSuccess(null, args);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        // Create REST API session on QuickBlox version 3.2.0
//        QBUsers.signIn(user).performAsync(new QbEntityCallbackTwoTypeWrapper<QBUser, Void>(callback) {
//            @Override
//            public void onSuccess(QBUser qbUser, Bundle args) {
//                user.setId(qbUser.getId());
//                loginToChat(user, new QbEntityCallbackWrapper<>(callback));
//            }
//        });
    }

    private void loginToChat(final QBUser user, final QBEntityCallback<Void> callback) {
        if (qbChatService.isLoggedIn()) {
            callback.onSuccess(null, null);
            return;
        }

        qbChatService.login(user, new QbEntityCallbackWrapper<Void>(callback) {
            @Override
            public void onSuccess(Void o, Bundle bundle) {
                super.onSuccess(o, bundle);
            }

            @Override
            public void onError(QBResponseException e) {
                super.onError(e);
            }
        });
    }

    public void join(QBChatDialog chatDialog, final QBEntityCallback<Void> callback) {
//        chatDialog.initForChat(qbChatService);
        DiscussionHistory history = new DiscussionHistory();
        history.setMaxStanzas(0);
        chatDialog.join(history, callback);
    }

    public void leaveChatDialog(QBChatDialog chatDialog) throws XMPPException, SmackException.NotConnectedException {
        chatDialog.leave();
    }

    public void logout(final QBEntityCallback<Void> callback) {
        qbChatService.logout(callback);
    }

    public void createDialogWithSelectedUsers(final List<QBUser> users,
                                              final QBEntityCallback<QBChatDialog> callback) {
        if (ConnectivityUtils.checkInternetConnection(CoreApp.getInstance())) {
            QBRestChatService.createChatDialog(QbDialogUtils.createDialog(users)).performAsync(
                    new QbEntityCallbackWrapper<QBChatDialog>(callback) {
                        @Override
                        public void onSuccess(QBChatDialog dialog, Bundle args) {
                            QbUsersHolder.getInstance().putUsers(users);
                            super.onSuccess(dialog, args);
                        }

                        @Override
                        public void onError(QBResponseException error) {
                            super.onError(error);
                        }
                    });
        } else {
            QbUsersHolder.getInstance().putUsers(users);
            QBChatDialog dialog = QbDialogUtils.createDialog(users);
            dialog.setDialogId("0");
            callback.onSuccess(dialog, new Bundle());
        }
    }

    public void deleteDialogs(Collection<QBChatDialog> dialogs, final QBEntityCallback<ArrayList<String>> callback) {
        StringifyArrayList<String> dialogsIds = new StringifyArrayList<>();
        for (QBChatDialog dialog : dialogs) {
            dialogsIds.add(dialog.getDialogId());
        }

        QBRestChatService.deleteDialogs(dialogsIds, false, null).performAsync(callback);
    }

    public void deleteDialog(QBChatDialog qbDialog, QBEntityCallback<Void> callback) {
        if (ConnectivityUtils.checkInternetConnection(CoreApp.getInstance())) {
            if (qbDialog.getType() == QBDialogType.PUBLIC_GROUP){
                Toaster.shortToast(R.string.public_group_chat_cannot_be_deleted);
            } else {
                QBRestChatService.deleteDialog(qbDialog.getDialogId(), false)
                        .performAsync(new QbEntityCallbackWrapper<Void>(callback));
            }
        }
    }

    public void exitFromDialog(QBChatDialog qbDialog, QBEntityCallback<QBChatDialog> callback) {
        try {
            leaveChatDialog(qbDialog);
        } catch (XMPPException | SmackException.NotConnectedException e) {
            callback.onError(new QBResponseException(e.getMessage()));
        }

        QBDialogRequestBuilder qbRequestBuilder = new QBDialogRequestBuilder();
        qbRequestBuilder.removeUsers(SharedPreferencesUtil.getQbUser().getId());

        QBRestChatService.updateGroupChatDialog(qbDialog, qbRequestBuilder).performAsync(callback);
    }

    public void updateDialogUsers(QBChatDialog qbDialog,
                                  final List<QBUser> newQbDialogUsersList,
                                  QBEntityCallback<QBChatDialog> callback) {
        if (ConnectivityUtils.checkInternetConnection(CoreApp.getInstance())) {
            List<QBUser> addedUsers = QbDialogUtils.getAddedUsers(qbDialog, newQbDialogUsersList);
            List<QBUser> removedUsers = QbDialogUtils.getRemovedUsers(qbDialog, newQbDialogUsersList);

            QbDialogUtils.logDialogUsers(qbDialog);
            QbDialogUtils.logUsers(addedUsers);
            Log.w(TAG, "=======================");
            QbDialogUtils.logUsers(removedUsers);

            QBDialogRequestBuilder qbRequestBuilder = new QBDialogRequestBuilder();
            if (!addedUsers.isEmpty()) {
                qbRequestBuilder.addUsers(addedUsers.toArray(new QBUser[addedUsers.size()]));
            }
            if (!removedUsers.isEmpty()) {
                qbRequestBuilder.removeUsers(removedUsers.toArray(new QBUser[removedUsers.size()]));
            }
            qbDialog.setName(DialogUtils.createChatNameFromUserList(
                    newQbDialogUsersList.toArray(new QBUser[newQbDialogUsersList.size()])));

            QBRestChatService.updateGroupChatDialog(qbDialog, qbRequestBuilder).performAsync(
                    new QbEntityCallbackWrapper<QBChatDialog>(callback) {
                        @Override
                        public void onSuccess(QBChatDialog qbDialog, Bundle bundle) {
                            QbUsersHolder.getInstance().putUsers(newQbDialogUsersList);
                            QbDialogUtils.logDialogUsers(qbDialog);
                            super.onSuccess(qbDialog, bundle);
                        }
                    });
        }
    }

    public void loadChatHistory(QBChatDialog dialog, int skipPagination,
                                final QBEntityCallback<ArrayList<QBChatMessage>> callback) {
        QBRequestGetBuilder customObjectRequestBuilder = new QBRequestGetBuilder();
        customObjectRequestBuilder.setSkip(skipPagination);
        customObjectRequestBuilder.setLimit(CHAT_HISTORY_ITEMS_PER_PAGE);
        customObjectRequestBuilder.sortDesc(CHAT_HISTORY_ITEMS_SORT_FIELD);

        if (ConnectivityUtils.checkInternetConnection(CoreApp.getInstance())) {
            QBRestChatService.getDialogMessages(dialog, customObjectRequestBuilder).performAsync(
                    new QbEntityCallbackWrapper<ArrayList<QBChatMessage>>(callback) {
                        @Override
                        public void onSuccess(ArrayList<QBChatMessage> qbChatMessages, Bundle bundle) {
                            callback.onSuccess(qbChatMessages, bundle);
//                            getUsersFromMessages(qbChatMessages, callback);
                            // Not calling super.onSuccess() because
                            // we're want to load chat users before triggering the callback
                        }

                        @Override
                        public void onError(QBResponseException error) {
                            super.onError(error);
                            callback.onError(error);
                        }
                    });
        } else {
            Log.d(TAG, "loadChatHistory:  NO INTERNET");
        }
    }

    public void getDialogs(QBRequestGetBuilder customObjectRequestBuilder,
                           final QBEntityCallback<ArrayList<QBChatDialog>> callback,
                           final int skip, int limit) {

        limit = limit == 0 ? ITEMS_PER_PAGE : limit;
        customObjectRequestBuilder.setLimit(limit);
        customObjectRequestBuilder.setSkip(skip);

        if (ConnectivityUtils.checkInternetConnection(CoreApp.getInstance())) {
            QBRestChatService.getChatDialogs(null, customObjectRequestBuilder).performAsync(
                    new QbEntityCallbackWrapper<ArrayList<QBChatDialog>>(callback) {
                        @Override
                        public void onSuccess(ArrayList<QBChatDialog> dialogs, Bundle args) {
                            Iterator<QBChatDialog> dialogIterator = dialogs.iterator();
                            while (dialogIterator.hasNext()) {
                                QBChatDialog dialog = dialogIterator.next();
                                if (dialog.getType() == QBDialogType.PUBLIC_GROUP) {
                                    dialogIterator.remove();
                                }
                            }
                            callback.onSuccess(dialogs, args);

                            // getUsersFromDialogs(dialogs, callback);
                            // Not calling super.onSuccess() because
                            // we want to load chat users before triggering callback
                        }

                        @Override
                        public void onError(QBResponseException error) {
                            super.onError(error);
                            callback.onError(error);
                        }
                    });
        } else {
            Log.d(TAG, "getDialogs: NO INTER NET");
        }
    }

    public void getDialogById(String dialogId, final QBEntityCallback<QBChatDialog> callback) {
        QBRestChatService.getChatDialogById(dialogId).performAsync(callback);
    }

    public void getUsersFromDialog(QBChatDialog dialog,
                                   final QBEntityCallback<ArrayList<QBUser>> callback) {
        ArrayList<Integer> userIds = new ArrayList<>(dialog.getOccupants().size());
        userIds.addAll(dialog.getOccupants());
        userIds.remove(SharedPreferencesUtil.getQbUser().getId());

        final ArrayList<QBUser> users = new ArrayList<>(userIds.size());
        for (Integer id : userIds) {
            users.add(QbUsersHolder.getInstance().getUserById(id));
        }

        // If we already have all users in memory
        // there is no need to make REST requests to QB
        if (userIds.size() == users.size()) {
            callback.onSuccess(users, null);
            return;
        }

        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder(userIds.size(), 1);
        QBUsers.getUsersByIDs(userIds, requestBuilder).performAsync(
                new QbEntityCallbackWrapper<ArrayList<QBUser>>(callback) {
                    @Override
                    public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                        QbUsersHolder.getInstance().putUsers(qbUsers);
                        callback.onSuccess(qbUsers, bundle);
                    }
                });
    }

    public void loadFileAsAttachment(File file, QBEntityCallback<QBAttachment> callback) {
        loadFileAsAttachment(file, callback, null);
    }

    public void loadFileAsAttachment(File file, QBEntityCallback<QBAttachment> callback,
                                     QBProgressCallback progressCallback) {
        QBContent.uploadFileTask(file, true, null, progressCallback).performAsync(
                new QbEntityCallbackTwoTypeWrapper<QBFile, QBAttachment>(callback) {
                    @Override
                    public void onSuccess(QBFile qbFile, Bundle bundle) {
                        QBAttachment attachment = new QBAttachment(QBAttachment.PHOTO_TYPE);
                        attachment.setId(qbFile.getId().toString());
                        attachment.setUrl(qbFile.getPublicUrl());
                        callback.onSuccess(attachment, bundle);
                    }
                });
    }

    private void getUsersFromDialogs(final ArrayList<QBChatDialog> dialogs,
                                     final QBEntityCallback<ArrayList<QBChatDialog>> callback) {
        List<Integer> userIds = new ArrayList<>();
        for (QBChatDialog dialog : dialogs) {
            userIds.addAll(dialog.getOccupants());
            userIds.add(dialog.getLastMessageUserId());
        }

        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder(userIds.size(), 1);
        QBUsers.getUsersByIDs(userIds, requestBuilder).performAsync(
                new QbEntityCallbackTwoTypeWrapper<ArrayList<QBUser>, ArrayList<QBChatDialog>>(callback) {
                    @Override
                    public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                        QbUsersHolder.getInstance().putUsers(users);
                        callback.onSuccess(dialogs, params);
                    }
                });
    }

    private void getUsersFromMessages(final ArrayList<QBChatMessage> messages,
                                      final QBEntityCallback<ArrayList<QBChatMessage>> callback) {
        Set<Integer> userIds = new HashSet<>();
        for (QBChatMessage message : messages) {
            userIds.add(message.getSenderId());
        }

        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder(userIds.size(), 1);
        QBUsers.getUsersByIDs(userIds, requestBuilder).performAsync(
                new QbEntityCallbackTwoTypeWrapper<ArrayList<QBUser>, ArrayList<QBChatMessage>>(callback) {
                    @Override
                    public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                        QbUsersHolder.getInstance().putUsers(users);
                        callback.onSuccess(messages, params);
                    }
                });
    }

//    /**
//     *
//     */
    private void getMediaListFromServer(QBChatDialog qbDialog) {
        final List<String> mMediaList = new ArrayList<>();
        QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
        requestBuilder.setLimit(100);
        requestBuilder.addRule("attachments.type", QueryRule.EQ, QBAttachment.PHOTO_TYPE);

        QBRestChatService.getDialogMessages(qbDialog, requestBuilder).performAsync(new QBEntityCallback<ArrayList<QBChatMessage>>() {
            @Override
            public void onSuccess(ArrayList<QBChatMessage> messages, Bundle bundle) {
                if (messages != null && !messages.isEmpty()) {
                    for (QBChatMessage message : messages) {
                        for (QBAttachment attachment : message.getAttachments()) {
                            mMediaList.add(attachment.getUrl());
                        }
                    }
                }
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });
    }

    public void createDialog(final QBChatDialog dialog, final QBEntityCallback<QBChatDialog> callback) {
        QBRestChatService.createChatDialog(dialog).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog dialog, Bundle args) {
//                    super.onSuccess(dialog, args);
                callback.onSuccess(dialog, args);
            }

            @Override
            public void onError(QBResponseException errors) {
                callback.onError(errors);
            }
        });
    }

    public void deleteMessages(Set<String> messagesIds, QBEntityCallback<Void> callback){
        QBRestChatService.deleteMessages(messagesIds, true).performAsync(callback);
    }
}