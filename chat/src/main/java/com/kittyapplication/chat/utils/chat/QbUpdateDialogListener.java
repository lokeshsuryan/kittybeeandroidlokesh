package com.kittyapplication.chat.utils.chat;

import com.quickblox.chat.model.QBChatDialog;

/**
 * Created by Pintu Riontech on 30/8/16.
 * vaghela.pintu31@gmail.com
 */
public interface QbUpdateDialogListener {

    void onSuccessResponse(QBChatDialog dialog);

    void onError();
}
