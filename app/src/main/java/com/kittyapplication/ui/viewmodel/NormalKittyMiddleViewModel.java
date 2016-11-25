package com.kittyapplication.ui.viewmodel;

import android.content.Intent;
import android.widget.ListView;

import com.google.gson.Gson;
import com.kittyapplication.R;
import com.kittyapplication.adapter.AddGroupContactAdapter;
import com.kittyapplication.model.CreateGroup;
import com.kittyapplication.ui.activity.KittyWithKidsActivity;
import com.kittyapplication.ui.activity.RuleActivity;
import com.kittyapplication.ui.activity.SelectHostActivity;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;

/**
 * Created by Pintu Riontech on 11/8/16.
 * vaghela.pintu31@gmail.com
 */
public class NormalKittyMiddleViewModel {
    private static final String TAG = NormalKittyMiddleViewModel.class.getSimpleName();
    private SelectHostActivity mActivity;
    private CreateGroup mGroupData;

    public NormalKittyMiddleViewModel(SelectHostActivity activity) {
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
        mGroupData.setGroupMember(((AddGroupContactAdapter) lv.getAdapter()).getListData());

        if (((AddGroupContactAdapter) lv.getAdapter()).getListData().size() > 0) {
            if (((AddGroupContactAdapter) lv.getAdapter()).isAllHostSelect()) {

                AppLog.d(TAG, "Data = " + new Gson().toJson(mGroupData).toString());
                if (type == 0) {
                    //For Couple
                    Intent intent = new Intent(mActivity, RuleActivity.class);
                    intent.putExtra(AppConstant.INTENT_KITTY_DATA, new Gson().toJson(mGroupData).toString());
                    intent.putExtra(AppConstant.INTENT_KITTY_TYPE, type);
                    mActivity.startActivity(intent);
                } else if (type == 1) {
                    //for Kitty with kids
                    Intent intent = new Intent(mActivity, KittyWithKidsActivity.class);
                    intent.putExtra(AppConstant.INTENT_KITTY_DATA, new Gson().toJson(mGroupData).toString());
                    intent.putExtra(AppConstant.INTENT_KITTY_TYPE, type);
                    mActivity.startActivity(intent);
                } else {
                    //for normal kitty
                    Intent intent = new Intent(mActivity, RuleActivity.class);
                    intent.putExtra(AppConstant.INTENT_KITTY_DATA, new Gson().toJson(mGroupData).toString());
                    intent.putExtra(AppConstant.INTENT_KITTY_TYPE, type);
                    mActivity.startActivity(intent);
                }
            } else {
                mActivity.showSnackbar(mActivity.getResources().getString(R.string.select_all_host_warning));
            }
        } else {
            mActivity.showSnackbar(mActivity.getResources().getString(R.string.kitty_couple_one_warning));
        }
    }
}
