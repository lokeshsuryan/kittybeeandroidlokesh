package com.kittyapplication.ui.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.kittyapplication.R;
import com.kittyapplication.adapter.HeadsUpAdapter;
import com.kittyapplication.custom.CustomTextViewBold;
import com.kittyapplication.model.HedsUpData;
import com.kittyapplication.ui.viewmodel.HeadsUpViewModel;

import java.util.List;

/**
 * Created by Pintu Riontech on 8/9/16.
 * vaghela.pintu31@gmail.com
 */
public class HeadsUpActivity extends BaseActivity {
    private static final String TAG = HeadsUpActivity.class.getSimpleName();
    private ListView mLvHedsUp;
    private HeadsUpViewModel mViewModel;
    private CustomTextViewBold mTxtEmpty;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(HeadsUpActivity.this).inflate(
                R.layout.activity_heads_up, null);
        addLayoutToContainer(view);
        hideLeftIcon();
        mLvHedsUp = (ListView) view.findViewById(R.id.lvHeadsUp);
        mTxtEmpty = (CustomTextViewBold) view.findViewById(R.id.txtNoContactFoundAddGroup);
        mViewModel = new HeadsUpViewModel(this);
    }

    @Override
    protected String getActionTitle() {
        return getResources().getStringArray(R.array.promotional_activity_name)[2];
    }

    @Override
    protected boolean isDisplayHomeAsUpEnabled() {
        return true;
    }

    @Override
    boolean hasDrawer() {
        return true;
    }

    public void getDataList(List<HedsUpData> list) {
        if (list != null && !list.isEmpty()) {
            HeadsUpAdapter adapter = new HeadsUpAdapter(this, list);
            mLvHedsUp.setAdapter(adapter);
        } else {
            finish();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }


    public void hideLayout() {
        mTxtEmpty.setVisibility(View.VISIBLE);
        mLvHedsUp.setVisibility(View.GONE);
    }

    public void showLayout() {
        mTxtEmpty.setVisibility(View.GONE);
        mLvHedsUp.setVisibility(View.VISIBLE);
    }


}
