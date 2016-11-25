package com.kittyapplication.ui.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.kittyapplication.R;
import com.kittyapplication.adapter.ChangeHostAdapter;
import com.kittyapplication.custom.CustomTextViewBold;
import com.kittyapplication.custom.CustomTextViewNormal;
import com.kittyapplication.model.ParticipantMember;
import com.kittyapplication.ui.viewmodel.ChangeHostViewModel;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.Utils;

import java.util.List;

/**
 * Created by HalfBloodPrince(RIONTECH)
 * Riontech on 19/9/16.
 */
public class ChangeHostActivity extends BaseActivity {
    private static final String TAG = ChangeHostActivity.class.getSimpleName();
    private ListView mLvAddMember;
    private CustomTextViewBold mTxtEmptyData;
    private ChangeHostAdapter mAdapter;
    private ChangeHostViewModel mViewModel;
    private CustomTextViewNormal mTxtNext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(ChangeHostActivity.this).inflate(
                R.layout.activity_add_member, null);
        addLayoutToContainer(view);
        hideLeftIcon();
        hideBottomLayout();
        mLvAddMember = (ListView) view.findViewById(R.id.lvAddMember);
        mTxtEmptyData = (CustomTextViewBold) view.findViewById(R.id.txtNoContactFoundAddGroup);
        view.findViewById(R.id.edtAddMemberSearch).setVisibility(View.GONE);
        mTxtNext = (CustomTextViewNormal) view.findViewById(R.id.txtAddMemberNext);
        mTxtNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewModel.sumbitData(mAdapter);
            }
        });

        String data = getIntent().getStringExtra(AppConstant.INTENT_CHANGE_HOST);
        String groupID = getIntent().getStringExtra(AppConstant.GROUP_ID);


        final String noOfHost = getIntent()
                .getStringExtra(AppConstant.INTENT_CHAT_DATA);

        mViewModel = new ChangeHostViewModel(this);
        AppLog.d(TAG, "DATA  = " + noOfHost + " ==== " + data);


        mViewModel.getData(noOfHost, data, groupID);
    }

    @Override
    String getActionTitle() {
        return getResources().getString(R.string.change_host);
    }

    @Override
    boolean isDisplayHomeAsUpEnabled() {
        return true;
    }

    @Override
    boolean hasDrawer() {
        return false;
    }

    public void setListData(List<ParticipantMember> listData, int noHost) {
        if (Utils.isValidList(listData)) {
            showLayout();
            mAdapter = new ChangeHostAdapter(this, listData, noHost, 1);
            mLvAddMember.setAdapter(mAdapter);

        } else {
            hideLayout();
        }
    }

    public void hideLayout() {
        mLvAddMember.setVisibility(View.INVISIBLE);
        mTxtEmptyData.setVisibility(View.VISIBLE);
        mTxtNext.setVisibility(View.GONE);
    }

    public void showLayout() {
        mLvAddMember.setVisibility(View.VISIBLE);
        mTxtEmptyData.setVisibility(View.GONE);
        mTxtNext.setVisibility(View.VISIBLE);
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
