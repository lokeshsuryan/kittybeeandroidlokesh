package com.kittyapplication.listener;

/**
 * Created by Pintu Riontech on 29/8/16.
 * vaghela.pintu31@gmail.com
 */
public interface ChatOptionClickListener {


    // delete group chat data
    void onDeleteGroup(int pos);

    // refresh group chat data
    void onRefreshGroupChatData(int pos);


    // delete private chat data
    void onDeletePrivateChatData(int pos);

}
