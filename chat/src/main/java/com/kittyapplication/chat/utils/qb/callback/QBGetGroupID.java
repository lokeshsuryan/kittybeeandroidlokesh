package com.kittyapplication.chat.utils.qb.callback;

import com.quickblox.chat.QBGroupChatManager;
import com.quickblox.chat.model.QBDialog;

/**
 * Created by Pintu Riontech on 20/8/16.
 * vaghela.pintu31@gmail.com
 */
public interface QBGetGroupID {

    void getQuickBloxGroupID(QBDialog dialog, String message, QBGroupChatManager groupChatManager);

    void getError(Exception e);
}
