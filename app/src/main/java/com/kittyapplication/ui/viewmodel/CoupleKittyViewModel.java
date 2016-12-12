package com.kittyapplication.ui.viewmodel;

import android.content.Intent;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.kittyapplication.AppApplication;
import com.kittyapplication.R;
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
import com.kittyapplication.ui.activity.CoupleKittyCreateActivity;
import com.kittyapplication.ui.activity.HomeActivity;
import com.kittyapplication.ui.activity.PaidActivity;
import com.kittyapplication.ui.activity.RuleActivity;
import com.kittyapplication.ui.activity.SelectHostActivity;
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
 * Created by Pintu Riontech on 15/8/16.
 * vaghela.pintu31@gmail.com
 */
public class CoupleKittyViewModel {
    private static final String TAG = CoupleKittyViewModel.class.getSimpleName();
    private CoupleKittyCreateActivity mActivity;
    private CreateGroup mGroupData;

    public CoupleKittyViewModel(CoupleKittyCreateActivity activity) {
        mActivity = activity;
    }

    public void setData(String data) {
        mGroupData = new Gson().fromJson(data, CreateGroup.class);
        AppLog.d(TAG, new Gson().toJson(mGroupData.getGroupMember()).toString());
        mActivity.setCreateCoupleDataList(mGroupData.getGroupMember(), false);
    }


    //kitty TYPE 0 (COUPLE KITTY)
    //kitty TYPE 1 (KITTY WITH KIDS)
    //kitty TYPE 2 (NORMAL KITTY)
    //kitty TYPE 4 (COMING KITTY FROM ADD MEMBER WHICH IS KITTY WITH KIDS WITHOUT PAID)
    //kitty TYPE 5 (COMING KITTY FROM ADD MEMBER WHICH IS KITTY WITH KIDS WITH PAID)
    //kitty TYPE 6 (COMING KITTY FROM ADD MEMBER WHICH IS COUPLE KITTY)


    public void getDataList(List<ContactDao> list, int type, boolean isHost) {
        mGroupData.setGroupMember(list);
        AppLog.d(TAG, "Data = " + new Gson().toJson(mGroupData).toString());
        if (type == 0) {
            if (!isHost) {
                Intent intent = new Intent(mActivity, RuleActivity.class);
                intent.putExtra(AppConstant.INTENT_KITTY_DATA,
                        new Gson().toJson(mGroupData).toString());

                intent.putExtra(AppConstant.INTENT_KITTY_TYPE, type);
                mActivity.startActivity(intent);
            } else if (isHost) {
                Intent intent = new Intent(mActivity, SelectHostActivity.class);

                intent.putExtra(AppConstant.INTENT_KITTY_DATA,
                        new Gson().toJson(mGroupData).toString());
                intent.putExtra(AppConstant.INTENT_KITTY_TYPE, type);

                intent.putExtra(AppConstant.INTENT_KITTY_IS_COUPLE_HOST, 0);
                mActivity.startActivity(intent);
            }
        } else if (type == 6) {

            //for add member
            if (Utils.checkInternetConnection(mActivity)) {
                new GetDialogIDTask().execute();
            } else {
                AlertDialogUtils.showSnackToast(
                        mActivity.getResources().getString(R.string.no_internet_available),
                        mActivity);
            }
        } else if (type == 7) {

            //Not Use Now  Deleted type 7 not using
            //for open paid activity
            ReqAddMember addMember = new ReqAddMember();
            addMember.setGroupId(mGroupData.getGroupID());
            List<MemberData> memberList =
                    Utils.convertContactDataListIntoMemberDataList(mGroupData.getGroupMember());
            addMember.setMember(memberList);
            Intent i = new Intent(mActivity, PaidActivity.class);
            i.putExtra(AppConstant.INTENT_PAID_DATA,
                    new Gson().toJson(addMember).toString());
            mActivity.startActivity(i);
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

    private List<ContactDao> getAddMemberIdsList(List<ContactDao> list) {
        List<ContactDao> seprateList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getPhone().contains("-!-")) {
                String[] couple = list.get(i).getName().split("-!-");
                String[] images = list.get(i).getImage().split("-!-");
                String[] phone = list.get(i).getPhone().split("-!-");
                String[] fullname = list.get(i).getFullName().split("-!-");
                String[] registration = list.get(i).getRegistration().split("-!-");
                String[] status = list.get(i).getStatus().split("-!-");
                String[] id = list.get(i).getID().split("-!-");

                for (int j = 0; j < 2; i++) {
                    ContactDao object = new ContactDao();
                    object.setImage(getStringFromArray(j, images));
                    object.setName(getStringFromArray(j, couple));
                    object.setPhone(getStringFromArray(j, phone));
                    object.setStatus(getStringFromArray(j, status));
                    object.setRegistration(getStringFromArray(j, registration));
                    object.setFullName(getStringFromArray(j, fullname));
                    object.setID(getStringFromArray(j, id));
                    seprateList.add(object);
                }

            } else {
                seprateList.add(list.get(i));
            }
        }
        AppLog.d(TAG, new Gson().toJson(seprateList).toString());
        return seprateList;
    }


    public static List<Integer> convertStringIdListIntoIntegerList(List<String> list) {
        List<Integer> listId = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (Utils.isValidString(list.get(i)))
                listId.add(Integer.valueOf(list.get(i)));
        }
        return listId;
    }

    private static String getStringFromArray(int pos, String[] array) {
        String str = "";
        try {
            if (array[pos] != null && array[pos].length() > 0) {
                str = array[pos];
            } else {
                str = "";
            }
        } catch (Exception e) {
            str = "";
        }
        return str;
    }

    private List<String> getIDsFromArray(List<ContactDao> list) {
        List<String> memberIdList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (Utils.isValidString(list.get(i).getID()))
                memberIdList.add(list.get(i).getID());
        }
        AppLog.d(TAG, new Gson().toJson(memberIdList).toString());
        return memberIdList;
    }


    /**
     *
     */
    private class GetDialogIDTask extends AsyncTask<Void, Void, List<Integer>> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mActivity.showProgressDialog();
        }

        @Override
        protected List<Integer> doInBackground(Void... params) {
            String cs = "-!-";
            List<ContactDao> list = mGroupData.getGroupMember();
            List<Integer> seperateList = new ArrayList<>();

            if (list != null && !list.isEmpty()) {
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).getPhone().contains(cs)) {
                        String[] id = list.get(i).getID().split(cs);
                        if (id.length > 0) {
                            for (int j = 0; j < id.length; j++) {
                                if (id[j].toString().length() > 0)
                                    seperateList.add(Integer.valueOf(id[j]));
                            }
                        }
                    } else {
                        if (list.get(i).getID().length() > 0)
                            seperateList.add(Integer.valueOf(list.get(i).getID()));
                    }
                }
            }
            return seperateList;
        }

        @Override
        protected void onPostExecute(List<Integer> list) {
            super.onPostExecute(list);
            AppLog.e(TAG, "integer list size = " + list.size());
            if (list != null
                    && !list.isEmpty()) {
                QbDialogUtils.updateQbDialogById(mActivity.getmDialogId(), 1,
                        list, null, new QbUpdateDialogListener() {

                            @Override
                            public void onSuccessResponse(QBChatDialog dialog)  {
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
        }
    }
}
