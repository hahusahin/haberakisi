package com.haberinadresi.androidapp.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "fav_news_table")
public class NewsItem {

    @PrimaryKey
    @NonNull
    @SerializedName("newsUrl")
    @Expose
    private String newsUrl;

    @SerializedName("source")
    @Expose
    private String source;

    @SerializedName("key")
    @Expose
    private String key;

    @SerializedName("title")
    @Expose
    private String title;

    @SerializedName("imageUrl")
    @Expose
    public String imageUrl;

    @SerializedName("time")
    @Expose
    private long updateTime;

    @SerializedName("summary")
    @Expose
    private String summary;

    @SerializedName("detail")
    @Expose
    private String detail;

    @SerializedName("logoUrl")
    @Expose
    private String logoUrl;

    public NewsItem(String source, String key, String title, @NonNull String newsUrl, String imageUrl,
                    long updateTime, String summary, String detail, String logoUrl) {
        this.source = source;
        this.key = key;
        this.title = title;
        this.newsUrl = newsUrl;
        this.imageUrl = imageUrl;
        this.updateTime = updateTime;
        this.summary = summary;
        this.detail = detail;
        this.logoUrl = logoUrl;
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

    public @NonNull String getNewsUrl() {
        return newsUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public String getSummary() {
        return summary;
    }

    public String getDetail() {
        return detail;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

}
