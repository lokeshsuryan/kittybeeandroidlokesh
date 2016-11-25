package com.kittyapplication.chat.utils.qb;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kittyapplication.chat.utils.Consts;
import com.kittyapplication.core.utils.SharedPrefsHelper;
import com.quickblox.chat.model.QBDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class QbDialogHolder {

    private static QbDialogHolder instance;
    private List<QBDialog> dialogList;

    public static synchronized QbDialogHolder getInstance() {
        if (instance == null) {
            instance = new QbDialogHolder();
        }
        return instance;
    }

    private QbDialogHolder() {
        dialogList = getListFromPreference();
    }

    private synchronized void saveIntoPreference(List<QBDialog> dialogs) {
        if (dialogs != null && dialogs.size() > 0) {
            String strDialogs = new Gson().toJson(dialogs);
            try {
                JSONArray jsonArray = new JSONArray(strDialogs);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    if (json.has("sdf")) {
                        json.remove("sdf");
                        System.out.println("After QBDialog = > " + strDialogs);
                    }
                }
                strDialogs = jsonArray.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            SharedPrefsHelper.getInstance().save(Consts.PREF_KEY_DIALOGS, strDialogs);
        }
    }

    private ArrayList<QBDialog> getListFromPreference() {
        String json = SharedPrefsHelper.getInstance().get(Consts.PREF_KEY_DIALOGS, null);
        if (json != null) {
            Type type = new TypeToken<ArrayList<QBDialog>>() {
            }.getType();
            return new Gson().fromJson(json, type);
        }
        return new ArrayList<>();
    }

    public List<QBDialog> getDialogList() {
        return dialogList;
    }

    public void clear() {
        dialogList.clear();
        SharedPrefsHelper.getInstance().delete(Consts.PREF_KEY_DIALOGS);
    }


    public synchronized void addDialogToList(QBDialog dialog) {
        if (!dialogList.contains(dialog)) {
            dialogList.add(dialog);
            saveIntoPreference(dialogList);
        }
    }

    public synchronized void addDialogToList(QBDialog dialog, int pos) {
        if (!dialogList.contains(dialog)) {
            dialogList.add(pos, dialog);
            saveIntoPreference(dialogList);
        } else {
            changeIndex(pos, dialog);
        }
    }

    public synchronized void addDialogs(List<QBDialog> dialogs) {
        for (int i = 0; i < dialogs.size(); i++) {
            addDialogToList(dialogs.get(i), i);
        }
    }

    public QBDialog getQBDialogByUserId(Integer id) {
        for (QBDialog dialog : dialogList) {
            if (dialog.getUserId() == id) {
                return dialog;
            }
        }
        return null;
    }

    public QBDialog getQBDialogByLogin(Integer id) {
        for (QBDialog dialog : dialogList) {
            if (dialog.getUserId() == id) {
                return dialog;
            }
        }
        return null;
    }

    public QBDialog getQBDialogByDialogId(String id) {
        for (QBDialog dialog : dialogList) {
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
                QBDialog dialog = dialogList.get(i);
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

    public synchronized void changeIndex(int index, QBDialog qbdialog) {
        try {
            for (int i = 0; i < dialogList.size(); i++) {
                QBDialog dialog = dialogList.get(i);
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
}
