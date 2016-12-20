package com.kittyapplication.model;

/**
 * Created by Dhaval Soneji Riontech on 2/9/16.
 */
public class ContactDaoWithHeader {
    private int header;
    private ContactDao data;

    public int getHeader() {
        return header;
    }

    public void setHeader(int header) {
        this.header = header;
    }

    public ContactDao getData() {
        return data;
    }

    public void setData(ContactDao data) {
        this.data = data;
    }
}
