package com.kittyapplication.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kittyapplication.R;
import com.kittyapplication.custom.CustomTextViewNormal;
import com.kittyapplication.custom.RoundedImageView;
import com.kittyapplication.model.ContactDao;
import com.kittyapplication.utils.ImageUtils;

import java.util.List;

/**
 * Created by HalfBloodPrince(RIONTECH)
 * Riontech on 3/10/16.
 */
public class MembersImageAdapter extends RecyclerView.Adapter<MembersImageAdapter.ViewHolder> {
    private static final String TAG = MembersImageAdapter.class.getSimpleName();
    private List<ContactDao> mList;
    private Context mContext;

    public MembersImageAdapter(List<ContactDao> list, Context ctx) {
        mList = list;
        mContext = ctx;
    }

    @Override
    public MembersImageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                             int viewType) {
        View v = LayoutInflater.from(mContext).
                inflate(R.layout.row_layout_add_members, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        ImageUtils.getImageLoader(mContext).displayImage(mList.get(position).getImage(),
                holder.mImgMember);
        holder.mTxtName.setText(mList.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private RoundedImageView mImgMember;
        private CustomTextViewNormal mTxtName;

        public ViewHolder(View v) {
            super(v);
            mImgMember = (RoundedImageView) v.findViewById(R.id.imgAddedMembers);
            mTxtName = (CustomTextViewNormal) v.findViewById(R.id.txtUserName);
        }
    }

    public void add(int position, ContactDao item) {
        mList.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(ContactDao item) {
        int position = mList.indexOf(item);
        mList.remove(position);
        notifyItemRemoved(position);
    }

    public void changeList(List<ContactDao> list) {
        mList = list;
        notifyDataSetChanged();
    }
}