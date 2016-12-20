package com.kittyapplication.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.google.gson.Gson;
import com.kittyapplication.R;
import com.kittyapplication.custom.CustomEditTextNormal;
import com.kittyapplication.custom.CustomTextViewBold;
import com.kittyapplication.custom.CustomTextViewNormal;
import com.kittyapplication.custom.DialogDateTimePicker;
import com.kittyapplication.listener.DateListener;
import com.kittyapplication.listener.TimePickerListener;
import com.kittyapplication.model.ChatData;
import com.kittyapplication.model.VenueResponseDao;
import com.kittyapplication.ui.viewmodel.SetVenueViewModel;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.DateTimeUtils;
import com.kittyapplication.utils.PreferanceUtils;
import com.kittyapplication.utils.Utils;

import java.util.Date;

/**
 * Created by Pintu Riontech on 21/8/16.
 */
public class VenueActivity extends BaseActivity implements View.OnTouchListener, DateListener, TimePickerListener {

    private static final String TAG = VenueActivity.class.getSimpleName();
    private SetVenueViewModel mViewModel;
    private CustomEditTextNormal mEdtVenue, mEdtVenueAddress, mEdtVenueDate, mEdtVenueTime;
    private CustomEditTextNormal mEdtPunctualityOne, mEdtPunctualityTwo, mEdtVenueDressCode;
    private CustomEditTextNormal mEdtAdditionalNote;
    private CustomTextViewNormal mBtnVenueSubmit;
    private CustomTextViewBold mTxtTitle;
    private int dateDialogFor = 0;
    private DialogDateTimePicker mDateTimePicker;
    private ChatData mChatData;
    private String mStrKittyID, mStrGroupID, mStrID, mStrKittyDate, mStrKittyName,
            mStrPunctuality, mStrKittyTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(VenueActivity.this)
                .inflate(R.layout.activity_set_venue, null);
        addLayoutToContainer(view);
        hideLeftIcon();

        mEdtVenue = (CustomEditTextNormal) view.findViewById(R.id.edtVenue);
        mEdtVenueAddress = (CustomEditTextNormal) view.findViewById(R.id.edtVenueAddress);
        mEdtVenueDate = (CustomEditTextNormal) view.findViewById(R.id.edtVenueDate);
        mEdtVenueTime = (CustomEditTextNormal) view.findViewById(R.id.edtVenueTime);
        mEdtPunctualityOne = (CustomEditTextNormal) view.findViewById(R.id.edtVenuePunctualityOne);
        mEdtPunctualityTwo = (CustomEditTextNormal) view.findViewById(R.id.edtVenuePunctualityTwo);
        mEdtVenueDressCode = (CustomEditTextNormal) view.findViewById(R.id.edtVenueDressCode);
        mEdtAdditionalNote = (CustomEditTextNormal) view.findViewById(R.id.edtVenueAdditionalNote);
        mBtnVenueSubmit = (CustomTextViewNormal) view.findViewById(R.id.txtVenueSubmit);
        mTxtTitle = (CustomTextViewBold) view.findViewById(R.id.txtSetVenueTitle);
        mEdtPunctualityOne.setOnTouchListener(this);
        mEdtPunctualityTwo.setOnTouchListener(this);
        mBtnVenueSubmit.setOnClickListener(this);
        mDateTimePicker = new DialogDateTimePicker(VenueActivity.this);
        mEdtVenueDate.setOnTouchListener(this);
        mEdtVenueTime.setOnTouchListener(this);

//        mEdtVenueTime.setText(DateTimeUtils.getServerTimeFormat().format(new Date()));

