package com.kittyapplication.sqlitedb;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

import com.kittyapplication.providers.KittyBeeContract;
import com.kittyapplication.rest.FinalWrapper;
import com.kittyapplication.services.OfflineSupportIntentService;
import com.kittyapplication.utils.AppLog;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Pintu Riontech on 8/9/16.
 * vaghela.pintu31@gmail.com
 */
public class MySQLiteHelper extends SQLiteOpenHelper {
    private AtomicInteger mDBCounter = new AtomicInteger();
    private SQLiteDatabase database;
    private static final Object LOCK = new Object();
    private static FinalWrapper<MySQLiteHelper> sInstance;
    private static final String TAG = MySQLiteHelper.class.getSimpleName();
    private Context mContext;

    public MySQLiteHelper(Context context) {
        super(context, SQLConstants.DATABASE_NAME, null, SQLConstants.DATABASE_VERSION);
        mContext = context;
    }

    public static synchronized MySQLiteHelper getInstance(Context context) {
        FinalWrapper<MySQLiteHelper> wrapper = sInstance;

        if (wrapper == null) {
            synchronized (LOCK) {
                if (sInstance == null) {
                    sInstance = new FinalWrapper<>(new MySQLiteHelper(context));
                }
                wrapper = sInstance;
            }
        }
        return wrapper.value;
    }

    public synchronized void open() {
        if (mDBCounter.incrementAndGet() == 1)
            database = getWritableDatabase();
    }

