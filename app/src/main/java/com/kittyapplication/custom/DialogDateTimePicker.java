package com.kittyapplication.custom;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.kittyapplication.listener.DateListener;
import com.kittyapplication.listener.TimePickerListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Pintu Riontech on 6/8/16.
 * vaghela.pintu31@gmail.com
 * For Show Date Time Dialog
 */
public class DialogDateTimePicker {

    private static final String TAG = DialogDateTimePicker.class.getSimpleName();
    private Context mContext;
    private DateListener mDateListener;
    private TimePickerListener mTimeListener;
    DateFormat originalFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
    DateFormat targetFormat = new SimpleDateFormat("dd MMM yyyy");

    /**
     * DialogDateTimePicker Constructor
     *
     * @param ctx
     */
    public DialogDateTimePicker(Context ctx) {
        mContext = ctx;

    }

    /**
     * Display Date Picker Dialog
     *
     * @param listener(DateListener)
     */
    public void showDatePickerDialog(DateListener listener) {
        mDateListener = listener;
        // Get Current Date
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(mContext,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        String selectedDate = dayOfMonth + "-" + (monthOfYear + 1) + "-" + year;
                        try {
                            Date date = originalFormat.parse(selectedDate);
                            String formattedDate = targetFormat.format(date);
                            mDateListener.getDate(formattedDate);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    /**
     * Display Time Picker Dialog
     *
     * @param listener(TimePickerListener)
     */
    public void showTimePickerDialog(TimePickerListener listener) {
        mTimeListener = listener;
        // Get Current Time
        final Calendar c = Calendar.getInstance();
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMinute = c.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(mContext,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minute) {

                        mTimeListener.getTime(hourOfDay + ":" + minute);
                    }
                }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    /**
     * Display Date Picker Dialog
     *
     * @param listener(DateListener)
     */
    public void showMinimumDatePickerDialog(DateListener listener) {
        mDateListener = listener;
        // Get Current Date
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(mContext,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        String selectedDate = dayOfMonth + "-" + (monthOfYear + 1) + "-" + year;
                        try {
                            Date date = originalFormat.parse(selectedDate);
                            String formattedDate = targetFormat.format(date);
                            mDateListener.getDate(formattedDate);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }
}
