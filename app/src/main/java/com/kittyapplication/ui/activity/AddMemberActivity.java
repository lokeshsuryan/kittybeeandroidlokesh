package com.kittyapplication.ui.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.kittyapplication.R;
import com.kittyapplication.adapter.AddMemberAdapter;
import com.kittyapplication.custom.CustomEditTextNormal;
import com.kittyapplication.custom.CustomTextViewBold;
import com.kittyapplication.model.ContactDao;
import com.kittyapplication.ui.viewinterface.AddGroupInterface;
import com.kittyapplication.ui.viewmodel.AddMemberViewModel;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.Utils;

import java.util.List;

/**
 * Created by Pintu Riontech on 18/8/16.
 * vaghela.pintu31@gmail.com
 */
public class AddMemberActivity extends BaseActivity implements AddGroupInterface {
    private static final String TAG = AddMemberActivity.class.getSimpleName();
    private ListView mLvAddMember;
    private CustomTextViewBold mTxtEmptyData;
    private AddMemberAdapter mAdapter;
    private AddMemberViewModel mViewModel;
    private CustomEditTextNormal mEdtSearch;
    private String mDialogId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(AddMemberActivity.this).inflate(
                R.layout.activity_add_member, null);
        addLayoutToContainer(view);
        hideLeftIcon();
        hideBottomLayout();
        String data = getIntent().getStringExtra(AppConstant.INTENT_ADD_MEMBER);

        final String kittyDate = getIntent()
                .getStringExtra(AppConstant.INTENT_ADD_MEMBER_KITTY_DATE);

        final String groupID = getIntent()
                .getStringExtra(AppConstant.INTENT_ADD_MEMBER_GROUP_ID);


        final String kittyType = getIntent()
                .getStringExtra(AppConstant.INTENT_KITTY_TYPE);

        final String isSetKittyRule = getIntent().getStringExtra(AppConstant.INTENT_GET_KITTY_RULE);

        mDialogId = getIntent().getStringExtra(AppConstant.INTENT_DIALOG_ID);

        AppLog.d(TAG, "Kitty TYPE  " + kittyType);

        mLvAddMember = (ListView) view.findViewById(R.id.lvAddMember);
        mTxtEmptyData = (CustomTextViewBold) view.findViewById(R.id.txtNoContactFoundAddGroup);
        mEdtSearch = (CustomEditTextNormal) view.findViewById(R.id.edtAddMemberSearch);
        view.findViewById(R.id.txtAddMemberNext)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mAdapter != null) {
                            if (mAdapter.getSelectedList().size() > 0) {
                                mViewModel.ClickOnNext(mAdapter, kittyDate, groupID, kittyType, isSetKittyRule);
                            }
                        }
                    }
                });
        view.findViewById(R.id.layoutAddMember).requestFocus();

        mViewModel = new AddMemberViewModel(this);
        mViewModel.getData(data);
    }

    @Override
    String getActionTitle() {
        return getResources().getString(R.string.add_member);
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

    @Override
    public void setDataIntoList(List<ContactDao> list) {
        mAdapter = new AddMemberAdapter(this, list);
        mLvAddMember.setAdapter(mAdapter);
        mViewModel.setSearchBar(mEdtSearch, mAdapter);
    }

    public void hideView() {
        mTxtEmptyData.setVisibility(View.VISIBLE);
        mLvAddMember.setVisibility(View.GONE);
        mEdtSearch.setVisibility(View.GONE);
    }

    public void showView() {
        mTxtEmptyData.setVisibility(View.GONE);
        mLvAddMember.setVisibility(View.VISIBLE);
        mEdtSearch.setVisibility(View.VISIBLE);
    }

    public String getmDialogId() {
        return mDialogId;
    }
}
