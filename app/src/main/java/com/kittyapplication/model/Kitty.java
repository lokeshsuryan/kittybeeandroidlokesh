package com.kittyapplication.model;

import android.view.View;

import com.kittyapplication.core.utils.StringUtils;
import com.quickblox.chat.model.QBDialog;

/**
 * Created by Scorpion on 24-08-2016.
 */
public class Kitty {
    private int id;
    private String dialogId;
    private boolean isChecked;
    private boolean isAnimated;
    private QBDialog qbDialog;
    private ChatData group;
    private int selectedItemPosition;
    private View selectedItemView;

    public void setGroup(ChatData group) {
        this.group = group;
    }

    public void setQbDialog(QBDialog qbDialog) {
        this.qbDialog = qbDialog;
    }

    public ChatData getGroup() {
        return group;
    }

    public QBDialog getQbDialog() {
        return qbDialog;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDialogId(String dialogId) {
        this.dialogId = dialogId;
    }

    public String getDialogId() {
        return dialogId;
    }

    @Override
    public String toString() {
        return StringUtils.toJson(this.getClass(), this);
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public boolean isAnimated() {
        return isAnimated;
    }

    public void setAnimated(boolean animated) {
        isAnimated = animated;
    }

    public int getSelectedItemPosition() {
        return selectedItemPosition;
    }

    public void setSelectedItemPosition(int selectedItemPosition) {
        this.selectedItemPosition = selectedItemPosition;
    }

    public void setSelectedItemView(View selectedItemView) {
        this.selectedItemView = selectedItemView;
    }

    public View getSelectedItemView() {
        return selectedItemView;
    }
}