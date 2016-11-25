package com.kittyapplication.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.kittyapplication.R;
import com.kittyapplication.model.MembersDaoCalendar;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.Utils;

import java.util.List;

/**
 * Created by Dhaval Riontech on 13/8/16.
 */
public class DiaryListAdapter extends BaseAdapter {

    private static final String TAG = DiaryListAdapter.class.getSimpleName();
    private final int mNoOfPunchuality;
    private String mHasPunctuality = "0";
    private boolean mHasUserId = false;
    private Context mContext;
    private List<MembersDaoCalendar> mList;
    private static LayoutInflater mInflater = null;
    private String mKittyDate;

    public DiaryListAdapter(Context context, List<MembersDaoCalendar> list,
                            String hasPunctuality, boolean hasUserId, int noOfPunchuality,
                            String kittyDate) {
        mContext = context;
        mList = list;
        mHasPunctuality = hasPunctuality;
        mHasUserId = hasUserId;
        mNoOfPunchuality = noOfPunchuality;
        mKittyDate = kittyDate;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public MembersDaoCalendar getItem(int position) {
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
            mInflater = (LayoutInflater) this.mContext.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.row_dairy, parent, false);
            viewHolder = new Holder();
            viewHolder.txtName = (TextView) convertView.findViewById(R.id.dairyPersonName);
            viewHolder.cbPaid = (CheckBox) convertView.findViewById(R.id.cbPaid);
            viewHolder.cbPresent = (CheckBox) convertView.findViewById(R.id.cbPresent);
            viewHolder.cbPunctuality = (CheckBox) convertView.findViewById(R.id.cbPunctuality);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (Holder) convertView.getTag();
        }

