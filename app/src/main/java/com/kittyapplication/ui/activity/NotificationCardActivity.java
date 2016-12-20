package com.kittyapplication.ui.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.kittyapplication.R;
import com.kittyapplication.model.AddAttendanceDao;
import com.kittyapplication.model.NotificationDao;
import com.kittyapplication.model.ServerResponse;
import com.kittyapplication.model.VenueResponseDao;
import com.kittyapplication.rest.Singleton;
import com.kittyapplication.ui.viewmodel.NotificationCardViewModel;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.ImageUtils;
import com.kittyapplication.utils.Utils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Dhaval Soneji : sonejidhavalm@gmail.com on 24/8/16.
 */
public class NotificationCardActivity extends BaseActivity {
    private static final String TAG = NotificationCardActivity.class.getSimpleName();
    private NotificationDao mNotificationData;
    private ImageView mImgInvitationCard;
    private TextView mTxtYes, mTxtNo, mTxtMaybe;
    private NotificationCardViewModel mViewModel;
    private ProgressDialog mDialog;
    private TextView mTxtDateInviteCard, mTxtTimeInviteCard,
            mTxtVenueInviteCard, mTxtKittyName,
            mTxtHostInviteCard, mTxtAddress,
            mTxtDressCode, mTxtPuncTimeOne,
            mTxtPuncTimeTwo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(NotificationCardActivity.this).inflate(
                R.layout.activity_invitation_card, null);

        Intent intent = getIntent();
        mNotificationData = new NotificationDao();
        mNotificationData = new Gson().fromJson
                (intent.getStringExtra(AppConstant.NOTIFICATION_CARD), NotificationDao.class);

        mTxtDateInviteCard = (TextView) view.findViewById(R.id.txtInviteKittyDate);
        mTxtTimeInviteCard = (TextView) view.findViewById(R.id.txtKittyTime);
        mTxtVenueInviteCard = (TextView) view.findViewById(R.id.txtInviteKittyVenue);
        mTxtKittyName = (TextView) view.findViewById(R.id.txtInviteKittyName);
        mTxtHostInviteCard = (TextView) view.findViewById(R.id.txthost);
        mTxtAddress = (TextView) view.findViewById(R.id.txtInviteKittyAddress);
        mTxtDressCode = (TextView) view.findViewById(R.id.txtInviteKittyDressCode);
        mTxtPuncTimeOne = (TextView) view.findViewById(R.id.txtInviteKittyPunctualTimeOne);
        mTxtPuncTimeTwo = (TextView) view.findViewById(R.id.txtInviteKittyPunctualTimeTwo);

        mTxtHostInviteCard.setText(getResources().getString(R.string.host_txt,
                mNotificationData.getHostName()));

        mImgInvitationCard = (ImageView) view.findViewById(R.id.imgInvitationCard);
        ImageUtils.getImageLoader(this).displayImage("drawable://" + R.drawable.ic_invite_card, mImgInvitationCard);
        mTxtYes = (TextView) view.findViewById(R.id.txtYes);
        mTxtNo = (TextView) view.findViewById(R.id.txtNo);
        mTxtMaybe = (TextView) view.findViewById(R.id.txtMaybe);
        mTxtYes.setOnClickListener(this);
        mTxtNo.setOnClickListener(this);
        mTxtMaybe.setOnClickListener(this);

        mViewModel = new NotificationCardViewModel(NotificationCardActivity.this);
        addLayoutToContainer(view);
        hideLeftIcon();
        hideBottomLayout();