        Intent intent = getIntent();
        String strData = intent.getStringExtra(AppConstant.INTENT_CHAT_DATA);
        AppLog.d(TAG, "strData " + strData);
        if (Utils.isValidString(strData)) {
            try {
                mChatData = new Gson().fromJson(strData, ChatData.class);

                mStrKittyID = mChatData.getKittyId();
                mStrGroupID = mChatData.getGroupID();
                mStrKittyDate = mChatData.getKittyDate();
                mStrKittyName = mChatData.getName();
                mStrPunctuality = mChatData.getPunctuality();
                mStrKittyTime = mChatData.getKittyTime();

                if (mStrPunctuality.equalsIgnoreCase("0")) {
                    mEdtPunctualityOne.setEnabled(false);
                    mEdtPunctualityTwo.setEnabled(false);
                } else if (mStrPunctuality.equalsIgnoreCase("1")) {
                    mEdtPunctualityOne.setEnabled(true);
                    mEdtPunctualityTwo.setEnabled(false);

                    mEdtPunctualityOne.setText(mChatData.getPunctualityTime());
                } else {
                    mEdtPunctualityOne.setEnabled(true);
                    mEdtPunctualityTwo.setEnabled(true);

                    mEdtPunctualityOne.setText(mChatData.getPunctualityTime());
                    mEdtPunctualityTwo.setText(mChatData.getPunctualityTime2());
                }

                mViewModel = new SetVenueViewModel(VenueActivity.this, mStrKittyID);

                mTxtTitle.setText(getResources()
                        .getString(R.string.setvenue_text, mStrKittyName,
                                mViewModel.getHostedName(mChatData), mStrKittyDate));

                mEdtVenueDate.setText(mStrKittyDate);

                mEdtVenueTime.setText(mStrKittyTime);
            } catch (Exception e) {
                AppLog.e(TAG, e.getMessage(), e);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mViewModel.init();
    }

    @Override
    String getActionTitle() {
        return getResources().getString(R.string.set_venue);
    }

    @Override
    boolean isDisplayHomeAsUpEnabled() {
        return true;
    }

    @Override
    boolean hasDrawer() {
        return false;
    }

    public void setVenue(VenueResponseDao venueResponse, boolean flag) {
        if (mViewModel.isVenueEdit)
            mBtnVenueSubmit.setText(getResources().getString(R.string.update));

        mStrID = venueResponse.getId();
        mEdtVenue.setText(venueResponse.getVanue());
        mEdtVenueAddress.setText(venueResponse.getAddress());
        mEdtVenueDate.setText(venueResponse.getKittyDate());
        mEdtVenueTime.setText(venueResponse.getVenueTime());
        mEdtPunctualityOne.setText(venueResponse.getPunctuality());
        mEdtPunctualityTwo.setText(venueResponse.getPunctuality2());
        mEdtVenueDressCode.setText(venueResponse.getDressCode());
        mEdtAdditionalNote.setText(venueResponse.getNote());
        if (Utils.isValidString(venueResponse.getKittyDate()))
            mTxtTitle.setText(getResources()
                    .getString(R.string.setvenue_text, mStrKittyName,
                            mViewModel.getHostedName(mChatData), venueResponse.getKittyDate()));

        if (flag)
            mViewModel.callAPI();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.txtVenueSubmit:
                /*if (mIsVenueAdded) {
                    mViewModel.editVenue(mVenueResponse, getFormData());
                } else {
                    if (validateForm()) {
                        mViewModel.addVenue(getFormData());
                    }
                }*/
                if (validateForm())
                    mViewModel.addVenue(getFormData());
                break;
        }
    }

