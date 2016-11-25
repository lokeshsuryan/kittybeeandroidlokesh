package com.kittyapplication.chat.utils.chat;

import android.os.Bundle;
import android.util.Log;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBGroupChat;
import com.quickblox.chat.QBGroupChatManager;
import com.quickblox.chat.QBMessageStatusesManager;
import com.quickblox.chat.listeners.QBMessageSentListener;
import com.quickblox.chat.listeners.QBMessageStatusListener;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.DiscussionHistory;

public class GroupChatImpl extends BaseChatImpl<QBGroupChat>
        implements QBMessageSentListener<QBGroupChat>, QBMessageStatusListener {
    private static final String TAG = GroupChatImpl.class.getSimpleName();

    private QBGroupChatManager qbGroupChatManager;

    private QBMessageStatusesManager messageStatusesManager;

    private MessageStatusListener mListener;

    public GroupChatImpl(QBChatMessageListener chatMessageListener,
                         MessageStatusListener listener) {
        super(chatMessageListener);
        mListener = listener;
    }

    @Override
    protected void initManagerIfNeed() {
        if (qbGroupChatManager == null) {
            qbGroupChatManager = QBChatService.getInstance().getGroupChatManager();

        }
    }

    public void joinGroupChat(QBDialog dialog, QBEntityCallback<Void> callback) {
        initManagerIfNeed();
        if (qbChat == null) {
            if (qbGroupChatManager != null) {
                qbChat = qbGroupChatManager.createGroupChat(dialog.getRoomJid());
                messageStatusesManager = QBChatService.getInstance().getMessageStatusesManager();
                join(callback);
            }
        }
    }

    private void join(final QBEntityCallback<Void> callback) {
        try {
            DiscussionHistory history = new DiscussionHistory();
            history.setMaxStanzas(0);

            qbChat.join(history, new QBEntityCallback<Void>() {
                @Override
                public void onSuccess(final Void result, final Bundle bundle) {
                    qbChat.addMessageListener(GroupChatImpl.this);
                    qbChat.addMessageSentListener(GroupChatImpl.this);
                    messageStatusesManager.addMessageStatusListener(GroupChatImpl.this);
                    mainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(result, bundle);
                        }
                    });
                    Log.i(TAG, "Join successful");
                }

                @Override
                public void onError(final QBResponseException e) {
                    mainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(e);
                        }
                    });
                }
            });
        } catch (Exception e) {
        }
    }

    public void leaveChatRoom() {
        try {
            qbChat.leave();
        } catch (SmackException.NotConnectedException | XMPPException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void release() throws XMPPException {
        if (qbChat != null) {
            leaveChatRoom();
            qbChat.removeMessageListener(this);
        }
    }

    @Override
    public void processMessageSent(QBGroupChat qbGroupChat, QBChatMessage qbChatMessage) {
        Log.d(TAG, "processMessageSent"
                + qbChatMessage.getBody());
        if (mListener != null)
            mListener.onMessageSent(qbChatMessage);
        else
            Log.i(TAG, "processMessageSent: null");
    }

    @Override
    public void processMessageFailed(QBGroupChat qbGroupChat, QBChatMessage qbChatMessage) {
        Log.d(TAG, "processMessageFailed"
                + qbChatMessage.getBody());
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
            Log.i(TAG, "processMessageRead: null");
    }


    public void addChatMessageListener(MessageStatusListener listener) {
        mListener = listener;
    }
}
