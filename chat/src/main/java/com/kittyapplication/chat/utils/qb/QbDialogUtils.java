package com.kittyapplication.chat.utils.qb;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.kittyapplication.chat.utils.SharedPreferencesUtil;
import com.kittyapplication.chat.utils.chat.ChatHelper;
import com.kittyapplication.chat.utils.chat.QbUpdateDialogListener;
import com.kittyapplication.chat.utils.qb.callback.QBGetGroupID;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBGroupChatManager;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.request.QBDialogRequestBuilder;
import com.quickblox.chat.utils.DialogUtils;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.core.request.QBRequestUpdateBuilder;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class QbDialogUtils {
    private static final String TAG = QbDialogUtils.class.getSimpleName();

    public static QBChatDialog createDialog(List<QBUser> users) {
        QBUser currentUser = ChatHelper.getCurrentUser();
        users.remove(currentUser);

        QBChatDialog dialogToCreate = new QBChatDialog();
        dialogToCreate.setName(QbDialogUtils.createChatNameFromUserList(users));
        if (users.size() == 1) {
            dialogToCreate.setType(QBDialogType.PRIVATE);
            dialogToCreate.setPhoto(users.get(0).getCustomData());
        } else {
            dialogToCreate.setType(QBDialogType.GROUP);
        }
        dialogToCreate.setOccupantsIds(new ArrayList<>
                (Arrays.asList(QbDialogUtils.getUserIds(users))));
        return dialogToCreate;
    }

    public static List<QBUser> getAddedUsers(QBChatDialog dialog,
                                             List<QBUser> currentUsers) {
        return getAddedUsers(getQbUsersFromQbDialog(dialog), currentUsers);
    }

    public static List<QBUser> getAddedUsers(List<QBUser> previousUsers, List<QBUser> currentUsers) {
        List<QBUser> addedUsers = new ArrayList<>();
        for (QBUser currentUser : currentUsers) {
            boolean wasInChatBefore = false;
            for (QBUser previousUser : previousUsers) {
                if (currentUser.getId().equals(previousUser.getId())) {
                    wasInChatBefore = true;
                    break;
                }
            }
            if (!wasInChatBefore) {
                addedUsers.add(currentUser);
            }
        }

        QBUser currentUser = ChatHelper.getCurrentUser();
        addedUsers.remove(currentUser);

        return addedUsers;
    }

    public static List<QBUser> getRemovedUsers(QBChatDialog dialog, List<QBUser> currentUsers) {
        return getRemovedUsers(getQbUsersFromQbDialog(dialog), currentUsers);
    }

    public static List<QBUser> getRemovedUsers(List<QBUser> previousUsers, List<QBUser> currentUsers) {
        List<QBUser> removedUsers = new ArrayList<>();
        for (QBUser previousUser : previousUsers) {
            boolean isUserStillPresented = false;
            for (QBUser currentUser : currentUsers) {
                if (previousUser.getId().equals(currentUser.getId())) {
                    isUserStillPresented = true;
                    break;
                }
            }
            if (!isUserStillPresented) {
                removedUsers.add(previousUser);
            }
        }

        QBUser currentUser = ChatHelper.getCurrentUser();
        removedUsers.remove(currentUser);

        return removedUsers;
    }

    public static void logDialogUsers(QBChatDialog qbDialog) {
        Log.v(TAG, "Dialog " + getDialogName(qbDialog));
        logUsersByIds(qbDialog.getOccupants());
    }

    public static void logUsers(List<QBUser> users) {
        for (QBUser user : users) {
            Log.i(TAG, user.getId() + " " + user.getFullName());
        }
    }

    private static void logUsersByIds(List<Integer> users) {
        for (Integer id : users) {
            QBUser user = QbUsersHolder.getInstance().getUserById(id);
            Log.i(TAG, user.getId() + " " + user.getFullName());
        }
    }

    public static Integer getOpponentIdForPrivateDialog(QBChatDialog dialog) {
        Integer opponentId = -1;
        QBUser qbUser = ChatHelper.getCurrentUser();
        if (qbUser == null) {
            return opponentId;
        }

        Integer currentUserId = qbUser.getId();

        for (Integer userId : dialog.getOccupants()) {
            if (!userId.equals(currentUserId)) {
                opponentId = userId;
                break;
            }
        }
        return opponentId;
    }

    public static Integer[] getUserIds(List<QBUser> users) {
        ArrayList<Integer> ids = new ArrayList<>();
        for (QBUser user : users) {
            ids.add(user.getId());
        }
        return ids.toArray(new Integer[ids.size()]);
    }

    public static String createChatNameFromUserList(List<QBUser> users) {
        String chatName = "";
        QBUser currentUser = ChatHelper.getCurrentUser();
        for (QBUser user : users) {
            if (user != null && currentUser != null) {
                if (user.getId().equals(currentUser.getId())) {
                    continue;
                }
            }
            String prefix = chatName.equals("") ? "" : ", ";
            chatName = chatName + prefix + user.getFullName();
        }
        return chatName;
    }

    public static String getDialogName(QBChatDialog dialog) {
        if (dialog.getType().equals(QBDialogType.GROUP)) {
            return dialog.getName();
        } else {
            // It's a private dialog, let's use opponent's name as chat name
            Integer opponentId = getOpponentIdForPrivateDialog(dialog);
            QBUser user = QbUsersHolder.getInstance().getUserById(opponentId);
            if (user != null) {
                return TextUtils.isEmpty(user.getFullName()) ? user.getLogin() : user.getFullName();
            } else {
                return dialog.getName();
            }
        }
    }

    public static List<QBUser> getQbUsersFromQbDialog(QBChatDialog dialog) {
        List<QBUser> previousDialogUsers = new ArrayList<>();
        for (Integer id : dialog.getOccupants()) {
            QBUser user = QbUsersHolder.getInstance().getUserById(id);

            if (user == null) {
                throw new RuntimeException("User from dialog is not in memory. " +
                        "This should never happen, or we are screwed");
            }
            previousDialogUsers.add(user);
        }
        return previousDialogUsers;
    }

    public static void getQbUserById(List<String> ids) {
        /*final List<QBUser> qbUserList = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            QBUsers.getUser(Integer.parseInt(ids.get(i)), new QBEntityCallback<QBUser>() {
                @Override
                public void onSuccess(QBUser qbUser, Bundle bundle) {
                    qbUserList.add(qbUser);
                }

                @Override
                public void onError(QBResponseException e) {
                    Log.d(TAG, "onError: " + e.getMessage());
                }
            });
        }*/
        //createDialog(qbUserList);
    }

    public static void createGroupChatDialog(final HashMap<String, String> map, final String groupName,
                                             final String photoData,
                                             final QBGetGroupID listener) {
        Log.d(TAG, "createGroupChatDialog =>" + map.toString());
        ArrayList<Integer> occupantIdsList = new ArrayList<>();
        String users = "";
        final QBChatMessage chatMessage = new QBChatMessage();
        try {
            Iterator it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                Log.d(TAG, pair.getKey() + " = " + pair.getValue());
                users += pair.getValue() + ", ";
                occupantIdsList.add(Integer.parseInt(String.valueOf(pair.getKey())));
                chatMessage.setRecipientId(Integer.parseInt(String.valueOf(pair.getKey())));
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, e.getMessage());
        }

        QBChatDialog dialog = new QBChatDialog();
        dialog.setName(groupName);
        dialog.setPhoto(photoData);
        dialog.setType(QBDialogType.GROUP);
        dialog.setOccupantsIds(occupantIdsList);
        final String finalUsers = users;

        ChatHelper.getInstance().createDialog(dialog, new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog dialog, Bundle bundle) {
                String message = "";
                if (finalUsers.length() > 2) {
                    message = SharedPreferencesUtil.getQbUser().getFullName()
                            + " created new group with: " +
                            finalUsers.substring(0, finalUsers.length() - 2);
                } else {
                    message = SharedPreferencesUtil.getQbUser().getFullName()
                            + " created new group.";
                }
                dialog.setLastMessage(message);
                dialog.setLastMessageDateSent(System.currentTimeMillis() / 1000);

                QbDialogHolder.getInstance().addDialogToList(dialog, 0);
                listener.getQuickBloxGroupID(dialog, message);
            }

            @Override
            public void onError(QBResponseException e) {
                listener.getError(e);
            }
        });

