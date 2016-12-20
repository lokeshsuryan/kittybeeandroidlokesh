package com.kittyapplication.providers;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.kittyapplication.sqlitedb.MySQLiteHelper;
import com.kittyapplication.sqlitedb.SQLConstants;
import com.kittyapplication.utils.AppLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Scorpion on 17-09-2016.
 */
public class KittyBeeContentProvider extends ContentProvider {

    private static final String TAG = KittyBeeContentProvider.class.getSimpleName();
    private final ThreadLocal<Boolean> mIsInBatchMode = new ThreadLocal<>();
    private MySQLiteHelper kittyBeeSqlOperation;

    // helper constants for use with the UriMatcher
    public static final int GROUP_LIST = 1;
    public static final int GROUP_ID = 2;

    public static final int DIARY_LIST = 5;
    public static final int DIARY_ID = 6;

    public static final int PARTICIPANT_LIST = 10;
    public static final int PARTICIPANT_ID = 11;

    public static final int BILL_LIST = 15;
    public static final int BILL_ID = 16;

    public static final int KITTY_NOTES_LIST = 19;
    public static final int KITTY_NOTES_ID = 20;

    public static final int KITTY_PERSONAL_LIST = 23;
    public static final int KITTY_PERSONAL_ID = 24;

    public static final int PERSONAL_LIST = 28;
    public static final int PERSONAL_ID = 29;

    public static final int ATTENDANCE_LIST = 31;
    public static final int ATTENDANCE_ID = 32;

    public static final int DIARY_UPDATE_LIST = 34;
    public static final int DIARY_UPDATE_ID = 35;

    public static final int RULES_LIST = 38;
    public static final int RULES_ID = 39;

    public static final int SUMMARY_LIST = 41;
    public static final int SUMMARY_ID = 42;

    public static final int VENUE_LIST = 44;
    public static final int VENUE_ID = 45;

    public static final int SETTING_LIST = 47;
    public static final int SETTING_ID = 48;

    public static final int CHAT_MESSAGE_LIST = 49;
    public static final int CHAT_MESSAGE_ID = 50;

    public static final int QBDIALOG_LIST = 51;
    public static final int QBDIALOG_ID = 52;

    public static final int GROUP_CHAT_MEMBER_LIST = 53;
    public static final int GROUP_CHAT_MEMBER_ID = 54;

    public static final int KITTIES_LIST = 55;
    public static final int KITTIES_ID = 56;

    public static final UriMatcher URI_MATCHER;

