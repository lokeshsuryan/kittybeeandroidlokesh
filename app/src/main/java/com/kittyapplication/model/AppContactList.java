package com.kittyapplication.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pintu Riontech on 10/8/16.
 * vaghela.pintu31@gmail.com
 */
public class AppContactList {

    private List<ContactDao> contactList = new ArrayList<>();

    public List<ContactDao> getContactList() {
        return contactList;
    }

    public void setContactList(List<ContactDao> contactList) {
        this.contactList = contactList;
    }
}
