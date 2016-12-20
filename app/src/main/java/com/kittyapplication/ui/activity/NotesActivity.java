package com.kittyapplication.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kittyapplication.R;
import com.kittyapplication.adapter.NotesListAdapter;
import com.kittyapplication.model.NotesRequestDao;
import com.kittyapplication.model.NotesResponseDao;
import com.kittyapplication.ui.viewmodel.NotesViewModel;
import com.kittyapplication.utils.AlertDialogUtils;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.PreferanceUtils;
import com.kittyapplication.utils.Utils;

import java.util.List;

/**
 * Created by Dhaval Soneji Riontech on 12/8/16.
 */
public class NotesActivity extends BaseActivity {
    private static final String TAG = NotesActivity.class.getSimpleName();
    private ListView mLvNotes;
    private NotesListAdapter mAdapter;
    private NotesViewModel mViewModel;
    private List<NotesResponseDao> mList;
    private FloatingActionButton mFabAddNotes;
    private ImageView mImgAdvertise;
    private String mNotesType;
    private String mGroupiId;
    private String mNotesTitle;
    private Intent mIntent;
    private String mKittyId;
    private TextView mTxtEmpty;
    private ProgressBar mPbLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(NotesActivity.this).inflate(
                R.layout.fragment_notes, null);

        int type = 0;
        mIntent = getIntent();
        if (mIntent.hasExtra(AppConstant.GROUP_ID)) {
            mGroupiId = mIntent.getStringExtra(AppConstant.GROUP_ID);
            mKittyId = mIntent.getStringExtra(AppConstant.KITTY_ID);
            mNotesType = mIntent.getStringExtra(AppConstant.NOTES_TYPE);
            switch (mNotesType) {
                case AppConstant.NOTES_TYPE_KITTY:
                    mNotesTitle = getResources().getString(R.string.kitty_notes);
                    type = 0;
                    break;
                case AppConstant.NOTES_TYPE_PERSONAL:
                    mNotesTitle = getResources().getString(R.string.personal_notes);
                    type = 1;
                    break;
            }
        } else {
            mNotesTitle = getString(R.string.add_personal_notes_dialog_title);
        }

        mLvNotes = (ListView) view.findViewById(R.id.lvNotes);
        mTxtEmpty = (TextView) view.findViewById(R.id.txtEmpty);
        mFabAddNotes = (FloatingActionButton) view.findViewById(R.id.fabAddNotes);
        mImgAdvertise = (ImageView) view.findViewById(R.id.imgAdvertisement);
        mPbLoader = (ProgressBar) view.findViewById(R.id.pbLoaderNotesActivity);
        mLvNotes.setDivider(null);
        mLvNotes.setDividerHeight(0);

        mViewModel = new NotesViewModel(NotesActivity.this, getNotesRequestDaoObject());
        mViewModel.setBanner(mImgAdvertise, type);
        mFabAddNotes.setOnClickListener(this);
        addLayoutToContainer(view);
        hideLeftIcon();
    }

    @Override
    String getActionTitle() {
        mIntent = getIntent();
        if (mIntent.hasExtra(AppConstant.GROUP_ID)) {
            mNotesType = mIntent.getStringExtra(AppConstant.NOTES_TYPE);
            switch (mNotesType) {
                case AppConstant.NOTES_TYPE_KITTY:
                    mNotesTitle = getResources().getString(R.string.kitty_notes);
                    break;
                case AppConstant.NOTES_TYPE_PERSONAL:
                    mNotesTitle = getResources().getString(R.string.personal_notes);
                    break;
            }
        } else {
            mNotesTitle = getString(R.string.add_personal_notes_dialog_title);
        }
        return mNotesTitle;
    }

    @Override
    boolean isDisplayHomeAsUpEnabled() {
        return true;
    }

    @Override
    boolean hasDrawer() {
        mIntent = getIntent();
        if (mIntent.hasExtra(AppConstant.GROUP_ID)) {
            return false;
        } else {
            return true;
        }
    }

    public void setNotesList(List<NotesResponseDao> responseDao) {
        mTxtEmpty.setVisibility(View.GONE);
        mLvNotes.setVisibility(View.VISIBLE);
        mList = responseDao;
        mAdapter = new NotesListAdapter(NotesActivity.this, responseDao);
        mLvNotes.setAdapter(mAdapter);
        hideLoader();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.fabAddNotes:
                AlertDialogUtils.showCreateNotesDialog(NotesActivity.this, mNotesTitle);
                break;
        }
    }

    public void saveNote(String title, String data) {
        mViewModel.callSaveNoteApi(title, data);
    }


    public void showEmptyLayout(String str) {
        mLvNotes.setVisibility(View.GONE);
        mTxtEmpty.setText(str);
        mTxtEmpty.setVisibility(View.VISIBLE);
    }

    public NotesRequestDao getNotesRequestDaoObject() {
        NotesRequestDao requestDao = new NotesRequestDao();
        if (mIntent.hasExtra(AppConstant.GROUP_ID)) {
            // CONTAIN PERSONAL KITTY NOTES AND KITTY NOTES
            requestDao.setType(mNotesType);
            requestDao.setUserId(PreferanceUtils.getLoginUserObject(this).getUserID());
            requestDao.setGroupId(mGroupiId);
            requestDao.setKitty(mKittyId);
        } else {
            //ONLY PERSONAL NOTE
            requestDao.setType(AppConstant.NOTES_TYPE_PERSONAL);
            requestDao.setUserId(PreferanceUtils.getLoginUserObject(this).getUserID());
        }
        return requestDao;
    }


    public void showLoader() {
        mPbLoader.setVisibility(View.VISIBLE);
    }

    public void hideLoader() {
        mPbLoader.setVisibility(View.GONE);
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
