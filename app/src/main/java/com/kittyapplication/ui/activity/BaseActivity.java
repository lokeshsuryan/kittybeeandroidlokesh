package com.kittyapplication.ui.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kittyapplication.R;
import com.kittyapplication.adapter.PromotionalRecylerAdapter;
import com.kittyapplication.custom.CustomTextViewNormal;
import com.kittyapplication.listener.SearchListener;
import com.kittyapplication.model.PromotionalItemObject;
import com.kittyapplication.ui.viewmodel.BaseViewModel;
import com.kittyapplication.utils.AlertDialogUtils;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.ImageUtils;
import com.kittyapplication.utils.PreferanceUtils;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pintu Riontech on 6/8/16.
 * vaghela.pintu31@gmail.com
 */
public abstract class BaseActivity extends AppCompatActivity implements View.OnClickListener,
        NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = BaseActivity.class.getSimpleName();

    private ProgressDialog mDialog;
    private AppBarLayout mAppBarLayout;
    private RelativeLayout mRlContainer;
    private DrawerLayout mDrawerLayout;
    private LinearLayout mLlBottom;
    private RelativeLayout mRlNoInterNet;
    private CoordinatorLayout mRootLayout;
    private int mDrawerIcon = R.mipmap.ic_launcher;
    private RelativeLayout mRlNotification;
    private ImageView mImgNotification, mImgProfile;
    private BaseViewModel mViewModel;
    private ImageView mImgDrawerUserImage;
    private TextView mTxtDrawerUserName;
    private ProgressBar mProgress;
    private NotificationReceiver mNotificationReceiver;
    private boolean mIsNotificationVisible;
    private ProgressBar mPbLoader;
    private MaterialSearchView mSearchbar;


    private LinearLayout animation_slide;
    private TextView cancel_animation;
    Animation slide_down,slide_up;
    boolean slide_check = false;
    RecyclerView permotional_list;
    private GridLayoutManager lLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        initView();
    }

    /**
     * Add layout To Container
     *
     * @param view
     */
    public void addLayoutToContainer(View view) {
        mRlContainer.removeAllViews();

        int matchParent = RelativeLayout.LayoutParams.MATCH_PARENT;
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(matchParent, matchParent);
        params.setMargins(0, 0, 0, 0);
        view.setLayoutParams(params);

        mRlContainer.addView(view);
        setupVersionText();
    }

    private void initView() {
        try {


            //Load animation
            slide_down = AnimationUtils.loadAnimation(getApplicationContext(),
                    R.anim.slide_down);

            slide_up = AnimationUtils.loadAnimation(getApplicationContext(),
                    R.anim.slide_up);

            mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            mViewModel = new BaseViewModel(this);

            mRootLayout = (CoordinatorLayout) mDrawerLayout.findViewById(R.id.main_content);
            mRlContainer = (RelativeLayout) mDrawerLayout.findViewById(R.id.rlContainer);
            Toolbar toolbar = (Toolbar) mDrawerLayout.findViewById(R.id.toolbarHome);
            setActionBar(toolbar);
            mAppBarLayout = (AppBarLayout) findViewById(R.id.appBarHome);
            TextView txtToolbarTitle = (TextView) mAppBarLayout.findViewById(R.id.txtToolbarTitleHome);
            txtToolbarTitle.setText(getActionTitle());

            mRlNotification = (RelativeLayout) mAppBarLayout.findViewById(R.id.rlNotification);
            mImgNotification = (ImageView) mAppBarLayout.findViewById(R.id.imgNotification);
            mImgProfile = (ImageView) mAppBarLayout.findViewById(R.id.imgProfile);


            final NavigationView navigationView = (NavigationView)
                    mDrawerLayout.findViewById(R.id.nav_view);
            View view = navigationView.getHeaderView(0);

            mProgress = (ProgressBar) view.findViewById(R.id.progressCenter);
            mImgDrawerUserImage = (ImageView) view.findViewById(R.id.imgDrawerUserIcon);
            mTxtDrawerUserName = (TextView) view.findViewById(R.id.txtDrawerUserName);
            navigationView.setNavigationItemSelectedListener(this);

            mViewModel.setUpDrawerItem(mDrawerLayout);

            mRlNoInterNet = (RelativeLayout) findViewById(R.id.rlNoInternet);

            mLlBottom = (LinearLayout) findViewById(R.id.llBottom);
            mLlBottom.setVisibility(View.VISIBLE);

            animation_slide = (LinearLayout)findViewById(R.id.animation_slide);
            cancel_animation = (TextView)findViewById(R.id.cancel_animation);
            cancel_animation.setOnClickListener(this);

            permotional_list = (RecyclerView)findViewById(R.id.permotional_list);


            if (hasDrawer()) {
                //            abc_ic_ab_back_mtrl_am_alpha
                final Drawable upArrow = ContextCompat.getDrawable(this,
                        R.drawable.ic_drawer_icon);
                upArrow.setColorFilter(ContextCompat.getColor(this, R.color.white),
                        PorterDuff.Mode.SRC_ATOP);
                // setHomeIndicator(upArrow);
                getSupportActionBar().setHomeAsUpIndicator(upArrow);
            } else {
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                final Drawable upArrow = ContextCompat.getDrawable(this,
                        R.drawable.ic_back_white);

                //            upArrow.setColorFilter(ContextCompat.getColor(this, R.color.white),
                //                    PorterDuff.Mode.SRC_ATOP);

                getSupportActionBar().setHomeAsUpIndicator(upArrow);
            }

            mPbLoader = (ProgressBar) mRootLayout.findViewById(R.id.pbLoaderBaseActivity);

            mSearchbar = (MaterialSearchView) mRootLayout.findViewById(R.id.searchViewBar);

            clickEvents();

            setDrawerHeaderItems();

            setPromotionalList();
        } catch (Exception e) {
        }

    }


    public void setPromotionalList()
    {
        List<PromotionalItemObject> rowListItem = getAllItemList();
        lLayout = new GridLayoutManager(this, 3);

        permotional_list.setHasFixedSize(true);
        permotional_list.setLayoutManager(lLayout);

        PromotionalRecylerAdapter rcAdapter = new PromotionalRecylerAdapter(this, rowListItem);
        permotional_list.setAdapter(rcAdapter);
    }



    private List<PromotionalItemObject> getAllItemList(){

        List<PromotionalItemObject> allItems = new ArrayList<PromotionalItemObject>();
        allItems.add(new PromotionalItemObject("Foot Wear", R.mipmap.footwear_200));
        allItems.add(new PromotionalItemObject("Fashion", R.mipmap.fashion_200));
        allItems.add(new PromotionalItemObject("Beauty",  R.mipmap.beauty_200));
        allItems.add(new PromotionalItemObject("Nail Art",  R.mipmap.nail_art));
        allItems.add(new PromotionalItemObject("GYM",  R.mipmap.gym_200));


        return allItems;
    }

    /**
     * Set toolbar As Actionbar
     *
     * @param toolbar
     */
    public void setActionBar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(isDisplayHomeAsUpEnabled());
        actionbar.setTitle("");
        actionbar.setHomeButtonEnabled(true);
    }

    abstract String getActionTitle();

    abstract boolean isDisplayHomeAsUpEnabled();

    /**
     * activity has drawer or not
     *
     * @return true/false
     */
    abstract boolean hasDrawer();

    /**
     * set home indicator Icon
     *
     * @param resId
     */
    public void setHomeIndicator(int resId) {
        getSupportActionBar().setHomeAsUpIndicator(
                ContextCompat.getDrawable(getApplicationContext(), resId));
    }

    /**
     * Drawer Toggle View
     */
    public void toggle() {
        if (mDrawerLayout.isDrawerVisible(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        return super.onCreateOptionsMenu(menu);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case android.R.id.home:
//                if (hasDrawer()) {
//                    toggle();
//                } else {
//                    onBackPressed();
//                }
//                break;
//        }
//        return true;
//    }
//
//    @Override
//    public boolean onPrepareOptionsMenu(Menu menu) {
//        return true;
//    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.llBottomJewellery:

                if(slide_check==false)
                {
                    // Start animation
                    animation_slide.setVisibility(View.VISIBLE);
                    animation_slide.startAnimation(slide_up);

                    slide_check = true;
                }
                else
                {
                    animation_slide.startAnimation(slide_down);
                    animation_slide.setVisibility(View.GONE);

                    slide_check = false;

                }


               // mViewModel.onBottomItemSelect(0);
                break;
            case R.id.cancel_animation:

                animation_slide.startAnimation(slide_down);
                animation_slide.setVisibility(View.GONE);

                slide_check = false;
                break;

            case R.id.llBottomRestaurant:
                mViewModel.onBottomItemSelect(1);
                break;

            case R.id.llBottomParlour:
                Intent intent = new Intent(this, HeadsUpActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
//                mViewModel.onBottomItemSelect(2);
                break;

            case R.id.llBottomBoutique:
                mViewModel.onBottomItemSelect(3);
                break;

            case R.id.llBottomPartners:
                mViewModel.onBottomItemSelect(4);
                break;

            case R.id.imgNotification:
                startActivity(new Intent(BaseActivity.this, NotificationActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                break;
            case R.id.imgProfile:
                startActivity(new Intent(BaseActivity.this, ProfileActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                break;
            case R.id.txtDrawerUserName:
                startActivity(new Intent(BaseActivity.this, ProfileActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                break;
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        if (mSearchbar != null && mSearchbar.isSearchOpen()) {
            mSearchbar.closeSearch();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * click event of items
     */
    private void clickEvents() {
        mLlBottom.findViewById(R.id.llBottomBoutique).setOnClickListener(this);
        mLlBottom.findViewById(R.id.llBottomJewellery).setOnClickListener(this);
        mLlBottom.findViewById(R.id.llBottomParlour).setOnClickListener(this);
        mLlBottom.findViewById(R.id.llBottomPartners).setOnClickListener(this);
        mLlBottom.findViewById(R.id.llBottomRestaurant).setOnClickListener(this);
        mImgProfile.setOnClickListener(this);
        mImgNotification.setOnClickListener(this);
        mImgDrawerUserImage.setOnClickListener(this);
        mTxtDrawerUserName.setOnClickListener(this);
    }


    public void showSnackbar(String str) {
//        Snackbar.make(mRootLayout, str, Snackbar.LENGTH_LONG).show();
        AlertDialogUtils.showSnackToast(str, this);
    }

    public ViewGroup getContainer() {
        return mRlContainer;
    }

    public void hideBottomLayout() {
        mLlBottom.setVisibility(View.GONE);
    }

    public void showProgressDialog() {
        mDialog = null;
        mDialog = new ProgressDialog(this);
        mDialog.setMessage(getResources().getString(R.string.loading_text));
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);
        mDialog.show();
        // mPbLoader.setVisibility(View.VISIBLE);
    }

    public void hideProgressDialog() {
        try {
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss();
                mDialog = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //mPbLoader.setVisibility(View.GONE);
    }

    public void hideLeftIcon() {
        mImgProfile.setVisibility(View.GONE);
        mImgNotification.setVisibility(View.GONE);
        mIsNotificationVisible = false;
    }

    public void visibleLeftIcon() {
        mImgProfile.setVisibility(View.VISIBLE);
        mImgNotification.setVisibility(View.VISIBLE);
        mIsNotificationVisible = true;
    }

    public void setActionbarTitle(String str) {
        TextView txtToolbarTitle = (TextView) mAppBarLayout.findViewById(R.id.txtToolbarTitleHome);
        txtToolbarTitle.setText(str);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START))
            mDrawerLayout.closeDrawer(GravityCompat.START);
        clickEvents();
        mNotificationReceiver = new NotificationReceiver();
        registerReceiver(mNotificationReceiver, new IntentFilter(AppConstant.NOTIFICAION_ATTENTION));
        if (PreferanceUtils.getHasNotification(this)) {
            showNotificationStar();
        } else {
            hideNotificationStar();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mNotificationReceiver);
    }

    public void showBottomLayout() {
        mLlBottom.setVisibility(View.VISIBLE);
    }

    public void setDrawerHeaderItems() {
//        String gender = PreferanceUtils.getLoginUserObject(BaseActivity.this).get
        ImageUtils.getImageLoader(BaseActivity.this).displayImage(
                PreferanceUtils.getLoginUserObject(BaseActivity.this).getProfilePic(), mImgDrawerUserImage,
                new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        super.onLoadingStarted(imageUri, view);
                        mProgress.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        super.onLoadingComplete(imageUri, view, loadedImage);
                        mProgress.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {
                        super.onLoadingCancelled(imageUri, view);
                        mProgress.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        super.onLoadingFailed(imageUri, view, failReason);
                        mProgress.setVisibility(View.GONE);
//                        if (gender.equalsIgnoreCase("female")) {
//                            mImgDrawerUserImage.setImageDrawable(ContextCompat.getDrawable(BaseActivity.this, R.drawable.ic_female));
//                        } else {
//                            mImgDrawerUserImage.setImageDrawable(ContextCompat.getDrawable(BaseActivity.this, R.drawable.ic_male));
//                        }
                    }
                });

        mTxtDrawerUserName.setText(PreferanceUtils.getLoginUserObject(BaseActivity.this).getName());
    }

    public <T extends View> T _findViewById(int viewId) {
        return (T) findViewById(viewId);
    }

    public void setDrawerItem() {
        toggle();
    }

    public void showNotificationStar() {
        if (mIsNotificationVisible)
            findViewById(R.id.txtBadgeText).setVisibility(View.VISIBLE);
        else
            hideNotificationStar();
//        mImgNotification.setImageDrawable(Utils.buildCounterDrawable(true, R.drawable.ic_drawer_notification, this, mRlNotification));
    }

    public void hideNotificationStar() {
        AppLog.d(TAG, "hideNotificationStar Called...........");
        findViewById(R.id.txtBadgeText).setVisibility(View.GONE);
//        mImgNotification.setImageDrawable(Utils.buildCounterDrawable(false, R.drawable.ic_drawer_notification, this, mRlNotification));
    }

    private class NotificationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean hasNotification = intent.getBooleanExtra(AppConstant.NOTIFICAION_ATTENTION, false);
            if (hasNotification) {
                showNotificationStar();
            }
        }
    }


    public void setUpSearchBar(MaterialSearchView searchView, final SearchListener listener) {
        searchView.setVoiceSearch(false);
        searchView.setCursorDrawable(R.drawable.search_bar_bg);
        searchView.setCursorDrawable(R.drawable.color_cursor_white);
        searchView.setEllipsize(true);
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                AppLog.e(TAG, "onQueryTextSubmit");
                listener.getSearchString(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                listener.getSearchString(newText);
                return false;
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                mSearchbar.setVisibility(View.VISIBLE);
                mAppBarLayout.setVisibility(View.GONE);
                listener.onSearchBarVisible();
                AppLog.e(TAG, "onSearchViewShown");
                //Do some magic
            }

            @Override
            public void onSearchViewClosed() {
                //Do some magic
                mSearchbar.setVisibility(View.GONE);
                mAppBarLayout.setVisibility(View.VISIBLE);
                listener.onSearchBarHide();
                AppLog.e(TAG, "onSearchViewClosed");
            }
        });
    }

    public MaterialSearchView getSearchbar() {
        return mSearchbar;
    }

    private void setupVersionText() {
        try {
            PackageManager manager = this.getPackageManager();
            final PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            CustomTextViewNormal txtVersion = (CustomTextViewNormal) findViewById(R.id.txtcurrentAppVersion);
            txtVersion.setText(this.getResources().getString(R.string.version_v, info.versionName));
        } catch (Exception e) {

        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_drawer_home:
                mViewModel.onDrawerItemClickEvent(0);
                break;

            case R.id.menu_drawer_my_profile:
                mViewModel.onDrawerItemClickEvent(1);
                break;

            case R.id.menu_drawer_notification:
                mViewModel.onDrawerItemClickEvent(2);
                break;

            case R.id.menu_drawer_add_group:
                mViewModel.onDrawerItemClickEvent(3);
                break;

            case R.id.menu_drawer_my_kitty:
                mViewModel.onDrawerItemClickEvent(4);
                break;

            case R.id.menu_drawer_kitty_manager:
                mViewModel.onDrawerItemClickEvent(5);
                break;

            case R.id.menu_drawer_personal_notes:
                mViewModel.onDrawerItemClickEvent(6);
                break;

            case R.id.menu_drawer_contact_us:
                mViewModel.onDrawerItemClickEvent(7);
                break;

            case R.id.menu_drawer_about_us:
                mViewModel.onDrawerItemClickEvent(8);
                break;
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
