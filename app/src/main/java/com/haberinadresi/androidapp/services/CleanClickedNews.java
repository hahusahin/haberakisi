package com.haberinadresi.androidapp.services;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;

import com.haberinadresi.androidapp.R;
import java.util.Map;

public class CleanClickedNews extends JobIntentService {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {

        clearNews();
        clearColumns();
    }

    // Clear the Clicked News
    private void clearNews(){
        SharedPreferences clickedNews = getSharedPreferences(getResources().getString(R.string.clicked_news_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = clickedNews.edit();
        Map<String,?> clickedItems = clickedNews.getAll();
        if(clickedItems != null){
            for(Map.Entry<String,?> entry : clickedItems.entrySet()){
                // if the clicked news is older than one day then delete it
                if(System.currentTimeMillis() - (Long) entry.getValue() > 86400000L ){ // 24 * 60 * 60 * 1000 = 86400000
                    editor.remove(entry.getKey());
                }
            }
        }
        editor.apply();
    }

    // Clear the Clicked Columns
    private void clearColumns(){
        SharedPreferences clickedColumns = getSharedPreferences(getResources().getString(R.string.clicked_columns_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = clickedColumns.edit();
        Map<String,?> clickedCols = clickedColumns.getAll();
        if(clickedCols != null){
            for(Map.Entry<String,?> entry : clickedCols.entrySet()){
                // if the clicked column is older than one week then delete it
                if(System.currentTimeMillis() - (Long) entry.getValue() > 604800000L ){ // 7* 24 * 60 * 60 * 1000 = 604800000
                    editor.remove(entry.getKey());
                }
            }
        }
        editor.apply();
    }

}

/*
ESKİSİ (ANDROID 9 VE ÜZERİNDE HATAYA SEBEP OLDUĞU İÇİN JOBINTENT SERVICE E ÇEVİRDİM)

package com.haberinadresi.androidapp.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.haberinadresi.androidapp.R;
import java.util.Map;

public class CleanClickedNews extends IntentService {

    public CleanClickedNews() {
        // Used to name the worker thread, important only for debugging.
        super("clicked_news_cleanup");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    // This describes what will happen when service is triggered
    @Override
    protected void onHandleIntent(Intent intent) {

        // Clear the Clicked News
        SharedPreferences clickedNews = getSharedPreferences(getApplicationContext().getResources().getString(R.string.clicked_news_key), Context.MODE_PRIVATE);
        Map<String,?> clickedItems = clickedNews.getAll();
        if(clickedItems != null){
            for(Map.Entry<String,?> entry : clickedItems.entrySet()){
                // if the clicked news is older than one day then delete it
                if(System.currentTimeMillis() - (Long) entry.getValue() > 86400000L ){ // 24 * 60 * 60 * 1000 = 86400000
                    SharedPreferences.Editor editor = clickedNews.edit();
                    editor.remove(entry.getKey());
                    editor.apply();
                }
            }
        }

        // Clear the Clicked Columns
        SharedPreferences clickedColumns = getSharedPreferences(getApplicationContext().getResources().getString(R.string.clicked_columns_key), Context.MODE_PRIVATE);
        Map<String,?> clickedCols = clickedColumns.getAll();
        if(clickedCols != null){
            for(Map.Entry<String,?> entry : clickedCols.entrySet()){
                // if the clicked column is older than one week then delete it
                if(System.currentTimeMillis() - (Long) entry.getValue() > 604800000L ){ // 7* 24 * 60 * 60 * 1000 = 604800000
                    SharedPreferences.Editor editor = clickedColumns.edit();
                    editor.remove(entry.getKey());
                    editor.apply();
                }
            }
        }
    }

}

 */
