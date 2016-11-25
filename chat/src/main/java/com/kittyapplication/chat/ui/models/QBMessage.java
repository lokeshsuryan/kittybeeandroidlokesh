package com.kittyapplication.chat.ui.models;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.kittyapplication.chat.utils.SharedPreferencesUtil;
import com.kittyapplication.core.utils.ImageUtils;
import com.kittyapplication.core.utils.SharedPrefsHelper;
import com.kittyapplication.core.utils.StringUtils;
import com.quickblox.chat.model.QBChatMessage;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

/**
 * Created by MIT on 11/12/2016.
 */

public class QBMessage {
    private int id;
    private int kittyId;
    private int read;
    private int sent;
    private int delivered;
    private int fail;
    private String senderName;
    private Drawable senderImage;
    private QBChatMessage message;
    private int updatedIndex;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getKittyId() {
        return kittyId;
    }

    public void setKittyId(int kittyId) {
        this.kittyId = kittyId;
    }

    public int getRead() {
        return read;
    }

    public void setRead(int read) {
        this.read = read;
    }

    public int getSent() {
        return sent;
    }

    public void setSent(int sent) {
        this.sent = sent;
    }

    public int getDelivered() {
        return delivered;
    }

    public void setDelivered(int delivered) {
        this.delivered = delivered;
    }

    public int getFail() {
        return fail;
    }

    public void setFail(int fail) {
        this.fail = fail;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public Drawable getSenderImage() {
        return senderImage;
    }

    public void setSenderImage(Drawable senderImage) {
        this.senderImage = senderImage;
    }

    public QBChatMessage getMessage() {
        return message;
    }

    public void setMessage(QBChatMessage message) {
        this.message = message;
    }

    public void setUpdatedIndex(int updatedIndex) {
        this.updatedIndex = updatedIndex;
    }

    public int getUpdatedIndex() {
        return updatedIndex;
    }

    public void generateMessageStatus(ArrayList<Integer> memberIds) {
        generateDeliveredStatus(memberIds);
        generateReadStatus(memberIds);
    }

    private void generateReadStatus(ArrayList<Integer> memberIds) {
        if (message.getReadIds() == null) {
            read = 0;
            return;
        }
        if (memberIds != null
                && memberIds.size() > 0
                && message.getReadIds() != null
                && message.getReadIds().size() > 0) {
            for (Integer readId : message.getReadIds()) {
                for (Integer memberId : memberIds) {
                    if (!memberId.equals(SharedPreferencesUtil.getQbUser().getId())) {
                        sent = 1;
                        if (readId.equals(memberId)) {
                            read = 1;
                        } else {
                            read = 0;
                            break;
                        }
                    }
                }
            }
        }
    }

    private void generateDeliveredStatus(ArrayList<Integer> memberIds) {
        if (message.getDeliveredIds() == null) {
            read = 0;
            return;
        }
        if (memberIds != null && memberIds.size() > 0
                && message.getDeliveredIds() != null
                && message.getDeliveredIds().size() > 0) {
            for (Integer deliveredId : message.getDeliveredIds()) {
                for (Integer memberId : memberIds) {
                    if (!memberId.equals(SharedPreferencesUtil.getQbUser().getId())) {
                        sent = 1;
                        if (deliveredId.equals(memberId)) {
                            delivered = 1;
                        } else {
                            delivered = 0;
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        return StringUtils.toJson(getClass(), this);
    }
}
