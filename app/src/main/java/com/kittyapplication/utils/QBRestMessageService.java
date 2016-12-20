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

package com.kittyapplication.utils;

import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.kittyapplication.chat.ui.adapter.ChatMessageAdapter;
import com.kittyapplication.chat.ui.models.QBMessage;
import com.kittyapplication.chat.utils.SharedPreferencesUtil;
import com.kittyapplication.chat.utils.chat.ChatHelper;
import com.kittyapplication.chat.utils.chat.QbChatDialogMessageListenerImp;
import com.kittyapplication.core.CoreApp;
import com.kittyapplication.core.utils.ConnectivityUtils;
import com.kittyapplication.listener.QBChatMessageServiceListener;
import com.kittyapplication.sqlitedb.DBQueryHandler;
import com.kittyapplication.ui.viewmodel.MainChatViewModel;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBMessageStatusesManager;
import com.quickblox.chat.listeners.QBChatDialogMessageSentListener;
import com.quickblox.chat.listeners.QBMessageStatusListener;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import java.util.ArrayList;

/**
 * Created by Vaghela Mithun on 12/11/2016.
 * vaghela.mithun@gmail.com
 */

public class QBRestMessageService implements QBMessageStatusListener, QBChatDialogMessageSentListener {
    private static QBRestMessageService messageService;
    private static final String TAG = QBRestMessageService.class.getSimpleName();
    private QBChatDialog dialog;
    private ChatMessageListener chatMessageListener;
    private QBChatMessageServiceListener qbChatMessageServiceListener;
    private MainChatViewModel viewModel;
    private int currentKittyId;

    private QBRestMessageService() {
        viewModel = new MainChatViewModel(CoreApp.getInstance());
    }

    public static synchronized QBRestMessageService getMessageService() {
        if (messageService == null) {
            messageService = new QBRestMessageService();
        }
        return messageService;
    }

    public void setCurrentKittyId(int kittyId) {
        currentKittyId = kittyId;
    }

