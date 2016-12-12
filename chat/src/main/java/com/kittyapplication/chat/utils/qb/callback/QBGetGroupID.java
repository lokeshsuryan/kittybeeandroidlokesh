package com.kittyapplication.chat.utils.qb.callback;

import com.quickblox.chat.QBGroupChatManager;
import com.quickblox.chat.model.QBChatDialog;

/**
 * Created by Pintu Riontech on 20/8/16.
 * vaghela.pintu31@gmail.com
 */
public interface QBGetGroupID {

    void getQuickBloxGroupID(QBChatDialog dialog, String message);

    void getError(Exception e);
}
