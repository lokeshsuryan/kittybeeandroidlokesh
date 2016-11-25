package com.kittyapplication.ui.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.kittyapplication.R;
import com.kittyapplication.adapter.MediaAdapter;
import com.kittyapplication.utils.AppConstant;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.core.request.QueryRule;

import java.util.ArrayList;

/**
 * Created by Pintu Riontech on 20/8/16.
 * vaghela.pintu31@gmail.com
 */
public class MediaActivity extends BaseActivity {

    private static final String EXTRA_MEDIA = "media";
    private static final String INTENT_MEDIA_DATA = "media_data";
    private ArrayList<String> mMediaList;
    private QBDialog qbDialog;
    private RecyclerView recyclerView;
    private ProgressDialog progressDialog;

    public static void startMediaActivity(Activity activity, ArrayList<String> list) {
        Intent intent = new Intent(activity, MediaActivity.class);
        intent.putExtra(MediaActivity.EXTRA_MEDIA, list);
        activity.startActivity(intent);
    }

    public static void startMediaActivity(Activity activity, QBDialog qbDialog, ArrayList<String> list) {
        Intent intent = new Intent(activity, MediaActivity.class);
        intent.putExtra(EXTRA_MEDIA, list);
        intent.putExtra(INTENT_MEDIA_DATA, qbDialog);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(MediaActivity.this).inflate(
                R.layout.activity_media, null);
        addLayoutToContainer(view);
        hideLeftIcon();
        hideBottomLayout();

        recyclerView = (RecyclerView) view;
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        // Set the adapter for RecyclerView

        mMediaList = (ArrayList<String>) getIntent().getSerializableExtra(EXTRA_MEDIA);
        if (mMediaList != null && !mMediaList.isEmpty()) {
            recyclerView.setAdapter(new MediaAdapter(mMediaList, this));
        } else {
            progressDialog = new ProgressDialog(MediaActivity.this);
            progressDialog.setMessage("Loading...");
            progressDialog.show();
            qbDialog = (QBDialog) getIntent().getSerializableExtra(INTENT_MEDIA_DATA);
            getMediaListFromServer();
        }
    }

    @Override
    String getActionTitle() {
        return getResources().getString(R.string.media);
    }

    @Override
    boolean isDisplayHomeAsUpEnabled() {
        return true;
    }

    @Override
    boolean hasDrawer() {
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                if (hasDrawer()) {
                    toggle();
                } else {
                    onBackPressed();
                }
                break;
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }

    /**
     *
     */
    private void getMediaListFromServer() {
        QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
        requestBuilder.setLimit(100);
        requestBuilder.addRule("attachments.type", QueryRule.EQ, QBAttachment.PHOTO_TYPE);


        QBChatService.getDialogMessages(qbDialog, requestBuilder, new QBEntityCallback<ArrayList<QBChatMessage>>() {
            @Override
            public void onSuccess(ArrayList<QBChatMessage> messages, Bundle args) {
                if (progressDialog != null)
                    progressDialog.dismiss();
                if (messages != null && !messages.isEmpty()) {
                    for (QBChatMessage message : messages) {
                        for (QBAttachment attachment : message.getAttachments()) {
                            if (mMediaList != null && !mMediaList.contains(attachment.getUrl()))
                                mMediaList.add(attachment.getUrl());
                        }
                    }
                }
                recyclerView.setAdapter(new MediaAdapter(mMediaList, MediaActivity.this));
            }

            @Override
            public void onError(QBResponseException errors) {
                if (progressDialog != null)
                    progressDialog.dismiss();
            }
        });
    }
}
