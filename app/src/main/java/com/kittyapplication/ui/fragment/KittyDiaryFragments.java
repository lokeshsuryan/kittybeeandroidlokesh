package com.kittyapplication.ui.fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.kittyapplication.R;
import com.kittyapplication.adapter.DiaryListAdapter;
import com.kittyapplication.custom.fabmenu.FloatingActionButton;
import com.kittyapplication.custom.fabmenu.FloatingActionMenu;
import com.kittyapplication.model.DiaryDao;
import com.kittyapplication.model.KittiesDao;
import com.kittyapplication.model.MembersDaoCalendar;
import com.kittyapplication.ui.activity.KittyDiaryActivity;
import com.kittyapplication.ui.viewmodel.KittyDiaryFragmentViewModel;
import com.kittyapplication.utils.AppLog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by HalfBloodPrince(RIONTECH)
 * Riontech on 18/10/16.
 */

public class KittyDiaryFragments extends Fragment implements View.OnClickListener {
    private static final String TAG = KittyDiaryFragments.class.getSimpleName();
    private int mCurrentPos;
    private DiaryDao mData;
    private ListView mLvDairy;
    private DiaryListAdapter mAdapter;
    private TextView mBtnSubmit, mBtnPunctuality, mBtnSelectHost;
    private TextView mTxtEmpty;
    private ProgressBar mPbLoader;
    private KittyDiaryFragmentViewModel mViewmodel;
    private int mNoOfPunctualityClicked = 0;
    private boolean mPunctualityFlag;
    private ViewGroup mRootView;
    private FloatingActionButton mFabDairyNotes, mFabBill, mFabKittyRules, mFabPersonalNotes;
    private FloatingActionMenu mFabMenu;
    private SetDataAsyncTask mAsyncTask;
    private List<MembersDaoCalendar> finalList;
    private boolean mIsSetDataTaskIsRunning = false;

    public KittyDiaryFragments() {
        // Empty constructor
    }

