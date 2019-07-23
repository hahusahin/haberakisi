package com.haberinadresi.androidapp.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "fav_columns_table")
public class Columnist {

    @PrimaryKey
    @NonNull
    @SerializedName("link")
    @Expose
    private String columnUrl;

    @SerializedName("source")
    @Expose
    private String source;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("key")
    @Expose
    private String key;

    @SerializedName("title")
    @Expose
    private String title;

    @SerializedName("imageUrl")
    @Expose
    public String imageUrl;

    //@SerializedName("day")
    @SerializedName(value="day", alternate="sourceKey") //ŞU AN DAY KULLANILMIYOR, İLERLEYEN ZAMANLARDA YERİNE SOURCE KULLANMAK İÇİN
    @Expose
    private String day;

    @SerializedName("timeForSort")
    @Expose
    private long time;

    public Columnist(String source, String name, String key, String title, @NonNull String columnUrl, String imageUrl, String day, long time) {
        this.source = source;
        this.name = name;
        this.key = key;
        this.title = title;
        this.columnUrl = columnUrl;
        this.imageUrl = imageUrl;
        this.day = day;
        this.time = time;
    }


    public String getSource() {
        return source;
    }

    public String getName() {
        return name;
    }

    public String getKey() {
        return key;
    }

    public String getTitle() {
        return title;
    }

    @NonNull
    public String getColumnUrl() {
        return columnUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDay() {
        return day;
    } // ŞİMDİLİK KULLANILMIYOR

    public long getTime() {
        return time;
    }
}
