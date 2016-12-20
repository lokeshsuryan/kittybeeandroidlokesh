package com.kittyapplication.ui.viewmodel;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.kittyapplication.R;
import com.kittyapplication.model.NotesRequestDao;
import com.kittyapplication.model.NotesResponseDao;
import com.kittyapplication.model.ServerResponse;
import com.kittyapplication.providers.KittyBeeContract;
import com.kittyapplication.rest.Singleton;
import com.kittyapplication.sqlitedb.SQLConstants;
import com.kittyapplication.ui.activity.NotesActivity;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.PreferanceUtils;
import com.kittyapplication.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Dhaval Riontech on 12/8/16.
 */
public class NotesViewModel {
    private static final String TAG = NotesViewModel.class.getSimpleName();
    private NotesActivity mActivity;
    private boolean mHasToastFromAddNotes = false;
    private String mToastFromAddNotes;
    private NotesRequestDao mReqNoteDao;
    private boolean flag = false;

    public NotesViewModel(NotesActivity activity, NotesRequestDao requestDao) {
        mActivity = activity;
        mReqNoteDao = requestDao;
        initRequest(requestDao, "");
    }

    public void initRequest(NotesRequestDao requestDao, String toastMessage) {
        mActivity.showLoader();
        mToastFromAddNotes = toastMessage;

        //0 == for KittyNotes
        //1 == for KittyPersonalNote
        //2 == for PersonalNote
        if (Utils.isValidString(requestDao.getGroupId())) {
            if (requestDao.getType().equalsIgnoreCase(AppConstant.NOTES_TYPE_PERSONAL)) {
                // FOR KITTY PERSONAL NOTE
                new SyncTask(1).execute(requestDao.getKitty());
            } else {
                // FOR KITTY NOTES
                new SyncTask(0).execute(requestDao.getKitty());
            }
        } else {
            // FOR PERSONAL NOTE
            new SyncTask(2).execute(PreferanceUtils.getLoginUserObject(mActivity).getUserID());
        }
    }

    private Callback<ServerResponse<List<NotesResponseDao>>> getNotesDataCallBack = new Callback<ServerResponse<List<NotesResponseDao>>>() {
        @Override
        public void onResponse(Call<ServerResponse<List<NotesResponseDao>>> call, Response<ServerResponse<List<NotesResponseDao>>> response) {
            try {
                if (response.code() == 200) {
                    if (response.body().getData() != null && !response.body().getData().isEmpty()) {

                        if (mHasToastFromAddNotes) {
                            if (Utils.isValidString(mToastFromAddNotes)) {
                                mActivity.showSnackbar(mToastFromAddNotes);
                            }
                            mHasToastFromAddNotes = false;
                            mToastFromAddNotes = "";
                        }
                        insertNoteInDb(response.body().getData(), false);

                        mActivity.setNotesList(response.body().getData());

                    } else {
                        mActivity.hideLoader();
                        mActivity.showEmptyLayout(mActivity.getResources().getString(R.string.empty_notes));
                    }
                } else {
                    mActivity.hideLoader();
                    mActivity.showEmptyLayout(mActivity.getResources().getString(R.string.server_error));
                    AppLog.e(TAG, String.format(mActivity.getResources().getString(R.string.response_code_not_200),
                            "Response Code", response.code()));
                }
            } catch (Exception e) {
                e.printStackTrace();
                mActivity.hideLoader();
            }
        }

        @Override
        public void onFailure(Call<ServerResponse<List<NotesResponseDao>>> call, Throwable t) {
            mActivity.hideLoader();
            mActivity.showEmptyLayout(mActivity.getResources().getString(R.string.server_error));
//            AppLog.e(TAG, "-" + t.getMessage());
        }
    };

    public void setBanner(ImageView img, int type) {
        if (type == 0) {
            Utils.setBannerItem(mActivity,
                    mActivity.getResources().getStringArray(R.array.adv_banner_name)[5],
                    img);
        } else {
            Utils.setBannerItem(mActivity,
                    mActivity.getResources().getStringArray(R.array.adv_banner_name)[6],
                    img);
        }
    }

