package com.kittyapplication.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.kittyapplication.R;
import com.kittyapplication.core.ui.adapter.BaseSelectedRecyclerViewAdapter;
import com.kittyapplication.custom.CustomTextViewBold;
import com.kittyapplication.custom.CustomTextViewNormal;
import com.kittyapplication.custom.ImageLoaderListener;
import com.kittyapplication.model.PromotionalDao;
import com.kittyapplication.ui.activity.PartnersDetailsActivity;
import com.kittyapplication.utils.ImageUtils;

import java.util.List;

/**
 * Created by HalfBloodPrince(RIONTECH)
 * Riontech on 21/11/16.
 */

public class PromotionalRecyclerAdapter extends BaseSelectedRecyclerViewAdapter<PromotionalDao> {
    private static final String TAG = PromotionalRecyclerAdapter.class.getSimpleName();
    private List<PromotionalDao> mListClone;
    private Context mContext;

    public PromotionalRecyclerAdapter(Context context, List<PromotionalDao> objectsList) {
        super(context, objectsList);
        mListClone = objectsList;
        mContext = context;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = LayoutInflater.from(mContext).
                inflate(R.layout.row_layout_parterner, null, false);
        return new PromotionalHolder(convertView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        PromotionalHolder promotionalHolder = (PromotionalHolder) holder;
        promotionalHolder.bindView(getItem(position));
    }

    class PromotionalHolder extends RecyclerView.ViewHolder {
        CustomTextViewBold txtName;
        CustomTextViewNormal txtAddress;
        ImageView imgPromotional;
        ProgressBar pbLoaderImage;

        public PromotionalHolder(View convertView) {
            super(convertView);
            txtName = (CustomTextViewBold) convertView.findViewById(R.id.txtPartnerNamePromotional);
            imgPromotional = (ImageView) convertView.findViewById(R.id.imgPartnerPromotional);
            txtAddress = (CustomTextViewNormal) convertView.findViewById(R.id.txtPartnerPromotionalAddress);
            pbLoaderImage = (ProgressBar) convertView.findViewById(R.id.pbLoaderPartnerPromotionalImage);

        }

        private void setItemClickListener(PromotionalDao data, int pos) {
            itemView.setTag(data);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PromotionalDao dao = (PromotionalDao) v.getTag();
                    Intent intent = new Intent(mContext, PartnersDetailsActivity.class);
                    intent.putExtra("object", new Gson().toJson(dao).toString());
                    mContext.startActivity(intent);
                }
            });
        }

        private void bindView(PromotionalDao dao) {
            txtName.setText(dao.getName());
            txtAddress.setText(dao.getAddress());
            ImageUtils.getImageLoader(mContext).displayImage(dao.getThumb(),
                    imgPromotional, new ImageLoaderListener(pbLoaderImage));
            setItemClickListener(dao, getAdapterPosition());

        }
    }
}

