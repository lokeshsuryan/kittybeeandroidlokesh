package com.kittyapplication.ui.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.kittyapplication.R;
import com.kittyapplication.ui.viewmodel.CalendarViewModel;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.DateTimeUtils;
import com.kittyapplication.utils.Utils;
import com.riontech.calendar.CustomCalendar;
import com.riontech.calendar.dao.EventData;
import com.riontech.calendar.dao.dataAboutDate;
import com.riontech.calendar.utils.CalendarUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

/**
 * Created by Dhaval Riontech on 7/8/16.
 */
public class CalendarActivity extends BaseActivity {

    private static final String TAG = CalendarActivity.class.getSimpleName();
    private CustomCalendar customCalendar;
    private CalendarViewModel mViewModel;
    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(CalendarActivity.this).inflate(
                R.layout.fragment_calendar, null);
        customCalendar = (CustomCalendar) view.findViewById(R.id.customCalendar);
        mViewModel = new CalendarViewModel(CalendarActivity.this);
        addLayoutToContainer(view);
        hideLeftIcon();
        hideBottomLayout();
    }

    public void insertEvent(String kittyDate, int eventCount, ArrayList<EventData> eventDataList) {
        customCalendar.insertEventIntoList(kittyDate, eventCount, eventDataList);
    }

    @Override
    protected String getActionTitle() {
        return "MY KITTY";
    }

    @Override
    protected boolean isDisplayHomeAsUpEnabled() {
        return true;
    }

    @Override
    boolean hasDrawer() {
        return true;
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

    public void refreshCalendar() {
        //refresh bottom event list
        customCalendar.refreshEvent(DateTimeUtils.getCalendarDateFormat()
                .format(Calendar.getInstance().getTime()));

        //refresh top calendar viewpager
        customCalendar.refreshCalendar();
    }

    public void showProgressDialog() {
        mDialog = new ProgressDialog(CalendarActivity.this);
        mDialog.setMessage(getResources().getString(R.string.loading_text));
        if (!mDialog.isShowing())
            mDialog.show();
    }

    public void hideProgressDialog() {
        try {
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss();
                mDialog = null;
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }
}
