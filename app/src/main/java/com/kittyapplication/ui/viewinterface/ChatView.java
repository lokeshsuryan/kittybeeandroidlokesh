package com.kittyapplication.ui.viewinterface;

import com.kittyapplication.model.ChatData;

import java.util.List;

/**
 * Created by Pintu Riontech on 8/8/16.
 * vaghela.pintu31@gmail.com
 */
public interface ChatView {

    void getChatList(List<ChatData> list);
    void showEmptyView();
}
