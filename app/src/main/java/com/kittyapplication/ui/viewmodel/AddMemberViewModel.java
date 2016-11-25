package com.kittyapplication.ui.viewmodel;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;

import com.google.gson.Gson;
import com.kittyapplication.R;
import com.kittyapplication.adapter.AddMemberAdapter;
import com.kittyapplication.chat.utils.chat.QbUpdateDialogListener;
import com.kittyapplication.chat.utils.qb.QbDialogUtils;
import com.kittyapplication.custom.CustomEditTextNormal;
import com.kittyapplication.model.ContactDao;
import com.kittyapplication.model.CreateGroup;
import com.kittyapplication.model.MemberData;
import com.kittyapplication.model.OfflineDao;
import com.kittyapplication.model.ParticipantDao;
import com.kittyapplication.model.ParticipantMember;
import com.kittyapplication.model.ReqAddMember;
import com.kittyapplication.model.ServerResponse;
import com.kittyapplication.rest.Singleton;
import com.kittyapplication.sqlitedb.SqlDataSetTask;
import com.kittyapplication.ui.activity.AddMemberActivity;
import com.kittyapplication.ui.activity.HomeActivity;
import com.kittyapplication.ui.activity.KittyWithKidsActivity;
import com.kittyapplication.ui.activity.SelectCoupleActivity;
import com.kittyapplication.utils.AlertDialogUtils;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.PreferanceUtils;
import com.kittyapplication.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Pintu Riontech on 18/8/16.
 */
public class AddMemberViewModel {
    private static final String TAG = AddMemberViewModel.class.getSimpleName();
    private List<ContactDao> mMemberList = new ArrayList<>();
    private List<ContactDao> mAleradyExistMemberList = new ArrayList<>();
    private AddMemberActivity mActivity;
    private ProgressDialog mDialog;
    private ParticipantDao mParticipantMember;

    public AddMemberViewModel(AddMemberActivity activity) {
        mActivity = activity;
    }

    public void getData(String data) {
        mParticipantMember = new Gson().fromJson(data, ParticipantDao.class);
//        addAlreadyExistMember(mParticipantMember);
        if (mParticipantMember != null && mParticipantMember.getParticipant() != null
                && Utils.isValidList(mParticipantMember.getParticipant())) {
            mActivity.showView();
            getAlereadyExitsMemberList();
        } else {
            mActivity.hideView();
        }
        AppLog.d(TAG, data);
    }

    private void addAlreadyExistMember(ParticipantDao obj) {
        for (int i = 0; i < obj.getParticipant().size(); i++) {
            ContactDao member = new ContactDao();
            member.setName(obj.getParticipant().get(i).getName());
            member.setPhone(obj.getParticipant().get(i).getNumber());
            member.setImage(obj.getParticipant().get(i).getProfile());
            member.setAlready(true);
            mMemberList.add(member);
            mAleradyExistMemberList.add(member);
            AppLog.d(TAG, new Gson().toJson(mAleradyExistMemberList));

        }
        new AddMemberTask().execute();
        //addContactMember();
    }

