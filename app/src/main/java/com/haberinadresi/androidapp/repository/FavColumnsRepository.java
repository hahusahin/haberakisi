package com.haberinadresi.androidapp.repository;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.os.AsyncTask;

import com.haberinadresi.androidapp.database.FavColumnsDatabase;
import com.haberinadresi.androidapp.interfaces.ColumnsDao;
import com.haberinadresi.androidapp.models.Columnist;

import java.util.List;

public class FavColumnsRepository {

    private ColumnsDao columnsDao;
    private LiveData<List<Columnist>> favColumnsLivedata;

    public FavColumnsRepository (Context context){
        FavColumnsDatabase database = FavColumnsDatabase.getInstance(context);
        columnsDao = database.columnsDao();
        favColumnsLivedata = columnsDao.getAllFavColumnsLivedata();
    }

    public void insert(Columnist columnist){
        new InsertColumnAsyncTask(columnsDao).execute(columnist);
    }

    public void delete(Columnist columnist){
        new DeleteColumnAsyncTask(columnsDao).execute(columnist);
    }

    public LiveData<List<Columnist>> getFavColumnsLivedata() {

        return favColumnsLivedata;
    }

    // Since android doesn't allow the database operations in main thread, asynctasks are defined

    private static class InsertColumnAsyncTask extends AsyncTask <Columnist, Void, Void> {

        private ColumnsDao columnsDao;

        private InsertColumnAsyncTask (ColumnsDao columnsDao){
            this.columnsDao = columnsDao;
        }

        @Override
        protected Void doInBackground(Columnist... columnists) {
            columnsDao.insert(columnists[0]);
            return null;
        }
    }

    private static class DeleteColumnAsyncTask extends AsyncTask <Columnist, Void, Void> {

        private ColumnsDao columnsDao;

        private DeleteColumnAsyncTask (ColumnsDao columnsDao){
            this.columnsDao = columnsDao;
        }

        @Override
        protected Void doInBackground(Columnist... columnists) {
            columnsDao.delete(columnists[0]);
            return null;
        }
    }

    //////////////////////////ŞİMDİLİK KULLANILMIYOR//////////////////////////////////////

    public void update(Columnist columnist){
        new UpdateColumnAsyncTask(columnsDao).execute(columnist);
    }

    public void deleteAll(){
        new DeleteAllColumnsAsyncTask(columnsDao).execute();
    }

    private static class UpdateColumnAsyncTask extends AsyncTask <Columnist, Void, Void> {

        private ColumnsDao columnsDao;

        private UpdateColumnAsyncTask (ColumnsDao columnsDao){
            this.columnsDao = columnsDao;
        }

        @Override
        protected Void doInBackground(Columnist... columnists) {
            columnsDao.update(columnists[0]);
            return null;
        }
    }

    private static class DeleteAllColumnsAsyncTask extends AsyncTask <Void, Void, Void> {

        private ColumnsDao columnsDao;

        private DeleteAllColumnsAsyncTask (ColumnsDao columnsDao){
            this.columnsDao = columnsDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            columnsDao.deleteAllColumns();
            return null;
        }
    }

}
