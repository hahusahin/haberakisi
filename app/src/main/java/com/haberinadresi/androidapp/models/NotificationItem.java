package com.haberinadresi.androidapp.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class NotificationItem {

    @SerializedName("source")
    @Expose
    private String source;

    @SerializedName("key")
    @Expose
    private String key;

    @SerializedName("newsUrl")
    @Expose
    private String newsUrl;

    @SerializedName("title")
    @Expose
    private String title;

    @SerializedName("time")
    @Expose
    private long time;


    public NotificationItem(String source, String key, String newsUrl, String title, long time){

        this.source = source;
        this.key = key;
        this.newsUrl = newsUrl;
        this.title = title;
        this.time = time;
    }

    public String getSource() {
        return source;
    }

    public String getKey() {
        return key;
    }

    public String getTitle() {
        return title;
    }

    public String getNewsUrl() {
        return newsUrl;
    }

    public long getTime() {
        return time;
    }


}
