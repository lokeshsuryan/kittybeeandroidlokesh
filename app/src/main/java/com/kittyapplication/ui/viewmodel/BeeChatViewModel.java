package com.kittyapplication.ui.viewmodel;

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

import com.kittyapplication.R;
import com.kittyapplication.adapter.BeeChatAdapter;
import com.kittyapplication.custom.ImageLoaderListenerUniversal;
import com.kittyapplication.model.BannerDao;
import com.kittyapplication.ui.fragment.BeeChatFragment;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.ImageUtils;
import com.kittyapplication.utils.PreferanceUtils;
import com.kittyapplication.utils.Utils;

import java.util.List;

/**
 * Created by HalfBloodPrince(RIONTECH)
 * Riontech on 8/11/16.
 */

public class BeeChatViewModel {
    private static final String TAG = BeeChatViewModel.class.getSimpleName();
    private BeeChatFragment mFragment;
    private Context mContext;
    private boolean isPullToRefresh = false;
    private SwipeRefreshLayout mRefreshLayout;

    public BeeChatViewModel(BeeChatFragment beeChatFragment, Context ctx) {
        mFragment = beeChatFragment;
        mContext = ctx;
    }

    public void initRequest(boolean flag) {
        if (Utils.checkInternetConnection(mContext)) {
            if (flag) {
//                mFragment.showProgress();
            }
//            mFragment.getHomeActivity().loadDialogsFromQb();
        } else {
            if (isPullToRefresh) {
                hideRefreshLayout();
            }
        }
    }

    public void setSearchFilter(EditText editText, final BeeChatAdapter adapter) {
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
