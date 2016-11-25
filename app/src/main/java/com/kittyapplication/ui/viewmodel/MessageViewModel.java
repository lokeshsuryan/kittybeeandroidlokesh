package com.kittyapplication.ui.viewmodel;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.kittyapplication.chat.ui.models.QBMessage;
import com.kittyapplication.model.ChatData;
import com.kittyapplication.model.Kitty;
import com.kittyapplication.providers.KittyBeeContract;
import com.kittyapplication.sqlitedb.DBQueryHandler;
import com.kittyapplication.sqlitedb.DBQueryHandler.OnQueryHandlerListener;
import com.kittyapplication.sqlitedb.MySQLiteHelper;
import com.kittyapplication.sqlitedb.SQLConstants;
import com.kittyapplication.ui.executor.BackgroundExecutorThread;
import com.kittyapplication.ui.executor.ExecutorThread;
import com.kittyapplication.ui.executor.Interactor;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by MIT on 9/24/2016.
 */
public class MessageViewModel extends ChatMemberViewModel {
    private static final String TAG = MessageViewModel.class.getSimpleName();
    private Context context;
    private List<Integer> insertedIds;

    public MessageViewModel(Context context) {
        super(context);
        this.context = context;
        insertedIds = Collections.synchronizedList(new ArrayList<Integer>());
    }

    public void saveMessages(ArrayList<QBMessage> messages, int kittyId) {
        for (QBMessage message : messages) {
            saveMessage(message, kittyId);
        }
    }

