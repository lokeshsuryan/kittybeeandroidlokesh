package com.kittyapplication.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.kittyapplication.AppApplication;
import com.kittyapplication.R;
import com.kittyapplication.chat.utils.Consts;
import com.kittyapplication.chat.utils.SharedPreferencesUtil;
import com.kittyapplication.chat.utils.chat.ChatHelper;
import com.kittyapplication.chat.utils.qb.QbAuthUtils;
import com.kittyapplication.core.utils.SharedPrefsHelper;
import com.kittyapplication.ui.viewmodel.WalkThroughViewModel;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dhaval Riontech on 21/8/16.
 */
public class WalkThroughActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = WalkThroughActivity.class.getSimpleName();
    private ViewPager mViewPager;
    //    private DotIndicator mIndicator;
    private WalkThroughViewModel mViewModel;
    private List<View> mList;
    private PagerAdapter mAdapter;
    private TextView mTxtLetsStart;

    private static final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    public boolean isAppSessionActive;
    private int retry = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreenLayout();

        setContentView(R.layout.activity_walk_through);
        mViewModel = new WalkThroughViewModel(WalkThroughActivity.this);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mTxtLetsStart = (TextView) findViewById(R.id.txtLetsStart);
        mTxtLetsStart.setOnClickListener(this);
//        mIndicator = (DotIndicator) findViewById(R.id.dot_indicator);
        mList = new ArrayList<>();
        mList = mViewModel.getImageList();

        if (mList != null && mList.size() > 0 && !mList.isEmpty()) {
            mAdapter = new PagerAdapter() {
                @Override
                public int getCount() {
                    return mList.size();
                }

                @Override
                public boolean isViewFromObject(View view, Object object) {
                    return view == object;
                }

                @Override
                public Object instantiateItem(ViewGroup container, int position) {
                    container.addView(mList.get(position));
                    return mList.get(position);
                }

                @Override
                public void destroyItem(ViewGroup container, int position, Object object) {
                    container.removeView(mList.get(position));
                }
            };
            mViewPager.setAdapter(mAdapter);
            mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//                    mIndicator.moveWithViewPager(position, positionOffset);
                }

                @Override
                public void onPageSelected(int position) {
                    if (position == (mList.size() - 1)) {
                        mTxtLetsStart.setVisibility(View.VISIBLE);
                    } else {
                        if (mTxtLetsStart.isShown())
                            mTxtLetsStart.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }

        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                recreateChatSession();
                isAppSessionActive = false;
            }
        });
    }

    private void setFullScreenLayout() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txtLetsStart:
                startActivity(new Intent(WalkThroughActivity.this, HomeActivity.class));
                finish();
                break;
        }
    }

    private void recreateChatSession() {
        Log.d(TAG, "Need to recreate chat session");

        QBUser user = SharedPreferencesUtil.getQbUser();
        if (user != null) {
            reloginToChat(user);
        }
    }

    /**
     * @param user
     */
    private void reloginToChat(final QBUser user) {
        ChatHelper.getInstance().login(user, new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void result, Bundle bundle) {
                Log.v(TAG, "Chat login onSuccess()");
                isAppSessionActive = true;
                subscribeForPushNotification();
            }

            @Override
            public void onError(QBResponseException e) {
                isAppSessionActive = false;
                if (retry < 3) {
                    retry++;
                    reloginToChat(user);
                }
            }
        });
    }

    private void subscribeForPushNotification() {
        if (!SharedPrefsHelper.getInstance().<Boolean>get(Consts.QB_SUBSCRIPTION, false)) {
            QbAuthUtils.subscribeWithQBPushNotification(AppApplication.getGCMId());
        }
    }
}
