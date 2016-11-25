package com.kittyapplication.ui.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.kittyapplication.R;
import com.kittyapplication.adapter.DeleteMemberAdapter;
import com.kittyapplication.custom.CustomTextViewBold;
import com.kittyapplication.custom.CustomTextViewNormal;
import com.kittyapplication.model.ParticipantMember;
import com.kittyapplication.ui.viewmodel.DeleteMemberViewModel;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.Utils;

import java.util.List;

/**
 * Created by Pintu Riontech on 18/8/16.
 * vaghela.pintu31@gmail.com
 */
public class DeleteMemberActivity extends BaseActivity {
    public static final String TAG = DeleteMemberViewModel.class.getSimpleName();
    private ListView mLvAddMember;
    private CustomTextViewBold mTxtEmptyData;
    private DeleteMemberAdapter mAdapter;
    private DeleteMemberViewModel mViewModel;
    private CustomTextViewNormal mTxtSubmitData;
    private String mDialogId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(DeleteMemberActivity.this).inflate(
                R.layout.activity_delete_member, null);
        addLayoutToContainer(view);
        hideLeftIcon();
        hideBottomLayout();

        final String kittyDate = getIntent()
                .getStringExtra(AppConstant.INTENT_ADD_MEMBER_KITTY_DATE);

        final String groupID = getIntent()
                .getStringExtra(AppConstant.INTENT_ADD_MEMBER_GROUP_ID);

        final String kittyID = getIntent()
                .getStringExtra(AppConstant.KITTY_ID);

        mDialogId = getIntent().getStringExtra(AppConstant.INTENT_DIALOG_ID);

        mLvAddMember = (ListView) view.findViewById(R.id.lvDeleteMember);

        mTxtEmptyData = (CustomTextViewBold) view.
                findViewById(R.id.txtNoContactFoundAddGroup);

        mTxtSubmitData = (CustomTextViewNormal) view.findViewById(R.id.txtAddMemberNext);
        mTxtSubmitData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isValidList(mAdapter.getSelectedList()))
                    mViewModel.submitDataToServer(mAdapter, groupID, kittyID);
            }
        });

        mViewModel = new DeleteMemberViewModel(this);
        String data = getIntent().getStringExtra(AppConstant.INTENT_ADD_MEMBER);
        AppLog.d(TAG, "DELETE DATA " + data);
        mViewModel.getData(data, kittyDate);
    }

    @Override
    String getActionTitle() {
        return getResources().getString(R.string.delete_member);
    }

    @Override
    boolean isDisplayHomeAsUpEnabled() {
        return true;
    }

    @Override
    boolean hasDrawer() {
        return false;
    }

    public void setDataIntoList(List<ParticipantMember> list) {
        AppLog.d(TAG, "Size =" + list.size());
        if (Utils.isValidList(list)) {
            mLvAddMember.setVisibility(View.VISIBLE);
            mTxtSubmitData.setVisibility(View.VISIBLE);
            mAdapter = new DeleteMemberAdapter(this, list);
            mLvAddMember.setAdapter(mAdapter);
            mTxtEmptyData.setVisibility(View.GONE);

        } else {
            mTxtEmptyData.setVisibility(View.VISIBLE);
            mLvAddMember.setVisibility(View.GONE);
            mTxtSubmitData.setVisibility(View.GONE);
        }
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

    public String getmDialogId() {
        return mDialogId;
    }
}
