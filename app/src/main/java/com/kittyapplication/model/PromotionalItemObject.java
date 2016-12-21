package com.kittyapplication.model;

/**
 * Created by Iball on 12/21/2016.
 */

public class PromotionalItemObject
{
    private String name;
    private int photo;

    public PromotionalItemObject(String name, int photo) {
        this.name = name;
        this.photo = photo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPhoto() {
        return photo;
    }

    public void setPhoto(int photo) {
        this.photo = photo;
    }
}