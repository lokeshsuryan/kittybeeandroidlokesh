package com.kittyapplication.sync;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.kittyapplication.chat.ui.models.QBMessage;
import com.kittyapplication.chat.utils.SharedPreferencesUtil;
import com.kittyapplication.chat.utils.chat.ChatHelper;
import com.kittyapplication.model.Kitty;
import com.kittyapplication.rest.APIManager;
import com.kittyapplication.sqlitedb.DBQueryHandler.OnQueryHandlerListener;
import com.kittyapplication.sync.callback.DataSyncListener;
import com.kittyapplication.ui.executor.ExecutorThread;
import com.kittyapplication.ui.viewmodel.MainChatViewModel;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;

import org.jivesoftware.smack.SmackException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by MIT on 10/12/2016.
 */
public class ChatSyncOperation {
    private static final String TAG = ChatSyncOperation.class.getSimpleName();
    private MainChatViewModel viewModel;
    private HashMap<String, QBChatDialog> dialogMap;

    public ChatSyncOperation(Context context) {
        viewModel = new MainChatViewModel(context);
        dialogMap = new HashMap<>();
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

            final QBChatDialog dialog = kitty.getQbDialog();
            ExecutorThread executorThread = new ExecutorThread();
            executorThread.startExecutor();
            executorThread.postTask(new Runnable() {
                @Override
                public void run() {
                    ChatHelper.getInstance().createDialog(dialog, new QBEntityCallback<QBChatDialog>() {
                        @Override
                        public void onSuccess(QBChatDialog qbdialog, Bundle bundle) {
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
                        final QBChatDialog dialog = kitty.getQbDialog();
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

    public synchronized void sendPendingMessage() {
        Log.e(TAG, "sendPendingMessage: ");
        try {
            if (QBChatService.getInstance().getGroupChatManager() != null) {
                viewModel.loadPendingMessages(new OnQueryHandlerListener<ArrayList<QBChatMessage>>() {
                    @Override
                    public void onResult(ArrayList<QBChatMessage> result) {
                        if (result != null) {
                            for (final QBChatMessage message : result) {
                                if (dialogMap.containsKey(message.getDialogId())) {
                                    QBChatDialog dialog = dialogMap.get(message.getDialogId());
                                    sendMessage(dialog, message);
                                } else {
                                    viewModel.fetchQBDialog(message.getDialogId(), new OnQueryHandlerListener<ArrayList<QBChatDialog>>() {
                                        @Override
                                        public void onResult(ArrayList<QBChatDialog> result) {
                                            for (QBChatDialog dialog : result) {
                                                dialogMap.put(message.getDialogId(), dialog);
                                                sendMessage(dialog, message);
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    }
                });

            } else {
                ChatHelper.getInstance().login(SharedPreferencesUtil.getQbUser(), new QBEntityCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid, Bundle bundle) {
                        sendPendingMessage();
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

    private void sendMessage(QBChatDialog dialog, QBChatMessage message) {
        if (dialog == null)
            return;
        try {
            message.setDialogId(dialog.getDialogId());
            switch (dialog.getType()) {
                case GROUP:
                    joinGroupChat(dialog, message);
                    break;

                case PRIVATE:
                    sendDialogMessage(dialog, message);
                    break;

                default:
                    break;
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }

    }

    protected void joinGroupChat(final QBChatDialog dialog, final QBChatMessage message) {
        try {
            ChatHelper.getInstance().join(dialog, new QBEntityCallback<Void>() {
                @Override
                public void onSuccess(Void result, Bundle b) {
                    sendDialogMessage(dialog, message);
                }

                @Override
                public void onError(QBResponseException e) {
                }
            });
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    private void sendDialogMessage(QBChatDialog dialog, QBChatMessage message) {
        try {
            dialog.sendMessage(message);
        } catch (SmackException.NotConnectedException e) {
        }
    }


    public synchronized void sendBroadCast(Context context) {
        Intent broadcastIntent = new Intent(AppConstant.NETWORK_STAT_ACTION);
        context.sendBroadcast(broadcastIntent);
    }

    public synchronized void syncMessages() {
        AppLog.e(TAG, "syncMessages: ");
        viewModel.fetchSyncableKitties(0, new OnQueryHandlerListener<ArrayList<Kitty>>() {
            @Override
            public void onResult(ArrayList<Kitty> result) {
                AppLog.e(TAG, "fetchSyncableKitties: " + result.size());
                if (!result.isEmpty()) {
                    startMessageSyncing(result, 0);
                }
            }
        });
    }

    private void startMessageSyncing(final ArrayList<Kitty> result, final int currentIndex) {
        AppLog.e(TAG, "startMessageSyncing: ");
        Kitty kitty = result.get(currentIndex);
        QBChatDialog dialog = kitty.getQbDialog();
        int page = kitty.getLastMessagePageNo();
        syncDialogMessages(kitty.getId(), dialog, 0, new DataSyncListener() {
            @Override
            public void onCompleted(int itemCount) {
                if (currentIndex != (result.size() - 1))
                    startMessageSyncing(result, currentIndex + 1);
            }
        });
    }

    public void syncDialogMessages(final int kittyId, final QBChatDialog dialog, final int page, final DataSyncListener listener) {
        AppLog.e(TAG, "syncDialogMessages: ");
        APIManager.getInstance().loadChatHistory(dialog, page, new QBEntityCallback<ArrayList<QBChatMessage>>() {
            @Override
            public void onSuccess(ArrayList<QBChatMessage> qbChatMessages, Bundle bundle) {
                if (!qbChatMessages.isEmpty()) {
                    for (QBChatMessage chatMessage : qbChatMessages) {
                        try {
                            QBMessage msg = new QBMessage();
                            msg.setMessage(chatMessage);
                            msg.setKittyId(kittyId);
                            msg.generateMessageStatus((ArrayList<Integer>) dialog.getOccupants());
                            viewModel.saveMessage(msg, kittyId);
                            if (page == 0)
                                viewModel.updatePageNoWithDisableSync(qbChatMessages.size(), kittyId);
                            else
                                viewModel.updatePageNoWithDisableSync(page, kittyId);
                        } catch (Exception e) {
                            AppLog.e(TAG, e.getMessage(), e);
                        }
                    }
                    listener.onCompleted(qbChatMessages.size());
                } else {
                    listener.onCompleted(qbChatMessages.size());
                }
            }

            @Override
            public void onError(QBResponseException e) {
                listener.onCompleted(0);
            }
        });
    }
}
