package com.kittyapplication.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.kittyapplication.R;

/**
 * Created by Pintu Riontech on 7/8/16.
 * vaghela.pintu31@gmail.com
 */
public class DrawerAdapter extends BaseAdapter {

    private Context mContext;
    private String[] mTitle;
    private int[] mIcons = {R.drawable.ic_drawer_home, R.drawable.ic_drawer_my_profile, R.drawable.ic_drawer_notification
            , R.drawable.ic_action_group, R.drawable.ic_drawer_my_kitty, R.drawable.ic_drawer_notes,
            R.drawable.ic_drawer_contact, R.drawable.ic_about_us};

    public DrawerAdapter(Context mContext) {
        this.mContext = mContext;
        mTitle = mContext.getResources().getStringArray(R.array.drawer_title);
//        mIcons = mContext.getResources().getIntArray(R.array.drawer_icon);
    }

    @Override
    public int getCount() {
        return mTitle.length;
    }

    @Override
    public Object getItem(int i) {
        return mTitle[i];
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {

        final ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.row_layout_drawer, parent,
                    false);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.txtDrawerTitle = (TextView) convertView.findViewById(R.id.txtDrawerItem);
        viewHolder.txtDrawerTitle.setText(mTitle[pos]);
        Drawable icon = ContextCompat.getDrawable(mContext, mIcons[pos]);
        //viewHolder.txtDrawerTitle.setCompoundDrawables(icon, null, null, null);
        viewHolder.txtDrawerTitle.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);

        return convertView;

    }

    class ViewHolder {
        private TextView txtDrawerTitle;
    }

}