//        final QBGroupChatManager groupChatManager = QBChatService.getInstance().getGroupChatManager();
//
//        if (groupChatManager != null) {
//
//            groupChatManager.createDialog(dialog, new QBEntityCallback<QBChatDialog>() {
//                @Override
//                public void onSuccess(final QBChatDialog dialog, Bundle args) {
//                    Log.d(TAG, "Created dialog =>" + dialog);
//
//                }
//
//                @Override
//                public void onError(QBResponseException errors) {
//                    listener.getError(errors);
//                }
//            });
//        } else {
//            Log.e(TAG, "NULL login user is null");
//            ChatHelper.getInstance().login(SharedPreferencesUtil.getQbUser(), new QBEntityCallback<Void>() {
//                @Override
//                public void onSuccess(Void aVoid, Bundle bundle) {
//                    createGroupChatDialog(map, groupName, photoData, listener);
//                }
//
//                @Override
//                public void onError(QBResponseException e) {
//                    listener.getError(null);
//                }
//            });
//        }
    }


    public static void updateQbDialogById(final String dialogId, final int type,
                                          final List<Integer> userList,
                                          final String groupName,
                                          final QbUpdateDialogListener listener,
                                          final String photo) {

        // type == 0 change group name
        // type == 1 add user in dialog
        // type == 2 remove user in dialog
        // type == 4 changeGroupImage in dialog


        QBDialogRequestBuilder requestBuilder = new QBDialogRequestBuilder();
        QBChatDialog dialog = new QBChatDialog();
        dialog.setDialogId(dialogId);

        if (type == 0) {
            //chanege group name
            dialog.setName(groupName);

        } else if (type == 1) {
            // add user
            int[] userIds = convertStringArrayIntoIntegerArray(userList);
            if (userIds != null && userIds.length > 0) {
                requestBuilder.addUsers(userIds);
            } else {
                listener.onError();
            }

        } else if (type == 2) {
            // remove user
            int[] userIds = convertStringArrayIntoIntegerArray(userList);
            if (userIds != null && userIds.length > 0) {
                requestBuilder.removeUsers(userIds);

            } else {
                listener.onError();
            }

        } else {
            // change group image
            dialog.setPhoto(photo);
        }

        QBRestChatService.updateGroupChatDialog(dialog, requestBuilder).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog dialog, Bundle bundle) {
                listener.onSuccessResponse(dialog);
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });
//        QBGroupChatManager groupChatManager = QBChatService.getInstance().getGroupChatManager();
//        if (groupChatManager != null) {
//            groupChatManager.updateDialog(dialog, requestBuilder, new QBEntityCallback<QBChatDialog>() {
//                @Override
//                public void onSuccess(QBChatDialog dialog, Bundle args) {
//
//                }
//
//                @Override
//                public void onError(QBResponseException errors) {
//                    listener.onError();
//                }
//            });
//        } else {
//            Log.e(TAG, "NULL login user is null");
//            ChatHelper.getInstance().login(SharedPreferencesUtil.getQbUser(), new QBEntityCallback<Void>() {
//                @Override
//                public void onSuccess(Void aVoid, Bundle bundle) {
//                    updateQbDialogById(dialogId, type, userList,
//                            groupName, listener, photo);
//                }
//
//                @Override
//                public void onError(QBResponseException e) {
//                    listener.onError();
//                }
//            });
//        }

    }


    private static int[] convertStringArrayIntoIntegerArray(List<Integer> user) {
        int[] ids = new int[0];
        if (user != null && !user.isEmpty()) {
            ids = new int[user.size()];
            for (int i = 0; i < user.size(); i++) {
                ids[i] = user.get(i);
            }
        }
        return ids;
    }

    /**
     * @param dialog
     * @return
     */
    public static QBChatMessage createChatNotificationForGroupChatCreation(QBChatDialog dialog, String message) {
        String dialogId = String.valueOf(dialog.getDialogId());
        String roomJid = dialog.getRoomJid();
        String occupantsIds = TextUtils.join(",", dialog.getOccupants());
        String dialogName = dialog.getName();
        String dialogTypeCode = String.valueOf(dialog.getType().ordinal());

        QBChatMessage chatMessage = new QBChatMessage();
        chatMessage.setBody(message);

        // Add notification_type=1 to extra params when you created a group chat
        //
        chatMessage.setProperty("notification_type", "1");
        chatMessage.setDialogId(dialogId);
        chatMessage.setProperty("_id", dialogId);
        if (!TextUtils.isEmpty(roomJid)) {
            chatMessage.setProperty("room_jid", roomJid);
        }
        chatMessage.setProperty("occupants_ids", occupantsIds);
        if (!TextUtils.isEmpty(dialogName)) {
            chatMessage.setProperty("name", dialogName);
        }
        chatMessage.setProperty("type", dialogTypeCode);
        return chatMessage;
    }


    public static void deleteChatDialog(String dialogId,
                                        final QbUpdateDialogListener listener) {
        QBGroupChatManager groupChatManager = QBChatService.getInstance().getGroupChatManager();
        StringifyArrayList<String> dialogIds = new StringifyArrayList<>();
        dialogIds.add(dialogId);
        if (dialogIds != null && !dialogIds.isEmpty()) {
            groupChatManager.deleteDialogs(dialogIds, true, new QBEntityCallback<ArrayList<String>>() {
                @Override
                public void onSuccess(ArrayList<String> dialogIds, Bundle params) {
//                    listener.onSuccessResponse();
                }

                @Override
                public void onError(QBResponseException responseException) {
                    listener.onError();
                }
            });
        } else {
            Log.d(TAG, "delete dialog list is null");
            listener.onError();
        }

    }

    public static void updateDialog(QBChatDialog dialog) {
        QBGroupChatManager groupChatManager = QBChatService.getInstance().getGroupChatManager();
        QBRequestUpdateBuilder requestBuilder = new QBDialogRequestBuilder();
        try {
            groupChatManager.updateDialog(dialog, requestBuilder);
        } catch (QBResponseException e) {
            e.printStackTrace();
        }
    }


    /**
     * Upgraded version code
     */

    public static List<Integer> getOccupantsIdsListFromString(String occupantIds) {
        List<Integer> occupantIdsList = new ArrayList<>();
        String[] occupantIdsArray = occupantIds.split(",");
        for (String occupantId : occupantIdsArray) {
            occupantIdsList.add(Integer.valueOf(occupantId));
        }
        return occupantIdsList;
    }

    public static String getOccupantsIdsStringFromList(Collection<Integer> occupantIdsList) {
        return TextUtils.join(",", occupantIdsList);
    }

    public static QBChatDialog buildPrivateChatDialog(String dialogId, Integer recipientId){
        QBChatDialog chatDialog = DialogUtils.buildPrivateDialog(recipientId);
        chatDialog.setDialogId(dialogId);

        return chatDialog;
    }
}
