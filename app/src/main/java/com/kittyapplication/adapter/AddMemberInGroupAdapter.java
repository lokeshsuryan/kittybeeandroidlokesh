package com.kittyapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.kittyapplication.R;
import com.kittyapplication.custom.CustomTextViewBold;
import com.kittyapplication.custom.CustomTextViewNormal;
import com.kittyapplication.custom.RoundedImageView;
import com.kittyapplication.model.ContactDao;
import com.kittyapplication.ui.activity.AddGroupActivity;
import com.kittyapplication.utils.ImageUtils;
import com.kittyapplication.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HalfBloodPrince(RIONTECH)
 * Riontech on 3/10/16.
 */
public class AddMemberInGroupAdapter extends BaseAdapter implements Filterable {
    private static final String TAG = AddMemberInGroupAdapter.class.getSimpleName();

    private List<ContactDao> mList;
    private List<ContactDao> mListClone;
    private Context mContext;
    private static LayoutInflater inflater = null;
    private ItemFilter filter;
    private List<ContactDao> mSelectedList;

    public AddMemberInGroupAdapter(Context context, List<ContactDao> lists) {
        mList = lists;
        mListClone = lists;
        mContext = context;
        inflater = (LayoutInflater) this.mContext.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mSelectedList = new ArrayList<>();
        filter = new ItemFilter();
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
    public Filter getFilter() {
        return filter;
    }

    public class Holder {
        private RoundedImageView mImgUser;
        private CustomTextViewBold mTxtUserName;
        private CustomTextViewNormal mTxtNumber;
        private CheckBox mCbIsAddToGroup = null;
        private ImageView imgStatus;
        private RelativeLayout mRlMain;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.row_create_group, parent, false);
            viewHolder = new Holder();
            viewHolder.mImgUser = (RoundedImageView) convertView.findViewById(R.id.imgGroupMember);
            viewHolder.mTxtUserName = (CustomTextViewBold) convertView.findViewById(R.id.txtGroupMemberName);
            viewHolder.mTxtNumber = (CustomTextViewNormal) convertView.findViewById(R.id.txtGroupMemberNumber);
            viewHolder.mCbIsAddToGroup = (CheckBox) convertView.findViewById(R.id.cbAddGroup);
            viewHolder.imgStatus = (ImageView) convertView.findViewById(R.id.imgStatus);
            viewHolder.mRlMain = (RelativeLayout) convertView.findViewById(R.id.rlCreateGroup);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (Holder) convertView.getTag();
        }


        ImageUtils.getImageLoader(mContext).displayImage(mList.get(position).getImage(),
                viewHolder.mImgUser);

        viewHolder.mTxtUserName.setText(Utils.checkNumberIsLoginUser(mContext,
                mList.get(position).getPhone(),
                mList.get(position).getName()));

        if (Utils.isValidString(mList.get(position).getPhone())) {
            viewHolder.mTxtNumber.setText(mList.get(position).getPhone());
        } else {
            viewHolder.mTxtNumber.setText("");
        }

        if (mList.get(position).isAlready()) {
            viewHolder.mCbIsAddToGroup.setVisibility(View.GONE);
        } else {
            viewHolder.mCbIsAddToGroup.setVisibility(View.VISIBLE);
        }

        //user is register in app
        //display app user icon
        if (mList.get(position).getRegistration().equalsIgnoreCase("1")) {
            viewHolder.imgStatus.setVisibility(View.VISIBLE);
        } else {
            viewHolder.imgStatus.setVisibility(View.GONE);
        }


        viewHolder.mCbIsAddToGroup.setTag(position);
        viewHolder.mCbIsAddToGroup.setOnCheckedChangeListener(null);
        viewHolder.mCbIsAddToGroup.setChecked(checkIsSelected(mList.get(position)));
        viewHolder.mCbIsAddToGroup.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                                                 boolean isChecked) {
                        int pos = (int) buttonView.getTag();
                        if (isChecked) {
                            addToSeletedList(mList.get(pos), true);
                        } else {
                            addToSeletedList(mList.get(pos), false);
                        }
                    }
                });

        convertView.setTag(R.id.cbAddGroup, viewHolder.mCbIsAddToGroup.isChecked());
        convertView.setTag(R.id.txtGroupMemberName, position);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = (int) v.getTag(R.id.txtGroupMemberName);
                boolean isChecked = (boolean) v.getTag(R.id.cbAddGroup);
                if (isChecked) {
                    addToSeletedList(mList.get(pos), false);
                } else {
                    addToSeletedList(mList.get(pos), true);
                }
            }
        });

        return convertView;
    }


    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String filterString = constraint.toString().toLowerCase();
            FilterResults results = new FilterResults();
            List<ContactDao> list = mListClone;
            int count = list.size();
            final ArrayList<ContactDao> newList = new ArrayList<>(count);

            String filterableString;

            for (int i = 0; i < count; i++) {
                filterableString = list.get(i).getName();
                if (filterableString.toLowerCase().contains(filterString)) {
                    newList.add(list.get(i));
                }
            }

            results.values = newList;
            results.count = newList.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override

        protected void publishResults(CharSequence constraint, FilterResults results) {
            mList = (List<ContactDao>) results.values;
            notifyDataSetChanged();
        }
    }

    private void addToSeletedList(ContactDao object, boolean isAdd) {
        if (isAdd) {
            if (mSelectedList.isEmpty()) {
                mSelectedList.add(object);
            } else {
                if (!checkIsSelected(object)) {
                    //if (!mSelectedList.contains(object)) {
                    mSelectedList.add(object);
                    // }
                }
            }
        } else {
            if (mSelectedList.size() > 0)
                mSelectedList.remove(object);
        }
        notifyDataSetChanged();
        getUpdatedList();
    }

    public List<ContactDao> getSelectedList() {
        return mSelectedList;
    }

    public boolean checkIsSelected(ContactDao obj) {
        if (mSelectedList.size() > 0) {
            for (int i = 0; i < mSelectedList.size(); i++) {
                if (mSelectedList.get(i).getPhone().equalsIgnoreCase(obj.getPhone())) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<ContactDao> getListData() {
        return mList;
    }

    public void reloadData() {
        mList = mListClone;
        notifyDataSetChanged();
    }

    public void getUpdatedList() {
        ((AddGroupActivity) mContext).getMemberImageList(mSelectedList);
    }
}