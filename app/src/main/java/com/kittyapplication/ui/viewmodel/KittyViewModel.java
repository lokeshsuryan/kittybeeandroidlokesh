/*
 * Copyright (c) 2016 riontech-xten
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kittyapplication.ui.viewmodel;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.google.gson.Gson;
import com.kittyapplication.chat.utils.SharedPreferencesUtil;
import com.kittyapplication.chat.utils.chat.ChatHelper;
import com.kittyapplication.model.ChatData;
import com.kittyapplication.model.Kitty;
import com.kittyapplication.providers.KittyBeeContract;
import com.kittyapplication.sqlitedb.DBQueryHandler;
import com.kittyapplication.sqlitedb.DBQueryHandler.OnQueryHandlerListener;
import com.kittyapplication.sqlitedb.MySQLiteHelper;
import com.kittyapplication.ui.executor.BackgroundExecutorThread;
import com.kittyapplication.ui.executor.Interactor;
import com.kittyapplication.utils.AppLog;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBDialogCustomData;
import com.quickblox.chat.model.QBDialogType;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class KittyViewModel {
    private static final String TAG = KittyViewModel.class.getSimpleName();
    private Context context;
    private static final String KEY_ID = "auto_increment_key";

    public KittyViewModel(Context context) {
        this.context = context;
    }

    public int getKittyId(QBChatDialog dialog) {
        return (Integer) dialog.getCustomData().get(KEY_ID);
    }

    public QBDialogCustomData getCustomData(int id) {
        QBDialogCustomData data = new QBDialogCustomData("Kitty");
        data.put(KEY_ID, id);
        return data;
    }

    public String getRawGeneratedDialogId(int id) {
        return KittyBeeContract.Kitties.QB_DIALOG_ID + "_" + id;
    }

    public int getRecordIdFromRawDialogId(String rawDialogId) {
        String rawId = rawDialogId.substring(KittyBeeContract.Kitties.GROUP_ID.length(), rawDialogId.length());
        return Integer.parseInt(rawId);
    }

    public String removeSDFFromQBDialog(String jsonStr) {
        try {
            if (jsonStr == null)
                return jsonStr;
            JSONObject json = new JSONObject(jsonStr);
            if (json.has("sdf")) {
                json.remove("sdf");
            }
            return json.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return jsonStr;
        }
    }

    public void saveKitty(Kitty kitty) {
        Gson gson = new Gson();
        ContentValues values = new ContentValues();
        DBQueryHandler queryHandler = new DBQueryHandler(context.getContentResolver());
        try {
            if (kitty.getGroup() != null) {
                values.put(KittyBeeContract.Kitties.GROUP_ID, kitty.getGroup().getGroupID());
                values.put(KittyBeeContract.Kitties.QB_DIALOG_ID, kitty.getGroup().getQuickId());
                values.put(KittyBeeContract.Kitties.GROUP, gson.toJson(kitty.getGroup()));
            }

            if (kitty.getQbDialog() != null) {
                values.put(KittyBeeContract.Kitties.QB_DIALOG_ID, kitty.getQbDialog().getDialogId());
                values.put(KittyBeeContract.Kitties.QB_LAST_MSG_TIMESTAMP, kitty.getQbDialog().getLastMessageDateSent());
                values.put(KittyBeeContract.Kitties.QB_DIALOG, gson.toJson(kitty.getQbDialog()));
                QBChatDialog chatDialog = kitty.getQbDialog();
                if (chatDialog.getType().equals(QBDialogType.PRIVATE)) {
                    List<Integer> members = new ArrayList<>();
                    members.addAll(chatDialog.getOccupants());
                    members.remove(SharedPreferencesUtil.getQbUser().getId());
                    values.put(KittyBeeContract.Kitties.PRIVATE_CHAT_MEMBER_ID, members.get(0));
                }
            }

//            System.out.println("saveKitty::" + kitty.toString());
            queryHandler.startInsert(0, null, KittyBeeContract.Kitties.CONTENT_URI, values);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveKitties(ArrayList<Kitty> Kitties) {
        for (Kitty kitty : Kitties) {
            saveKitty(kitty);
        }
    }

    public void saveQBDialog(QBChatDialog dialog) {
        Kitty kitty = new Kitty();
        kitty.setQbDialog(dialog);
        saveKitty(kitty);
    }

    public void saveGroup(ChatData group) {
        Kitty kitty = new Kitty();
        kitty.setGroup(group);
        saveKitty(kitty);
    }

    public void updateKitty(Kitty kitty) {
        try {
            DBQueryHandler queryHandler = new DBQueryHandler(context.getContentResolver());
            String selection = KittyBeeContract.Kitties._ID + "=?";
            String[] selectionArg = new String[]{"" + kitty.getId()};

            Gson gson = new Gson();
            ContentValues values = new ContentValues();
            values.put(KittyBeeContract.Kitties.GROUP_ID, kitty.getGroup().getGroupID());
            values.put(KittyBeeContract.Kitties.QB_DIALOG_ID, kitty.getQbDialog().getDialogId());
            values.put(KittyBeeContract.Kitties.GROUP, gson.toJson(kitty.getGroup()));
            values.put(KittyBeeContract.Kitties.QB_DIALOG, gson.toJson(kitty.getQbDialog()));
            queryHandler.startUpdate(0, null, KittyBeeContract.Kitties.CONTENT_URI, values, selection, selectionArg);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public void updateGroup(ChatData group) {
        try {
            String selection = KittyBeeContract.Kitties.GROUP_ID + "=?";
            String[] selectionArg = new String[]{"" + group.getGroupID()};

            Gson gson = new Gson();
            ContentValues values = new ContentValues();
            values.put(KittyBeeContract.Kitties.GROUP_ID, group.getGroupID());
            values.put(KittyBeeContract.Kitties.GROUP, gson.toJson(group));
            DBQueryHandler queryHandler = new DBQueryHandler(context.getContentResolver());
            queryHandler.startUpdate(0, null, KittyBeeContract.Kitties.CONTENT_URI, values, selection, selectionArg);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public void updateDialog(QBChatDialog dialog) {
        updateDialog(new ContentValues(), dialog);
    }

    public void updateWithSyncableDialog(QBChatDialog dialog) {

        try {
            ContentValues values = new ContentValues();
            values.put(KittyBeeContract.Kitties.SYNCABLE, 1);
            updateDialog(values, dialog);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    private void updateDialog(ContentValues values, QBChatDialog dialog) {
        String selection = KittyBeeContract.Kitties.QB_DIALOG_ID + "=?";
        String[] selectionArg = new String[]{"" + dialog.getDialogId()};

        try {
            Gson gson = new Gson();
            values.put(KittyBeeContract.Kitties.QB_DIALOG_ID, dialog.getDialogId());
            values.put(KittyBeeContract.Kitties.QB_DIALOG, gson.toJson(dialog));
            QBDialogCustomData data = dialog.getCustomData();
            if (data != null && data.get(KittyBeeContract.Kitties.LAST_MESSAGE_PAGE_NO) != null) {
                int pageNo = (int) data.get(KittyBeeContract.Kitties.LAST_MESSAGE_PAGE_NO);
                values.put(KittyBeeContract.Kitties.LAST_MESSAGE_PAGE_NO, pageNo);
            }
            values.put(KittyBeeContract.Kitties.QB_LAST_MSG_TIMESTAMP, dialog.getLastMessageDateSent());
//            DBQueryHandler queryHandler = new DBQueryHandler(context.getContentResolver());
            context.getContentResolver().update(KittyBeeContract.Kitties.CONTENT_URI, values, selection, selectionArg);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public void updateDialogByKittyId(int kittyId, QBChatDialog dialog) {
        String selection = KittyBeeContract.Kitties.GROUP_ID + "=?";
        String[] selectionArg = new String[]{"" + kittyId};
//
//        String selection = KittyBeeContract.Kitties.QB_DIALOG_ID + "=?";
//        String[] selectionArg = new String[]{"" + dialog.getDialogId()};
//
//        if (dialog.getCustomData() != null) {
//            int keyId = (int) dialog.getCustomData().get(KEY_ID);
//            selection = KittyBeeContract.ChatDialog._ID + "=?";
//            selectionArg = new String[]{"" + keyId};
//        }

        try {
            Gson gson = new Gson();
            ContentValues values = new ContentValues();
            values.put(KittyBeeContract.Kitties.QB_DIALOG_ID, dialog.getDialogId());
            values.put(KittyBeeContract.Kitties.QB_DIALOG, gson.toJson(dialog));
            DBQueryHandler queryHandler = new DBQueryHandler(context.getContentResolver());
            queryHandler.startUpdate(0, null, KittyBeeContract.Kitties.CONTENT_URI, values, selection, selectionArg);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public void updateGroupByDialogId(ChatData group, String dialogId) {
        try {
            String selection = KittyBeeContract.Kitties.QB_DIALOG_ID + "=?";
            String[] selectionArg = new String[]{"" + dialogId};

            Gson gson = new Gson();
            ContentValues values = new ContentValues();
            values.put(KittyBeeContract.Kitties.GROUP_ID, group.getGroupID());
            values.put(KittyBeeContract.Kitties.GROUP, gson.toJson(group));
            DBQueryHandler queryHandler = new DBQueryHandler(context.getContentResolver());
            queryHandler.startUpdate(0, null, KittyBeeContract.Kitties.CONTENT_URI, values, selection, selectionArg);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public int updateDeleteFlag(int kittyId) {
        int deletedRow = 0;
        try {
            String selection = KittyBeeContract.Kitties._ID + "=?";
            String[] selectionArg = new String[]{"" + kittyId};

            Uri uri = KittyBeeContract.Kitties.CONTENT_URI;
            ContentResolver resolver = context.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(KittyBeeContract.Kitties.DELETED, 1);
            deletedRow = resolver.update(uri, values, selection, selectionArg);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
        return deletedRow;
    }

    public int updatePageNoWithDisableSync(int page, int kittyId) {
        int updateRow = 0;
        try {
            String selection = KittyBeeContract.Kitties._ID + "=?";
            String[] selectionArg = new String[]{"" + kittyId};

            Uri uri = KittyBeeContract.Kitties.CONTENT_URI;
            ContentResolver resolver = context.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(KittyBeeContract.Kitties.LAST_MESSAGE_PAGE_NO, page);
            values.put(KittyBeeContract.Kitties.SYNCABLE, 0);
            DBQueryHandler handler = new DBQueryHandler(resolver);
            handler.startUpdate(0, 0, uri, values, selection, selectionArg);
//            updateRow = resolver.update(uri, values, selection, selectionArg);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
        return updateRow;
    }

    public int updateSyncable(int kittyId) {
        int deletedRow = 0;
        try {
            String selection = KittyBeeContract.Kitties._ID + "=?";
            String[] selectionArg = new String[]{"" + kittyId};

            Uri uri = KittyBeeContract.Kitties.CONTENT_URI;
            ContentResolver resolver = context.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(KittyBeeContract.Kitties.SYNCABLE, 1);
            deletedRow = resolver.update(uri, values, selection, selectionArg);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
        return deletedRow;
    }

    public int updateClearMessageFlag(int kittyId) {
        int deletedRow = 0;
        try {
            String selection = KittyBeeContract.Kitties._ID + "=?";
            String[] selectionArg = new String[]{"" + kittyId};

            Uri uri = KittyBeeContract.Kitties.CONTENT_URI;
            ContentResolver resolver = context.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(KittyBeeContract.Kitties.CLEAR_MESSAGE, 1);
            deletedRow = resolver.update(uri, values, selection, selectionArg);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
        return deletedRow;
    }

    public void fetchKitties(int offset, int limit, OnQueryHandlerListener<ArrayList<Kitty>> listener) {
        String selection = KittyBeeContract.Kitties.DELETED + "=? AND " +
                KittyBeeContract.Kitties.QB_DIALOG_ID + " !=? AND " +
                KittyBeeContract.Kitties.QB_DIALOG + " !=? ";
        String[] selectionArg = new String[]{"0", "", ""};
        limit = limit == 0 ? ChatHelper.ITEMS_PER_PAGE : limit;
        fetchKitties(offset, limit, selection, selectionArg, listener);
    }

    public void fetchSyncableKitties(int offset, OnQueryHandlerListener<ArrayList<Kitty>> listener) {
        String selection = " (" + KittyBeeContract.Kitties.DELETED + "=? AND " +
                KittyBeeContract.Kitties.QB_DIALOG_ID + " !=? AND " +
                KittyBeeContract.Kitties.QB_DIALOG + " !=? ) AND (" +
                KittyBeeContract.Kitties.LAST_MESSAGE_PAGE_NO + " =? OR " +
                KittyBeeContract.Kitties.SYNCABLE + " =? )";
        String[] selectionArg = new String[]{"0", "", "", "0", "1"};

        fetchKitties(offset, ChatHelper.ITEMS_PER_PAGE, selection, selectionArg, listener);
    }

    public void fetchKittyByQBDialogId(String dialogId, OnQueryHandlerListener<ArrayList<Kitty>> listener) {
        String selection = KittyBeeContract.Kitties.DELETED + "=? AND " + KittyBeeContract.Kitties.QB_DIALOG_ID + "=?";
        String[] selectionArg = new String[]{"0", dialogId};

        fetchKitties(0, ChatHelper.ITEMS_PER_PAGE, selection, selectionArg, listener);
    }

    public void fetchKittyById(int kittyId, OnQueryHandlerListener<ArrayList<Kitty>> listener) {
        String selection = KittyBeeContract.Kitties.DELETED + "=? AND " + KittyBeeContract.Kitties._ID + "=?";
        String[] selectionArg = new String[]{"0", "" + kittyId};

        fetchKitties(0, ChatHelper.ITEMS_PER_PAGE, selection, selectionArg, listener);
    }

    private void fetchKitties(int offset, int limit, String selection, String[] selectionArg, final OnQueryHandlerListener listener) {
        Uri uri = KittyBeeContract.Kitties.CONTENT_URI;
        String orderBy = KittyBeeContract.Kitties.QB_LAST_MSG_TIMESTAMP + " DESC ";
        if (limit > 0)
            orderBy += "LIMIT " + offset + ", " + limit;

        try {
            ContentResolver resolver = context.getContentResolver();
            final Cursor cursor = resolver.query(uri,
                    KittyBeeContract.Kitties.PROJECTION_ALL,
                    selection, selectionArg, orderBy);

            final ArrayList<Kitty> kitties = new ArrayList<>();
            BackgroundExecutorThread backgroundExecutorThread = new BackgroundExecutorThread();
            backgroundExecutorThread.execute(new Interactor() {
                @Override
                public void execute() {
                    try {
                        if (cursor != null && cursor.getCount() > 0) {
                            while (cursor.moveToNext()) {
                                Kitty kitty = new Kitty();
                                kitty.setId(cursor.getInt(cursor.getColumnIndex(KittyBeeContract.Kitties._ID)));
                                kitty.setDialogId(cursor.getString(cursor.getColumnIndex(KittyBeeContract.Kitties.QB_DIALOG_ID)));
                                kitty.setGroup(new Gson().fromJson(cursor.getString(cursor.getColumnIndex(KittyBeeContract.Kitties.GROUP)), ChatData.class));
                                String dialogStr = removeSDFFromQBDialog(cursor.getString(cursor.getColumnIndex(KittyBeeContract.Kitties.QB_DIALOG)));
                                kitty.setQbDialog(new Gson().fromJson(dialogStr, QBChatDialog.class));
                                kitty.setLastMessagePageNo(cursor.getInt(cursor.getColumnIndex(KittyBeeContract.Kitties.LAST_MESSAGE_PAGE_NO)));
                                kitties.add(kitty);
                            }
                            cursor.close();
                        }
                        MySQLiteHelper.getInstance(context).close();
                    } catch (Exception e) {
                        AppLog.e(TAG, e.getMessage(), e);
                    }
                }
            }, new Interactor.OnExecutionFinishListener() {
                @Override
                public void onFinish() {
                    listener.onResult(kitties);
                }
            });
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public void fetchRawDialogs(OnQueryHandlerListener listener) {
        String keyGroupId = KittyBeeContract.Kitties.QB_DIALOG_ID;
        String selection = keyGroupId + " LIKE ?";
        String[] selectionArg = new String[]{keyGroupId + "%"};
        fetchDialogs(selection, selectionArg, listener);
    }

    public void fetchRawDialogKitties(OnQueryHandlerListener listener) {
        String keyGroupId = KittyBeeContract.Kitties.QB_DIALOG_ID;
        String selection = keyGroupId + " LIKE ?";
        String[] selectionArg = new String[]{keyGroupId + "%"};
        fetchKitties(0, 0, selection, selectionArg, listener);
    }

    public void fetchGroups(int offset, int limit, final OnQueryHandlerListener listener) {
        String selection = KittyBeeContract.Kitties.DELETED + "=? AND " + KittyBeeContract.Kitties.GROUP_ID + " != ?";
        String[] selectionArg = new String[]{"0", ""};
        limit = limit == 0 ? ChatHelper.ITEMS_PER_PAGE : limit;
        fetchGroups(offset, limit, selection, selectionArg, listener);
    }

    private void fetchGroups(int offset, int limit, String selection, String[] selectionArg, final OnQueryHandlerListener listener) {
        Uri uri = KittyBeeContract.Kitties.CONTENT_URI;
        String orderBy = KittyBeeContract.Kitties.TIMESTAMP + " DESC ";
        if (limit > 0)
            orderBy += "LIMIT " + offset + ", " + limit;


        try {
            ContentResolver resolver = context.getContentResolver();
            final Cursor cursor = resolver.query(uri,
                    new String[]{KittyBeeContract.Kitties.GROUP},
                    selection, selectionArg, orderBy);

            final ArrayList<ChatData> groups = new ArrayList<>();
            BackgroundExecutorThread backgroundExecutorThread = new BackgroundExecutorThread();
            backgroundExecutorThread.execute(new Interactor() {
                @Override
                public void execute() {
                    try {
                        if (cursor != null && cursor.getCount() > 0) {
                            while (cursor.moveToNext()) {
                                String strJson = cursor.getString(0);
                                ChatData group = new Gson().fromJson(strJson, ChatData.class);
                                groups.add(group);
                            }
                            cursor.close();
                        }
                        MySQLiteHelper.getInstance(context).close();
                    } catch (Exception e) {
                        AppLog.e(TAG, e.getMessage(), e);
                    }
                }
            }, new Interactor.OnExecutionFinishListener() {
                @Override
                public void onFinish() {
                    listener.onResult(groups);
                }
            });
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }

//        DBQueryHandler queryHandler = new DBQueryHandler(context.getContentResolver()) {
//            @Override
//            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
//                super.onQueryComplete(token, cookie, cursor);
//                ArrayList<ChatData> groups = new ArrayList<>();
//                if (cursor != null && cursor.getCount() > 0) {
//                    while (cursor.moveToNext()) {
//                        String strJson = cursor.getString(0);
//                        ChatData group = new Gson().fromJson(strJson, ChatData.class);
//                        groups.add(group);
//                    }
//                    cursor.close();
//                }
//                MySQLiteHelper.getInstance(context).close();
//                listener.onResult(groups);
//            }
//        };
//
//        queryHandler.startQuery(0, null, uri,
//                new String[]{KittyBeeContract.Kitties.GROUP},
//                selection, selectionArg, orderBy);
    }

    public void fetchGroup(String groupId, final OnQueryHandlerListener listener) {
        String selection = KittyBeeContract.Kitties.GROUP_ID + "=? AND "
                + KittyBeeContract.Kitties.DELETED + "=?";

        String[] selectionArg = new String[]{groupId, "0"};

        Uri uri = KittyBeeContract.Kitties.CONTENT_URI;

        fetchGroups(0, 1, selection, selectionArg, listener);

    }

    public void fetchQBDialog(String dialogId, final OnQueryHandlerListener listener) {
        String selection = KittyBeeContract.Kitties.QB_DIALOG_ID + "=? AND "
                + KittyBeeContract.Kitties.DELETED + "=?";

        String[] selectionArg = new String[]{dialogId, "0"};

        fetchDialogs(selection, selectionArg, listener);
    }

    public void fetchQBDialogs(final OnQueryHandlerListener listener) {
        String selection = KittyBeeContract.Kitties.DELETED + "=?";

        String[] selectionArg = new String[]{"0"};

        fetchDialogs(selection, selectionArg, listener);
    }

    private void fetchDialogs(String selection, String[] selectionArg, final OnQueryHandlerListener listener) {
        Uri uri = KittyBeeContract.Kitties.CONTENT_URI;

        try {
            ContentResolver resolver = context.getContentResolver();
            final Cursor cursor = resolver.query(uri,
                    new String[]{KittyBeeContract.Kitties.QB_DIALOG},
                    selection, selectionArg, null);

            final ArrayList<QBChatDialog> dialogs = new ArrayList<>();
            BackgroundExecutorThread backgroundExecutorThread = new BackgroundExecutorThread();
            backgroundExecutorThread.execute(new Interactor() {
                @Override
                public void execute() {
                    try {
                        if (cursor != null && cursor.getCount() > 0) {
                            while (cursor.moveToNext()) {
                                String strJson = removeSDFFromQBDialog(cursor.getString(0));
                                QBChatDialog dialog = new Gson().fromJson(strJson, QBChatDialog.class);
                                dialogs.add(dialog);
                            }
                            cursor.close();
                        }
                        MySQLiteHelper.getInstance(context).close();

                    } catch (Exception e) {
                        AppLog.e(TAG, e.getMessage(), e);
                    }
                }
            }, new Interactor.OnExecutionFinishListener() {
                @Override
                public void onFinish() {
                    listener.onResult(dialogs);
                }
            });
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public void fetchDeletedKitties(OnQueryHandlerListener listener) {

        try {
            String keyGroupId = KittyBeeContract.Kitties.QB_DIALOG_ID;
            String selection = KittyBeeContract.Kitties.DELETED + "=? AND ";
            selection += keyGroupId + " NOT LIKE ?";
            String[] selectionArg = new String[]{"1", keyGroupId + "%"};

            fetchKitties(0, 0, selection, selectionArg, listener);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public void fetchClearMessageDialog(OnQueryHandlerListener listener) {

        String selection = KittyBeeContract.Kitties.CLEAR_MESSAGE + "=?";
        String[] selectionArg = new String[]{"1"};

        fetchDialogs(selection, selectionArg, listener);
    }

    public int deleteKitty(int id) {
        int deletedRow = 0;
        try {
            Uri uri = KittyBeeContract.Kitties.CONTENT_URI;

            String selection = KittyBeeContract.Kitties._ID + "=?";
            String[] selectionArg = new String[]{"" + id};
            ContentResolver resolver = context.getContentResolver();
            deletedRow = resolver.delete(uri, selection, selectionArg);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
        return deletedRow;
    }

    public int deleteKittyByGroupId(String groupId) {
        try {
            String selection = KittyBeeContract.Kitties.GROUP_ID + "=?";
            String[] selectionArg = new String[]{"" + groupId};
            Uri uri = KittyBeeContract.Kitties.CONTENT_URI;
            DBQueryHandler queryHandler = new DBQueryHandler(context.getContentResolver());
            queryHandler.startDelete(0, null, uri, selection, selectionArg);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
        return 0;
    }

    public int deleteKittyByQBDialogId(String qbdialogId) {
        try {
            String selection = KittyBeeContract.Kitties.QB_DIALOG_ID + "=?";
            String[] selectionArg = new String[]{"" + qbdialogId};
            Uri uri = KittyBeeContract.Kitties.CONTENT_URI;
            DBQueryHandler queryHandler = new DBQueryHandler(context.getContentResolver());
            queryHandler.startDelete(0, null, uri, selection, selectionArg);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
        return 0;
    }

    public boolean isDeleted(int kittyId) {
        boolean deleted = false;
        try {
            Uri uri = KittyBeeContract.Kitties.CONTENT_URI;

            String selection = KittyBeeContract.Kitties._ID + "=?";
            String[] selectionArg = new String[]{"" + kittyId};

            ContentResolver resolver = context.getContentResolver();
            Cursor cursor = resolver.query(
                    uri,                      // the URI to query
                    new String[]{KittyBeeContract.Kitties.DELETED},   // the projection to use
                    selection,                           // the where clause without the WHERE keyword
                    selectionArg,                           // any wildcard substitutions
                    null);                          // the sort order without the SORT BY keyword

            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    //TODO if get String grt group id than use get string 2
                    deleted = cursor.getInt(0) == 1 ? true : false;
                }
                cursor.close();
            }
            MySQLiteHelper.getInstance(context).close();
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
        return deleted;
    }

    public boolean isMessageCleared(String dialogId) {
        boolean cleared = false;
        try {
            Uri uri = KittyBeeContract.Kitties.CONTENT_URI;

            String selection = KittyBeeContract.Kitties.QB_DIALOG_ID + "=?";
            String[] selectionArg = new String[]{dialogId};

            ContentResolver resolver = context.getContentResolver();
            Cursor cursor = resolver.query(
                    uri,                      // the URI to query
                    new String[]{KittyBeeContract.Kitties.CLEAR_MESSAGE},   // the projection to use
                    selection,                           // the where clause without the WHERE keyword
                    selectionArg,                           // any wildcard substitutions
                    null);                          // the sort order without the SORT BY keyword

            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    //TODO if get String grt group id than use get string 2
                    cleared = cursor.getInt(0) == 1 ? true : false;
                }
                cursor.close();
            }
            MySQLiteHelper.getInstance(context).close();
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
        return cleared;
    }

    public int getLastKittyId() {
        Uri uri = KittyBeeContract.Kitties.CONTENT_URI;
        int maxId = 0;

        try {
            ContentResolver resolver = context.getContentResolver();
            Cursor cursor = resolver.query(
                    uri,                      // the URI to query
                    new String[]{KittyBeeContract.Kitties._ID},   // the projection to use
                    null,                           // the where clause without the WHERE keyword
                    null,                           // any wildcard substitutions
                    KittyBeeContract.Kitties._ID + " DESC LIMIT 1");                          // the sort order without the SORT BY keyword

            if (cursor != null && cursor.getCount() > 0) {
                // show data from db
                while (cursor.moveToNext()) {
                    maxId = cursor.getInt(0);
                }
                cursor.close();
            }
            MySQLiteHelper.getInstance(context).close();
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
        return maxId + 1;
    }

    public void getPrivateDialogWithUser(Integer memberId, OnQueryHandlerListener listener) {
        try {
            String selection = KittyBeeContract.Kitties.PRIVATE_CHAT_MEMBER_ID + "=?";
            String[] selectionArg = new String[]{"" + memberId};
            fetchKitties(0, 0, selection, selectionArg, listener);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

}
