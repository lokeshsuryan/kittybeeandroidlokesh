package com.kittyapplication.chat.utils.chat;

import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBChatDialogMessageListener;
import com.quickblox.chat.model.QBChatMessage;

/**
 * Created by HalfBloodPrince(RIONTECH)
 * Riontech on 26/11/16.
 */

public class QbChatDialogMessageListenerImp implements QBChatDialogMessageListener {
    public QbChatDialogMessageListenerImp() {
    }

    @Override
    public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {

    }

    @Override
    public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {

    }
}
