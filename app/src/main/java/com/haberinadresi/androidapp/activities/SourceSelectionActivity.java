package com.haberinadresi.androidapp.activities;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.JobIntentService;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.haberinadresi.androidapp.adapters.SourceAdapter;
import com.haberinadresi.androidapp.models.SourceItem;
import com.haberinadresi.androidapp.R;
import com.haberinadresi.androidapp.services.UpdateMySources;
import com.haberinadresi.androidapp.viewmodels.SourceSelectionVM;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SourceSelectionActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private SourceAdapter sourceAdapter;
    private SourceSelectionVM sourceSelectionVM;
    private SharedPreferences customPreferences;
    private ProgressBar progressBar;
    Intent sourceSelection;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the text style (size) of user's preference
        customPreferences = getSharedPreferences(getResources().getString(R.string.custom_keys), Context.MODE_PRIVATE);
        String fontPreference = customPreferences.getString(getResources().getString(R.string.pref_font_key), "medium");
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

        setContentView(R.layout.activity_source_select);
        Toolbar toolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);


        progressBar = findViewById(R.id.pb_source_select);
        progressBar.setVisibility(View.VISIBLE);

        // Get the category from the clicked intent (from Main Activity)
        sourceSelection = getIntent();
        String categoryKey = sourceSelection.getStringExtra(getResources().getString(R.string.news_category_key));
        String categoryName = sourceSelection.getStringExtra(getResources().getString(R.string.news_category_name));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(categoryName + " Kaynakları");
        }

        RecyclerView recyclerView = findViewById(R.id.rv_source_item);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setHasFixedSize(true);

        sourceAdapter = new SourceAdapter(SourceSelectionActivity.this, new ArrayList<SourceItem>());
        recyclerView.setAdapter(sourceAdapter);

        sourceSelectionVM = ViewModelProviders.of(this).get(SourceSelectionVM.class);
        sourceSelectionVM.getSourcesLiveData(categoryKey).observe(this, new Observer<List<SourceItem>>() {
            @Override
            public void onChanged(@Nullable List<SourceItem> sourceList) {

                // attach the list to the recyclerview adapter and stop progressbar
                sourceAdapter.setSourceList(sourceList);
                progressBar.setVisibility(View.INVISIBLE);

                // Start the service that updates the user's source preference values that are in SharedPreferences (image url's etc..)
                Intent updateMySources = new Intent(SourceSelectionActivity.this, UpdateMySources.class);
                Gson gson = new Gson();
                String json = gson.toJson(sourceList);
                updateMySources.putExtra(getResources().getString(R.string.source_list), json);
                JobIntentService.enqueueWork(SourceSelectionActivity.this, UpdateMySources.class, 101, updateMySources);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // KAYNAKLARDA ARAMA YAPMAK İÇİN
        getMenuInflater().inflate(R.menu.search_toolbar, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint(getResources().getString(R.string.search_sources));
        searchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home){
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    // Methods for making Search (OnQueryTextListener)
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String searchText) {

        searchText = searchText.toLowerCase(new Locale("tr", "TR"));
        List<SourceItem> newList = new ArrayList<>();

        // Get the category from the intent
        String category = sourceSelection.getStringExtra(getResources().getString(R.string.news_category_key));

        // Get the list of sources from viewmodel to make comparisons
        if (sourceSelectionVM.getSourcesLiveData(category).getValue() != null ){

            List<SourceItem> allSources = sourceSelectionVM.getSourcesLiveData(category).getValue();

            if (allSources != null) {
                for (SourceItem source : allSources){

                    String sourceName = source.getSourceName().toLowerCase(new Locale("tr", "TR"));
                    if(sourceName.contains(searchText))
                        newList.add(source);
                }
            }

        }

        sourceAdapter.setSourceList(newList);
        return true;
    }

    @Override
    public void onResume() {
        // If intro is not seen by the user, then show it (just one time)
        if(! customPreferences.getBoolean(getResources().getString(R.string.is_intro_displayed), false)){
            Intent intent = new Intent(SourceSelectionActivity.this, IntroActivity.class);
            startActivity(intent);
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
