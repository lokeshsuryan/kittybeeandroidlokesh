package com.kittyapplication.ui.activity;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.kittyapplication.AppApplication;
import com.kittyapplication.R;
import com.kittyapplication.adapter.ViewPagerAdapter;
import com.kittyapplication.custom.CustomTextViewBold;
import com.kittyapplication.custom.CustomTextViewNormal;
import com.kittyapplication.model.ChatData;
import com.kittyapplication.model.DiaryDao;
import com.kittyapplication.model.DiaryResponseDao;
import com.kittyapplication.model.KittiesDao;
import com.kittyapplication.ui.viewmodel.KittyDiaryActivityViewModel;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.Utils;

/**
 * Created by HalfBloodPrince(RIONTECH)
 * Riontech on 18/10/16.
 */

public class KittyDiaryActivity extends BaseActivity implements View.OnClickListener,
        ViewPager.OnPageChangeListener {

    private static final String TAG = KittyDiaryActivity.class.getSimpleName();
    private ChatData mChatData;
    private KittyDiaryActivityViewModel mViewModel;
    private ViewPager mViewPager;
    private ImageView mImgNext, mImgPrevious;
    private CustomTextViewBold mTxtHost, mTxtEmptyData;
    private ProgressBar mPbLoader;
    private ViewPagerAdapter mViewPagerAdapter;
    private CustomTextViewNormal mTxtRefreshing;
    private DiaryDao mData = new DiaryDao();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(KittyDiaryActivity.this).inflate(
                R.layout.activity_diary_new, null);
        addLayoutToContainer(view);
        hideLeftIcon();
        hideBottomLayout();
        String data = getIntent().getStringExtra(AppConstant.INTENT_DIARY_DATA);
        mChatData = new Gson().fromJson(data, ChatData.class);
        AppApplication.setDairyData(mChatData);

        mViewPager = (ViewPager) view.findViewById(R.id.vpDiary);
        mImgPrevious = (ImageView) view.findViewById(R.id.imgPrevious);
        mImgPrevious.setOnClickListener(this);
        mImgNext = (ImageView) view.findViewById(R.id.imgNext);
        mImgNext.setOnClickListener(this);
        mTxtRefreshing = (CustomTextViewNormal) view.findViewById(R.id.txtRefreshingDiary);
        mTxtHost = (CustomTextViewBold) view.findViewById(R.id.txtHostedName);
        mTxtEmptyData = (CustomTextViewBold) view.findViewById(R.id.txtNoDataFound);
        mPbLoader = (ProgressBar) view.findViewById(R.id.pbLoaderDiaryActivity);
        mTxtEmptyData.setVisibility(View.GONE);
    }

    @Override
    protected void onStart() {
        super.onStart();

        showProgressBar();
        mViewModel = new KittyDiaryActivityViewModel(this, mChatData);
    }

    @Override
    String getActionTitle() {
        return getResources().getString(R.string.diary_app_title);
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
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.imgNext:
                setCurrentItem(true);
                break;

            case R.id.imgPrevious:
                setCurrentItem(false);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mInflater = getMenuInflater();
        mInflater.inflate(R.menu.menu_dairy_summary, menu);
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

            case R.id.action_dairy_summary:
                mViewModel.actionSummeryData();
                break;

            case R.id.menu_home_jump:
                Utils.openActivity(this, HomeActivity.class);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    public void showProgressBar() {
        mPbLoader.setVisibility(View.VISIBLE);
    }

    public void showLayout() {
        mViewPager.setVisibility(View.VISIBLE);
        mImgPrevious.setVisibility(View.VISIBLE);
        mImgNext.setVisibility(View.VISIBLE);
        mTxtHost.setVisibility(View.VISIBLE);
        mTxtEmptyData.setVisibility(View.GONE);
    }

    public void hideLayout() {
        mViewPager.setVisibility(View.GONE);
        mImgPrevious.setVisibility(View.GONE);
        mImgNext.setVisibility(View.GONE);
        mTxtHost.setVisibility(View.GONE);
        mTxtEmptyData.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        mPbLoader.setVisibility(View.GONE);
    }

    public void setHostedByText(String text) {
        mTxtHost.setText(text);
    }

    public void showHidePreviousNextButtons(int position) {
        try {
            if (mViewPagerAdapter.getCount() > 1) {
                if (position == 0) {
                    mImgPrevious.setVisibility(View.GONE);
                } else {
                    mImgPrevious.setVisibility(View.VISIBLE);
                }

                if (position == (mViewPagerAdapter.getCount() - 1)) {
                    mImgNext.setVisibility(View.GONE);
                } else {
                    mImgNext.setVisibility(View.VISIBLE);
                }
            } else {
                mImgNext.setVisibility(View.GONE);
                mImgPrevious.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            mImgNext.setVisibility(View.VISIBLE);
            mImgPrevious.setVisibility(View.GONE);
        }
    }

    /**
     * @param data
     */
    public void setUpViewPager(DiaryResponseDao data, boolean flag) {
        mData.setChatData(mChatData);
        mData.setDiaryData(data);

        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), this, mData);
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.addOnPageChangeListener(this);
        mViewPagerAdapter.notifyDataSetChanged();

        // set last selected item
        if (mViewPagerAdapter.getCount() == mData.getDiaryData().getKitties().size()) {
            int position = mViewPagerAdapter.getCount() - 1;
            mViewPager.setCurrentItem(position);

            setUpHostByText(position);
            showHidePreviousNextButtons(position);
        }

        hideProgressBar();
        showHideRefreshingText(false);

        if (flag)
            mViewModel.callAPI();
    }

    /**
     * @param pos
     */
    private void setUpHostByText(int pos) {
        String strHostedBy = "";
        KittiesDao dao = mData.getDiaryData().getKitties().get(pos);
        try {
            if (dao.getHostNumber() != null && !dao.getHostNumber().isEmpty() &&
                    dao.getKittyDate() != null && !dao.getKittyDate().isEmpty()) {

                if (dao.getHostNumber() != null && !dao.getHostNumber().isEmpty()) {
                    for (int i = 0; i < dao.getHostNumber().size(); i++) {
                        if (Utils.isValidString(dao.getHostNumber().get(i))) {
                            if (dao.getHostName() != null && !dao.getHostName().isEmpty()) {
                                if (Utils.isValidString(dao.getHostNumber().get(i)))
                                    dao.getHostName().set(i, Utils.checkIfMe(this,
                                            dao.getHostNumber().get(i), dao.getHostName().get(i)));
                            }
                        }
                    }
                }

                strHostedBy = String.format(getResources().getString(R.string.hosted_by_on_msg),
                        TextUtils.join(", ", dao.getHostName()), dao.getKittyDate());
            }
            setHostedByText(strHostedBy);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        setUpHostByText(position);
        showHidePreviousNextButtons(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    /**
     * @param isNext
     */
    private void setCurrentItem(boolean isNext) {
        if (isNext) {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
        } else {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mViewModel != null) {
            mViewModel.destroyApiCall();
        }
    }

    public void showHideRefreshingText(boolean flag) {
        if (flag) {
            mTxtRefreshing.setVisibility(View.VISIBLE);
        } else {
            mTxtRefreshing.setVisibility(View.GONE);
        }
    }
}
