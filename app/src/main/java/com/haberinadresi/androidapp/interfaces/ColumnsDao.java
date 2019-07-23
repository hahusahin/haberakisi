package com.haberinadresi.androidapp.interfaces;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.haberinadresi.androidapp.models.Columnist;

import java.util.List;

@Dao
public interface ColumnsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Columnist columnist);

    @Delete
    void delete(Columnist columnist);

    @Query("SELECT * FROM fav_columns_table ORDER BY time DESC")
    LiveData<List<Columnist>> getAllFavColumnsLivedata();

    @Query("DELETE FROM fav_columns_table")
    void deleteAllColumns();

    @Update
    void update(Columnist columnist);

}
