package com.riontech.calendar;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.riontech.calendar.adapter.CalendarDataAdapter;
import com.riontech.calendar.adapter.ViewPagerAdapter;
import com.riontech.calendar.dao.Event;
import com.riontech.calendar.dao.EventData;
import com.riontech.calendar.fragment.CalendarFragment;
import com.riontech.calendar.utils.CalendarUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by Dhaval Soneji on 2/6/16.
 */
public class CustomCalendar extends LinearLayout implements View.OnClickListener {
    private static final String TAG = CustomCalendar.class.getSimpleName();
    private String mStrStartMonth;
    private String mStrEndMonth;
    private ViewPager mViewPager;
    private ViewPagerAdapter mVpAdapter;
    private int mTotalMonthCount;
    private int mDuplicateTotalMonthCount;
    private int mViewPagerCurrentPosition;
    private RecyclerView mRvCalendar;
    private LinearLayoutManager mLinearLayoutManager;
    private TextView mTxtEventMessage;
    private TextView mTxtFailed;
    private ImageView mImgFailed;
    //    private ArrayList<Event> mEventList;
    private boolean isValidAttr = true;

    private Context mContext;
    private AttributeSet mAttributeSet = null;
    private RelativeLayout mRlHeader;
    private TextView mTxtMonthTitle;
    private RelativeLayout mRlNext, mRlPrevious;

    public CustomCalendar(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.layout_viewpager_recyclerview, this);
        Calendar calendar = Calendar.getInstance();
        mStrStartMonth = "1, " + String.valueOf(calendar.get(Calendar.YEAR));
        mStrEndMonth = "12, " + String.valueOf(calendar.get(Calendar.YEAR));
        mContext = context;
        initViews();
    }

    public CustomCalendar(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.layout_viewpager_recyclerview, this);
        mContext = context;
        mAttributeSet = attrs;
        initViews();
    }

    public CustomCalendar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.layout_viewpager_recyclerview, this);
        mContext = context;
        mAttributeSet = attrs;
        initViews();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CustomCalendar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        LayoutInflater.from(context).inflate(R.layout.layout_viewpager_recyclerview, this);
        this.mContext = context;
        this.mAttributeSet = attrs;
        initViews();
    }

    private void initViews() {
        if (mAttributeSet != null) {
            TypedArray a = mContext.getTheme().obtainStyledAttributes(mAttributeSet,
                    R.styleable.CustomCalendar, 0, 0);
            try {
                String startMonth = a.getString(R.styleable.CustomCalendar_startMonth);
                String startYear = a.getString(R.styleable.CustomCalendar_startYear);
                String endMonth = a.getString(R.styleable.CustomCalendar_endMonth);
                String endYear = a.getString(R.styleable.CustomCalendar_endYear);

                validateAttributes(startMonth, startYear, endMonth, endYear);

                mStrStartMonth = startMonth + ", " + startYear;
                mStrEndMonth = endMonth + ", " + endYear;
            } finally {
                a.recycle();
            }
        }
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mTxtEventMessage = (TextView) findViewById(R.id.txtEventMessage);
        mImgFailed = (ImageView) findViewById(R.id.imgFailed);
        mTxtFailed = (TextView) findViewById(R.id.txtCalendarMessage);
        mRvCalendar = (RecyclerView) findViewById(R.id.rvCalendar);
        mRlHeader = (RelativeLayout) findViewById(R.id.rlMonthTitle);
        mTxtMonthTitle = (TextView) findViewById(R.id.title);
        mRlPrevious = (RelativeLayout) findViewById(R.id.rlPrevious);
        mRlNext = (RelativeLayout) findViewById(R.id.rlNext);

        mRlNext.setOnClickListener(this);
        mRlPrevious.setOnClickListener(this);

        if (!isValidAttr) {
            invalidAttributes(getResources().getString(R.string.invalid_attribute));
            return;
        }

        /*
            first time setup calendar currentMonth
         */
        Singleton.getInstance().setMonth((GregorianCalendar) GregorianCalendar.getInstance());
        Singleton.getInstance().setCurrentDate(
                CalendarUtils.getCalendarDBFormat().format(Calendar.getInstance().getTime()));
        Singleton.getInstance().setTodayDate(
                CalendarUtils.getCalendarDBFormat().format(Calendar.getInstance().getTime()));

//        mEventList = new ArrayList();

        mLinearLayoutManager = new LinearLayoutManager(mContext);
        mRvCalendar.setLayoutManager(mLinearLayoutManager);
        Singleton.getInstance().setStartMonth(mStrStartMonth);
        Singleton.getInstance().setEndMonth(mStrEndMonth);

        setupCalendar(Singleton.getInstance().getStartMonth(), Singleton.getInstance().getEndMonth());

//        Singleton.getInstance().setEventManager(mEventList);
    }

    private void validateAttributes(String startMonth, String startYear, String endMonth, String endYear) {
        if (Integer.parseInt(startMonth) < 1 || Integer.parseInt(startMonth) > 12) {
            isValidAttr = false;
        }
        if (Integer.parseInt(endMonth) < 1 || Integer.parseInt(endMonth) > 12) {
            isValidAttr = false;
        }
    }

    private void invalidAttributes(String message) {
        mViewPager.setVisibility(GONE);
        mTxtEventMessage.setVisibility(GONE);
        mRvCalendar.setVisibility(GONE);
        mImgFailed.setVisibility(VISIBLE);
        mTxtFailed.setText(message);
        mTxtFailed.setVisibility(VISIBLE);
    }

    public void insertEventIntoList(String eventDate, int eventCount, ArrayList<EventData> eventData) {
        if (!isValidAttr)
            return;

        Event event = new Event();
        event.setDate(eventDate);
        event.setCount(String.valueOf(eventCount));
        event.setEventData(eventData);

//        mEventList.add(date);
//        Singleton.getInstance().setEventManager(mEventList);
        Singleton.getInstance().addEventIntoEventManager(event);
    }

    private void setupCalendar(String startMonth, String endMonth) {
        String temp[] = endMonth.split(",");
        int a = Integer.parseInt(temp[0]);
        String b = temp[1];
        a = a + 1;
        mStrStartMonth = startMonth;
        mStrEndMonth = String.valueOf(a) + ", " + b;

        SimpleDateFormat sdf = new SimpleDateFormat("MM, yyyy");
        Calendar currentCalendar = Calendar.getInstance();
        Calendar startCalendar = Calendar.getInstance();
        Calendar endCalendar = Calendar.getInstance();

        Date startDate = null;
        Date endDate = null;

        try {

            startDate = sdf.parse(mStrStartMonth);
            endDate = sdf.parse(mStrEndMonth);
            startCalendar.setTime(startDate);
            endCalendar.setTime(endDate);
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        int diffYear = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
        mTotalMonthCount = diffYear * 12 + endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH);
        mDuplicateTotalMonthCount = mTotalMonthCount;
        int diffCurrentYear = currentCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
        int diffCurrentMonth = diffCurrentYear * 12 + currentCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH);
        mViewPagerCurrentPosition = diffCurrentMonth;

        FragmentActivity fragmentActivity = (FragmentActivity) mContext;
        FragmentManager fm = fragmentActivity.getSupportFragmentManager();

        mVpAdapter = new ViewPagerAdapter(fm, mTotalMonthCount, this);
