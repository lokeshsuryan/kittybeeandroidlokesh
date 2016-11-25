package com.kittyapplication.adapter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.kittyapplication.R;
import com.kittyapplication.custom.CustomTextViewBold;
import com.kittyapplication.custom.CustomTextViewNormal;
import com.kittyapplication.custom.ImageLoaderListener;
import com.kittyapplication.model.PromotionalDao;
import com.kittyapplication.ui.activity.PromotionalDetailsActivity;
import com.kittyapplication.utils.ImageUtils;

import java.util.List;

/**
 * Created by Pintu Riontech on 8/8/16.
 * vaghela.pintu31@gmail.com
 */
public class PromotionalAdapter extends BaseAdapter {

    private Activity mActivity;
    private LayoutInflater mInflater;
    private List<PromotionalDao> mList;
    private String name;

    public PromotionalAdapter(Activity mActivity,
                              List<PromotionalDao> lists) {
        this.mActivity = mActivity;
        this.mList = lists;
        this.name = name;
        mInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public String getItem(int position) {
        try {
            return mList.get(position).getAddress();
        } catch (Exception e) {
            return "Sample";
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.row_item_promotional, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.txtName = (CustomTextViewBold) convertView.findViewById(R.id.txtNamePromotional);
            viewHolder.imgPromotional = (ImageView) convertView.findViewById(R.id.imgPromotional);
            viewHolder.txtAddress = (CustomTextViewNormal) convertView.findViewById(R.id.txtPromotionalAddress);
            viewHolder.pbLoaderImage = (ProgressBar) convertView.findViewById(R.id.pbLoaderPromotionalImage);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        try {
            convertView.setTag(position);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = (int) v.getTag();
                    Intent intent = new Intent(mActivity, PromotionalDetailsActivity.class);
                    intent.putExtra("object", new Gson().toJson(mList.get(pos)).toString());
                    mActivity.startActivity(intent);
                }
            });
            viewHolder.txtName.setText(mList.get(position).getName());
            viewHolder.txtAddress.setText(mList.get(position).getAddress());
            ImageUtils.getImageLoader(mActivity).displayImage(mList.get(position).getThumb(),
                    viewHolder.imgPromotional, new ImageLoaderListener(viewHolder.pbLoaderImage));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return convertView;
    }


    class ViewHolder {
        CustomTextViewBold txtName;
        CustomTextViewNormal txtAddress;
        ImageView imgPromotional;
        ProgressBar pbLoaderImage;
    }
}
