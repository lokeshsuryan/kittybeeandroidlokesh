package com.kittyapplication.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kittyapplication.core.utils.SharedPrefsHelper;
import com.kittyapplication.model.Kitty;
import com.quickblox.chat.model.QBDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by HalfBloodPrince(RIONTECH)
 * Riontech on 21/10/16.
 */

public class KittyPrefHolder {

    private static KittyPrefHolder instance;
    private ArrayList<Kitty> groupList;

    public static synchronized KittyPrefHolder getInstance() {
        if (instance == null) {
            instance = new KittyPrefHolder();
        }
        return instance;
    }

    private KittyPrefHolder() {
        groupList = getList();
    }

    public ArrayList<Kitty> getGroupList() {
        return groupList;
    }

    public synchronized void saveGroupChats(List<Kitty> groupList) {
        if (groupList != null && groupList.size() > 0) {
//            String strGroupChats = new Gson().toJson(groupList);
            String groups = new Gson().toJson(groupList);
            try {
                JSONArray jsonArray = new JSONArray(groups);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json = jsonArray.getJSONObject(i).getJSONObject("qbDialog");
                    if (json.has("sdf")) {
                        json.remove("sdf");
                    }
                }
                groups = jsonArray.toString();
                SharedPrefsHelper.getInstance().save(AppConstant.PREF_KITTIES, groups);
                this.groupList = getList();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private ArrayList<Kitty> getList() {
        String json = SharedPrefsHelper.getInstance().get(AppConstant.PREF_KITTIES, null);
        if (json != null) {
            Type type = new TypeToken<ArrayList<Kitty>>() {
            }.getType();
            return new Gson().fromJson(json, type);
        }
        return new ArrayList<>();
    }

    public synchronized void add(Kitty kitty) {
        groupList.add(kitty);
        saveGroupChats(groupList);
    }

    public void addAt(Kitty kitty, int index) {
        groupList.add(index, kitty);
        saveGroupChats(groupList);
    }

    public synchronized void update(Kitty kitty) {
        for (int i = 0; i < groupList.size(); i++) {
            Kitty chat = groupList.get(i);
            QBDialog dialog = chat.getQbDialog();
            if (dialog.getDialogId().equals(kitty.getQbDialog().getDialogId())) {
                groupList.set(i, kitty);
            }
        }
        saveGroupChats(groupList);
    }

    public synchronized void updateByQBId(Kitty kitty) {
        for (int i = 0; i < groupList.size(); i++) {
            Kitty chat = groupList.get(i);
            QBDialog dialog = chat.getQbDialog();
            if (dialog.getDialogId().equals(kitty.getQbDialog().getDialogId())) {
                groupList.set(i, kitty);
            }
        }
        saveGroupChats(groupList);
    }

    public synchronized void updateQBDialog(int index, QBDialog qbDialog) {
        for (int i = 0; i < groupList.size(); i++) {
            Kitty chat = groupList.get(i);
            QBDialog dialog = chat.getQbDialog();
            if (dialog.getDialogId().equals(qbDialog.getDialogId())) {
                groupList.remove(i);
                chat.setQbDialog(qbDialog);
                groupList.add(index, chat);
            }
        }
        saveGroupChats(groupList);
    }

    public synchronized void remove(Kitty kitty) {
        for (int i = 0; i < groupList.size(); i++) {
            Kitty chat = groupList.get(i);
            QBDialog dialog = chat.getQbDialog();
            if (dialog.getDialogId().equals(kitty.getQbDialog().getDialogId())) {
                groupList.remove(i);
            }
        }
        saveGroupChats(groupList);
    }

    public synchronized void remove(String dialogId) {
        for (int i = 0; i < groupList.size(); i++) {
            Kitty chat = groupList.get(i);
            QBDialog dialog = chat.getQbDialog();
            if (dialog.getDialogId().equals(dialogId)) {
                groupList.remove(i);
            }
        }
        saveGroupChats(groupList);
    }

    public synchronized void remove(int position) {
        groupList.remove(position);
        saveGroupChats(groupList);
    }

    public Kitty getByDialog(String dialogId) {
        for (int i = 0; i < groupList.size(); i++) {
            Kitty chat = groupList.get(i);
            QBDialog dialog = chat.getQbDialog();
            if (dialog.getDialogId().equals(dialogId)) {
                return chat;
            }
        }
        return null;
    }

    public synchronized void clear() {
        SharedPrefsHelper.getInstance().delete(AppConstant.PREF_KITTIES);
    }
}
