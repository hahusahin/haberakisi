package com.haberinadresi.androidapp.repository;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.os.AsyncTask;

import com.haberinadresi.androidapp.database.FavNewsDatabase;
import com.haberinadresi.androidapp.interfaces.NewsDao;
import com.haberinadresi.androidapp.models.NewsItem;

import java.util.List;

public class FavNewsRepository {

    private NewsDao newsDao;
    private LiveData<List<NewsItem>> favNewsLivedata;

    public FavNewsRepository(Context context){
        FavNewsDatabase database = FavNewsDatabase.getInstance(context);
        newsDao = database.newsDao();
        favNewsLivedata = newsDao.getAllFavNewsLivedata();
    }

    public void insert(NewsItem newsItem){
        new InsertNewsAsyncTask(newsDao).execute(newsItem);
    }

    public void delete(NewsItem newsItem){
        new DeleteNewsAsyncTask(newsDao).execute(newsItem);
    }

    public LiveData<List<NewsItem>> getFavNewsLivedata() {

        return favNewsLivedata;
    }

    // Since android doesn't allow the database operations in main thread, asynctasks are defined

    private static class InsertNewsAsyncTask extends AsyncTask <NewsItem, Void, Void> {

        private NewsDao newsDao;

        private InsertNewsAsyncTask (NewsDao newsDao){
            this.newsDao = newsDao;
        }

        @Override
        protected Void doInBackground(NewsItem... news) {
            newsDao.insert(news[0]);
            return null;
        }
    }

    private static class DeleteNewsAsyncTask extends AsyncTask <NewsItem, Void, Void> {

        private NewsDao newsDao;

        private DeleteNewsAsyncTask (NewsDao newsDao){
            this.newsDao = newsDao;
        }

        @Override
        protected Void doInBackground(NewsItem... news) {
            newsDao.delete(news[0]);
            return null;
        }
    }

    //////////////////////////ŞİMDİLİK KULLANILMIYOR//////////////////////////////////////

    public void update(NewsItem newsItem){
        new UpdateNewsAsyncTask(newsDao).execute(newsItem);
    }

    public void deleteAll(){
        new DeleteAllNewsAsyncTask(newsDao).execute();
    }


    private static class UpdateNewsAsyncTask extends AsyncTask <NewsItem, Void, Void> {

        private NewsDao newsDao;

        private UpdateNewsAsyncTask (NewsDao newsDao){
            this.newsDao = newsDao;
        }

        @Override
        protected Void doInBackground(NewsItem... news) {
            newsDao.update(news[0]);
            return null;
        }
    }

    private static class DeleteAllNewsAsyncTask extends AsyncTask <Void, Void, Void> {

        private NewsDao newsDao;

        private DeleteAllNewsAsyncTask (NewsDao newsDao){
            this.newsDao = newsDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            newsDao.deleteAllNews();
            return null;
        }
    }

}
