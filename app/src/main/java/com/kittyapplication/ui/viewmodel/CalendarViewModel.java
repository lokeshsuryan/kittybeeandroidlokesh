package com.kittyapplication.ui.viewmodel;

import android.os.AsyncTask;

import com.kittyapplication.R;
import com.kittyapplication.model.CalendarDao;
import com.kittyapplication.model.ServerResponse;
import com.kittyapplication.rest.Singleton;
import com.kittyapplication.ui.activity.CalendarActivity;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.DateTimeUtils;
import com.kittyapplication.utils.PreferanceUtils;
import com.riontech.calendar.dao.EventData;
import com.riontech.calendar.dao.dataAboutDate;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Dhaval Riontech on 16/8/16.
 */
public class CalendarViewModel {
    private static final String TAG = CalendarViewModel.class.getSimpleName();
    private final CalendarActivity mActivity;

    public CalendarViewModel(CalendarActivity activity) {
        mActivity = activity;
        getCalendarData();
    }

    private void getCalendarData() {
        mActivity.showProgressDialog();

        Call<ServerResponse<List<CalendarDao>>> call = Singleton.getInstance().getRestOkClient()
                .calander(PreferanceUtils.getLoginUserObject(mActivity).getUserID());
        call.enqueue(calendarCallback);
    }

    Callback<ServerResponse<List<CalendarDao>>> calendarCallback = new Callback<ServerResponse<List<CalendarDao>>>() {
        @Override
        public void onResponse(Call<ServerResponse<List<CalendarDao>>> call, Response<ServerResponse<List<CalendarDao>>> response) {
//            mActivity.hideProgressDialog();
            if (response.code() == 200) {
                if (response.body().getStatus().equalsIgnoreCase(AppConstant.SUCCESS)) {
                    if (!response.body().getData().isEmpty() && response.body().getData().size() > 0) {
                        new CalendarTask(response.body().getData()).execute();
                    }
                } else {
                    mActivity.hideProgressDialog();
                    mActivity.showSnackbar(response.body().getMessage());
                }
            } else {
                AppLog.e(TAG, "Response code Not 200");
                mActivity.hideProgressDialog();
                mActivity.showSnackbar(mActivity.getResources().getString(R.string.server_error));
            }
        }

        @Override
        public void onFailure(Call<ServerResponse<List<CalendarDao>>> call, Throwable t) {
            mActivity.hideProgressDialog();
        }
    };

    private void insertEventList(List<CalendarDao> data) {

        com.riontech.calendar.Singleton.getInstance().clearEventManager();

        Set<String> dateSet = new HashSet<>();
        for (int i = 0; i < data.size(); i++) {
            dateSet.add(data.get(i).getKittyDate());
        }


        for (Iterator<String> it = dateSet.iterator(); it.hasNext(); ) {
            String date = it.next();

            // Arraylist of 15 august stored inside event data and
            // that event data added to event data arraylist
            ArrayList<EventData> eventDataList = new ArrayList<>();

            // 15 August -> multiple events stored inside this arraylist
            ArrayList<dataAboutDate> dataAboutDateList = new ArrayList<>();
            for (int j = 0; j < data.size(); j++) {
                if (date.equalsIgnoreCase(data.get(j).getKittyDate())) {
                    dataAboutDate dataAboutdata = new dataAboutDate();
                    String event1 = "";
                    String event2 = "";
                    String event3 = "";
                    if (!data.get(j).getKittyVanue().isEmpty() && data.get(j).getKittyVanue() != null) {
                        event1 = String.format(mActivity.getResources().getString(R.string.calender_event_data)
                                , data.get(j).getGroupName(), date,
                                data.get(j).getKittyTime(), data.get(j).getKittyVanue());
                        event3 = "at " + data.get(j).getKittyVanue();

                    } else {
                        event1 = String.format(mActivity.getResources().getString(R.string.calender_event_data_without_venue)
                                , data.get(j).getGroupName(), date,
                                data.get(j).getKittyTime());
                        event3 = "";
                    }
                    event1 = data.get(j).getGroupName() + " On ";
                    event2 = data.get(j).getKittyDate() + "," + data.get(j).getKittyTime() + " ";
                    dataAboutdata.setTitle(event1);
                    dataAboutdata.setSubject(event2);
                    dataAboutdata.setRemarks(event3);
                    dataAboutdata.setSubmissionDate("");
                    dataAboutDateList.add(dataAboutdata);
                }
            }

            EventData eventData = new EventData();
            eventData.setSection("Kitty ");
            eventData.setData(dataAboutDateList);

            eventDataList.add(eventData);


            Date d = null;
            try {
                d = DateTimeUtils.getHostedByDateFormat().parse(date);
            } catch (ParseException e) {
            }

            // App format -> 16-Aug-2016
            // Calendar Format -> 2016-08-16
            if (dataAboutDateList.size() > 3) {
                mActivity.insertEvent(DateTimeUtils.getCalendarDateFormat().format(d), 3, eventDataList);
            } else {
                mActivity.insertEvent(DateTimeUtils.getCalendarDateFormat().format(d),
                        dataAboutDateList.size(), eventDataList);
            }
        }
    }

    /**
     *
     */
    private class CalendarTask extends AsyncTask<Void, Void, Void> {
        List<CalendarDao> data;

        CalendarTask(List<CalendarDao> data) {
            this.data = data;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            insertEventList(data);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mActivity.hideProgressDialog();

            mActivity.refreshCalendar();
        }
    }
}
