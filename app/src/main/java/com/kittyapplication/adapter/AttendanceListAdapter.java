package com.kittyapplication.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kittyapplication.R;
import com.kittyapplication.model.AttendanceDataDao;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.Utils;

import java.util.List;

/**
 * Created by Dhaval Riontech on 11/8/16.
 */
public class AttendanceListAdapter extends BaseAdapter {

    private List<AttendanceDataDao> mList;
    private Context mContext;
    private static LayoutInflater inflater = null;

    public AttendanceListAdapter(Context context, List<AttendanceDataDao> list) {
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
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Holder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.row_attendance, parent, false);
            viewHolder = new Holder();
            viewHolder.txtName = (TextView) convertView.findViewById(R.id.txtAttendanceName);
            viewHolder.txtNameWith = (TextView) convertView.findViewById(R.id.txtAttendanceNameWith);
            viewHolder.txtYes = (ImageView) convertView.findViewById(R.id.imgAttendanceYes);
            viewHolder.txtNo = (ImageView) convertView.findViewById(R.id.imgAttendanceNo);
            viewHolder.txtMayBe = (ImageView) convertView.findViewById(R.id.imgAttendanceMayBe);
            viewHolder.txtNumber = (TextView) convertView.findViewById(R.id.txtAttendanceMobile);
            viewHolder.txtNumberWith = (TextView) convertView.findViewById(R.id.txtAttendanceMobileWith);
            viewHolder.mLLAttandace = (LinearLayout) convertView.findViewById(R.id.llAttendance);
            viewHolder.mLLAttandaceWith = (LinearLayout) convertView.findViewById(R.id.llAttendanceWith);

