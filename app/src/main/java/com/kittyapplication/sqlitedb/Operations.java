package com.kittyapplication.sqlitedb;

import android.content.ContentValues;
import android.content.Context;

import com.google.gson.Gson;
import com.kittyapplication.model.BillDao;
import com.kittyapplication.model.ChatData;
import com.kittyapplication.model.CreateGroup;
import com.kittyapplication.model.DiaryResponseDao;
import com.kittyapplication.model.Kitty;
import com.kittyapplication.model.NotesResponseDao;
import com.kittyapplication.model.ParticipantDao;
import com.kittyapplication.model.SummaryListDao;
import com.kittyapplication.model.VenueResponseDao;
import com.kittyapplication.providers.KittyBeeContract;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.PreferanceUtils;
import com.kittyapplication.utils.Utils;
import com.quickblox.chat.model.QBDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HalfBloodPrince(RIONTECH)
 * Riontech on 6/10/16.
 */
public class Operations {

    private static final String TAG = Operations.class.getSimpleName();

    /**
     * Insert Venue
     */
    public static void insertVenue(Context ctx, VenueResponseDao dao, boolean flag) {
        try {
            ContentValues values = new ContentValues();
            values.put(SQLConstants.KEY_ID, dao.getKittyId());
            values.put(SQLConstants.KEY_DATA, new Gson().toJson(dao));
            if (flag) {
                values.put(SQLConstants.KEY_IS_SYNC, 1);
            }
//            ctx.getContentResolver().insert(KittyBeeContract.Venue.CONTENT_URI, values);

            final MyQueryHandler queryHandler = new MyQueryHandler(ctx.getContentResolver());
            queryHandler.startInsert(0, null, KittyBeeContract.Venue.CONTENT_URI, values);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }


    /**
     * Insert Bill
     */
    public static void insertBill(Context ctx, BillDao dao, boolean flag) {
        try {
            ContentValues values = new ContentValues();
            values.put(SQLConstants.KEY_GROUP_ID, dao.getGroupId());
            values.put(SQLConstants.KEY_KITTY_ID, dao.getKittyId());
            values.put(SQLConstants.KEY_DATA, new Gson().toJson(dao));
            if (flag) {
                values.put(SQLConstants.KEY_IS_SYNC, 1);
            }

            final MyQueryHandler queryHandler = new MyQueryHandler(ctx.getContentResolver());
            queryHandler.startInsert(0, null, KittyBeeContract.Bill.CONTENT_URI, values);

//            ctx.getContentResolver().insert(KittyBeeContract.Bill.CONTENT_URI, values);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public int getNotePosByDescription(String description, List<NotesResponseDao> list) {
        int pos = -1;
        if (Utils.isValidList(list)) {
            for (int i = 0; i < list.size(); i++) {
                if (description.equalsIgnoreCase(list.get(i).getDescription())) {
                    pos = i;
                    break;
                }
            }
        }
        return pos;
    }


    public static void insertKittyPersonalNotes(Context ctx, List<NotesResponseDao> responseDao, boolean flag, String kittyId) {

        if (Utils.isValidList(responseDao)) {
            final MyQueryHandler queryHandler = new MyQueryHandler(ctx.getContentResolver());

            for (int i = 0; i < responseDao.size(); i++) {
                ContentValues values = new ContentValues();
                /*if (Utils.isValidString(mReqNoteDao.getGroupId())) {
                    values.put(SQLConstants.KEY_ID, mReqNoteDao.getKitty());
                } else {
                    values.put(SQLConstants.KEY_ID, PreferanceUtils.getLoginUserObject(mActivity)
                            .getUserID());
                }*/
                if (flag) {
                    values.put(SQLConstants.KEY_IS_SYNC, 1);
                }
                values.put(SQLConstants.KEY_ID, kittyId);
                values.put(SQLConstants.KEY_NOTE_ID, responseDao.get(i).getNoteID());
                values.put(SQLConstants.KEY_DATA, new Gson().toJson(responseDao.get(i)));
//                ctx.getContentResolver().insert(KittyBeeContract.KittyPersonalNote.CONTENT_URI, values);

                queryHandler.startInsert(0, null, KittyBeeContract.KittyPersonalNote.CONTENT_URI, values);
            }
        }
    }

    public static void insertKittyNotes(Context ctx, List<NotesResponseDao> responseDao, boolean flag, String kittyId) {
        if (Utils.isValidList(responseDao)) {
            final MyQueryHandler queryHandler = new MyQueryHandler(ctx.getContentResolver());
            for (int i = 0; i < responseDao.size(); i++) {
                ContentValues values = new ContentValues();
                values.put(SQLConstants.KEY_ID, kittyId);
                values.put(SQLConstants.KEY_NOTE_ID, responseDao.get(i).getNoteID());
                values.put(SQLConstants.KEY_DATA, new Gson().toJson(responseDao.get(i)));
                if (flag) {
                    values.put(SQLConstants.KEY_IS_SYNC, 1);
                }
//                ctx.getContentResolver().insert(KittyBeeContract.KittyNotes.CONTENT_URI, values);
                queryHandler.startInsert(0, null, KittyBeeContract.KittyNotes.CONTENT_URI, values);
            }
        }
    }

    public static void insertPersonalNotes(Context ctx, List<NotesResponseDao> responseDao,
                                           boolean flag) {

        if (Utils.isValidList(responseDao)) {
            final MyQueryHandler queryHandler = new MyQueryHandler(ctx.getContentResolver());
            for (int i = 0; i < responseDao.size(); i++) {
                ContentValues values = new ContentValues();
                /*if (Utils.isValidString(mReqNoteDao.getGroupId())) {
                    values.put(SQLConstants.KEY_ID, mReqNoteDao.getKitty());
                } else {
                    values.put(SQLConstants.KEY_ID, PreferanceUtils.getLoginUserObject(mActivity)
                            .getUserID());
                }*/
                if (flag) {
                    values.put(SQLConstants.KEY_IS_SYNC, 1);
                }
                values.put(SQLConstants.KEY_ID, PreferanceUtils.getLoginUserObject(ctx)
                        .getUserID());
                values.put(SQLConstants.KEY_NOTE_ID, responseDao.get(i).getNoteID());
                values.put(SQLConstants.KEY_DATA, new Gson().toJson(responseDao.get(i)));
//                ctx.getContentResolver().insert(KittyBeeContract.PersonalNote.CONTENT_URI, values);

                queryHandler.startInsert(0, null, KittyBeeContract.PersonalNote.CONTENT_URI, values);
            }
        }
    }

    public static void insertSetting(Context ctx, ParticipantDao dao) {
        ContentValues values = new ContentValues();
        values.put(SQLConstants.KEY_ID, dao.getId());
        values.put(SQLConstants.KEY_DATA, new Gson().toJson(dao));
//        ctx.getContentResolver().insert(KittyBeeContract.Setting.CONTENT_URI, values);

        final MyQueryHandler queryHandler = new MyQueryHandler(ctx.getContentResolver());
        queryHandler.startInsert(0, null, KittyBeeContract.Setting.CONTENT_URI, values);
    }

    /**
     * @param diaryResponseDao
     */

    public static void insertIntoDiary(Context ctx, DiaryResponseDao diaryResponseDao, String groupId) {
        try {
            if (diaryResponseDao != null && diaryResponseDao.getId() != null) {
                final MyQueryHandler queryHandler = new MyQueryHandler(ctx.getContentResolver());

                ContentValues values = new ContentValues();
                values.put(SQLConstants.KEY_ID, groupId);
                values.put(SQLConstants.KEY_DATA, new Gson().toJson(diaryResponseDao));
//                ctx.getContentResolver().insert(KittyBeeContract.Diaries.CONTENT_URI, values);
                queryHandler.startInsert(0, null, KittyBeeContract.Diaries.CONTENT_URI, values);

                if (Utils.isValidList(diaryResponseDao.getKitties()))
                    for (int i = 0; i < diaryResponseDao.getKitties().size(); i++) {
                        String kittyId = diaryResponseDao.getKitties().get(i).getId();

                        // insert kitty notes
                        if (Utils.isValidList(diaryResponseDao.getKitties().get(i).getKittyNotes())) {
                            insertKittyNotes(ctx, diaryResponseDao.getKitties().get(i).getKittyNotes(), false, kittyId);
                        }

                        //insert kitty personal notes
                        if (Utils.isValidList(diaryResponseDao.getKitties().get(i).getKittyPersonalNotes())) {
                            insertKittyPersonalNotes(ctx, diaryResponseDao.getKitties().get(i).getKittyPersonalNotes(), false, kittyId);
                        }
                        // inser bill
                        if (diaryResponseDao.getKitties().get(i).getBillData() != null
                                && diaryResponseDao.getKitties().get(i).getBillData().getId() != null) {
                            insertBill(ctx, diaryResponseDao.getKitties().get(i).getBillData(), false);
                        }
                    }
            } else {
                AppLog.d(TAG, "insertIntoDiary is null at group id " + groupId);
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }


    /**
     * @param diaryResponseDao
     */
    public static void insertIntoSummary(Context ctx, List<SummaryListDao> diaryResponseDao, String groupId) {
        try {
            if (Utils.isValidString(groupId) && Utils.isValidList(diaryResponseDao)) {
                ContentValues values = new ContentValues();
                values.put(SQLConstants.KEY_ID, groupId);
                values.put(SQLConstants.KEY_DATA, new Gson().toJson(diaryResponseDao));
//                ctx.getContentResolver().insert(KittyBeeContract.Summary.CONTENT_URI, values);

                final MyQueryHandler queryHandler = new MyQueryHandler(ctx.getContentResolver());
                queryHandler.startInsert(0, null, KittyBeeContract.Summary.CONTENT_URI, values);
            } else {
                AppLog.d(TAG, "insertIntoSummary is null at group id " + groupId);
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }


    /**
     * Insert Kitty Rules
     */
    public static void insertKittyRules(Context ctx, CreateGroup dao, String groupId) {
        try {
            if (dao != null) {
                ContentValues values = new ContentValues();
                AppLog.d(TAG, "IDS = " + dao.getGroupID() + " data " + new Gson().toJson(dao));
                values.put(SQLConstants.KEY_ID, groupId);
                values.put(SQLConstants.KEY_DATA, new Gson().toJson(dao));
//                ctx.getContentResolver().insert(KittyBeeContract.Rules.CONTENT_URI, values);

                final MyQueryHandler queryHandler = new MyQueryHandler(ctx.getContentResolver());
                queryHandler.startInsert(0, null, KittyBeeContract.Rules.CONTENT_URI, values);
            } else {
                AppLog.d(TAG, "Rule is null at group id" + groupId);
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }


    /**
     */
    public static void insertGroups(List<ChatData> dataList, final Context mContext) {
        if (dataList != null && !dataList.isEmpty()) {
            final MyQueryHandler queryHandler = new MyQueryHandler(mContext.getContentResolver());
            for (int i = 0; i < dataList.size(); i++) {
                ContentValues values = new ContentValues();
                values.put(SQLConstants.KEY_ID, dataList.get(i).getGroupID());
                values.put(SQLConstants.KEY_DATA, new Gson().toJson(dataList.get(i)));

                queryHandler.startInsert(0, null, KittyBeeContract.Groups.CONTENT_URI, values);
            }
        }
    }

    /**
     */
    public static void insertGroups(ChatData dataList, final Context mContext) {
        ContentValues values = new ContentValues();
        values.put(SQLConstants.KEY_ID, dataList.getGroupID());
        values.put(SQLConstants.KEY_DATA, new Gson().toJson(dataList));
//        mContext.getContentResolver().insert(KittyBeeContract.Groups.CONTENT_URI, values);

        final MyQueryHandler queryHandler = new MyQueryHandler(mContext.getContentResolver());
        queryHandler.startInsert(0, null, KittyBeeContract.Groups.CONTENT_URI, values);
    }

    /**
     *
     */
    public static void insertQBDialog(ArrayList<QBDialog> dialogs, final Context mContext) {
        if (dialogs != null && !dialogs.isEmpty()) {
            for (int i = 0; i < dialogs.size(); i++) {
                ContentValues values = new ContentValues();
                values.put(SQLConstants.KEY_ID, dialogs.get(i).getDialogId());
                values.put(SQLConstants.KEY_DATA, new Gson().toJson(dialogs.get(i)));
                values.put(SQLConstants.KEY_TIMESTAMP, dialogs.get(i).getLastMessageDateSent());
//                mContext.getContentResolver().insert(KittyBeeContract.QBDialog.CONTENT_URI, values);

                final MyQueryHandler queryHandler = new MyQueryHandler(mContext.getContentResolver());
                queryHandler.startInsert(0, null, KittyBeeContract.QBDialog.CONTENT_URI, values);
            }
        }
    }

    /**
     *
     */
    public static void insertQBDialog(QBDialog dialogs, final Context mContext) {
        ContentValues values = new ContentValues();
        values.put(SQLConstants.KEY_ID, dialogs.getDialogId());
        values.put(SQLConstants.KEY_DATA, new Gson().toJson(dialogs));
        values.put(SQLConstants.KEY_TIMESTAMP, dialogs.getLastMessageDateSent());
//        mContext.getContentResolver().insert(KittyBeeContract.QBDialog.CONTENT_URI, values);

        final MyQueryHandler queryHandler = new MyQueryHandler(mContext.getContentResolver());
        queryHandler.startInsert(0, null, KittyBeeContract.QBDialog.CONTENT_URI, values);
    }

    /**
     * @param chats
     * @param mContext
     */
    public static void insertKitties(ArrayList<Kitty> chats, final Context mContext) {
        if (chats != null && !chats.isEmpty()) {
            final MyQueryHandler queryHandler = new MyQueryHandler
                    (mContext.getContentResolver());
            for (int i = 0; i < chats.size(); i++) {
                ContentValues values = new ContentValues();
                if (chats.get(i).getGroup() != null
                        && chats.get(i).getGroup().getGroupID() != null) {
                    values.put(SQLConstants.KEY_ID, chats.get(i).getGroup().getGroupID());
                    values.put(SQLConstants.KEY_DATA, new Gson().toJson(chats.get(i)));
                    values.put(SQLConstants.KEY_TIMESTAMP, chats.get(i).getQbDialog().getLastMessageDateSent());
                    queryHandler.startInsert(0, null, KittyBeeContract.Kitties.CONTENT_URI, values);

//                    mContext.getContentResolver().insert(KittyBeeContract.Kitties.CONTENT_URI, values);
                } else {
                    AppLog.e(TAG, "Group Id is NULL.");
                }
            }
        }
    }

    /**
     * @param chats
     * @param mContext
     */
    public static void insertKitties(Kitty chats, final Context mContext) {
        final MyQueryHandler queryHandler = new MyQueryHandler
                (mContext.getContentResolver());
        ContentValues values = new ContentValues();
        if (chats.getGroup() != null && chats.getGroup().getGroupID() != null) {
            values.put(SQLConstants.KEY_ID, chats.getGroup().getGroupID());
            values.put(SQLConstants.KEY_DATA, new Gson().toJson(chats));
            values.put(SQLConstants.KEY_TIMESTAMP, chats.getQbDialog().getLastMessageDateSent());
//            mContext.getContentResolver().insert(KittyBeeContract.Kitties.CONTENT_URI, values);

            queryHandler.startInsert(0, null, KittyBeeContract.Kitties.CONTENT_URI, values);
        } else {
            AppLog.e(TAG, "Group Id is NULL.");
        }
    }

    /**
     * @param mContext
     */
    public static void deleteAllKitties(final Context mContext) {
        MyQueryHandler queryHandler = new MyQueryHandler(mContext.getContentResolver());
        queryHandler.startDelete(0, null, KittyBeeContract.Kitties.CONTENT_URI, null, null);
    }
}