package com.haberinadresi.androidapp.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.haberinadresi.androidapp.interfaces.ColumnsDao;
import com.haberinadresi.androidapp.models.Columnist;

@Database(entities = {Columnist.class}, version = 1, exportSchema = false)
public abstract class FavColumnsDatabase extends RoomDatabase {

    private static FavColumnsDatabase instance;

    public abstract ColumnsDao columnsDao();

    public static synchronized FavColumnsDatabase getInstance(Context context) {

        if (instance == null){
            instance = Room
                .databaseBuilder(context.getApplicationContext(), FavColumnsDatabase.class, "fav_columns_database")
                .fallbackToDestructiveMigration()
                //.allowMainThreadQueries()
                .build();
        }

        return instance;
    }

}
