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
import com.kittyapplication.model.ContactDaoWithHeader;
import com.kittyapplication.utils.ImageUtils;
import com.kittyapplication.utils.Utils;

import java.util.List;

/**
 * Created by Dhaval Soneji Riontech on 1/9/16.
 */
public class SelectCoupleWithListAdapter extends BaseAdapter {
    private static final String TAG = SelectWithAdapter.class.getSimpleName();

    private List<ContactDaoWithHeader> mList;
    private List<ContactDaoWithHeader> mListClone;
    private Context mContext;
    private static LayoutInflater inflater = null;

    public SelectCoupleWithListAdapter(Context context, List<ContactDaoWithHeader> lists) {
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
        viewHolder.txtContactName.setText(Utils.checkIfMe(mContext,
                mList.get(position).getData().getPhone(),
                mList.get(position).getData().getName()));
        viewHolder.txtContactInvite.setVisibility(View.GONE);
        viewHolder.imgContactPhone.setVisibility(View.GONE);
        ImageUtils.getImageLoader(mContext).displayImage(mList.get(position).getData().getImage(), viewHolder.imgContactUser);

        return convertView;
    }
}
