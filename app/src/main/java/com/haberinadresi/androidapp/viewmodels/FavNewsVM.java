package com.haberinadresi.androidapp.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.haberinadresi.androidapp.models.NewsItem;
import com.haberinadresi.androidapp.repository.FavNewsRepository;

import java.util.List;

public class FavNewsVM extends AndroidViewModel {

    private FavNewsRepository repository;
    private LiveData<List<NewsItem>> favNews;

    public FavNewsVM(@NonNull Application application) {
        super(application);
        repository = new FavNewsRepository(application);
        favNews = repository.getFavNewsLivedata();
    }

    // Database operation methods

    public void insert(NewsItem newsItem){
        repository.insert(newsItem);
    }

    public void delete(NewsItem newsItem){
        repository.delete(newsItem);
    }

    public LiveData<List<NewsItem>> getFavNews() {
        return favNews;
    }


    ////////////ŞİMDİLİK KULLANILMIYOR//////////
    public void update(NewsItem newsItem){
        repository.update(newsItem);
    }

    public void deleteAll(){
        repository.deleteAll();
    }
}
