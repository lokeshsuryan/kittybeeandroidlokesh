package com.kittyapplication.ui.activity;

import android.content.Context;
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
import com.kittyapplication.ui.viewmodel.SelectDairyHostViewModel;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.DateTimeUtils;
import com.kittyapplication.utils.PreferanceUtils;
import com.kittyapplication.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Dhaval Riontech on 16/8/16.
 */
public class SelectDairyHostActivity extends BaseActivity implements DateListener {

    private static final String TAG = SelectDairyHostActivity.class.getSimpleName();
    private String mGroupId;
    private LinearLayout llBottom;
    private TextView btnSelectRandomly;
    private TextView btnSelectManually;
    private ListView mLvHostList;
    private List<SummaryListDao> mList;
    private DiarySummaryListAdapter mAdapter;
    private SelectDairyHostViewModel mViewModel;
    private String mKittyId;
    private String mNoOfHost;
    private DialogDateTimePicker mDateTimePicker;
    private String mDateSelectedFromRandomDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mGroupId = intent.getStringExtra(AppConstant.GROUP_ID);
        mKittyId = intent.getStringExtra(AppConstant.KITTY_ID);
        mNoOfHost = intent.getStringExtra(AppConstant.NO_OF_HOST);
        View view = LayoutInflater.from(SelectDairyHostActivity.this).inflate(
                R.layout.fragment_hostlist, null);
        mLvHostList = (ListView) view.findViewById(R.id.lvHostList);
        llBottom = (LinearLayout) view.findViewById(R.id.llBottom);
        btnSelectRandomly = (TextView) view.findViewById(R.id.btnSelectRandomly);
        btnSelectManually = (TextView) view.findViewById(R.id.btnSelectManually);
        mViewModel = new SelectDairyHostViewModel(SelectDairyHostActivity.this, mGroupId);
        llBottom.setVisibility(View.VISIBLE);
        btnSelectRandomly.setOnClickListener(this);
        btnSelectManually.setOnClickListener(this);
        mDateTimePicker = new DialogDateTimePicker(SelectDairyHostActivity.this);
        addLayoutToContainer(view);
        hideLeftIcon();
        hideBottomLayout();
    }

    public void setHostList(List<SummaryListDao> list) {
        mList = list;
        mAdapter = new DiarySummaryListAdapter(SelectDairyHostActivity.this, list, false);
        mLvHostList.setAdapter(mAdapter);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.btnSelectRandomly:
//                showConfirmationDialog();
                if (checkAvailbleNextHost()) {
                    selectHostRandomly(SelectDairyHostActivity.this);
                } else {
                    showSnackbar(getResources().getString(R.string.host_selection_warning));
                }
                break;
            case R.id.btnSelectManually:
                if (checkAvailbleNextHost()) {
                    Intent intent = new Intent(SelectDairyHostActivity.this, SelectDairyHostManuallyActivity.class);
                    intent.putExtra(AppConstant.GROUP_ID, mGroupId);
                    intent.putExtra(AppConstant.KITTY_ID, mKittyId);
                    intent.putExtra(AppConstant.NO_OF_HOST, mNoOfHost);
                    startActivity(intent);
                } else {
                    showSnackbar(getResources().getString(R.string.host_selection_warning));
                }
                break;
        }
    }

    AlertDialog dialog;
    View alertView;


    private void selectHostRandomly(Context context) {
        List<SummaryListDao> selectedNotHostedMember = new ArrayList<>();
        List<SummaryListDao> notHostedList = new ArrayList<>();
        try {
            // select member randomly from selected
            for (int i = 0; i < mList.size(); i++) {
                if (!dualCheck(mList.get(i).getHost())) {
                    notHostedList.add(Utils.checkIfSingleMemberInCoupleKitty(mList.get(i)));
                }
            }
            List<String> mNames = new ArrayList<>();
            for (int i = 0; i < Integer.valueOf(mNoOfHost); i++) {
                selectedNotHostedMember.add(notHostedList.get(new Random().nextInt(notHostedList.size())));
                mNames.add(Utils.checkIfMeInCouple(this, selectedNotHostedMember.get(i).getNumber(),
                        selectedNotHostedMember.get(i).getName()));
            }
            mViewModel.setmRandomHostedMember(selectedNotHostedMember);


            //create dialog
            final Context ctx = context;
            dialog = new AlertDialog.Builder(ctx).create();
            LayoutInflater inflater = LayoutInflater.from(ctx);
            alertView = inflater.inflate(R.layout.dialog_random_host_display, null);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setView(alertView);

            ((TextView) alertView.findViewById(R.id.txtdate)).setText(mViewModel.getmNextKittyDate());
            mDateSelectedFromRandomDialog = DateTimeUtils.getDiarySelectHostDateFormat(mViewModel.getmNextKittyDate());
            ((TextView) alertView.findViewById(R.id.txtHostNames)).setText(android.text.TextUtils.join(",", mNames));
            alertView.findViewById(R.id.txtdate).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDateTimePicker.showDatePickerDialog(SelectDairyHostActivity.this);
                }
            });

            alertView.findViewById(R.id.txtSubmit)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            mViewModel.setSelectHost(mViewModel.getmRandomHostedMember(),
                                    mDateSelectedFromRandomDialog
                                    , mKittyId, mNoOfHost, mGroupId);
                        }
                    });
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    String getActionTitle() {
        return "SELECT HOST";
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
    public void getDate(String date) {
        if (date != null && !date.isEmpty()) {
            if (DateTimeUtils.getSelectedDateIsAfterCurrentDate(date, true)) {
                ((TextView) alertView.findViewById(R.id.txtdate)).setText(date);
                mDateSelectedFromRandomDialog = date;
            } else {
                showSnackbar(getResources().getString(R.string.kitty_date_error_text));
            }
        }
    }

    private String checkIfMe(String number, String name) {
        if (name.contains(AppConstant.SEPERATOR_STRING)) {
            String[] names = name.split(AppConstant.SEPERATOR_STRING);
            String[] numbers = number.split(AppConstant.SEPERATOR_STRING);
            String result = "";
            if (numbers.length == 2) {
                for (int i = 0; i < 2; i++) {
                    if (numbers[i].equalsIgnoreCase(PreferanceUtils.getLoginUserObject(this).getPhone())) {
                        result = result.concat("You-");
                    } else {
                        result = result.concat(names[i] + "-");
                    }
                }
                result = result.substring(0, (result.length() - 1));
                return result;
            } else {
                if (number.equalsIgnoreCase(PreferanceUtils.getLoginUserObject(this).getPhone())) {
                    return "You";
                } else {
                    return name.replace(AppConstant.SEPERATOR_STRING, "");
                }
            }
        } else {
            if (number.equalsIgnoreCase(PreferanceUtils.getLoginUserObject(this).getPhone())) {
                return "You";
            } else {
                return name;
            }
        }
    }

    private boolean dualCheck(String str) {
        if (str.contains(AppConstant.SEPERATOR_STRING)) {
            String hosts[] = str.split(AppConstant.SEPERATOR_STRING);
            return hosts[0].equalsIgnoreCase(AppConstant.ATTENDANCE_CHECKED);
        } else {
            return str.equalsIgnoreCase(AppConstant.ATTENDANCE_CHECKED);
        }
    }

    private boolean checkAvailbleNextHost() {
        boolean flag = false;
        List<SummaryListDao> listDaos = new ArrayList<>();

        if (Utils.isValidList(mList)) {
            for (int i = 0; i < mList.size(); i++) {
                if (mList.get(i).getNumber().contains(AppConstant.SEPERATOR_STRING)) {
                    String[] host = mList.get(i).getHost().split(AppConstant.SEPERATOR_STRING);
                    for (int j = 0; j < host.length; j++) {
                        String hostSelection = host.length > j ? host[j] : "";
                        hostSelection = hostSelection.replace(AppConstant.SEPERATOR_STRING, "");
                        if (hostSelection.equalsIgnoreCase("0")) {
                            listDaos.add(mList.get(i));
                            break;
                        }
                    }
                } else {
                    if (mList.get(i).getHost().equalsIgnoreCase("0"))
                        listDaos.add(mList.get(i));
                }
            }
        }

        if (Utils.isValidList(listDaos)) {
            flag = true;
        } else {
            flag = false;
        }

        return flag;
    }

    public void enableDisableButtons(boolean isEnable) {
        btnSelectRandomly.setEnabled(isEnable);
        btnSelectManually.setEnabled(isEnable);
    }
}
