package com.haberinadresi.androidapp.interfaces;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

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
