package com.kittyapplication.ui.viewmodel;

import com.google.gson.Gson;
import com.kittyapplication.R;
import com.kittyapplication.adapter.GiveRightAdapter;
import com.kittyapplication.model.ParticipantDao;
import com.kittyapplication.model.ParticipantMember;
import com.kittyapplication.model.ReqGiveRights;
import com.kittyapplication.model.ServerResponse;
import com.kittyapplication.rest.Singleton;
import com.kittyapplication.ui.activity.GiveRightToEditActivity;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.PreferanceUtils;
import com.kittyapplication.utils.Utils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Pintu Riontech on 24/8/16.
 * vaghela.pintu31@gmail.com
 */
public class GiveRightToEditViewModel {
    private static final String TAG = GiveRightToEditViewModel.class.getSimpleName();
    private GiveRightToEditActivity mActivity;

    public GiveRightToEditViewModel(GiveRightToEditActivity activity) {
        mActivity = activity;
    }

    public void getData(String data) {
        ParticipantDao dao = new Gson().fromJson(data, ParticipantDao.class);
        List<ParticipantMember> dataList = dao.getParticipant();
        if (dataList != null && !dataList.isEmpty()) {
            mActivity.showView();
            mActivity.setDataIntoList(dataList);
        } else {
            mActivity.hideView();
        }
    }

    public void submitDataToServer(GiveRightAdapter adapter, String kittyId) {
        if (adapter.getSelectedList() != null
                && !adapter.getSelectedList().isEmpty()) {

            String rightToId = adapter.getSelectedList().get(0).getUserId();
            ReqGiveRights reqGiveRights = new ReqGiveRights();
            reqGiveRights.setTo(rightToId);
            reqGiveRights.setFrom(PreferanceUtils.getLoginUserObject(mActivity).getUserID());
            reqGiveRights.setKittyId(kittyId);
            if (Utils.checkInternetConnection(mActivity)) {
                mActivity.showProgressDialog();
                Call<ServerResponse> call = Singleton.getInstance()
                        .getRestOkClient().giveRights(reqGiveRights);
                call.enqueue(getParticipantCallBack);
            } else {
                mActivity.showSnackbar(mActivity.getResources().getString(R.string.no_internet_available));
            }
            
        } else {
            mActivity.showSnackbar(mActivity.getResources().getString(R.string.give_rights_warning));
        }


    }

    private Callback<ServerResponse> getParticipantCallBack =
            new Callback<ServerResponse>() {
                @Override
                public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                    mActivity.hideProgressDialog();
                    if (response.code() == 200) {
                        if (response.body().getResponse() == AppConstant.RESPONSE_SUCCESS) {
                            showServerError(response.body().getMessage());
                            mActivity.onBackPressed();
                        } else {
                            showServerError(response.body().getMessage());
                        }
                    } else {
                        showServerError(response.body().getMessage());
                    }
                }

                @Override
                public void onFailure(Call<ServerResponse> call, Throwable t) {
                    mActivity.hideProgressDialog();
                    mActivity.showSnackbar(mActivity.getResources().getString(R.string.server_error));
                }
            };

    private void showServerError(String error) {
        if (Utils.isValidString(error)) {
            mActivity.showSnackbar(error);
        } else {
            mActivity.showSnackbar(mActivity.getResources().getString(R.string.server_error));
        }
    }
}
