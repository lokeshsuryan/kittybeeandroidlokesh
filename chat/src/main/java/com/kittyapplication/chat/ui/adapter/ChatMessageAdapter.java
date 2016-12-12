package com.kittyapplication.chat.ui.adapter;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kittyapplication.chat.R;
import com.kittyapplication.chat.custom.CircluarProgressBarWithNumber;
import com.kittyapplication.chat.ui.activity.AttachmentZoomActivity;
import com.kittyapplication.chat.ui.models.QBMessage;
import com.kittyapplication.chat.utils.Consts;
import com.kittyapplication.chat.utils.ImageLoaderUtils;
import com.kittyapplication.chat.utils.SharedPreferencesUtil;
import com.kittyapplication.chat.utils.TimeUtils;
import com.kittyapplication.chat.utils.qb.PaginationHistoryListener;
import com.kittyapplication.core.ui.adapter.BaseListAdapter;
import com.kittyapplication.core.utils.ResourceUtils;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.core.helper.Utils;
import com.quickblox.users.model.QBUser;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Created by HalfBloodPrince(RIONTECH)
 * Riontech on 12/11/16.
 */

public class ChatMessageAdapter extends BaseListAdapter<QBMessage> implements StickyListHeadersAdapter {

    private static final String TAG = ChatMessageAdapter.class.getSimpleName();
    private ChatMessageAdapter.OnItemInfoExpandedListener onItemInfoExpandedListener;
    private PaginationHistoryListener paginationListener;
    private int previousGetCount = 0;
    private int messageBGcolor = Color.GREEN;
    private Map<File, Integer> fileUploadProgressMap;
    private Map<String, Integer> currentFileUploadedMap;
    private Map<Integer, Object[]> senderAvatar;
    private Context mContext;
    private boolean isCurrentFileUploaded;

    public ChatMessageAdapter(Context context, List<QBMessage> chatMessages) {
        super(context, chatMessages);
        fileUploadProgressMap = Collections.synchronizedMap(new HashMap<File, Integer>());
        currentFileUploadedMap = Collections.synchronizedMap(new HashMap<String, Integer>());
        senderAvatar = new HashMap<>();
        mContext = context;
    }

