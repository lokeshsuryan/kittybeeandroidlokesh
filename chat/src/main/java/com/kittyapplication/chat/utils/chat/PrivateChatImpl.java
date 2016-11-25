package com.kittyapplication.chat.utils.chat;

import android.util.Log;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBMessageStatusesManager;
import com.quickblox.chat.QBPrivateChat;
import com.quickblox.chat.QBPrivateChatManager;
import com.quickblox.chat.listeners.QBMessageSentListener;
import com.quickblox.chat.listeners.QBMessageStatusListener;
import com.quickblox.chat.listeners.QBPrivateChatManagerListener;
import com.quickblox.chat.model.QBChatMessage;

public class PrivateChatImpl extends BaseChatImpl<QBPrivateChat>
        implements QBPrivateChatManagerListener, QBMessageSentListener<QBPrivateChat>,
        QBMessageStatusListener {
    private static final String TAG = PrivateChatImpl.class.getSimpleName();

    private QBPrivateChatManager qbPrivateChatManager;
    private QBMessageStatusesManager messageStatusesManager;

    private MessageStatusListener mListener;

    public PrivateChatImpl(QBChatMessageListener chatMessageListener,
                           Integer opponentId,
                           MessageStatusListener listener) {
        super(chatMessageListener);

        mListener = listener;

        qbChat = qbPrivateChatManager.getChat(opponentId);
        if (qbChat == null) {
            qbChat = qbPrivateChatManager.createChat(opponentId, this);
        } else {
            qbChat.addMessageListener(this);
        }
        qbChat.addMessageSentListener(this);
        messageStatusesManager.addMessageStatusListener(this);
    }

    @Override
    protected void initManagerIfNeed() {
        if (qbPrivateChatManager == null) {
            qbPrivateChatManager = QBChatService.getInstance().getPrivateChatManager();
            messageStatusesManager = QBChatService.getInstance().getMessageStatusesManager();
            if (qbPrivateChatManager != null)
                qbPrivateChatManager.addPrivateChatManagerListener(this);
        }
    }

    @Override
    public void release() {
        Log.i(TAG, "Release private chat");
        initManagerIfNeed();

        qbChat.removeMessageSentListener(this);
        qbChat.removeMessageListener(this);
        qbPrivateChatManager.removePrivateChatManagerListener(this);
    }

    @Override
    public void chatCreated(QBPrivateChat incomingPrivateChat, boolean createdLocally) {
        Log.i(TAG, "Private chat created: " + incomingPrivateChat.getParticipant() + ", createdLocally:" + createdLocally);

        if (!createdLocally) {
            qbChat = incomingPrivateChat;
            qbChat.addMessageListener(this);
        }
    }

    @Override
    public void processMessageSent(QBPrivateChat qbPrivateChat, QBChatMessage qbChatMessage) {
        Log.i(TAG, "processMessageSent: " + qbChatMessage.getBody());
        if (mListener != null)
            mListener.onMessageSent(qbChatMessage);
        else
            Log.i(TAG, "processMessageSent: null");
    }

    @Override
    public void processMessageFailed(QBPrivateChat qbPrivateChat, QBChatMessage qbChatMessage) {
        Log.i(TAG, "processMessageFailed: " + qbChatMessage.getBody());
        if (mListener != null)
            mListener.onMessageFail(qbChatMessage);
        else
            Log.i(TAG, "processMessageFailed: null");
    }

    @Override
    public void processMessageDelivered(String s, String s1, Integer integer) {
        Log.i(TAG, "processMessageDelivered: " + s + "-" + s1 + "-" + integer);
        if (mListener != null)
            mListener.onMessageDelivered(s, s1, integer);
        else
            Log.i(TAG, "processMessageDelivered: null");
    }

    @Override
    public void processMessageRead(String s, String s1, Integer integer) {
        Log.i(TAG, "processMessageRead: " + s + "-" + s1 + "-" + integer);
        if (mListener != null)
            mListener.onMessageRead(s, s1, integer);
        else
            Log.i(TAG, "processMessageReads: null");
    }

    public void addChatMessageListener(MessageStatusListener listener) {
        mListener = listener;
    }
}