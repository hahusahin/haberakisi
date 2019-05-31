package com.haberinadresi.androidapp.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.haberinadresi.androidapp.R;

import java.util.Locale;
import java.util.Map;

public class UpdateMyColumnists extends IntentService {

    public UpdateMyColumnists() {
        super("to_update_user's_columnist_preferences");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    // FAVORİLERDEKİ YAZARLARIN KEY'LERİNİ HEPSİ KÜÇÜK HARF OLACAK ŞEKİLDE DÜZELTMEK İÇİN
    // (GÜNCELLEMEDEN ÖNCEKİ KULLANICILAR İÇİN SADECE, SONRAKİLERDE ZATEN HEPSİ KÜÇÜK HARF OLARAK KAYDEDİLECEK)
    // SADECE BİR DEFA ÇALIŞTIRILACAK
    @Override
    protected void onHandleIntent(Intent intent) {

        // Get the user's favorite columnists in map format
        SharedPreferences myColumnists = getSharedPreferences(getResources().getString(R.string.columnist_prefs_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = myColumnists.edit();
        Map<String,?> columnistsMap = myColumnists.getAll();
        //If at least one columnist is in favorite
        if(! columnistsMap.isEmpty()){
            for (Map.Entry<String, ?> entry : columnistsMap.entrySet()) {
                String originalKey = entry.getKey();
                String lowercasedKey = entry.getKey().toLowerCase(new Locale("tr", "TR"));
                if(! originalKey.equals(lowercasedKey)){
                    editor.putBoolean(lowercasedKey, true);
                    editor.remove(originalKey);
                }
            }
        }
        editor.apply();
    }
}