    public void updateFileUploadProgress(File file, int progress) {
        try {
            isCurrentFileUploaded = false;
            fileUploadProgressMap.put(file, progress);
            notifyDataSetChanged();
            Log.d(TAG, "fileUploadProgressMap" + progress);
            if (progress == 100) {
                fileUploadProgressMap.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public QBMessage getMessageById(String messageId) {
        try {
            for (int i = 0; i < getList().size(); i++) {
                String id = getList().get(i).getMessage().getId();
                if (messageId.equalsIgnoreCase(id)) {
                    QBMessage message = getList().get(i);
                    message.setUpdatedIndex(i);
                    return message;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void addSenderAvatar(int senderId, Object[] objects) {
        try {
//            if (!senderAvatar.containsKey(senderId))
            int i = 0;
            Log.d(TAG, "senderId => " + senderId);
            for (Object object : objects) {
                Log.d(TAG, "objects[" + (i++) + "] => " + object);
            }
            Log.d(TAG, "====================================");
            senderAvatar.put(senderId, objects);
            notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void replaceFileIntoAttachment(File file, QBAttachment qbAttachment) {
        try {
            for (QBMessage chatMessage : getList()) {
                for (QBAttachment attachment : chatMessage.getMessage().getAttachments()) {
                    if (attachment.getId().equals("0")) {
                        File oldFile = new File(attachment.getUrl());
                        if (file.equals(oldFile)) {
                            attachment.setId(qbAttachment.getId());
                            attachment.setUrl(qbAttachment.getUrl());
                            currentFileUploadedMap.put(attachment.getId(), qbAttachment.hashCode());
                            break;
                        }
                    }
                }
            }

            notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopProgress(File file) {
        try {
            isCurrentFileUploaded = true;
            fileUploadProgressMap.remove(file);
            fileUploadProgressMap.clear();
            notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setOutGoingMessageBgColor(@ColorRes int color) {
        Log.d(TAG, "setOutGoingMessageBgColor");
        this.messageBGcolor = color;
    }

    private Drawable getOutGoingMessageBg() {
        int resId = R.drawable.outgoing_message_bg;
        return ResourceUtils.changeResourceColor(mContext, R.color.image_loader_color,
                ContextCompat.getDrawable(mContext, R.drawable.outgoing_message_bg));
    }

    private Drawable getIncomingMessageBg() {
        int resId = R.drawable.incoming_message_bg;
        return ResourceUtils.changeResourceColor(mContext, R.color.white, resId);
    }

    public void setOnItemInfoExpandedListener(ChatMessageAdapter.OnItemInfoExpandedListener onItemInfoExpandedListener) {
        this.onItemInfoExpandedListener = onItemInfoExpandedListener;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        try {
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.list_item_chat_message, parent, false);
                holder.messageBodyTextView = (TextView) convertView.findViewById(R.id.text_image_message);
                holder.messageAuthorTextView = (TextView) convertView.findViewById(R.id.text_message_author);
                holder.messageContainerLayout = (LinearLayout) convertView.findViewById(R.id.layout_chat_message_container);
                holder.messageBodyContainerLayout = (LinearLayout) convertView.findViewById(R.id.rlMessageContainer);
                holder.messageInfoTextView = (TextView) convertView.findViewById(R.id.text_message_info);
                holder.attachmentImageView = (ImageView) convertView.findViewById(R.id.image_message_attachment);
//                holder.attachmentProgressBar = (ProgressBar) convertView.findViewById(R.id.progress_message_attachment);
                holder.progressBar = (CircluarProgressBarWithNumber) convertView.findViewById(R.id.progressLoader);
//                holder.txtProgressCounter = (TextView) convertView.findViewById(R.id.txtProgressCounter);
                holder.rlAttachmentProgress = (RelativeLayout) convertView.findViewById(R.id.rlImgProgress);
                holder.userIcon = (ImageView) convertView.findViewById(R.id.imgUserProfile);
//                holder.attachmentProgressBar.setProgress(1);
                holder.messageStatus = (ImageView) convertView.findViewById(R.id.imgMessageStatus);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final QBChatMessage chatMessage = getItem(position).getMessage();

            setIncomingOrOutgoingMessageAttributes(holder, getItem(position));
            setMessageBody(holder, chatMessage);
            setMessageInfo(chatMessage, holder);
            setMessageAuthor(holder, getItem(position));

//            holder.messageContainerLayout.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (hasAttachments(chatMessage)) {
//                        Collection<QBAttachment> attachments = chatMessage.getAttachments();
//                        QBAttachment attachment = attachments.iterator().next();
//                        //                    AttachmentImageActivity.start(context, attachment.getUrl());
//                    } else {
//                        toggleItemInfo(holder, position);
//                    }
//                }
//            });
//            holder.messageContainerLayout.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View v) {
//                    if (hasAttachments(chatMessage)) {
//                        toggleItemInfo(holder, position);
//                        return true;
//                    }
//
//                    return false;
//                }
//            });
            holder.messageInfoTextView.setVisibility(View.VISIBLE);
//            holder.messageStatus.setVisibility(View.GONE);

            downloadMore(position);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return convertView;
    }

    private void downloadMore(int position) {
        try {
            if (position == 0) {
                if (paginationListener != null) {
                    if (getCount() != previousGetCount) {
                        paginationListener.downloadMore();
                        previousGetCount = getCount();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setPaginationHistoryListener(PaginationHistoryListener paginationListener) {
        this.paginationListener = paginationListener;
    }

    private void toggleItemInfo(ViewHolder holder, int position) {
        try {
            boolean isMessageInfoVisible = holder.messageInfoTextView.getVisibility() == View.VISIBLE;
            holder.messageInfoTextView.setVisibility(isMessageInfoVisible ? View.GONE : View.VISIBLE);
//            holder.messageStatus.setVisibility(isMessageInfoVisible ? View.GONE : View.VISIBLE);

            if (onItemInfoExpandedListener != null) {
//                onItemInfoExpandedListener.onItemInfoExpanded(position);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;
        try {
            if (convertView == null) {
                holder = new HeaderViewHolder();
                convertView = inflater.inflate(R.layout.view_chat_message_header, parent, false);
                holder.dateTextView = (TextView) convertView.findViewById(R.id.header_date_textview);
                convertView.setTag(holder);
            } else {
                holder = (HeaderViewHolder) convertView.getTag();
            }

            QBChatMessage chatMessage = getItem(position).getMessage();
            holder.dateTextView.setText(TimeUtils.getDate(chatMessage.getDateSent() * 1000));

            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) holder.dateTextView.getLayoutParams();
            if (position == 0) {
                lp.topMargin = ResourceUtils.getDimen(mContext, R.dimen.chat_date_header_top_margin);
            } else {
                lp.topMargin = 0;
            }
            holder.dateTextView.setLayoutParams(lp);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        QBChatMessage chatMessage = getItem(position).getMessage();
        return TimeUtils.getDateAsHeaderId(chatMessage.getDateSent() * 1000);
    }

    private void setMessageBody(final ViewHolder holder, QBChatMessage chatMessage) {
        try {
            if (hasAttachments(chatMessage)) {
                Collection<QBAttachment> attachments = chatMessage.getAttachments();
                QBAttachment attachment = attachments.iterator().next();

                holder.messageBodyTextView.setVisibility(View.GONE);
                holder.attachmentImageView.setVisibility(View.VISIBLE);
                holder.rlAttachmentProgress.setVisibility(View.VISIBLE);

                if (attachment.getId().equals("0")) {
                    BitmapFactory.Options opt = new BitmapFactory.Options();
                    opt.inSampleSize = 2;
                    Bitmap bmp = BitmapFactory.decodeFile(attachment.getUrl(), opt);
                    holder.attachmentImageView.setImageBitmap(bmp);

//                    holder.progressBar.setVisibility(View.GONE);
                    if (isFileUploading()) {
//                        holder.rlAttachmentProgress.setVisibility(View.VISIBLE);
//                        holder.attachmentProgressBar.setVisibility(View.VISIBLE);
                        holder.rlAttachmentProgress.setVisibility(View.VISIBLE);
                        holder.progressBar.setVisibility(View.VISIBLE);
                        int progress = fileUploadProgressMap.get(new File(attachment.getUrl()));
//                        holder.attachmentProgressBar.setProgress(progress);
                        holder.progressBar.setProgress(progress);
//                        holder.txtProgressCounter.setText("" + progress + "%");
                        Log.d(TAG, "file progress => " + progress);
                    } else {
                        holder.rlAttachmentProgress.setVisibility(View.GONE);
                        holder.progressBar.setVisibility(View.GONE);
//                        holder.rlAttachmentProgress.setVisibility(View.GONE);
                    }

                    holder.attachmentImageView.setTag("file:///" + attachment.getUrl());
                    holder.attachmentImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AttachmentZoomActivity.startMediaActivity((Activity) context, (String) v.getTag(), v);
                        }
                    });
                } else {
                    Log.d(TAG, "Loaded");
//                    holder.rlAttachmentProgress.setVisibility(View.GONE);
//                    holder.rlAttachmentProgress.setVisibility(View.VISIBLE);

                    if (!isCurrentUploadedFile(attachment.getId())) {
                        holder.rlAttachmentProgress.setVisibility(View.VISIBLE);
                        ImageLoadingListener progressListener =
                                ImageLoaderUtils.getImageLoadingListener(holder.progressBar,
                                        holder.rlAttachmentProgress);
                        ImageLoaderUtils.getImageLoader(context)
                                .displayImage(attachment.getUrl(),
                                        holder.attachmentImageView,
                                        ImageLoaderUtils.getDisplayOption(),
                                        progressListener, new ImageLoadingProgressListener() {
                                            @Override
                                            public void onProgressUpdate(String imageUri, View view, int current, int total) {
                                                int done = ((current * 100) / total);
                                                holder.progressBar.setProgress(done);
                                            }

                                        });
                    } else {
                        holder.rlAttachmentProgress.setVisibility(View.GONE);
                        ImageLoaderUtils.getImageLoader(context)
                                .displayImage(attachment.getUrl(),
                                        holder.attachmentImageView,
                                        ImageLoaderUtils.getDisplayOption(null));
                    }
                    holder.attachmentImageView.setTag(attachment.getUrl());
                    holder.attachmentImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AttachmentZoomActivity.startMediaActivity((Activity) context, (String) v.getTag(), v);
                        }
                    });
                }

            } else {
                holder.messageBodyTextView.setText(chatMessage.getBody());
                holder.messageBodyTextView.setVisibility(View.VISIBLE);
                holder.attachmentImageView.setVisibility(View.GONE);
//                holder.rlAttachmentProgress.setVisibility(View.GONE);
                holder.rlAttachmentProgress.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setMessageStatusIcon(ViewHolder holder, QBMessage message) {

        if (message.getRead() == 1) {
            holder.messageStatus.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_message_read));
        } else if (message.getDelivered() == 1) {
            holder.messageStatus.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_message_delivered));
        } else if (message.getSent() == 1) {
            holder.messageStatus.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_message_sent));
        } else if (message.getFail() == 1) {
            holder.messageStatus.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_message_not_sent));
        } else {
            holder.messageStatus.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_message_not_sent));
        }

    }


    private boolean isCurrentUploadedFile(String key) {
        return currentFileUploadedMap.containsKey(key);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void setMessageAuthor(ViewHolder holder, QBMessage message) {
        try {
            QBChatMessage chatMessage = message.getMessage();
            Object name = chatMessage.getProperty(Consts.KEY_USERNAME);
            if (isIncoming(chatMessage)) {
//                QBUser sender = QbUsersHolder.getInstance().getUserById(chatMessage.getSenderId());
                holder.userIcon.setVisibility(View.VISIBLE);
                holder.messageAuthorTextView.setVisibility(View.VISIBLE);
//                holder.messageAuthorTextView.setTextColor(ResourceUtils.getColor(messageBGcolor));


                if (name == null) {
                    holder.messageAuthorTextView.setVisibility(View.GONE);
                    holder.userIcon.setVisibility(View.GONE);
                    holder.messageContainerLayout.setGravity(Gravity.CENTER_HORIZONTAL);
                    holder.messageBodyTextView.setGravity(Gravity.CENTER_HORIZONTAL);
                    holder.messageBodyContainerLayout.setBackgroundColor(0);
                    holder.messageInfoTextView.setVisibility(View.GONE);
                } else {
                    if (message.getSenderName() != null
                            && message.getSenderName().length() > 0
                            && message.getSenderName().equalsIgnoreCase("null")) {
                        holder.messageAuthorTextView.setText("" + message.getSenderName());
                    } else {
                        String userName = String.valueOf((String) message.getMessage().getProperty("username"));
                        holder.messageAuthorTextView.setText(userName);
                    }
                    if (message.getSenderImage() != null)
                        holder.userIcon.setImageDrawable(message.getSenderImage());
                    else
                        holder.userIcon.setImageDrawable(ResourceUtils.getDrawable(mContext, R.drawable.ic_user));
                    holder.messageBodyContainerLayout.setBackground(getIncomingMessageBg());
                }
            } else {
                holder.messageAuthorTextView.setVisibility(View.GONE);
                holder.userIcon.setVisibility(View.GONE);
                if (name == null) {
                    holder.messageContainerLayout.setGravity(Gravity.CENTER_HORIZONTAL);
                    holder.messageBodyTextView.setGravity(Gravity.CENTER_HORIZONTAL);
                    holder.messageBodyContainerLayout.setBackgroundColor(0);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

    }

    private void setMessageInfo(QBChatMessage chatMessage, ViewHolder holder) {
        holder.messageInfoTextView.setText(TimeUtils.getTime(chatMessage.getDateSent() * 1000));
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @SuppressLint("RtlHardcoded")
    private void setIncomingOrOutgoingMessageAttributes(ViewHolder holder, QBMessage chatMessage) {
        try {
            boolean isIncoming = isIncoming(chatMessage.getMessage());
            int gravity = isIncoming ? Gravity.LEFT | Gravity.START : Gravity.RIGHT | Gravity.END;
            holder.messageContainerLayout.setGravity(gravity);

            Drawable messageBodyContainerBgResource = isIncoming
                    ? getIncomingMessageBg()
                    : getOutGoingMessageBg();
            holder.messageBodyContainerLayout.setBackground(messageBodyContainerBgResource);

//            int textColorResource = isIncoming
//                    ? R.color.black
//                    : R.color.background_chat_color;

            int textColorResource = R.color.black;
            holder.messageBodyTextView.setTextColor(ResourceUtils.getColor(mContext, textColorResource));
            holder.messageInfoTextView.setTextColor(ResourceUtils.getColor(mContext, textColorResource));

            //set icon
            if (gravity == (Gravity.RIGHT | Gravity.END)) {
                holder.messageStatus.setVisibility(View.VISIBLE);
                setMessageStatusIcon(holder, chatMessage);
            } else {
                holder.messageStatus.setVisibility(View.GONE);
            }
//            int color = isIncoming ? R.color.white : messageBGcolor;
//            changeProgressbarColor(holder.progressBar, color);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean hasAttachments(QBChatMessage chatMessage) {
        Collection<QBAttachment> attachments = chatMessage.getAttachments();
        return attachments != null && !attachments.isEmpty();
    }

    private boolean isIncoming(QBChatMessage chatMessage) {
        QBUser currentUser = SharedPreferencesUtil.getQbUser();
        return chatMessage.getSenderId() != null && !chatMessage.getSenderId().equals(currentUser.getId());
    }

    private static class HeaderViewHolder {
        public TextView dateTextView;
    }

    private void changeProgressbarColor(ProgressBar progressBar, int color) {
        progressBar.getIndeterminateDrawable().setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_ATOP);
    }

    private static class ViewHolder {
        public TextView messageBodyTextView;
        public TextView messageAuthorTextView;
        public TextView messageInfoTextView;
        public LinearLayout messageContainerLayout;
        public LinearLayout messageBodyContainerLayout;
        public ImageView attachmentImageView;
        //        public ProgressBar attachmentProgressBar;
        public CircluarProgressBarWithNumber progressBar;
        //        public TextView txtProgressCounter;
        public RelativeLayout rlAttachmentProgress;
        public ImageView userIcon, messageStatus;
    }

    public interface OnItemInfoExpandedListener {
        void onItemInfoExpanded(int position);
    }

    private boolean isFileUploading() {
        return fileUploadProgressMap.size() > 0;
    }

    public void clearChatData() {
        getList().clear();
        notifyDataSetChanged();
    }

    public void updateObject(QBMessage message, int pos) {
        Log.i(TAG, "updateObject: ");
        try {
            Log.i(TAG, "updateObject: try");
            QBMessage message1 = getList().get(pos);
            message1.setFail(message.getFail());
            message1.setSent(message.getSent());
            message1.setRead(message.getRead());
            message1.setDelivered(message.getDelivered());
            message1.setMessage(message.getMessage());
            getList().set(pos, message);
            notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateList(final QBMessage message, final int pos) {
        ((AppCompatActivity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    QBMessage message1 = getList().get(pos);
                    message1.setFail(message.getFail());
                    message1.setSent(message.getSent());
                    message1.setRead(message.getRead());
                    message1.setDelivered(message.getDelivered());
//                    message1.setMessage(message.getMessage());
                    getList().set(pos, message);
                    notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
