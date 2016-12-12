package com.kittyapplication.ui.viewmodel;

import android.content.Intent;

import com.google.gson.Gson;
import com.kittyapplication.AppApplication;
import com.kittyapplication.R;
import com.kittyapplication.adapter.DeleteMemberAdapter;
import com.kittyapplication.chat.utils.chat.QbUpdateDialogListener;
import com.kittyapplication.chat.utils.qb.QbDialogUtils;
import com.kittyapplication.model.OfflineDao;
import com.kittyapplication.model.ParticipantDao;
import com.kittyapplication.model.ParticipantMember;
import com.kittyapplication.model.ReqDeleteMember;
import com.kittyapplication.model.ServerResponse;
import com.kittyapplication.rest.Singleton;
import com.kittyapplication.sqlitedb.SqlDataSetTask;
import com.kittyapplication.ui.activity.DeleteMemberActivity;
import com.kittyapplication.ui.activity.SettingActivity;
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
 * Created by Pintu Riontech on 18/8/16.
 * vaghela.pintu31@gmail.com
 */
public class DeleteMemberViewModel {
    public static final String TAG = DeleteMemberViewModel.class.getSimpleName();
    private DeleteMemberActivity mActivity;

    public DeleteMemberViewModel(DeleteMemberActivity activity) {
        mActivity = activity;
    }

    public void getData(String data, String kittyDate) {
        List<ParticipantMember> list = new Gson().fromJson(data, ParticipantDao.class)
                .getParticipant();

        AppLog.d(TAG, "Kitty Date = " + kittyDate);
        if (Utils.isValidList(list)) {
            mActivity.setDataIntoList(removeCurrentHostParticipant(list));
        } else {
            List<ParticipantMember> dummyList = new ArrayList<>();
            mActivity.setDataIntoList(dummyList);
        }
        /*if (DateTimeUtils.getServerDateIsAfterCurrentDate(kittyDate)) {
            List<ParticipantMember> members = getHostedMember(list);
            AppLog.d(TAG, new Gson().toJson(members));
            AppLog.d(TAG, "ashbdkjnas" + new Gson().toJson(removeCurrentHostParticipant(members)));
            mActivity.setDataIntoList(removeCurrentHostParticipant(members));
        } else {
            mActivity.setDataIntoList(removeCurrentHostParticipant(getNotHostedMember(list)));
        }*/
    }

    private List<ParticipantMember> getHostedMember(List<ParticipantMember> list) {
        List<ParticipantMember> hostedMemberList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {

            if (list.get(i).getNumber().contains("-!-")) {

                List<ParticipantMember> members =
                        Utils.seprateCoupleObjectToParticipentMember(list.get(i));

                AppLog.d(TAG, "Seprate" + new Gson().toJson(members));

                for (int k = 0; k < members.size(); k++) {
                    if (members.get(k).getHost().equalsIgnoreCase("1")) {
                        hostedMemberList.add(list.get(i));
                        break;
                    }
                }
            } else {
                if (list.get(i).getHost().equalsIgnoreCase("1"))
                    hostedMemberList.add(list.get(i));
            }

        }
        return hostedMemberList;
    }

    private List<ParticipantMember> getNotHostedMember(List<ParticipantMember> list) {
        List<ParticipantMember> noHostKittyMember = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {

            if (list.get(i).getNumber().contains("-!-")) {

                List<ParticipantMember> members =
                        Utils.seprateCoupleObjectToParticipentMember(list.get(i));

                for (int k = 0; k < members.size(); k++) {
                    if (members.get(k).getHost().equalsIgnoreCase("0")) {
                        noHostKittyMember.add(list.get(i));
                        break;
                    }
                }
            } else {
                if (list.get(i).getHost().equalsIgnoreCase("0"))
                    noHostKittyMember.add(list.get(i));
            }

        }
        return noHostKittyMember;
    }

    public void submitDataToServer(DeleteMemberAdapter adapter, final String groupId, final String kittyID) {
        if (Utils.checkInternetConnection(mActivity)) {
            mActivity.showProgressDialog();
            final List<String> memberList = getChatIdListDeleteIdList(
                    adapter.getSelectedList());

            final List<String> serverList = getDeleteIdList(
                    adapter.getSelectedList());

            if (memberList != null && !memberList.isEmpty()) {

                QbDialogUtils.updateQbDialogById(mActivity.getmDialogId(), 2,
                        Utils.convertStringIdListIntoIntegerList(memberList),
                        null, new QbUpdateDialogListener() {

                            @Override
                            public void onSuccessResponse(QBChatDialog dialog)  {
                                AppApplication.getInstance().setRefresh(true);
                                ReqDeleteMember deleteMemeber = new ReqDeleteMember();
                                deleteMemeber.setGroupId(groupId);
                                deleteMemeber.setKittyId(kittyID);
                                deleteMemeber.setMemberId(serverList);
                                Call<ServerResponse<OfflineDao>> call = Singleton.getInstance()
                                        .getRestOkClient().deleteMember(deleteMemeber);
                                call.enqueue(deleteMemberCallBack);

                            }

                            @Override
                            public void onError() {
                                mActivity.hideProgressDialog();
//                                mActivity.showSnackbar(mActivity.getResources()
//                                        .getString(R.string.quick_blox_error_found));
                            }
                        }, null);
            } else {
                if (serverList != null && !serverList.isEmpty()) {
                    AppApplication.getInstance().setRefresh(true);
                    ReqDeleteMember deleteMemeber = new ReqDeleteMember();
                    deleteMemeber.setGroupId(groupId);
                    deleteMemeber.setKittyId(kittyID);
                    deleteMemeber.setMemberId(serverList);
                    Call<ServerResponse<OfflineDao>> call = Singleton.getInstance()
                            .getRestOkClient().deleteMember(deleteMemeber);
                    call.enqueue(deleteMemberCallBack);
                }
            }


        } else {
            mActivity.showSnackbar(mActivity.getResources()
                    .getString(R.string.no_internet_available));
        }
    }


