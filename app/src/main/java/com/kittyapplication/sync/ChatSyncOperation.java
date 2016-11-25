package com.kittyapplication.sync;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.kittyapplication.chat.utils.SharedPreferencesUtil;
import com.kittyapplication.chat.utils.chat.Chat;
import com.kittyapplication.chat.utils.chat.ChatHelper;
import com.kittyapplication.chat.utils.chat.GroupChatImpl;
import com.kittyapplication.chat.utils.chat.MessageStatusListener;
import com.kittyapplication.chat.utils.chat.PrivateChatImpl;
import com.kittyapplication.chat.utils.chat.QBChatMessageListener;
import com.kittyapplication.chat.utils.qb.QbDialogUtils;
import com.kittyapplication.model.Kitty;
import com.kittyapplication.sqlitedb.DBQueryHandler.OnQueryHandlerListener;
import com.kittyapplication.ui.executor.ExecutorThread;
import com.kittyapplication.ui.viewmodel.MainChatViewModel;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.quickblox.chat.QBChat;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by MIT on 10/12/2016.
 */
public class ChatSyncOperation implements MessageStatusListener {
    private static final String TAG = ChatSyncOperation.class.getSimpleName();
    private MainChatViewModel viewModel;

    public ChatSyncOperation(Context context) {
        viewModel = new MainChatViewModel(context);
    }

    public synchronized void syncCreatedDialog() {
        Log.e(TAG, "syncCreatedDialog: ");

        viewModel.fetchRawDialogKitties(new OnQueryHandlerListener<ArrayList<Kitty>>() {

            @Override
            public void onResult(ArrayList<Kitty> result) {
                // create pending dialogs on QB server
                try {
                    if (result != null && result.size() > 0) {
                        createDialog(result, 0);
                    }
//                    for (final Kitty kitty : result) {
//                        System.out.println("Local dialog::" + kitty.toString());
//                        final QBDialog dialog = kitty.getQbDialog();
//                        ChatHelper.getInstance().createDialog(dialog, new QBEntityCallback<QBDialog>() {
//                            @Override
//                            public void onSuccess(QBDialog qbdialog, Bundle bundle) {
//                                System.out.println("Created dialog::" + qbdialog.toString());
//                                // updated pending dialog with original data
//                                viewModel.updateDialogByKittyId(kitty.getId(), qbdialog);
//                                // update of all chat messages's dialogId with original dialogId
//                                viewModel.updateMessageDialogId(dialog.getDialogId(), qbdialog.getDialogId());
//                            }
//
//                            @Override
//                            public void onError(QBResponseException e) {
//
//                            }
//                        });
//                    }
                } catch (Exception e) {
                    AppLog.e(TAG, e.getMessage(), e);
                }
            }
        });

    }

