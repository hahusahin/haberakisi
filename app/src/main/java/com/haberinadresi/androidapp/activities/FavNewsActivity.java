package com.haberinadresi.androidapp.activities;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.haberinadresi.androidapp.R;
import com.haberinadresi.androidapp.adapters.FavNewsAdapter;
import com.haberinadresi.androidapp.models.NewsItem;
import com.haberinadresi.androidapp.viewmodels.FavNewsVM;

import java.util.Collections;
import java.util.List;

public class FavNewsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FavNewsAdapter newsAdapter;
    private ProgressBar progressBar;
    private TextView emptyFavNewsWarning;
    private AdView bannerAdView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_news);

        final Toolbar toolbar = findViewById(R.id.favorite_news_toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //Reklam
        bannerAdView = findViewById(R.id.bannerAdView);
        AdRequest adRequest = new AdRequest.Builder().build();
        bannerAdView.loadAd(adRequest);

        // Don't show empty favorite dialog at the beginning
        emptyFavNewsWarning = findViewById(R.id.tv_empty_fav_dialog);
        // If this bookmark is added in XML, it gives error on low API level (19), so added in here
        emptyFavNewsWarning.setCompoundDrawablesWithIntrinsicBounds(null,
                AppCompatResources.getDrawable(this, R.drawable.ic_saved_news_big),null,null );
        emptyFavNewsWarning.setText(getResources().getString(R.string.empty_fav_news_database));
        emptyFavNewsWarning.setVisibility(View.GONE);

        recyclerView = findViewById(R.id.rv_favorite_news);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setHasFixedSize(true);

        // The ProgressBar that will indicate to the user that news are loading
        progressBar = findViewById(R.id.pb_favorite_news);
        progressBar.setVisibility(View.VISIBLE);

        FavNewsVM favNewsVM = ViewModelProviders.of(this).get(FavNewsVM.class);
        favNewsVM.getFavNews().observe(this, new Observer<List<NewsItem>>() {
            @Override
            public void onChanged(@Nullable List<NewsItem> newsList) {
                //To show recently added news at the top (reverse list)
                Collections.reverse(newsList);
                newsAdapter = new FavNewsAdapter(FavNewsActivity.this, newsList);
                recyclerView.setAdapter(newsAdapter);
                // Hide ProgressBar when news are loaded
                progressBar.setVisibility(View.INVISIBLE);
                // If the user didn't add any news to favorite show dialog
                if(newsAdapter.getItemCount() < 1){
                    emptyFavNewsWarning.setVisibility(View.VISIBLE);
                } else {
                    emptyFavNewsWarning.setVisibility(View.GONE);
                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home){
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        if (bannerAdView != null) {
            bannerAdView.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (bannerAdView != null) {
            bannerAdView.resume();
        }
    }

    @Override
    public void onDestroy() {
        if (bannerAdView != null) {
            bannerAdView.destroy();
        }
        super.onDestroy();
    }

}
