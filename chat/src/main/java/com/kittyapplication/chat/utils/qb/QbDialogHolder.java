package com.kittyapplication.chat.utils.qb;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kittyapplication.chat.utils.Consts;
import com.kittyapplication.core.utils.SharedPrefsHelper;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.users.model.QBUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class QbDialogHolder {

    private static QbDialogHolder instance;
    private List<QBChatDialog> dialogList;

    public static synchronized QbDialogHolder getInstance() {
        if (instance == null) {
            instance = new QbDialogHolder();
        }
        return instance;
    }

    private QbDialogHolder() {
        dialogList = getListFromPreference();
    }

    private synchronized void saveIntoPreference(List<QBChatDialog> dialogs) {
        if (dialogs != null && dialogs.size() > 0) {
            String strDialogs = new Gson().toJson(dialogs);
            try {
                JSONArray jsonArray = new JSONArray(strDialogs);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    if (json.has("sdf")) {
                        json.remove("sdf");
                        System.out.println("After QBChatDialog = > " + strDialogs);
                    }
                }
                strDialogs = jsonArray.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            SharedPrefsHelper.getInstance().save(Consts.PREF_KEY_DIALOGS, strDialogs);
        }
    }

    private ArrayList<QBChatDialog> getListFromPreference() {
        String json = SharedPrefsHelper.getInstance().get(Consts.PREF_KEY_DIALOGS, null);
        if (json != null) {
            Type type = new TypeToken<ArrayList<QBChatDialog>>() {
            }.getType();
            return new Gson().fromJson(json, type);
        }
        return new ArrayList<>();
    }

    public List<QBChatDialog> getDialogList() {
        return dialogList;
    }

    public void clear() {
        dialogList.clear();
        SharedPrefsHelper.getInstance().delete(Consts.PREF_KEY_DIALOGS);
    }


    public synchronized void addDialogToList(QBChatDialog dialog) {
        if (!dialogList.contains(dialog)) {
            dialogList.add(dialog);
            saveIntoPreference(dialogList);
        }
    }

    public synchronized void addDialogToList(QBChatDialog dialog, int pos) {
        if (!dialogList.contains(dialog)) {
            dialogList.add(pos, dialog);
            saveIntoPreference(dialogList);
        } else {
            changeIndex(pos, dialog);
        }
    }

    public synchronized void addDialogs(List<QBChatDialog> dialogs) {
        for (int i = 0; i < dialogs.size(); i++) {
            addDialogToList(dialogs.get(i), i);
        }
    }

    public QBChatDialog getQBChatDialogByUserId(Integer id) {
        for (QBChatDialog dialog : dialogList) {
            if (dialog.getUserId() == id) {
                return dialog;
            }
        }
        return null;
    }

    public QBChatDialog getQBChatDialogByLogin(Integer id) {
        for (QBChatDialog dialog : dialogList) {
            if (dialog.getUserId() == id) {
                return dialog;
            }
        }
        return null;
    }

    public QBChatDialog getQBChatDialogByDialogId(String id) {
        for (QBChatDialog dialog : dialogList) {
            if (dialog.getDialogId().equals(id)) {
                return dialog;
            }
        }
        return null;
    }

    public synchronized void deleteDialogById(String dialogId) {
        if (dialogId != null && dialogId.length() > 0) {
            for (int i = 0; i < dialogList.size(); i++) {
                if (dialogList.get(i).getDialogId() != null
                        && dialogList.get(i).getDialogId().length() > 0) {
                    if (dialogList.get(i).getDialogId().equalsIgnoreCase(dialogId)) {
                        dialogList.remove(i);
                        break;
                    }
                }
            }
            saveIntoPreference(dialogList);
        }
    }

    public int getDialogIndex(String dialogId) {
        try {
            for (int i = 0; i < dialogList.size(); i++) {
                QBChatDialog dialog = dialogList.get(i);
                if (dialog.getDialogId().equals(dialogId)) {
                    return i;
                }
            }
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public synchronized void changeIndex(int index, QBChatDialog qbdialog) {
        try {
            for (int i = 0; i < dialogList.size(); i++) {
                QBChatDialog dialog = dialogList.get(i);
                String dialogId = qbdialog.getDialogId();
                if (dialog.getDialogId().equals(dialogId)) {
                    dialogList.remove(i);
                    dialogList.add(index, qbdialog);
                    break;
                }
            }
            saveIntoPreference(dialogList);
        } catch (Exception e) {
        }
    }

    /**
     * Upgraded version code
     */

    private Map<String, QBChatDialog> dialogsMap;

//    public static synchronized QbDialogHolder getInstance() {
//        if (instance == null) {
//            instance = new QbDialogHolder();
//        }
//        return instance;
//    }
//
//    private QbDialogHolder() {
//        dialogsMap = new TreeMap<>();
//    }

    public Map<String, QBChatDialog> getDialogs() {
        return getSortedMap(dialogsMap);
    }

    public QBChatDialog getChatDialogById(String dialogId){
        return dialogsMap.get(dialogId);
    }

//    public void clear() {
//        dialogsMap.clear();
//    }

    public void addDialog(QBChatDialog dialog) {
        if (dialog != null) {
            dialogsMap.put(dialog.getDialogId(), dialog);
        }
    }
//
//    public void addDialogs(List<QBChatDialog> dialogs) {
//        for (QBChatDialog dialog : dialogs) {
//            addDialog(dialog);
//        }
//    }

    public void deleteDialogs(Collection<QBChatDialog> dialogs) {
        for (QBChatDialog dialog : dialogs) {
            deleteDialog(dialog);
        }
    }

    public void deleteDialogs(ArrayList<String> dialogsIds) {
        for (String dialogId : dialogsIds) {
            deleteDialog(dialogId);
        }
    }

    public void deleteDialog(QBChatDialog chatDialog){
        dialogsMap.remove(chatDialog.getDialogId());
    }

    public void deleteDialog(String dialogId){
        dialogsMap.remove(dialogId);
    }

    public boolean hasDialogWithId(String dialogId){
        return dialogsMap.containsKey(dialogId);
    }

    public boolean hasPrivateDialogWithUser(QBUser user){
        return getPrivateDialogWithUser(user) != null;
    }

    public QBChatDialog getPrivateDialogWithUser(QBUser user){
        for (QBChatDialog chatDialog : dialogsMap.values()){
            if (QBDialogType.PRIVATE.equals(chatDialog.getType())
                    && chatDialog.getOccupants().contains(user.getId())){
                return chatDialog;
            }
        }

        return null;
    }

    private Map<String, QBChatDialog> getSortedMap(Map <String, QBChatDialog> unsortedMap){
        Map <String, QBChatDialog> sortedMap = new TreeMap(new LastMessageDateSentComparator(unsortedMap));
        sortedMap.putAll(unsortedMap);
        return sortedMap;
    }

    public void updateDialog(String dialogId, QBChatMessage qbChatMessage){
        QBChatDialog updatedDialog = getChatDialogById(dialogId);
        updatedDialog.setLastMessage(qbChatMessage.getBody());
        updatedDialog.setLastMessageDateSent(qbChatMessage.getDateSent());
        updatedDialog.setUnreadMessageCount(updatedDialog.getUnreadMessageCount() != null
                ? updatedDialog.getUnreadMessageCount() + 1 : 1);
        updatedDialog.setLastMessageUserId(qbChatMessage.getSenderId());

        dialogsMap.put(updatedDialog.getDialogId(), updatedDialog);
    }

    static class LastMessageDateSentComparator implements Comparator<String> {
        Map<String, QBChatDialog> map;

        public LastMessageDateSentComparator(Map <String, QBChatDialog> map) {

            this.map = map;
        }

        public int compare(String keyA, String keyB) {

            long valueA = map.get(keyA).getLastMessageDateSent();
            long valueB = map.get(keyB).getLastMessageDateSent();

            if (valueB < valueA){
                return -1;
            } else {
                return 1;
            }
        }
    }
}
