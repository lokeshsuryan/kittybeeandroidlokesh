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
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
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
import com.quickblox.core.request.QBRequestUpdateBuilder;
import com.quickblox.core.request.QueryRule;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ChatHelper {
    private static final String TAG = ChatHelper.class.getSimpleName();

    private static final int AUTO_PRESENCE_INTERVAL_IN_SECONDS = 30;

    public static final int DIALOG_ITEMS_PER_PAGE = 100;
    public static final int CHAT_HISTORY_ITEMS_PER_PAGE = 50;
    private static final String CHAT_HISTORY_ITEMS_SORT_FIELD = "date_sent";
    private static final int TIMEOUT = 120000;

    private static ChatHelper instance;

    private QBChatService qbChatService;

    public static synchronized ChatHelper getInstance() {
        if (instance == null) {
            QBSettings.getInstance().setLogLevel(LogLevel.DEBUG);

            QBChatService.ConfigurationBuilder configurationBuilder =
                    new QBChatService.ConfigurationBuilder();
            configurationBuilder.setKeepAlive(true);
            configurationBuilder.setSocketTimeout(TIMEOUT);

            QBChatService.setConfigurationBuilder(configurationBuilder);
            QBChatService.setDebugEnabled(true);
            QBChatService.setDefaultPacketReplyTimeout(TIMEOUT);
            QBChatService.setDefaultConnectionTimeout(TIMEOUT);
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
    }

    public void addConnectionListener(ConnectionListener listener) {
        qbChatService.addConnectionListener(listener);
    }

    public void removeConnectionListener(ConnectionListener listener) {
        qbChatService.removeConnectionListener(listener);
    }

    public void login(final QBUser user, final QBEntityCallback<Void> callback) {
        // Create REST API session on QuickBlox
        QBAuth.createSession(user, new QbEntityCallbackTwoTypeWrapper<QBSession, Void>(callback) {
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

            @Override
            public void onError(QBResponseException error) {
                super.onError(error);
                callback.onError(error);
            }
        });
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

    public boolean logout() {
        try {
            qbChatService.logout();
            return true;
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void createDialog(final QBDialog dialog, final QBEntityCallback<QBDialog> callback) {
        if (QBChatService.getInstance().getGroupChatManager() != null) {
            QBChatService.getInstance().getGroupChatManager().createDialog(dialog,
                    new QbEntityCallbackWrapper<QBDialog>(callback) {
                        @Override
                        public void onSuccess(QBDialog dialog, Bundle args) {
                            super.onSuccess(dialog, args);
                        }
                    }
            );
        } else {
            login(SharedPreferencesUtil.getQbUser(), new QBEntityCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid, Bundle bundle) {
                    createDialog(dialog, callback);
                }

                @Override
                public void onError(QBResponseException e) {
                    callback.onError(e);
                }
            });
        }
    }

    public void createDialogWithSelectedUsers(final List<QBUser> users,
                                              final QBEntityCallback<QBDialog> callback) {
        if (ConnectivityUtils.checkInternetConnection(CoreApp.getInstance())) {
            if (QBChatService.getInstance().getGroupChatManager() != null) {
                QBChatService.getInstance().getGroupChatManager().createDialog(QbDialogUtils.createDialog(users),
                        new QbEntityCallbackWrapper<QBDialog>(callback) {
                            @Override
                            public void onSuccess(QBDialog dialog, Bundle args) {
                                QbUsersHolder.getInstance().putUsers(users);
                                super.onSuccess(dialog, args);
                            }
                        }
                );
            } else {
                login(SharedPreferencesUtil.getQbUser(), new QBEntityCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid, Bundle bundle) {
                        createDialogWithSelectedUsers(users, callback);
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        callback.onError(e);
                    }
                });
            }
        } else {
            QbUsersHolder.getInstance().putUsers(users);
            QBDialog dialog = QbDialogUtils.createDialog(users);
            dialog.setDialogId("0");
            callback.onSuccess(dialog, new Bundle());
        }
    }

    public void deleteDialogs(Collection<QBDialog> dialogs, QBEntityCallback<Void> callback) {
        for (QBDialog dialog : dialogs) {
            deleteDialog(dialog, new QBEntityCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid, Bundle bundle) {
                }

                @Override
                public void onError(QBResponseException e) {
                }
            });
        }

        callback.onSuccess(null, null);
    }

    public void deleteDialog(final QBDialog qbDialog, final QBEntityCallback<Void> callback) {
        if (QBChatService.getInstance().getGroupChatManager() != null) {
            if (qbDialog.getType() == QBDialogType.GROUP) {
                QBChatService.getInstance().getGroupChatManager()
                        .deleteDialog(qbDialog.getDialogId(), true,
                                new QbEntityCallbackWrapper<Void>(callback));
            } else if (qbDialog.getType() == QBDialogType.PRIVATE) {
                QBChatService.getInstance().getPrivateChatManager()
                        .deleteDialog(qbDialog.getDialogId(),
                                false,
                                new QbEntityCallbackWrapper<Void>(callback));
            } else if (qbDialog.getType() == QBDialogType.PUBLIC_GROUP) {
                Toaster.shortToast(R.string.public_group_chat_cannot_be_deleted);
            }
        } else {
            login(SharedPreferencesUtil.getQbUser(), new QBEntityCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid, Bundle bundle) {
                    deleteDialog(qbDialog, callback);
                }

                @Override
                public void onError(QBResponseException e) {

                }
            });
        }
    }

    public void deleteDialogWithCallback(final QBDialog qbDialog, final QBEntityCallback<ArrayList<String>> callback) {
        StringifyArrayList<String> dialogIds = new StringifyArrayList<>();
        dialogIds.add(qbDialog.getDialogId());

        if (QBChatService.getInstance().getGroupChatManager() != null) {
            if (qbDialog.getType() == QBDialogType.GROUP) {
                QBChatService.getInstance().getGroupChatManager()
                        .deleteDialogs(dialogIds, true, callback);
            } else if (qbDialog.getType() == QBDialogType.PRIVATE) {
                QBChatService.getInstance().getPrivateChatManager()
                        .deleteDialogs(dialogIds, false, callback);
            } else if (qbDialog.getType() == QBDialogType.PUBLIC_GROUP) {
                Toaster.shortToast(R.string.public_group_chat_cannot_be_deleted);
            }
        } else {
            login(SharedPreferencesUtil.getQbUser(), new QBEntityCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid, Bundle bundle) {
                    deleteDialogWithCallback(qbDialog, callback);
                }

                @Override
                public void onError(QBResponseException e) {

                }
            });
        }
    }

    public void leaveDialog(QBDialog qbDialog, QBEntityCallback<QBDialog> callback) {
        QBRequestUpdateBuilder qbRequestBuilder = new QBRequestUpdateBuilder();
        qbRequestBuilder.pullAll("occupants_ids", SharedPreferencesUtil.getQbUser().getId());

        QBChatService.getInstance().getGroupChatManager().updateDialog(qbDialog, qbRequestBuilder,
                new QbEntityCallbackWrapper<QBDialog>(callback) {
                    @Override
                    public void onSuccess(QBDialog qbDialog, Bundle bundle) {
                        super.onSuccess(qbDialog, bundle);
                    }
                });
    }

    public void updateDialogUsers(QBDialog qbDialog,
                                  final List<QBUser> newQbDialogUsersList,
                                  QBEntityCallback<QBDialog> callback) {
        List<QBUser> addedUsers = QbDialogUtils.getAddedUsers(qbDialog, newQbDialogUsersList);
        List<QBUser> removedUsers = QbDialogUtils.getRemovedUsers(qbDialog, newQbDialogUsersList);

        QbDialogUtils.logDialogUsers(qbDialog);
        QbDialogUtils.logUsers(addedUsers);
        Log.w(TAG, "=======================");
        QbDialogUtils.logUsers(removedUsers);

        QBRequestUpdateBuilder qbRequestBuilder = new QBRequestUpdateBuilder();
        if (!addedUsers.isEmpty()) {
            qbRequestBuilder.pushAll("occupants_ids", QbDialogUtils.getUserIds(addedUsers));
        }
        if (!removedUsers.isEmpty()) {
            qbRequestBuilder.pullAll("occupants_ids", QbDialogUtils.getUserIds(removedUsers));
        }
        qbDialog.setName(QbDialogUtils.createChatNameFromUserList(newQbDialogUsersList));

        QBChatService.getInstance().getGroupChatManager().updateDialog(qbDialog, qbRequestBuilder,
                new QbEntityCallbackWrapper<QBDialog>(callback) {
                    @Override
                    public void onSuccess(QBDialog qbDialog, Bundle bundle) {
                        QbUsersHolder.getInstance().putUsers(newQbDialogUsersList);
                        QbDialogUtils.logDialogUsers(qbDialog);
                        super.onSuccess(qbDialog, bundle);
                    }
                });
    }

    public void loadChatHistory(final QBDialog dialog, final int skipPagination,
                                final QBEntityCallback<ArrayList<QBChatMessage>> callback) {
        QBRequestGetBuilder customObjectRequestBuilder = new QBRequestGetBuilder();
        customObjectRequestBuilder.setSkip(skipPagination);
        customObjectRequestBuilder.setLimit(CHAT_HISTORY_ITEMS_PER_PAGE);
        customObjectRequestBuilder.sortDesc(CHAT_HISTORY_ITEMS_SORT_FIELD);
//        if (QBChatService.getInstance() != null) {
        QBChatService.getDialogMessages(dialog, customObjectRequestBuilder,
                new QbEntityCallbackWrapper<ArrayList<QBChatMessage>>(callback) {
                    @Override
                    public void onSuccess(ArrayList<QBChatMessage> qbChatMessages, Bundle bundle) {
                        getUsersFromMessages(qbChatMessages, callback);
                        // Not calling super.onSuccess() because
                        // we're want to load chat users before triggering the callback
                    }
                });
//        } else {
//            login(SharedPreferencesUtil.getQbUser(), new QBEntityCallback<Void>() {
//                @Override
//                public void onSuccess(Void aVoid, Bundle bundle) {
//                    loadChatHistory(dialog, skipPagination, callback);
//                }
//
//                @Override
//                public void onError(QBResponseException e) {
//                    callback.onError(e);
//                }
//            });
//        }
    }

    public void getDialogs(final QBRequestGetBuilder customObjectRequestBuilder,
                           final QBEntityCallback<ArrayList<QBDialog>> callback, final int skip) {
        customObjectRequestBuilder.setLimit(DIALOG_ITEMS_PER_PAGE);
        customObjectRequestBuilder.setSkip(skip);
        if (QBChatService.getInstance().getGroupChatManager() != null) {
            Log.i(TAG, "onSuccess: getGroupChatManager");
            QBChatService.getChatDialogs(null, customObjectRequestBuilder,
                    new QbEntityCallbackWrapper<ArrayList<QBDialog>>(callback) {
                        @Override
                        public void onSuccess(ArrayList<QBDialog> dialogs, Bundle args) {
                            Log.i(TAG, "onSuccess: ArrayList<QBDialog>::" + dialogs.size());
                            Iterator<QBDialog> dialogIterator = dialogs.iterator();
                            while (dialogIterator.hasNext()) {
                                QBDialog dialog = dialogIterator.next();
                                if (dialog.getType() == QBDialogType.PUBLIC_GROUP) {
                                    dialogIterator.remove();
                                }
                            }

                            for (QBDialog dialog : dialogs) {
                                dialog.setId(dialog.getDialogId().hashCode());
                            }

                            callback.onSuccess(dialogs, args);
                        }

                        @Override
                        public void onError(QBResponseException error) {
                            super.onError(error);
                            callback.onError(error);
                        }
                    });
        } else {
            Log.d(TAG, "getDialogs: chat ");
            login(SharedPreferencesUtil.getQbUser(), new QBEntityCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid, Bundle bundle) {
                    Log.d(TAG, "getDialogs: chat ");
                    getDialogs(customObjectRequestBuilder, callback, skip);
                }

                @Override
                public void onError(QBResponseException e) {
                    Log.d(TAG, "getDialogs: login$onError ");
                    getDialogs(customObjectRequestBuilder, callback, skip);
                }
            });
        }
    }

    public void getUsersFromDialog(QBDialog dialog,
                                   final QBEntityCallback<ArrayList<QBUser>> callback) {
        List<Integer> userIds = dialog.getOccupants();

        ArrayList<QBUser> users = new ArrayList<>(userIds.size());
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
        QBUsers.getUsersByIDs(userIds, requestBuilder,
                new QbEntityCallbackWrapper<ArrayList<QBUser>>(callback) {
                    @Override
                    public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                        QbUsersHolder.getInstance().putUsers(qbUsers);
                        super.onSuccess(qbUsers, bundle);
                    }
                });
    }

    public void loadFileAsAttachment(File file, QBEntityCallback<QBAttachment> callback) {
        loadFileAsAttachment(file, callback, null);
    }

    public void loadFileAsAttachment(File file, QBEntityCallback<QBAttachment> callback,
                                     QBProgressCallback progressCallback) {
        QBContent.uploadFileTask(file, true, null,
                new QbEntityCallbackTwoTypeWrapper<QBFile, QBAttachment>(callback) {
                    @Override
                    public void onSuccess(QBFile qbFile, Bundle bundle) {
                        QBAttachment attachment = new QBAttachment(QBAttachment.PHOTO_TYPE);
                        attachment.setId(qbFile.getId().toString());
                        attachment.setUrl(qbFile.getPublicUrl());
                        onSuccessInMainThread(attachment, bundle);
                    }
                }, progressCallback);
    }

    private void getUsersFromDialogs(final ArrayList<QBDialog> dialogs,
                                     final QBEntityCallback<ArrayList<QBDialog>> callback) {
        List<Integer> userIds = new ArrayList<>();
        for (QBDialog dialog : dialogs) {
            userIds.addAll(dialog.getOccupants());
            userIds.add(dialog.getLastMessageUserId());
        }

        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder(userIds.size(), 1);
        QBUsers.getUsersByIDs(userIds, requestBuilder,
                new QbEntityCallbackTwoTypeWrapper<ArrayList<QBUser>, ArrayList<QBDialog>>(callback) {
                    @Override
                    public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                        QbUsersHolder.getInstance().putUsers(users);
                        onSuccessInMainThread(dialogs, params);
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
        QBUsers.getUsersByIDs(userIds, requestBuilder,
                new QbEntityCallbackTwoTypeWrapper<ArrayList<QBUser>, ArrayList<QBChatMessage>>(callback) {
                    @Override
                    public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                        QbUsersHolder.getInstance().putUsers(users);
                        onSuccessInMainThread(messages, params);
                    }
                });
    }


    /**
     *
     */
    private void getMediaListFromServer(QBDialog qbDialog) {
        final List<String> mMediaList = new ArrayList<>();
        QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
        requestBuilder.setLimit(100);
        requestBuilder.addRule("attachments.type", QueryRule.EQ, QBAttachment.PHOTO_TYPE);


        QBChatService.getDialogMessages(qbDialog, requestBuilder,
                new QBEntityCallback<ArrayList<QBChatMessage>>() {
                    @Override
                    public void onSuccess(ArrayList<QBChatMessage> messages, Bundle args) {
                        if (messages != null && !messages.isEmpty()) {
                            for (QBChatMessage message : messages) {
                                for (QBAttachment attachment : message.getAttachments()) {
                                    mMediaList.add(attachment.getUrl());
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(QBResponseException errors) {
                    }
                });
    }
}