package com.kittyapplication.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.kittyapplication.R;
import com.kittyapplication.core.utils.SpannableUtils;
import com.kittyapplication.custom.CustomTextViewNormal;
import com.kittyapplication.model.HedsUpData;
import com.kittyapplication.ui.activity.HeadsUpDescriptionActivity;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.Utils;

import java.util.List;

/**
 * Created by Pintu Riontech on 8/9/16.
 * vaghela.pintu31@gmail.com
 */
public class HeadsUpAdapter extends BaseAdapter {
    private static final String TAG = HeadsUpAdapter.class.getSimpleName();

    private List<HedsUpData> mList;
    private List<HedsUpData> mListClone;
    private Context mContext;
    private static LayoutInflater inflater = null;

    public HeadsUpAdapter(Context context, List<HedsUpData> lists) {
        mList = lists;
        mListClone = lists;
        mContext = context;
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


    public class Holder {
        //        private CustomTextViewBold txtDescription;
        private CustomTextViewNormal txtTitle, txtAddress, txtTime, txtDate;
        private ImageView imgHeadsup;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.row_layout_heads_up, parent, false);
            viewHolder = new Holder();
            viewHolder.txtTitle = (CustomTextViewNormal)
                    convertView.findViewById(R.id.txtHeadsUpTitle);
            viewHolder.txtAddress = (CustomTextViewNormal)
                    convertView.findViewById(R.id.txtHeadsUpAddress);
            viewHolder.txtTime = (CustomTextViewNormal)
                    convertView.findViewById(R.id.txtHeadsUpTime);
            viewHolder.txtDate = (CustomTextViewNormal)
                    convertView.findViewById(R.id.txtHeadsUpDate);
            viewHolder.imgHeadsup = (ImageView)
                    convertView.findViewById(R.id.imgHeadsUp);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (Holder) convertView.getTag();
        }
        viewHolder.txtTitle.setText(mList.get(position).getTitle());


        viewHolder.txtAddress.setText(getAddressString(mList.get(position)));


        String[] spannableTimeString = {mContext.getResources().getString(R.string.time_str)};
        String[] spannableDateString = {mContext.getResources().getString(R.string.date_str)};
        String time = mContext.getResources().getString(R.string.hedsup_time_str, mList.get(position).getFromTime(), mList.get(position).getToTime());
        String date = mContext.getResources().getString(R.string.hedsup_date_str, mList.get(position).getFromDate(), mList.get(position).getToDate());

        SpannableUtils spannableUtils = new SpannableUtils(viewHolder.txtTime);
        spannableUtils.setString(time);
        spannableUtils.setIsBold(true);
        spannableUtils.setIsColored(true);
        spannableUtils.setTextColor(ContextCompat.getColor(mContext, R.color.black));
        spannableUtils.setSpannableArray(spannableTimeString);
        spannableUtils.buildSpannable();

        SpannableUtils spannableUtilsDate = new SpannableUtils(viewHolder.txtDate);
        spannableUtilsDate.setString(date);
        spannableUtilsDate.setIsBold(true);
        spannableUtilsDate.setIsColored(true);
        spannableUtilsDate.setTextColor(ContextCompat.getColor(mContext, R.color.black));
        spannableUtilsDate.setSpannableArray(spannableDateString);
        spannableUtilsDate.buildSpannable();

        convertView.setTag(R.id.txtHeadsUpTitle, position);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = (int) v.getTag(R.id.txtHeadsUpTitle);
                Intent intent = new Intent(mContext, HeadsUpDescriptionActivity.class);
                intent.putExtra(AppConstant.EXTRA_HEADS_UP_DESCRIPTION,
                        new Gson().toJson(mList.get(pos)).toString());
                mContext.startActivity(intent);
            }
        });


        /*   //Visibility Gone from XML

        if (Utils.isValidString(mList.get(position).getDecription())) {
            viewHolder.txtDescription.setText(mList.get(position).getDecription());
        } else {
            viewHolder.txtDescription.setVisibility(View.GONE);
        }

        //Visibility Gone from XML

        if (Utils.isValidString(mList.get(position).getImage())) {
            ImageUtils.getImageLoader(mContext).displayImage(mList.get(position).getImage(),
                    viewHolder.imgHeadsup, ImageUtils.getDisplayOptionProfileRoundedCorner());
        } else {
            viewHolder.imgHeadsup.setVisibility(View.GONE);
        }*/
        return convertView;
    }


    private String getAddressString(HedsUpData data) {
        String address = "";
        try {
            if (Utils.isValidString(data.getAddressOne())) {
                address = address + data.getAddressOne() + ", ";
            }
            if (Utils.isValidString(data.getAddressTwo())) {
                address = address + data.getAddressTwo() + ", ";
            }
            if (Utils.isValidString(data.getState())) {
                address = address + data.getState() + ", ";
            }
            if (Utils.isValidString(data.getCountry())) {
                address = address + data.getCountry();
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
        return address;
    }
}