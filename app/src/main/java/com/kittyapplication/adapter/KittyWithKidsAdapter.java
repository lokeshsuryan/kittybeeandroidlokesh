package com.kittyapplication.adapter;

import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.kittyapplication.R;
import com.kittyapplication.custom.CustomTextViewBold;
import com.kittyapplication.custom.CustomTextViewNormal;
import com.kittyapplication.custom.RoundedImageView;
import com.kittyapplication.listener.AddKidsCountListener;
import com.kittyapplication.model.ContactDao;
import com.kittyapplication.utils.AlertDialogUtils;
import com.kittyapplication.utils.ImageUtils;
import com.kittyapplication.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pintu Riontech on 12/8/16.
 * vaghela.pintu31@gmail.com
 */
public class KittyWithKidsAdapter extends BaseAdapter implements Filterable, AddKidsCountListener {
    private static final String TAG = ContactAdapter.class.getSimpleName();

    private List<ContactDao> mList;
    private List<ContactDao> mListClone;
    private Context mContext;
    private static LayoutInflater inflater = null;
    private ItemFilter filter;
    private List<ContactDao> mSelectedList;
    private int mType;

    public KittyWithKidsAdapter(Context context, List<ContactDao> lists) {
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

    @Override
    public void getKidsCount(int pos, String text) {
        mList.get(pos).setKids(text);
        notifyDataSetChanged();

    }

    public class Holder {
        private RoundedImageView mImgUser;
        private CustomTextViewBold mTxtUserName;
        private CustomTextViewNormal mTxtNumber;
        private CustomTextViewNormal edtKidsCount;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Holder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.row_layout_kitty_with_kids, parent, false);
            viewHolder = new Holder();
            viewHolder.mImgUser = (RoundedImageView) convertView.findViewById(R.id.imgKittyKidsMember);
            viewHolder.mTxtUserName = (CustomTextViewBold) convertView.findViewById(R.id.txtKittyKidsMemberName);
            viewHolder.mTxtNumber = (CustomTextViewNormal) convertView.findViewById(R.id.txtKittyKidsMemberNumber);
            viewHolder.edtKidsCount = (CustomTextViewNormal) convertView.findViewById(R.id.edtKittyKidsMemberKidsCount);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (Holder) convertView.getTag();
        }

        ImageUtils.getImageLoader(mContext).displayImage(mList.get(position).getImage(), viewHolder.mImgUser);

        viewHolder.mTxtUserName.setText(Utils.checkNumberIsLoginUser(mContext,
                mList.get(position).getPhone(), mList.get(position).getName()));

        if (Utils.isValidString(mList.get(position).getPhone())) {
            viewHolder.mTxtNumber.setText(mList.get(position).getPhone());
        } else {
            viewHolder.mTxtNumber.setText("");
        }



        viewHolder.edtKidsCount.setTag(position);
        viewHolder.edtKidsCount.setText(mList.get(position).getKids());
        viewHolder.edtKidsCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = (int) v.getTag();
                showKidsCountDialog(pos);

            }
        });
        return convertView;
    }

    private void showKidsCountDialog(int pos) {
        AlertDialogUtils.showEnterKidsCountDialog(mContext,
                this, pos);
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

    public void setImeiOption(EditText edt, int pos) {
        edt.setTag(pos);
        String count = Utils.getText(edt);
        edt.setText(count);
        // edt.setTag(R.id.edtKittyKidsMemberKidsCount, count);
        edt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    /*int pos = (int) v.getTag();
                    SpannableStringBuilder count = (SpannableStringBuilder) v.getText();
                    Utils.hideKeyboard(mContext, v);
                    setKidsCountToList(pos, count);
                    v.setFocusable(false);*/
                }
                return false;
            }
        });
        //setKidsCountToList(pos, count);
    }


    public List<ContactDao> getListData() {
        return mList;
    }
}