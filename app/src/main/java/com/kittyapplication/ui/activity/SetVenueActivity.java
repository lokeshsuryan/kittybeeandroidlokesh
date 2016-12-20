package com.kittyapplication.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.kittyapplication.R;
import com.kittyapplication.custom.CustomEditTextNormal;
import com.kittyapplication.custom.CustomTextViewNormal;
import com.kittyapplication.custom.DialogDateTimePicker;
import com.kittyapplication.listener.DateListener;
import com.kittyapplication.listener.TimePickerListener;
import com.kittyapplication.model.VenueResponseDao;
import com.kittyapplication.ui.viewmodel.SetVenueViewModel;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.DateTimeUtils;
import com.kittyapplication.utils.PreferanceUtils;
import com.kittyapplication.utils.Utils;

import java.text.ParseException;
import java.util.Date;

/**
 * Created by Pintu Riontech on 9/8/16.
 * vaghela.pintu31@gmail.com
 */
public class SetVenueActivity extends BaseActivity implements View.OnTouchListener, DateListener, TimePickerListener {

    private static final String TAG = SetVenueActivity.class.getSimpleName();
    private SetVenueViewModel mViewModel;
    private CustomEditTextNormal edtVenue;
    private CustomEditTextNormal edtVenueAddress;
    private CustomEditTextNormal edtVenueDate;
    private CustomEditTextNormal edtVenueTime;
    private CustomEditTextNormal edtVenuePunctualityOne;
    private CustomEditTextNormal edtVenuePunctualityTwo;
    private CustomEditTextNormal edtVenueDressCode;
    private CustomEditTextNormal edtVenueAdditionalNote;
    private CustomTextViewNormal mBtnVenueSubmit;
    private int dateDialogFor = 0;
    private DialogDateTimePicker mDateTimePicker;
    private String kittyId, groupId, id, date, name, punchtuality;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        kittyId = intent.getStringExtra(AppConstant.KITTY_ID);
        groupId = intent.getStringExtra(AppConstant.GROUP_ID);
        date = intent.getStringExtra(AppConstant.KITTY_DATE);
        name = intent.getStringExtra(AppConstant.KITTY_NAME);
        punchtuality = intent.getStringExtra(AppConstant.VENUE_PUNCH);

        View view = LayoutInflater.from(SetVenueActivity.this)
                .inflate(R.layout.activity_set_venue, null);
        addLayoutToContainer(view);
        hideLeftIcon();

        edtVenue = (CustomEditTextNormal) view.findViewById(R.id.edtVenue);
        edtVenueAddress = (CustomEditTextNormal) view.findViewById(R.id.edtVenueAddress);
        edtVenueDate = (CustomEditTextNormal) view.findViewById(R.id.edtVenueDate);
        edtVenueTime = (CustomEditTextNormal) view.findViewById(R.id.edtVenueTime);
        edtVenuePunctualityOne = (CustomEditTextNormal) view.findViewById(R.id.edtVenuePunctualityOne);
        edtVenuePunctualityTwo = (CustomEditTextNormal) view.findViewById(R.id.edtVenuePunctualityTwo);
        edtVenueDressCode = (CustomEditTextNormal) view.findViewById(R.id.edtVenueDressCode);
        edtVenueAdditionalNote = (CustomEditTextNormal) view.findViewById(R.id.edtVenueAdditionalNote);
        mBtnVenueSubmit = (CustomTextViewNormal) view.findViewById(R.id.txtVenueSubmit);
        edtVenuePunctualityOne.setOnTouchListener(this);
        edtVenuePunctualityTwo.setOnTouchListener(this);
        mBtnVenueSubmit.setOnClickListener(this);
        mDateTimePicker = new DialogDateTimePicker(SetVenueActivity.this);
        edtVenueDate.setOnTouchListener(this);
        edtVenueTime.setOnTouchListener(this);

        if (punchtuality.equalsIgnoreCase("0")) {
            edtVenuePunctualityOne.setEnabled(false);
            edtVenuePunctualityTwo.setEnabled(false);
        } else if (punchtuality.equalsIgnoreCase("1")) {
            edtVenuePunctualityOne.setEnabled(true);
            edtVenuePunctualityTwo.setEnabled(false);
        } else {
            edtVenuePunctualityOne.setEnabled(true);
            edtVenuePunctualityTwo.setEnabled(true);
        }

        //mViewModel = new SetVenueViewModel(SetVenueActivity.this, kittyId);

        CustomTextViewNormal txtTitle = (CustomTextViewNormal) view.findViewById(R.id.txtSetVenueTitle);
        txtTitle.setText(getResources().getString(R.string.setvenue_text, date, name));

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

