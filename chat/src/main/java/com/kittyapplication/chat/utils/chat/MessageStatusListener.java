package com.kittyapplication.chat.utils.chat;

import com.quickblox.chat.model.QBChatMessage;

/**
 * Created by HalfBloodPrince(RIONTECH)
 * Riontech on 12/11/16.
 */

public interface MessageStatusListener {

    void onMessageFail(QBChatMessage chatMessage);

    void onMessageSent(QBChatMessage chatMessage);

    void onMessageDelivered(String messageID, String dialogID, Integer occupantsID);

    void onMessageRead(String messageID, String dialogID, Integer occupantsID);
}
