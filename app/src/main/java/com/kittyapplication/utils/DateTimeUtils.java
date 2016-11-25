package com.kittyapplication.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Dhaval Riontech on 12/8/16.
 */
public class DateTimeUtils {
    private static final String SERVER_TIME_FORMAT = "h:mm aa";
    private static final String PICKER_TIME_FORMAT = "HH:mm";
    private static final String SERVER_DATE_FORMAT = "dd-MMM-yyyy";
    private static Calendar cacheCalendar;
    private static final String KITTY_RULE_FORMAT = "dd-MMM-yyyy"; // 23-Aug-1990
    private static final String CALENDER_DATE_FORMAT = "yyyy-MM-dd";
    private static final String PICKER_DATE_FORMAT = "dd MMM yyyy";
    private static String TAG = DateTimeUtils.class.getSimpleName();

    public static SimpleDateFormat getServerTimeFormat() {
        return new SimpleDateFormat(SERVER_TIME_FORMAT);
    }

    public static SimpleDateFormat getPickerTimeFormat() {
        return new SimpleDateFormat(PICKER_TIME_FORMAT);
    }

    public static SimpleDateFormat getHostedByDateFormat() {
        return new SimpleDateFormat(SERVER_DATE_FORMAT);
    }

    public static SimpleDateFormat getCalendarDateFormat() {
        return new SimpleDateFormat(CALENDER_DATE_FORMAT);
    }

    /**
     * @param year
     * @param month
     * @param whichDayOfMonth  MON/TUE/WED/THU/FRI/SAT/SUN
     * @param dayOfWeekInMonth 1st/2nd/3rd/4th
     */
    public static String getFirstMonday(int year, int month, int whichDayOfMonth, int dayOfWeekInMonth) {
        try {
            Calendar currentDate = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat(PICKER_DATE_FORMAT, Locale.getDefault());
            cacheCalendar = Calendar.getInstance();

            AppLog.d("DATE", "DAY_OF_WEEK = " + whichDayOfMonth + "\nDAY_OF_WEEK_IN_MONTH = " + dayOfWeekInMonth
                    + "\nMONTH = " + month + "\nYEAR = " + year);

            cacheCalendar.set(Calendar.DAY_OF_WEEK, whichDayOfMonth);
            cacheCalendar.set(Calendar.DAY_OF_WEEK_IN_MONTH, dayOfWeekInMonth);
            cacheCalendar.set(Calendar.MONTH, month);
            cacheCalendar.set(Calendar.YEAR, year);

            AppLog.d("DATE", "DATE ===== " + sdf.format(cacheCalendar.getTime()));

            if (cacheCalendar.getTime().before(currentDate.getTime())) {
                cacheCalendar = Calendar.getInstance();
                cacheCalendar.set(Calendar.DAY_OF_WEEK, whichDayOfMonth);
                cacheCalendar.set(Calendar.DAY_OF_WEEK_IN_MONTH, dayOfWeekInMonth);
                if (month == Calendar.DECEMBER) {
                    year += 1;
                }
                cacheCalendar.set(Calendar.MONTH, month + 1);
                cacheCalendar.set(Calendar.YEAR, year);

                AppLog.d("DATE", "DATE 22222 = " + sdf.format(cacheCalendar.getTime()));
            }

            return sdf.format(cacheCalendar.getTime());
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
            return "";
        }
    }

    public static String getKittyRuleDateFormat(String time) {
        SimpleDateFormat inputFormat = new SimpleDateFormat(PICKER_DATE_FORMAT);
        SimpleDateFormat outputFormat = new SimpleDateFormat(SERVER_DATE_FORMAT);

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(time);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
        return str;
    }

    public static String getDiarySelectHostDateFormat(String time) {
        SimpleDateFormat inputFormat = new SimpleDateFormat(SERVER_DATE_FORMAT);
        SimpleDateFormat outputFormat = new SimpleDateFormat(PICKER_DATE_FORMAT);

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(time);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
        return str;
    }


    public static SimpleDateFormat getKittyRuleDateFormat() {
        return new SimpleDateFormat(KITTY_RULE_FORMAT);
    }

