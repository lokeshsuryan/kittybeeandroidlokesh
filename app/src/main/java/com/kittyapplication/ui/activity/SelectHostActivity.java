package com.kittyapplication.ui.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.kittyapplication.R;
import com.kittyapplication.adapter.AddGroupContactAdapter;
import com.kittyapplication.model.ContactDao;
import com.kittyapplication.ui.viewinterface.AddGroupInterface;
import com.kittyapplication.ui.viewmodel.NormalKittyMiddleViewModel;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.Utils;

import java.util.List;

/**
 * Created by Pintu Riontech on 11/8/16.
 * vaghela.pintu31@gmail.com
 */
public class SelectHostActivity extends BaseActivity implements AddGroupInterface {
    private static final String TAG = SelectHostActivity.class.getSimpleName();
    private ListView mLvContacts;
    private NormalKittyMiddleViewModel mViewModel;
    private AddGroupContactAdapter mKittyNormalAdapter;
    private int mType;
    private boolean isSelectHost;


    //kitty TYPE 0 (COUPLE KITTY)
    //kitty TYPE 1 (KITTY WITH KIDS)
    //kitty TYPE 2 (NORMAL KITTY)
    //kitty TYPE 4 (COMING KITTY FROM ADD MEMBER WHICH IS KITTY WITH KIDS WITHOUT PAID)
    //kitty TYPE 5 (COMING KITTY FROM ADD MEMBER WHICH IS KITTY WITH KIDS WITH PAID)
    //kitty TYPE 6 (COMING KITTY FROM ADD MEMBER WHICH IS COUPLE KITTY)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(SelectHostActivity.this).inflate(
                R.layout.activity_kitty_in_middle, null);
        addLayoutToContainer(view);
        hideLeftIcon();
        hideBottomLayout();
        String data = getIntent().getStringExtra(AppConstant.INTENT_KITTY_DATA);
        mType = getIntent().getIntExtra(AppConstant.INTENT_KITTY_TYPE, 1);
        mLvContacts = (ListView) view.findViewById(R.id.lvMiddleKitty);
        mViewModel = new NormalKittyMiddleViewModel(this);
        mViewModel.getData(data);

        if (this.getIntent().getExtras() != null &&
                this.getIntent().getExtras().containsKey(AppConstant.INTENT_KITTY_IS_COUPLE_HOST)) {
            isSelectHost = true;
        }
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
        return getResources().getString(R.string.select_host_title);
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
        // TYPE 0 FOR ADD GROUP MEMBER
        // TYPE 1 FOR SELECT HOST
        // TYPE 3 FOR SELECT HOST WITH COUPLE

        if (isSelectHost) {
            mKittyNormalAdapter = new AddGroupContactAdapter(this, list, 1);
        } else {
            mKittyNormalAdapter = new AddGroupContactAdapter(this, list, 3);
        }
        mLvContacts.setAdapter(mKittyNormalAdapter);
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