    public void callSaveNoteApi(String title, String data) {
        if (Utils.checkInternetConnection(mActivity)) {
            mActivity.showLoader();
            NotesRequestDao requestDao = mActivity.getNotesRequestDaoObject();

            requestDao.setTitle(title);
            requestDao.setDescription(data);

            Call<ServerResponse<List<NotesResponseDao>>> call = Singleton.getInstance().getRestOkClient().addNote(requestDao);
            call.enqueue(addNotesCallBack);
        } else {
            //for create random integer for noteId
            Random r = new Random();
            int id = r.nextInt(65 - 1);
            NotesResponseDao dao = new NotesResponseDao();
            dao.setGroupId(mReqNoteDao.getGroupId());
            dao.setDescription(data);
            dao.setNoteID(id);
            dao.setKitty(mReqNoteDao.getKitty());
            dao.setUserId(mReqNoteDao.getUserId());
            dao.setNoteType(mReqNoteDao.getType());
            dao.setTitle(title);

            List<NotesResponseDao> responceDao = new ArrayList<>();
            responceDao.add(dao);
            insertNoteInDb(responceDao, true);
            mActivity.showSnackbar(mActivity.getResources().getString(R.string.note_added_success));
            // for display offline support note which have added currently
            initRequest(mReqNoteDao, "");
        }
    }

    private Callback<ServerResponse<List<NotesResponseDao>>> addNotesCallBack = new Callback<ServerResponse<List<NotesResponseDao>>>() {
        @Override
        public void onResponse(Call<ServerResponse<List<NotesResponseDao>>> call, Response<ServerResponse<List<NotesResponseDao>>> response) {
            try {
                if (response.code() == 200) {
                    if (response.body().getData() != null && !response.body().getData().isEmpty()) {
                        mActivity.setNotesList(response.body().getData());
                        insertNoteInDb(response.body().getData(), false);
                    } else {
                        mActivity.hideLoader();
                        mActivity.showEmptyLayout(mActivity.getResources().getString(R.string.empty_notes));
                    }
                } else {
                    mActivity.hideLoader();
                    mActivity.showEmptyLayout(mActivity.getResources().getString(R.string.empty_notes));
                    AppLog.e(TAG, String.format(mActivity.getResources().getString(R.string.response_code_not_200),
                            "Response Code", response.code()));
                }
            } catch (Resources.NotFoundException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(Call<ServerResponse<List<NotesResponseDao>>> call, Throwable t) {
            mActivity.hideLoader();
            mActivity.showEmptyLayout(mActivity.getResources().getString(R.string.empty_notes));
            AppLog.e(TAG, "-" + t.getMessage());
        }
    };

    public void insertKittyNotes(List<NotesResponseDao> responseDao, boolean flag) {
        if (Utils.isValidList(responseDao)) {
            for (int i = 0; i < responseDao.size(); i++) {
                ContentValues values = new ContentValues();
                values.put(SQLConstants.KEY_ID, mReqNoteDao.getKitty());
                values.put(SQLConstants.KEY_NOTE_ID, responseDao.get(i).getNoteID());
                values.put(SQLConstants.KEY_DATA, new Gson().toJson(responseDao.get(i)));
                if (flag) {
                    values.put(SQLConstants.KEY_IS_SYNC, 1);
                }
                mActivity.getContentResolver().insert(KittyBeeContract.KittyNotes.CONTENT_URI, values);
            }
        }
    }

    public void insertPersonalNotes(List<NotesResponseDao> responseDao, boolean flag) {

        if (Utils.isValidList(responseDao)) {
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
                values.put(SQLConstants.KEY_ID, PreferanceUtils.getLoginUserObject(mActivity)
                        .getUserID());
                values.put(SQLConstants.KEY_NOTE_ID, responseDao.get(i).getNoteID());
                values.put(SQLConstants.KEY_DATA, new Gson().toJson(responseDao.get(i)));
                mActivity.getContentResolver().insert(KittyBeeContract.PersonalNote.CONTENT_URI, values);
            }
        }
    }

    public void insertKittyPersonalNotes(List<NotesResponseDao> responseDao, boolean flag) {

        if (Utils.isValidList(responseDao)) {
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
                values.put(SQLConstants.KEY_ID, mReqNoteDao.getKitty());
                values.put(SQLConstants.KEY_NOTE_ID, responseDao.get(i).getNoteID());
                values.put(SQLConstants.KEY_DATA, new Gson().toJson(responseDao.get(i)));
                mActivity.getContentResolver().insert(KittyBeeContract.KittyPersonalNote.CONTENT_URI, values);
            }
        }
    }

