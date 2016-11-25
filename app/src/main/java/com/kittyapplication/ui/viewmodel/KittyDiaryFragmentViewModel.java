package com.kittyapplication.ui.viewmodel;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;

import com.google.gson.Gson;
import com.kittyapplication.R;
import com.kittyapplication.adapter.DiaryListAdapter;
import com.kittyapplication.custom.MiddleOfKittyListener;
import com.kittyapplication.model.ChatData;
import com.kittyapplication.model.DiaryDao;
import com.kittyapplication.model.DiaryResponseDao;
import com.kittyapplication.model.DiarySubmitDao;
import com.kittyapplication.model.KittiesDao;
import com.kittyapplication.model.MembersDaoCalendar;
import com.kittyapplication.model.ServerResponse;
import com.kittyapplication.model.SummaryListDao;
import com.kittyapplication.providers.KittyBeeContract;
import com.kittyapplication.rest.Singleton;
import com.kittyapplication.sqlitedb.SQLConstants;
import com.kittyapplication.ui.activity.BillActivity;
import com.kittyapplication.ui.activity.NotesActivity;
import com.kittyapplication.ui.activity.RuleActivity;
import com.kittyapplication.ui.activity.SelectDairyHostActivity;
import com.kittyapplication.ui.fragment.KittyDiaryFragments;
import com.kittyapplication.utils.AlertDialogUtils;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.DateTimeUtils;
import com.kittyapplication.utils.PreferanceUtils;
import com.kittyapplication.utils.Utils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by HalfBloodPrince(RIONTECH)
 * Riontech on 18/10/16.
 */

public class KittyDiaryFragmentViewModel {
    private static final String TAG = KittyDiaryFragmentViewModel.class.getSimpleName();
    private KittyDiaryFragments mFragment;
    private KittiesDao mKittyData;
    private DiaryResponseDao mDiaryData;
    private ChatData mChatData;
    private boolean mSelectMySelfForPunctuality = false;
    private int mPosition;
    private boolean mIsNextPunctuality, mFirstPunctualitySelect = false;
    private SetDataTask mSetDataTask;


    public KittyDiaryFragmentViewModel(KittyDiaryFragments fragments, int pos, DiaryDao dao) {
        AppLog.e(TAG, "KittyDiaryFragmentViewModel");
        mPosition = pos;
        mFragment = fragments;
        mKittyData = dao.getDiaryData().getKitties().get(pos);
        mDiaryData = dao.getDiaryData();
        mChatData = dao.getChatData();
        if (mKittyData.getGetPunctuality().equalsIgnoreCase("1")) {
            mIsNextPunctuality = true;
        }
    }


