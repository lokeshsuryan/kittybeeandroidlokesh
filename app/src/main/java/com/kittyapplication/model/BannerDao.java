package com.kittyapplication.model;

/**
 * Created by Pintu Riontech on 11/8/16.
 * vaghela.pintu31@gmail.com
 */
public class BannerDao {
    private String id;

    private String title;

    private String slug;

    private String thamb;

    private String url;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getThamb() {
        return thamb;
    }

    public void setThamb(String thamb) {
        this.thamb = thamb;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