    /**
     *
     */
    private class SyncTask extends AsyncTask<String, Void, List<NotesResponseDao>> {
        private int mType;

        public SyncTask(int type) {
            mType = type;
        }

        @Override
        protected List<NotesResponseDao> doInBackground(String... params) {
            List<NotesResponseDao> noteList = new ArrayList<>();
            Uri uri;
            //0 == for KittyNotes
            //1 == for KittyPersonalNote
            //2 == for PersonalNote

            if (mType == 0) {
                uri = ContentUris.withAppendedId(KittyBeeContract.KittyNotes.CONTENT_URI,
                        Long.valueOf(params[0]));
            } else if (mType == 1) {
                uri = ContentUris.withAppendedId(KittyBeeContract.KittyPersonalNote.CONTENT_URI,
                        Long.valueOf(params[0]));
            } else {
                uri = ContentUris.withAppendedId(KittyBeeContract.PersonalNote.CONTENT_URI,
                        Long.valueOf(params[0]));
            }
            ContentResolver resolver =
                    mActivity.getContentResolver();
            Cursor cursor = resolver.query(
                    uri,                      // the URI to query
                    KittyBeeContract.Groups.PROJECTION_ALL,   // the projection to use
                    null,                           // the where clause without the WHERE keyword
                    null,                           // any wildcard substitutions
                    null);                          // the sort order without the SORT BY keyword

            if (cursor != null && cursor.getCount() > 0) {
                // show data from db
                Gson gson = new Gson();
                while (cursor.moveToNext()) {
                    AppLog.d(TAG, "alsdlkam" + cursor.getString(1));
                    noteList.add(gson.fromJson(cursor.getString(1), NotesResponseDao.class));
                }


                cursor.close();
            }
            return noteList;
        }

        @Override
        protected void onPostExecute(List<NotesResponseDao> noteList) {
            super.onPostExecute(noteList);
//            MySQLiteHelper.getInstance(mActivity).close();
            if (Utils.isValidList(noteList)) {
                mActivity.setNotesList(noteList);
                flag = true;
            }

            if (Utils.checkInternetConnection(mActivity)) {
                mHasToastFromAddNotes = true;
                Call<ServerResponse<List<NotesResponseDao>>> call =
                        Singleton.getInstance().getRestOkClient().getNotes(mReqNoteDao);
                call.enqueue(getNotesDataCallBack);
            } else {
                if (!flag) {
                    mActivity.hideLoader();
                    mActivity.showSnackbar(mActivity.getResources().getString(R.string.no_internet_available));
                    mActivity.showEmptyLayout(mActivity.getResources().getString(R.string.no_data_found));
                }
            }
        }
    }

    private void insertNoteInDb(List<NotesResponseDao> notes, boolean flag) {
        if (Utils.isValidString(mReqNoteDao.getGroupId())) {
            if (mReqNoteDao.getType().equalsIgnoreCase(AppConstant.NOTES_TYPE_PERSONAL)) {
                // FOR KITTY PERSONAL NOTE
                insertKittyPersonalNotes(notes, flag);
            } else {
                // FOR KITTY NOTES
                insertKittyNotes(notes, flag);
            }
        } else {
            // FOR PERSONAL NOTE
            insertPersonalNotes(notes, flag);
        }
    }
}
