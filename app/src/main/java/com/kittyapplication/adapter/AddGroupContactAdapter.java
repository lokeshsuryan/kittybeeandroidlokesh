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
import com.kittyapplication.utils.ImageUtils;
import com.kittyapplication.utils.PreferanceUtils;
import com.kittyapplication.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pintu Riontech on 10/8/16.
 * vaghela.pintu31@gmail.com
 */
public class AddGroupContactAdapter extends BaseAdapter implements Filterable {
    private static final String TAG = AddGroupContactAdapter.class.getSimpleName();

    private List<ContactDao> mList;
    private List<ContactDao> mListClone;
    private Context mContext;
    private static LayoutInflater inflater = null;
    private ItemFilter filter;
    private List<ContactDao> mSelectedList;
    private int mType;

    // TYPE 0 FOR ADD GROUP MEMBER
    // TYPE 1 FOR SELECT HOST
    // TYPE 3 FOR SELECT HOST WITH COUPLE

    public AddGroupContactAdapter(Context context, List<ContactDao> lists, int type) {
        mList = lists;
        mListClone = lists;
        mContext = context;
        inflater = (LayoutInflater) this.mContext.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mSelectedList = new ArrayList<>();
        filter = new ItemFilter();
        mType = type;
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
        private RoundedImageView mImgUser, mImgUserWith;
        private CustomTextViewBold mTxtUserName, mTxtUserNameWith;
        private CustomTextViewNormal mTxtNumber, mTxtNumberWith;
        private CheckBox mCbIsAddToGroup = null;
        private ImageView imgStatus;
        private RelativeLayout mRlMain;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            if (mType != 3) {
                convertView = inflater.inflate(R.layout.row_create_group, parent, false);
            } else {
                convertView = inflater.inflate(R.layout.row_layout_couple_host, parent, false);
            }
            viewHolder = new Holder();
            viewHolder.mImgUser = (RoundedImageView) convertView.findViewById(R.id.imgGroupMember);
            viewHolder.mTxtUserName = (CustomTextViewBold) convertView.findViewById(R.id.txtGroupMemberName);
            viewHolder.mTxtNumber = (CustomTextViewNormal) convertView.findViewById(R.id.txtGroupMemberNumber);
            viewHolder.mCbIsAddToGroup = (CheckBox) convertView.findViewById(R.id.cbAddGroup);
            viewHolder.mRlMain = (RelativeLayout) convertView.findViewById(R.id.rlCreateGroup);
            viewHolder.imgStatus = (ImageView) convertView.findViewById(R.id.imgStatus);
            if (mType == 3) {
                viewHolder.mImgUserWith = (RoundedImageView) convertView.findViewById(R.id.imgGroupMemberTwo);
                viewHolder.mTxtUserNameWith = (CustomTextViewBold) convertView.findViewById(R.id.txtGroupMemberNameTwo);
                viewHolder.mTxtNumberWith = (CustomTextViewNormal) convertView.findViewById(R.id.txtGroupMemberNumberTwo);
            }
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (Holder) convertView.getTag();
        }