        initRequest(mNotificationData.getKittyId());
    }

    @Override
    String getActionTitle() {
        return getResources().getString(R.string.invitation_card_title);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (hasDrawer()) {
                    toggle();
                } else {
                    onBackPressed();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        final AddAttendanceDao dao = new AddAttendanceDao();
        dao.setGroupId(mNotificationData.getGroupId());
        dao.setKittyId(mNotificationData.getKittyId());
        dao.setUserId(mNotificationData.getUserId());
        super.onClick(v);
        switch (v.getId()) {
            case R.id.txtYes:
                dao.setAttendanceYes(AppConstant.ATTENDANCE_CHECKED);
                dao.setAttendanceNo(AppConstant.ATTENDANCE_UNCHECKED);
                dao.setAttendanceMayBe(AppConstant.ATTENDANCE_UNCHECKED);
                mViewModel.addAttendance(dao);
                break;
            case R.id.txtNo:
                dao.setAttendanceYes(AppConstant.ATTENDANCE_UNCHECKED);
                dao.setAttendanceNo(AppConstant.ATTENDANCE_CHECKED);
                dao.setAttendanceMayBe(AppConstant.ATTENDANCE_UNCHECKED);
                mViewModel.addAttendance(dao);
                break;
            case R.id.txtMaybe:
                dao.setAttendanceYes(AppConstant.ATTENDANCE_UNCHECKED);
                dao.setAttendanceNo(AppConstant.ATTENDANCE_UNCHECKED);
                dao.setAttendanceMayBe(AppConstant.ATTENDANCE_CHECKED);
                mViewModel.addAttendance(dao);
                break;
        }
    }

    /**
     * Show progress dialog
     */
    public void showProgressDialog() {
        mDialog = new ProgressDialog(NotificationCardActivity.this);
        mDialog.setMessage(getResources().getString(R.string.loading_text));
        mDialog.show();
    }

    /**
     * Dismiss progress dialog
     */
    public void hideProgressDialog() {
        try {
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss();
                mDialog = null;
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    /**
     * @param kittyId
     */
    private void initRequest(String kittyId) {
        if (Utils.checkInternetConnection(this)) {
            showProgressDialog();
            Call<ServerResponse<List<VenueResponseDao>>> call =
                    Singleton.getInstance().getRestOkClient().getVenue(kittyId);
            call.enqueue(getVenueCallBack);

        } else {
            showSnackbar(getResources().getString(R.string.no_internet_connection));
        }
    }

    /**
     *
     */
    private Callback<ServerResponse<List<VenueResponseDao>>> getVenueCallBack
            = new Callback<ServerResponse<List<VenueResponseDao>>>() {
        @Override
        public void onResponse(Call<ServerResponse<List<VenueResponseDao>>> call,
                               Response<ServerResponse<List<VenueResponseDao>>> response) {
            try {
                hideProgressDialog();

                if (response.code() == 200) {
                    ServerResponse<List<VenueResponseDao>> serverResponse = response.body();
                    if (serverResponse.getData() != null && !serverResponse.getData().isEmpty()) {
                        setVenue(serverResponse.getData().get(0));
                    } else {
                        showSnackbar(serverResponse.getMessage());
                    }
                } else {
                    showSnackbar(getResources()
                            .getString(R.string.server_error));
                }
            } catch (Exception e) {
                showSnackbar(getResources()
                        .getString(R.string.server_error));
            }
        }

        @Override
        public void onFailure(Call<ServerResponse<List<VenueResponseDao>>> call, Throwable t) {
            hideProgressDialog();
            showSnackbar(getResources()
                    .getString(R.string.server_error));
            AppLog.e(TAG, "," + t.getMessage());
        }
    };

    /**
     * @param venueResponse
     */
    public void setVenue(VenueResponseDao venueResponse) {
        if (Utils.isValidString(venueResponse.getVanue())) {
            mTxtVenueInviteCard.setVisibility(View.VISIBLE);
            mTxtVenueInviteCard.setText(getResources().getString(R.string.venue_txt, venueResponse.getVanue()));
        } else {
            mTxtVenueInviteCard.setVisibility(View.GONE);
        }

        if (Utils.isValidString(venueResponse.getKittyDate())) {
            mTxtDateInviteCard.setVisibility(View.VISIBLE);
            mTxtDateInviteCard.setText(getResources().getString(R.string.date_txt, venueResponse.getKittyDate()));
        } else {
            mTxtDateInviteCard.setVisibility(View.GONE);
        }

        if (Utils.isValidString(venueResponse.getVenueTime())) {
            mTxtTimeInviteCard.setVisibility(View.VISIBLE);
            mTxtTimeInviteCard.setText(getResources().getString(R.string.time_txt, venueResponse.getVenueTime()));
        } else {
            mTxtTimeInviteCard.setVisibility(View.GONE);
        }

        if (Utils.isValidString(mNotificationData.getGroupName())) {
            mTxtKittyName.setVisibility(View.VISIBLE);
            mTxtKittyName.setText(mNotificationData.getGroupName());
        } else {
            mTxtKittyName.setVisibility(View.GONE);
        }

        if (Utils.isValidList(mNotificationData.getHostName())) {
            mTxtHostInviteCard.setVisibility(View.VISIBLE);
            //Display name by number
            for (int i = 0; i < mNotificationData.getHostnumber().size(); i++) {
                if (Utils.isValidString(mNotificationData.getHostnumber().get(i))) {
                    String name = Utils.getNameForDiary(this,
                            mNotificationData.getHostnumber().get(i),
                            mNotificationData.getHostName().get(i));
                    mNotificationData.getHostName().set(i, name);
                }
            }
            mTxtHostInviteCard.setText(getResources().getString(R.string.host_txt,
                    TextUtils.join(", ", mNotificationData.getHostName())));
        } else {
            mTxtHostInviteCard.setVisibility(View.GONE);
        }

        if (Utils.isValidString(venueResponse.getAddress())) {
            mTxtAddress.setVisibility(View.VISIBLE);
            mTxtAddress.setText(getResources().getString(R.string.address_card, venueResponse.getAddress()));
        } else {
            mTxtAddress.setVisibility(View.GONE);
        }

        if (Utils.isValidString(venueResponse.getDressCode())) {
            mTxtDressCode.setVisibility(View.VISIBLE);
            mTxtDressCode.setText(getResources().getString(R.string.dress_card, venueResponse.getDressCode()));
        } else {
            mTxtDressCode.setVisibility(View.GONE);
        }

        if (Utils.isValidString(venueResponse.getPunctuality())) {
            mTxtPuncTimeOne.setVisibility(View.VISIBLE);
            mTxtPuncTimeOne.setText(getResources().getString(R.string.punc_one_card, venueResponse.getPunctuality()));
        } else {
            mTxtPuncTimeOne.setVisibility(View.GONE);
        }

        if (Utils.isValidString(venueResponse.getPunctuality2())) {
            mTxtPuncTimeTwo.setVisibility(View.VISIBLE);
            mTxtPuncTimeTwo.setText(getResources().getString(R.string.punc_two_card, venueResponse.getPunctuality2()));
        } else {
            mTxtPuncTimeTwo.setVisibility(View.GONE);
        }
    }
}