    public static KittyDiaryFragments newInstance(int pos, DiaryDao diaryDao) {
        AppLog.e(TAG, "newInstance");
        KittyDiaryFragments fragment = new KittyDiaryFragments();
        Bundle args = new Bundle();
        args.putInt("position", pos);
        args.putString("data", new Gson().toJson(diaryDao));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrentPos = getArguments().getInt("position", 0);
        String data = getArguments().getString("data");
        mData = new Gson().fromJson(data, DiaryDao.class);
        AppLog.i(TAG, "KittyDiary Data:" + new Gson().toJson(mData.getDiaryData()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AppLog.e(TAG, "onCreateView");
        mRootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_kitty_diary, container, false);
        mLvDairy = (ListView) mRootView.findViewById(R.id.lvDairy);
        mTxtEmpty = (TextView) mRootView.findViewById(R.id.txtEmpty);
        mBtnSubmit = (TextView) mRootView.findViewById(R.id.btnSubmitDairy);
        mBtnPunctuality = (TextView) mRootView.findViewById(R.id.btnPunctuality);
        mBtnSelectHost = (TextView) mRootView.findViewById(R.id.btnSelectHost);
        mPbLoader = (ProgressBar) mRootView.findViewById(R.id.pbLoaderDiaryFragment);
        mPbLoader.setVisibility(View.VISIBLE);
        mBtnSelectHost.setOnClickListener(this);
        mBtnSubmit.setOnClickListener(this);
        mBtnPunctuality.setOnClickListener(this);


        mFabMenu = (FloatingActionMenu) mRootView.findViewById(R.id.fabMenuDiary);
        mFabDairyNotes = (FloatingActionButton) mRootView.findViewById(R.id.fabDairyNotes);
        mFabBill = (FloatingActionButton) mRootView.findViewById(R.id.fabBill);
        mFabKittyRules = (FloatingActionButton) mRootView.findViewById(R.id.fabKittyRules);
        mFabPersonalNotes = (FloatingActionButton) mRootView.findViewById(R.id.fabPersonalNotes);
        mFabDairyNotes.setOnClickListener(this);
        mFabBill.setOnClickListener(this);
        mFabKittyRules.setOnClickListener(this);
        mFabPersonalNotes.setOnClickListener(this);
        mViewmodel = new KittyDiaryFragmentViewModel(this, mCurrentPos, mData);
        AppLog.d(TAG, "progress show time" + Calendar.getInstance().getTime());
        return mRootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewmodel.startSetViewTask();

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mViewmodel.cancelTask();
    }

    @Override
    public void onClick(View v) {
        mFabMenu.close(true);
        switch (v.getId()) {
            case R.id.btnSelectHost:
                mViewmodel.submitDiaryData(mAdapter);
                mViewmodel.actionSelectHost();
                break;

            case R.id.btnPunctuality:
                mViewmodel.actionPunctualityTask(mAdapter.getList(), mAdapter);
                break;

            case R.id.btnSubmitDairy:
                mViewmodel.submitDiaryData(mAdapter);
                break;

            case R.id.fabPersonalNotes:
                mViewmodel.actionKittyPersonalNote();
                break;
            case R.id.fabBill:
                mViewmodel.actionBill();
                break;

            case R.id.fabDairyNotes:
                mViewmodel.actionKittyNotes();
                break;

            case R.id.fabKittyRules:
                mViewmodel.actionKittyRules();
                break;
        }

    }

    public boolean isPunctualityFlag() {
        return mPunctualityFlag;
    }

    public void setPunctualityFlag(boolean mPunctualityFlag) {
        this.mPunctualityFlag = mPunctualityFlag;
    }

    /**
     * enable disable view
     *
     * @param viewId
     * @param enable
     */
    public void setEnableDisableView(int viewId, boolean enable) {
        if (enable) {
            mRootView.findViewById(viewId).setEnabled(true);
        } else {
            mRootView.findViewById(viewId).setEnabled(false);
        }
    }


    /**
     * enable disable view
     *
     * @param viewId
     */
    public void setVisibilityForView(int viewId, int visibility) {
        mRootView.findViewById(viewId).setVisibility(visibility);
    }

    /**
     * notify adapter when punctuality is Assign
     *
     * @param number
     */
    public void notifyAdapterForDisplayPunctuality(String number) {
        mAdapter.setGetPunctuality(number);
    }

    public int getNoOfPunctualityClicked() {
        return mNoOfPunctualityClicked;
    }

    public void setNoOfPunctualityClicked(int mNoOfPunctualityClicked) {
        this.mNoOfPunctualityClicked = mNoOfPunctualityClicked;
    }


    public void showToastMessage(String message) {
        try {
            if (getActivity() != null) {
                ((KittyDiaryActivity) getActivity()).showSnackbar(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Set Data into list
     *
     * @param hasUserId
     * @param noOfPunctuality
     * @param dao
     */
    public void setDataIntoList(boolean hasUserId, final int noOfPunctuality, final KittiesDao dao) {
        finalList = new ArrayList<>();
        mAdapter = new DiaryListAdapter(getActivity(), finalList,
                dao.getGetPunctuality(), hasUserId, noOfPunctuality, dao.getKittyDate());
        mLvDairy.setAdapter(mAdapter);

        mAsyncTask = new SetDataAsyncTask(dao.getMembers(), dao.getGetPunctuality(),
                noOfPunctuality, dao.getKittyDate());
        mAsyncTask.execute();

        /*new AsyncTask<Void, MembersDaoCalendar, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                List<MembersDaoCalendar> list =
                        mViewmodel.setDiaryListWithLogic(dao.getMembers(), dao.getGetPunctuality(),
                                noOfPunctuality, dao.getKittyDate());
                for (MembersDaoCalendar dao : list) {
                    publishProgress(dao);
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(MembersDaoCalendar... values) {
                super.onProgressUpdate(values);
                finalList.add(values[0]);
                mAdapter.notifyDataSetChanged();
//                mAdapter.addMember(values[0]);
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                mPbLoader.setVisibility(View.GONE);
            }
        }.execute();*/
    }

    /*public void refreshData(DiaryDao dao, int pos) {
        mViewmodel.updateData(dao, pos);
        mAdapter.notifyDataSetChanged();
    }*/

    @Override
    public void onStop() {
        super.onStop();
        if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
        }
    }

    public Context getActivityContext() {
        return getActivity();
    }

    private class SetDataAsyncTask extends AsyncTask<Void, MembersDaoCalendar, Void> {
        List<MembersDaoCalendar> mList;
        String mPunctuality;
        int mNoOfPunctuality;
        String mKittyDate;

        public SetDataAsyncTask(List<MembersDaoCalendar> list, String punctuality, int noPUnctuality,
                                String kittyDate) {
            mList = list;
            mPunctuality = punctuality;
            mNoOfPunctuality = noPUnctuality;
            mKittyDate = kittyDate;
            mIsSetDataTaskIsRunning = true;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onCancelled(Void aVoid) {
            super.onCancelled(aVoid);
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (mIsSetDataTaskIsRunning) {
                List<MembersDaoCalendar> list =
                        mViewmodel.setDiaryListWithLogic(mList, mPunctuality,
                                mNoOfPunctuality, mKittyDate);
                for (MembersDaoCalendar dao : list) {
                    publishProgress(dao);
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(MembersDaoCalendar... values) {
            super.onProgressUpdate(values);
            if (mIsSetDataTaskIsRunning) {
                finalList.add(values[0]);
                mAdapter.notifyDataSetChanged();
            }
//                mAdapter.addMember(values[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (mIsSetDataTaskIsRunning) {
                mPbLoader.setVisibility(View.GONE);
                mIsSetDataTaskIsRunning = false;
            }
        }
    }

}