        if (mType != 3) {

            ImageUtils.getImageLoader(mContext).displayImage(mList.get(position).getImage(),
                    viewHolder.mImgUser);

            viewHolder.mTxtUserName.setText(Utils.checkNumberIsLoginUser(mContext
                    , mList.get(position).getPhone(), mList.get(position).getName()));

            //Add for app user
            //if user is register display icon
            //type == 0 means coming from add group add contacts
            if (mType == 0) {
                if (mList.get(position).getRegistration().equalsIgnoreCase("1")) {
                    viewHolder.imgStatus.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.imgStatus.setVisibility(View.GONE);
                }
            }

            if (Utils.isValidString(mList.get(position).getPhone())) {
                viewHolder.mTxtNumber.setText(mList.get(position).getPhone());
            } else {
                viewHolder.mTxtNumber.setText("");
            }

            // R.layout.row_layout_couple_host
            convertView.setTag(R.id.cbAddGroup, viewHolder.mCbIsAddToGroup.isChecked());
            convertView.setTag(R.id.txtGroupMemberName, position);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isChecked = (boolean) v.getTag(R.id.cbAddGroup);
                    int pos = (int) v.getTag(R.id.txtGroupMemberName);
                    if (isChecked) {
                        if (mType == 0) {
                            addToSeletedList(mList.get(pos), false);
                        } else {
                            isHostAleready(pos, false);
                            addToSeletedList(mList.get(pos), false);
                        }
                    } else {
                        if (mType == 0) {
                            addToSeletedList(mList.get(pos), true);
                        } else {
                            isHostAleready(pos, true);
                            addToSeletedList(mList.get(pos), true);
                        }
                    }
                }
            });

            viewHolder.mCbIsAddToGroup.setTag(position);
            viewHolder.mCbIsAddToGroup.setOnCheckedChangeListener(null);
            viewHolder.mCbIsAddToGroup.setChecked(checkIsSelected(mList.get(position)));
            viewHolder.mCbIsAddToGroup.setOnCheckedChangeListener(
                    new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            int pos = (int) buttonView.getTag();
                            if (isChecked) {
                                if (mType == 0) {
                                    addToSeletedList(mList.get(pos), true);
                                } else {
                                    isHostAleready(pos, true);
                                    addToSeletedList(mList.get(pos), true);
                                }
                            } else {
                                if (mType == 0) {
                                    addToSeletedList(mList.get(pos), false);
                                } else {
                                    isHostAleready(pos, false);
                                    addToSeletedList(mList.get(pos), false);
                                }
                            }
                        }
                    });

        } else {
            //For Select Host
            //For Check it is Couple Or Not
            if (mList.get(position).getPhone().contains("-!-")) {

                viewHolder.mTxtNumberWith.setVisibility(View.VISIBLE);
                viewHolder.mTxtUserNameWith.setVisibility(View.VISIBLE);
                viewHolder.mImgUserWith.setVisibility(View.VISIBLE);


                String[] name = mList.get(position).getName().split("-!-");
                String[] images = mList.get(position).getImage().split("-!-");
                String[] number = mList.get(position).getPhone().split("-!-");

                viewHolder.mTxtNumber.setText(number.length > 0 ? number[0] : "");
                viewHolder.mTxtNumberWith.setText(number.length > 0 ? number[1] : "");

//                viewHolder.mTxtUserName.setText(couple.length > 0 ? couple[0] : "");
//                viewHolder.mTxtUserNameWith.setText(couple.length > 0 ? couple[1] : "");


                viewHolder.mTxtUserName.setText(Utils.checkNumberIsLoginUser(mContext,
                        (number.length > 0 ? number[0] : ""),
                        (name.length > 0 ? name[0] : "")));

                viewHolder.mTxtUserNameWith.setText(Utils.checkNumberIsLoginUser(mContext,
                        (number.length > 0 ? number[1] : ""),
                        (name.length > 0 ? name[1] : "")));


                ImageUtils.getImageLoader(mContext).displayImage(images.length > 0 ? images[0] : "",
                        viewHolder.mImgUser);

                ImageUtils.getImageLoader(mContext).displayImage(images.length > 0 ? images[1] : "",
                        viewHolder.mImgUserWith);
            } else {
                //IF data is not couple
//                String name = Utils.checkIfMe(mContext, mList.get(position).getPhone(),
//                        mList.get(position).getName());

                String name = Utils.checkNumberIsLoginUser(mContext,
                        mList.get(position).getPhone(),
                        mList.get(position).getName());

                viewHolder.mTxtUserName.setText(name);

                if (Utils.isValidString(mList.get(position).getPhone())) {
                    viewHolder.mTxtNumber.setText(mList.get(position).getPhone());
                } else {
                    viewHolder.mTxtNumber.setText(mList.get(position).getPhone());
                }
                viewHolder.mTxtNumberWith.setVisibility(View.GONE);
                viewHolder.mTxtUserNameWith.setVisibility(View.GONE);
                viewHolder.mImgUserWith.setVisibility(View.GONE);

                ImageUtils.getImageLoader(mContext).displayImage(mList.get(position).getImage(),
                        viewHolder.mImgUser);
            }

            viewHolder.mCbIsAddToGroup.setTag(position);
            viewHolder.mCbIsAddToGroup.setChecked(checkIsSelected(mList.get(position)));
            viewHolder.mCbIsAddToGroup.setOnCheckedChangeListener(null);
            viewHolder.mCbIsAddToGroup.setOnCheckedChangeListener(
                    new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            int pos = (int) buttonView.getTag();
                            if (isChecked) {
                                if (mType == 0) {
                                    addToSeletedList(mList.get(pos), true);
                                } else {
                                    isHostAleready(pos, true);
                                    addToSeletedList(mList.get(pos), true);
                                }
                            } else {
                                if (mType == 0) {
                                    addToSeletedList(mList.get(pos), false);
                                } else {
                                    isHostAleready(pos, false);
                                    addToSeletedList(mList.get(pos), false);
                                }
                            }
                        }
                    });


            convertView.setTag(R.id.cbAddGroup, viewHolder.mCbIsAddToGroup.isChecked());
            convertView.setTag(R.id.txtGroupMemberName, position);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isChecked = (boolean) v.getTag(R.id.cbAddGroup);
                    int pos = (int) v.getTag(R.id.txtGroupMemberName);
                    if (isChecked) {
                        if (mType == 0) {
                            addToSeletedList(mList.get(pos), false);
                        } else {
                            isHostAleready(pos, false);
                            addToSeletedList(mList.get(pos), false);
                        }
                    } else {
                        if (mType == 0) {
                            addToSeletedList(mList.get(pos), true);
                        } else {
                            isHostAleready(pos, true);
                            addToSeletedList(mList.get(pos), true);
                        }
                    }
                }
            });

        }
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
                if (!checkDataIntoList(object)) {
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

    private void isHostAleready(int pos, boolean isAdd) {
        if (isAdd) {
            mList.get(pos).setIsHost("1");
        } else {
            mList.get(pos).setIsHost("0");
        }
    }

    public List<ContactDao> getListData() {
        return mList;
    }

    public boolean isAllHostSelect() {
        try {
            if (mList.size() == mSelectedList.size()) {
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public void reloadData() {
        try {
            mList = mListClone;
            notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checkDataIntoList(ContactDao obj) {
        try {
            if (mSelectedList != null && mSelectedList.size() > 0) {
                for (int i = 0; i < mSelectedList.size(); i++) {
                    if (mSelectedList.get(i).getPhone().equalsIgnoreCase(obj.getPhone())) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String checkIfMe(Context context, String number, String name) {
        try {
            if (number.equalsIgnoreCase(PreferanceUtils.getLoginUserObject(context).getPhone())) {
                return "You";
            } else {
                return name;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return name;
    }
}