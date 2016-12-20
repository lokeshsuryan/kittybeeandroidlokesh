package com.kittyapplication.ui.viewmodel;

import android.content.Intent;

import com.google.gson.Gson;
import com.kittyapplication.R;
import com.kittyapplication.adapter.ChangeHostAdapter;
import com.kittyapplication.model.ChangeHostDao;
import com.kittyapplication.model.ParticipantDao;
import com.kittyapplication.model.ParticipantMember;
import com.kittyapplication.ui.activity.ChangeHostActivity;
import com.kittyapplication.ui.activity.SelectChangeHostActivity;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HalfBloodPrince(RIONTECH)
 * Riontech on 19/9/16.
 */
public class ChangeHostViewModel {
    private static final String TAG = ChangeHostViewModel.class.getSimpleName();
    private ChangeHostActivity mActivity;
    private ParticipantDao mParticipantDao;
    private List<ParticipantMember> mPendingMeberList;
    private String mGroupId;


    public ChangeHostViewModel(ChangeHostActivity activity) {
        mActivity = activity;
    }

    public void getData(String noOfHost, String participantData, String groupId) {
        if (Utils.isValidString(participantData) && Utils.isValidString(noOfHost)) {
            AppLog.d(TAG, "GroupId = " + groupId);
            mGroupId = groupId;
            mActivity.showLayout();
            mParticipantDao = new Gson().fromJson(participantData, ParticipantDao.class);
            try {
                int hostNo = Integer.parseInt(noOfHost);
                getHostList(hostNo);
            } catch (Exception e) {
                AppLog.e(TAG, e.getMessage(), e);
            }

        } else {
            mActivity.showSnackbar(mActivity.getResources().getString(R.string.server_error));
            mActivity.hideLayout();
        }
    }

    private void getHostList(int noHost) {
        if (Utils.isValidList(mParticipantDao.getParticipant())) {
            List<ParticipantMember> listMember = new ArrayList<>();
            mPendingMeberList = new ArrayList<>();
            for (int i = 0; i < mParticipantDao.getParticipant().size(); i++) {
                ParticipantMember member = mParticipantDao.getParticipant().get(i);
                if (member.getNumber().contains("-!-")) {
                    if (checkCoupleDataIsCurrentHost(member))
                        listMember.add(member);
                    else if (checkCoupleDataIsAlereadyHosted(member))
                        mPendingMeberList.add(member);
                } else if (member.getCurrentHost().equalsIgnoreCase("1")) {
                    listMember.add(member);
                } else if (member.getHost().equalsIgnoreCase("0")
                        && member.getCurrentHost().equalsIgnoreCase("0"))
                    mPendingMeberList.add(member);
            }
            mActivity.setListData(listMember, noHost);
        } else {
            mActivity.showSnackbar(mActivity.getResources().getString(R.string.server_error));
            mActivity.hideLayout();
        }
    }

    public void sumbitData(ChangeHostAdapter adapter) {
        if (Utils.isValidList(adapter.getSelectedList())
                && adapter.getSelectedList().size() > 0) {

            ChangeHostDao changeHostDao = new ChangeHostDao();
            changeHostDao.setCurrentHostList(adapter.getSelectedList());
            changeHostDao.setNewHostList(mPendingMeberList);

            Intent intent = new Intent(mActivity, SelectChangeHostActivity.class);
            intent.putExtra(AppConstant.INTENT_CHANGE_HOST,
                    new Gson().toJson(changeHostDao).toString());
            intent.putExtra(AppConstant.GROUP_ID, mGroupId);
            mActivity.startActivity(intent);

        } else {
            mActivity.showSnackbar(mActivity.getResources().getString(R.string.change_host_no_member));
        }
    }

    private boolean checkCoupleDataIsCurrentHost(ParticipantMember member) {
        boolean flag = false;
        String[] currentHost = member.getCurrentHost().split(AppConstant.SEPERATOR_STRING);
        if (currentHost != null && currentHost.length > 0)
            for (int j = 0; j < 2; j++) {
                String currentHostStr = Utils.getStringFromArray(j, currentHost);
                if (currentHostStr.replace(AppConstant.SEPERATOR_STRING, "").equalsIgnoreCase("1")) {
                    flag = true;
                    break;
                } else {
                    flag = false;
                }
            }
        return flag;
    }

    private boolean checkCoupleDataIsAlereadyHosted(ParticipantMember member) {
        boolean flag = false;
        String[] hostArray = member.getHost().split(AppConstant.SEPERATOR_STRING);
        String[] currentHostArray = member.getCurrentHost().split(AppConstant.SEPERATOR_STRING);
        if (hostArray != null
                && hostArray.length > 0
                && currentHostArray != null
                && currentHostArray.length > 0)
            for (int j = 0; j < hostArray.length; j++) {
                String isHost = Utils.getStringFromArray(j, hostArray);
                String isCurrentHost = Utils.getStringFromArray(j, currentHostArray);
                if (isHost.replace(AppConstant.SEPERATOR_STRING, "").equalsIgnoreCase("0")
                        && isCurrentHost.replace(AppConstant.SEPERATOR_STRING, "").equalsIgnoreCase("0")) {
                    flag = true;
                } else {
                    return false;
                }
            }
        return flag;
    }
}
