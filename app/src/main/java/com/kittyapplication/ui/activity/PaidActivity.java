package com.kittyapplication.ui.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.kittyapplication.R;
import com.kittyapplication.adapter.PaidAdapter;
import com.kittyapplication.custom.CustomEditTextNormal;
import com.kittyapplication.custom.CustomTextViewBold;
import com.kittyapplication.model.MemberData;
import com.kittyapplication.ui.viewmodel.PaidViewModel;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.Utils;

import java.util.List;

/**
 * Created by Pintu Riontech on 19/8/16.
 * vaghela.pintu31@gmail.com
 */
public class PaidActivity extends BaseActivity {

    private static final String TAG = AddMemberActivity.class.getSimpleName();
    private ListView mLvAddMember;
    private CustomTextViewBold mTxtEmptyData;
    private PaidAdapter mAdapter;
    private PaidViewModel mViewModel;
    private CustomEditTextNormal mEdtSearch;
    // for set in adapter is couple by default is 0 means is not couple
    private int isFromCouple = 0;
    private String mDialogId;
    private String mParticipant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(PaidActivity.this).inflate(
                R.layout.activity_add_member, null);
        addLayoutToContainer(view);
        hideLeftIcon();
        hideBottomLayout();
        String data = getIntent().getStringExtra(AppConstant.INTENT_PAID_DATA);

        getIntent().getStringExtra(AppConstant.INTENT_IS_COUPLE);

        //check data is from couple if is true than isFromCouple =1
        //means is add paid data like 0-!-0 or 1-!-1
        if (this.getIntent().getExtras() != null &&
                this.getIntent().getExtras().containsKey(AppConstant.INTENT_IS_COUPLE)) {
            isFromCouple = 1;
        }

        if (this.getIntent().getExtras() != null &&
                this.getIntent().getExtras().containsKey(AppConstant.INTENT_DIALOG_ID)) {
            mDialogId = getIntent().getStringExtra(AppConstant.INTENT_DIALOG_ID);
        }

        if (this.getIntent().getExtras() != null &&
                this.getIntent().getExtras().containsKey(AppConstant.EXTRA_PARTICIPENT_MEMBER)) {
            mParticipant = getIntent().getStringExtra(AppConstant.EXTRA_PARTICIPENT_MEMBER);
        }


        view.findViewById(R.id.llrowPaid).setVisibility(View.VISIBLE);
        mLvAddMember = (ListView) view.findViewById(R.id.lvAddMember);
        mTxtEmptyData = (CustomTextViewBold) view.findViewById(R.id.txtNoContactFoundAddGroup);
        mEdtSearch = (CustomEditTextNormal) view.findViewById(R.id.edtAddMemberSearch);
        mEdtSearch.setVisibility(View.GONE);
        view.findViewById(R.id.txtAddMemberNext)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mViewModel.ClickOnNext(mAdapter, isFromCouple);
                    }
                });

        mViewModel = new PaidViewModel(this);
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

    public void setDataIntoList(List<MemberData> list) {
        mAdapter = new PaidAdapter(this, list);
        mLvAddMember.setAdapter(mAdapter);
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

    public void setmDialogId(String mDialogId) {
        this.mDialogId = mDialogId;
    }

    public String getmParticipant() {
        return mParticipant;
    }
}
