package com.kittyapplication.ui.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.gson.Gson;
import com.kittyapplication.R;
import com.kittyapplication.custom.CustomTextViewBold;
import com.kittyapplication.custom.CustomTextViewNormal;
import com.kittyapplication.model.HedsUpData;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.Utils;

/**
 * Created by Pintu Riontech on 14/9/16.
 * vaghela.pintu31@gmail.com
 */
public class HeadsUpDescriptionActivity extends BaseActivity {
    private static final String TAG = HeadsUpDescriptionActivity.class.getSimpleName();
    private HedsUpData mHeadsUpData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(HeadsUpDescriptionActivity.this).inflate(
                R.layout.activity_heads_up_desc, null);
        addLayoutToContainer(view);
        hideLeftIcon();

        CustomTextViewBold txtTitle = (CustomTextViewBold) view.findViewById(R.id.txtHeadsupDescTitle);
        CustomTextViewBold txtDate = (CustomTextViewBold) view.findViewById(R.id.txtHeadsupDescDate);
        CustomTextViewBold txtTime = (CustomTextViewBold) view.findViewById(R.id.txtHeadsupDescTime);
        CustomTextViewNormal txtAddress = (CustomTextViewNormal) view.findViewById(R.id.txtHeadsupDescAddress);
        CustomTextViewNormal txtDesc = (CustomTextViewNormal) view.findViewById(R.id.txtHeadsupDescDesc);

        try {
            if (getIntent() != null
                    && getIntent().getStringExtra(AppConstant.EXTRA_HEADS_UP_DESCRIPTION) != null) {
                String data = getIntent().getStringExtra(AppConstant.EXTRA_HEADS_UP_DESCRIPTION);
                mHeadsUpData = new Gson().fromJson(data, HedsUpData.class);

                txtTitle.setText(mHeadsUpData.getTitle());
                txtTime.setText(getResources().getString(R.string.desc_heads_up_txt, mHeadsUpData.getFromTime(), mHeadsUpData.getToTime()));
                txtDate.setText(getResources().getString(R.string.desc_heads_up_txt, mHeadsUpData.getFromDate(), mHeadsUpData.getToDate()));
                txtAddress.setText(getAddressString(mHeadsUpData));
                txtDesc.setText(mHeadsUpData.getDecription());
            } else {
                showSnackbar(getResources().getString(R.string.quick_blox_error_found));
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }

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
        return false;
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

    private String getAddressString(HedsUpData data) {
        String address = "";
        try {
            if (Utils.isValidString(data.getAddressOne())) {
                address = address + data.getAddressOne() + ", ";
            }
            if (Utils.isValidString(data.getAddressTwo())) {
                address = address + data.getAddressTwo() + ", ";
            }
            if (Utils.isValidString(data.getState())) {
                address = address + data.getState() + ", ";
            }
            if (Utils.isValidString(data.getCountry())) {
                address = address + data.getCountry();
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
        return address;
    }
}
