package com.kittyapplication.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;

import com.google.gson.Gson;
import com.kittyapplication.model.BillDao;
import com.kittyapplication.model.ChatData;
import com.kittyapplication.model.CreateGroup;
import com.kittyapplication.model.DiarySubmitDao;
import com.kittyapplication.model.NotesRequestDao;
import com.kittyapplication.model.NotesResponseDao;
import com.kittyapplication.model.ServerResponse;
import com.kittyapplication.model.SummaryListDao;
import com.kittyapplication.model.VenueResponseDao;
import com.kittyapplication.providers.KittyBeeContract;
import com.kittyapplication.rest.Singleton;
import com.kittyapplication.sqlitedb.Operations;
import com.kittyapplication.sqlitedb.SQLConstants;
import com.kittyapplication.sqlitedb.SqlDataSetTask;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.GroupPrefHolder;
import com.kittyapplication.utils.PreferanceUtils;
import com.kittyapplication.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by Scorpion on 04-08-2015.
 */
public class SynchAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = SynchAdapter.class.getSimpleName();
    private final Context mContext;
    private String baseUrl;
    private Operations mOperation;

    public SynchAdapter(final Context context, final boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
        this.baseUrl = AppConstant.BASE_URL;
        mOperation = new Operations();

    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              final ContentProviderClient provider, SyncResult syncResult) {
        try {
            Intent myIntent = new Intent(AppConstant.ACTION_SERVER_DETAILS);
            AppLog.e(TAG, "onPerformSync...");

            //TODO get Sync only data args
            String selectionForUpdate = SQLConstants.KEY_IS_SYNC + "=?";
            String[] selectionArgsForUpdate = {"1"};

            if (Utils.checkInternetConnection(mContext)) {
                try {
                    // TODO Calling getGroup Web Service API and ins
                    Call<ServerResponse<List<ChatData>>> call = Singleton.getInstance()
                            .getRestOkClient().getGroupChatData
                                    (PreferanceUtils.getLoginUserObject(mContext).getUserID());
                    List<ChatData> dataList = call.execute().body().getData();
                    if (Utils.isValidList(dataList)) {
                        GroupPrefHolder.getInstance().saveGroupChats(dataList);
                        final ArrayList<ContentProviderOperation> providerOperationList = new ArrayList<>();
                        for (int j = 0; j < dataList.size(); j++) {
                            final ContentProviderOperation operation = ContentProviderOperation
                                    .newInsert(KittyBeeContract.Groups.CONTENT_URI)
                                    .withValue(SQLConstants.KEY_ID, dataList.get(j).getGroupID())
                                    .withValue(SQLConstants.KEY_DATA, new Gson().toJson(dataList.get(j)))
                                    .build();
                            providerOperationList.add(operation);
                        }
                        provider.applyBatch(providerOperationList);
                    }

                    // TODO Calling BILL Web Service API and ins

                    Cursor cursor = provider.query
                            (KittyBeeContract.Bill.CONTENT_URI, null, selectionForUpdate,
                                    selectionArgsForUpdate, null);
                    if (cursor != null && cursor.getCount() > 0) {
                        AppLog.e(TAG, "##### Bill " + cursor.getCount() + " #####");
                        while (cursor.moveToNext()) {
                            String data = cursor.getString(cursor.getColumnIndex(SQLConstants.KEY_DATA));
                            final BillDao dao = new Gson().fromJson(data, BillDao.class);

                            Call<ServerResponse<BillDao>> billCall = null;
                            Response<ServerResponse<BillDao>> billResponse = null;

                            if (dao.getId() != null && dao.getId().length() > 0) {
                                billCall = Singleton.getInstance().
                                        getRestOkClient().editBill(dao);
                            } else {
                                billCall = Singleton.getInstance().
                                        getRestOkClient().addBill(dao);
                            }
                            if (billCall != null)
                                billResponse = billCall.execute();

                            if (billResponse != null && billResponse.code() == 200) {
                                if (billResponse.body().getResponse() == AppConstant.RESPONSE_SUCCESS) {
                                    if (billResponse.body().getData() != null) {
                                        // Update Value in db After sync with server
                                        mOperation.insertBill(mContext, dao, false);
                                    }
                                }
                            }
                        }
                        AppLog.e(TAG, "##### Bill End#####");
                        cursor.close();
                    }

                    // TODO CALLING VENUE API

                    Cursor venueCursor = provider.query
                            (KittyBeeContract.Venue.CONTENT_URI, null, selectionForUpdate,
                                    selectionArgsForUpdate, null);
                    if (venueCursor != null && venueCursor.getCount() > 0) {
                        AppLog.e(TAG, "##### Venue " + venueCursor.getCount() + " #####");
                        while (venueCursor.moveToNext()) {
                            String data = venueCursor.getString(venueCursor.getColumnIndex(SQLConstants.KEY_DATA));
                            final VenueResponseDao dao = new Gson().fromJson(data, VenueResponseDao.class);

                            Call<ServerResponse<List<VenueResponseDao>>> venueCall;
                            Response<ServerResponse<List<VenueResponseDao>>> venueResponse = null;

                            if (dao.getId() != null && dao.getId().length() > 0) {
                                venueCall = Singleton.getInstance().
                                        getRestOkClient().editVenue(dao.getId(), dao);
                            } else {
                                venueCall = Singleton.getInstance().
                                        getRestOkClient().addVenue(dao);
                            }

                            if (venueCall != null)
                                venueResponse = venueCall.execute();

                            if (venueResponse != null && venueResponse.code() == 200) {
                                if (venueResponse.body().getResponse() == AppConstant.RESPONSE_SUCCESS) {
                                    if (venueResponse.body().getData().get(0) != null) {
                                        // Update Value in db After sync with server
                                        mOperation.insertVenue(mContext, dao, false);
                                    }
                                }
                            }
                        }

                        AppLog.e(TAG, "##### Venue End#####");
                        venueCursor.close();
                    }

                    // TODO Calling DIARY Web Service API and ins
                    Cursor diaryCursor = provider.query
                            (KittyBeeContract.UpdateDiary.CONTENT_URI, null,
                                    selectionForUpdate, selectionArgsForUpdate, null);
                    if (diaryCursor != null && diaryCursor.getCount() > 0) {
                        AppLog.e(TAG, "##### Update Diary " + diaryCursor.getCount() + " #####");
                        while (diaryCursor.moveToNext()) {
                            String groupId = diaryCursor.getString(diaryCursor.getColumnIndex(SQLConstants.KEY_ID));
                            String selection = SQLConstants.KEY_ID + "=?";
                            String[] selectionArgs = {groupId};
                            String data = diaryCursor.getString(diaryCursor.getColumnIndex(SQLConstants.KEY_DATA));
                            DiarySubmitDao dao = new Gson().fromJson(data, DiarySubmitDao.class);
                            Call<ServerResponse<List<SummaryListDao>>> updateDiaryCall =
                                    Singleton.getInstance().getRestOkClient().addDairies(dao);


                            Response<ServerResponse<List<SummaryListDao>>> response = updateDiaryCall.execute();
                            if (response != null && response.code() == 200) {
                                provider.delete(KittyBeeContract.UpdateDiary.CONTENT_URI, selection, selectionArgs);
                            }

                        }
                        AppLog.e(TAG, "##### Update Diary End#####");
                        diaryCursor.close();
                    }


                    // TODO Calling KITTY RULES Web Service API and ins
                    Cursor rulesCursor = provider.query
                            (KittyBeeContract.Rules.CONTENT_URI, null, selectionForUpdate,
                                    selectionArgsForUpdate, null);
                    if (rulesCursor != null && rulesCursor.getCount() > 0) {
                        AppLog.e(TAG, "##### Update Rule To Server " + rulesCursor.getCount() + " #####");
                        while (rulesCursor.moveToNext()) {

                            final String groupId = rulesCursor.getString(rulesCursor.getColumnIndex(SQLConstants.KEY_ID));
                            String data = rulesCursor.getString(rulesCursor.getColumnIndex(SQLConstants.KEY_DATA));
                            final CreateGroup dao = new Gson().fromJson(data, CreateGroup.class);

                            if (Utils.isValidString(groupId) && dao != null) {
                                Call<ServerResponse<List<CreateGroup>>> rulesCall = Singleton.getInstance().getRestOkClient()
                                        .updateRuleData(groupId, dao);
                                Response<ServerResponse<List<CreateGroup>>> responseRules = rulesCall.execute();

                                if (responseRules != null && responseRules.code() == 200) {
                                    if (responseRules.body().getResponse() == AppConstant.RESPONSE_SUCCESS) {
                                        if (responseRules.body().getData() != null) {
                                            // Update Value in db After sync with server
                                            //remove from sync & update in db
                                          /*  ContentValues values = new ContentValues();
                                            values.put(SQLConstants.KEY_ID, groupId);
                                            values.put(SQLConstants.KEY_DATA, new Gson().toJson(dao));
                                            values.put(SQLConstants.KEY_IS_SYNC, 0);
                                            mContext.getContentResolver().insert(KittyBeeContract.Rules.CONTENT_URI, values);*/

                                            // insert data into db
                                            new SqlDataSetTask(mContext, responseRules.body().getGroup());
                                        }
                                    }
                                }
                            }

                        }
                        AppLog.e(TAG, "##### Update Rule To Server End#####");
                        rulesCursor.close();
                    }

                    // TODO CALLING KITTY PERSONAL NOTES
                    Cursor kittyPersonalNotes = provider.query
                            (KittyBeeContract.KittyPersonalNote.CONTENT_URI,
                                    null, selectionForUpdate,
                                    selectionArgsForUpdate, null);

                    if (kittyPersonalNotes != null && kittyPersonalNotes.getCount() > 0) {
                        AppLog.e(TAG, "##### Update Kitty personal notes To Server " +
                                kittyPersonalNotes.getCount() + " #####");
                        while (kittyPersonalNotes.moveToNext()) {

                            String data = kittyPersonalNotes.getString(kittyPersonalNotes.getColumnIndex(SQLConstants.KEY_DATA));
                            final NotesResponseDao dataRes = new Gson().fromJson(data, NotesResponseDao.class);
                            // create request object
                            final NotesRequestDao requestDao = new NotesRequestDao();
                            requestDao.setTitle(dataRes.getTitle());
                            requestDao.setUserId(dataRes.getUserId());
                            requestDao.setKitty(dataRes.getKitty());
                            requestDao.setDescription(dataRes.getDescription());
                            requestDao.setGroupId(dataRes.getGroupId());
                            requestDao.setType(dataRes.getNoteType());


                            //call api
                            Call<ServerResponse<List<NotesResponseDao>>> kittyPersonalCall =
                                    Singleton.getInstance().getRestOkClient().addNote(requestDao);
                            Response<ServerResponse<List<NotesResponseDao>>> kittyPersonalResponce
                                    = kittyPersonalCall.execute();

                            if (kittyPersonalResponce != null && kittyPersonalResponce.code() == 200) {

                                if (kittyPersonalResponce.body().getResponse() == AppConstant.RESPONSE_SUCCESS) {
                                    if (Utils.isValidList(kittyPersonalResponce.body().getData())) {

                                        // delete old note
                                        String deleteSelection = SQLConstants.KEY_NOTE_ID + "=?";
                                        String[] deleteArgs = {String.valueOf(dataRes.getNoteID())};
                                        try {
                                            provider.delete(KittyBeeContract.KittyPersonalNote.CONTENT_URI,
                                                    deleteSelection, deleteArgs);
                                        } catch (Exception e) {
                                        }

                                        // insert updated note
                                        int pos = mOperation.getNotePosByDescription(dataRes.getDescription(),
                                                kittyPersonalResponce.body().getData());
                                        if (pos != -1) {
                                            List<NotesResponseDao> list = new ArrayList<>();
                                            list.add(kittyPersonalResponce.body().getData().get(pos));
                                            mOperation.insertKittyPersonalNotes(mContext, list, false, dataRes.getKitty());
                                        }
                                    }
                                }
                            }
                        }
                        AppLog.e(TAG, "##### Update Kitty personal notes To Server End #####");
                        kittyPersonalNotes.close();
                    }


                    // TODO CALLING KITTY NOTES
                    Cursor kittyNotes = provider.query
                            (KittyBeeContract.KittyNotes.CONTENT_URI,
                                    null, selectionForUpdate,
                                    selectionArgsForUpdate, null);

                    if (kittyNotes != null && kittyNotes.getCount() > 0) {
                        AppLog.e(TAG, "##### Update kittyPersonalNote  To Server " +
                                kittyNotes.getCount() + " #####");
                        while (kittyNotes.moveToNext()) {

                            String data = kittyNotes.getString(kittyNotes.getColumnIndex(SQLConstants.KEY_DATA));
                            final NotesResponseDao dataRes = new Gson().fromJson(data, NotesResponseDao.class);
                            // create request object
                            final NotesRequestDao requestDao = new NotesRequestDao();
                            requestDao.setTitle(dataRes.getTitle());
                            requestDao.setUserId(dataRes.getUserId());
                            requestDao.setKitty(dataRes.getKitty());
                            requestDao.setDescription(dataRes.getDescription());
                            requestDao.setGroupId(dataRes.getGroupId());
                            requestDao.setType(dataRes.getNoteType());
                            //call api

                            Call<ServerResponse<List<NotesResponseDao>>> kittyNotesCall =
                                    Singleton.getInstance().getRestOkClient().addNote(requestDao);

                            Response<ServerResponse<List<NotesResponseDao>>> kittyNoteResponce = kittyNotesCall.execute();

                            if (kittyNoteResponce != null && kittyNoteResponce.code() == 200) {

                                if (kittyNoteResponce.body().getResponse() == AppConstant.RESPONSE_SUCCESS) {
                                    if (Utils.isValidList(kittyNoteResponce.body().getData())) {

                                        // delete old note
                                        String deleteSelection = SQLConstants.KEY_NOTE_ID + "=?";
                                        String[] deleteArgs = {String.valueOf(dataRes.getNoteID())};
                                        try {
                                            provider.delete(KittyBeeContract.KittyNotes.CONTENT_URI,
                                                    deleteSelection, deleteArgs);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        // insert updated note
                                        int pos = mOperation.getNotePosByDescription(dataRes.getDescription(),
                                                kittyNoteResponce.body().getData());
                                        if (pos != -1) {
                                            List<NotesResponseDao> list = new ArrayList<>();
                                            list.add(kittyNoteResponce.body().getData().get(pos));
                                            mOperation.insertKittyNotes(mContext, list, false, dataRes.getKitty());
                                        }
                                    }
                                }
                            }

                        }
                        AppLog.e(TAG, "##### Update kittyPersonalNote  To Server End #####");
                        kittyNotes.close();
                    }
                    // TODO CALLING PERSONAL NOTES
                    Cursor personalNote = provider.query
                            (KittyBeeContract.PersonalNote.CONTENT_URI,
                                    null, selectionForUpdate,
                                    selectionArgsForUpdate, null);

                    if (personalNote != null && personalNote.getCount() > 0) {
                        AppLog.e(TAG, "##### Update Personal notes To Server " +
                                personalNote.getCount() + " #####");
                        while (personalNote.moveToNext()) {

                            String data = personalNote.getString(personalNote.getColumnIndex(SQLConstants.KEY_DATA));
                            final NotesResponseDao dataRes = new Gson().fromJson(data, NotesResponseDao.class);
                            // create request object
                            NotesRequestDao requestDao = new NotesRequestDao();
                            requestDao.setTitle(dataRes.getTitle());
                            requestDao.setUserId(dataRes.getUserId());
                            requestDao.setKitty(dataRes.getKitty());
                            requestDao.setDescription(dataRes.getDescription());
                            requestDao.setGroupId(dataRes.getGroupId());
                            requestDao.setType(dataRes.getNoteType());
                            //call api
                            Call<ServerResponse<List<NotesResponseDao>>> personalNoteCall =
                                    Singleton.getInstance().getRestOkClient().addNote(requestDao);

                            Response<ServerResponse<List<NotesResponseDao>>> personalResponse = personalNoteCall.execute();

                            if (personalResponse != null && personalResponse.code() == 200) {

                                if (personalResponse.body().getResponse() == AppConstant.RESPONSE_SUCCESS) {
                                    if (Utils.isValidList(personalResponse.body().getData())) {

                                        // delete old note
                                        String deleteSelection = SQLConstants.KEY_NOTE_ID + "=?";
                                        String[] deleteArgs = {String.valueOf(dataRes.getNoteID())};
                                        try {
                                            provider.delete(KittyBeeContract.PersonalNote.CONTENT_URI,
                                                    deleteSelection, deleteArgs);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        // insert updated note
                                        int pos = mOperation.getNotePosByDescription(dataRes.getDescription(),
                                                personalResponse.body().getData());
                                        if (pos != -1) {
                                            List<NotesResponseDao> list = new ArrayList<>();
                                            list.add(personalResponse.body().getData().get(pos));
                                            mOperation.insertPersonalNotes(mContext, list, false);
                                        }
                                    }
                                }
                            }

                        }
                        AppLog.e(TAG, "##### Update Personal notes To Server End #####");
                        personalNote.close();
                    }

                } catch (IOException e) {
                    AppLog.e(TAG, "IOException");
                    syncResult.stats.numIoExceptions++;
                    AppLog.e(TAG, e.getMessage(), e);
                } catch (RemoteException e) {
                    AppLog.e(TAG, "RemoteException");
                    syncResult.stats.numIoExceptions++;
                    AppLog.e(TAG, e.getMessage(), e);
                } catch (OperationApplicationException e) {
                    AppLog.e(TAG, "OperationApplicationException");
                    syncResult.stats.numIoExceptions++;
                    AppLog.e(TAG, e.getMessage(), e);
                } finally {
                    if (syncResult.hasError())
                        AppLog.e(TAG, "Sync has Error...");
                }
            } else {
                myIntent.putExtra("responseCode", 500);
            }
            mContext.sendBroadcast(myIntent);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
        AppLog.e(TAG, "*****onPerformSync ends*****");
    }
}