    public void setVenue(VenueResponseDao venueResponse) {
        id = venueResponse.getId();
        edtVenue.setText(venueResponse.getVanue());
        edtVenueAddress.setText(venueResponse.getAddress());
        edtVenueDate.setText(venueResponse.getKittyDate());
        edtVenueTime.setText(venueResponse.getVenueTime());
        edtVenuePunctualityOne.setText(venueResponse.getPunctuality());
        edtVenuePunctualityTwo.setText(venueResponse.getPunctuality2());
        edtVenueDressCode.setText(venueResponse.getDressCode());
        edtVenueAdditionalNote.setText(venueResponse.getNote());
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
                mViewModel.addVenue(getFormData());
                break;
        }
    }

    private boolean validateForm() {
        if (Utils.isValidText(edtVenue, getResources().getString(R.string.venue))) {
            if (Utils.isValidText(edtVenue, getResources().getString(R.string.venue_address))) {
                if (Utils.isValidText(edtVenueDate, getResources().getString(R.string.venue_date))) {
                    if (Utils.isValidText(edtVenueTime, getResources().getString(R.string.venue_time))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private VenueResponseDao getFormData() {
        VenueResponseDao dao = new VenueResponseDao();
        dao.setVanue(edtVenue.getText().toString());
        dao.setAddress(edtVenueAddress.getText().toString());
        dao.setKittyDate(edtVenueDate.getText().toString());
        dao.setReqKittyTime(edtVenueTime.getText().toString());
        dao.setPunctuality(edtVenuePunctualityOne.getText().toString());
        dao.setPunctuality2(edtVenuePunctualityTwo.getText().toString());
        dao.setDressCode(edtVenueDressCode.getText().toString());
        dao.setNote(edtVenueAdditionalNote.getText().toString());
        dao.setReqKittyId(kittyId);
        dao.setGroupID(groupId);
        dao.setId(id);
        dao.setReqUserId(PreferanceUtils.getLoginUserObject(this).getUserID());
        return dao;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        final int DRAWABLE_LEFT = 0;
        final int DRAWABLE_TOP = 1;
        final int DRAWABLE_RIGHT = 2;
        final int DRAWABLE_BOTTOM = 3;
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            switch (v.getId()) {
                case R.id.edtVenueDate:
                    Utils.hideKeyboard(SetVenueActivity.this, edtVenueDate);
                    dateDialogFor = 1;
                    if (event.getRawX() >= (edtVenueDate.getRight() -
                            edtVenueDate.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds()
                                    .width())) {
                        mDateTimePicker.showDatePickerDialog(SetVenueActivity.this);
                        return true;
                    }
                    break;

                case R.id.edtVenueTime:
                    Utils.hideKeyboard(SetVenueActivity.this, edtVenueDate);
                    dateDialogFor = 2;
                    if (event.getRawX() >= (edtVenueTime.getRight() -
                            edtVenueTime.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds()
                                    .width())) {
                        mDateTimePicker.showTimePickerDialog(SetVenueActivity.this);
                        return true;
                    }
                    break;

                case R.id.edtVenuePunctualityOne:
                    Utils.hideKeyboard(SetVenueActivity.this, edtVenueDate);
                    dateDialogFor = 3;
                    if (event.getRawX() >= (edtVenuePunctualityOne.getRight() -
                            edtVenuePunctualityOne.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds()
                                    .width())) {
                        mDateTimePicker.showTimePickerDialog(SetVenueActivity.this);
                        return true;
                    }
                    break;

                case R.id.edtVenuePunctualityTwo:
                    Utils.hideKeyboard(SetVenueActivity.this, edtVenueDate);
                    dateDialogFor = 4;
                    if (event.getRawX() >= (edtVenuePunctualityTwo.getRight() -
                            edtVenuePunctualityTwo.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds()
                                    .width())) {
                        mDateTimePicker.showTimePickerDialog(SetVenueActivity.this);
                        return true;
                    }
                    break;
            }
        }
        return false;
    }

    @Override
    public void getDate(String date) {
        if (dateDialogFor == 1) {
            edtVenueDate.setText(date);
        }

    }

    @Override
    public void getTime(String time) {
        try {
            Date date = DateTimeUtils.getPickerTimeFormat().parse(time);
            if (dateDialogFor == 2) {
                edtVenueTime.setText(DateTimeUtils.getServerTimeFormat().format(date));
            } else if (dateDialogFor == 3) {
                edtVenuePunctualityOne.setText(DateTimeUtils.getServerTimeFormat().format(date));
            } else if (dateDialogFor == 4) {
                edtVenuePunctualityTwo.setText(DateTimeUtils.getServerTimeFormat().format(date));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
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