    public static boolean checkCurrentDateIsBeforeKittyDate(String kittyDate) {
        boolean flag = false;
        try {
            AppLog.d("DATE UTILS", "DATE +" + kittyDate);
            if (Utils.isValidString(kittyDate)) {
                SimpleDateFormat sdf = new SimpleDateFormat(SERVER_DATE_FORMAT);
                Date strDate = null;
                try {
                    strDate = sdf.parse(kittyDate);
                    Date today = Calendar.getInstance().getTime();
                    String reportDate = sdf.format(today);
                    System.out.println("Report Date: " + reportDate);

                    if (strDate.after(today)) {
                        flag = true;
                    } else {
                        flag = false;
                    }


                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
        return flag;
    }

    public static boolean checkCurrentDateIsAfterKittyDate(String kittyDate) {
        boolean flag = false;
        try {

            if (Utils.isValidString(kittyDate)) {
                SimpleDateFormat sdf = new SimpleDateFormat(SERVER_DATE_FORMAT);
                Date strDate = null;
                try {
                    strDate = sdf.parse(kittyDate);
                    Date today = Calendar.getInstance().getTime();
                    String reportDate = sdf.format(today);
                    AppLog.d("DATE UTILS", "DATE +" + kittyDate + " -- " + reportDate);

                    if (today.after(strDate)) {
                        flag = true;
                    } else {
                        flag = false;
                    }


                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
        return flag;
    }

    /**
     * Check selected date is after current date
     *
     * @param date selected date
     * @return boolean
     */
    public static boolean getSelectedDateIsAfterCurrentDate(String date, boolean withCurrentDate) {
        boolean flag = false;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(PICKER_DATE_FORMAT);
            // convert string date in to format
            Date convertedDate = dateFormat.parse(date);
            // get current date
            Date today = new Date();
            // convert date into string
            String todayDateInString = dateFormat.format(today);
            // convert current date in date
            today = dateFormat.parse(todayDateInString);
            // check convertDate is today Date OR current date is after today date
            if (withCurrentDate) {
                if (convertedDate.equals(today) || today.before(convertedDate)) {
                    flag = true;
                } else {
                    flag = false;
                }
            } else {
                if (today.before(convertedDate)) {
                    flag = true;
                } else {
                    flag = false;
                }
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
            flag = false;
        }
        return flag;
    }


    /**
     * Check Time Validation in particular date
     *
     * @param compareTime  Time where compare With
     * @param selectedTime current selected time
     * @return boolean true or false
     */
    public static boolean getSelectedTimeIsAfterParticularTime(String compareTime,
                                                               Date selectedTime) {
        boolean flag = false;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(SERVER_TIME_FORMAT);
            // convert compare time into timeCompareDate
            Date timeCompare = dateFormat.parse(compareTime);
            //convert selected Time into string
            String selectedTIME = dateFormat.format(selectedTime);
            //convert Selected Time into String
            Date timeCopareWith = dateFormat.parse(selectedTIME);

            //compare logic
            if (timeCompare.before(timeCopareWith)) {
                flag = true;
            } else {
                flag = false;
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
            flag = false;
        }
        return flag;
    }

    /**
     * get Current time in String
     *
     * @return String
     */
    public static String getCurrentTime() {
        Date currentTime = new Date();
        String time = DateTimeUtils.getServerTimeFormat().format(currentTime);
        return time;
    }


    /**
     * Check selected date is after current date
     *
     * @param date selected date
     * @return boolean
     */
    public static boolean getServerDateIsAfterCurrentDate(String date) {
        boolean flag = false;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(SERVER_DATE_FORMAT);
            // convert string date in to format
            Date convertedDate = dateFormat.parse(date);
            // get current date
            Date today = new Date();
            // convert date into string
            String todayDateInString = dateFormat.format(today);
            // convert current date in date
            today = dateFormat.parse(todayDateInString);
            // check convertDate is today Date OR current date is after today date
            if (convertedDate.equals(today) || today.before(convertedDate)) {
                flag = true;
            } else {
                flag = false;
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
            flag = false;
        }
        return flag;
    }
}
