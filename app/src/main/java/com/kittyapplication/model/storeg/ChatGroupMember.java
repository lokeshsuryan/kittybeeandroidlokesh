package com.kittyapplication.model.storeg;

import com.kittyapplication.core.utils.StringUtils;
import com.kittyapplication.providers.KittyBeeContract;

import java.lang.reflect.Field;

/**
 * Created by MIT on 10/5/2016.
 */
public class ChatGroupMember {
    private int id;
    private String groupId;
    private Integer memberId;
    private String memberName;
    private String memberNumber;
    private byte[] image;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public Integer getMemberId() {
        return memberId;
    }

    public void setMemberId(Integer memberId) {
        this.memberId = memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getMemberNumber() {
        return memberNumber;
    }

    public void setMemberNumber(String memberNumber) {
        this.memberNumber = memberNumber;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return StringUtils.toJson(getClass(), this);
    }

}
