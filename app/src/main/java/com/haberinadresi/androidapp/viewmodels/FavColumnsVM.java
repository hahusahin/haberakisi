package com.haberinadresi.androidapp.viewmodels;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.annotation.NonNull;

import com.haberinadresi.androidapp.models.Columnist;
import com.haberinadresi.androidapp.repository.FavColumnsRepository;

import java.util.List;

public class FavColumnsVM extends AndroidViewModel {

    private FavColumnsRepository repository;
    private LiveData<List<Columnist>> favColumns;

    public FavColumnsVM(@NonNull Application application) {
        super(application);
        repository = new FavColumnsRepository(application);
        favColumns = repository.getFavColumnsLivedata();
    }

    // Database operation methods

    public void insert(Columnist columnist){
        repository.insert(columnist);
    }

    public void delete(Columnist columnist){
        repository.delete(columnist);
    }

    public LiveData<List<Columnist>> getFavColumns() {
        return favColumns;
    }


    ////////////ŞİMDİLİK KULLANILMIYOR//////////
    public void update(Columnist columnist){
        repository.update(columnist);
    }

    public void deleteAll(){
        repository.deleteAll();
    }
}
