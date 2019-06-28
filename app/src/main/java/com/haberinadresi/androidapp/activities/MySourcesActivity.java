package com.haberinadresi.androidapp.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.haberinadresi.androidapp.R;
import com.haberinadresi.androidapp.adapters.MySourcesAdapter;
import com.haberinadresi.androidapp.models.SourceItem;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class MySourcesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the text style (size) of user's preference
        SharedPreferences customPrefs = getSharedPreferences(getResources().getString(R.string.custom_keys), MODE_PRIVATE);
        String fontPreference = customPrefs.getString(getResources().getString(R.string.pref_font_key), "medium");
        if(fontPreference != null){
            switch (fontPreference){
                case "small":
                    setTheme(R.style.FontStyle_Small);
                    break;
                case "medium":
                    setTheme(R.style.FontStyle_Medium);
                    break;
                case "large":
                    setTheme(R.style.FontStyle_Large);
                    break;
                case "xlarge":
                    setTheme(R.style.FontStyle_XLarge);
                    break;
                default:
                    setTheme(R.style.FontStyle_Medium);
            }
        } else {
            setTheme(R.style.FontStyle_Medium);
        }

        setContentView(R.layout.activity_my_sources);

        Toolbar toolbar = findViewById(R.id.my_sources_toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Title Textviews for sources
        TextView gundemTextView = findViewById(R.id.tv_my_sources_gundem);
        TextView sporTextView = findViewById(R.id.tv_my_sources_spor);
        TextView teknolojiTextView = findViewById(R.id.tv_my_sources_teknoloji);
        TextView kultursanatTextView = findViewById(R.id.tv_my_sources_kultursanat);
        TextView saglikTextView = findViewById(R.id.tv_my_sources_saglik);
        TextView magazinTextView = findViewById(R.id.tv_my_sources_magazin);

        // get the users preferences
        SharedPreferences mySources = getSharedPreferences(getResources().getString(R.string.source_prefs_key), Context.MODE_PRIVATE);

        List<SourceItem> myGundemSources = new ArrayList<>();
        List<SourceItem> mySporSources = new ArrayList<>();
        List<SourceItem> myTeknolojiSources = new ArrayList<>();
        List<SourceItem> myKulturSanatSources = new ArrayList<>();
        List<SourceItem> mySaglikSources = new ArrayList<>();
        List<SourceItem> myMagazinSources = new ArrayList<>();

        // get all preferences and iterate over them
        Map<String,?> sources = mySources.getAll();
        if(sources != null){
            for(Map.Entry<String,?> entry : sources.entrySet()){
                String json = (String) entry.getValue();
                SourceItem sourceItem = convertString(json);
                if(entry.getKey().contains("_gundem") || entry.getKey().contains("_ekonomi")){
                    myGundemSources.add(sourceItem);
                } else if(entry.getKey().contains("_spor")){
                    mySporSources.add(sourceItem);
                } else if(entry.getKey().contains("_teknoloji")){
                    myTeknolojiSources.add(sourceItem);
                } else if(entry.getKey().contains("_magazin")){
                    myMagazinSources.add(sourceItem);
                } else if(entry.getKey().contains("_kultursanat")){
                    myKulturSanatSources.add(sourceItem);
                } else if(entry.getKey().contains("_saglik")){
                    mySaglikSources.add(sourceItem);
                }
            }
        }

        // Initialize recyclerview and attach the Gundem + Ekonomi sources to adapter
        if (! myGundemSources.isEmpty()){
            RecyclerView rvGundem = findViewById(R.id.rv_my_gundem_sources);
            rvGundem.setLayoutManager(new GridLayoutManager(this,3));
            //rvGundem.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            rvGundem.setHasFixedSize(true);
            rvGundem.setNestedScrollingEnabled(false);

            gundemTextView.setVisibility(View.VISIBLE);
            // sort by name before attaching
            Collections.sort(myGundemSources, new Comparator<SourceItem>() {
                @Override
                public int compare(SourceItem source1, SourceItem source2) {
                    return source1.getKey().compareTo(source2.getKey());
                }
            });
            MySourcesAdapter gundemAdapter = new MySourcesAdapter(MySourcesActivity.this, myGundemSources);
            rvGundem.setAdapter(gundemAdapter);
        } else {
            gundemTextView.setVisibility(View.GONE);
        }

        // Initialize recyclerview and attach the Spor sources to adapter
        if (! mySporSources.isEmpty()){
            RecyclerView rvSpor = findViewById(R.id.rv_my_spor_sources);
            rvSpor.setLayoutManager(new GridLayoutManager(this,3));
            rvSpor.setHasFixedSize(true);
            rvSpor.setNestedScrollingEnabled(false);

            sporTextView.setVisibility(View.VISIBLE);
            // sort by name before attaching
            Collections.sort(mySporSources, new Comparator<SourceItem>() {
                @Override
                public int compare(SourceItem source1, SourceItem source2) {
                    return source1.getKey().compareTo(source2.getKey());
                }
            });
            MySourcesAdapter sporAdapter = new MySourcesAdapter(MySourcesActivity.this, mySporSources);
            rvSpor.setAdapter(sporAdapter);
        }else {
            sporTextView.setVisibility(View.GONE);
        }
        // Initialize recyclerview and attach the Teknoloji sources to adapter
        if (! myTeknolojiSources.isEmpty()){
            RecyclerView rvTeknoloji = findViewById(R.id.rv_my_teknoloji_sources);
            rvTeknoloji.setLayoutManager(new GridLayoutManager(this,3));
            rvTeknoloji.setHasFixedSize(true);
            rvTeknoloji.setNestedScrollingEnabled(false);

            teknolojiTextView.setVisibility(View.VISIBLE);
            // sort by name before attaching
            Collections.sort(myTeknolojiSources, new Comparator<SourceItem>() {
                @Override
                public int compare(SourceItem source1, SourceItem source2) {
                    return source1.getKey().compareTo(source2.getKey());
                }
            });
            MySourcesAdapter teknolojiAdapter = new MySourcesAdapter(MySourcesActivity.this, myTeknolojiSources);
            rvTeknoloji.setAdapter(teknolojiAdapter);
        }else {
            teknolojiTextView.setVisibility(View.GONE);
        }
        // Initialize recyclerview and attach the KulturSanat sources to adapter
        if (! myKulturSanatSources.isEmpty()){
            RecyclerView rvKulturSanat = findViewById(R.id.rv_my_kultursanat_sources);
            rvKulturSanat.setLayoutManager(new GridLayoutManager(this,3));
            rvKulturSanat.setHasFixedSize(true);
            rvKulturSanat.setNestedScrollingEnabled(false);

            kultursanatTextView.setVisibility(View.VISIBLE);
            // sort by name before attaching
            Collections.sort(myKulturSanatSources, new Comparator<SourceItem>() {
                @Override
                public int compare(SourceItem source1, SourceItem source2) {
                    return source1.getKey().compareTo(source2.getKey());
                }
            });
            MySourcesAdapter kulturSanatAdapter = new MySourcesAdapter(MySourcesActivity.this, myKulturSanatSources);
            rvKulturSanat.setAdapter(kulturSanatAdapter);
        } else {
            kultursanatTextView.setVisibility(View.GONE);
        }
        // Initialize recyclerview and attach the Sağlık sources to adapter
        if (! mySaglikSources.isEmpty()){
            RecyclerView rvSaglik = findViewById(R.id.rv_my_saglik_sources);
            rvSaglik.setLayoutManager(new GridLayoutManager(this,3));
            rvSaglik.setHasFixedSize(true);
            rvSaglik.setNestedScrollingEnabled(false);

            saglikTextView.setVisibility(View.VISIBLE);
            // sort by name before attaching
            Collections.sort(mySaglikSources, new Comparator<SourceItem>() {
                @Override
                public int compare(SourceItem source1, SourceItem source2) {
                    return source1.getKey().compareTo(source2.getKey());
                }
            });
            MySourcesAdapter saglikAdapter = new MySourcesAdapter(MySourcesActivity.this, mySaglikSources);
            rvSaglik.setAdapter(saglikAdapter);
        }else {
            saglikTextView.setVisibility(View.GONE);
        }
        // Initialize recyclerview and attach the Magazin sources to adapter
        if (! myMagazinSources.isEmpty()){
            RecyclerView rvMagazin = findViewById(R.id.rv_my_magazin_sources);
            rvMagazin.setLayoutManager(new GridLayoutManager(this,3));
            rvMagazin.setHasFixedSize(true);
            rvMagazin.setNestedScrollingEnabled(false);

            magazinTextView.setVisibility(View.VISIBLE);
            // sort by name before attaching
            Collections.sort(myMagazinSources, new Comparator<SourceItem>() {
                @Override
                public int compare(SourceItem source1, SourceItem source2) {
                    return source1.getKey().compareTo(source2.getKey());
                }
            });
            MySourcesAdapter magazinAdapter = new MySourcesAdapter(MySourcesActivity.this, myMagazinSources);
            rvMagazin.setAdapter(magazinAdapter);
        }else {
            magazinTextView.setVisibility(View.GONE);
        }
    }
    //convert the data stored in sharedpreferences as string to source model
    public SourceItem convertString(String json) {
        Gson gson = new Gson();
        Type type = new TypeToken<SourceItem>() {}.getType();
        return gson.fromJson(json, type);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home){
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

}