package com.kittyapplication.model;

import java.util.List;

/**
 * Created by Pintu Riontech on 14/8/16.
 * vaghela.pintu31@gmail.com
 */
public class ChatDao {

    private List<ChatData> chatList;

    public List<ChatData> getChatList() {
        return chatList;
    }

    public void setChatList(List<ChatData> chatList) {
        this.chatList = chatList;
    }
}
