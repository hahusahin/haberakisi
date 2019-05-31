package com.haberinadresi.androidapp.services;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.haberinadresi.androidapp.R;
import com.haberinadresi.androidapp.models.SourceItem;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UpdateMySources extends JobIntentService {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    // İKONU DEĞİŞEN HABER KAYNAKLARININ İKON LİNKİNİ KİŞİNİN FAVORİLERİNDE DÜZELTMEK VE
    // SONRADAN SİLDİĞİM KAYNAKLARI KİŞİNİN FAVORİ LİSTESİNDEN SİLMEK İÇİN
    // SOURCE SELECTION ACTİVİTY AÇILDIĞINDA ÇALIŞIYOR

    @Override
    protected void onHandleWork(@NonNull Intent intent) {

        // Get user's source preferences
        SharedPreferences mySources = getSharedPreferences(getResources().getString(R.string.source_prefs_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySources.edit();
        // Get the source list from intent as a String
        Gson gson = new Gson();
        String json = intent.getStringExtra(getResources().getString(R.string.source_list));
        if (json != null) {
            // Convert the source list from String to ArrayList
            Type type = new TypeToken<ArrayList<SourceItem>>() {}.getType();
            List<SourceItem> sourceList = gson.fromJson(json, type);
            // Iterate over the whole source list
            if(sourceList != null){
                for (SourceItem sourceItem : sourceList){
                    // If this source is in users' preferences, than replace it with the new one (update the image resource file etc...)
                    if(mySources.contains(sourceItem.getKey())) {
                        editor.putString(sourceItem.getKey(), gson.toJson(sourceItem));
                    }
                }
            }
        }

        // BİR SONRAKİ GÜNCELLEMEDE SİL
        // Delete the sources that are added by the user but later on removed by the app (yeniasır gibi)
        ArrayList<String> deletedSources = new ArrayList<>(Arrays.asList("yeniasir_gundem", "goal_spor"));
        for (String item : deletedSources){
            if(mySources.contains(item)){
                editor.remove(item);
            }
        }

        editor.apply();

    }

}
