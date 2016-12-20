package com.kittyapplication.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.kittyapplication.R;
import com.kittyapplication.adapter.AddMemberInGroupAdapter;
import com.kittyapplication.adapter.MembersImageAdapter;
import com.kittyapplication.custom.CustomEditTextNormal;
import com.kittyapplication.listener.SearchListener;
import com.kittyapplication.model.ContactDao;
import com.kittyapplication.ui.viewinterface.AddGroupInterface;
import com.kittyapplication.ui.viewmodel.AddGroupViewModel;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.Utils;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pintu Riontech on 10/8/16.
 * vaghela.pintu31@gmail.com
 */
public class AddGroupActivity extends BaseActivity implements AddGroupInterface, SearchListener {
    private static final String TAG = AddGroupActivity.class.getSimpleName();
    private ListView mLvContactList;
    private ImageView mGroupImage;
    private CustomEditTextNormal mEdtGroupName;
    private AddGroupViewModel mViewModel;
    private boolean isSearch = false;
    private AddMemberInGroupAdapter mAdapter;
    private RecyclerView mRvSelectedMembers;
    private MembersImageAdapter mImgAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(AddGroupActivity.this).inflate(
                R.layout.activity_add_group, null);
        addLayoutToContainer(view);
        hideLeftIcon();
        hideBottomLayout();
        mLvContactList = (ListView) view.findViewById(R.id.rvAddGroupMember);
        mGroupImage = (ImageView) view.findViewById(R.id.imgAddGroupUser);
        mEdtGroupName = (CustomEditTextNormal) view.findViewById(R.id.edtAddGroupName);
        mViewModel = new AddGroupViewModel(this);
        final String kittyType = getIntent().getStringExtra("type");
        AppLog.d(TAG, "Kitty Type " + kittyType);

        mRvSelectedMembers = (RecyclerView) view.findViewById(R.id.rvMembers);

        view.findViewById(R.id.txtAddGroupNext)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mAdapter != null)
                            mViewModel.clickOnNextButton(mAdapter, kittyType, mEdtGroupName);
                    }
                });

        view.findViewById(R.id.imgAddGroupUser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isStoragePermissionGranted()) {
                    mViewModel.setGroupImageIcon();
                }
            }
        });

        setUpSearchBar(getSearchbar(), this);

    }

    @Override
    String getActionTitle() {
        return getResources().getString(R.string.add_group);
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
        mAdapter = new AddMemberInGroupAdapter(this, list);
        mLvContactList.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mInflater = getMenuInflater();
        mInflater.inflate(R.menu.menu_create_group, menu);
        MenuItem item = menu.findItem(R.id.menu_create_group_search);
        getSearchbar().setMenuItem(item);

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
            /*case R.id.menu_create_group_search:
                if (!isSearch) {
                    isSearch = true;
                    mViewModel.getSearchView(mAdapter, true);
                } else {
                    isSearch = false;
                    mViewModel.getSearchView(mAdapter, false);
                }
                break;*/

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


    /**
     * @return
     */
    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                AppLog.d(TAG, "Permission is granted");
                return true;
            } else {

                AppLog.d(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.CAMERA}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            AppLog.d(TAG, "Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            AppLog.d(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
            //resume tasks needing this permission
            if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                AppLog.d(TAG, "Permission: " + permissions[1] + "was " + grantResults[1]);
                mViewModel.setGroupImageIcon();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);
            if (mViewModel != null && mViewModel.mImagePickerDialog != null)
                mViewModel.mImagePickerDialog.onActivityResult(requestCode, resultCode, data);
            else {
                if (requestCode == MaterialSearchView.REQUEST_VOICE && resultCode == RESULT_OK) {
                    ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (matches != null && matches.size() > 0) {
                        String searchWrd = matches.get(0);
                        if (!TextUtils.isEmpty(searchWrd)) {
                            getSearchbar().setQuery(searchWrd, false);
                        }
                    }
                }

                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void getMemberImageList(List<ContactDao> list) {
        if (Utils.isValidList(list)) {
            mRvSelectedMembers.setVisibility(View.VISIBLE);
            if (mImgAdapter == null) {
                LinearLayoutManager layoutManager
                        = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
                mRvSelectedMembers.setLayoutManager(layoutManager);
                mImgAdapter = new MembersImageAdapter(list, AddGroupActivity.this);
                mRvSelectedMembers.setAdapter(mImgAdapter);
            } else {
                mImgAdapter.changeList(list);
            }
        } else {
            mRvSelectedMembers.setVisibility(View.GONE);
        }
    }

    public AddMemberInGroupAdapter getAdapter() {
        return mAdapter;
    }


    /**
     * get image string from path
     *
     * @param path
     * @return
     */
    public String getStringFromURIPath(String path) {
        String img = "";
        if (Utils.isValidString(path)) {
            File imgFile = new File(path);
            if (imgFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                img = Utils.getImageInString(bitmap);
            }
        }
        return img;
    }

    @Override
    public void getSearchString(String str) {
        if (mAdapter != null) {
            mAdapter.getFilter().filter(str);
        }
    }

    @Override
    public void onSearchBarVisible() {
        onSearchHideShowComponentVisibility(View.GONE);
    }

    @Override
    public void onSearchBarHide() {
        onSearchHideShowComponentVisibility(View.VISIBLE);
    }

    private void onSearchHideShowComponentVisibility(int visibility) {
        findViewById(R.id.txtAddGroupNext).setVisibility(visibility);
        findViewById(R.id.imgAddGroupUser).setVisibility(visibility);
        findViewById(R.id.txtLabelAddGroup).setVisibility(visibility);
        findViewById(R.id.txtLabelGroupIcon).setVisibility(visibility);
        findViewById(R.id.edtAddGroupName).setVisibility(visibility);
        findViewById(R.id.viewDividerAddGroup).setVisibility(visibility);
    }
}