            viewHolder.txtYesWith = (ImageView) convertView.findViewById(R.id.imgAttendanceYesWith);
            viewHolder.txtNoWith = (ImageView) convertView.findViewById(R.id.imgAttendanceNoWith);
            viewHolder.txtMayBeWith = (ImageView) convertView.findViewById(R.id.imgAttendanceMayBeWith);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (Holder) convertView.getTag();
        }


        try {
            //Check Couple or Not
            if (mList.get(position).getNumber().contains(AppConstant.SEPERATOR_STRING)) {

                String[] name = mList.get(position).getName().split(AppConstant.SEPERATOR_STRING);
                String[] number = mList.get(position).getNumber().split(AppConstant.SEPERATOR_STRING);
                String[] yes = mList.get(position).getAttendanceYes().split(AppConstant.SEPERATOR_STRING);
                String[] no = mList.get(position).getAttendanceNo().split(AppConstant.SEPERATOR_STRING);
                String[] mayBe = mList.get(position).getAttendanceMaybe().split(AppConstant.SEPERATOR_STRING);

                //Check is couple Pair of two or single
                if (number.length == 1) {
                    viewHolder.txtNumberWith.setVisibility(View.GONE);
                    viewHolder.txtNameWith.setVisibility(View.GONE);

                    viewHolder.mLLAttandaceWith.setVisibility(View.GONE);
                    viewHolder.mLLAttandace.setVisibility(View.VISIBLE);

                    String userName = name.length > 0 ? name[0] : "";
                    String userNumber = number.length > 0 ? number[0] : "";
                    String strYes = yes.length > 0 ? yes[0] : "";
                    String strNo = no.length > 0 ? no[0] : "";
                    String strMayBe = mayBe.length > 0 ? mayBe[0] : "";

                    userName = userName.replace(AppConstant.SEPERATOR_STRING, "");
                    userNumber = userNumber.replace(AppConstant.SEPERATOR_STRING, "");
                    strYes = strYes.replace(AppConstant.SEPERATOR_STRING, "");
                    strNo = strNo.replace(AppConstant.SEPERATOR_STRING, "");
                    strMayBe = strMayBe.replace(AppConstant.SEPERATOR_STRING, "");

                    viewHolder.txtName.setText(Utils.checkIfMe(mContext, userNumber, userName));
                    viewHolder.txtNumber.setText(userNumber);


                    if (strYes.equalsIgnoreCase(AppConstant.ATTENDANCE_CHECKED)) {
                        viewHolder.txtYes.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_context_green_check));
                    } else {
                        viewHolder.txtYes.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_context_green_uncheck));
                    }

                    if (strNo.equalsIgnoreCase(AppConstant.ATTENDANCE_CHECKED)) {
                        viewHolder.txtNo.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_context_red_check));
                    } else {
                        viewHolder.txtNo.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_context_red_uncheck));
                    }

                    if (strMayBe.equalsIgnoreCase(AppConstant.ATTENDANCE_CHECKED)) {
                        viewHolder.txtMayBe.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_context_orange_check));
                    } else {
                        viewHolder.txtMayBe.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_context_orange_uncheck));
                    }

                } else {

                    viewHolder.txtNumberWith.setVisibility(View.VISIBLE);
                    viewHolder.txtNameWith.setVisibility(View.VISIBLE);

                    viewHolder.mLLAttandaceWith.setVisibility(View.VISIBLE);
                    viewHolder.mLLAttandace.setVisibility(View.VISIBLE);

                    String userName = name.length > 0 ? name[0] : "";
                    String userNumber = number.length > 0 ? number[0] : "";
                    userName = userName.replace(AppConstant.SEPERATOR_STRING, "");
                    userNumber = userNumber.replace(AppConstant.SEPERATOR_STRING, "");

                    String userNameWith = null;
                    String userNumberWith = number.length > 0 ? number[1] : "";
                    userNumberWith = userNumberWith.replace(AppConstant.SEPERATOR_STRING, "");
                    try {
                        userNameWith = name.length > 0 ? name[1] : "";
                        userNameWith = userNameWith.replace(AppConstant.SEPERATOR_STRING, "");
                    } catch (Exception e) {
                        userNameWith = "";
                    }

                    String strYes = yes.length > 0 ? yes[0] : "";
                    String strNo = no.length > 0 ? no[0] : "";
                    String strMayBe = mayBe.length > 0 ? mayBe[0] : "";

                    String strYesWith = yes.length > 0 ? yes[1] : "";
                    String strNoWith = no.length > 0 ? no[1] : "";
                    String strMayBeWith = mayBe.length > 0 ? mayBe[1] : "";

                    strYes = strYes.replace(AppConstant.SEPERATOR_STRING, "");
                    strNo = strNo.replace(AppConstant.SEPERATOR_STRING, "");
                    strMayBe = strMayBe.replace(AppConstant.SEPERATOR_STRING, "");

                    strYesWith = strYesWith.replace(AppConstant.SEPERATOR_STRING, "");
                    strNoWith = strNoWith.replace(AppConstant.SEPERATOR_STRING, "");
                    strMayBeWith = strMayBeWith.replace(AppConstant.SEPERATOR_STRING, "");

                    viewHolder.txtName.setText(Utils.checkIfMe(mContext, userNumber, userName));
                    viewHolder.txtNumber.setText(userNumber);

                    viewHolder.txtNameWith.setText(Utils.checkIfMe(mContext, userNumberWith, userNameWith));
                    viewHolder.txtNumberWith.setText(userNumberWith);


                    if (strYes.equalsIgnoreCase(AppConstant.ATTENDANCE_CHECKED)) {
                        viewHolder.txtYes.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_context_green_check));
                    } else {
                        viewHolder.txtYes.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_context_green_uncheck));
                    }

                    if (strNo.equalsIgnoreCase(AppConstant.ATTENDANCE_CHECKED)) {
                        viewHolder.txtNo.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_context_red_check));
                    } else {
                        viewHolder.txtNo.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_context_red_uncheck));
                    }

                    if (strMayBe.equalsIgnoreCase(AppConstant.ATTENDANCE_CHECKED)) {
                        viewHolder.txtMayBe.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_context_orange_check));
                    } else {
                        viewHolder.txtMayBe.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_context_orange_uncheck));
                    }


                    if (strYesWith.equalsIgnoreCase(AppConstant.ATTENDANCE_CHECKED)) {
                        viewHolder.txtYesWith.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_context_green_check));
                    } else {
                        viewHolder.txtYesWith.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_context_green_uncheck));
                    }

                    if (strNoWith.equalsIgnoreCase(AppConstant.ATTENDANCE_CHECKED)) {
                        viewHolder.txtNoWith.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_context_red_check));
                    } else {
                        viewHolder.txtNoWith.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_context_red_uncheck));
                    }

                    if (strMayBeWith.equalsIgnoreCase(AppConstant.ATTENDANCE_CHECKED)) {
                        viewHolder.txtMayBeWith.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_context_orange_check));
                    } else {
                        viewHolder.txtMayBeWith.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_context_orange_uncheck));
                    }

                }


            } else {
                // no couple
                viewHolder.txtNumberWith.setVisibility(View.GONE);
                viewHolder.txtNameWith.setVisibility(View.GONE);
                viewHolder.mLLAttandaceWith.setVisibility(View.GONE);
                viewHolder.mLLAttandace.setVisibility(View.VISIBLE);

                viewHolder.txtName.setText(Utils.checkIfMe(mContext, mList.get(position).getNumber(), mList.get(position).getName()));
                viewHolder.txtNumber.setText(mList.get(position).getNumber());


                if (mList.get(position).getAttendanceYes().equalsIgnoreCase(AppConstant.ATTENDANCE_CHECKED)) {
                    viewHolder.txtYes.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_context_green_check));
                } else {
                    viewHolder.txtYes.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_context_green_uncheck));
                }

                if (mList.get(position).getAttendanceNo().equalsIgnoreCase(AppConstant.ATTENDANCE_CHECKED)) {
                    viewHolder.txtNo.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_context_red_check));
                } else {
                    viewHolder.txtNo.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_context_red_uncheck));
                }

                if (mList.get(position).getAttendanceMaybe().equalsIgnoreCase(AppConstant.ATTENDANCE_CHECKED)) {
                    viewHolder.txtMayBe.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_context_orange_check));
                } else {
                    viewHolder.txtMayBe.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_context_orange_uncheck));
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return convertView;
    }

    public class Holder {
        private TextView txtName, txtNameWith;
        private ImageView txtYes, txtNo, txtMayBe, txtYesWith, txtNoWith, txtMayBeWith;
        private TextView txtNumber, txtNumberWith;
        private LinearLayout mLLAttandace, mLLAttandaceWith;

    }
}