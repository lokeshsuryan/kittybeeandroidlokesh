package com.kittyapplication.ui.viewmodel;

import android.content.Intent;
import android.widget.ListView;

import com.google.gson.Gson;
import com.kittyapplication.AppApplication;
import com.kittyapplication.R;
import com.kittyapplication.adapter.KittyWithKidsAdapter;
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
import com.kittyapplication.ui.activity.KittyWithKidsActivity;
import com.kittyapplication.ui.activity.PaidActivity;
import com.kittyapplication.ui.activity.RuleActivity;
import com.kittyapplication.utils.AlertDialogUtils;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Pintu Riontech on 12/8/16.
 * vaghela.pintu31@gmail.com
 */
public class KittyWithKidsViewModel {
    private static final String TAG = KittyWithKidsViewModel.class.getSimpleName();
    private KittyWithKidsActivity mActivity;
    private CreateGroup mGroupData;


    public KittyWithKidsViewModel(KittyWithKidsActivity activity) {
        mActivity = activity;
    }

    public void getData(String str) {
        mGroupData = new Gson().fromJson(str, CreateGroup.class);
        mActivity.setDataIntoList(mGroupData.getGroupMember());
    }


    //kitty TYPE 0 (COUPLE KITTY)
    //kitty TYPE 1 (KITTY WITH KIDS)
    //kitty TYPE 2 (NORMAL KITTY)
    //kitty TYPE 4 (COMING KITTY FROM ADD MEMBER WHICH IS KITTY WITH KIDS WITHOUT PAID)
    //kitty TYPE 5 (COMING KITTY FROM ADD MEMBER WHICH IS KITTY WITH KIDS WITH PAID)
    //kitty TYPE 6 (COMING KITTY FROM ADD MEMBER WHICH IS COUPLE KITTY)


    public void onClickNextButton(ListView lv, int type) {


        if (isValidKidsMemberData(((KittyWithKidsAdapter) lv.getAdapter()).getListData())) {
            mGroupData.setGroupMember(((KittyWithKidsAdapter) lv.getAdapter()).getListData());

            if (type == 4) {
                //kitty Not started added member
                if (Utils.checkInternetConnection(mActivity)) {
                    mActivity.showProgressDialog();

                    List<Integer> idsList = Utils.convertStringIdListIntoIntegerList(
                            getAddMemberIdsList(mGroupData.getGroupMember()));

                    if (idsList != null && !idsList.isEmpty()) {

                        QbDialogUtils.updateQbDialogById(mActivity.getmDialogId(), 1,
                                idsList,
                                null, new QbUpdateDialogListener() {

                                    @Override
                                    public void onSuccessResponce() {
                                        AppApplication.getInstance().setRefresh(true);
                                        ReqAddMember addMember = new ReqAddMember();
                                        addMember.setGroupId(mGroupData.getGroupID());
                                        List<MemberData> memberList =
                                                Utils.convertContactDataListIntoMemberDataList
                                                        (mGroupData.getGroupMember());
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
                        AppApplication.getInstance().setRefresh(true);
                        ReqAddMember addMember = new ReqAddMember();
                        addMember.setGroupId(mGroupData.getGroupID());
                        List<MemberData> memberList =
                                Utils.convertContactDataListIntoMemberDataList
                                        (mGroupData.getGroupMember());
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

            } else if (type == 5)

            {
                //kitty started than added member
                ReqAddMember addMember = new ReqAddMember();
                addMember.setGroupId(mGroupData.getGroupID());
                List<MemberData> memberList =
                        Utils.convertContactDataListIntoMemberDataList(mGroupData.getGroupMember());
                addMember.setMember(memberList);
                Intent i = new Intent(mActivity, PaidActivity.class);
                i.putExtra(AppConstant.INTENT_DIALOG_ID, mActivity.getmDialogId());
                i.putExtra(AppConstant.INTENT_PAID_DATA,
                        new Gson().toJson(addMember).toString());
                mActivity.startActivity(i);

            } else

            {
                AppLog.d(TAG, "Data = " + new Gson().toJson(mGroupData).toString());
                Intent intent = new Intent(mActivity, RuleActivity.class);
                intent.putExtra(AppConstant.INTENT_KITTY_DATA, new Gson().toJson(mGroupData).toString());
                intent.putExtra(AppConstant.INTENT_KITTY_TYPE, type);
                if (mActivity.getmIsCreateKitty()) {
                    intent.putExtra(AppConstant.EXTRA_IS_CREATE_KITTY, 1);
                }
                mActivity.startActivity(intent);
            }
        } else {
            mActivity.showSnackbar(mActivity.getResources().getString(R.string.kids_warning));
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

    private List<String> getAddMemberIdsList(List<ContactDao> list) {
        List<String> memberIdList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (Utils.isValidString(list.get(i).getID()))
                memberIdList.add(list.get(i).getID());
        }
        return memberIdList;
    }


    private boolean isValidKidsMemberData(List<ContactDao> list) {
        AppLog.d(TAG, "kids " + new Gson().toJson(list).toString());
        boolean isValid = true;
        if (list != null && !list.isEmpty()) {
            for (int i = 0; i < list.size(); i++) {
                String kidsCount = list.get(i).getKids();
                if (kidsCount == null
                        || kidsCount.length() == 0
                        || kidsCount.equalsIgnoreCase("0")) {
                    isValid = false;
                    break;
                }
            }
        }
        return isValid;
    }
}
