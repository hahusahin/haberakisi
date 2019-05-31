package com.haberinadresi.androidapp.activities;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.haberinadresi.androidapp.R;
import com.haberinadresi.androidapp.adapters.ColumnistAdapter;
import com.haberinadresi.androidapp.models.Columnist;
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

        setContentView(R.layout.activity_columnist_select);
        Toolbar toolbar = findViewById(R.id.columnist_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        progressBar = findViewById(R.id.pb_columnist_select);
        progressBar.setVisibility(View.VISIBLE);

        RecyclerView recyclerView = findViewById(R.id.rv_columnist_item);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setHasFixedSize(true);

        columnistAdapter = new ColumnistAdapter(this, new ArrayList<Columnist>());
        recyclerView.setAdapter(columnistAdapter);

        columnistSelectVM = ViewModelProviders.of(this).get(ColumnistSelectVM.class);
        columnistSelectVM.getColumnistLiveData().observe(this, new Observer<List<Columnist>>() {

            @Override
            public void onChanged(@Nullable List<Columnist> columnistList) {

                columnistAdapter.setColumnistList(columnistList);
                progressBar.setVisibility(View.INVISIBLE);
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.search_toolbar, menu);
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
                    if(columnistName.contains(searchText))
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