//        mViewPager.setOffscreenPageLimit(4);
        mViewPager.setAdapter(mVpAdapter);

        mViewPager.setCurrentItem(diffCurrentMonth);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position <= mDuplicateTotalMonthCount && position >= 0) {
                    if (position > mViewPagerCurrentPosition) {
                        Singleton.getInstance().setIsSwipeViewPager(1);

                        ((CalendarFragment) mVpAdapter.getRegisteredFragment(position)).setNextMonth();
                        ((CalendarFragment) mVpAdapter.getRegisteredFragment(position)).refreshCalendar();
                    } else {
                        Singleton.getInstance().setIsSwipeViewPager(0);

                        ((CalendarFragment) mVpAdapter.getRegisteredFragment(position)).setPreviousMonth();
                        ((CalendarFragment) mVpAdapter.getRegisteredFragment(position)).refreshCalendar();
                    }
                    mViewPagerCurrentPosition = position;
                }
                mVpAdapter.notifyDataSetChanged();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mRlHeader.setVisibility(View.VISIBLE);
    }

    /**
     * @param dateData
     */
    public void setDateSelectionData(ArrayList<EventData> dateData) {

        mRvCalendar.setVisibility(View.VISIBLE);
        mTxtEventMessage.setVisibility(View.GONE);
        mTxtEventMessage.setText("");
        ArrayList<Object> items = new ArrayList<>();
        items.clear();
        if (dateData.size() > 0) {
            for (int i = 0; i < dateData.size(); i++) {
                if (dateData.get(i).getSection() != null && !dateData.get(i).getSection().isEmpty()) {
                    if (dateData.get(i).getSection() instanceof String) {
                        items.add(dateData.get(i).getSection());
                    }
                }
                if (dateData.size() > 0) {
                    for (int j = 0; j < dateData.get(i).getData().size(); j++) {
                        ArrayList<String> list = new ArrayList<>();

                        if (dateData.get(i).getData().get(j).getRemarks() != null)
                            list.add(dateData.get(i).getData().get(j).getRemarks());
                        else
                            list.add("");
                        if (dateData.get(i).getData().get(j).getSubject() != null)
                            list.add(dateData.get(i).getData().get(j).getSubject());
                        else
                            list.add("");
                        if (dateData.get(i).getData().get(j).getSubmissionDate() != null)
                            list.add(dateData.get(i).getData().get(j).getSubmissionDate());
                        else
                            list.add("");
                        if (dateData.get(i).getData().get(j).getTitle() != null)
                            list.add(dateData.get(i).getData().get(j).getTitle());
                        else
                            list.add("");
                        items.add(list);
                    }
                }
            }
        }
        if (items.size() == 0) {
            mRvCalendar.setVisibility(View.GONE);
            try {
                Date dateTemp = CalendarUtils.getCalendarDBFormat().parse(Singleton.getInstance().getCurrentDate());
                mTxtEventMessage.setText(getResources().getString(R.string.no_events) + " (" + CalendarUtils.getCalendarDateFormat().format(dateTemp) + ")");
            } catch (ParseException e) {
                Log.e(TAG, e.getMessage(), e);
            }
            mTxtEventMessage.setVisibility(View.VISIBLE);
        } else {
            mRvCalendar.setLayoutManager(mLinearLayoutManager);
            mRvCalendar.setAdapter(new CalendarDataAdapter(items, mContext));
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Checks the orientation of the screen for landscape and portrait
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.d(TAG, "screenOrientation: landscape");
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.d(TAG, "screenOrientation: portrait");
        }
    }

    public void refreshCalendar() {
        ((CalendarFragment) mVpAdapter.getRegisteredFragment(mViewPager.getCurrentItem())).refreshCalendar();
    }

    public void refreshEvent(String date) {
        ((CalendarFragment) mVpAdapter.getRegisteredFragment(mViewPager.getCurrentItem())).refreshEvent(date);
    }

    public void setMonthTitle(String month) {
        mTxtMonthTitle.setText(month);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.rlNext) {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
        } else if (i == R.id.rlPrevious) {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
        }
    }
}
