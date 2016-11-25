package com.kittyapplication.ui.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.kittyapplication.R;
import com.kittyapplication.adapter.DiarySummaryListAdapter;
import com.kittyapplication.model.SummaryListDao;
import com.kittyapplication.ui.viewmodel.DiarySummaryViewModel;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.Utils;

import java.util.List;

/**
 * Created by Dhaval Riontech on 15/8/16.
 */
public class DiarySummaryActivity extends BaseActivity {
    private static final String TAG = DiarySummaryActivity.class.getSimpleName();
    private String mGroupId;
    private ListView mLvHostList;
    private DiarySummaryViewModel mViewModel;
    private ProgressDialog mDialog;
    private List<SummaryListDao> mList;
    private DiarySummaryListAdapter mAdapter;
    private TextView mTxtParticipants;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mGroupId = intent.getStringExtra(AppConstant.GROUP_ID);
        View view = LayoutInflater.from(DiarySummaryActivity.this).inflate(
                R.layout.fragment_hostlist, null);
        mTxtParticipants = ((TextView) view.findViewById(R.id.txtParticipant));
        mLvHostList = (ListView) view.findViewById(R.id.lvHostList);
        mViewModel = new DiarySummaryViewModel(DiarySummaryActivity.this, mGroupId);
        AppLog.e(TAG, mGroupId);
        addLayoutToContainer(view);
        hideLeftIcon();
        hideBottomLayout();
    }

    @Override
    String getActionTitle() {
        return getString(R.string.summary_title);
    }

    @Override
    boolean isDisplayHomeAsUpEnabled() {
        return true;
    }

    @Override
    boolean hasDrawer() {
        return false;
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

    public void showProgressDialog() {
        mDialog = new ProgressDialog(DiarySummaryActivity.this);
        mDialog.setMessage(getResources().getString(R.string.loading_text));
        mDialog.show();
    }

    public void hideProgressDialog() {
        try {
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss();
                mDialog = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setHostList(List<SummaryListDao> list) {
        try {
            mList = list;
            if (mViewModel != null && mViewModel.getCount() != 0) {
                mTxtParticipants.setText(getString(R.string.pariticpant_count, "" + mViewModel.getCount()));
            } else {
                mTxtParticipants.setText(getString(R.string.pariticpant_count, "" + list.size()));
            }
            mAdapter = new DiarySummaryListAdapter(DiarySummaryActivity.this, list, false);
            mLvHostList.setAdapter(mAdapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
