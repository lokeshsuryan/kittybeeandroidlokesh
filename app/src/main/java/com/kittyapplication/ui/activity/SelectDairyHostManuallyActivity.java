package com.kittyapplication.ui.activity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.kittyapplication.R;
import com.kittyapplication.adapter.DiarySummaryListAdapter;
import com.kittyapplication.custom.DialogDateTimePicker;
import com.kittyapplication.listener.DateListener;
import com.kittyapplication.model.SummaryListDao;
import com.kittyapplication.ui.viewmodel.SelectDairyHostManuallyViewModel;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.DateTimeUtils;
import com.kittyapplication.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dhaval Riontech on 16/8/16.
 */
public class SelectDairyHostManuallyActivity extends BaseActivity implements DateListener {
    private static final String TAG = SelectDairyHostManuallyActivity.class.getSimpleName();
    private String mGroupId;
    private LinearLayout llBottom;
    private TextView btnSubmit;
    private ListView mLvHostList;
    private List<SummaryListDao> mList;
    private DiarySummaryListAdapter mAdapter;
    private SelectDairyHostManuallyViewModel mViewModel;
    private String mKittyId;
    private String mNoOfHost;
    private DialogDateTimePicker mDateTimePicker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mGroupId = intent.getStringExtra(AppConstant.GROUP_ID);
        mKittyId = intent.getStringExtra(AppConstant.KITTY_ID);
        mNoOfHost = intent.getStringExtra(AppConstant.NO_OF_HOST);
        View view = LayoutInflater.from(SelectDairyHostManuallyActivity.this).inflate(
                R.layout.fragment_hostlist, null);
        mList = new ArrayList<>();
        mLvHostList = (ListView) view.findViewById(R.id.lvHostList);
        llBottom = (LinearLayout) view.findViewById(R.id.llBottom);
        view.findViewById(R.id.btnSelectRandomly).setVisibility(View.GONE);
        view.findViewById(R.id.btnSelectManually).setVisibility(View.GONE);
        btnSubmit = (TextView) view.findViewById(R.id.btnSubmitHost);
        btnSubmit.setVisibility(View.VISIBLE);
        mViewModel = new SelectDairyHostManuallyViewModel(SelectDairyHostManuallyActivity.this, mGroupId);
        llBottom.setVisibility(View.VISIBLE);
        btnSubmit.setOnClickListener(this);
        mDateTimePicker = new DialogDateTimePicker(SelectDairyHostManuallyActivity.this);
        addLayoutToContainer(view);
        hideLeftIcon();
        hideBottomLayout();
    }

    @Override
    String getActionTitle() {
        return getResources().getString(R.string.select_host_manually);
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

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.btnSubmitHost:
                int noOfHost = Integer.valueOf(mNoOfHost); //1 - 2
                if (mList.size() > noOfHost) {
                    if (mAdapter.getCheckedList().isEmpty() ||
                            mAdapter.getCheckedList().size() < noOfHost) {
                        showSnackbar(getResources().getString(R.string.select_host_warning));
                    } else if (mAdapter.getCheckedList().size() > noOfHost) {
                        showSnackbar("You can select only " + noOfHost + " Host");
                    } else {
                        selectHostManuallyDialog();
                    }
                } else {
                    if (mAdapter.getCheckedList().isEmpty()) {
                        showSnackbar("Please select host");
                    } else {
                        selectHostManuallyDialog();
                    }
                }
                break;
        }
    }

    public void setHostList(List<SummaryListDao> list) {
        for (int i = 0; i < list.size(); i++) {
            if (!dualCheck(list.get(i).getHost())) {
                mList.add(list.get(i));
            }
            mAdapter = new DiarySummaryListAdapter(SelectDairyHostManuallyActivity.this, mList, true);
            mLvHostList.setAdapter(mAdapter);
        }
    }

    public String getmNoOfHost() {
        return mNoOfHost;
    }

    AlertDialog dialog;
    View alertView;

    public void selectHostManuallyDialog() {
        try {
            List<String> mNames = new ArrayList<>();
            for (int i = 0; i < Integer.valueOf(mNoOfHost); i++) {
                mNames.add(Utils.checkIfMeInCouple(this
                        , mAdapter.getCheckedList().get(i).getNumber()
                        , mAdapter.getCheckedList().get(i).getName()));
            }

            //create dialog
            dialog = new AlertDialog.Builder(SelectDairyHostManuallyActivity.this).create();
            LayoutInflater inflater = LayoutInflater.from(SelectDairyHostManuallyActivity.this);
            alertView = inflater.inflate(R.layout.dialog_random_host_display, null);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setView(alertView);

            ((TextView) alertView.findViewById(R.id.txtdate)).setText(mViewModel.getmNextKittyDate());
            ((TextView) alertView.findViewById(R.id.txtHostNames)).setText(
                    android.text.TextUtils.join(",", mNames).replace(AppConstant.SEPERATOR_STRING, "-"));
            alertView.findViewById(R.id.txtdate).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final TextView textViewDate = (TextView) v;
                    mDateTimePicker.showDatePickerDialog(new DateListener() {
                        @Override
                        public void getDate(String date) {
                            if (DateTimeUtils.getSelectedDateIsAfterCurrentDate(date, true)) {
                                textViewDate.setText(date);
                                mViewModel.setmNextKittyDate(date);
                            } else {
                                showSnackbar(getResources().getString(R.string.kitty_date_error_text));
                            }
                        }
                    });
                }
            });

            alertView.findViewById(R.id.txtSubmit)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();

                            mViewModel.submitApiCall(mAdapter.getCheckedList(),
                                    mViewModel.getmNextKittyDate(), mKittyId, mNoOfHost, mGroupId);
                        }
                    });
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getDate(String date) {

    }

    private boolean dualCheck(String str) {
        if (str.contains(AppConstant.SEPERATOR_STRING)) {
            String hosts[] = str.split(AppConstant.SEPERATOR_STRING);
            return hosts[0].equalsIgnoreCase(AppConstant.ATTENDANCE_CHECKED);
        } else {
            return str.equalsIgnoreCase(AppConstant.ATTENDANCE_CHECKED);
        }
    }
}
