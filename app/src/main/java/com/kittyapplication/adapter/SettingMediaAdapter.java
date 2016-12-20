package com.kittyapplication.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.kittyapplication.R;
import com.kittyapplication.utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pintu Riontech on 29/8/16.
 * vaghela.pintu31@gmail.com
 */
public class SettingMediaAdapter extends RecyclerView.Adapter<SettingMediaAdapter.ViewHolder> {
    private static final String TAG = SettingMediaAdapter.class.getSimpleName();

    private final Context mContext;
    private final List<String> mListMedia;


    public SettingMediaAdapter(Context context,
                               List<String> mediaList) {
        List<String> media = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            media.add("https://encrypted-tbn3.gstatic.com/images?q=tbn:ANd9GcSoZ9SgkZ1bxnGd7XuAYG6uz8vduTDKHdM-FMi6THC_g4VtRX-IoLTbnt2I");
        }
        this.mContext = context;
        this.mListMedia = media;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).
                inflate(R.layout.row_layout_media_setting, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ImageUtils.getImageLoader(mContext)
                .displayImage(mListMedia.get(position), holder.imgMedia,
                        ImageUtils.getDisplayOptionProfileRoundedCorner());

    }

    @Override
    public int getItemCount() {
        return mListMedia.size();
    }

    /**
     * View Holder
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgMedia;

        private ViewHolder(View itemView) {
            super(itemView);
            this.imgMedia = (ImageView) itemView.findViewById(R.id.imgSettingMedia);
        }
    }
}