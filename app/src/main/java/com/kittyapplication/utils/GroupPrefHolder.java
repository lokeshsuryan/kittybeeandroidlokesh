package com.kittyapplication.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kittyapplication.core.utils.SharedPrefsHelper;
import com.kittyapplication.model.ChatData;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by HalfBloodPrince(RIONTECH)
 * Riontech on 21/10/16.
 */

public class GroupPrefHolder {

    private static GroupPrefHolder instance;
    private ArrayList<ChatData> groupList;

    public static synchronized GroupPrefHolder getInstance() {
        if (instance == null) {
            instance = new GroupPrefHolder();
        }
        return instance;
    }

    private GroupPrefHolder() {
        groupList = getList();
    }

    public ArrayList<ChatData> getGroupList() {
        return groupList;
    }

    public synchronized void saveGroupChats(List<ChatData> groupList) {
        if (groupList != null && groupList.size() > 0) {
            String strGroupChats = new Gson().toJson(groupList);
            SharedPrefsHelper.getInstance().save(AppConstant.PREFERANCE_CHAT, strGroupChats);
            this.groupList = getList();
        }
    }

    private ArrayList<ChatData> getList() {
        String json = SharedPrefsHelper.getInstance().get(AppConstant.PREFERANCE_CHAT, null);
        if (json != null) {
            Type type = new TypeToken<ArrayList<ChatData>>() {
            }.getType();
            return new Gson().fromJson(json, type);
        }
        return new ArrayList<>();
    }

    public synchronized void add(ChatData groupChat) {
        groupList.add(groupChat);
        saveGroupChats(groupList);
    }

    public synchronized void addAt(ChatData groupChat, int index) {
        groupList.add(index, groupChat);
        saveGroupChats(groupList);
    }

    public synchronized void update(ChatData groupChat) {
        for (int i = 0; i < groupList.size(); i++) {
            ChatData chat = groupList.get(i);
            if (chat.getGroupID().equals(groupChat.getGroupID())) {
                groupList.set(i, groupChat);
            }
        }
        saveGroupChats(groupList);
    }

    public synchronized void updateByQBId(ChatData groupChat) {
        for (int i = 0; i < groupList.size(); i++) {
            ChatData chat = groupList.get(i);
            if (chat.getQuickId().equals(groupChat.getQuickId())) {
                groupList.set(i, groupChat);
            }
        }
        saveGroupChats(groupList);
    }

    public synchronized void remove(ChatData groupChat) {
        for (int i = 0; i < groupList.size(); i++) {
            ChatData chat = groupList.get(i);
            if (chat.getGroupID().equals(groupChat.getGroupID())) {
                groupList.remove(i);
            }
        }
        saveGroupChats(groupList);
    }

    public synchronized void remove(String groupId) {
        for (int i = 0; i < groupList.size(); i++) {
            ChatData chat = groupList.get(i);
            if (chat.getGroupID().equals(groupId)) {
                groupList.remove(i);
            }
        }
        saveGroupChats(groupList);
    }

    public ChatData getByGroupId(String groupId) {
        for (int i = 0; i < groupList.size(); i++) {
            ChatData chat = groupList.get(i);
            if (chat.getGroupID().equals(groupId)) {
                return chat;
            }
        }
        return null;
    }

    public void clear() {
        SharedPrefsHelper.getInstance().delete(AppConstant.PREFERANCE_CHAT);
    }
}