    private List<String> getDeleteIdList(List<ParticipantMember> list) {
        List<String> memberIdList = new ArrayList<>();
       /* for (int i = 0; i < list.size(); i++) {
            if (Utils.isValidString(list.get(i).getId()))
                memberIdList.add(list.get(i).getId());
        }*/
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getNumber().contains("-!-")) {
                List<ParticipantMember> members =
                        Utils.seprateCoupleObjectToParticipentMember(list.get(i));

                for (int k = 0; k < members.size(); k++) {

                    if (Utils.isValidString(list.get(i).getId())
                            && !memberIdList.contains(list.get(i).getId())) {

                        memberIdList.add(members.get(k).getId());
                    }
                }
            } else {
                if (Utils.isValidString(list.get(i).getId()))
                    memberIdList.add(list.get(i).getId());
            }
        }

        return memberIdList;
    }

    private List<String> getChatIdListDeleteIdList(List<ParticipantMember> list) {
        List<String> memberIdList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getNumber().contains("-!-")) {
                List<ParticipantMember> members =
                        Utils.seprateCoupleObjectToParticipentMember(list.get(i));
                for (int k = 0; k < members.size(); k++) {
                    if (Utils.isValidString(list.get(i).getChatId())
                            && !memberIdList.contains(list.get(i).getChatId())) {
                        memberIdList.add(members.get(k).getChatId());
                    }
                }
            } else {
                if (Utils.isValidString(list.get(i).getChatId()))
                    memberIdList.add(list.get(i).getChatId());
            }
        }
        return memberIdList;
    }

    /**
     *
     */
    private Callback<ServerResponse<OfflineDao>> deleteMemberCallBack = new Callback<ServerResponse<OfflineDao>>() {
        @Override
        public void onResponse(Call<ServerResponse<OfflineDao>> call,
                               Response<ServerResponse<OfflineDao>> response) {
            mActivity.hideProgressDialog();
            String message = response.body().getMessage();
            boolean flag = false;
            if (response.code() == 200) {
                if (response.body().getResponse() == AppConstant.RESPONSE_SUCCESS) {

                    // call db update
                    SqlDataSetTask dbEntryTask = new SqlDataSetTask(mActivity,
                            response.body().getData());
                    AlertDialogUtils.showSnackToast(mActivity.getResources()
                                    .getString(R.string.delete_member_sucess),
                            mActivity);
                    mActivity.startActivity(new Intent(mActivity, SettingActivity.class));
                    mActivity.finish();
                } else {
                    flag = true;
                }
            } else {
                flag = true;
            }

            if (flag) {
                AppApplication.getInstance().setRefresh(false);
                AlertDialogUtils.showSnackToast(mActivity.getResources()
                                .getString(R.string.server_error),
                        mActivity);
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


    private List<ParticipantMember> removeCurrentHostParticipant(List<ParticipantMember> list) {
        List<ParticipantMember> membersList = new ArrayList<>();
        List<ParticipantMember> posList = new ArrayList<>();
        membersList.addAll(list);
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getNumber().contains("-!-")) {
                List<ParticipantMember> participantMembers =
                        Utils.seprateCoupleObjectToParticipentMember(list.get(i));
                if (Utils.isValidList(participantMembers)) {
                    for (int j = 0; j < participantMembers.size(); j++) {
                        // remove current host members
                        if (participantMembers.get(j).getCurrentHost().equalsIgnoreCase("1")) {
                            posList.add(list.get(i));
                            break;
                            // remove admin members
                        } else if (participantMembers.get(j).getIsAdmin().equalsIgnoreCase("1")) {
                            posList.add(list.get(i));
                            break;
                        }
                    }
                }
            } else {
                // remove current host members
                if (list.get(i).getCurrentHost().equalsIgnoreCase("1")) {
                    posList.add(list.get(i));
                    // remove   admin member
                } else if (list.get(i).getIsAdmin().equalsIgnoreCase("1")) {
                    posList.add(list.get(i));
                }
            }
        }
        AppLog.d(TAG, new Gson().toJson(posList));
        // Remove current host or admin members
        if (Utils.isValidList(membersList) && Utils.isValidList(posList)) {
            for (int i = 0; i < posList.size(); i++) {
                membersList.remove(posList.get(i));
            }
        }

        return membersList;
    }
}