    public synchronized void close() {
        if (mDBCounter.decrementAndGet() == 0)
            database.close();
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        AppLog.d(TAG, "=========CREATE TABLE START ====================");
//        database.execSQL(getMessageCreateTableQuery(SQLConstants.TABLE_KITTIES, SQLConstants.KEY_ID,
//                SQLConstants.KEY_DATA, SQLConstants.KEY_TIMESTAMP, SQLConstants.KEY_IS_SYNC));
        try {

            database.execSQL(getCreateTableQuery(SQLConstants.TABLE_KITTY_NOTES, SQLConstants.KEY_ID,
                    SQLConstants.KEY_NOTE_ID, SQLConstants.KEY_DATA,
                    SQLConstants.KEY_TIMESTAMP, SQLConstants.KEY_IS_SYNC));

            database.execSQL(getCreateTableQuery(SQLConstants.TABLE_KITTY_PERSONAL_NOTES, SQLConstants.KEY_ID,
                    SQLConstants.KEY_NOTE_ID, SQLConstants.KEY_DATA,
                    SQLConstants.KEY_TIMESTAMP, SQLConstants.KEY_IS_SYNC));

            database.execSQL(getCreateTableQuery(SQLConstants.TABLE_PERSONAL_NOTES, SQLConstants.KEY_ID,
                    SQLConstants.KEY_NOTE_ID, SQLConstants.KEY_DATA,
                    SQLConstants.KEY_TIMESTAMP, SQLConstants.KEY_IS_SYNC));

            database.execSQL(getCreateTableQuery(SQLConstants.TABLE_KITTY_RULES, SQLConstants.KEY_ID,
                    SQLConstants.KEY_DATA, SQLConstants.KEY_TIMESTAMP
                    , SQLConstants.KEY_IS_SYNC));

            database.execSQL(getCreateTableQuery(SQLConstants.TABLE_DAIRY, SQLConstants.KEY_ID,
                    SQLConstants.KEY_DATA, SQLConstants.KEY_TIMESTAMP
                    , SQLConstants.KEY_IS_SYNC));

            database.execSQL(getCreateTableQuery(SQLConstants.TABLE_UPDATE_DAIRY, SQLConstants.KEY_ID,
                    SQLConstants.KEY_DATA, SQLConstants.KEY_TIMESTAMP
                    , SQLConstants.KEY_IS_SYNC));

            database.execSQL(getCreateTableQuery(SQLConstants.TABLE_PARTICIPANT, SQLConstants.KEY_ID,
                    SQLConstants.KEY_DATA, SQLConstants.KEY_TIMESTAMP,
                    SQLConstants.KEY_IS_SYNC));

            database.execSQL(getCreateTableQuery(SQLConstants.TABLE_ATTENDANCE, SQLConstants.KEY_GROUP_ID,
                    SQLConstants.KEY_KITTY_ID, SQLConstants.KEY_DATA,
                    SQLConstants.KEY_TIMESTAMP, SQLConstants.KEY_IS_SYNC));

            database.execSQL(getCreateTableQuery(SQLConstants.TABLE_BILL, SQLConstants.KEY_GROUP_ID,
                    SQLConstants.KEY_KITTY_ID, SQLConstants.KEY_DATA,
                    SQLConstants.KEY_TIMESTAMP, SQLConstants.KEY_IS_SYNC));

            database.execSQL(getCreateTableQuery(SQLConstants.TABLE_SUMMARY, SQLConstants.KEY_ID,
                    SQLConstants.KEY_DATA, SQLConstants.KEY_TIMESTAMP
                    , SQLConstants.KEY_IS_SYNC));

            database.execSQL(getCreateTableQuery(SQLConstants.TABLE_VENUE, SQLConstants.KEY_ID,
                    SQLConstants.KEY_DATA, SQLConstants.KEY_TIMESTAMP
                    , SQLConstants.KEY_IS_SYNC));

            database.execSQL(getCreateTableQuery(SQLConstants.TABLE_SETTING, SQLConstants.KEY_ID,
                    SQLConstants.KEY_DATA, SQLConstants.KEY_TIMESTAMP
                    , SQLConstants.KEY_IS_SYNC));

            /**************************** TABLE CREATED BY MITHUN *******************************/

            database.execSQL(getKittyCreateTableQuery());

            database.execSQL(getChatGroupMemberCreateTableQuery(SQLConstants.TABLE_GROUP_CHAT_MEMBER,
                    KittyBeeContract.GroupChatMember._ID,
                    KittyBeeContract.GroupChatMember.MEMBER_ID,
                    KittyBeeContract.GroupChatMember.MEMBER_NAME,
                    KittyBeeContract.GroupChatMember.MEMBER_NUMBER,
                    KittyBeeContract.GroupChatMember.MEMBER_IMAGE,
                    KittyBeeContract.GroupChatMember.TIMESTAMP));

            database.execSQL(getMessageCreateTableQuery());


//            database.execSQL(getCreateGroupTableQuery(
//                    SQLConstants.TABLE_GROUP,
//                    KittyBeeContract.Groups._ID,
//                    KittyBeeContract.Groups.GROUP_ID,
//                    KittyBeeContract.Groups.DATA,
//                    KittyBeeContract.Groups.DELETED,
//                    KittyBeeContract.Groups.UPDATED,
//                    SQLConstants.KEY_TIMESTAMP));
//
//            database.execSQL(getCreateTableQuery(SQLConstants.TABLE_QB_DIALOGS, SQLConstants.KEY_ID,
//                    SQLConstants.KEY_DATA, SQLConstants.KEY_TIMESTAMP, SQLConstants.KEY_IS_SYNC));

        } catch (SQLException e) {
            System.out.println("MySQLiteHelper$onCreate error ::>" + e.getMessage());
            AppLog.e(TAG, e.getMessage(), e);
        }

        AppLog.d(TAG, "=========CREATE TABLE END ====================");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {


        if (oldVersion == 3) {
            //upgrade from version 3 to 4
            db.execSQL(getDropQuery(SQLConstants.TABLE_GROUP));
            db.execSQL(getDropQuery(SQLConstants.TABLE_QB_DIALOGS));
            db.execSQL(getDropQuery(SQLConstants.TABLE_KITTIES));
            db.execSQL(getDropQuery(SQLConstants.TABLE_KITTY_NOTES));
            db.execSQL(getDropQuery(SQLConstants.TABLE_KITTY_PERSONAL_NOTES));
            db.execSQL(getDropQuery(SQLConstants.TABLE_PERSONAL_NOTES));
            db.execSQL(getDropQuery(SQLConstants.TABLE_DAIRY));
            db.execSQL(getDropQuery(SQLConstants.TABLE_UPDATE_DAIRY));
            db.execSQL(getDropQuery(SQLConstants.TABLE_PARTICIPANT));
            db.execSQL(getDropQuery(SQLConstants.TABLE_ATTENDANCE));
            db.execSQL(getDropQuery(SQLConstants.TABLE_BILL));
            db.execSQL(getDropQuery(SQLConstants.TABLE_SUMMARY));
            db.execSQL(getDropQuery(SQLConstants.TABLE_VENUE));
            db.execSQL(getDropQuery(SQLConstants.TABLE_SETTING));
            db.execSQL(getDropQuery(SQLConstants.TABLE_GROUP_CHAT_MEMBER));
            onCreate(db);
            mContext.startService(new Intent(mContext, OfflineSupportIntentService.class));
        } else {
            onCreate(db);
        }


    }

    /**
     * @param values
     */
    public long insertDiary(ContentValues values) {
        long inserted = -1;
        try {
            open();
            String tableName = SQLConstants.TABLE_DAIRY;
            String groupId = values.getAsString(SQLConstants.KEY_ID);
            if (!checkDataAvailableInDb(tableName,
                    SQLConstants.KEY_ID, groupId)) {
                inserted = database.insert(tableName, null, values);
                AppLog.e(TAG, "---- inserted = " + inserted);
            } else {
                String selection = SQLConstants.KEY_ID + "=?";
                inserted = database.update(tableName, values, selection,
                        new String[]{groupId});
                AppLog.e(TAG, "---- updated = " + inserted);
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        } finally {
            try {
                close();
            } catch (Exception e) {
            }
        }
        return inserted;
    }

    /**
     * @param values
     */
    public long insertGroups(ContentValues values) {
        long inserted = -1;
        try {
            open();
            String tableName = SQLConstants.TABLE_GROUP;
            String groupId = values.getAsString(SQLConstants.KEY_ID);
            if (!checkDataAvailableInDb(tableName,
                    SQLConstants.KEY_ID, groupId)) {
                inserted = database.insert(tableName, null, values);
                AppLog.e(TAG, "---- inserted = " + inserted);
            } else {
                String selection = SQLConstants.KEY_ID + "=?";
                inserted = database.update(tableName, values, selection,
                        new String[]{groupId});
                AppLog.e(TAG, "---- updated = " + inserted);
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        } finally {
            try {
                close();
            } catch (Exception e) {
            }
        }
        return inserted;
    }

    /**
     * @param values
     */
    public long insertSummary(ContentValues values) {
        long inserted = -1;
        try {
            open();
            String tableName = SQLConstants.TABLE_SUMMARY;
            String groupId = values.getAsString(SQLConstants.KEY_ID);
            if (!checkDataAvailableInDb(tableName,
                    SQLConstants.KEY_ID, groupId)) {
                inserted = database.insert(tableName, null, values);
                AppLog.e(TAG, "---- inserted = " + inserted);
            } else {
                String selection = SQLConstants.KEY_ID + "=?";
                inserted = database.update(tableName, values, selection,
                        new String[]{groupId});
                AppLog.e(TAG, "---- updated = " + inserted);
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        } finally {
            try {
                close();
            } catch (Exception e) {
            }
        }
        return inserted;
    }

    /**
     * Insert Bill
     *
     * @param values
     */
    public long insertBill(ContentValues values) {
        long inserted = -1;
        try {
            open();
            String tableName = SQLConstants.TABLE_BILL;
            String groupId = values.getAsString(SQLConstants.KEY_GROUP_ID);
            String kittyId = values.getAsString(SQLConstants.KEY_KITTY_ID);
            String[] selectionArgs = {groupId, kittyId};
            if (!checkDataAvailableInDb(tableName,
                    SQLConstants.KEY_GROUP_ID, SQLConstants.KEY_KITTY_ID, selectionArgs)) {
                inserted = database.insert(tableName, null, values);
                AppLog.e(TAG, "---- inserted = " + inserted);
            } else {
                String selection = SQLConstants.KEY_GROUP_ID + "=? AND "
                        + SQLConstants.KEY_KITTY_ID + "=?";
                inserted = database.update(tableName, values, selection,
                        selectionArgs);
                AppLog.e(TAG, "---- updated = " + inserted);
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        } finally {
            try {
                close();
            } catch (Exception e) {
            }
        }
        return inserted;
    }

    /**
     * Insert Bill
     *
     * @param values
     */
    public long insertAttendance(ContentValues values) {
        long inserted = -1;
        try {
            open();
            String tableName = SQLConstants.TABLE_ATTENDANCE;
            String groupId = values.getAsString(SQLConstants.KEY_GROUP_ID);
            String kittyId = values.getAsString(SQLConstants.KEY_KITTY_ID);
            String[] selectionArgs = {groupId, kittyId};
            if (!checkDataAvailableInDb(tableName,
                    SQLConstants.KEY_GROUP_ID, SQLConstants.KEY_KITTY_ID, selectionArgs)) {
                inserted = database.insert(tableName, null, values);
                AppLog.e(TAG, "---- inserted = " + inserted);
            } else {
                String selection = SQLConstants.KEY_GROUP_ID + "=? AND "
                        + SQLConstants.KEY_KITTY_ID + "=?";
                inserted = database.update(tableName, values, selection,
                        selectionArgs);
                AppLog.e(TAG, "---- updated = " + inserted);
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        } finally {
            try {
                close();
            } catch (Exception e) {
            }
        }
        return inserted;
    }

    /**
     * @return
     */
    public Cursor getQuery(SQLiteQueryBuilder builder, String[] projection, String selection,
                           String[] selectionArgs, String sortOrder) {
        open();
        Cursor cursor = builder.query(database,
                projection, selection, selectionArgs, null, null, sortOrder);
        return cursor;
    }

    /**
     * @return
     */
    public Cursor getQuery(String tableName, String[] projection, String selection,
                           String[] selectionArgs, String sortOrder) {
        open();
        Cursor cursor = database.query(tableName,
                projection, selection, selectionArgs, null, null, sortOrder);
        return cursor;
    }

    /**
     * Create table query
     *
     * @return query for creating table
     */
    public String getCreateGroupTableQuery(String tableName, String id, String groupId,
                                           String data, String deleted, String updated, String timeStamp) {
        String query = String.format("CREATE TABLE %s (" +
                        " %s INTEGER PRIMARY KEY AUTOINCREMENT," +
                        " %s TEXT, %s TEXT, %s INTEGER DEFAULT 0," +
                        " %s INTEGER DEFAULT 0, %s TIMESTAMP DEFAULT CURRENT_TIMESTAMP )",
                tableName, id, groupId, data, deleted, updated, timeStamp);
        return query;
    }

    /**
     * Create table query
     *
     * @return query for creating table
     */
    public String getKittyCreateTableQuery() {
        String query = "CREATE TABLE " + SQLConstants.TABLE_KITTIES + " ( " +
                KittyBeeContract.Kitties._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KittyBeeContract.Kitties.GROUP_ID + " TEXT," +
                KittyBeeContract.Kitties.QB_DIALOG_ID + " TEXT," +
                KittyBeeContract.Kitties.GROUP + " TEXT," +
                KittyBeeContract.Kitties.QB_DIALOG + " TEXT," +
                KittyBeeContract.Kitties.CLEAR_MESSAGE + " INTEGER DEFAULT 0, " +
                KittyBeeContract.Kitties.DELETED + " INTEGER DEFAULT 0, " +
                KittyBeeContract.Kitties.QB_LAST_MSG_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                KittyBeeContract.Kitties.TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP )";
        return query;
    }

    /**
     * Create table query
     *
     * @return query for creating table
     */
    public String getCreateTableQuery(String tableName, String id, String data, String timeStamp, String isSync) {
        String query = String.format("CREATE TABLE %s (%s TEXT, %s TEXT, %s TIMESTAMP DEFAULT CURRENT_TIMESTAMP , "
                        + " %s INTEGER DEFAULT 0 " + ")",
                tableName, id, data, timeStamp, isSync);
        return query;
    }

    /**
     * Create table query
     *
     * @return query for creating table
     */
    public String getCreateTableQuery(String tableName, String id, String kittyId, String data, String timeStamp, String isSync) {
        String query = String.format("CREATE TABLE %s (%s TEXT, %s TEXT, %s TEXT, " +
                        "%s TIMESTAMP DEFAULT CURRENT_TIMESTAMP , " + " %s INTEGER DEFAULT 0 " + ")",
                tableName, id, kittyId, data, timeStamp, isSync);
        return query;
    }

    /**
     * Create table query
     *
     * @return query for creating table
     */
    public String getMessageCreateTableQuery() {

        String query = "CREATE TABLE " + SQLConstants.TABLE_MESSAGE + " ( " +
                KittyBeeContract.ChatMessage._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KittyBeeContract.ChatMessage.KITTY_ID + " INTEGER, " +
                KittyBeeContract.ChatMessage.MESSAGE_ID + " TEXT," +
                KittyBeeContract.ChatMessage.DATA + " TEXT," +
                KittyBeeContract.ChatMessage.SENT + " INTEGER DEFAULT 0," +
                KittyBeeContract.ChatMessage.READ + " INTEGER DEFAULT 0," +
                KittyBeeContract.ChatMessage.DELIVERED + " INTEGER DEFAULT 0," +
                KittyBeeContract.ChatMessage.DELETED + " INTEGER DEFAULT 0, " +
                KittyBeeContract.ChatMessage.TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                " FOREIGN KEY(" + KittyBeeContract.ChatMessage.KITTY_ID + ")" +
                " REFERENCES " + SQLConstants.TABLE_KITTIES + "(" + KittyBeeContract.Kitties._ID + ")" +
                " ON DELETE CASCADE )";
        return query;
    }

    /**
     * Create table query
     *
     * @return query for creating table
     */
    public String getChatGroupMemberCreateTableQuery(String tableName, String id,
                                                     String memberId, String memberName, String memberNumber,
                                                     String image, String timeStamp) {
        String query = String.format("CREATE TABLE %s ("
                        + " %s INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + " %s INTEGER, %s TEXT, %s TEXT, %s BLOB,"
                        + " %s TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",
                tableName, id, memberId, memberName, memberNumber, image, timeStamp);
        AppLog.d(TAG, query);
        return query;
    }

    /**
     * for check data is available in database or not
     *
     * @param tableName for find data in which table
     * @param key       for which key to identify
     * @param value     for which value identify with key
     * @return true(means available) or false( means not available)
     */
    public boolean checkDataAvailableInDb(String tableName, String key, String value) {
        boolean result = false;
        try {
            if (value == null)
                return false;
            String selection = key + "=?";
            Cursor cursor = database.query
                    (tableName, null, selection, new String[]{value}, null, null, null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    cursor.close();
                    result = true;
                } else {
                    result = false;
                }
            } else {
                result = false;
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
        return result;
    }

    /**
     * for check data is available in database or not
     *
     * @param tableName for find data in which table
     * @param key       for which key to identify
     * @param value     for which value identify with key
     * @return true(means available) or false( means not available)
     */
    public boolean checkDataAvailableInDb(String tableName, String key, String key2, String[] value) {
        boolean result = false;
        try {
            String selection = key + "=? AND " + key2 + "=?";
            Cursor cursor = database.query
                    (tableName, null, selection, value, null, null, null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    cursor.close();
                    result = true;
                } else {
                    result = false;
                }
            } else {
                result = false;
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
        return result;
    }


    /**
     * @param values
     */
    public long insertVenue(ContentValues values) {
        long inserted = -1;
        try {
            open();
            String tableName = SQLConstants.TABLE_VENUE;
            String groupId = values.getAsString(SQLConstants.KEY_ID);
            if (!checkDataAvailableInDb(tableName,
                    SQLConstants.KEY_ID, groupId)) {
                inserted = database.insert(tableName, null, values);
                AppLog.e(TAG, "---- inserted = " + inserted);
            } else {
                String selection = SQLConstants.KEY_ID + "=?";
                inserted = database.update(tableName, values, selection,
                        new String[]{groupId});
                AppLog.e(TAG, "---- updated = " + inserted);
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        } finally {
            try {
                close();
            } catch (Exception e) {
            }
        }
        return inserted;
    }

    /**
     * @param values
     */
    public long insertKittyNotes(ContentValues values) {
        long inserted = -1;
        try {
            open();
            String tableName = SQLConstants.TABLE_KITTY_NOTES;
            String groupId = values.getAsString(SQLConstants.KEY_NOTE_ID);
            if (!checkDataAvailableInDb(tableName,
                    SQLConstants.KEY_NOTE_ID, groupId)) {
                inserted = database.insert(tableName, null, values);
                AppLog.e(TAG, "---- inserted = " + inserted);
            } else {
                String selection = SQLConstants.KEY_NOTE_ID + "=?";
                inserted = database.update(tableName, values, selection,
                        new String[]{groupId});
                AppLog.e(TAG, "---- updated = " + inserted);
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        } finally {
            try {
                close();
            } catch (Exception e) {
            }
        }
        return inserted;
    }

    /**
     * @param values
     */
    public long insertKittyPersonalNotes(ContentValues values) {
        long inserted = -1;
        try {
            open();
            String tableName = SQLConstants.TABLE_KITTY_PERSONAL_NOTES;
            String groupId = values.getAsString(SQLConstants.KEY_NOTE_ID);
            if (!checkDataAvailableInDb(tableName,
                    SQLConstants.KEY_NOTE_ID, groupId)) {
                inserted = database.insert(tableName, null, values);
                AppLog.e(TAG, "---- inserted = " + inserted);
            } else {
                String selection = SQLConstants.KEY_NOTE_ID + "=?";
                inserted = database.update(tableName, values, selection,
                        new String[]{groupId});
                AppLog.e(TAG, "---- updated = " + inserted);
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        } finally {
            try {
                close();
            } catch (Exception e) {
            }
        }
        return inserted;
    }

    /**
     * @param values
     */
    public long insertPersonalNotes(ContentValues values) {
        long inserted = -1;
        try {
            open();
            String tableName = SQLConstants.TABLE_PERSONAL_NOTES;
            String groupId = values.getAsString(SQLConstants.KEY_NOTE_ID);
            if (!checkDataAvailableInDb(tableName,
                    SQLConstants.KEY_NOTE_ID, groupId)) {
                inserted = database.insert(tableName, null, values);
                AppLog.e(TAG, "---- inserted = " + inserted);
            } else {
                String selection = SQLConstants.KEY_NOTE_ID + "=?";
                inserted = database.update(tableName, values, selection,
                        new String[]{groupId});
                AppLog.e(TAG, "---- updated = " + inserted);
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        } finally {
            try {
                close();
            } catch (Exception e) {
            }
        }
        return inserted;
    }

    /**
     * @param values
     */
    public long insertRules(ContentValues values) {
        long inserted = -1;
        try {
            open();
            String tableName = SQLConstants.TABLE_KITTY_RULES;
            String groupId = values.getAsString(SQLConstants.KEY_ID);
            if (!checkDataAvailableInDb(tableName,
                    SQLConstants.KEY_ID, groupId)) {
                inserted = database.insert(tableName, null, values);
                AppLog.e(TAG, "---- inserted = " + inserted);
            } else {
                String selection = SQLConstants.KEY_ID + "=?";
                inserted = database.update(tableName, values, selection,
                        new String[]{groupId});
                AppLog.e(TAG, "---- updated = " + inserted);
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        } finally {
            try {
                close();
            } catch (Exception e) {
            }
        }
        return inserted;
    }

    /**
     * @param values
     */
    public long insertUpdateDiary(ContentValues values) {
        long inserted = -1;
        try {
            open();
            String tableName = SQLConstants.TABLE_UPDATE_DAIRY;
            String groupId = values.getAsString(SQLConstants.KEY_ID);
            if (!checkDataAvailableInDb(tableName,
                    SQLConstants.KEY_ID, groupId)) {
                inserted = database.insert(tableName, null, values);
                AppLog.e(TAG, "---- inserted = " + inserted);
            } else {
                String selection = SQLConstants.KEY_ID + "=?";
                inserted = database.update(tableName, values, selection,
                        new String[]{groupId});
                AppLog.e(TAG, "---- updated = " + inserted);
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        } finally {
            try {
                close();
            } catch (Exception e) {
            }
        }
        return inserted;
    }


    /**
     * @param values
     */
    public long insertSetting(ContentValues values) {
        long inserted = -1;
        try {
            open();
            String tableName = SQLConstants.TABLE_SETTING;
            String groupId = values.getAsString(SQLConstants.KEY_ID);
            if (!checkDataAvailableInDb(tableName,
                    SQLConstants.KEY_ID, groupId)) {
                inserted = database.insert(tableName, null, values);
                AppLog.e(TAG, "---- inserted = " + inserted);
            } else {
                String selection = SQLConstants.KEY_ID + "=?";
                inserted = database.update(tableName, values, selection,
                        new String[]{groupId});
                AppLog.e(TAG, "---- updated = " + inserted);
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        } finally {
            try {
                close();
            } catch (Exception e) {
            }
        }
        return inserted;
    }


    public void deleteItemFromDb(String tableName, String whereClause, String whereArgs) {
        open();
        String selection = whereClause + "= ?";
        database.delete(tableName, selection, new String[]{whereArgs});
        close();
    }

    public void updateParticularFiled(String tableName, String whereClause, String whereArgs, ContentValues values) {
        open();
        String selection = whereClause + "= ?";
        database.update(tableName, values, selection, new String[]{whereArgs});
        close();
    }

    /**
     * @param values
     */
    public long insertQbDialogs(ContentValues values) {
        AppLog.e(TAG, "##### Inside QBDIALOG #####");
        long inserted = -1;
        try {
            open();
            String tableName = SQLConstants.TABLE_QB_DIALOGS;
            String groupId = values.getAsString(SQLConstants.KEY_ID);

            if (groupId != null) {
                if (!checkDataAvailableInDb(tableName,
                        SQLConstants.KEY_ID, groupId)) {
                    inserted = database.insert(tableName, null, values);
                    AppLog.e(TAG, "Inserted = " + inserted);
                } else {
                    String selection = SQLConstants.KEY_ID + "=?";
                    inserted = database.update(tableName, values, selection,
                            new String[]{groupId});
                    AppLog.e(TAG, "Updated = " + inserted);
                }
            } else {
                AppLog.e(TAG, "Group ID is NULL.");
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        } finally {
            try {
                close();
            } catch (Exception e) {
            }
            AppLog.e(TAG, "##### Inside QBDIALOG END #####");
        }
        return inserted;
    }

    /**
     * @param values
     */
    public long insertKitties(ContentValues values) {
        AppLog.e(TAG, "##### Inside KITTIES #####");

        long inserted = -1;
        try {
            open();
            String tableName = SQLConstants.TABLE_KITTIES;
            String key = KittyBeeContract.Kitties.QB_DIALOG_ID;
            String dialogId = values.getAsString(key);

            if (dialogId != null) {
                if (!checkDataAvailableInDb(tableName, key, dialogId)) {
                    inserted = database.insert(tableName, null, values);
                    AppLog.e(TAG, "Inserted = " + inserted);
                } else {
                    String selection = key + "=?";
                    inserted = database.update(tableName, values, selection, new String[]{dialogId});
                    AppLog.e(TAG, "Updated = " + inserted);
                }
            } else {
                AppLog.e(TAG, "Group ID is NULL.");
            }

        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        } finally {
            try {
                close();
            } catch (Exception e) {
            }
            AppLog.e(TAG, "##### Inside KITTIES END #####");
        }
        return inserted;
    }

    /**
     * @param values
     */
    public long insertMessage(ContentValues values) {
        long inserted = -1;
        try {
            open();
            String tableName = SQLConstants.TABLE_MESSAGE;
            String chatId = KittyBeeContract.ChatMessage.MESSAGE_ID;
            String value = values.getAsString(chatId);
            if (value != null && checkDataAvailableInDb(tableName, chatId, value)) {
                String selection = chatId + "=?";
                inserted = database.update(tableName, values, selection, new String[]{value});
                AppLog.e(TAG, "---- Updated = " + inserted);
            } else {
                inserted = database.insert(tableName, null, values);
                AppLog.e(TAG, "---- inserted = " + inserted);
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        } finally {
            close();
        }
        return inserted;
    }

    /**
     * @param values
     */
    public long insertGroupChatMember(ContentValues values) {
        long inserted = -1;
        try {
            open();
            AppLog.e(TAG, "---- insertGroupChatMember = " + values.toString());
            String tableName = SQLConstants.TABLE_GROUP_CHAT_MEMBER;
            String memberId = KittyBeeContract.GroupChatMember.MEMBER_ID;
            String value = values.getAsString(memberId);
            if (value != null && checkDataAvailableInDb(tableName, memberId, value)) {
                String selection = memberId + "=?";
                inserted = database.update(tableName, values, selection, new String[]{value});
                AppLog.e(TAG, "---- Updated = " + inserted);
            } else {
                inserted = database.insert(tableName, null, values);
                AppLog.e(TAG, "---- inserted = " + inserted);
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        } finally {
            close();
        }
        return inserted;
    }

    private String getDropQuery(String tableName) {
        return "DROP TABLE IF EXISTS '" + tableName + "'";
    }

}