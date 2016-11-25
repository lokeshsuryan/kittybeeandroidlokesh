package com.kittyapplication.ui.viewmodel;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;

import com.kittyapplication.R;
import com.kittyapplication.adapter.HomeViewTabAdapter;
import com.kittyapplication.custom.MiddleOfKittyListener;
import com.kittyapplication.model.BannerDao;
import com.kittyapplication.model.BannerData;
import com.kittyapplication.model.ServerResponse;
import com.kittyapplication.rest.Singleton;
import com.kittyapplication.services.OfflineSupportIntentService;
import com.kittyapplication.ui.activity.HomeActivity;
import com.kittyapplication.ui.fragment.BeeChatFragment;
import com.kittyapplication.ui.fragment.ContactsFragment;
import com.kittyapplication.ui.fragment.KittiesFragment;
import com.kittyapplication.utils.AlertDialogUtils;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.PreferanceUtils;
import com.kittyapplication.utils.Utils;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Pintu Riontech on 8/8/16.
 * vaghela.pintu31@gmail.com
 */
public class HomeViewModel implements ViewPager.OnPageChangeListener {
    private static final String TAG = HomeViewModel.class.getSimpleName();
    private HomeActivity mActivity;
    private HomeViewTabAdapter mAdapter;
    private int mCurrentPagePosition;

    public HomeViewModel(HomeActivity activity) {
        mActivity = activity;
    }