    private class AddMemberTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mActivity.showProgressDialog();
        }

        @Override
        protected Void doInBackground(Void... params) {

            List<ContactDao> contactList = PreferanceUtils
                    .getContactListFromPreferance(mActivity).getContactList();

            if (contactList != null && !contactList.isEmpty()) {
                for (int i = 0; i < contactList.size(); i++) {
                    boolean isMatch = false;
                    for (int j = 0; j < mAleradyExistMemberList.size(); j++) {
                        ContactDao object = mAleradyExistMemberList.get(j);
                        if (object.getPhone()
                                .equalsIgnoreCase(contactList.get(i).getPhone())) {
                            isMatch = true;
                            break;
                        }
                    }
                    if (!isMatch) {
                        mMemberList.add(contactList.get(i));
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mActivity.hideProgressDialog();
            mActivity.setDataIntoList(mMemberList);
        }
    }

    public void setSearchBar(final CustomEditTextNormal edtSearch, final AddMemberAdapter adapter) {
        edtSearch.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search_dark, 0, R.drawable.ic_remove_filter, 0);
        edtSearch
                .setOnTouchListener(new View.OnTouchListener() {
                                        @Override
                                        public boolean onTouch(View v, MotionEvent event) {
                                            final int DRAWABLE_LEFT = 0;
                                            final int DRAWABLE_TOP = 1;
                                            final int DRAWABLE_RIGHT = 2;
                                            final int DRAWABLE_BOTTOM = 3;
                                            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                                CustomEditTextNormal edt = (CustomEditTextNormal) mActivity.findViewById(R.id.edtAddMemberSearch);
                                                if (event.getRawX() >= (edt.getRight() -
                                                        edt.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds()
                                                                .width())) {
                                                    adapter.reloadData();
                                                    edtSearch.setText("");
                                                    return true;
                                                }
                                            }
                                            return false;
                                        }
                                    }

                );
        edtSearch
                .addTextChangedListener
                        (new TextWatcher() {
                             @Override
                             public void beforeTextChanged(CharSequence s, int start,
                                                           int count, int after) {

                             }

                             @Override
                             public void onTextChanged(CharSequence s, int start,
                                                       int before, int count) {

                                 adapter.getFilter().filter(s);
                             }

                             @Override
                             public void afterTextChanged(Editable s) {

                             }
                         }

                        );
    }

    //kitty TYPE 0 (COUPLE KITTY)
    //kitty TYPE 1 (KITTY WITH KIDS)
    //kitty TYPE 2 (NORMAL KITTY)
    //kitty TYPE 4 (COMING KITTY FROM ADD MEMBER WHICH IS KITTY WITH KIDS WITHOUT PAID)
    //kitty TYPE 5 (COMING KITTY FROM ADD MEMBER WHICH IS KITTY WITH KIDS WITH PAID)
    //kitty TYPE 6 (COMING KITTY FROM ADD MEMBER WHICH IS COUPLE KITTY)

    public void ClickOnNext(AddMemberAdapter adapter,
                            String date,
                            final String grouId, String kittyType, String isKittyRule) {
        List<ContactDao> member = adapter.getSelectedList();

        AppLog.d(TAG, "DATE +" + date);

        if (Utils.isValidString(isKittyRule) && isKittyRule.equalsIgnoreCase("1")) {

            //For Normal Kitty
            if (kittyType.equalsIgnoreCase(mActivity.getResources()
                    .getStringArray(R.array.kitty)[2])) {

                /*if (DateTimeUtils.checkCurrentDateIsBeforeKittyDate(date)) {*/

                if (Utils.checkInternetConnection(mActivity)) {
                    mActivity.showProgressDialog();

                    final List<ContactDao> listMember = addMemberList(member);
                    final String groupID = grouId;

                    AppLog.d(TAG, new Gson().toJson(getAddMemberIdsList(listMember)));
                    AppLog.d(TAG, "========" + new Gson().toJson(listMember));

                    AppLog.d(TAG, mActivity.getmDialogId());

                    if (getAddMemberIdsList(listMember) != null && !getAddMemberIdsList(listMember).isEmpty()) {
                        QbDialogUtils.updateQbDialogById(mActivity.getmDialogId(), 1,
                                Utils.convertStringIdListIntoIntegerList(
                                        getAddMemberIdsList(listMember)),
                                null, new QbUpdateDialogListener() {

                                    @Override
                                    public void onSuccessResponce() {

                                        ReqAddMember addMember = new ReqAddMember();
                                        addMember.setGroupId(groupID);
                                        List<MemberData> memberList =
                                                Utils.convertContactDataListIntoMemberDataList(listMember);
                                        addMember.setMember(memberList);
                                        Call<ServerResponse<OfflineDao>> call = Singleton.getInstance()
                                                .getRestOkClient().addMember(addMember);
                                        call.enqueue(addMemberCallBack);

                                    }

                                    @Override
                                    public void onError() {
                                        mActivity.hideProgressDialog();
                                        mActivity.showSnackbar(mActivity.getResources()
                                                .getString(R.string.quick_blox_error_found));
                                    }
                                }, null);
                    } else {
                        ReqAddMember addMember = new ReqAddMember();
                        addMember.setGroupId(groupID);
                        List<MemberData> memberList =
                                Utils.convertContactDataListIntoMemberDataList(listMember);
                        addMember.setMember(memberList);
                        Call<ServerResponse<OfflineDao>> call = Singleton.getInstance()
                                .getRestOkClient().addMember(addMember);
                        call.enqueue(addMemberCallBack);
                    }


                } else {
                    AlertDialogUtils.showSnackToast(
                            mActivity.getResources().getString(R.string.no_internet_available),
                            mActivity);
                }
                /*} else {
                    //openPaidActivity
                    ReqAddMember addMember = new ReqAddMember();
                    addMember.setGroupId(grouId);
                    List<MemberData> memberList =
                            Utils.convertContactDataListIntoMemberDataList(addMemberList(member));
                    addMember.setMember(memberList);
                    Intent i = new Intent(mActivity, PaidActivity.class);
                    i.putExtra(AppConstant.INTENT_PAID_DATA,
                            new Gson().toJson(addMember).toString());

                    i.putExtra(AppConstant.INTENT_DIALOG_ID, mActivity.getmDialogId());

                    mActivity.startActivity(i);

                }*/

                //For Kitty With Kids
            } else if (kittyType.equalsIgnoreCase(mActivity.getResources()
                    .getStringArray(R.array.kitty)[1])) {

                CreateGroup groupObject = new CreateGroup();
                groupObject.setGroupID(grouId);
                groupObject.setGroupMember(addMemberList(member));

                Intent i = new Intent(mActivity, KittyWithKidsActivity.class);
                i.putExtra(AppConstant.INTENT_KITTY_DATA,
                        new Gson().toJson(groupObject).toString());
//                if (DateTimeUtils.checkCurrentDateIsBeforeKittyDate(date)) {
                i.putExtra(AppConstant.INTENT_KITTY_TYPE, 4);
//                } else {
//                    i.putExtra(AppConstant.INTENT_KITTY_TYPE, 5);
//                }
                i.putExtra(AppConstant.INTENT_DIALOG_ID, mActivity.getmDialogId());

                mActivity.startActivity(i);

                // Couple Kitty
            } else {

               /* if (DateTimeUtils.checkCurrentDateIsBeforeKittyDate(date)) {*/
                CreateGroup groupObject = new CreateGroup();
                groupObject.setGroupID(grouId);
                groupObject.setGroupMember(addMemberList(member));
                Intent i = new Intent(mActivity, SelectCoupleActivity.class);
                i.putExtra(AppConstant.INTENT_KITTY_DATA,
                        new Gson().toJson(groupObject).toString());

                i.putExtra(AppConstant.EXTRA_PARTICIPENT_MEMBER,
                        new Gson().toJson(mParticipantMember).toString());

                i.putExtra(AppConstant.INTENT_KITTY_TYPE, 6);

                i.putExtra(AppConstant.INTENT_DIALOG_ID, mActivity.getmDialogId());
                mActivity.startActivity(i);

                /*} else {
                    // open paid activity
                    ReqAddMember addMember = new ReqAddMember();
                    addMember.setGroupId(grouId);
                    List<MemberData> memberList =
                            Utils.convertContactDataListIntoMemberDataList(addMemberList(member));
                    addMember.setMember(memberList);

                    Intent openPaidActivity = new Intent(mActivity, PaidActivity.class);

                    openPaidActivity.putExtra(AppConstant.INTENT_PAID_DATA,
                            new Gson().toJson(addMember).toString());

                    openPaidActivity.putExtra(AppConstant.INTENT_IS_COUPLE, "1");

                    openPaidActivity.putExtra(AppConstant.INTENT_DIALOG_ID,
                            mActivity.getmDialogId());

                    openPaidActivity.putExtra(AppConstant.EXTRA_PARTICIPENT_MEMBER,
                            new Gson().toJson(mParticipantMember).toString());

                    mActivity.startActivity(openPaidActivity);

                }*/
            }
        } else {
            // if no Rule set
            if (Utils.checkInternetConnection(mActivity)) {
                mActivity.showProgressDialog();

                final List<ContactDao> listMember = addMemberList(member);
                final String groupID = grouId;

                AppLog.d(TAG, new Gson().toJson(getAddMemberIdsList(listMember)));
                AppLog.d(TAG, "========" + new Gson().toJson(listMember));

                AppLog.d(TAG, mActivity.getmDialogId());

                if (getAddMemberIdsList(listMember) != null && !getAddMemberIdsList(listMember).isEmpty()) {
                    QbDialogUtils.updateQbDialogById(mActivity.getmDialogId(), 1,
                            Utils.convertStringIdListIntoIntegerList(
                                    getAddMemberIdsList(listMember)),
                            null, new QbUpdateDialogListener() {

                                @Override
                                public void onSuccessResponce() {

                                    ReqAddMember addMember = new ReqAddMember();
                                    addMember.setGroupId(groupID);
                                    List<MemberData> memberList =
                                            Utils.convertContactDataListIntoMemberDataList(listMember);
                                    addMember.setMember(memberList);
                                    Call<ServerResponse<OfflineDao>> call = Singleton.getInstance()
                                            .getRestOkClient().addMember(addMember);
                                    call.enqueue(addMemberCallBack);

                                }

                                @Override
                                public void onError() {
                                    mActivity.hideProgressDialog();
                                    mActivity.showSnackbar(mActivity.getResources()
                                            .getString(R.string.quick_blox_error_found));
                                }
                            }, null);
                } else {
                    ReqAddMember addMember = new ReqAddMember();
                    addMember.setGroupId(groupID);
                    List<MemberData> memberList =
                            Utils.convertContactDataListIntoMemberDataList(listMember);
                    addMember.setMember(memberList);
                    Call<ServerResponse<OfflineDao>> call = Singleton.getInstance()
                            .getRestOkClient().addMember(addMember);
                    call.enqueue(addMemberCallBack);
                }


            } else {
                AlertDialogUtils.showSnackToast(
                        mActivity.getResources().getString(R.string.no_internet_available),
                        mActivity);
            }
        }

    }

    public List<ContactDao> addMemberList(List<ContactDao> list) {
        List<ContactDao> memberList = new ArrayList<>();
        memberList.clear();
        memberList.addAll(list);
        return memberList;
    }

    public void showDialog() {
        mDialog = new ProgressDialog(mActivity);
        mDialog.setMessage(mActivity.getResources().getString(R.string.loading_text));
        mDialog.show();
    }

    public void hideDialog() {
        try {
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss();
                mDialog = null;
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    private Callback<ServerResponse<OfflineDao>> addMemberCallBack =
            new Callback<ServerResponse<OfflineDao>>() {
                @Override
                public void onResponse(Call<ServerResponse<OfflineDao>> call,
                                       Response<ServerResponse<OfflineDao>> response) {
                    mActivity.hideProgressDialog();
                    hideDialog();
                    if (response.code() == 200) {
                        if (response.body().getResponse() == AppConstant.RESPONSE_SUCCESS) {

                            // insert data into db
                            new SqlDataSetTask(mActivity, response.body().getData());

                            AlertDialogUtils.showSnackToast(mActivity
                                            .getResources()
                                            .getString(R.string.add_member_success),
                                    mActivity);
                            mActivity.startActivity(new Intent(mActivity, HomeActivity.class));
                        } else {
                            showServerError(response.body().getMessage());
                        }
                    } else {
                        showServerError(response.body().getMessage());
                    }
                }

                @Override
                public void onFailure(Call<ServerResponse<OfflineDao>> call, Throwable t) {
                    hideDialog();
                    mActivity.hideProgressDialog();
                    AlertDialogUtils.showSnackToast(mActivity.getResources()
                            .getString(R.string.server_error), mActivity);
                }
            };

    private void showServerError(String error) {
        if (Utils.isValidString(error)) {
            AlertDialogUtils.showSnackToast(error, mActivity);
        } else {
            AlertDialogUtils.showSnackToast(mActivity.getResources()
                    .getString(R.string.server_error), mActivity);
        }
    }

    private List<String> getAddMemberIdsList(List<ContactDao> list) {
        List<String> memberIdList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (Utils.isValidString(list.get(i).getID()))
                memberIdList.add(list.get(i).getID());
        }
        return memberIdList;
    }


    private void getAlereadyExitsMemberList() {
        for (int j = 0; j < mParticipantMember.getParticipant().size(); j++) {
            ParticipantMember member = mParticipantMember.getParticipant().get(j);

            if (member.getNumber().contains("-!-")) {

                List<ParticipantMember> members =
                        Utils.seprateCoupleObjectToParticipentMember(member);

                for (int k = 0; k < members.size(); k++) {
                    ParticipantMember coupleSeprateMember = members.get(k);
                    ContactDao dao = new ContactDao();
                    dao.setName(Utils.checkIfMe(mActivity, coupleSeprateMember.getNumber()
                            .replace(AppConstant.SEPERATOR_STRING, ""), coupleSeprateMember.getName()));
                    dao.setPhone(coupleSeprateMember.getNumber().replace(AppConstant.SEPERATOR_STRING, ""));
                    dao.setImage(coupleSeprateMember.getProfile().replace(AppConstant.SEPERATOR_STRING, ""));
                    dao.setAlready(true);
                    mMemberList.add(dao);
                }
            } else {
                ParticipantMember coupleSeprateMember = mParticipantMember.getParticipant().get(j);
                ContactDao dao = new ContactDao();
                dao.setName(Utils.checkIfMe(mActivity, coupleSeprateMember.getNumber(), coupleSeprateMember.getName()));
                dao.setPhone(coupleSeprateMember.getNumber());
                dao.setImage(coupleSeprateMember.getProfile());
                dao.setAlready(true);
                mMemberList.add(dao);
            }
        }
        mAleradyExistMemberList.addAll(mMemberList);
        new AddMemberTask().execute();
    }

}
