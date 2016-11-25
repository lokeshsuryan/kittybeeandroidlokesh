package com.kittyapplication.chat.utils.qb;

import com.kittyapplication.chat.ui.models.QBMessage;
import com.quickblox.chat.model.QBChatMessage;

import java.util.ArrayList;

import static android.R.attr.id;

/**
 * Created by HalfBloodPrince(RIONTECH)
 * Riontech on 14/11/16.
 */

public class MessageStatusUtils {
    private ArrayList<QBMessage> receivedMessages;
    private static MessageStatusUtils instance;

    public static MessageStatusUtils getInstance() {
        if (instance == null) {
            instance = new MessageStatusUtils();
        }
        return instance;
    }

    private MessageStatusUtils() {
        receivedMessages = new ArrayList<>();
    }

    public void addMessage(QBMessage message) {
        receivedMessages.add(message);
    }

    public void remove(String id) {
        for (int i = 0; i < receivedMessages.size(); i++) {
            QBChatMessage message = receivedMessages.get(i).getMessage();
            if (message.getId().equals(id)) {
                remove(i);
            }
        }
    }

    public void clear() {
        receivedMessages.clear();
    }

    public boolean isReceived(QBMessage message) {
        for (int i = 0; i < receivedMessages.size(); i++) {
            QBChatMessage msg = receivedMessages.get(i).getMessage();
            if (msg.getId().equals(id)) {
                remove(i);
            }
        }
        return false;
    }

    private void remove(int index) {
        receivedMessages.remove(index);
    }

    public ArrayList<QBMessage> getReceivedMessages() {
        return receivedMessages;
    }
}
