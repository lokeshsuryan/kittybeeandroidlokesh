package com.kittyapplication.ui.activity;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.kittyapplication.R;
import com.kittyapplication.adapter.PromotionalRecyclerAdapter;
import com.kittyapplication.custom.CustomTextViewBold;
import com.kittyapplication.custom.GridItemDecoration;
import com.kittyapplication.custom.StaggredGridView;
import com.kittyapplication.model.PromotionalDao;
import com.kittyapplication.ui.viewinterface.PromotionalView;
import com.kittyapplication.ui.viewmodel.PromotionalViewModel;
import com.kittyapplication.utils.Utils;

import java.util.List;

/**
 * Created by Pintu Riontech on 7/8/16.
 * vaghela.pintu31@gmail.com
 */
public class PromotionalActivity extends BaseActivity implements PromotionalView {
    private static final String TAG = PromotionalActivity.class.getSimpleName();
    private int mBottomMenuPos;
    private StaggredGridView mGridView;
    private PromotionalViewModel mViewModel;
    private CustomTextViewBold mTxtNoDataFound;
    private RecyclerView mRvPromotional;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(PromotionalActivity.this).inflate(
                R.layout.activity_promotional, null);
        addLayoutToContainer(view);
        mRvPromotional = (RecyclerView) view.findViewById(R.id.rvPromotional);
        mGridView = (StaggredGridView) view.findViewById(R.id.staggeredGridViewPromotional);
        mTxtNoDataFound = (CustomTextViewBold) view.findViewById(R.id.txtPromotionalNoDataFound);
        hideLeftIcon();

        mBottomMenuPos = getIntent().getIntExtra("pos", 0);
        if (mBottomMenuPos < 5) {
//            if (mBottomMenuPos == 4) {
                mRvPromotional.setVisibility(View.VISIBLE);
                mGridView.setVisibility(View.GONE);
//            } else {
//                mRvPromotional.setVisibility(View.GONE);
//                mGridView.setVisibility(View.VISIBLE);
//            }
            setActionbarTitle(getResources().getStringArray(R.array.promotional_activity_name)[mBottomMenuPos]);
        } else {
            setActionbarTitle(getResources().getString(R.string.special));
        }


        mViewModel = new PromotionalViewModel(this);
        mViewModel.initRequest(mBottomMenuPos);
    }

    @Override
    protected String getActionTitle() {
        return getResources().getStringArray(R.array.promotional_activity_name)[mBottomMenuPos];
    }

    @Override
    protected boolean isDisplayHomeAsUpEnabled() {
        return true;
    }

    @Override
    boolean hasDrawer() {
        return true;
    }

    @Override
    public void getDataList(List<PromotionalDao> list) {
        showMainLayout();
//        if (mBottomMenuPos == 4) {
            mRvPromotional.setLayoutManager(new GridLayoutManager(this, 2));
            mRvPromotional.setAdapter(new PromotionalRecyclerAdapter(this, list));
            int spanCount = 2; // columns
            int spacing = getResources().getDimensionPixelOffset(R.dimen.common_10_dp); // 50px
            boolean includeEdge = true;
            mRvPromotional.addItemDecoration(new GridItemDecoration(spanCount, spacing, includeEdge));
//        } else {
//            mGridView.setmAdapter(new PromotionalAdapter(this, list));
//        }

    }

    @Override
    public void hideMainLayout() {
        mGridView.setVisibility(View.GONE);
        mTxtNoDataFound.setVisibility(View.VISIBLE);
        mRvPromotional.setVisibility(View.GONE);
    }

    @Override
    public void showMainLayout() {
//        if (mBottomMenuPos == 4) {
            mRvPromotional.setVisibility(View.VISIBLE);
//        } else {
//            mGridView.setVisibility(View.VISIBLE);
//        }
        mTxtNoDataFound.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
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

            case R.id.menu_home_jump:
                Utils.openActivity(this, HomeActivity.class);
                break;
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }


}