    public void initDialogForChat(QBChatDialog dialog, QBChatMessageServiceListener qbChatMessageServiceListener) {
        try {
            if (dialog != null) {
                this.qbChatMessageServiceListener = qbChatMessageServiceListener;
                this.dialog = dialog;
                this.dialog.initForChat(QBChatService.getInstance());
                addMessageListener();
                initChat();
            } else {
                throw new NullPointerException("QBChatDialog can\'t be null");
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public void sendMessage(QBChatMessage message) {
        try {
            if (dialog == null)
                throw new NullPointerException("QBChatDialog can\'t be null");

            if (dialog.getType().equals(QBDialogType.PRIVATE) || dialog.isJoined())
                dialog.sendMessage(message);
        } catch (SmackException.NotConnectedException e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    private void addMessageListener() {
        try {
            if (dialog == null)
                throw new NullPointerException("QBChatDialog can\'t be null");

            chatMessageListener = new ChatMessageListener();
            dialog.addMessageListener(chatMessageListener);
            QBMessageStatusesManager manager = QBChatService.getInstance().getMessageStatusesManager();
            manager.addMessageStatusListener(this);
            dialog.addMessageSentListener(this);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    private class ChatMessageListener extends QbChatDialogMessageListenerImp {
        @Override
        public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {
            try {
                QBMessage qbMessage = new QBMessage();
                qbMessage.setMessage(qbChatMessage);
                qbMessage.setKittyId(currentKittyId);
                Integer userId = SharedPreferencesUtil.getQbUser().getId();

                if (!userId.equals(qbChatMessage.getSenderId())) {
                    // save locally
                    viewModel.saveMessage(qbMessage, currentKittyId);

                    if (qbChatMessageServiceListener != null)
                        qbChatMessageServiceListener.onMessageReceived(qbMessage, qbChatMessage, integer);

                    markAsRead(qbChatMessage);

                } else {
                    int lastInsertedId = Integer.parseInt((String) qbChatMessage.getProperty(AppConstant.LAST_INSERTED_ID));
                    viewModel.updateMessage(qbChatMessage, lastInsertedId);
                }

            } catch (Exception e) {
                AppLog.e(TAG, e.getMessage(), e);
            }
        }
    }

    private void markAsRead(QBChatMessage chatMessage) {
        if (chatMessage.isMarkable()) {
            try {
                dialog.readMessage(chatMessage);
            } catch (XMPPException | SmackException.NotConnectedException e) {
                AppLog.e(TAG, e.getMessage(), e);
            }
        }
    }

    private ConnectionListener chatConnectionListener = new ConnectionListener() {
        @Override
        public void connected(XMPPConnection xmppConnection) {

        }

        @Override
        public void authenticated(XMPPConnection xmppConnection, boolean b) {

        }

        @Override
        public void connectionClosed() {

        }

        @Override
        public void connectionClosedOnError(Exception e) {

        }

        @Override
        public void reconnectionSuccessful() {
            if (qbChatMessageServiceListener != null)
                qbChatMessageServiceListener.onConnected();
        }

        @Override
        public void reconnectingIn(int i) {

        }

        @Override
        public void reconnectionFailed(Exception e) {

        }
    };

    private void initChat() {
        dialog.setUnreadMessageCount(0);
        switch (dialog.getType()) {
            case GROUP:
            case PUBLIC_GROUP:
                joinGroupChat();
                break;

            case PRIVATE:
                if (qbChatMessageServiceListener != null)
                    qbChatMessageServiceListener.onConnected();
                break;

            default:
                break;
        }
    }

    private void joinGroupChat() {
        System.out.println("joinGroupChat");
        try {
            ChatHelper.getInstance().join(dialog, new QBEntityCallback<Void>() {
                @Override
                public void onSuccess(Void result, Bundle b) {
                    if (qbChatMessageServiceListener != null)
                        qbChatMessageServiceListener.onConnected();
                }

                @Override
                public void onError(QBResponseException e) {
                    AppLog.e(TAG, e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public void addConnectionListener() {
        if (ConnectivityUtils.checkInternetConnection(CoreApp.getInstance()))
            ChatHelper.getInstance().addConnectionListener(chatConnectionListener);
    }

    public void removeConnectionListener() {
        if (ConnectivityUtils.checkInternetConnection(CoreApp.getInstance()))
            ChatHelper.getInstance().removeConnectionListener(chatConnectionListener);
    }

    public void releaseChat() {
        try {
            dialog.removeMessageListrener(chatMessageListener);
            if (!QBDialogType.PRIVATE.equals(dialog.getType())) {
                leaveGroupDialog();
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to release chat", e);
        }
    }

    private void leaveGroupDialog() {
        try {
            ChatHelper.getInstance().leaveChatDialog(dialog);
        } catch (XMPPException | SmackException.NotConnectedException e) {
            Log.w(TAG, e);
        }
    }

    @Override
    public void processMessageDelivered(final String messageID, String dialogID, final Integer occupantsID) {
        try {
            AppLog.e(TAG, "onMessageDelivered: " + messageID);
            viewModel.getMessageByMessageId(messageID, new DBQueryHandler.OnQueryHandlerListener<ArrayList<QBMessage>>() {
                @Override
                public void onResult(ArrayList<QBMessage> result) {
                    if(!result.isEmpty()){
                        QBMessage message = result.get(0);
                        if (message != null) {
                            message.setDelivered(1);
                            if (message.getMessage().getDeliveredIds() == null) {
                                message.getMessage().setDeliveredIds(new ArrayList<Integer>());
                            }
                            message.getMessage().getDeliveredIds().add(occupantsID);
                            viewModel.updateDelivered(message.getMessage(), messageID);
                        }
                    }
                }
            });

        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public void processMessageRead(final String messageID, String dialogID, final Integer occupantsID) {
        try {
            AppLog.e(TAG, "onMessageRead: " + messageID);
            viewModel.getMessageByMessageId(messageID, new DBQueryHandler.OnQueryHandlerListener<ArrayList<QBMessage>>() {
                @Override
                public void onResult(ArrayList<QBMessage> result) {
                    if(!result.isEmpty()){
                        QBMessage message = result.get(0);
                        if (message != null) {
                            message.setRead(1);
                            if (message.getMessage().getReadIds() == null) {
                                message.getMessage().setReadIds(new ArrayList<Integer>());
                            }
                            message.getMessage().getReadIds().add(occupantsID);
                            viewModel.updateRead(message.getMessage(), messageID);
                        }
                    }
                }
            });
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public void processMessageSent(String s, QBChatMessage qbChatMessage) {
        Log.d(TAG, "processMessageSent: ");
        try {
            AppLog.d(TAG, new Gson().toJson(qbChatMessage).toString());
            int lastInsertedId = Integer.parseInt((String) qbChatMessage.getProperty(AppConstant.LAST_INSERTED_ID));
            viewModel.updateSent(qbChatMessage, lastInsertedId);
            Log.i(TAG, "onMessageSent: " + qbChatMessage.getBody());
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public void processMessageFailed(String s, QBChatMessage qbChatMessage) {

    }

}
