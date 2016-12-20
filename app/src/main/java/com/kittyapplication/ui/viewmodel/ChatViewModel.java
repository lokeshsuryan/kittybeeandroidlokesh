package com.kittyapplication.ui.viewmodel;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.kittyapplication.R;
import com.kittyapplication.adapter.ChatListAdapter;
import com.kittyapplication.custom.ImageLoaderListenerUniversal;
import com.kittyapplication.model.BannerDao;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.ImageUtils;
import com.kittyapplication.utils.PreferanceUtils;

import java.util.List;

/**
 * Created by Pintu Riontech on 8/8/16.
 * vaghela.pintu31@gmail.com
 */
public class ChatViewModel extends MainChatViewModel {
    private static final String TAG = ChatViewModel.class.getSimpleName();
    private Context mContext;

    public ChatViewModel(Context context) {
        super(context);
        mContext = context;
    }

    public void setSearchFilter(EditText editText, final ChatListAdapter adapter) {
        editText.setText("");
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                AppLog.d(TAG, "addTextChangedListener " + s);
                adapter.getFilter().filter(s);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public void setBanner(final ImageView img) {
//        ExecutorThread executorThread = new ExecutorThread();
//        executorThread.startExecutor();
//        executorThread.postTask(new Runnable() {
//            @Override
//            public void run() {
                try {
                    String itemName = mContext.getResources().getStringArray(R.array.adv_banner_name)[1];
                    if (PreferanceUtils.getBannerFromPreferance(mContext) != null) {
                        List<BannerDao> bannerDaoList = PreferanceUtils.getBannerFromPreferance(mContext).getBanner();
                        if (bannerDaoList != null && !bannerDaoList.isEmpty()) {
                            for (int i = 0; i < bannerDaoList.size(); i++) {
                                if (itemName.equalsIgnoreCase(bannerDaoList.get(i).getTitle())) {
                                    img.setVisibility(View.VISIBLE);
                                    ImageUtils.getImageLoader(mContext).displayImage(bannerDaoList.get(i).getThamb(), img,
                                            new ImageLoaderListenerUniversal(mContext, img, "drawable://" + R.drawable.no_image_bottom_banner));
                                    img.setTag(bannerDaoList.get(i).getUrl());
                                    img.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            String url = (String) v.getTag();
                                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                            mContext.startActivity(browserIntent);
                                        }
                                    });
                                    break;
                                } else {
                                    img.setVisibility(View.GONE);
                                }
                            }
                            //img.setVisibility(View.GONE);
                        } else {
                            img.setVisibility(View.GONE);
                        }
                    }
                } catch (Exception e) {
                    AppLog.e(TAG, e.getMessage(), e);
                    img.setVisibility(View.GONE);
                }
//            }
//        });

//        Utils.setBannerItem(mFragment.getActivity(),
//                mContext.getResources().getStringArray(R.array.adv_banner_name)[1],
//                img);
    }
}