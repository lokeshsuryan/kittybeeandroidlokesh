package com.kittyapplication.ui.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.kittyapplication.R;
import com.kittyapplication.adapter.KittyWithKidsAdapter;
import com.kittyapplication.model.ContactDao;
import com.kittyapplication.ui.viewinterface.AddGroupInterface;
import com.kittyapplication.ui.viewmodel.KittyWithKidsViewModel;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.Utils;

import java.util.List;

/**
 * Created by Pintu Riontech on 12/8/16.
 * vaghela.pintu31@gmail.com
 */
public class KittyWithKidsActivity extends BaseActivity implements AddGroupInterface {
    private static final String TAG = KittyWithKidsActivity.class.getSimpleName();
    private ListView mLvContacts;
    private KittyWithKidsViewModel mViewModel;
    private KittyWithKidsAdapter mKittyKidsAdapter;
    private int mType;
    private String mDialogId;
    private boolean mIsCreateKitty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(KittyWithKidsActivity.this).inflate(
                R.layout.activity_kitty_in_middle, null);
        addLayoutToContainer(view);
        hideLeftIcon();
        hideBottomLayout();
        String data = getIntent().getStringExtra(AppConstant.INTENT_KITTY_DATA);

        //kitty TYPE 0 (COUPLE KITTY)
        //kitty TYPE 1 (KITTY WITH KIDS)
        //kitty TYPE 2 (NORMAL KITTY)
        //kitty TYPE 4 (COMING KITTY FROM ADD MEMBER WHICH IS KITTY WITH KIDS WITHOUT PAID)
        //kitty TYPE 5 (COMING KITTY FROM ADD MEMBER WHICH IS KITTY WITH KIDS WITH PAID)
        //kitty TYPE 6 (COMING KITTY FROM ADD MEMBER WHICH IS COUPLE KITTY)

        mType = getIntent().getIntExtra(AppConstant.INTENT_KITTY_TYPE, 1);
        AppLog.d(TAG, "type " + mType);

        if (this.getIntent().getExtras() != null &&
                this.getIntent().getExtras().containsKey(AppConstant.INTENT_DIALOG_ID)) {
            mDialogId = getIntent().getStringExtra(AppConstant.INTENT_DIALOG_ID);
        }


        if (this.getIntent().getExtras() != null &&
                this.getIntent().getExtras().containsKey(AppConstant.EXTRA_IS_CREATE_KITTY)) {
            mIsCreateKitty = true;
        }

        mLvContacts = (ListView) view.findViewById(R.id.lvMiddleKitty);
        mLvContacts.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        mLvContacts.setItemsCanFocus(true);
        mViewModel = new KittyWithKidsViewModel(this);
        mViewModel.getData(data);
        view.findViewById(R.id.txtNextMiddleKitty).
                setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mViewModel.onClickNextButton(mLvContacts, mType);
                    }
                });
    }

    @Override
    String getActionTitle() {
        return getResources().getString(R.string.kids);
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
    public void setDataIntoList(List<ContactDao> list) {
        mKittyKidsAdapter = new KittyWithKidsAdapter(this, list);
        mLvContacts.setAdapter(mKittyKidsAdapter);
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

    public String getmDialogId() {
        return mDialogId;
    }

    public boolean getmIsCreateKitty() {
        return mIsCreateKitty;
    }
}