    public void setUpTabView(TabLayout tablayout, ViewPager viewPager) {
        try {
            mAdapter = new HomeViewTabAdapter(mActivity.getSupportFragmentManager(), mActivity);
            viewPager.setAdapter(mAdapter);
            viewPager.setOffscreenPageLimit(mActivity.getResources().getStringArray(R.array.home_tab_title).length);
            viewPager.addOnPageChangeListener(this);
            tablayout.setupWithViewPager(viewPager);

            BannerData data = PreferanceUtils.getBannerFromPreferance(mActivity);
            if (data == null || data.getBanner() == null)
                if (Utils.checkInternetConnection(mActivity)) {
                    new BannerAsyncTask().execute();
                }

            if (!PreferanceUtils.getIsOfflineDataAvailable(mActivity)) {
                StartOfflineServiceThread serviceThread = new StartOfflineServiceThread("startServiceThread");
                serviceThread.start();
                serviceThread.prepareHandler();
                serviceThread.postTask(task);
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

        getUpdateVersion();
    }

    /**
     * Get Banner API
     */
    private void getBanners() {
        Call<ServerResponse<List<BannerDao>>> call = Singleton.getInstance()
                .getRestOkClient().getBanner();
        call.enqueue(getBannerCallBack);
    }


    private Callback<ServerResponse<List<BannerDao>>> getBannerCallBack = new Callback<ServerResponse<List<BannerDao>>>() {
        @Override
        public void onResponse(Call<ServerResponse<List<BannerDao>>> call, Response<ServerResponse<List<BannerDao>>> response) {
            if (response.code() == 200) {
                List<BannerDao> dataList = response.body().getData();
                if (response.body().getResponse() == AppConstant.RESPONSE_SUCCESS) {
                    if (dataList != null && !dataList.isEmpty()) {
                        BannerData data = new BannerData();
                        data.setBanner(dataList);
                        PreferanceUtils.setBannerIntoPreferance(mActivity, data);
                    }
                }
            }
        }

        @Override
        public void onFailure(Call<ServerResponse<List<BannerDao>>> call, Throwable t) {
            mActivity.showSnackbar(mActivity.getResources().getString(R.string.server_error));
        }
    };

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (mActivity != null && mActivity.getSearchbar() != null)
            mActivity.getSearchbar().closeSearch();
    }

    @Override
    public void onPageSelected(int position) {
        setCurrentPagePosition(position);
        Fragment fragment = mAdapter.getRegisteredFragment(position);
        if (fragment instanceof KittiesFragment || fragment instanceof ContactsFragment) {
            if (mActivity.getCurrentActionMode() != null) {
                mActivity.getCurrentActionMode().finish();
            } else {
                AppLog.e(TAG, "Action mode is null");
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    /**
     * Banner Async task
     */
    private class BannerAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            getBanners();
            return null;
        }
    }

//    public void registerManagers() {
//        try {
//            ((ChatsFragment) mAdapter.getRegisteredFragment(0)).registerQbChatListeners();
//        } catch (Exception e) {
//            AppLog.e(TAG, e.getMessage(), e);
//        }
//    }

//    public void addCreatedDialog(String createdDialogId) {
//        try {
//            ChatsFragment fragment = (ChatsFragment) mAdapter.getRegisteredFragment(0);
//            fragment.addedNewCreatedDialog(createdDialogId);
//        } catch (Exception e) {
//            AppLog.e(TAG, e.getMessage(), e);
//        }
//    }

    public void updatedDialog(QBDialog qbDialog) {
        try {
            BeeChatFragment fragment = (BeeChatFragment) mAdapter.getRegisteredFragment(1);
            fragment.updatedDialog(qbDialog);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public void updatedMessage(QBChatMessage message) {
        try {
            BeeChatFragment fragment = (BeeChatFragment) mAdapter.getRegisteredFragment(1);
            fragment.updateMessage(message);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public int getCurrentPagePosition() {
        return mCurrentPagePosition;
    }

    public void setCurrentPagePosition(int mCurrentPagePosition) {
        this.mCurrentPagePosition = mCurrentPagePosition;
    }

    public void filterData(String searchString) {
        try {
            if (getCurrentPagePosition() == 0) {
                KittiesFragment fragment = (KittiesFragment) mAdapter.getRegisteredFragment(0);
                fragment.applyFilter(searchString);
            } else if (getCurrentPagePosition() == 1) {
                BeeChatFragment fragment = (BeeChatFragment) mAdapter.getRegisteredFragment(1);
                fragment.applyFilter(searchString);
            } else {
                ContactsFragment fragment = (ContactsFragment) mAdapter.getRegisteredFragment(2);
                fragment.applyFilter(searchString);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     */
    private class StartOfflineServiceThread extends HandlerThread {
        private Handler mWorkerHandler;

        public StartOfflineServiceThread(String name) {
            super(name);
        }

        public void postTask(Runnable task) {
            mWorkerHandler.post(task);
        }

        public void prepareHandler() {
            mWorkerHandler = new Handler(getLooper());
        }

    }

    /**
     *
     */
    Runnable task = new Runnable() {
        @Override
        public void run() {
            mActivity.startService(new Intent(mActivity,
                    OfflineSupportIntentService.class));
        }
    };


    private void getUpdateVersion() {
        if (Utils.checkInternetConnection(mActivity)) {
            Call<ServerResponse> call = Singleton.getInstance()
                    .getRestOkClient().getUpdatedVersion();
            call.enqueue(new Callback<ServerResponse>() {
                @Override
                public void onResponse(Call<ServerResponse> call,
                                       Response<ServerResponse> response) {
                    try {
                        if (response.code() == 200) {
                            response.body();
                            showDialogTask(response.body().getAndroid().getVersion());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ServerResponse> call,
                                      Throwable t) {

                }
            });
        }
    }

    private void showDialogTask(String version) {
        try {
            Float updateVersion = Float.valueOf(version);
            PackageManager manager = mActivity.getPackageManager();
            final PackageInfo info = manager.getPackageInfo(mActivity.getPackageName(), 0);
            Float currentVersion = Float.valueOf(info.versionName);
            if (currentVersion < updateVersion) {
                AlertDialogUtils.showYesNoAlert(mActivity,
                        mActivity.getResources().getString(R.string.update_version_string)
                        , new MiddleOfKittyListener() {
                            @Override
                            public void clickOnYes() {
                                try {
                                    mActivity.startActivity(new Intent
                                            (Intent.ACTION_VIEW,
                                                    Uri.parse("market://details?id="
                                                            + info.packageName)));
                                } catch (android.content.ActivityNotFoundException anfe) {
                                    mActivity.startActivity(new
                                            Intent(Intent.ACTION_VIEW,
                                            Uri.parse("https://play.google.com/store/apps/details?id="
                                                    + info.packageName)));
                                }
                            }

                            @Override
                            public void clickOnNo() {

                            }
                        });
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}