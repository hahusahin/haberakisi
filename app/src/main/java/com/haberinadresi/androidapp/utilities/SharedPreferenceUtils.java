package com.haberinadresi.androidapp.utilities;

import android.content.Context;
import android.content.SharedPreferences;

import com.haberinadresi.androidapp.R;

import java.util.Map;

public class SharedPreferenceUtils {


    // Clear the Clicked Columns
    public static void clearColumns(Context context){
        SharedPreferences clickedColumns = context.getSharedPreferences(context.getResources().getString(R.string.clicked_columns_key), Context.MODE_PRIVATE);
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

    /* ŞİMDİLİK BU DETAYA GEREK YOK, TOPLU SİLİYORUM
    // Clear the Clicked News
    public static void clearNews(Context context){
        SharedPreferences clickedNews = context.getSharedPreferences(context.getResources().getString(R.string.clicked_news_key), Context.MODE_PRIVATE);
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
     */
}
