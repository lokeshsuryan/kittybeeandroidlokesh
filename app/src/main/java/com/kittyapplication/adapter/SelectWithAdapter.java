package com.kittyapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.kittyapplication.R;
import com.kittyapplication.custom.CustomTextViewBold;
import com.kittyapplication.custom.CustomTextViewNormal;
import com.kittyapplication.custom.RoundedImageView;
import com.kittyapplication.model.ContactDao;
import com.kittyapplication.utils.ImageUtils;

import java.util.List;

/**
 * Created by Pintu Riontech on 15/8/16.
 * vaghela.pintu31@gmail.com
 */
public class SelectWithAdapter extends BaseAdapter {
    private static final String TAG = SelectWithAdapter.class.getSimpleName();

    private List<ContactDao> mList;
    private List<ContactDao> mListClone;
    private Context mContext;
    private static LayoutInflater inflater = null;

    public SelectWithAdapter(Context context, List<ContactDao> lists) {
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
        private CustomTextViewNormal txtContactTitle;
        private CustomTextViewBold txtContactName;
        private CustomTextViewNormal txtContactInvite;
        private ImageView imgContactPhone;
        private RoundedImageView imgContactUser;
        private RelativeLayout rlMain;
        private RelativeLayout rlButtons;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.row_layout_contact, parent, false);
            viewHolder = new Holder();
            viewHolder.imgContactUser = (RoundedImageView) convertView.findViewById(R.id.imgContactUser);
            viewHolder.txtContactName = (CustomTextViewBold) convertView.findViewById(R.id.txtContactName);
            viewHolder.txtContactTitle = (CustomTextViewNormal) convertView.findViewById(R.id.txtContactTitle);
            viewHolder.rlMain = (RelativeLayout) convertView.findViewById(R.id.rlContact);
            viewHolder.rlButtons = (RelativeLayout) convertView.findViewById(R.id.rlContactButtons);
            viewHolder.imgContactPhone = (ImageView) viewHolder.rlButtons.findViewById(R.id.imgContactPhone);
            viewHolder.txtContactInvite = (CustomTextViewNormal) viewHolder.rlButtons.findViewById(R.id.txtContactInvite);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (Holder) convertView.getTag();
        }
        //if (Utils.isValidString(mList.get(position).getName())) {
            viewHolder.txtContactName.setText(mList.get(position).getName());
//        } else {
//            viewHolder.txtContactName.setText("");
//        }
        viewHolder.txtContactInvite.setVisibility(View.GONE);
        viewHolder.imgContactPhone.setVisibility(View.GONE);
        ImageUtils.getImageLoader(mContext).displayImage(mList.get(position).getImage(), viewHolder.imgContactUser);

        return convertView;
    }
}