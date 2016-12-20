package com.kittyapplication.ui.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.gson.Gson;
import com.kittyapplication.R;
import com.kittyapplication.adapter.AttendanceListAdapter;
import com.kittyapplication.model.AttendanceDataDao;
import com.kittyapplication.model.NotificationDao;
import com.kittyapplication.ui.viewmodel.AttendanceViewModel;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.Utils;

import java.util.List;

/**
 * Created by Dhaval Riontech on 11/8/16.
 */
public class AttendanceActivity extends BaseActivity {
    private AttendanceViewModel mViewModel;
    private ListView mLvAttendance;
    private ProgressDialog mDialog;
    private AttendanceListAdapter mAdapter;
    private List<AttendanceDataDao> mList;
    private SwipeRefreshLayout mRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        NotificationDao notificationDao = null;
        if (intent.hasExtra(AppConstant.PUT_ATTENDANCE_EXTRA)) {
            notificationDao = new Gson().fromJson
                    (String.valueOf(intent.getStringExtra(AppConstant.PUT_ATTENDANCE_EXTRA)),
                            NotificationDao.class);
        } else {
            finish();
        }

        View view = LayoutInflater.from(AttendanceActivity.this).inflate(
                R.layout.fragment_attendance, null);
        mRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.srfAttendanceList);
        mLvAttendance = (ListView) view.findViewById(R.id.lvAttendance);
        if (notificationDao != null) {
            mViewModel = new AttendanceViewModel(AttendanceActivity.this, notificationDao);
        }
        ImageView imgBanner = (ImageView) view.findViewById(R.id.imgAdvertisement);

        mViewModel.onSwipeRefreshReloadData(mRefreshLayout);
        addLayoutToContainer(view);
        hideLeftIcon();
        Utils.setBannerItem(this,
                getResources().getStringArray(R.array.adv_banner_name)[4],
                imgBanner);
    }

    @Override
    String getActionTitle() {
        return getResources().getString(R.string.rsvp);
    }

    @Override
    boolean isDisplayHomeAsUpEnabled() {
        return true;
    }

    @Override
    boolean hasDrawer() {
        return false;
    }

    public void showProgressDialog() {
        mDialog = new ProgressDialog(AttendanceActivity.this);
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

    public void setAttendanceList(List<AttendanceDataDao> data) {
        this.mList = data;
        if (Utils.isValidList(mList)) {
            mAdapter = new AttendanceListAdapter(AttendanceActivity.this, data);
            mLvAttendance.setAdapter(mAdapter);
        } else {
            showSnackbar(getResources().getString(R.string.empty_attendance));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }
}
