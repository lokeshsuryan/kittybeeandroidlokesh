package com.kittyapplication.adapter;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.kittyapplication.R;
import com.kittyapplication.core.ui.adapter.BaseSelectedRecyclerViewAdapter;
import com.kittyapplication.core.utils.ConnectivityUtils;
import com.kittyapplication.core.utils.ErrorUtils;
import com.kittyapplication.custom.CustomTextViewBold;
import com.kittyapplication.custom.CustomTextViewNormal;
import com.kittyapplication.custom.RoundedImageView;
import com.kittyapplication.model.ContactDao;
import com.kittyapplication.model.ReqInvite;
import com.kittyapplication.model.ServerResponse;
import com.kittyapplication.rest.Singleton;
import com.kittyapplication.ui.activity.ChatActivity;
import com.kittyapplication.utils.AlertDialogUtils;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.ImageUtils;
import com.kittyapplication.utils.PreferanceUtils;
import com.kittyapplication.utils.Utils;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Pintu Riontech on 8/8/16.
 * vaghela.pintu31@gmail.com
 */
public class ContactAdapter extends BaseSelectedRecyclerViewAdapter<ContactDao> implements Filterable {
    private static final String TAG = ContactAdapter.class.getSimpleName();

    private List<ContactDao> mListClone = new ArrayList<>();
    private Activity mContext;
    private ItemFilter filter;

    public ContactAdapter(Activity context, List<ContactDao> lists) {
        super(context, lists);
        try {
            mListClone.addAll(lists);
            mContext = context;
            filter = new ItemFilter();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getCount() {
        return getList().size();
    }

    @Override
    public ContactDao getItem(int position) {
        return getList().get(position);
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
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = inflater.inflate(R.layout.row_layout_contact, null, false);
        return new Holder(convertView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Holder chatHolder = (Holder) holder;
        ContactDao contactDao = getItem(position);
        chatHolder.setPosition(position);
        chatHolder.bind(contactDao);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

    class Holder extends RecyclerView.ViewHolder {
        private CustomTextViewNormal txtContactTitle;
        private CustomTextViewBold txtContactName;
        private CustomTextViewNormal txtContactInvite;
        private ImageView imgContactPhone;
        private RoundedImageView imgContactUser;
        private RelativeLayout rlMain;
        private RelativeLayout rlButtons;
        private int position;

        public void setPosition(int position) {
            this.position = position;
        }

        public Holder(View convertView) {
            super(convertView);

            imgContactUser = (RoundedImageView) convertView.findViewById(R.id.imgContactUser);
            txtContactName = (CustomTextViewBold) convertView.findViewById(R.id.txtContactName);
            txtContactTitle = (CustomTextViewNormal) convertView.findViewById(R.id.txtContactTitle);
            rlMain = (RelativeLayout) convertView.findViewById(R.id.rlContact);
            rlButtons = (RelativeLayout) convertView.findViewById(R.id.rlContactButtons);
            imgContactPhone = (ImageView) rlButtons.findViewById(R.id.imgContactPhone);
            txtContactInvite = (CustomTextViewNormal) rlButtons.findViewById(R.id.txtContactInvite);

            imgContactPhone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    String strPhone = getItem(pos).getPhone();
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + strPhone));
                    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mContext.startActivity(intent);

                }
            });

            txtContactInvite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = (int) v.getTag();
                    inviteMember(getItem(pos), pos);
                }
            });

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContactDao contact = getItem(getAdapterPosition());//(ContactDao) v.getTag(R.layout.row_layout_contact);

                    if (ConnectivityUtils.checkInternetConnection(mContext)) {
                        if (contact.getID() != null) {
                            QBUser user = new QBUser(contact.getLogin(), contact.getPhone());
                            user.setId(Integer.parseInt(contact.getID()));
                            user.setFullName(contact.getName());
                            user.setCustomData(contact.getImage());
                            ChatActivity.startForResult((Activity) mContext,
                                    AppConstant.REQUEST_UPDATE_DIALOG, 0, user, -1);
//                            ChatActivity.startForResult(mContext,
//                                    AppConstant.REQUEST_UPDATE_DIALOG, null,
//                                    user, contact.getUserId(), null);
                        } else {
                            ErrorUtils.showErrorDialog(mContext, R.string.chat_load_users_error, "No Internet");
                        }
                    } else {
                        ErrorUtils.showErrorDialog(mContext, R.string.no_internet_connection, "No Internet");
                    }
                }
            });
        }

        public void bind(ContactDao contactDao) {
            if (Utils.isValidString(contactDao.getName())) {
                txtContactName.setText(contactDao.getName());
            } else {
                txtContactName.setText("");
            }

            txtContactInvite.setVisibility(View.GONE);
            imgContactPhone.setVisibility(View.GONE);

            if (contactDao.getRegistration().equalsIgnoreCase("0")) {
                txtContactInvite.setVisibility(View.VISIBLE);
                imgContactPhone.setVisibility(View.GONE);
            }

            if (contactDao.getRegistration().equalsIgnoreCase("1")) {
                txtContactInvite.setVisibility(View.GONE);
                imgContactPhone.setVisibility(View.VISIBLE);

                if (contactDao.getStatus() != null
                        && !contactDao.getStatus().equalsIgnoreCase(""))
                    txtContactTitle.setText(contactDao.getStatus());
                else
                    txtContactTitle.setText(mContext.getResources().getString(R.string.app_msg));
            } else {
                txtContactTitle.setText(contactDao.getPhone());
            }

            ImageUtils.getImageLoader(mContext).displayImage(contactDao.getImage(), imgContactUser);
            imgContactPhone.setTag(position);

            if (contactDao.isInvite()) {
                txtContactInvite.setVisibility(View.GONE);
            }

            txtContactInvite.setTag(position);
            if (contactDao.getLogin() == null || contactDao.getLogin().length() == 0) {
                itemView.setOnClickListener(null);
            } else {
                itemView.setTag(R.layout.row_layout_contact, contactDao);
            }
        }
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
            updateList((List<ContactDao>) results.values);
            notifyDataSetChanged();
        }
    }

    public void reloadData() {
        updateList(mListClone);
        notifyDataSetChanged();
    }


    private void inviteMember(ContactDao obj, final int pos) {
        if (Utils.checkInternetConnection(mContext)) {
            ReqInvite invite = new ReqInvite();
            invite.setPhone(obj.getPhone());
            invite.setName(PreferanceUtils.getLoginUserObject(mContext).getName());
            Singleton.getInstance().getRestOkClient().
                    inviteMember(invite).enqueue(new Callback<ServerResponse>() {
                @Override
                public void onResponse(Call<ServerResponse> call,
                                       Response<ServerResponse> response) {
                    if (response.body().getResponse() == AppConstant.RESPONSE_SUCCESS) {
                        if (response.body().getMessage() != null) {
                            AlertDialogUtils.showSnackToast(response.body().getMessage()
                                    , mContext);
                            getItem(pos).setInvite(true);
                            notifyDataSetChanged();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ServerResponse> call,
                                      Throwable t) {
                }
            });
        } else {
            AlertDialogUtils.showSnackToast(mContext.getResources()
                            .getString(R.string.no_internet_available)
                    , mContext);
        }

    }

}