    public ArrayList<QBMessage> getUnSavedFilteredMessage(ArrayList<QBMessage> savedList, ArrayList<QBMessage> incoming) {
        ArrayList<QBMessage> results = new ArrayList<>();
        try {
            if (savedList.size() > 0) {
                for (QBMessage message : incoming) {
                    boolean isExists = false;
                    for (int i = 0; i < savedList.size(); i++) {
                        QBMessage msg = savedList.get(i);
                        if (msg.getMessage().getId().equals(message.getMessage().getId())) {
                            isExists = true;
                            break;
                        }
                    }
                    if (!isExists) {
                        results.add(message);
                    }
                }
            } else {
                results.addAll(incoming);
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
        return results;
    }

    public void saveMessage(QBMessage message, int kittyId) {
        try {
//            int lastId = getLastMessageId(message.getDialogId()) + 1;
//            String lastRecordId = String.valueOf(lastId);

//            System.out.println("Last Record id::" + lastRecordId);
//            message.setProperty(AppConstant.APP_MSG_ID, "" + lastRecordId);
            QBChatMessage chatMessage = message.getMessage();
            ContentValues values = new ContentValues();
            values.put(KittyBeeContract.ChatMessage.KITTY_ID, kittyId);
            values.put(KittyBeeContract.ChatMessage.SENT, message.getSent());
            values.put(KittyBeeContract.ChatMessage.MESSAGE_ID, chatMessage.getId());
            values.put(KittyBeeContract.ChatMessage.DATA, new Gson().toJson(chatMessage));
            values.put(KittyBeeContract.ChatMessage.TIMESTAMP, chatMessage.getDateSent());

            DBQueryHandler queryHandler = new DBQueryHandler(context.getContentResolver()) {
                @Override
                protected void onInsertComplete(int token, Object cookie, Uri uri) {
                    super.onInsertComplete(token, cookie, uri);
                    insertedIds.add((int) ContentUris.parseId(uri));
                }
            };
            queryHandler.startInsert(0, 0, KittyBeeContract.ChatMessage.CONTENT_URI, values);

        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public void loadMessages(int kittyId, int offset, int limit, final OnQueryHandlerListener listener) {
        System.out.println("loadMessages dialogId::" + kittyId);
        Uri uri = getQueryUri();

        String orderBy = KittyBeeContract.ChatMessage.TIMESTAMP + " ASC LIMIT " + offset + "," + limit;
        String selection = KittyBeeContract.ChatMessage.KITTY_ID + " =? AND "
                + KittyBeeContract.ChatMessage.DELETED + " =?";

        String[] selectionArg = new String[]{"" + kittyId, "0"};

        ContentResolver resolver = context.getContentResolver();
        final Cursor cursor = resolver.query(uri,                      // the URI to query
                KittyBeeContract.ChatMessage.PROJECTION_ALL,   // the projection to use
                selection,                           // the where clause without the WHERE keyword
                selectionArg,                           // any wildcard substitutions
                orderBy);

        final ArrayList<QBMessage> messageList = new ArrayList<>();
        BackgroundExecutorThread backgroundExecutorThread = new BackgroundExecutorThread();
        backgroundExecutorThread.execute(new Interactor() {
            @Override
            public void execute() {
                try {
                    if (cursor != null && cursor.getCount() > 0) {
                        // show data from db
                        Gson gson = new Gson();
                        while (cursor.moveToNext()) {
                            QBMessage message = new QBMessage();
                            message.setId(cursor.getInt(0));
                            message.setKittyId(cursor.getInt(1));
                            message.setMessage(gson.fromJson(cursor.getString(3), QBChatMessage.class));
                            message.setSent(cursor.getInt(4));
                            message.setRead(cursor.getInt(5));
                            message.setDelivered(cursor.getInt(6));
                            messageList.add(message);
                        }
                        cursor.close();

                    }
                } catch (Exception e) {
                    AppLog.e(TAG, e.getMessage(), e);
                }
            }
        }, new Interactor.OnExecutionFinishListener() {
            @Override
            public void onFinish() {
                listener.onResult(messageList);
            }
        });
    }

    public void getMessageByMessageId(String messageId, final OnQueryHandlerListener listener) {
        Uri uri = getQueryUri();

        String selection = KittyBeeContract.ChatMessage.MESSAGE_ID + " =?"
                + KittyBeeContract.ChatMessage.DELETED + " =?";

        String[] selectionArg = new String[]{"" + messageId, "0"};

        ContentResolver resolver = context.getContentResolver();
        final Cursor cursor = resolver.query(uri,                      // the URI to query
                KittyBeeContract.ChatMessage.PROJECTION_ALL,   // the projection to use
                selection,                           // the where clause without the WHERE keyword
                selectionArg,                           // any wildcard substitutions
                null);

        final ArrayList<QBMessage> messageList = new ArrayList<>();
        BackgroundExecutorThread backgroundExecutorThread = new BackgroundExecutorThread();
        backgroundExecutorThread.execute(new Interactor() {
            @Override
            public void execute() {
                try {
                    if (cursor != null && cursor.getCount() > 0) {
                        // show data from db
                        Gson gson = new Gson();
                        while (cursor.moveToNext()) {
                            QBMessage message = new QBMessage();
                            message.setId(cursor.getInt(0));
                            message.setKittyId(cursor.getInt(1));
                            message.setMessage(gson.fromJson(cursor.getString(3), QBChatMessage.class));
                            message.setSent(cursor.getInt(4));
                            message.setRead(cursor.getInt(5));
                            message.setDelivered(cursor.getInt(6));
                            messageList.add(message);
                        }
                        cursor.close();

                    }
                } catch (Exception e) {
                    AppLog.e(TAG, e.getMessage(), e);
                }
            }
        }, new Interactor.OnExecutionFinishListener() {
            @Override
            public void onFinish() {
                listener.onResult(messageList);
            }
        });
    }

    private void deleteMessage(String selection, String[] selectionArg) {
        try {
            DBQueryHandler queryHandler = new DBQueryHandler(context.getContentResolver());
            queryHandler.startDelete(0, 0, getQueryUri(), selection, selectionArg);
            AppLog.d(TAG, "deleteMessage:");
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public void clearHistory(int kittyId) {
        deleteMessage(KittyBeeContract.ChatMessage.KITTY_ID + "=?", new String[]{""+kittyId});
    }

    public void deleteByChatId(String chatId) {
        String selection = KittyBeeContract.ChatMessage.MESSAGE_ID + "=?";
        String[] selectionArg = new String[]{chatId};
        deleteMessage(selection, selectionArg);
    }

    public void deleteByChatIds(Set<String> chatIds) {
        for (String chatId : chatIds) {
            String selection = KittyBeeContract.ChatMessage.MESSAGE_ID + "=?";
            String[] selectionArg = new String[]{chatId};
            deleteMessage(selection, selectionArg);
        }
    }

    private Uri getQueryUri() {
        return KittyBeeContract.ChatMessage.CONTENT_URI;
    }

    public void updateSent(QBChatMessage message, int msgId) {
        try {
            ContentValues values = new ContentValues();
            values.put(KittyBeeContract.ChatMessage.SENT, 1);
            values.put(KittyBeeContract.ChatMessage.MESSAGE_ID, message.getId());
            values.put(KittyBeeContract.ChatMessage.DATA, new Gson().toJson(message));
            updateMessage(values, msgId);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public void updateFail(QBChatMessage message, int msgId) {
        try {
            ContentValues values = new ContentValues();
            values.put(KittyBeeContract.ChatMessage.FAIL, 1);
            values.put(KittyBeeContract.ChatMessage.MESSAGE_ID, message.getId());
            values.put(KittyBeeContract.ChatMessage.DATA, new Gson().toJson(message));
            updateMessage(values, msgId);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public void updateRead(QBChatMessage message, String msgId) {
        try {
            ContentValues values = new ContentValues();
            values.put(KittyBeeContract.ChatMessage.READ, 1);
            values.put(KittyBeeContract.ChatMessage.DATA, new Gson().toJson(message));
            updateMessage(values, msgId);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public void updateRead(String msgId) {
        try {
            ContentValues values = new ContentValues();
            values.put(KittyBeeContract.ChatMessage.READ, 1);
//            values.put(KittyBeeContract.ChatMessage.DATA, new Gson().toJson(message));
            updateMessage(values, msgId);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }


    public void updateDelivered(QBChatMessage message, String msgId) {
        try {
            ContentValues values = new ContentValues();
            values.put(KittyBeeContract.ChatMessage.DELIVERED, 1);
            values.put(KittyBeeContract.ChatMessage.DATA, new Gson().toJson(message));
            updateMessage(values, msgId);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public void updateDelivered(String msgId) {
        try {
            ContentValues values = new ContentValues();
            values.put(KittyBeeContract.ChatMessage.DELIVERED, 1);
//            values.put(KittyBeeContract.ChatMessage.DATA, new Gson().toJson(message));
            updateMessage(values, msgId);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public void updateMessage(QBChatMessage message) {
        try {
            int messageId = Integer.parseInt((String) message.getProperty(AppConstant.LAST_INSERTED_ID));
            ContentValues values = new ContentValues();
            values.put(KittyBeeContract.ChatMessage.MESSAGE_ID, message.getId());
            values.put(KittyBeeContract.ChatMessage.DATA, new Gson().toJson(message));
            updateMessage(values, messageId);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    private void updateMessage(final ContentValues values, final String msgId) {
        try {
            String selection = KittyBeeContract.ChatMessage.MESSAGE_ID + " =?";
            String[] selectionArg = new String[]{"" + msgId};
            updateMessage(values, selection, selectionArg);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    private void updateMessage(final ContentValues values, final int msgId) {
        try {
            String selection = KittyBeeContract.ChatMessage._ID + " =?";
            String[] selectionArg = new String[]{"" + msgId};
            updateMessage(values, selection, selectionArg);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    private void updateMessage(final ContentValues values, final String selection, final String[] selectionArg){
        try {
            ContentResolver resolver = context.getContentResolver();
            resolver.update(KittyBeeContract.ChatMessage.CONTENT_URI, values, selection, selectionArg);

//            ExecutorThread executorThread = new ExecutorThread();
//            executorThread.startExecutor();
//            executorThread.postTask(new Runnable() {
//                @Override
//                public void run() {

//                    DBQueryHandler queryHandler = new DBQueryHandler(context.getContentResolver());
//                    queryHandler.startUpdate(0, 0, KittyBeeContract.ChatMessage.CONTENT_URI, values, selection, selectionArg);
//                }
//            });
//            ContentResolver resolver = context.getContentResolver();
//            resolver.update(KittyBeeContract.ChatMessage.CONTENT_URI, values, selection, selectionArg);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public void updateAllMessageDeleted(int kittyId) {
        try {
            ContentValues values = new ContentValues();
            values.put(KittyBeeContract.ChatMessage.DELETED, 1);
            String where = KittyBeeContract.ChatMessage.KITTY_ID + "=?";
            String[] selectionArg = new String[]{""+kittyId};

            DBQueryHandler queryHandler = new DBQueryHandler(context.getContentResolver());
            queryHandler.startUpdate(0, 0, KittyBeeContract.ChatMessage.CONTENT_URI, values, where, selectionArg);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

//    public void updateMessageDialogId(String oldDialogId, String newDialogId) {
//        try {
//            ContentValues values = new ContentValues();
//            values.put(KittyBeeContract.ChatMessage.GROUP_ID, newDialogId);
//            String where = SQLConstants.KEY_GROUP_ID + "=?";
//            String[] selectionArg = new String[]{oldDialogId};
//            DBQueryHandler queryHandler = new DBQueryHandler(context.getContentResolver());
//            queryHandler.startUpdate(0, 0, KittyBeeContract.ChatMessage.CONTENT_URI, values, where, selectionArg);
//        } catch (Exception e) {
//            AppLog.e(TAG, e.getMessage(), e);
//        }
//    }

    public int getLastMessageId() {
        Uri uri = getQueryUri();
        int maxId = 0;

        try {
            ContentResolver resolver = context.getContentResolver();
            Cursor cursor = resolver.query(
                    uri,                      // the URI to query
                    new String[]{KittyBeeContract.ChatMessage._ID},   // the projection to use
                    null,                           // the where clause without the WHERE keyword
                    null,                           // any wildcard substitutions
                    SQLConstants.KEY_ID + " DESC LIMIT 1");                          // the sort order without the SORT BY keyword

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
        return maxId;
    }

    public void loadDeletedMessageSet(final OnQueryHandlerListener listener) {
        Uri uri = getQueryUri();

        ContentResolver resolver = context.getContentResolver();
        final Cursor cursor = resolver.query(uri,                      // the URI to query
                new String[]{KittyBeeContract.ChatMessage.MESSAGE_ID},   // the projection to use
                "deleted = 1",                           // the where clause without the WHERE keyword
                null,                           // any wildcard substitutions
                null);

        final Set<String> messageId = new HashSet<>();
        BackgroundExecutorThread backgroundExecutorThread = new BackgroundExecutorThread();
        backgroundExecutorThread.execute(new Interactor() {
            @Override
            public void execute() {
                try {
                    if (cursor != null && cursor.getCount() > 0) {
                        // show data from db
                        while (cursor.moveToNext()) {
                            //TODO if get String grt group id than use get string 2
                            messageId.add(cursor.getString(0));
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
                listener.onResult(messageId);
            }
        });
    }

    public void loadPendingMessages(final OnQueryHandlerListener listener) {
        Uri uri = getQueryUri();

        ContentResolver resolver = context.getContentResolver();
        final Cursor cursor = resolver.query(uri,                      // the URI to query
                KittyBeeContract.ChatMessage.PROJECTION_ALL,   // the projection to use
                "deleted = 0",                           // the where clause without the WHERE keyword
                null,                           // any wildcard substitutions
                null);

        final ArrayList<QBChatMessage> messageList = new ArrayList<>();
        BackgroundExecutorThread backgroundExecutorThread = new BackgroundExecutorThread();
        backgroundExecutorThread.execute(new Interactor() {
            @Override
            public void execute() {
                try {
                    if (cursor != null && cursor.getCount() > 0) {

                        // show data from db
                        Gson gson = new Gson();
                        while (cursor.moveToNext()) {
                            //TODO if get String grt group id than use get string 2
                            messageList.add(gson.fromJson(cursor.getString(3), QBChatMessage.class));
                        }
                        cursor.close();

                    } else {
                        listener.onResult(null);
                    }
                    MySQLiteHelper.getInstance(context).close();
                } catch (Exception e) {

                }
            }
        }, new Interactor.OnExecutionFinishListener() {
            @Override
            public void onFinish() {
                listener.onResult(messageList);
            }
        });
    }
}
