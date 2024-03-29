package com.haberinadresi.androidapp.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

import com.haberinadresi.androidapp.interfaces.NewsDao;
import com.haberinadresi.androidapp.models.NewsItem;

@Database(entities = {NewsItem.class}, version = 1, exportSchema = false)
public abstract class FavNewsDatabase extends RoomDatabase {

    private static FavNewsDatabase instance;

    public abstract NewsDao newsDao();

    public static synchronized FavNewsDatabase getInstance(Context context) {

        if (instance == null){
            instance = Room
                .databaseBuilder(context.getApplicationContext(), FavNewsDatabase.class, "fav_news_database")
                .fallbackToDestructiveMigration()
                //.allowMainThreadQueries()
                .build();
        }

        return instance;
    }

}
