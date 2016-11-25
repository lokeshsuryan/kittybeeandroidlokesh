package com.kittyapplication.ui.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.kittyapplication.R;
import com.kittyapplication.adapter.SelectCoupleListAdapter;
import com.kittyapplication.custom.MiddleOfKittyListener;
import com.kittyapplication.model.ContactDao;
import com.kittyapplication.model.ContactDaoWithHeader;
import com.kittyapplication.ui.viewmodel.SelectCoupleViewModel;
import com.kittyapplication.utils.AlertDialogUtils;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pintu Riontech on 15/8/16.
 * vaghela.pintu31@gmail.com
 */
public class SelectCoupleActivity extends BaseActivity {
    private static final String TAG = SelectCoupleActivity.class.getSimpleName();
    private SelectCoupleViewModel mViewModel;
    private int mType;
    private boolean isSelectHost = false;
    private String mDialogId;

    private RecyclerView mRecyclerView;
    private SelectCoupleListAdapter mRvAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private ArrayList<ContactDaoWithHeader> mCloneList;
    private String mParticipantMember;

    private boolean mIsCreateKitty = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(SelectCoupleActivity.this).inflate(
                R.layout.select_couple_activity, null);
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

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mLinearLayoutManager = new LinearLayoutManager(SelectCoupleActivity.this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        if (this.getIntent().getExtras() != null &&
                this.getIntent().getExtras().containsKey(AppConstant.INTENT_KITTY_IS_COUPLE_HOST)) {
            isSelectHost = true;
        }

        if (this.getIntent().getExtras() != null &&
                this.getIntent().getExtras().containsKey(AppConstant.INTENT_DIALOG_ID)) {
            mDialogId = getIntent().getStringExtra(AppConstant.INTENT_DIALOG_ID);
        }


        if (this.getIntent().getExtras() != null &&
                this.getIntent().getExtras().containsKey(AppConstant.EXTRA_PARTICIPENT_MEMBER)) {
            mParticipantMember = getIntent().getStringExtra(AppConstant.EXTRA_PARTICIPENT_MEMBER);
        }

        if (this.getIntent().getExtras() != null &&
                this.getIntent().getExtras().containsKey(AppConstant.EXTRA_IS_CREATE_KITTY)) {
            mIsCreateKitty = true;
        }

//        hideCoupleList();


        view.findViewById(R.id.txtCoupleSelectDone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mRvAdapter != null
                            && mRvAdapter.getCouplePairedList() != null
                            && mRvAdapter.getCouplePairedList().size() > 0) {
                        submitData();
                    } else {
                        AlertDialogUtils.
                                showOKButtonPopUpWithMessage(SelectCoupleActivity.this,
                                        getResources().getString(R.string.couple_warning)
                                        , new MiddleOfKittyListener() {
                                            @Override
                                            public void clickOnYes() {
                                                submitData();
                                            }

                                            @Override
                                            public void clickOnNo() {

                                            }
                                        });

                    }
                } catch (Exception e) {
                    AppLog.e(TAG, e.getMessage(), e);
                }
            }
        });
        mCloneList = new ArrayList<>();
        mViewModel = new SelectCoupleViewModel(this);
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


    public void setCreateCoupleDataList(List<ContactDao> list, boolean isAdd, int position) {
        if (!isAdd) {
            ContactDaoWithHeader dao = new ContactDaoWithHeader();
            dao.setHeader(AppConstant.ADD_GROUP_TYPE_HEADER);
            ArrayList<ContactDaoWithHeader> mListWithHeader = new ArrayList<>();
            mListWithHeader.add(dao);

            for (int i = 0; i < list.size(); i++) {
                ContactDaoWithHeader dao1 = new ContactDaoWithHeader();
                dao1.setHeader(AppConstant.ADD_GROUP_TYPE_SINGLE);
                dao1.setData(list.get(i));
                mListWithHeader.add(dao1);
            }

            mRvAdapter = new SelectCoupleListAdapter(mListWithHeader, this);
            mRecyclerView.setAdapter(mRvAdapter);
        } else {
            ArrayList<ContactDaoWithHeader> mListWithHeader = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                ContactDaoWithHeader dao1 = new ContactDaoWithHeader();
                dao1.setHeader(AppConstant.ADD_GROUP_TYPE_SINGLE);
                dao1.setData(list.get(i));
                mListWithHeader.add(dao1);
            }
            mRvAdapter.appendSingleMemberToList(mListWithHeader, position);
        }
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
                if (mRvAdapter != null)
                    mRvAdapter.reloadData(getCloneList());
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

    public ArrayList<ContactDaoWithHeader> getCloneList() {
        return mCloneList;
    }

    public void setCloneList(ArrayList<ContactDaoWithHeader> cloneList) {
        this.mCloneList = cloneList;
    }

    public int getmType() {
        return mType;
    }

    public String getmParticipantMember() {
        return mParticipantMember;
    }

    public boolean getmIsCreateKitty() {
        return mIsCreateKitty;
    }

    private void submitData() {

        try {
            mViewModel.getDataList(mRvAdapter.getMemberListWithOutHeader(),
                    mType, isSelectHost);
        } catch (Exception e) {

        }
    }
}
