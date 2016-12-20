package com.kittyapplication.ui.viewmodel;

import android.app.ProgressDialog;
import android.content.Intent;

import com.google.gson.Gson;
import com.kittyapplication.AppApplication;
import com.kittyapplication.R;
import com.kittyapplication.adapter.PaidAdapter;
import com.kittyapplication.chat.utils.chat.QbUpdateDialogListener;
import com.kittyapplication.chat.utils.qb.QbDialogUtils;
import com.kittyapplication.model.ContactDao;
import com.kittyapplication.model.CreateGroup;
import com.kittyapplication.model.MemberData;
import com.kittyapplication.model.OfflineDao;
import com.kittyapplication.model.ReqAddMember;
import com.kittyapplication.model.ServerResponse;
import com.kittyapplication.rest.Singleton;
import com.kittyapplication.sqlitedb.SqlDataSetTask;
import com.kittyapplication.ui.activity.HomeActivity;
import com.kittyapplication.ui.activity.PaidActivity;
import com.kittyapplication.ui.activity.SelectCoupleActivity;
import com.kittyapplication.utils.AlertDialogUtils;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.Utils;
import com.quickblox.chat.model.QBChatDialog;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Pintu Riontech on 19/8/16.
 * vaghela.pintu31@gmail.com
 */
public class PaidViewModel {
    private static final String TAG = PaidViewModel.class.getSimpleName();
    private PaidActivity mActivity;
    private ProgressDialog mDialog;
    private ReqAddMember mMemberData;


    public PaidViewModel(PaidActivity activity) {
        mActivity = activity;
    }

    public void getData(String data) {
        mMemberData = new Gson().fromJson(data, ReqAddMember.class);
        AppLog.d(TAG, "Paid data" + data);
        mActivity.setDataIntoList(mMemberData.getMember());
    }

    public void ClickOnNext(PaidAdapter adapter, int isFromCouple) {
        if (isFromCouple == 0) {
            List<MemberData> member = adapter.getListData();
            AppLog.d(TAG, new Gson().toJson(member).toString());
            if (Utils.checkInternetConnection(mActivity)) {
                mActivity.showProgressDialog();
                final List<MemberData> memberData = adapter.getListData();

                if (getAddMemberIdsList(memberData) != null
                        && !getAddMemberIdsList(memberData).isEmpty()) {

                    QbDialogUtils.updateQbDialogById(mActivity.getmDialogId(), 1,
                            Utils.convertStringIdListIntoIntegerList(
                                    getAddMemberIdsList(memberData)),
                            null, new QbUpdateDialogListener() {

                                @Override
                                public void onSuccessResponse(QBChatDialog dialog)  {
                                    AppApplication.getInstance().setRefresh(true);
                                    ReqAddMember addMember = new ReqAddMember();
                                    addMember.setGroupId(mMemberData.getGroupId());
                                    addMember.setMember(memberData);
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
                    AppApplication.getInstance().setRefresh(true);
                    ReqAddMember addMember = new ReqAddMember();
                    addMember.setGroupId(mMemberData.getGroupId());
                    addMember.setMember(memberData);
                    Call<ServerResponse<OfflineDao>> call = Singleton.getInstance()
                            .getRestOkClient().addMember(addMember);
                    call.enqueue(addMemberCallBack);
                }


            } else {
                AlertDialogUtils.showSnackToast(
                        mActivity.getResources().getString(R.string.no_internet_available),
                        mActivity);
            }
        } else {

            CreateGroup group = new CreateGroup();
            group.setGroupID(mMemberData.getGroupId());
            group.setGroupMember(convertMemberDataListIntoGroupMemberList(mMemberData.getMember()));

            Intent i = new Intent(mActivity, SelectCoupleActivity.class);
            i.putExtra(AppConstant.INTENT_KITTY_DATA,
                    new Gson().toJson(group).toString());
            i.putExtra(AppConstant.INTENT_KITTY_TYPE, 6);
            i.putExtra(AppConstant.INTENT_DIALOG_ID, mActivity.getmDialogId());
            i.putExtra(AppConstant.EXTRA_PARTICIPENT_MEMBER, mActivity.getmParticipant());
            mActivity.startActivity(i);

        }
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
                    if (response.code() == 200) {
                        if (response.body().getResponse() == AppConstant.RESPONSE_SUCCESS) {

                            // insert data into db
                            new SqlDataSetTask(mActivity, response.body().getData());

                            AlertDialogUtils.showSnackToast(mActivity
                                            .getResources()
                                            .getString(R.string.add_member_success),
                                    mActivity);
                            mActivity.startActivity(new Intent(mActivity, HomeActivity.class));
                            mActivity.finish();
                        } else {
                            AppApplication.getInstance().setRefresh(false);
                            showServerError(response.body().getMessage());
                        }
                    } else {
                        AppApplication.getInstance().setRefresh(false);
                        showServerError(response.body().getMessage());
                    }
                }

                @Override
                public void onFailure(Call<ServerResponse<OfflineDao>> call, Throwable t) {
                    AppApplication.getInstance().setRefresh(false);
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


    private List<ContactDao> convertMemberDataListIntoGroupMemberList(List<MemberData> list) {
        List<ContactDao> contactList = new ArrayList<>();
        if (list != null && !list.isEmpty()) {
            for (int i = 0; i < list.size(); i++) {
                ContactDao dao = new ContactDao();
                dao.setName(list.get(i).getName());
                dao.setImage(list.get(i).getImage());
                dao.setIs_Paid(list.get(i).getIsPaid());
                dao.setPhone(list.get(i).getPhone());
                dao.setUserId(list.get(i).getUserid());
                dao.setRegistration(list.get(i).getRegistration());
                dao.setStatus(list.get(i).getStatus());
                dao.setID(list.get(i).getId());
                contactList.add(dao);
            }
        }
        return contactList;
    }


    private List<String> getAddMemberIdsList(List<MemberData> list) {
        List<String> memberIdList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (Utils.isValidString(list.get(i).getId()))
                memberIdList.add(list.get(i).getId());
        }
        return memberIdList;
    }
}