    // Uri Matcher for the content provider
    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(KittyBeeContract.AUTHORITY, "groups", GROUP_LIST);
        URI_MATCHER.addURI(KittyBeeContract.AUTHORITY, "groups/#", GROUP_ID);
        URI_MATCHER.addURI(KittyBeeContract.AUTHORITY, "diaries", DIARY_LIST);
        URI_MATCHER.addURI(KittyBeeContract.AUTHORITY, "diaries/#", DIARY_ID);
        URI_MATCHER.addURI(KittyBeeContract.AUTHORITY, "diariesupdate", DIARY_UPDATE_LIST);
        URI_MATCHER.addURI(KittyBeeContract.AUTHORITY, "diariesupdate/#", DIARY_UPDATE_ID);
        URI_MATCHER.addURI(KittyBeeContract.AUTHORITY, "participants", PARTICIPANT_LIST);
        URI_MATCHER.addURI(KittyBeeContract.AUTHORITY, "participants/#", PARTICIPANT_ID);
        URI_MATCHER.addURI(KittyBeeContract.AUTHORITY, "bills", BILL_LIST);
        URI_MATCHER.addURI(KittyBeeContract.AUTHORITY, "bills/#", BILL_ID);
        URI_MATCHER.addURI(KittyBeeContract.AUTHORITY, "kittynotes", KITTY_NOTES_LIST);
        URI_MATCHER.addURI(KittyBeeContract.AUTHORITY, "kittynotes/#", KITTY_NOTES_ID);
        URI_MATCHER.addURI(KittyBeeContract.AUTHORITY, "kittypersonalnotes", KITTY_PERSONAL_LIST);
        URI_MATCHER.addURI(KittyBeeContract.AUTHORITY, "kittypersonalnotes/#", KITTY_PERSONAL_ID);
        URI_MATCHER.addURI(KittyBeeContract.AUTHORITY, "personalnotes", PERSONAL_LIST);
        URI_MATCHER.addURI(KittyBeeContract.AUTHORITY, "personalnotes/#", PERSONAL_ID);
        URI_MATCHER.addURI(KittyBeeContract.AUTHORITY, "attendance", ATTENDANCE_LIST);
        URI_MATCHER.addURI(KittyBeeContract.AUTHORITY, "attendance/#", ATTENDANCE_ID);
        URI_MATCHER.addURI(KittyBeeContract.AUTHORITY, "rules", RULES_LIST);
        URI_MATCHER.addURI(KittyBeeContract.AUTHORITY, "rules/#", RULES_ID);
        URI_MATCHER.addURI(KittyBeeContract.AUTHORITY, "summary", SUMMARY_LIST);
        URI_MATCHER.addURI(KittyBeeContract.AUTHORITY, "summary/#", SUMMARY_ID);
        URI_MATCHER.addURI(KittyBeeContract.AUTHORITY, "venue", VENUE_LIST);
        URI_MATCHER.addURI(KittyBeeContract.AUTHORITY, "venue/#", VENUE_ID);
        URI_MATCHER.addURI(KittyBeeContract.AUTHORITY, "settings", SETTING_LIST);
        URI_MATCHER.addURI(KittyBeeContract.AUTHORITY, "settings/#", SETTING_ID);
        URI_MATCHER.addURI(KittyBeeContract.AUTHORITY, "qbdialogs", QBDIALOG_LIST);
        URI_MATCHER.addURI(KittyBeeContract.AUTHORITY, "qbdialogs/#", QBDIALOG_ID);
        URI_MATCHER.addURI(KittyBeeContract.AUTHORITY, KittyBeeContract.ChatMessage.PATH, CHAT_MESSAGE_LIST);
        URI_MATCHER.addURI(KittyBeeContract.AUTHORITY, KittyBeeContract.ChatMessage.PATH + "/#", CHAT_MESSAGE_ID);
        URI_MATCHER.addURI(KittyBeeContract.AUTHORITY, KittyBeeContract.GroupChatMember.PATH, GROUP_CHAT_MEMBER_LIST);
        URI_MATCHER.addURI(KittyBeeContract.AUTHORITY, KittyBeeContract.GroupChatMember.PATH + "/#", GROUP_CHAT_MEMBER_ID);
        URI_MATCHER.addURI(KittyBeeContract.AUTHORITY, KittyBeeContract.Kitties.PATH, KITTIES_LIST);
        URI_MATCHER.addURI(KittyBeeContract.AUTHORITY, KittyBeeContract.Kitties.PATH + "/#", KITTIES_ID);
    }

    @Override
    public boolean onCreate() {
        kittyBeeSqlOperation =
                MySQLiteHelper.getInstance(getContext());
        return true;
    }

    @NonNull
    @Override
    public synchronized ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        AppLog.e(TAG, "##### Inside applyBatch #####");
        mIsInBatchMode.set(true);
        try {
            final ContentProviderResult[] retResult = super.applyBatch(operations);
            return retResult;
        } finally {
            mIsInBatchMode.remove();
        }
    }

    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case GROUP_LIST:
                return KittyBeeContract.Groups.CONTENT_TYPE;
            case GROUP_ID:
                return KittyBeeContract.Groups.CONTENT_ITEM_TYPE;
            case RULES_LIST:
                return KittyBeeContract.Rules.CONTENT_TYPE;
            case RULES_ID:
                return KittyBeeContract.Rules.CONTENT_ITEM_TYPE;
            case DIARY_LIST:
                return KittyBeeContract.Diaries.CONTENT_TYPE;
            case DIARY_ID:
                return KittyBeeContract.Diaries.CONTENT_ITEM_TYPE;
            case DIARY_UPDATE_LIST:
                return KittyBeeContract.UpdateDiary.CONTENT_TYPE;
            case DIARY_UPDATE_ID:
                return KittyBeeContract.UpdateDiary.CONTENT_ITEM_TYPE;
            case BILL_LIST:
                return KittyBeeContract.Bill.CONTENT_TYPE;
            case BILL_ID:
                return KittyBeeContract.Bill.CONTENT_ITEM_TYPE;
            case KITTY_NOTES_LIST:
                return KittyBeeContract.KittyNotes.CONTENT_TYPE;
            case KITTY_NOTES_ID:
                return KittyBeeContract.KittyNotes.CONTENT_ITEM_TYPE;
            case KITTY_PERSONAL_LIST:
                return KittyBeeContract.KittyPersonalNote.CONTENT_TYPE;
            case KITTY_PERSONAL_ID:
                return KittyBeeContract.KittyPersonalNote.CONTENT_ITEM_TYPE;
            case PERSONAL_LIST:
                return KittyBeeContract.PersonalNote.CONTENT_TYPE;
            case PERSONAL_ID:
                return KittyBeeContract.PersonalNote.CONTENT_ITEM_TYPE;
            case ATTENDANCE_LIST:
                return KittyBeeContract.Attendance.CONTENT_TYPE;
            case ATTENDANCE_ID:
                return KittyBeeContract.Attendance.CONTENT_ITEM_TYPE;
            case SUMMARY_LIST:
                return KittyBeeContract.Summary.CONTENT_TYPE;
            case SUMMARY_ID:
                return KittyBeeContract.Summary.CONTENT_ITEM_TYPE;
            case VENUE_LIST:
                return KittyBeeContract.Venue.CONTENT_TYPE;
            case VENUE_ID:
                return KittyBeeContract.Venue.CONTENT_ITEM_TYPE;
            case SETTING_LIST:
                return KittyBeeContract.Setting.CONTENT_TYPE;
            case SETTING_ID:
                return KittyBeeContract.Setting.CONTENT_ITEM_TYPE;
            case QBDIALOG_LIST:
                return KittyBeeContract.QBDialog.CONTENT_TYPE;
            case QBDIALOG_ID:
                return KittyBeeContract.QBDialog.CONTENT_ITEM_TYPE;
            case KITTIES_LIST:
                return KittyBeeContract.Kitties.CONTENT_TYPE;
            case KITTIES_ID:
                return KittyBeeContract.Kitties.CONTENT_ITEM_TYPE;
            case CHAT_MESSAGE_LIST:
                return KittyBeeContract.QBDialog.CONTENT_TYPE;
            case CHAT_MESSAGE_ID:
                return KittyBeeContract.QBDialog.CONTENT_ITEM_TYPE;
            case GROUP_CHAT_MEMBER_LIST:
                return KittyBeeContract.QBDialog.CONTENT_TYPE;
            case GROUP_CHAT_MEMBER_ID:
                return KittyBeeContract.QBDialog.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        AppLog.e(TAG, "##### Inside query #####");
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        boolean useAuthorityUri = false;
        boolean flag = false;

        final int match = URI_MATCHER.match(uri);
        int id = 0;
        switch (match) {
            case GROUP_ID:
                id = (int) ContentUris.parseId(uri);
                builder.setTables(SQLConstants.TABLE_GROUP);
                builder.appendWhere(SQLConstants.KEY_ID + "=" + id);
                break;

            case GROUP_LIST:
                builder.setTables(SQLConstants.TABLE_GROUP);
                break;

            case DIARY_ID:
                id = (int) ContentUris.parseId(uri);
                builder.setTables(SQLConstants.TABLE_DAIRY);
                builder.appendWhere(SQLConstants.KEY_ID + "=" + id);
                break;

            case DIARY_LIST:
                builder.setTables(SQLConstants.TABLE_DAIRY);
                break;

            case SUMMARY_LIST:
                builder.setTables(SQLConstants.TABLE_SUMMARY);
                break;

            case SUMMARY_ID:
                id = (int) ContentUris.parseId(uri);
                builder.setTables(SQLConstants.TABLE_SUMMARY);
                builder.appendWhere(SQLConstants.KEY_ID + "=" + id);
                break;

            case ATTENDANCE_LIST:
                flag = true;
                builder.setTables(SQLConstants.TABLE_ATTENDANCE);
                break;

            case ATTENDANCE_ID:
                flag = true;
                id = (int) ContentUris.parseId(uri);
                builder.setTables(SQLConstants.TABLE_ATTENDANCE);
                builder.appendWhere(SQLConstants.KEY_GROUP_ID + "=" + id);
                break;

            case BILL_LIST:
                flag = true;
                builder.setTables(SQLConstants.TABLE_BILL);
                break;

            case BILL_ID:
                flag = true;
                builder.setTables(SQLConstants.TABLE_BILL);
                break;

            case VENUE_ID:
                id = (int) ContentUris.parseId(uri);
                builder.setTables(SQLConstants.TABLE_VENUE);
                builder.appendWhere(SQLConstants.KEY_ID + "=" + id);
                break;

            case VENUE_LIST:
                builder.setTables(SQLConstants.TABLE_VENUE);
                break;

            case PERSONAL_ID:
                id = (int) ContentUris.parseId(uri);
                builder.setTables(SQLConstants.TABLE_PERSONAL_NOTES);
                builder.appendWhere(SQLConstants.KEY_ID + "=" + id);
                break;

            case PERSONAL_LIST:
                builder.setTables(SQLConstants.TABLE_PERSONAL_NOTES);
                break;

            case KITTY_NOTES_ID:
                id = (int) ContentUris.parseId(uri);
                builder.setTables(SQLConstants.TABLE_KITTY_NOTES);
                builder.appendWhere(SQLConstants.KEY_ID + "=" + id);
                break;

            case KITTY_NOTES_LIST:
                builder.setTables(SQLConstants.TABLE_KITTY_NOTES);
                break;

            case KITTY_PERSONAL_ID:
                id = (int) ContentUris.parseId(uri);
                builder.setTables(SQLConstants.TABLE_KITTY_PERSONAL_NOTES);
                builder.appendWhere(SQLConstants.KEY_ID + "=" + id);
                break;

            case KITTY_PERSONAL_LIST:
                builder.setTables(SQLConstants.TABLE_KITTY_PERSONAL_NOTES);
                break;

            case RULES_ID:
                id = (int) ContentUris.parseId(uri);
                builder.setTables(SQLConstants.TABLE_KITTY_RULES);
                builder.appendWhere(SQLConstants.KEY_ID + "=" + id);
                break;

            case RULES_LIST:
                flag = true;
                builder.setTables(SQLConstants.TABLE_KITTY_RULES);
                break;

            case DIARY_UPDATE_LIST:
            case DIARY_UPDATE_ID:
                flag = true;
                builder.setTables(SQLConstants.TABLE_UPDATE_DAIRY);
                break;


            case SETTING_ID:
                id = (int) ContentUris.parseId(uri);
                builder.setTables(SQLConstants.TABLE_SETTING);
                builder.appendWhere(SQLConstants.KEY_ID + "=" + id);
                break;

            case SETTING_LIST:
                flag = true;
                builder.setTables(SQLConstants.TABLE_SETTING);
                break;

            case QBDIALOG_ID:
                id = (int) ContentUris.parseId(uri);
                builder.setTables(SQLConstants.TABLE_QB_DIALOGS);
                builder.appendWhere(SQLConstants.KEY_ID + "=" + id);
                break;

            case QBDIALOG_LIST:
                flag = true;
                builder.setTables(SQLConstants.TABLE_QB_DIALOGS);
                break;

            case GROUP_CHAT_MEMBER_ID:
            case GROUP_CHAT_MEMBER_LIST:
                flag = true;
                builder.setTables(SQLConstants.TABLE_GROUP_CHAT_MEMBER);
                break;

            case CHAT_MESSAGE_ID:
            case CHAT_MESSAGE_LIST:
                flag = true;
                builder.setTables(SQLConstants.TABLE_MESSAGE);
                break;

            case KITTIES_ID:
                id = (int) ContentUris.parseId(uri);
                builder.setTables(SQLConstants.TABLE_KITTIES);
                builder.appendWhere(SQLConstants.KEY_ID + "=" + id);
                break;

            case KITTIES_LIST:
                flag = true;
                builder.setTables(SQLConstants.TABLE_KITTIES);
                break;

        }

        // if you like you can log the query
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            logQuery(builder, projection, selection, sortOrder);
        } else {
            logQueryDeprecated(builder, projection, selection, sortOrder);
        }

        Cursor cursor;
        if (flag) {
            cursor = kittyBeeSqlOperation.getQuery(builder.getTables(),
                    projection, selection, selectionArgs, sortOrder);
        } else {
            cursor = kittyBeeSqlOperation.getQuery(builder,
                    projection, selection, selectionArgs, sortOrder);
        }

        // if we want to be notified of any changes:
        if (useAuthorityUri) {
            cursor.setNotificationUri(getContext().getContentResolver(), KittyBeeContract.CONTENT_URI);
        } else {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        if (cursor != null)
            AppLog.e(TAG, "##### Completed query ##### " + cursor.getCount());
        return cursor;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        AppLog.e(TAG, "##### Inside delete #####");
        SQLiteDatabase db = MySQLiteHelper.getInstance
                (getContext().getApplicationContext()).getWritableDatabase();
        db.enableWriteAheadLogging();
        final int match = URI_MATCHER.match(uri);
        int rowsDeleted = -1;

        switch (match) {
            case GROUP_ID:
                rowsDeleted = db.delete(SQLConstants.TABLE_GROUP, selection, selectionArgs);
                AppLog.d(TAG, "Deleted Rows = {}" + rowsDeleted);
                break;

            case GROUP_LIST:
                String whereClause = SQLConstants.KEY_ID
                        + "=" + uri.getLastPathSegment();

                if (!TextUtils.isEmpty(selection)) {
                    whereClause += " AND " + selection;
                }
                rowsDeleted = db.delete(SQLConstants.TABLE_GROUP,
                        whereClause, selectionArgs);
                AppLog.d(TAG, "Deleted Rows = {}" + rowsDeleted);
                break;

            case DIARY_UPDATE_ID:
            case DIARY_UPDATE_LIST:

                break;

            case KITTY_PERSONAL_ID:
            case KITTY_PERSONAL_LIST:
                rowsDeleted = db.delete(SQLConstants.TABLE_KITTY_PERSONAL_NOTES, selection, selectionArgs);
                break;


            case KITTY_NOTES_ID:
            case KITTY_NOTES_LIST:
                rowsDeleted = db.delete(SQLConstants.TABLE_KITTY_NOTES, selection, selectionArgs);
                break;

            case PERSONAL_ID:
            case PERSONAL_LIST:
                rowsDeleted = db.delete(SQLConstants.TABLE_PERSONAL_NOTES, selection, selectionArgs);
                break;

            case CHAT_MESSAGE_ID:
            case CHAT_MESSAGE_LIST:
                rowsDeleted = db.delete(SQLConstants.TABLE_MESSAGE, selection, selectionArgs);
                AppLog.d(TAG, "Deleted Rows = {}" + rowsDeleted);
                break;

            case KITTIES_ID:
            case KITTIES_LIST:
                rowsDeleted = db.delete(SQLConstants.TABLE_KITTIES, selection, selectionArgs);
                AppLog.d(TAG, "Deleted Rows = {}" + rowsDeleted);
                break;

            case GROUP_CHAT_MEMBER_ID:
            case GROUP_CHAT_MEMBER_LIST:
                rowsDeleted = db.delete(SQLConstants.TABLE_GROUP_CHAT_MEMBER, selection, selectionArgs);
                AppLog.d(TAG, "Deleted Rows = {}" + rowsDeleted);
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        // Notifying the changes, if there are any
        if (rowsDeleted != -1)
            getContext().getContentResolver().notifyChange(uri, null);

        AppLog.e(TAG, "##### Completed delete #####");
        return rowsDeleted;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        AppLog.e(TAG, "##### Inside insert #####");
        try {
            int token = URI_MATCHER.match(uri);
            long id = -1;
            switch (token) {
                case GROUP_LIST:
                    id = kittyBeeSqlOperation.insertGroups(values);
                    return getUriForId(id, uri);

                case DIARY_LIST:
                    id = kittyBeeSqlOperation.insertDiary(values);
                    return getUriForId(id, uri);

                case DIARY_ID:
                    id = kittyBeeSqlOperation.insertDiary(values);
                    return getUriForId(id, uri);

                case SUMMARY_LIST:
                    id = kittyBeeSqlOperation.insertSummary(values);
                    return getUriForId(id, uri);

                case SUMMARY_ID:
                    id = kittyBeeSqlOperation.insertSummary(values);
                    return getUriForId(id, uri);

                case BILL_LIST:
                    id = kittyBeeSqlOperation.insertBill(values);
                    return getUriForId(id, uri);

                case BILL_ID:
                    id = kittyBeeSqlOperation.insertBill(values);
                    return getUriForId(id, uri);

                case ATTENDANCE_LIST:
                    id = kittyBeeSqlOperation.insertAttendance(values);
                    return getUriForId(id, uri);

                case ATTENDANCE_ID:
                    id = kittyBeeSqlOperation.insertAttendance(values);
                    return getUriForId(id, uri);

                case VENUE_LIST:
                    id = kittyBeeSqlOperation.insertVenue(values);
                    return getUriForId(id, uri);

                case VENUE_ID:
                    id = kittyBeeSqlOperation.insertVenue(values);
                    return getUriForId(id, uri);


                case PERSONAL_ID:
                    id = kittyBeeSqlOperation.insertPersonalNotes(values);
                    return getUriForId(id, uri);

                case PERSONAL_LIST:
                    id = kittyBeeSqlOperation.insertPersonalNotes(values);
                    return getUriForId(id, uri);

                case KITTY_NOTES_ID:
                    id = kittyBeeSqlOperation.insertKittyNotes(values);
                    return getUriForId(id, uri);

                case KITTY_NOTES_LIST:
                    id = kittyBeeSqlOperation.insertKittyNotes(values);
                    return getUriForId(id, uri);

                case KITTY_PERSONAL_ID:
                    id = kittyBeeSqlOperation.insertKittyPersonalNotes(values);
                    return getUriForId(id, uri);

                case KITTY_PERSONAL_LIST:
                    id = kittyBeeSqlOperation.insertKittyPersonalNotes(values);
                    return getUriForId(id, uri);


                case RULES_ID:
                    id = kittyBeeSqlOperation.insertRules(values);
                    return getUriForId(id, uri);

                case RULES_LIST:
                    id = kittyBeeSqlOperation.insertRules(values);
                    return getUriForId(id, uri);

                case DIARY_UPDATE_ID:
                case DIARY_UPDATE_LIST:
                    id = kittyBeeSqlOperation.insertUpdateDiary(values);
                    return getUriForId(id, uri);

                case SETTING_ID:
                    id = kittyBeeSqlOperation.insertSetting(values);
                    return getUriForId(id, uri);

                case SETTING_LIST:
                    id = kittyBeeSqlOperation.insertSetting(values);
                    return getUriForId(id, uri);

                case QBDIALOG_ID:
                case QBDIALOG_LIST:
                    id = kittyBeeSqlOperation.insertQbDialogs(values);
                    return getUriForId(id, uri);

                case KITTIES_ID:
                case KITTIES_LIST:
                    id = kittyBeeSqlOperation.insertKitties(values);
                    return getUriForId(id, uri);

                case GROUP_CHAT_MEMBER_ID:
                case GROUP_CHAT_MEMBER_LIST:
                    id = kittyBeeSqlOperation.insertGroupChatMember(values);
                    return getUriForId(id, uri);

                case CHAT_MESSAGE_ID:
                case CHAT_MESSAGE_LIST:
                    id = kittyBeeSqlOperation.insertMessage(values);
                    return getUriForId(id, uri);


            }
        } catch (SQLException e) {
        }
        AppLog.e(TAG, "##### Completed insert #####");
        return null;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        doAnalytics(uri, "bulkInsert");
        AppLog.e(TAG, "##### Inside bulkInsert #####");
        /*int token = URI_MATCHER.match(uri);
        switch (token) {
            case GROUP_LIST:
                for (int i = 0; i < values.length; i++) {
                    kittyBeeSqlOperation.insertGroups(values[i]);
                }
                getContext().getContentResolver().notifyChange(uri, null);
                break;

            case GROUP_ID:
                for (int i = 0; i < values.length; i++) {
                    kittyBeeSqlOperation.insertGroups(values[i]);
                }
                getContext().getContentResolver().notifyChange(uri, null);
                break;
        }*/
        AppLog.e(TAG, "##### Completed bulkInsert #####");
        return values.length;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        AppLog.e(TAG, "##### Inside updated #####");
        SQLiteDatabase db = MySQLiteHelper.getInstance
                (getContext().getApplicationContext()).getWritableDatabase();
        db.enableWriteAheadLogging();
        final int match = URI_MATCHER.match(uri);
        int rowUpdated = -1;

        switch (match) {

            case CHAT_MESSAGE_ID:
            case CHAT_MESSAGE_LIST:
                rowUpdated = db.update(SQLConstants.TABLE_MESSAGE, values, selection, selectionArgs);
                AppLog.d(TAG, "Updated Rows = {}" + rowUpdated);
                break;

            case GROUP_CHAT_MEMBER_ID:
            case GROUP_CHAT_MEMBER_LIST:
                rowUpdated = db.update(SQLConstants.TABLE_GROUP_CHAT_MEMBER, values, selection, selectionArgs);
                AppLog.d(TAG, "Updated Rows = {}" + rowUpdated);
                break;

            case KITTIES_ID:
            case KITTIES_LIST:
                rowUpdated = db.update(SQLConstants.TABLE_KITTIES, values, selection, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        // Notifying the changes, if there are any
        if (rowUpdated != -1)
            getContext().getContentResolver().notifyChange(uri, null);

        AppLog.e(TAG, "##### Completed updated #####");
        return rowUpdated;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void logQuery(SQLiteQueryBuilder builder, String[] projection, String selection, String sortOrder) {
        if (AppLog.DEBUG) {
            AppLog.d(TAG, "Query: " + builder.buildQuery(projection, selection, null, null, sortOrder, null));
        }
    }

    @SuppressWarnings("deprecation")
    private void logQueryDeprecated(SQLiteQueryBuilder builder, String[] projection, String selection, String sortOrder) {
        if (AppLog.DEBUG) {
            AppLog.d(TAG, "Query: " + builder.buildQuery(projection, selection, null, null, null, sortOrder, null));
        }
    }

    /**
     * @param id
     * @param uri
     * @return
     */
    private Uri getUriForId(long id, Uri uri) throws SQLException {
        if (id > 0) {
            Uri itemUri = ContentUris.withAppendedId(uri, id);
            if (!isInBatchMode()) {
                // notify all listeners of changes and return itemUri:
                getContext().getContentResolver().notifyChange(itemUri, null);
            }
            return itemUri;
        }
        // s.th. went wrong:
        throw new SQLException("Problem while inserting into uri: " + uri);
    }

    private boolean isInBatchMode() {
        return mIsInBatchMode.get() != null && mIsInBatchMode.get();
    }

    /**
     * I do not really use analytics, but if you export
     * your content provider it makes sense to do so, to get
     * a feeling for client usage. Especially if you want to
     * _change_ something which might break existing clients,
     * please check first if you can safely do so.
     */
    private void doAnalytics(Uri uri, String event) {
        AppLog.d(TAG, event + " -> " + uri);
        AppLog.d(TAG, "caller: " + detectCaller());
    }

    /**
     * You can use this for Analytics.
     * <p/>
     * Be aware though: This might be costly if many apps
     * are running.
     */
    private String detectCaller() {
        // found here:
        // https://groups.google.com/forum/#!topic/android-developers/0HsvyTYZldA
        int pid = Binder.getCallingPid();
        return getProcessNameFromPid(pid);
    }

    /**
     * Returns the name of the process the pid belongs to. Can be null if neither
     * an Activity nor a Service could be found.
     *
     * @param givenPid
     * @return
     */
    private String getProcessNameFromPid(int givenPid) {
        ActivityManager am = (ActivityManager) getContext().getSystemService(
                Activity.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> lstAppInfo = am
                .getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo ai : lstAppInfo) {
            if (ai.pid == givenPid) {
                return ai.processName;
            }
        }
        // added to take care of calling services as well:
        List<ActivityManager.RunningServiceInfo> srvInfo = am
                .getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo si : srvInfo) {
            if (si.pid == givenPid) {
                return si.process;
            }
        }
        return null;
    }
}