        // for couples
        if (mList.get(position).isCouple()) {
            if (!mList.get(position).isUserDeleted()) {

                viewHolder.txtName.setText(mList.get(position).getUsersName());

                //ispunctualcouple()
                if (mList.get(position).isPunctualCouple())
                    viewHolder.txtName.setTextColor(ContextCompat
                            .getColor(mContext, R.color.dark_sky));
                else
                    viewHolder.txtName.setTextColor(ContextCompat
                            .getColor(mContext, R.color.registrationtextcolor));


                viewHolder.cbPaid.setEnabled(true);
                viewHolder.cbPresent.setEnabled(true);
                viewHolder.cbPunctuality.setEnabled(true);

                viewHolder.cbPaid.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            mList.get(position).setPaid(true);
                            mList.get(position).setPaid(AppConstant.ATTENDANCE_CHECKED);
                        } else {
                            mList.get(position).setPaid(false);
                            mList.get(position).setPaid(AppConstant.ATTENDANCE_UNCHECKED);
                        }
                    }
                });
                viewHolder.cbPresent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            mList.get(position).setPresent(true);
                            mList.get(position).setPresent(AppConstant.ATTENDANCE_CHECKED);
                        } else {
                            mList.get(position).setPresent(false);
                            mList.get(position).setPresent(AppConstant.ATTENDANCE_UNCHECKED);
                        }
                    }
                });
                viewHolder.cbPunctuality.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            mList.get(position).setPunctuality(true);
                            mList.get(position).setPunctuality(AppConstant.ATTENDANCE_CHECKED);
                            mList.get(position).setPresent(AppConstant.ATTENDANCE_CHECKED);

                            viewHolder.cbPresent.setChecked(true);
                        } else {
                            mList.get(position).setPunctuality(false);
                            mList.get(position).setPunctuality(AppConstant.ATTENDANCE_UNCHECKED);
                            mList.get(position).setPresent(AppConstant.ATTENDANCE_UNCHECKED);
                        }
                    }
                });

                // paid------------start-------------------
                //isPaid()
                if (mList.get(position).isPaid()) {
//                if (mList.get(position).getPaid().equalsIgnoreCase(AppConstant.ATTENDANCE_CHECKED)) {
                    viewHolder.cbPaid.setChecked(true);
                } else {
                    viewHolder.cbPaid.setChecked(false);
                }
                // paid------------end-------------------


                // present------------start-------------------
                if (mList.get(position).isAfterHostedByDate()) {
//                if (new Date().after(hostedByDate)) {
                    if (mList.get(position).isPresent()) {
//                    if (mList.get(position).getPresent().equalsIgnoreCase(AppConstant.ATTENDANCE_CHECKED)) {
                        viewHolder.cbPresent.setChecked(true);
                    } else {
                        viewHolder.cbPresent.setChecked(false);
                    }
                } else {
                    viewHolder.cbPresent.setEnabled(false);
                }
                // present------------end-------------------


                // punctuality------------start-------------------
                if (mList.get(position).isAvailablePunctuality()) {
                    if (mList.get(position).isHost())
                        viewHolder.cbPunctuality.setEnabled(false);

                    if (mList.get(position).isPunctuality()) {
//                    if (mList.get(position).getPunctuality().equalsIgnoreCase(AppConstant.ATTENDANCE_CHECKED)) {
                        viewHolder.cbPunctuality.setChecked(true);
                    } else {
                        viewHolder.cbPunctuality.setChecked(false);
                    }
                } else {
                    viewHolder.cbPunctuality.setEnabled(false);
                    if (mList.get(position).isPunctuality()) {
//                    if (mList.get(position).getPunctuality().equalsIgnoreCase(AppConstant.ATTENDANCE_CHECKED)) {
                        viewHolder.cbPunctuality.setChecked(true);
                    } else {
                        viewHolder.cbPunctuality.setChecked(false);
                    }
                }
                // punctuality------------end-------------------


                if (!mHasUserId) {
                    viewHolder.cbPaid.setEnabled(false);
                    viewHolder.cbPresent.setEnabled(false);
                } else {
                    viewHolder.cbPaid.setEnabled(true);
                    viewHolder.cbPresent.setEnabled(true);
                }
            } else {
                viewHolder.txtName.setText(mList.get(position).getUsersName());
                viewHolder.cbPaid.setEnabled(false);
                viewHolder.cbPresent.setEnabled(false);
                viewHolder.cbPunctuality.setEnabled(false);
            }
        } else {
            //check if user not deleted
            //replace method is for user that has value like '9966996699-!-'
            if (!mList.get(position).isUserDeleted()) {
                viewHolder.txtName.setText(mList.get(position).getUsersName());
                viewHolder.cbPaid.setEnabled(true);
                viewHolder.cbPresent.setEnabled(true);
                viewHolder.cbPunctuality.setEnabled(true);

                viewHolder.cbPaid.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            mList.get(position).setPaid(true);
                            mList.get(position).setPaid(AppConstant.ATTENDANCE_CHECKED);
                        } else {
                            mList.get(position).setPaid(false);
                            mList.get(position).setPaid(AppConstant.ATTENDANCE_UNCHECKED);
                        }
                    }
                });

                viewHolder.cbPresent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            mList.get(position).setPresent(true);
                            mList.get(position).setPresent(AppConstant.ATTENDANCE_CHECKED);
                        } else {
                            mList.get(position).setPresent(false);
                            mList.get(position).setPresent(AppConstant.ATTENDANCE_UNCHECKED);
                        }
                    }
                });

                viewHolder.cbPunctuality.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            mList.get(position).setPunctuality(true);
                            mList.get(position).setPunctuality(AppConstant.ATTENDANCE_CHECKED);

                            mList.get(position).setPresent(AppConstant.ATTENDANCE_CHECKED);
                            viewHolder.cbPresent.setChecked(true);
                        } else {
                            mList.get(position).setPunctuality(false);
                            mList.get(position).setPunctuality(AppConstant.ATTENDANCE_UNCHECKED);
                            mList.get(position).setPresent(AppConstant.ATTENDANCE_UNCHECKED);
                        }
                    }
                });

                if (!mHasUserId) {
                    viewHolder.cbPaid.setEnabled(false);
                    viewHolder.cbPresent.setEnabled(false);
                } else {
                    viewHolder.cbPaid.setEnabled(true);
                    viewHolder.cbPresent.setEnabled(true);
                }

                // paid------------start-------------------
                if (mList.get(position).isPaid())
                    viewHolder.cbPaid.setChecked(true);
                else
                    viewHolder.cbPaid.setChecked(false);
                // paid------------end-------------------

                //present------------start-------------------
                if (mList.get(position).isAfterHostedByDate()) {
                    if (mList.get(position).isPresent()) {
                        viewHolder.cbPresent.setChecked(true);
                    } else {
                        viewHolder.cbPresent.setChecked(false);
                    }
                } else {
                    viewHolder.cbPresent.setEnabled(false);
                }
                // present------------end-------------------

                //punctuality------------start-------------------
                if (mList.get(position).isAvailablePunctuality()) {
                    if (mList.get(position).isHost())
                        viewHolder.cbPunctuality.setEnabled(false);


                    if (mList.get(position).isPunctuality())
                        viewHolder.cbPunctuality.setChecked(true);
                    else
                        viewHolder.cbPunctuality.setChecked(false);
                } else {
                    viewHolder.cbPunctuality.setEnabled(false);
                    if (mList.get(position).isPunctuality())
                        viewHolder.cbPunctuality.setChecked(true);
                    else
                        viewHolder.cbPunctuality.setChecked(false);
                }
                // punctuality------------end-------------------
                if (mList.get(position).isGetPunctuality()) {
                    viewHolder.txtName.setTextColor(ContextCompat.getColor
                            (mContext, R.color.dark_sky));
                } else {
                    viewHolder.txtName.setTextColor(ContextCompat.getColor
                            (mContext, R.color.registrationtextcolor));
                }
            } else {
                viewHolder.txtName.setText(mList.get(position).getUsersName());
                viewHolder.cbPaid.setEnabled(false);
                viewHolder.cbPresent.setEnabled(false);
                viewHolder.cbPunctuality.setEnabled(false);
            }
        }
        return convertView;
    }

    public List<MembersDaoCalendar> getList() {
        return mList;
    }

    public void addMember(MembersDaoCalendar member) {
        mList.add(member);
//        notifyDataSetChanged();
    }

    public void setGetPunctuality(String number) {
        for (int i = 0; i < mList.size(); i++) {
            if (mList.get(i).getNumber().equalsIgnoreCase(number)) {
                mList.get(i).setGetPunctuality(AppConstant.ATTENDANCE_CHECKED);
                mList.get(i).setGetPunctuality(true);
                if (number.contains(AppConstant.SEPERATOR_STRING)) {
                    mList.get(i).setPunctualCouple(true);
                }
            }
        }
        notifyDataSetChanged();
    }

    class Holder {
        private TextView txtName;
        private CheckBox cbPaid;
        private CheckBox cbPresent;
        private CheckBox cbPunctuality;
    }

    /**
     * couple object has puctuality
     *
     * @param list
     * @return
     */
    private boolean hasPunctualCouple(List<MembersDaoCalendar> list) {
        boolean flag = false;
        if (Utils.isValidList(list)) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getGetPunctuality() != null && Utils.isValidString(list.get(i).getGetPunctuality())) {
                    String getPunctuality = list.get(i).getGetPunctuality();
                    getPunctuality = getPunctuality.replace(AppConstant.SEPERATOR_STRING, "");
                    if (Utils.isValidString(getPunctuality) && getPunctuality.equals(AppConstant.STRING_ONE)) {
                        flag = true;
                        break;
                    }
                }
            }
        }
        return flag;
    }
}
