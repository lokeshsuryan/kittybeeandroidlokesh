package com.kittyapplication.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.kittyapplication.R;
import com.kittyapplication.core.async.BaseAsyncTask;
import com.kittyapplication.core.ui.adapter.BaseSelectedRecyclerViewAdapter;
import com.kittyapplication.custom.CustomTextViewBold;
import com.kittyapplication.custom.CustomTextViewNormal;
import com.kittyapplication.custom.RoundedImageView;
import com.kittyapplication.model.ChatData;
import com.kittyapplication.model.Kitty;
import com.kittyapplication.ui.activity.ChatActivity;
import com.kittyapplication.ui.activity.HomeActivity;
import com.kittyapplication.ui.callback.ChatActionModeCallback;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.ImageUtils;
import com.kittyapplication.utils.Utils;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;

import java.util.ArrayList;
import java.util.List;

import static android.support.v7.widget.RecyclerView.ViewHolder;

/**
 * Created by MIT on 10/22/2016.
 */
public class ChatRecyclerAdapter extends BaseSelectedRecyclerViewAdapter<Kitty>
        implements Filterable {

    private static final String TAG = ChatListAdapter.class.getSimpleName();
    private ItemFilter filter;
    private ProgressDialog mDialog;
    private List<Kitty> mListClone;
    private ActionMode currentActionMode;

    public ChatRecyclerAdapter(Context context, List<Kitty> objectsList) {
        super(context, objectsList);
        filter = new ItemFilter();
        mListClone = objectsList;
    }

    public void setCurrentActionMode(ActionMode currentActionMode) {
        this.currentActionMode = currentActionMode;
    }

    public void addCreatedDialog(QBDialog qbDialog) {
        try {
            Kitty Kitty = new Kitty();
            Kitty.setQbDialog(qbDialog);
            addAt(0, Kitty);
            notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updatedMessage(QBChatMessage message) {
        try {
            for (Kitty Kitty : getList()) {
                QBDialog qbDialog = Kitty.getQbDialog();
                if (qbDialog.getDialogId().equals(message.getDialogId())) {
                    qbDialog.setLastMessage(message.getBody());
                    int unreadCount = qbDialog.getUnreadMessageCount();
                    qbDialog.setUnreadMessageCount(unreadCount + 1);
                    remove(Kitty);
                    addAt(0, Kitty);
                    notifyDataSetChanged();
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Update dialog when change in last message
     *
     * @param qbDialog
     */
    public void updatedDialog(final QBDialog qbDialog) {
        try {
            new BaseAsyncTask<Void, Void, Void>() {
                boolean isCreated = true;
                private Kitty kitty = null;
                private int index = 0;
                private boolean isUpdated = true;

                @Override
                public Void performInBackground(Void... params) throws Exception {
                    AppLog.d(TAG, "updatedDialog::" + new Gson().toJson(qbDialog));

                    try {
                        String qbLastMessage = qbDialog.getLastMessage();
                        for (int i = 0; i < getList().size(); i++) {
                            Kitty kitty = getList().get(i);
                            String groupLastMessage = kitty.getQbDialog().getLastMessage();
                            if (kitty.getQbDialog().getDialogId().equals(qbDialog.getDialogId())) {
                                // Don't update if no changes in last message
                                if (qbLastMessage != null && !groupLastMessage.equals(qbLastMessage)) {
                                    this.kitty = kitty;
                                    index = i;
                                    isCreated = false;
                                    break;
                                } else {
                                    this.kitty = kitty;
                                    isUpdated = false;
                                    isCreated = false;
                                    break;
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                public void onResult(Void aVoid) {
                    try {
                        String qbLastMessage = qbDialog.getLastMessage();
                        if (isCreated) {
                            if (qbLastMessage != null && qbLastMessage.length() > 0) {
                                Log.d(TAG, "isCreated ");
                                addCreatedDialog(qbDialog);
                            }
                        } else if (!isCreated && isUpdated) {
                            remove(index);
                            kitty.setQbDialog(qbDialog);
                            addAt(0, kitty);
                            notifyDataSetChanged();
                        } else {
                            if (kitty != null) {
                                kitty.setQbDialog(qbDialog);
                                notifyDataSetChanged();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        } catch (Exception e) {
            e.printStackTrace();
        }
//        try {
//
//            AppLog.d(TAG, "updatedDialog::" + new Gson().toJson(qbDialog));
//            boolean isCreated = true;
//            String qbLastMessage = qbDialog.getLastMessage();
//            for (int i = 0; i < getList().size(); i++) {
//                Kitty kitty = getList().get(i);
//                String groupLastMessage = kitty.getQbDialog().getLastMessage();
//                if (kitty.getQbDialog().getDialogId().equals(qbDialog.getDialogId())) {
//                    // Don't update if no changes in last message
//                    if (qbLastMessage != null && !groupLastMessage.equals(qbLastMessage)) {
//                        remove(i);
//                        kitty.setQbDialog(qbDialog);
//                        addAt(0, kitty);
//                        notifyDataSetChanged();
//                        isCreated = false;
//                        break;
//                    } else {
//                        isCreated = false;
//                        break;
//                    }
//                }
//            }
//            AppLog.d(TAG, "Last Message::" + qbLastMessage);
//            // add new created dialog into list if last message created
//            if (isCreated) {
//                if (qbLastMessage != null && qbLastMessage.length() > 0) {
//                    Log.d(TAG, "isCreated ");
//                    addCreatedDialog(qbDialog);
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    /**
     * @param group
     */
    public void updatedGroupChat(Kitty group) {
        try {
            boolean isCreated = true;
            AppLog.d(TAG, "updatedGroupChat: " + group.getGroup().getName());
            for (int i = 0; i < getList().size(); i++) {
                Kitty kitty = getList().get(i);
                QBDialog qbDialog = group.getQbDialog();

                if (kitty.getQbDialog().getDialogId().equals(qbDialog.getDialogId())) {
                    // Don't update if no changes in last message
                    if (getList().size() < group.getId()) {
                        add(group);
                    } else if (getList().size() == group.getId()) {
                        remove(i);
                        addAt(group.getId() - 1, group);
                    } else {
                        remove(i);
                        addAt(group.getId(), group);
                    }

                    notifyDataSetChanged();
                    isCreated = false;
                    break;
                }
            }

            // add new created group into list if last message created
            if (isCreated) {
                if (getList().size() > group.getId())
                    addAt(group.getId(), group);
                else
                    add(group);
                AppLog.d(TAG, "Group position = >" + group.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = inflater.inflate(R.layout.row_chat_list, null, false);
        return new ChatHolder(convertView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ChatHolder chatHolder = (ChatHolder) holder;
        Kitty gc = getItem(position);
        gc.setChecked(isSelected(gc));
        chatHolder.setPosition(position);
        chatHolder.bind(getItem(position));
        holder.itemView.setActivated(gc.isChecked());
        chatHolder.startItemSelectionAnimation(gc);
        downloadMore(position);
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String filterString = constraint.toString().toLowerCase();
            FilterResults results = new FilterResults();
            try {
                List<Kitty> list = mListClone;
                int count = list.size();
                final ArrayList<Kitty> newList = new ArrayList<>(count);

                String filterableString;

                for (int i = 0; i < count; i++) {
                    if (list.get(i).getQbDialog() != null) {
                        filterableString = list.get(i).getQbDialog().getName();
                        if (Utils.isValidString(filterableString))
                            if (filterableString.toLowerCase().contains(filterString)) {
                                newList.add(list.get(i));
                            }
                    }
                }

                results.values = newList;
//                AppLog.d(TAG, new Gson().toJson(newList).toString());
//                results.count = newList.size();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            try {
                updateList((List<Kitty>) results.values);
                notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void reloadData() {
        try {
            updateList(mListClone);
            notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showDialog() {
        mDialog = new ProgressDialog(context);
        mDialog.setMessage(context.getResources().getString(R.string.loading_text));
        mDialog.show();
    }

    public void hideDialog() {
        try {
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss();
                mDialog = null;
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public boolean isSelected(Kitty chat) {
        if (getSelectedItems().size() > 0) {
            for (Kitty kitty : getSelectedItems()) {
                return chat.getId() == kitty.getId();
            }
        }
        return false;
    }

    class ChatHolder extends ViewHolder {
        CustomTextViewBold txtChatTitle;
        CustomTextViewNormal txtChatLastMessage;
        RoundedImageView imgChatUser;
        RelativeLayout rlMain;
        ImageView cbSelected;
        TextView txtMessageUnreadCount;
        private int position;

        public ChatHolder(View convertView) {
            super(convertView);
            convertView.findViewById(R.id.imgChatHost).setVisibility(View.GONE);
            convertView.findViewById(R.id.imgChatDairy).setVisibility(View.GONE);
            convertView.findViewById(R.id.imgChatAnnouncement).setVisibility(View.GONE);
            convertView.findViewById(R.id.imgChatHostAdmin).setVisibility(View.GONE);

            imgChatUser = (RoundedImageView) convertView.findViewById(R.id.imgChatUser);
            txtChatTitle = (CustomTextViewBold) convertView.findViewById(R.id.txtChatTitle);
            txtChatLastMessage = (CustomTextViewNormal) convertView.findViewById(R.id.txtChatLastMessage);
            txtMessageUnreadCount = (TextView) convertView.findViewById(R.id.txtMessageUnreadCount);
            rlMain = (RelativeLayout) convertView.findViewById(R.id.rlChatRow);
            cbSelected = (ImageView) convertView.findViewById(R.id.cbSelected);
            setParams();
        }

        private void setItemClickListener(Kitty kitty) {
            itemView.setTag(kitty);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Kitty kitty = (Kitty) v.getTag();
                    QBDialog dialog = kitty.getQbDialog();
                    ChatActivity.startForResult((Activity) itemView.getContext(),
                            AppConstant.REQUEST_UPDATE_DIALOG, kitty.getId(), null);
                }
            });

        }

        private void setItemLongClickListener(Kitty kitty) {
            itemView.setTag(kitty);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Kitty k = (Kitty) v.getTag();
                    if (isSelected(k)) {
                        clearSelection();
                        currentActionMode.finish();
                        currentActionMode = null;
                    } else {
                        System.out.println(TAG + "selectItem size = >" + getSelectedItems().size());
                        ChatActionModeCallback callback = new ChatActionModeCallback(ChatRecyclerAdapter.this, context, isPrivate(k));
                        HomeActivity activity = (HomeActivity) context;
                        currentActionMode = activity.startSupportActionMode(callback);
                        currentActionMode.setTitle(k.getQbDialog().getName());
                        activity.setCurrentActionMode(currentActionMode);
                        callback.setCurrentActionMode(currentActionMode);
                        selectItem(position);
                    }
                    return true;
                }
            });
        }

        private void setParams() {
            RelativeLayout rlMessageBody = (RelativeLayout) rlMain.findViewById(R.id.rlMessageBody);
            int width = RelativeLayout.LayoutParams.MATCH_PARENT;
            int height = RelativeLayout.LayoutParams.WRAP_CONTENT;
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
            params.addRule(RelativeLayout.CENTER_VERTICAL);
            params.addRule(RelativeLayout.RIGHT_OF, R.id.imgChatUser);
            params.addRule(RelativeLayout.END_OF, R.id.imgChatUser);
            params.addRule(RelativeLayout.LEFT_OF, R.id.txtMessageUnreadCount);
            params.addRule(RelativeLayout.START_OF, R.id.txtMessageUnreadCount);
            rlMessageBody.setLayoutParams(params);
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public void bind(Kitty kitty) {
            try {
                QBDialog dialog = kitty.getQbDialog();
                if (dialog != null && Utils.isValidString(dialog.getName()))
                    setChatTitle(dialog.getName());
                if (dialog != null && dialog.getLastMessage() != null)
                    setLastMessage(dialog.getLastMessage());
                if (dialog != null && dialog.getUnreadMessageCount() != null && dialog.getUnreadMessageCount() > 0)
                    setUnreadMessageCount(dialog.getUnreadMessageCount());
                else
                    txtMessageUnreadCount.setVisibility(View.GONE);

                // user for remove item animation
                kitty.setSelectedItemPosition(position);
                View view = itemView.findViewById(R.id.rlChatRow);
                kitty.setSelectedItemView(view);

                setPrivateAndKittyAttribute(kitty);
                setItemClickListener(kitty);
                setItemLongClickListener(kitty);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        private void setPrivateAndKittyAttribute(Kitty kitty) {
            try {
                QBDialog dialog = kitty.getQbDialog();
                setProfileImage(dialog.getPhoto());
                ChatData chatData = kitty.getGroup();
                if (chatData != null && Utils.isValidString(chatData.getGroupImage()))
                    setProfileImage(chatData.getGroupImage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void setProfileImage(String url) {
            try {
                ImageUtils.getImageLoader(rlMain.getContext()).displayImage(url, imgChatUser);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void setChatTitle(String title) {
            txtChatTitle.setText(title);
        }

        private void setLastMessage(String message) {
            txtChatLastMessage.setText(message);
        }

        private void setUnreadMessageCount(int count) {
            try {
                txtMessageUnreadCount.setVisibility(View.VISIBLE);
                txtMessageUnreadCount.setText("" + count);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void startItemSelectionAnimation(Kitty Kitty) {
            if (Kitty.isChecked()) {
                if (!Kitty.isAnimated()) {
                    Kitty.setAnimated(true);
                    applyItemCheckedAnimation();
                } else
                    cbSelected.setVisibility(View.VISIBLE);
            } else {
                if (Kitty.isAnimated()) {
                    Kitty.setAnimated(false);
                    applyItemCheckedReverseAnimation();
                } else
                    cbSelected.setVisibility(View.GONE);

            }
        }

        public void applyItemCheckedAnimation() {
            Animation anim = AnimationUtils.loadAnimation(cbSelected.getContext(), R.anim.zoom_out);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    cbSelected.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            cbSelected.startAnimation(anim);
        }

        public void applyItemCheckedReverseAnimation() {
            Animation anim = AnimationUtils.loadAnimation(cbSelected.getContext(), R.anim.zoom_in);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    cbSelected.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            cbSelected.startAnimation(anim);
        }

        public boolean isSelected(Kitty kitty) {

            if (getSelectedItems().size() > 0) {
                for (Kitty selectedKitty : getSelectedItems()) {
                    return kitty.getId() == selectedKitty.getId();
                }
            }
            return false;
        }

        private boolean isPrivate(Kitty data) {
            if (data.getQbDialog().getType() == QBDialogType.PRIVATE)
                return true;
            return false;
        }
    }
}

