package com.kittyapplication.ui.viewmodel;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.kittyapplication.R;
import com.kittyapplication.adapter.KittiesAdapter;
import com.kittyapplication.custom.ImageLoaderListenerUniversal;
import com.kittyapplication.model.BannerDao;
import com.kittyapplication.model.ChatData;
import com.kittyapplication.model.ServerResponse;
import com.kittyapplication.rest.Singleton;
import com.kittyapplication.ui.activity.HomeActivity;
import com.kittyapplication.ui.fragment.KittiesFragment;
import com.kittyapplication.ui.viewinterface.FragmentViewModelInterface;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.GroupPrefHolder;
import com.kittyapplication.utils.ImageUtils;
import com.kittyapplication.utils.PreferanceUtils;
import com.kittyapplication.utils.Utils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by HalfBloodPrince(RIONTECH)
 * Riontech on 8/11/16.
 */

public class KittiesViewModel implements FragmentViewModelInterface {
    private static final String TAG = ChatViewModel.class.getSimpleName();
    private Context mContext;
    private ProgressDialog mDialog = null;
    private KittiesFragment mFragment;
    private SwipeRefreshLayout mRefreshLayout;
    private ProgressBar mPbLoader;
    private boolean isPullToRefresh = false;


    public KittiesViewModel(KittiesFragment fragment) {
        mContext = fragment.getActivity();
        mFragment = fragment;
    }

    @Override
    public void initRequest(boolean flag) {
        if (Utils.checkInternetConnection(mContext)) {
            if (flag) {
//                mFragment.showProgress();
            }
            Call<ServerResponse<List<ChatData>>> call = Singleton.getInstance()
                    .getRestOkClient().getGroupChatData(PreferanceUtils.getLoginUserObject(mContext).getUserID());
            call.enqueue(getGroupChatDataCallBack);
        } else {
            if (isPullToRefresh) {
                hideRefreshLayout();
            }
        }
    }

    public void setSearchFilter(EditText editText, final KittiesAdapter adapter) {
        editText.setText("");
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    public void showDialog() {

    }

    @Override
    public void hideDialog() {
    }

    @Override
    public void onSwipeRefreshReloadData(SwipeRefreshLayout refreshLayout) {
        mRefreshLayout = refreshLayout;
        mRefreshLayout.setColorSchemeColors(ContextCompat.getColor(mContext, R.color.colorAccent));
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isPullToRefresh = true;
                initRequest(false);
            }
        });
    }

    private Callback<ServerResponse<List<ChatData>>> getGroupChatDataCallBack = new Callback<ServerResponse<List<ChatData>>>() {
        @Override
        public void onResponse(Call<ServerResponse<List<ChatData>>> call, Response<ServerResponse<List<ChatData>>> response) {
            if (response.code() == 200) {
                List<ChatData> dataList = response.body().getData();
                if (response.body().getResponse() == AppConstant.RESPONSE_SUCCESS) {
                    if (dataList != null && !dataList.isEmpty()) {
                        GroupPrefHolder.getInstance().saveGroupChats(dataList);
//                        mFragment.getChatList(dataList);
                    } else {
//                        mFragment.showEmptyView();
                    }
                } else {
//                    mFragment.showEmptyView();
                }
            } else {
//                mFragment.showEmptyView();
            }
        }

        @Override
        public void onFailure(Call<ServerResponse<List<ChatData>>> call, Throwable t) {
            hideRefreshLayout();
            mFragment.showEmptyView();
            ((HomeActivity) mContext).showSnackbar(mContext.getResources().getString(R.string.server_error));
        }
    };

    @Override
    public void hideRefreshLayout() {
        if (mRefreshLayout != null && mRefreshLayout.isRefreshing()) {
            mRefreshLayout.setRefreshing(false);
        }
    }

    public void setBanner(ImageView img) {
        try {
            String itemName = mContext.getResources().getStringArray(R.array.adv_banner_name)[1];
            if (PreferanceUtils.getBannerFromPreferance(mContext) != null) {
                List<BannerDao> bannerDaoList = PreferanceUtils.getBannerFromPreferance(mContext).getBanner();
                if (bannerDaoList != null && !bannerDaoList.isEmpty()) {
                    for (int i = 0; i < bannerDaoList.size(); i++) {
                        if (itemName.equalsIgnoreCase(bannerDaoList.get(i).getTitle())) {
                            img.setVisibility(View.VISIBLE);
                            ImageUtils.getImageLoader(mContext).displayImage(bannerDaoList.get(i).getThamb(), img,
                                    new ImageLoaderListenerUniversal(mContext, img, "drawable://" + R.drawable.no_image_bottom_banner));
                            img.setTag(bannerDaoList.get(i).getUrl());
                            img.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String url = (String) v.getTag();
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                    mContext.startActivity(browserIntent);
                                }
                            });
                            break;
                        } else {
                            img.setVisibility(View.GONE);
                        }
                    }
                    //img.setVisibility(View.GONE);
                } else {
                    img.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
            img.setVisibility(View.GONE);
        }
    }
}