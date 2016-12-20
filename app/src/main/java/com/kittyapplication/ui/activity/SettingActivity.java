package com.kittyapplication.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.kittyapplication.R;
import com.kittyapplication.adapter.MediaAdapter;
import com.kittyapplication.adapter.ParticipantAdapter;
import com.kittyapplication.chat.ui.activity.AttachmentZoomActivity;
import com.kittyapplication.custom.CustomTextViewBold;
import com.kittyapplication.custom.CustomTextViewNormal;
import com.kittyapplication.custom.ImageLoaderListener;
import com.kittyapplication.custom.fabmenu.FloatingActionMenu;
import com.kittyapplication.model.ChatData;
import com.kittyapplication.model.NotificationDao;
import com.kittyapplication.model.ParticipantDao;
import com.kittyapplication.ui.viewmodel.SettingViewModel;
import com.kittyapplication.utils.AlertDialogUtils;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.DateTimeUtils;
import com.kittyapplication.utils.ImageUtils;
import com.kittyapplication.utils.Utils;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.core.request.QueryRule;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Pintu Riontech on 17/8/16.
 */
public class SettingActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = SettingActivity.class.getSimpleName();
    private ImageView mImgGroup;
    private ListView mLvMembers;
    private SettingViewModel mViewModel;
    private ParticipantAdapter mAdapter;
    private Toolbar mToolbar;
    private CustomTextViewBold mTxtCount;
    private RecyclerView mRvImages;
    private FloatingActionMenu mFabMenu;
    private LinearLayout llMedia;
    private ArrayList<String> mMediaList = new ArrayList<>();
    private String mDialogId;
    private QBChatDialog qbDialog;
    private View mBlurView;
    private CustomTextViewNormal mTxtEmptyText;
    private String mChatDataString;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        mLvMembers = (ListView) findViewById(R.id.lvChatMember);
        mImgGroup = (ImageView) findViewById(R.id.imgChatGroupImage);
        mChatDataString = getIntent().getStringExtra(AppConstant.INTENT_CHAT);
        qbDialog = (QBChatDialog) getIntent().getSerializableExtra(AppConstant.INTENT_MEDIA_DATA);
        mDialogId = getIntent().getStringExtra(AppConstant.INTENT_DIALOG_ID);


        //mTxtGroupName = (CustomTextViewNormal) findViewById(R.id.txtGroupTitle);
        mToolbar = (Toolbar) findViewById(R.id.toolbarChat);
        mTxtCount = (CustomTextViewBold) findViewById(R.id.txtParticipantCount);
        llMedia = (LinearLayout) findViewById(R.id.llMedia);
        mFabMenu = (FloatingActionMenu) findViewById(R.id.fabMenuSetting);
        mFabMenu.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener() {
            @Override
            public void onMenuToggle(boolean opened) {
                if (opened) {
                    mBlurView.setVisibility(View.VISIBLE);
                    mLvMembers.setEnabled(false);
                } else {
                    mBlurView.setVisibility(View.GONE);
                    mLvMembers.setEnabled(true);
                }
            }
        });

        findViewById(R.id.fabChatAddGroup).setOnClickListener(this);
        findViewById(R.id.fabChatDeleteMember).setOnClickListener(this);
        findViewById(R.id.fabChatKittyRule).setOnClickListener(this);
        findViewById(R.id.fabRightToEdit).setOnClickListener(this);
        findViewById(R.id.txtMediaSeeAll).setOnClickListener(this);
        findViewById(R.id.fabChangeHost).setOnClickListener(this);


        mBlurView = findViewById(R.id.viewTransparent);
        mTxtEmptyText = (CustomTextViewNormal) findViewById(R.id.txtEmptyData);


//        mRvImages = (RecyclerView) findViewById(R.id.rvImages);
//
//        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
//        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
//        mRvImages.setLayoutManager(layoutManager);
//        mRvImages.setAdapter(new SettingMediaAdapter(this, null));
        mViewModel = new SettingViewModel(this);
        mViewModel.getData(mChatDataString);

//        getMediaListFromServer();


    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mFabMenu.open(false);
//        AppLog.d(TAG, "dialogF = " + qbDialog.getDialogId());
//        AppLog.d(TAG, "Dialog Id = " + mDialogId);
        AppLog.d(TAG, "Chat Data String = " + mChatDataString);