    private void createDialog(final ArrayList<Kitty> kitties, final int index) {
        if (index == kitties.size() - 1)
            return;

        try {
            final Kitty kitty = kitties.get(index);

            final QBDialog dialog = kitty.getQbDialog();
            ExecutorThread executorThread = new ExecutorThread();
            executorThread.startExecutor();
            executorThread.postTask(new Runnable() {
                @Override
                public void run() {
                    ChatHelper.getInstance().createDialog(dialog, new QBEntityCallback<QBDialog>() {
                        @Override
                        public void onSuccess(QBDialog qbdialog, Bundle bundle) {
                            System.out.println("Created dialog::" + qbdialog.toString());
                            // updated pending dialog with original data
                            viewModel.updateDialogByKittyId(kitty.getId(), qbdialog);
                            // update of all chat messages's dialogId with original dialogId
                            //                        viewModel.updateMessageDialogId(dialog.getDialogId(), qbdialog.getDialogId());

                            createDialog(kitties, index + 1);
                        }

                        @Override
                        public void onError(QBResponseException e) {

                        }
                    });
                }
            });
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public synchronized void syncDeletedDialog() {
        viewModel.fetchDeletedKitties(new OnQueryHandlerListener<ArrayList<Kitty>>() {
            @Override
            public void onResult(ArrayList<Kitty> result) {
                try {
                    for (final Kitty kitty : result) {
                        final QBDialog dialog = kitty.getQbDialog();
                        ChatHelper.getInstance().deleteDialog(dialog, new QBEntityCallback<Void>() {
                            @Override
                            public void onSuccess(Void aVoid, Bundle bundle) {
                                System.out.println("QBDialog " + dialog.getName() + " has deleted");
                                viewModel.deleteKitty(kitty.getId());
                            }

                            @Override
                            public void onError(QBResponseException e) {

                            }
                        });
                    }
                } catch (Exception e) {
                    AppLog.e(TAG, e.getMessage(), e);
                }
            }
        });
    }

    public synchronized void syncDeletedMessages() {
        try {
            if (QBChatService.getInstance() != null) {
                viewModel.loadDeletedMessageSet(new OnQueryHandlerListener<Set<String>>() {
                    @Override
                    public void onResult(final Set<String> result) {
                        if (result != null) {
                            QBChatService.deleteMessages(result, new QBEntityCallback<Void>() {
                                @Override
                                public void onSuccess(Void aVoid, Bundle bundle) {
                                    viewModel.deleteByChatIds(result);
                                }

                                @Override
                                public void onError(QBResponseException errors) {
                                }
                            });
                        }
                    }
                });

            } else {
                ChatHelper.getInstance().login(SharedPreferencesUtil.getQbUser(), new QBEntityCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid, Bundle bundle) {
                        syncDeletedMessages();
                    }

                    @Override
                    public void onError(QBResponseException e) {

                    }
                });
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public synchronized void syncMessages() {
        Log.e(TAG, "syncMessages: ");
        try {
            if (QBChatService.getInstance().getGroupChatManager() != null) {
                viewModel.loadPendingMessages(new OnQueryHandlerListener<ArrayList<QBChatMessage>>() {
                    @Override
                    public void onResult(ArrayList<QBChatMessage> result) {
                        if (result != null) {
                            Chat chat = null;
                            for (final QBChatMessage message : result) {
                                viewModel.fetchQBDialog(message.getDialogId(), new OnQueryHandlerListener<ArrayList<QBDialog>>() {
                                    @Override
                                    public void onResult(ArrayList<QBDialog> result) {
                                        for (QBDialog dialog : result) {
                                            sendMessage(dialog, message);
                                        }
                                    }
                                });
                            }
                        }
                    }
                });

            } else {
                ChatHelper.getInstance().login(SharedPreferencesUtil.getQbUser(), new QBEntityCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid, Bundle bundle) {
                        syncMessages();
                    }

                    @Override
                    public void onError(QBResponseException e) {

                    }
                });
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    private void sendMessage(QBDialog dialog, QBChatMessage message) {
        Chat chat = null;
        if (dialog == null)
            return;
        try {
            message.setDialogId(dialog.getDialogId());
            switch (dialog.getType()) {
                case GROUP:
                case PUBLIC_GROUP:
                    chat = new GroupChatImpl(chatMessageListener, ChatSyncOperation.this);
                    joinGroupChat(chat, dialog, message);
                    break;

                case PRIVATE:
                    chat = new PrivateChatImpl(chatMessageListener,
                            QbDialogUtils.getOpponentIdForPrivateDialog(dialog), ChatSyncOperation.this);
                    sendMessage(chat, message);
                    break;

                default:
                    break;
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }

    }

    protected void joinGroupChat(final Chat chat, QBDialog dialog, final QBChatMessage message) {
        System.out.println("joinGroupChat");
        try {
            if (chat != null) {
                ((GroupChatImpl) chat).joinGroupChat(dialog, new QBEntityCallback<Void>() {
                    @Override
                    public void onSuccess(Void result, Bundle b) {
                        sendMessage(chat, message);
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
    private QBChatMessageListener chatMessageListener = new QBChatMessageListener() {
        @Override
        public void onQBChatMessageReceived(QBChat chat, QBChatMessage message) {
            Log.e(TAG, "onQBChatMessageReceived: ");
            try {
//                String lastRecordId = (String) message.getProperty(AppConstant.LAST_INSERTED_ID);
//                if (lastRecordId != null) {
//                    int id = Integer.parseInt(lastRecordId);
//                    viewModel.updateSent(message, id);
//                }
                viewModel.updateMessage(message);
            } catch (Exception e) {
                AppLog.e(TAG, e.getMessage(), e);
            }
        }
    };

    private void sendMessage(Chat chat, QBChatMessage message) {
        Log.e(TAG, "sendMessage: ");
        if (chat != null) {
            try {
                chat.sendMessage(message);
            } catch (XMPPException e) {
                AppLog.e(TAG, e.getMessage(), e);
            } catch (SmackException.NotConnectedException e) {
                AppLog.e(TAG, e.getMessage(), e);
            }
        }
//        try {
//            if (chat != null) {
//                chat.release();
//            }
//        } catch (XMPPException e) {
//            Log.e(TAG, "Failed to release chat", e);
//        }
    }

    public synchronized void sendBroadCast(Context context) {
        Intent broadcastIntent = new Intent(AppConstant.NETWORK_STAT_ACTION);
        context.sendBroadcast(broadcastIntent);
    }

    @Override
    public void onMessageFail(QBChatMessage chatMessage) {
        Log.e(TAG, "onMessageFail: ");
        try {
            String lastRecordId = (String) chatMessage.getProperty(AppConstant.LAST_INSERTED_ID);
            if (lastRecordId != null) {
                int id = Integer.parseInt(lastRecordId);
                viewModel.updateFail(chatMessage, id);
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public void onMessageSent(QBChatMessage chatMessage) {
        Log.e(TAG, "onMessageSent: ");
        try {
            String lastRecordId = (String) chatMessage.getProperty(AppConstant.LAST_INSERTED_ID);
            if (lastRecordId != null) {
                int id = Integer.parseInt(lastRecordId);
                viewModel.updateSent(chatMessage, id);
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public void onMessageDelivered(String messageID, String dialogID, Integer occupantsID) {
        Log.e(TAG, "onMessageDelivered: ");
        viewModel.updateRead(messageID);
    }

    @Override
    public void onMessageRead(String messageID, String dialogID, Integer occupantsID) {
        Log.e(TAG, "onMessageRead: ");
        viewModel.updateDelivered(messageID);
    }
}
