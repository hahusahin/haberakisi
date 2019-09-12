package com.haberinadresi.androidapp.activities;

import androidx.core.app.JobIntentService;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.haberinadresi.androidapp.R;
import com.haberinadresi.androidapp.adapters.ColumnistAdapter;
import com.haberinadresi.androidapp.models.Columnist;
import com.haberinadresi.androidapp.services.UpdateMyColumnists;
import com.haberinadresi.androidapp.viewmodels.ColumnistSelectVM;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ColumnistSelectActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private ColumnistAdapter columnistAdapter;
    private ColumnistSelectVM columnistSelectVM;
    private ProgressBar progressBar;

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

        setContentView(R.layout.activity_columnist_select);
        Toolbar toolbar = findViewById(R.id.columnist_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        progressBar = findViewById(R.id.pb_columnist_select);
        progressBar.setVisibility(View.VISIBLE);

        RecyclerView recyclerView = findViewById(R.id.rv_columnist_item);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        recyclerView.setHasFixedSize(true);

        columnistAdapter = new ColumnistAdapter(this, new ArrayList<Columnist>());
        recyclerView.setAdapter(columnistAdapter);

        columnistSelectVM = ViewModelProviders.of(this).get(ColumnistSelectVM.class);
        columnistSelectVM.getColumnistLiveData().observe(this, new Observer<List<Columnist>>() {

            @Override
            public void onChanged(@Nullable List<Columnist> columnistList) {

                columnistAdapter.setColumnistList(columnistList);
                progressBar.setVisibility(View.INVISIBLE);

                // Start the service that deletes the outdated columnists from preferences
                Intent updateMyColumnists = new Intent(ColumnistSelectActivity.this, UpdateMyColumnists.class);
                Gson gson = new Gson();
                String json = gson.toJson(columnistList);
                updateMyColumnists.putExtra(getResources().getString(R.string.columnists_list), json);
                JobIntentService.enqueueWork(ColumnistSelectActivity.this, UpdateMyColumnists.class,
                        201, updateMyColumnists);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint(getResources().getString(R.string.search_columnist));
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
        List<Columnist> newList = new ArrayList<>();

        // Get the list of sources from viewmodel to make comparisons
        if (columnistSelectVM.getColumnistLiveData().getValue() != null ){

            List<Columnist> allColumnists = columnistSelectVM.getColumnistLiveData().getValue();

            if (allColumnists != null) {
                for (Columnist columnist : allColumnists){

                    String columnistName = columnist.getName().toLowerCase(new Locale("tr", "TR"));
                    String sourceName = columnist.getSource().toLowerCase(new Locale("tr", "TR"));
                    // If the string that user typed exists in the Columnist name OR in the source name then show that columnist
                    if(columnistName.contains(searchText) || sourceName.contains(searchText))
                        newList.add(columnist);
                }
            }

        }

        columnistAdapter.setColumnistList(newList);
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
