package com.haberinadresi.androidapp.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SourceItem {

    @SerializedName("category")
    @Expose
    private String category;

    @SerializedName("sourceName")
    @Expose
    private String sourceName;

    @SerializedName("sourceKey")
    @Expose
    private String sourceKey;

    @SerializedName("key")
    @Expose
    private String key;

    @SerializedName("logo")
    @Expose
    private String logoUrl;

    public SourceItem(String category, String sourceName, String sourceKey, String key, String logoUrl) {
        this.category = category;
        this.sourceName = sourceName;
        this.sourceKey = sourceKey;
        this.key = key;
        this.logoUrl = logoUrl;
    }

    public SourceItem(){
    }

    public String getCategory() {
        return category;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getSourceKey() {
        return sourceKey;
    }

    public String getKey() {
        return key;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

}
