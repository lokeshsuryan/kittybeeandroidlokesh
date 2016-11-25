package com.kittyapplication.sqlitedb;

import android.content.Context;

import com.kittyapplication.AppApplication;
import com.kittyapplication.listener.DataInsertedListener;
import com.kittyapplication.model.ChatData;
import com.kittyapplication.model.OfflineDao;
import com.kittyapplication.ui.viewmodel.KittyViewModel;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.GroupPrefHolder;
import com.kittyapplication.utils.Utils;

/**
 * Created by HalfBloodPrince(RIONTECH)
 * Riontech on 14/10/16.
 */

public class SqlDataSetTask {
    private static final String TAG = SqlDataSetTask.class.getSimpleName();
    private Context mContext;
    private DataInsertedListener mListener;

    public SqlDataSetTask(Context ctx, OfflineDao dao) {
        AppLog.e(TAG, "without listener");
        mContext = ctx;
        AppApplication.getInstance().setRefresh(true);
        doInBackground(dao);
    }

    public SqlDataSetTask(Context ctx, OfflineDao dao, DataInsertedListener listener) {
        AppLog.e(TAG, "with listener");
        mContext = ctx;
        AppApplication.getInstance().setRefresh(true);
        mListener = listener;
        doInBackground(dao);
    }


    /**
     *
     */
    private void doInBackground(OfflineDao params) {
        String groupId = null;
        try {
            OfflineDao offlineDao = params;
            if (offlineDao != null) {
                groupId = offlineDao.getGroupID();
                AppApplication.getInstance().setUpdatedGroupId(groupId);

                //TODO add chat data object
                ChatData data = new ChatData();
                data.setMemberId(offlineDao.getMemberId());
                data.setHostId(offlineDao.getHostId());
                data.setRights(offlineDao.getRights());
                data.setGroupID(offlineDao.getGroupID());
                data.setQuickId(offlineDao.getQuickId());
                data.setName(offlineDao.getName());

                data.setCategory(offlineDao.getCategory());
                data.setGroupImage(offlineDao.getGroupImage());
                data.setHostName(offlineDao.getHostName());
                data.setHostnumber(offlineDao.getHostnumber());
                data.setIsHost(offlineDao.getIsHost());
                data.setIsAdmin(offlineDao.getIsAdmin());
                data.setIsVenue(offlineDao.getIsVenue());

                data.setNoOfHost(offlineDao.getNoOfHost());
                data.setKittyId(offlineDao.getKittyId());
                data.setKittyDate(offlineDao.getKittyDate());
                data.setKittyTime(offlineDao.getKittyTime());
                data.setPunctuality(offlineDao.getPunctuality());
                data.setPunctualityTime(offlineDao.getPunctualityTime());
                data.setPunctualityTime2(offlineDao.getPunctualityTime2());
                data.setRule(offlineDao.getRule());

                KittyViewModel model = new KittyViewModel(mContext);
                model.updateGroup(data);


                GroupPrefHolder.getInstance().updateByQBId(data);

                //TODO insert kitty rules
                Operations.insertKittyRules(mContext, offlineDao.getKittyRules(), groupId);

                //TODO insert summary members
                if (Utils.isValidList(offlineDao.getSummaryMembers().getData()))
                    Operations.insertIntoSummary(mContext, offlineDao.getSummaryMembers().getData(), groupId);

                //TODO insert venue data
                if (offlineDao.getVenueData() != null && offlineDao.getVenueData().getId() != null)
                    Operations.insertVenue(mContext, offlineDao.getVenueData(), false);

                //TODO insert diary data
                if (offlineDao.getDairyData() != null) {
                    Operations.insertIntoDiary(mContext, offlineDao.getDairyData(), groupId);
                }

                //TODO insert personal notes
                if (Utils.isValidList(offlineDao.getPersonalNotes()))
                    Operations.insertPersonalNotes(mContext, offlineDao.getPersonalNotes(), false);

                //TODO insert setting
                if (offlineDao.getGroupSetting() != null)
                    Operations.insertSetting(mContext, offlineDao.getGroupSetting());
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        } finally {
            if (mListener != null) {
                AppLog.e(TAG, " listener");
                mListener.insertedSuccess();
            } else
                AppLog.e(TAG, "null listener");
        }
    }

//        void insertedSuccess();
//    }
}
