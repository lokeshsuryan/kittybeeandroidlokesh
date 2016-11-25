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
import com.kittyapplication.ui.viewmodel.ChangeSelectHostViewModel;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.Utils;

import java.util.List;

/**
 * Created by HalfBloodPrince(RIONTECH)
 * Riontech on 19/9/16.
 */
public class SelectChangeHostActivity extends BaseActivity {
    private static final String TAG = SelectChangeHostActivity.class.getSimpleName();
    private ListView mLvAddMember;
    private CustomTextViewBold mTxtEmptyData;
    private ChangeHostAdapter mAdapter;
    private ChangeSelectHostViewModel mViewModel;
    private CustomTextViewNormal mTxtSubmitData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(SelectChangeHostActivity.this).inflate(
                R.layout.activity_add_member, null);
        addLayoutToContainer(view);
        hideLeftIcon();
        hideBottomLayout();
        mLvAddMember = (ListView) view.findViewById(R.id.lvAddMember);
        mTxtEmptyData = (CustomTextViewBold) view.findViewById(R.id.txtNoContactFoundAddGroup);
        mTxtEmptyData.setText(getResources().getString(R.string.no_data_for_change_host));
        view.findViewById(R.id.edtAddMemberSearch).setVisibility(View.GONE);
        mTxtSubmitData = (CustomTextViewNormal) view.findViewById(R.id.txtAddMemberNext);
        mTxtSubmitData.setText(getResources().getString(R.string.submit));
        mTxtSubmitData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewModel.sumbitData(mAdapter);
            }
        });

        String data = getIntent().getStringExtra(AppConstant.INTENT_CHANGE_HOST);
        String id = getIntent().getStringExtra(AppConstant.GROUP_ID);


        mViewModel = new ChangeSelectHostViewModel(this);
        mViewModel.getData(data, id);
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
            mAdapter = new ChangeHostAdapter(this, listData, noHost, 2);
            mAdapter.getSelectedList().clear();
            mLvAddMember.setAdapter(mAdapter);
        } else {
            hideLayout();
        }
    }

    public void hideLayout() {
        mLvAddMember.setVisibility(View.GONE);
        mTxtEmptyData.setVisibility(View.VISIBLE);
        mTxtSubmitData.setVisibility(View.GONE);
    }

    public void showLayout() {
        mLvAddMember.setVisibility(View.VISIBLE);
        mTxtEmptyData.setVisibility(View.GONE);
        mTxtSubmitData.setVisibility(View.VISIBLE);
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