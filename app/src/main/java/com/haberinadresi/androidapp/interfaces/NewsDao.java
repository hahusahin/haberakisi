package com.haberinadresi.androidapp.interfaces;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.haberinadresi.androidapp.models.NewsItem;

import java.util.List;

@Dao
public interface NewsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(NewsItem newsItem);

    @Delete
    void delete(NewsItem newsItem);

    @Query("SELECT * FROM fav_news_table") // ORDER BY updateTime DESC
    LiveData<List<NewsItem>> getAllFavNewsLivedata();

    @Query("DELETE FROM fav_news_table")
    void deleteAllNews();

    @Update
    void update(NewsItem newsItem);

    /*
    @Query("SELECT COUNT(*) FROM fav_news_table WHERE newsUrl = :url")
    int count(String url);
    */

}