    private boolean validateForm() {
        if (Utils.isValidText(mEdtVenue, getResources().getString(R.string.venue))) {
            if (Utils.isValidText(mEdtVenueAddress, getResources().getString(R.string.venue_address))) {
                if (Utils.isValidText(mEdtVenueDate, getResources().getString(R.string.venue_date))) {
                    if (Utils.isValidText(mEdtVenueTime, getResources().getString(R.string.venue_time))) {
                        if (mStrPunctuality.equalsIgnoreCase("0")) {
                            return true;
                        } else if (mStrPunctuality.equalsIgnoreCase("1")) {
                            if (Utils.isValidText(mEdtPunctualityOne, getResources().getString(R.string.puntuality_time))) {
                                return true;
                            }
                        } else {
                            if (Utils.isValidText(mEdtPunctualityOne, getResources().getString(R.string.puntuality_time))) {
                                if (Utils.isValidText(mEdtPunctualityTwo, getResources().getString(R.string.punchtuality_time_2))) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private VenueResponseDao getFormData() {
        VenueResponseDao dao = new VenueResponseDao();
        dao.setVanue(mEdtVenue.getText().toString());
        dao.setAddress(mEdtVenueAddress.getText().toString());
        dao.setKittyDate(mEdtVenueDate.getText().toString());
        dao.setReqKittyTime(mEdtVenueTime.getText().toString());
        dao.setPunctuality(mEdtPunctualityOne.getText().toString());
        dao.setPunctuality2(mEdtPunctualityTwo.getText().toString());
        dao.setDressCode(mEdtVenueDressCode.getText().toString());
        dao.setNote(mEdtAdditionalNote.getText().toString());
        dao.setReqKittyId(mStrKittyID);
        dao.setGroupID(mStrGroupID);
        dao.setId(mStrID);
        dao.setReqUserId(PreferanceUtils.getLoginUserObject(this).getUserID());
        return dao;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            switch (v.getId()) {
                case R.id.edtVenueDate:
                    Utils.hideKeyboard(VenueActivity.this, mEdtVenueDate);
                    dateDialogFor = 1;
                    mDateTimePicker.showDatePickerDialog(VenueActivity.this);
                    return true;

                case R.id.edtVenueTime:
                    Utils.hideKeyboard(VenueActivity.this, mEdtVenueDate);
                    dateDialogFor = 2;
                    mDateTimePicker.showTimePickerDialog(VenueActivity.this);
                    return true;

                case R.id.edtVenuePunctualityOne:
                    Utils.hideKeyboard(VenueActivity.this, mEdtVenueDate);
                    dateDialogFor = 3;
                    mDateTimePicker.showTimePickerDialog(VenueActivity.this);
                    return true;

                case R.id.edtVenuePunctualityTwo:
                    Utils.hideKeyboard(VenueActivity.this, mEdtVenueDate);
                    dateDialogFor = 4;
                    mDateTimePicker.showTimePickerDialog(VenueActivity.this);
                    return true;
            }
        }
        return false;
    }

    @Override
    public void getDate(String date) {
        if (dateDialogFor == 1) {
            if (DateTimeUtils.getSelectedDateIsAfterCurrentDate(date, true)) {
                mEdtVenueDate.setText(date);
                mTxtTitle.setText(getResources()
                        .getString(R.string.setvenue_text, mStrKittyName,
                                mViewModel.getHostedName(mChatData), date));
            } else {
//                showSnackbar(getResources().getString(R.string.vanue_date_validation));
                showSnackbar(getResources().getString(R.string.kitty_date_error_text));
            }
        }

    }

    @Override
    public void getTime(String time) {
        try {
            Date date = DateTimeUtils.getPickerTimeFormat().parse(time);
            if (dateDialogFor == 2) {
                mEdtVenueTime.setText(DateTimeUtils.getServerTimeFormat().format(date));

            } else if (dateDialogFor == 3) {

                // validation for check punctual time is after kitty time
                if (DateTimeUtils.getSelectedTimeIsAfterParticularTime
                        (mEdtVenueTime.getText().toString(), date)) {
                    mEdtPunctualityOne.setError(null);
                    mEdtPunctualityOne
                            .setText(DateTimeUtils.getServerTimeFormat().format(date));
                } else {
                    showSnackbar(getResources().getString(R.string.punctual_time_one_warning));
                }

            } else if (dateDialogFor == 4) {

                // validation for check punctual time two is after punctual time time
                if (DateTimeUtils
                        .getSelectedTimeIsAfterParticularTime
                                (mEdtPunctualityOne.getText().toString(), date)) {
                    mEdtPunctualityTwo.setError(null);
                    mEdtPunctualityTwo
                            .setText(DateTimeUtils.getServerTimeFormat().format(date));
                } else {
                    showSnackbar(getResources().getString(R.string.punctual_time_two_warning));
                }
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
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
}