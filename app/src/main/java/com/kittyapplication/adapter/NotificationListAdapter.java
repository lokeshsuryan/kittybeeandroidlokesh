package com.kittyapplication.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.kittyapplication.R;
import com.kittyapplication.custom.RoundedImageView;
import com.kittyapplication.model.AddAttendanceDao;
import com.kittyapplication.model.NotificationDao;
import com.kittyapplication.model.ServerResponse;
import com.kittyapplication.rest.Singleton;
import com.kittyapplication.ui.activity.NotificationActivity;
import com.kittyapplication.ui.activity.NotificationCardActivity;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.ImageUtils;
import com.kittyapplication.utils.Utils;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Dhaval Riontech on 10/8/16.
 */
public class NotificationListAdapter extends BaseAdapter {
    private static final String TAG = NotificationListAdapter.class.getSimpleName();
    private final Context mContext;
    private final List<NotificationDao> mList;
    private static LayoutInflater inflater = null;
    private ProgressDialog mDialog;

    public NotificationListAdapter(Context context, List<NotificationDao> list) {
        mContext = context;
        mList = list;
        inflater = (LayoutInflater) this.mContext.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Holder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.row_notification, parent, false);
            viewHolder = new Holder();
            viewHolder.imgUser = (RoundedImageView) convertView.findViewById(R.id.imgUser);
            viewHolder.txtTitle = (TextView) convertView.findViewById(R.id.txtNotificationTitle);
            viewHolder.txtYes = (TextView) convertView.findViewById(R.id.txtYes);
            viewHolder.txtNo = (TextView) convertView.findViewById(R.id.txtNo);
            viewHolder.txtMayBe = (TextView) convertView.findViewById(R.id.txtMaybe);
            viewHolder.txtTime = (TextView) convertView.findViewById(R.id.txtTime);
            viewHolder.progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (Holder) convertView.getTag();
        }

        ImageUtils.getImageLoader(mContext).displayImage(mList.get(position).getGroupImage(), viewHolder.imgUser,
                new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        super.onLoadingStarted(imageUri, view);
                        viewHolder.progressBar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        super.onLoadingComplete(imageUri, view, loadedImage);
                        viewHolder.progressBar.setVisibility(View.GONE);
                    }
                });

        viewHolder.txtTitle.setText(mList.get(position).getMessage());
        viewHolder.txtTime.setText(mList.get(position).getNotificationTime());

        if ((mList.get(position).getType().equalsIgnoreCase(AppConstant.ADD_VENUE)
                || mList.get(position).getType().equalsIgnoreCase(AppConstant.EDIT_VENUE))) {
            viewHolder.txtYes.setVisibility(View.VISIBLE);
            viewHolder.txtNo.setVisibility(View.VISIBLE);
            viewHolder.txtMayBe.setVisibility(View.VISIBLE);
            final AddAttendanceDao dao = new AddAttendanceDao();
            dao.setGroupId(mList.get(position).getGroupId());
            dao.setKittyId(mList.get(position).getKittyId());
            dao.setUserId(mList.get(position).getUserId());
            viewHolder.txtYes.setTag(position);
            viewHolder.txtYes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openCardActivity((Integer) v.getTag());

                    /*dao.setAttendanceYes(AppConstant.ATTENDANCE_CHECKED);
                    dao.setAttendanceNo(AppConstant.ATTENDANCE_UNCHECKED);
                    dao.setAttendanceMayBe(AppConstant.ATTENDANCE_UNCHECKED);
                    addAttendance(dao);*/
                }
            });
            viewHolder.txtNo.setTag(position);
            viewHolder.txtNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openCardActivity((Integer) v.getTag());
                    /*dao.setAttendanceYes(AppConstant.ATTENDANCE_UNCHECKED);
                    dao.setAttendanceNo(AppConstant.ATTENDANCE_CHECKED);
                    dao.setAttendanceMayBe(AppConstant.ATTENDANCE_UNCHECKED);
                    addAttendance(dao);*/
                }
            });
            viewHolder.txtMayBe.setTag(position);
            viewHolder.txtMayBe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openCardActivity((Integer) v.getTag());
                    /*dao.setAttendanceYes(AppConstant.ATTENDANCE_UNCHECKED);
                    dao.setAttendanceNo(AppConstant.ATTENDANCE_UNCHECKED);
                    dao.setAttendanceMayBe(AppConstant.ATTENDANCE_CHECKED);
                    addAttendance(dao);*/
                }
            });


        } else {
            viewHolder.txtYes.setVisibility(View.GONE);
            viewHolder.txtNo.setVisibility(View.GONE);
            viewHolder.txtMayBe.setVisibility(View.GONE);
        }

        return convertView;
    }

    public class Holder {
        private RoundedImageView imgUser;
        private TextView txtTitle;
        private TextView txtYes, txtNo, txtMayBe;
        private TextView txtTime;
        private ProgressBar progressBar;
    }

    public void addAttendance(AddAttendanceDao dao) {
        if (Utils.checkInternetConnection(mContext)) {
            showProgressDialog();
            Call<ServerResponse> call = Singleton.getInstance().getRestOkClient().addAttendance(dao);
            call.enqueue(addAttendanceDataCallBack);
        } else {
            ((NotificationActivity) mContext).showSnackbar(mContext.getResources().getString(R.string.no_internet_available));
        }
    }

    Callback<ServerResponse> addAttendanceDataCallBack = new Callback<ServerResponse>() {
        @Override
        public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
            hideProgressDialog();

            if (response.code() == 200) {
                ServerResponse serverResponse = response.body();
                if (serverResponse.getResponse() == AppConstant.RESPONSE_SUCCESS
                        && serverResponse.getMessage().equalsIgnoreCase(AppConstant.SUCCESS)) {

                    ((NotificationActivity) mContext).showSnackbar(serverResponse.getMessage());

                } else {
                    ((NotificationActivity) mContext).showSnackbar(serverResponse.getMessage());
                    AppLog.e(TAG, "Response code = 0");
                }

            } else {
                ((NotificationActivity) mContext).showSnackbar(mContext.getResources()
                        .getString(R.string.server_error));
                AppLog.e(TAG, "HTTP Status code is not 200");
            }
        }

        @Override
        public void onFailure(Call<ServerResponse> call, Throwable t) {
            hideProgressDialog();
            ((NotificationActivity) mContext).showSnackbar(mContext.getResources()
                    .getString(R.string.server_error));
            AppLog.e(TAG, "-" + t.getMessage());
        }
    };

    public void showProgressDialog() {
        mDialog = new ProgressDialog(mContext);
        mDialog.setMessage(mContext.getResources().getString(R.string.loading_text));
        mDialog.show();
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
    }

    public void remove(int position) {
        mList.remove(position);
        notifyDataSetChanged();
    }

    private void openCardActivity(int pos) {
        Intent intent = new Intent(mContext, NotificationCardActivity.class);
        intent.putExtra(AppConstant.NOTIFICATION_CARD,
                new Gson().toJson(mList.get(pos)));
        mContext.startActivity(intent);
    }
}
