package com.kittyapplication.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.kittyapplication.R;
import com.kittyapplication.chat.utils.ImageLoaderUtils;
import com.kittyapplication.core.utils.DeviceUtils;
import com.kittyapplication.custom.ImageLoaderListener;
import com.kittyapplication.chat.ui.activity.AttachmentZoomActivity;

import java.util.ArrayList;

/**
 * Created by MIT on 8/23/2016.
 */
public class MediaAdapter extends RecyclerView.Adapter<MediaHolder> {

    private ArrayList<String> media;
    private Activity activity;

    public MediaAdapter(ArrayList<String> media, Activity activity) {
        this.media = media;
        this.activity = activity;
    }

    @Override
    public MediaHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_media, parent, false);
        return new MediaHolder(v, activity);
    }

    @Override
    public void onBindViewHolder(MediaHolder holder, int position) {
        holder.bind(getItem(position));
    }

    public String getItem(int position) {
        return media.get(position);
    }

    @Override
    public int getItemCount() {
        return media.size();
    }
}

class MediaHolder extends RecyclerView.ViewHolder {
    private ImageView image;
    private ProgressBar progressBar;
    private Activity activity;

    public MediaHolder(View itemView, final Activity activity) {
        super(itemView);
        this.activity = activity;
        image = (ImageView) itemView.findViewById(R.id.imgMedia);
        progressBar = (ProgressBar) itemView.findViewById(R.id.pbMedia);
        setImageParams();
    }

    private void setImageParams() {
        int size = DeviceUtils.getDeviceWidth(activity) / 2;
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size, size);
        image.setLayoutParams(params);
    }

    public void bind(String url) {
        ImageLoaderUtils.getImageLoader(image.getContext())
                .displayImage(url, image, new ImageLoaderListener(progressBar));

        image.setTag(url);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AttachmentZoomActivity.startMediaActivity(activity, (String) v.getTag(), v);
            }
        });
    }
}

