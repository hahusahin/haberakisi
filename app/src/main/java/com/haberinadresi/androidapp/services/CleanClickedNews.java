package com.haberinadresi.androidapp.services;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.haberinadresi.androidapp.R;
import java.util.Map;


////////////// BAZI TELEFONLARDA HATAYA SEBEP OLDUĞUNDAN ŞİMDİLİK KULLANILMIYOR /////////////

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