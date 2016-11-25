package com.kittyapplication.ui.viewmodel;

import android.view.View;
import android.widget.ImageView;

import com.kittyapplication.R;
import com.kittyapplication.ui.activity.WalkThroughActivity;
import com.kittyapplication.utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dhaval Riontech on 21/8/16.
 */
public class WalkThroughViewModel {
    private WalkThroughActivity mActivity;
    private List<View> mList;

    public WalkThroughViewModel(WalkThroughActivity activity) {
        mActivity = activity;
    }

    public List<View> getImageList() {
        mList = new ArrayList<>();
        mList.add(loagImage(R.drawable.walk_through_image1));
        mList.add(loagImage(R.drawable.walk_through_image2));
        mList.add(loagImage(R.drawable.walk_through_image3));
        mList.add(loagImage(R.drawable.walk_through_image4));
        return mList;
    }

    private ImageView loagImage(int walkThrougnImage) {
        ImageView imageView = new ImageView(mActivity);
        ImageUtils.getImageLoader(mActivity).displayImage(("drawable://" + walkThrougnImage), imageView);
//        imageView.setImageResource(walkThrougnImage);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        return imageView;
    }
}