//        if (Utils.isValidString(mChatDataString)) {
        mViewModel.getData(mChatDataString);
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    public void showSnackBar(String str) {
        AlertDialogUtils.showSnackToast(str, this);
    }

    public void setDataIntoList(ParticipantDao dao) {
        try {
            ImageUtils.getImageLoader(this).displayImage(dao.getGroupIMG(), mImgGroup);
            mTxtCount.setText(String.valueOf(dao.getCount()));
            ChatData mChatData = new Gson().fromJson(mChatDataString, ChatData.class);
            if (mChatData != null && Utils.isValidString(mChatData.getRule())) {
                mAdapter = new ParticipantAdapter(this, dao.getParticipant(), mChatData.getRule());
            } else {
                mAdapter = new ParticipantAdapter(this, dao.getParticipant(), "0");
            }
            mLvMembers.setAdapter(mAdapter);
            //mTxtGroupName.setText(dao.getName());
            setToolBar(mToolbar, dao.getName());
            mImgGroup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isStoragePermissionGranted()) {
                        mViewModel.changeGroupImage();
                    }
                }
            });

            getImageUrlForBitMap(dao.getGroupIMG());
            mFabMenu.close(false);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }


    @Override
    public void onClick(View v) {
        mFabMenu.close(true);
        switch (v.getId()) {
            case R.id.fabChatAddGroup:
                mViewModel.actionAddMember();
                break;

            case R.id.fabChatDeleteMember:
                mViewModel.actionDeleteMember();
                break;

            case R.id.fabChatKittyRule:
                mFabMenu.close(true);
                mViewModel.actionKittyRule();
                break;

            case R.id.fabChatRefresh:
                mViewModel.actionRefreshGroup();
                break;

            case R.id.fabRightToEdit:
                mViewModel.actionGiveRightToEdit();
                break;

            case R.id.txtMediaSeeAll:
                MediaActivity.startMediaActivity(SettingActivity.this, mMediaList);
                break;

            case R.id.fabChangeHost:
                mViewModel.actionChangeHost();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mInflater = getMenuInflater();
        mInflater.inflate(R.menu.menu_setting, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_setting_edit_name:
                if (mViewModel != null) {
                    mViewModel.changeGroupName();
                }
                break;
//            case R.id.menu_setting_open_media:
//                //startActivity(new Intent(this, MediaActivity.class));
//                break;

            case R.id.menu_action_diary:
                try {
                    if (Utils.isValidString(mChatDataString) && !mChatDataString.equalsIgnoreCase("null")) {
                        ChatData data = new Gson().fromJson(mChatDataString, ChatData.class);
                        NotificationDao notiDao = new NotificationDao();
                        notiDao.setGroupId(data.getGroupID());
                        notiDao.setKittyId(data.getKittyId());

                        AppLog.d(TAG, "Chat DATA = " + mChatDataString);

                        Intent attendanceIntent = new Intent(SettingActivity.this, AttendanceActivity.class);
                        attendanceIntent.putExtra(AppConstant.PUT_ATTENDANCE_EXTRA,
                                new Gson().toJson(notiDao).toString());
                        startActivity(attendanceIntent);
                    }
                } catch (Exception e) {
                    AppLog.e(TAG, e.getMessage(), e);
                }
                return true;

            case R.id.menu_home_jump:
                Utils.openActivity(this, HomeActivity.class);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    private void setToolBar(Toolbar toolBar, String title) {
        setSupportActionBar(toolBar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setTitle(title);
        actionbar.setHomeButtonEnabled(true);
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
                mViewModel.changeGroupImage();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mViewModel.mImagePickerDialog.onActivityResult(requestCode, resultCode, data);
    }

    public void setImage(Bitmap image) {
        mImgGroup.setImageBitmap(image);
        chnageToolbarTextColor(image);
    }

    public void setName(String name) {
        //setToolBar(mToolbar, name);
        //getSupportActionBar().setTitle(name);
        mToolbar.setTitle(name);
        CollapsingToolbarLayout collapsingToolbarLayout =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(name);
    }


    /**
     *
     */
    public void setMediaData() {
        if (mMediaList != null
                && !mMediaList.isEmpty()) {

            AppLog.d(TAG, new Gson().toJson(mMediaList).toString());

            llMedia.setVisibility(View.VISIBLE);
            findViewById(R.id.txtMedia).setVisibility(View.VISIBLE);
            findViewById(R.id.txtMediaSeeAll).setVisibility(View.VISIBLE);

            AppLog.d(TAG, "Size" + mMediaList.size());
            for (int i = 0; i < 6; i++) {
                FrameLayout frame = (FrameLayout) llMedia.getChildAt(i);
                ImageView imgMedia = (ImageView) frame.findViewById(R.id.imgSettingMedia);
                ProgressBar pbloader = (ProgressBar) frame.findViewById(R.id.pbLoaderMediaSettingImage);

                try {
                    if (i <= mMediaList.size()) {
                        if (Utils.isValidString(mMediaList.get(i))) {

                            ImageUtils.getImageLoader(this)
                                    .displayImage(mMediaList.get(i), imgMedia
                                            , ImageUtils.getDisplayOptionProfileRoundedCorner()
                                            , new ImageLoaderListener(pbloader));

                            imgMedia.setTag(mMediaList.get(i));
                            imgMedia.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    AttachmentZoomActivity.
                                            startMediaActivity(SettingActivity.this,
                                                    (String) v.getTag(), v);
                                }
                            });

                            imgMedia.setTag(mMediaList.get(i));
                            imgMedia.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    AttachmentZoomActivity.startMediaActivity(SettingActivity.this, (String) v.getTag(), v);
                                }
                            });

                        } else {
                            frame.setVisibility(View.GONE);
                        }
                    } else {
                        frame.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    frame.setVisibility(View.GONE);
                }
            }
        } else {
            AppLog.d(TAG, "Size ====================");
            llMedia.setVisibility(View.GONE);
            findViewById(R.id.txtMedia).setVisibility(View.GONE);
            findViewById(R.id.txtMediaSeeAll).setVisibility(View.GONE);
        }
    }

    public String getmDialogId() {
        return mDialogId;
    }

    /**
     *
     */
    private void getMediaListFromServer() {
        try {
            QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
            requestBuilder.setLimit(100);
            requestBuilder.addRule("attachments.type", QueryRule.EQ, QBAttachment.PHOTO_TYPE);

            QBRestChatService.getDialogMessages(qbDialog, requestBuilder).performAsync(new QBEntityCallback<ArrayList<QBChatMessage>>() {
                @Override
                public void onSuccess(ArrayList<QBChatMessage> messages, Bundle bundle) {
                    if (messages != null && !messages.isEmpty()) {
                        for (QBChatMessage message : messages) {
                            for (QBAttachment attachment : message.getAttachments()) {
                                if (mMediaList != null && !mMediaList.contains(attachment.getUrl()))
                                    mMediaList.add(attachment.getUrl());
                            }
                        }
                    }
                    setMediaData();
                }

                @Override
                public void onError(QBResponseException e) {
                    setMediaData();
                }
            });

        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }


    private void chnageToolbarTextColor(Bitmap image) {
        if (image != null && !image.isRecycled()) {
            Palette.from(image).generate(paletteListener);
        }
    }

    Palette.PaletteAsyncListener paletteListener = new Palette.PaletteAsyncListener() {
        public void onGenerated(Palette palette) {
            // access palette colors here
            int byDefaultColor = 0x000000;
            CollapsingToolbarLayout collapsingToolbarLayout =
                    (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
            try {

                int vibrant = palette.getVibrantColor(byDefaultColor);
                int vibrantLight = palette.getLightVibrantColor(byDefaultColor);
                int vibrantDark = palette.getDarkVibrantColor(byDefaultColor);
                int muted = palette.getMutedColor(byDefaultColor);
                int mutedLight = palette.getLightMutedColor(byDefaultColor);
                int mutedDark = palette.getDarkMutedColor(byDefaultColor);


                int titleTextColor = 0;
                int bodyTextColor = 0;
                Palette.Swatch swatch = palette.getVibrantSwatch();
                titleTextColor = swatch.getTitleTextColor();
                bodyTextColor = swatch.getBodyTextColor();
                    /*AppLog.d(TAG, "vibrant " + vibrant);
                    AppLog.d(TAG, "vibrantLight " + vibrantLight);
                    AppLog.d(TAG, "vibrantDark " + vibrantDark);
                    AppLog.d(TAG, "muted " + muted);
                    AppLog.d(TAG, "mutedLight " + mutedLight);
                    AppLog.d(TAG, "mutedDark " + mutedDark);
                    AppLog.d(TAG, "titleTextColor " + titleTextColor);
                    AppLog.d(TAG, "bodyTextColor " + bodyTextColor);*/
                collapsingToolbarLayout.setExpandedTitleColor(titleTextColor);


                //mToolbar.setTitleTextColor(mutedDark);
            } catch (Exception e) {
                collapsingToolbarLayout.setExpandedTitleColor(ContextCompat
                        .getColor(SettingActivity.this, R.color.black));
            }
        }
    };

    private void getImageUrlForBitMap(String url) {
        final String strUrl = url;
        try {
            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(final Void... params) {
                    try {
                        if (Utils.isValidString(strUrl)) {
                            URL url = new URL(strUrl);
                            Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                            chnageToolbarTextColor(image);
                        } else {
                            AppLog.d(TAG, "ELSE");
                        }
                    } catch (IOException e) {
                        AppLog.e(TAG, e.getMessage(), e);
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(final Void result) {
                    super.onPostExecute(result);
                }

            }.execute();
        } catch (final Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public void setRefreshMenuButton(String date) {
        try {
            if (Utils.isValidString(date)
                    && DateTimeUtils
                    .checkCurrentDateIsAfterKittyDate(date)) {
                findViewById(R.id.fabChatRefresh).setVisibility(View.VISIBLE);
                findViewById(R.id.fabChatRefresh).setOnClickListener(this);
            } else {
                findViewById(R.id.fabChatRefresh).setVisibility(View.GONE);
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public void changeHostButtonVisibility(boolean visibility) {
        if (visibility) {
            findViewById(R.id.fabChangeHost).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.fabChangeHost).setVisibility(View.GONE);
        }
    }

    public void showEmptyView() {
        mBlurView.setVisibility(View.VISIBLE);
        mTxtEmptyText.setVisibility(View.VISIBLE);
        findViewById(R.id.rlFabMenu).setVisibility(View.GONE);
        findViewById(R.id.labelParticipant).setVisibility(View.GONE);
        mTxtCount.setVisibility(View.GONE);
    }

    public void hideEmptyView() {
        mBlurView.setVisibility(View.GONE);
        mTxtEmptyText.setVisibility(View.GONE);
        findViewById(R.id.labelParticipant).setVisibility(View.VISIBLE);
        mTxtCount.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mViewModel != null) {
            mViewModel.destroyApiCall();
        }
    }
}