    /**
     * @param hostList
     * @return
     */
    public boolean isUserHosting(List<String> hostList) {
        String userId = PreferanceUtils.getLoginUserObject(mFragment.getActivityContext()).getUserID();
        for (int i = 0; i < hostList.size(); i++) {
            if (hostList.get(i).equalsIgnoreCase(userId)
                    || hostList.get(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }


    /**
     * @param rightsList
     * @return
     */
    public boolean isUserHasRights(List<String> rightsList) {
        for (int i = 0; i < rightsList.size(); i++) {
            if (rightsList.get(i).equalsIgnoreCase
                    (PreferanceUtils.getLoginUserObject(mFragment.getActivityContext()).getUserID())) {
                return true;
            }
        }
        return false;
    }


    private void checkUserCanSetPunctuality(int noOfPunchuality, boolean kittyDone) {
        AppLog.e(TAG, "checkUserCanSetPunctuality");
        String noOfPunctualityStr = String.valueOf(noOfPunchuality);
        //check kitty not done & check noOfpunc. is not 0
        if (!noOfPunctualityStr.equalsIgnoreCase(AppConstant.STRING_ZERO) && !kittyDone) {
            if (!noOfPunctualityStr.equalsIgnoreCase(mKittyData.getGetPunctuality())
                    && mKittyData.getGetPunctuality().equalsIgnoreCase(AppConstant.STRING_ONE)) {
                mFragment.setVisibilityForView(R.id.btnPunctuality, View.VISIBLE);

            } else if (!noOfPunctualityStr.equalsIgnoreCase(mKittyData.getGetPunctuality())
                    && mKittyData.getGetPunctuality().equalsIgnoreCase(AppConstant.STRING_TWO)) {
                mFragment.setVisibilityForView(R.id.btnPunctuality, View.VISIBLE);

            } else if (!noOfPunctualityStr.equalsIgnoreCase(mKittyData.getGetPunctuality())
                    && mKittyData.getGetPunctuality().equalsIgnoreCase(AppConstant.STRING_ZERO)) {
                mFragment.setVisibilityForView(R.id.btnPunctuality, View.VISIBLE);
            } else {
                mFragment.setVisibilityForView(R.id.btnPunctuality, View.GONE);
            }
        } else {
            mFragment.setVisibilityForView(R.id.btnPunctuality, View.GONE);
        }
    }


    private void checkKittyDone(boolean hasHostOrRights, boolean kittyDone) {
        AppLog.e(TAG, "checkUserCanSetPunctuality");
        // check is current host & kitty not done
        if (hasHostOrRights && !kittyDone) {
            mFragment.setVisibilityForView(R.id.btnSelectHost, View.VISIBLE);
        } else {
            mFragment.setVisibilityForView(R.id.btnSelectHost, View.GONE);
        }
    }

    public void selectHost(String groupId, String id, String mNoOfHost) {
        Intent intent = new Intent(mFragment.getActivityContext(), SelectDairyHostActivity.class);
        intent.putExtra(AppConstant.GROUP_ID, groupId);
        intent.putExtra(AppConstant.KITTY_ID, id);
        intent.putExtra(AppConstant.NO_OF_HOST, mNoOfHost);
//        intent.putExtra(AppConstant., mData.getp)
        mFragment.startActivity(intent);
    }

    /**
     * @param
     * @return - list
     */
    private ArrayList<MembersDaoCalendar> getSelectedMemberListNotCurrentHost
    (List<MembersDaoCalendar> list) {
        ArrayList<MembersDaoCalendar> selectedList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getPunctuality().equalsIgnoreCase(AppConstant.ATTENDANCE_CHECKED)) {
                if (list.get(i).getHost().equalsIgnoreCase(AppConstant.ATTENDANCE_CHECKED) ||
                        checkIsCurrentHost(list.get(i).getHost()) &&
                                list.get(i).getNumber().equalsIgnoreCase
                                        (PreferanceUtils.getLoginUserObject(mFragment.getActivityContext()).getPhone())) {
                    mSelectMySelfForPunctuality = true;
                } else {
                    if (list.get(i).getGetPunctuality().equalsIgnoreCase(AppConstant.ATTENDANCE_CHECKED)) {
                        mSelectMySelfForPunctuality = true;
                    } else {
                        selectedList.add(list.get(i));
                    }
                }
            }
        }
        return selectedList;
    }


    private boolean checkIsCurrentHost(String host) {
        try {
            if (host.contains(AppConstant.SEPERATOR_STRING)) {
                String[] hosts = host.split(AppConstant.SEPERATOR_STRING);
                if (hosts != null && host.length() > 0) {
                    if (hosts[0].equalsIgnoreCase(AppConstant.ATTENDANCE_CHECKED) ||
                            hosts[1].equalsIgnoreCase(AppConstant.ATTENDANCE_CHECKED)) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    private ArrayList<MembersDaoCalendar> getShuffleList(ArrayList<MembersDaoCalendar> selectedList) {
        Collections.shuffle(selectedList);
        return selectedList;
    }


    /**
     * @param list
     */
    public void submitDairy(List<MembersDaoCalendar> list) {
        DiarySubmitDao diarySubmitDao = new DiarySubmitDao();
        diarySubmitDao.setGroupId(mKittyData.getGroupId());
        diarySubmitDao.setPunctuality(mKittyData.getGetPunctuality());
        diarySubmitDao.setKittyId(mKittyData.getId());
        diarySubmitDao.setMemberCheckList(list);

        updateDiary(list, mKittyData.getGetPunctuality());
        mFragment.showToastMessage(mFragment.getActivityContext().getResources().getString(R.string.diary_update));

        if (Utils.checkInternetConnection(mFragment.getActivityContext())) {
            Call<ServerResponse<List<SummaryListDao>>> call =
                    Singleton.getInstance().getRestOkClient().addDairies(diarySubmitDao);
            call.enqueue(summarySubmitCallBack);
        }
    }

    Callback<ServerResponse<List<SummaryListDao>>> summarySubmitCallBack =
            new Callback<ServerResponse<List<SummaryListDao>>>() {
                @Override
                public void onResponse(Call<ServerResponse<List<SummaryListDao>>> call,
                                       Response<ServerResponse<List<SummaryListDao>>> response) {
//                    mFragment.hideProgressBar();

                }

                @Override
                public void onFailure(Call<ServerResponse<List<SummaryListDao>>> call, Throwable t) {
//                    mFragment.hideProgressBar();

                }
            };


    /**
     * @param list
     * @param puctuality
     */
    private void updateDiary(List<MembersDaoCalendar> list, String puctuality) {
//        int position = ((DiaryActivity) mFragment.getActivityContext()).getCurrentViewPagerItemPosition();

        Uri uri = ContentUris.withAppendedId(KittyBeeContract.Diaries.CONTENT_URI,
                Long.valueOf(mKittyData.getGroupId()));
        ContentResolver resolver = mFragment.getActivityContext().getContentResolver();
        Cursor cursor = resolver.query(uri, KittyBeeContract.Groups.PROJECTION_ALL,
                null, null, null);

        if (cursor != null && cursor.getCount() > 0) {
            // show data from db
            Gson gson = new Gson();
            DiaryResponseDao diaryResponseDao = new DiaryResponseDao();
            while (cursor.moveToNext()) {
                diaryResponseDao = gson.fromJson(cursor.getString
                        (cursor.getColumnIndex(SQLConstants.KEY_DATA)), DiaryResponseDao.class);
            }
            cursor.close();
            for (int i = 0; i < diaryResponseDao.getKitties().size(); i++) {
                if (diaryResponseDao.getKitties().get(i).getId().equalsIgnoreCase(mKittyData.getId())) {
                    diaryResponseDao.getKitties().get(i).setGetPunctuality(puctuality);
                    diaryResponseDao.getKitties().get(i).setMembers(list);
                    break;
                }
            }

//            diaryResponseDao.getKitties().get(mPosition).setGetPunctuality(puctuality);
//            diaryResponseDao.getKitties().get(mPosition).setMembers(list);
            insertIntoDiary(diaryResponseDao, true);
        }
    }


    private void insertIntoDiary(DiaryResponseDao diaryResponseDao, boolean flag) {
        ContentValues values = new ContentValues();
        values.put(SQLConstants.KEY_ID, mKittyData.getGroupId());
        values.put(SQLConstants.KEY_DATA, new Gson().toJson(diaryResponseDao));
        if (flag) {
            values.put(SQLConstants.KEY_IS_SYNC, 1);
        }
        mFragment.getActivityContext().getContentResolver()
                .insert(KittyBeeContract.Diaries.CONTENT_URI, values);
    }


    public void actionPunctualityTask(List<MembersDaoCalendar> list, DiaryListAdapter adapter) {
        new PunctualityTask(list, adapter).execute();
    }

    /**
     * punctuality Task
     */
    public class PunctualityTask extends AsyncTask<Void, Void, List<MembersDaoCalendar>> {

        private List<MembersDaoCalendar> mMemberList;
        private String mAlertMessage;
        private DiaryListAdapter mAdapter;
        private boolean isErrorMessageOccur = false;

        public PunctualityTask(List<MembersDaoCalendar> list, DiaryListAdapter adapter) {
            mMemberList = list;
            mAdapter = adapter;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // disable punctuality button
            mFragment.setEnableDisableView(R.id.btnPunctuality, false);
            mFragment.setVisibilityForView(R.id.btnPunctuality, View.GONE);

            if (Integer.valueOf(mDiaryData.getNoOfPunctuality()) == 2
                    && !mIsNextPunctuality) {
                // enable punctuality button
                mFragment.setEnableDisableView(R.id.btnPunctuality, true);
                mFragment.setVisibilityForView(R.id.btnPunctuality, View.VISIBLE);
            }
        }

        @Override
        protected List<MembersDaoCalendar> doInBackground(Void... params) {
            mSelectMySelfForPunctuality = false;
            mFragment.setPunctualityFlag(false);

            ArrayList<MembersDaoCalendar> selectedList =
                    getSelectedMemberListNotCurrentHost(mMemberList);

            if (!selectedList.isEmpty()) {
                if (Integer.valueOf(mDiaryData.getNoOfPunctuality()) == 1) {
                    selectedList = getShuffleList(selectedList);

                    if (!selectedList.get(0).getName().isEmpty()) {
                        mSelectMySelfForPunctuality = true;
                        mFragment.setPunctualityFlag(true);
                        mAlertMessage = mFragment.getActivityContext().getResources()
                                .getString(R.string.punctuality_goes_to_one_person,
                                        Utils.checkIfMeForBoth(mFragment.getActivityContext(),
                                                selectedList.get(0).getNumber(),
                                                selectedList.get(0).getName()));
                    } else {
                        mSelectMySelfForPunctuality = true;
                        mFragment.setPunctualityFlag(true);
//                        mAlertMessage = mFragment.getActivityContext().getResources()
//                                .getString(R.string.punctuality_goes_to_one_person,
//                                        selectedList.get(0).getNumber().replace(AppConstant.SEPERATOR_STRING, ""));
                        mAlertMessage = mFragment.getActivityContext().getResources()
                                .getString(R.string.punctuality_goes_to_one_person,
                                        Utils.checkIfMeForBoth(mFragment.getActivityContext(),
                                                selectedList.get(0).getNumber(),
                                                selectedList.get(0).getName()));

                    }
                    selectedList.get(0).setPunctuality(AppConstant.ATTENDANCE_CHECKED);
                    mKittyData.setGetPunctuality("1");
                    mFragment.setNoOfPunctualityClicked(1);
                } else if (Integer.valueOf(mDiaryData.getNoOfPunctuality()) == 2) {
                    selectedList = getShuffleList(selectedList);

                    if (mFragment.getNoOfPunctualityClicked() >= 2) {
                        mFragment.setPunctualityFlag(false);
                        isErrorMessageOccur = true;
                        mAlertMessage = mFragment.getActivityContext().getResources().
                                getString(R.string.both_punctuality_already_selected);
                    }

                    if (!mIsNextPunctuality) {
                        String firstPerson = selectedList.get(0).getName();
                        if (!firstPerson.isEmpty()) {
                            firstPerson = Utils.checkIfMeForBoth(mFragment.getActivityContext(),
                                    selectedList.get(0).getNumber()
                                    , selectedList.get(0).getName());
                        } else {
//                            firstPerson = selectedList.get(0).getNumber()
//                                    .replace(AppConstant.SEPERATOR_STRING, "");

                            firstPerson = Utils.checkIfMeForBoth(mFragment.getActivityContext(),
                                    selectedList.get(0).getNumber()
                                    , selectedList.get(0).getName());

                        }

                        mFragment.setPunctualityFlag(true);
                        mAlertMessage = mFragment.getActivityContext().getResources()
                                .getString(R.string.punctuality_goes_to_one_person, firstPerson);
                        selectedList.get(0).setPunctuality(AppConstant.ATTENDANCE_CHECKED);
                        mIsNextPunctuality = true;
                        mFragment.setNoOfPunctualityClicked(1);
                        mKittyData.setGetPunctuality("1");
                        return selectedList;
                    } else {
                        String secondPerson;
                        secondPerson = Utils.checkIfMeForBoth(mFragment.getActivityContext(),
                                selectedList.get(0).getNumber(), selectedList.get(0).getName());
                        mFragment.setPunctualityFlag(true);

                        mAlertMessage = mFragment.getActivityContext().getResources()
                                .getString(R.string.second_punctuality_goes_to_one_person,
                                        secondPerson);
                        selectedList.get(0).setPunctuality(AppConstant.ATTENDANCE_CHECKED);
                        mKittyData.setGetPunctuality("2");
                        mIsNextPunctuality = false;
                        mFragment.setNoOfPunctualityClicked(2);
                    }
                }
            }
            return selectedList;
        }

        @Override
        protected void onPostExecute(List<MembersDaoCalendar> selectedList) {
            super.onPostExecute(selectedList);
            if (Utils.isValidList(selectedList)) {
                if (Integer.valueOf(mDiaryData.getNoOfPunctuality()) == 1
                        && mFragment.isPunctualityFlag()) {
                    mFragment.notifyAdapterForDisplayPunctuality(selectedList.get(0).getNumber());
                    mFragment.setVisibilityForView(R.id.btnPunctuality, View.GONE);
                } else if (Integer.valueOf(mDiaryData.getNoOfPunctuality()) == 2
                        && mFragment.isPunctualityFlag()) {
                    if (!mFirstPunctualitySelect) {
                        mFragment.notifyAdapterForDisplayPunctuality(selectedList.get(0).getNumber());
                        mFirstPunctualitySelect = true;
                    } else {
                        mFragment.notifyAdapterForDisplayPunctuality(selectedList.get(0).getNumber());
                        mFragment.setVisibilityForView(R.id.btnPunctuality, View.GONE);
                    }
                }
            } else {
                mFragment.setEnableDisableView(R.id.btnPunctuality, true);
                mFragment.setVisibilityForView(R.id.btnPunctuality, View.VISIBLE);
                if (mSelectMySelfForPunctuality) {
                    isErrorMessageOccur = true;
                    mAlertMessage = mFragment.getActivityContext().getResources()
                            .getString(R.string.you_can_not_select_host_for_punctuality);
                } else {
                    isErrorMessageOccur = true;
                    mAlertMessage = mFragment.getActivityContext().getResources()
                            .getString(R.string.mark_member_to_set_punctuality);
                }
            }

            AlertDialogUtils.showOKButtonPopUpWithMessage(mFragment.getActivityContext(), mAlertMessage,
                    new MiddleOfKittyListener() {
                        @Override
                        public void clickOnYes() {

                        }

                        @Override
                        public void clickOnNo() {

                        }
                    });
            //check if any alert message occured
            if (!isErrorMessageOccur)
                submitDiaryData(mAdapter);
        }
    }


    /**
     * get Diary data for submit to server
     *
     * @param adapter
     */
    public void submitDiaryData(DiaryListAdapter adapter) {
        if (Integer.valueOf(mDiaryData.getNoOfPunctuality()) > 0 &&
                mFragment.getNoOfPunctualityClicked() > 0) {
            if (Integer.valueOf(mDiaryData.getNoOfPunctuality()) == 1) {
                if (mFragment.getNoOfPunctualityClicked() == 1) {
                    submitDairy(adapter.getList());
                }
            } else if (Integer.valueOf(mDiaryData.getNoOfPunctuality()) == 2) {
                submitDairy(adapter.getList());
            } else {
                submitDairy(adapter.getList());
            }
        } else {
            submitDairy(adapter.getList());
        }
    }


    /**
     * select host
     */
    public void actionSelectHost() {
        try {
            Date hostedByDate = DateTimeUtils.getHostedByDateFormat().parse(mKittyData.getKittyDate());
            if (new Date().after(hostedByDate)) {
                if (!mKittyData.getCurrentHost().equalsIgnoreCase(AppConstant.ATTENDANCE_CHECKED)) {
                    mFragment.showToastMessage(
                            mFragment.getActivityContext().getResources().getString(R.string.host_already_selected));
                } else {
                    Intent intent = new Intent(mFragment.getActivityContext(), SelectDairyHostActivity.class);
                    intent.putExtra(AppConstant.GROUP_ID, mChatData.getGroupID());
                    intent.putExtra(AppConstant.KITTY_ID, mKittyData.getId());
                    intent.putExtra(AppConstant.NO_OF_HOST, mChatData.getNoOfHost());
                    mFragment.startActivity(intent);
                }
            } else {
                mFragment.showToastMessage(mFragment.getActivityContext().getResources()
                        .getString(R.string.cannot_edit_before)
                        + mKittyData.getKittyDate());
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public void startSetViewTask() {
        mSetDataTask = new SetDataTask();
        mSetDataTask.execute();
    }

    public void cancelTask() {
        if (mSetDataTask != null) {
            mSetDataTask.cancel(true);
        }
    }


    /**
     *
     */
    private class SetDataTask extends AsyncTask<Void, Void, Void> {
        private int hostVisibility, submiutVisibility, punctualityVisibility = View.GONE;
        private boolean hostEnable, submitEnable, puctualityEnable = true;
        private boolean hasHostOrRights = false;
        private boolean isHostEmpty = false;
        private int noOfPunctuality = 0;
        private boolean isRunning = true;

        @Override
        protected void onCancelled() {
            AppLog.e(TAG, "CANCELLED....");
            isRunning = false;
            super.onCancelled();
        }

        @Override
        protected void onCancelled(Void aVoid) {
            AppLog.e(TAG, "CANCELLED....");
            isRunning = false;
            super.onCancelled(aVoid);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (isCancelled()) {
                    isRunning = false;
                    return null;
                }

                List<String> hostList = mKittyData.getHostId();
                String hostedByDate = mKittyData.getKittyDate();
                String todayDate = DateTimeUtils.getHostedByDateFormat().format(new Date());

                //Check Current user is Hosting Kitty
                if (Utils.isValidList(hostList)) {
                    hasHostOrRights = isUserHosting(hostList);
                } else {
                    isHostEmpty = true;
                }

                //Check Login user has Rights to edit for kitty
                List<String> rightsList = mKittyData.getRights();
                if (Utils.isValidList(rightsList) && !hasHostOrRights) {
                    hasHostOrRights = isUserHasRights(rightsList);
                }

                //  Hint: (if hostList is Empty OR host not install kittyApp
                //        && user is Admin then he/she can do Punctuality & Host selection)
                //Check is hostList Empty && isAdmin == 1
                if (isHostEmpty && mChatData.getIsAdmin().equalsIgnoreCase(AppConstant.ATTENDANCE_CHECKED)) {
                    hasHostOrRights = true;
                }

                //Check kitty is Done or Not
                boolean kittyDone = false;
                if (mKittyData.getKittyDone().equalsIgnoreCase(AppConstant.ATTENDANCE_UNCHECKED)) {
                    kittyDone = true;
                }

                Date hostedDate = DateTimeUtils.getHostedByDateFormat().parse(hostedByDate);
                Date currentDate = DateTimeUtils.getHostedByDateFormat().parse(todayDate);


                if (hasHostOrRights) {
                    //check if hostedDate before todayDate && hostedDate is todayDate
                    if (hostedDate.before(currentDate) || hostedDate.equals(currentDate)) {
                        //                        noOfPunchuality = Integer.valueOf(mNoOfPunctuality);
                        //TODO
                        noOfPunctuality = Integer.valueOf(mDiaryData.getNoOfPunctuality());
//                        checkUserCanSetPunctuality(noOfPunctuality, kittyDone);
                        if (!mDiaryData.getNoOfPunctuality().equalsIgnoreCase(AppConstant.STRING_ZERO)
                                && !kittyDone) {
                            if (!mDiaryData.getNoOfPunctuality().equalsIgnoreCase(mKittyData.getGetPunctuality())
                                    && mKittyData.getGetPunctuality().equalsIgnoreCase(AppConstant.STRING_ONE)) {
//                                mFragment.setVisibilityForView(R.id.btnPunctuality, View.VISIBLE);
                                punctualityVisibility = View.VISIBLE;
                            } else if (!mDiaryData.getNoOfPunctuality().equalsIgnoreCase(mKittyData.getGetPunctuality())
                                    && mKittyData.getGetPunctuality().equalsIgnoreCase(AppConstant.STRING_TWO)) {
//                                mFragment.setVisibilityForView(R.id.btnPunctuality, View.VISIBLE);
                                punctualityVisibility = View.VISIBLE;
                            } else if (!mDiaryData.getNoOfPunctuality().equalsIgnoreCase(mKittyData.getGetPunctuality())
                                    && mKittyData.getGetPunctuality().equalsIgnoreCase(AppConstant.STRING_ZERO)) {
//                                mFragment.setVisibilityForView(R.id.btnPunctuality, View.VISIBLE);
                                punctualityVisibility = View.VISIBLE;
                            } else {
//                                mFragment.setVisibilityForView(R.id.btnPunctuality, View.GONE);
                                punctualityVisibility = View.GONE;
                            }
                        } else {
                            if ((noOfPunctuality - Integer.valueOf(mKittyData.getGetPunctuality())) == 0) {
//                              mFragment.setVisibilityForView(R.id.btnPunctuality, View.GONE);
                                punctualityVisibility = View.GONE;
                            } else {
                                punctualityVisibility = View.VISIBLE;
                            }
                        }


//                        checkKittyDone(hasHostOrRights, kittyDone);

                        if (hasHostOrRights && !kittyDone) {
//                            mFragment.setVisibilityForView(R.id.btnSelectHost, View.VISIBLE);
                            hostVisibility = View.VISIBLE;
                            hostEnable = true;
                        } else {
//                            mFragment.setVisibilityForView(R.id.btnSelectHost, View.GONE);
                            hostVisibility = View.GONE;
                            hostEnable = false;
                        }


                    } else {
                        hostVisibility = View.GONE;
                        hostEnable = false;

                        punctualityVisibility = View.GONE;
                        puctualityEnable = false;
                    }

                    submitEnable = true;
                    submiutVisibility = View.VISIBLE;
                } else {
                    hostVisibility = View.GONE;
                    hostEnable = false;
                    punctualityVisibility = View.GONE;
                    puctualityEnable = false;

                    submitEnable = false;
                    submiutVisibility = View.GONE;
                }
            } catch (Exception e) {
                isRunning = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            AppLog.e(TAG, "IS CANCELLED ---- " + isRunning);

            if (isRunning) {
                mFragment.setVisibilityForView(R.id.btnPunctuality, punctualityVisibility);
                mFragment.setEnableDisableView(R.id.btnPunctuality, puctualityEnable);

                mFragment.setVisibilityForView(R.id.btnSelectHost, hostVisibility);
                mFragment.setEnableDisableView(R.id.btnSelectHost, hostEnable);

                mFragment.setVisibilityForView(R.id.btnSubmitDairy, submiutVisibility);
                mFragment.setEnableDisableView(R.id.btnSubmitDairy, submitEnable);

                mFragment.setDataIntoList(hasHostOrRights, noOfPunctuality, mKittyData);
            }
        }
    }


    /**
     *
     */
    public void actionKittyPersonalNote() {
        try {
            if (Utils.isValidString(mDiaryData.getId())) {
                Intent intent = new Intent(mFragment.getActivityContext(), NotesActivity.class);
                intent.putExtra(AppConstant.GROUP_ID, mKittyData.getGroupId());
                intent.putExtra(AppConstant.KITTY_ID, mKittyData.getId());
                intent.putExtra(AppConstant.NOTES_TYPE, AppConstant.NOTES_TYPE_PERSONAL);
                mFragment.getActivityContext().startActivity(intent);
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public void actionBill() {
        try {
            Date hostedByDate = DateTimeUtils.getHostedByDateFormat().parse(mKittyData.getKittyDate());
            if (new Date().after(hostedByDate)) {
                if (Utils.isValidString(mKittyData.getId())) {
                    Intent intent = new Intent(mFragment.getActivityContext(), BillActivity.class);
                    intent.putExtra(AppConstant.GROUP_ID, mKittyData.getGroupId());
                    intent.putExtra(AppConstant.KITTY_ID, mKittyData.getId());
                    mFragment.getActivityContext().startActivity(intent);
                }
            } else {
                mFragment.showToastMessage(mFragment.getActivityContext().
                        getResources().getString(R.string.cannot_edit_before)
                        + mChatData.getKittyDate());
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public void actionKittyNotes() {
        try {
            if (Utils.isValidString(mKittyData.getId())) {
                Intent intent = new Intent(mFragment.getActivityContext(), NotesActivity.class);
                intent.putExtra(AppConstant.GROUP_ID, mKittyData.getGroupId());
                intent.putExtra(AppConstant.KITTY_ID, mKittyData.getId());
                intent.putExtra(AppConstant.NOTES_TYPE, AppConstant.NOTES_TYPE_KITTY);
                mFragment.getActivityContext().startActivity(intent);
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public void actionKittyRules() {
        try {
            if (mChatData != null) {
                Intent ruleIntent = new Intent(mFragment.getActivityContext(), RuleActivity.class);
                ruleIntent.putExtra(AppConstant.INTENT_DIARY_DATA,
                        new Gson().toJson(mChatData));
                mFragment.getActivityContext().startActivity(ruleIntent);
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    /*public void updateData(DiaryDao dao, int pos) {
        mKittyData = dao.getDiaryData().getKitties().get(pos);
        mDiaryData = dao.getDiaryData();
        mChatData = dao.getChatData();
        startSetViewTask();
    }*/

    //TODO
    public List<MembersDaoCalendar> setDiaryListWithLogic(List<MembersDaoCalendar> mList,
                                                          String mHasPunctuality, int mNoOfPunchuality, String kittyDate) {
        for (int position = 0; position < mList.size(); position++) {
            //set HostedByDate
            List<MembersDaoCalendar> list = Utils.seprateCoupleObjectToMember(mList.get(position));
            try {
                Date hostedByDate = DateTimeUtils.getHostedByDateFormat().parse(kittyDate);
                mList.get(position).setHostedByDate(hostedByDate);
            } catch (ParseException e) {
                e.printStackTrace();
                mList.get(position).setHostedByDate(new Date());
            }

            if (list.size() == 2) {
                //couple member
                mList.get(position).setCouple(true);

                //check user is not deleted
                if (list.get(0).getDelete().equalsIgnoreCase(AppConstant.ATTENDANCE_UNCHECKED)
                        || list.get(1).getDelete().equalsIgnoreCase(AppConstant.ATTENDANCE_UNCHECKED)) {
                    // user is not deleted
                    mList.get(position).setUserDeleted(false);

                    // display username
                    mList.get(position).setUsersName("");
                    for (int j = 0; j < list.size(); j++) {
                        if (j > 0) {
                            mList.get(position).setUsersName(mList.get(position).getUsersName().concat("\n" +
                                    Utils.checkIfMe(mFragment.getActivityContext(), list.get(j).getNumber(), list.get(j).getName())));
                        } else {
                            mList.get(position).setUsersName(
                                    Utils.checkIfMe(mFragment.getActivityContext(), list.get(j).getNumber(), list.get(j).getName()));
                        }
                    }

                    //check is punctual Couple
                    if (hasPunctualCouple(list)) {

                        //punctual couple
                        mList.get(position).setPunctualCouple(true);
                    } else {

                        //not punctual couple
                        mList.get(position).setPunctualCouple(false);
                    }

                    //check is paid
                    if (mList.get(position).getPaid().equalsIgnoreCase(AppConstant.ATTENDANCE_CHECKED)) {

                        //set paid = true
                        mList.get(position).setPaid(true);
                    } else {

                        //set paid = false
                        mList.get(position).setPaid(false);
                    }

                    //
                    if (new Date().after(mList.get(position).getHostedByDate())) {
                        mList.get(position).setAfterHostedByDate(true);

                        if (mList.get(position).getPresent().equalsIgnoreCase(AppConstant.ATTENDANCE_CHECKED)) {
                            mList.get(position).setPresent(true);
                        } else {
                            mList.get(position).setPresent(false);
                        }
                    } else {
                        mList.get(position).setAfterHostedByDate(false);
                    }

                    if ((mHasPunctuality.equalsIgnoreCase(AppConstant.ATTENDANCE_UNCHECKED)
                            || mHasPunctuality.equalsIgnoreCase(AppConstant.ATTENDANCE_CHECKED))
                            && mNoOfPunchuality > 0) {
                        mList.get(position).setAvailablePunctuality(true);

                        if (mList.get(position).getHost().contains(AppConstant.ATTENDANCE_CHECKED) &&
                                mList.get(position).getNumber().contains
                                        (PreferanceUtils.getLoginUserObject(mFragment.getActivityContext()).getPhone())) {
                            mList.get(position).setHost(true);
                        } else {
                            mList.get(position).setHost(false);
                        }

                        if (mList.get(position).getPunctuality().equalsIgnoreCase(AppConstant.ATTENDANCE_CHECKED)) {
                            mList.get(position).setPunctuality(true);
                        } else {
                            mList.get(position).setPunctuality(false);
                        }
                    } else {
                        mList.get(position).setAvailablePunctuality(false);
                        if (mList.get(position).getPunctuality().equalsIgnoreCase(AppConstant.ATTENDANCE_CHECKED)) {
                            mList.get(position).setPunctuality(true);
                        } else {
                            mList.get(position).setPunctuality(false);
                        }
                    }
                } else {
                    // user is deleted
                    mList.get(position).setUserDeleted(true);

                    // display deleted username
                    mList.get(position).setUsersName(Utils.checkIfMe(mFragment.getActivityContext(),
                            list.get(0).getNumber().replace(AppConstant.SEPERATOR_STRING, ""), list.get(0).getName().replace(AppConstant.SEPERATOR_STRING, ""))
                            + mFragment.getActivityContext().getResources().getString(R.string.user_deleted_with_dash)
                            + Utils.checkIfMe(mFragment.getActivityContext(), list.get(1).getNumber(),
                            list.get(0).getName().replace(AppConstant.SEPERATOR_STRING, ""))
                            + mFragment.getActivityContext().getResources().getString(R.string.user_deleted))
                    ;
                }
            } else {
                //single members
                mList.get(position).setCouple(false);

                //check user is not deleted
                if (mList.get(position).getDelete().replace(AppConstant.SEPERATOR_STRING, "")
                        .equalsIgnoreCase(AppConstant.ATTENDANCE_UNCHECKED)) {

                    // user is not deleted
                    mList.get(position).setUserDeleted(false);

                    // display username
                    mList.get(position).setUsersName(Utils.checkIfMe(
                            mFragment.getActivityContext(),
                            mList.get(position).getNumber()
                                    .replace(AppConstant.SEPERATOR_STRING, ""),
                            mList.get(position).getName()
                                    .replace(AppConstant.SEPERATOR_STRING, "")));

                    //check is paid
                    if (mList.get(position).getPaid().replace(AppConstant.SEPERATOR_STRING, "")
                            .equalsIgnoreCase(AppConstant.ATTENDANCE_CHECKED)) {

                        //set paid = true
                        mList.get(position).setPaid(true);
                    } else {

                        //set paid = false
                        mList.get(position).setPaid(false);
                    }

                    if (new Date().after(mList.get(position).getHostedByDate())) {
                        mList.get(position).setAfterHostedByDate(true);
                        if (mList.get(position).getPresent().replace(AppConstant.SEPERATOR_STRING, "")
                                .equalsIgnoreCase(AppConstant.ATTENDANCE_CHECKED)) {
                            mList.get(position).setPresent(true);
                        } else {
                            mList.get(position).setPresent(false);
                        }
                    } else {
                        mList.get(position).setAfterHostedByDate(false);
                    }

                    if ((mHasPunctuality.equalsIgnoreCase(AppConstant.ATTENDANCE_UNCHECKED)
                            || mHasPunctuality.equalsIgnoreCase(AppConstant.ATTENDANCE_CHECKED))
                            && mNoOfPunchuality > 0) {
                        mList.get(position).setAvailablePunctuality(true);

                        String hostField = mList.get(position).getHost()
                                .replace(AppConstant.SEPERATOR_STRING, "");
                        String numberField = mList.get(position).getNumber()
                                .replace(AppConstant.SEPERATOR_STRING, "");

                        if (mFragment.getActivityContext() != null) {
                            if (hostField.equalsIgnoreCase(AppConstant.ATTENDANCE_CHECKED) &&
                                    numberField.equalsIgnoreCase((PreferanceUtils.getLoginUserObject(mFragment.getActivityContext())
                                            .getPhone()))) {
                                mList.get(position).setHost(true);
                            } else {
                                mList.get(position).setHost(false);
                            }
                        }

                        if (mList.get(position).getPunctuality().replace(AppConstant.SEPERATOR_STRING, "")
                                .equalsIgnoreCase(AppConstant.ATTENDANCE_CHECKED)) {
                            mList.get(position).setPunctuality(true);
                        } else {
                            mList.get(position).setPunctuality(false);
                        }
                    } else {
                        mList.get(position).setAvailablePunctuality(false);

                        if (mList.get(position).getPunctuality().replace(AppConstant.SEPERATOR_STRING, "")
                                .equalsIgnoreCase(AppConstant.ATTENDANCE_CHECKED)) {
                            mList.get(position).setPunctuality(true);
                        } else {
                            mList.get(position).setPunctuality(false);
                        }
                    }

                    if (Utils.isValidString(mList.get(position).getGetPunctuality())
                            && mList.get(position).getGetPunctuality().equalsIgnoreCase("1")) {
                        mList.get(position).setGetPunctuality(true);
                    } else {
                        mList.get(position).setGetPunctuality(false);
                    }

                } else {
                    // user is not deleted
                    mList.get(position).setUserDeleted(true);

                    // display deleted username
                    mList.get(position).setUsersName(Utils.checkIfMe(
                            mFragment.getActivityContext(), mList.get(position).getNumber().replace(AppConstant.SEPERATOR_STRING, ""),
                            mList.get(position).getName().replace(AppConstant.SEPERATOR_STRING, "")) +
                            mFragment.getActivityContext().getResources().getString(R.string.user_deleted));
                }
            }
        }
        return mList;
    }

    /**
     * couple object has puctuality
     *
     * @param list
     * @return
     */
    private boolean hasPunctualCouple(List<MembersDaoCalendar> list) {
        boolean flag = false;
        if (Utils.isValidList(list)) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getGetPunctuality() != null && Utils.isValidString(list.get(i).getGetPunctuality())) {
                    String getPunctuality = list.get(i).getGetPunctuality();
                    getPunctuality = getPunctuality.replace(AppConstant.SEPERATOR_STRING, "");
                    if (Utils.isValidString(getPunctuality) && getPunctuality.equals(AppConstant.STRING_ONE)) {
                        flag = true;
                        break;
                    }
                }
            }
        }
        return flag;
    }
}