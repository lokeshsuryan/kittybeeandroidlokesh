package com.kittyapplication.ui.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.google.gson.Gson;
import com.kittyapplication.R;
import com.kittyapplication.adapter.GiveRightAdapter;
import com.kittyapplication.custom.CustomTextViewNormal;
import com.kittyapplication.model.ParticipantMember;
import com.kittyapplication.ui.viewmodel.GiveRightToEditViewModel;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pintu Riontech on 24/8/16.
 * vaghela.pintu31@gmail.com
 */
public class GiveRightToEditActivity extends BaseActivity {
    private static final String TAG = GiveRightToEditActivity.class.getSimpleName();
    private GiveRightToEditViewModel mViewModel;
    private ListView mLvMembers;
    private CustomTextViewNormal mTxtEmpty, mTxtSubmit;
    private GiveRightAdapter mAdapter;
    private ArrayList<String> mRightsArray;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(GiveRightToEditActivity.this).inflate(
                R.layout.activity_give_rights, null);
        addLayoutToContainer(view);
        hideLeftIcon();
        hideBottomLayout();

        String data = getIntent().getStringExtra(AppConstant.INTENT_GIVE_RIGHTS_DATA);


        final String kittyId = getIntent()
                .getStringExtra(AppConstant.INTENT_ADD_MEMBER_GROUP_ID);

        mRightsArray = getIntent().getStringArrayListExtra(AppConstant.INTENT_GIVE_RIGHTS_ARRYA);

        mLvMembers = (ListView) view.findViewById(R.id.lvGiveRights);

        mTxtEmpty = (CustomTextViewNormal) view.findViewById(R.id.txtEmptyTextGiveRights);

        mTxtSubmit = (CustomTextViewNormal) view.findViewById(R.id.txtSubmitGiveRights);


        mViewModel = new GiveRightToEditViewModel(this);
        mViewModel.getData(data);

        mTxtSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mAdapter.getSelectedList().isEmpty()) {
                    mViewModel.submitDataToServer(mAdapter, kittyId);
                } else {
                    showSnackbar(getString(R.string.no_member_rights_warning));
                }
            }
        });
    }

    @Override
    String getActionTitle() {
        return getResources().getString(R.string.give_rights);
    }

    @Override
    boolean isDisplayHomeAsUpEnabled() {
        return true;
    }

    @Override
    boolean hasDrawer() {
        return false;
    }

    public void showView() {
        mLvMembers.setVisibility(View.VISIBLE);
        mTxtEmpty.setVisibility(View.GONE);
        mTxtSubmit.setVisibility(View.VISIBLE);
    }

    public void hideView() {
        mLvMembers.setVisibility(View.GONE);
        mTxtEmpty.setVisibility(View.VISIBLE);
        mTxtSubmit.setVisibility(View.GONE);
    }

    public void setDataIntoList(List<ParticipantMember> list) {
        AppLog.d(TAG, new Gson().toJson(list).toString());
        mAdapter = new GiveRightAdapter(this, list, mRightsArray);
        mLvMembers.setAdapter(mAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (hasDrawer()) {
                    toggle();
                } else {
                    onBackPressed();
                }
                break;

            case R.id.menu_home_jump:
                Utils.openActivity(this, HomeActivity.class);
                break;
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }

}
