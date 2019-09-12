package com.haberinadresi.androidapp.services;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.haberinadresi.androidapp.R;
import com.haberinadresi.androidapp.models.Columnist;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UpdateMyColumnists extends JobIntentService {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    // ESKİDEN LİSTEDE OLUP FAVORİLERE EKLENMİŞ
    // AMA DAHA SONRA GÜNCEL YAZAR LİSTESİNDEN ÇIKARILAN YAZARLARIN
    // FAVORİ LİSTESİNDEN DE SİLİNMESİ

    @Override
    protected void onHandleWork(@NonNull Intent intent) {

        // Get the user's favorite columnists AND columnist sources in map format
        SharedPreferences myColumnists = getSharedPreferences(getResources().getString(R.string.columnist_prefs_key), Context.MODE_PRIVATE);
        Locale turkish = new Locale("tr", "TR");

        Map<String,?> columnistsMap = myColumnists.getAll();
        //If at least one columnist is in favorite
        if(! columnistsMap.isEmpty()) {
            // Get the most recent columnist list from intent as String
            Gson gson = new Gson();
            String json = intent.getStringExtra(getResources().getString(R.string.columnists_list));
            if (json != null) {
                // Convert the columnists list from String to ArrayList
                Type type = new TypeToken<ArrayList<Columnist>>() {}.getType();
                List<Columnist> columnistList = gson.fromJson(json, type);
                // Iterate over the whole list
                if(columnistList != null) {
                    for (Map.Entry<String, ?> entry : columnistsMap.entrySet()) {
                        boolean columnistExists = false;
                        for (Columnist columnist : columnistList){
                            if (columnist.getKey().toLowerCase(turkish).equals(entry.getKey())){
                                columnistExists = true;
                                break;
                            }
                        }
                        // If a columnist in user's preference couldn't be found in the most recent list (outdated)
                        if (! columnistExists){
                            // Delete this old columnist from preferences
                            myColumnists.edit().remove(entry.getKey()).apply();
                        }
                    }
                }
            }
        }

    }
}