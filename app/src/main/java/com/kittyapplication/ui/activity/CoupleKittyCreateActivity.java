package com.kittyapplication.ui.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.kittyapplication.R;
import com.kittyapplication.adapter.CreateCoupleAdapter;
import com.kittyapplication.adapter.DisplayCoupleAdapter;
import com.kittyapplication.model.ContactDao;
import com.kittyapplication.ui.viewmodel.CoupleKittyViewModel;
import com.kittyapplication.utils.AppConstant;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pintu Riontech on 15/8/16.
 * vaghela.pintu31@gmail.com
 */
public class CoupleKittyCreateActivity extends BaseActivity {
    private static final String TAG = CoupleKittyCreateActivity.class.getSimpleName();
    private ListView mLVCreateCouple, mLvCouple;
    private CoupleKittyViewModel mViewModel;
    private int mType;
    private CreateCoupleAdapter mCreateCoupleAdapter;
    private DisplayCoupleAdapter mDisplayCoupleAdapter;
    private List<ContactDao> mListCouple = new ArrayList<>();
    private boolean isSelectHost = false;
    private LinearLayout mLLSelectCoupleHeader, mLlSelectedCoupleHeader;
    private String mDialogId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(CoupleKittyCreateActivity.this).inflate(
                R.layout.activity_couple_kitty, null);
        addLayoutToContainer(view);
        hideLeftIcon();
        hideBottomLayout();


        //kitty TYPE 0 (COUPLE KITTY)
        //kitty TYPE 1 (KITTY WITH KIDS)
        //kitty TYPE 2 (NORMAL KITTY)
        //kitty TYPE 4 (COMING KITTY FROM ADD MEMBER WHICH IS KITTY WITH KIDS WITHOUT PAID)
        //kitty TYPE 5 (COMING KITTY FROM ADD MEMBER WHICH IS KITTY WITH KIDS WITH PAID)
        //kitty TYPE 6 (COMING KITTY FROM ADD MEMBER WHICH IS COUPLE KITTY)

        String data = getIntent().getStringExtra(AppConstant.INTENT_KITTY_DATA);
        mType = getIntent().getIntExtra(AppConstant.INTENT_KITTY_TYPE, 1);
        mLvCouple = (ListView) view.findViewById(R.id.lvCoupleList);
        mLVCreateCouple = (ListView) view.findViewById(R.id.lvCoupleSelectList);

        mLLSelectCoupleHeader = (LinearLayout) view.findViewById(R.id.llCreateCoupleHeader);
        mLlSelectedCoupleHeader = (LinearLayout) view.findViewById(R.id.llSelectedCoupleHeader);

        if (this.getIntent().getExtras() != null &&
                this.getIntent().getExtras().containsKey(AppConstant.INTENT_KITTY_IS_COUPLE_HOST)) {
            isSelectHost = true;
        }

        if (this.getIntent().getExtras() != null &&
                this.getIntent().getExtras().containsKey(AppConstant.INTENT_DIALOG_ID)) {
            mDialogId = getIntent().getStringExtra(AppConstant.INTENT_DIALOG_ID);
        }

//        hideCoupleList();


        view.findViewById(R.id.txtCoupleSelectDone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if (getCoupleData().size() > 0 && !getCoupleData().isEmpty()) {
                    if (mDisplayCoupleAdapter != null
                            && mDisplayCoupleAdapter.getMemberList() != null
                            && !mDisplayCoupleAdapter.getMemberList().isEmpty()
                            && mDisplayCoupleAdapter.getMemberList().size() > 0) {
                        mViewModel.getDataList(getCoupleData(), mType, isSelectHost);
                    } else {
                        showSnackbar(getResources().getString(R.string.no_couple_warning));
                    }

                    mViewModel.getDataList(getCoupleData(), mType, isSelectHost);
                } else {
                    showSnackbar(getResources().getString(R.string.no_couple_warning));
                }*/
                mViewModel.getDataList(getCoupleData(), mType, isSelectHost);
            }
        });
        mViewModel = new CoupleKittyViewModel(this);
        mViewModel.setData(data);
    }

    @Override
    String getActionTitle() {
        return getResources().getString(R.string.select_couple);
    }

    @Override
    boolean isDisplayHomeAsUpEnabled() {
        return true;
    }

    @Override
    boolean hasDrawer() {
        return false;
    }


    public void setCreateCoupleDataList(List<ContactDao> list, boolean isAdd) {
        if (!isAdd) {
            mCreateCoupleAdapter = new CreateCoupleAdapter(this, list);
            mLVCreateCouple.setAdapter(mCreateCoupleAdapter);
        } else {
            mCreateCoupleAdapter.addMemberList(list);
            // hide and show couple listview
//            if (mDisplayCoupleAdapter != null && mDisplayCoupleAdapter.getCount() != 0) {
//                showCoupleList();
//            } else {
//                hideCoupleList();
//            }
        }
    }

    public void setDataCoupleListAdapter(List<ContactDao> list) {
        if (mListCouple.isEmpty()) {
            showCoupleList();
            mListCouple.addAll(list);
            mDisplayCoupleAdapter = new DisplayCoupleAdapter(this, mListCouple);
            mLvCouple.setAdapter(mDisplayCoupleAdapter);
        } else {
            // hide and show create couple listview
//            if (mCreateCoupleAdapter.getCount() == 0) {
//                hideCoupleList();
//            } else {
//                showCoupleList();
//            }
            mDisplayCoupleAdapter.addDataToList(list);
        }
    }

    private List<ContactDao> getCoupleData() {
        List<ContactDao> coupleData = new ArrayList<>();
        if (mDisplayCoupleAdapter != null) {
            coupleData.addAll(mDisplayCoupleAdapter.getMemberList());
        }
        coupleData.addAll(mCreateCoupleAdapter.getMemberList());
        return coupleData;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mInflater = getMenuInflater();
        mInflater.inflate(R.menu.menu_create_couple, menu);
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
            case R.id.menu_create_reload_kitty:
                if (mDisplayCoupleAdapter != null)
                    mDisplayCoupleAdapter.reloadData();

                break;
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }


    private void hideCoupleList() {
        mLlSelectedCoupleHeader.setVisibility(View.GONE);
        mLvCouple.setVisibility(View.GONE);
    }

    private void showCoupleList() {
        mLlSelectedCoupleHeader.setVisibility(View.VISIBLE);
        mLvCouple.setVisibility(View.VISIBLE);
    }

    private void hideCreateCoupleList() {
        mLLSelectCoupleHeader.setVisibility(View.GONE);
        mLVCreateCouple.setVisibility(View.GONE);
    }

    private void showCreateCoupleList() {
        mLLSelectCoupleHeader.setVisibility(View.VISIBLE);
        mLVCreateCouple.setVisibility(View.VISIBLE);
    }

    public String getmDialogId() {
        return mDialogId;
    }
}
