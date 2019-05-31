package com.haberinadresi.androidapp.activities;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.haberinadresi.androidapp.R;
import com.haberinadresi.androidapp.adapters.NewsAdapterWithAds;
import com.haberinadresi.androidapp.models.NewsItem;
import com.haberinadresi.androidapp.viewmodels.OneSourceNewsVM;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

//This activity loads news from only the selected source
public class OneSrcNewsActivity extends AppCompatActivity {

    private NewsAdapterWithAds newsAdapter;
    private ProgressBar progressBar;
    Intent oneSourceIntent;
    private List<Object> recyclerViewItems = new ArrayList<>(); // List of banner ads and NewsItems that populate the RecyclerView.
    public static final int ITEMS_PER_AD = 12; // A banner ad is placed in every 12th position in the RecyclerView.

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_src_news);

        Toolbar toolbar = findViewById(R.id.one_source_toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        //initialize recyclerview and viewmodel
        RecyclerView recyclerView = findViewById(R.id.rv_one_source_news);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setHasFixedSize(true);

        newsAdapter = new NewsAdapterWithAds(this, recyclerViewItems);
        recyclerView.setAdapter(newsAdapter);

        // The ProgressBar that will indicate to the user that news are loading
        progressBar = findViewById(R.id.pb_one_src_news);
        progressBar.setVisibility(View.VISIBLE);

        // Get the intent from clicked item (MySourcesAdapter And NewsDetail Activity)
        oneSourceIntent = getIntent();
        String sourceName = oneSourceIntent.getStringExtra(getResources().getString(R.string.news_source_for_display));
        String key = oneSourceIntent.getStringExtra(getResources().getString(R.string.news_source_key));

        // Set the title of toolbar to the current source
        if(getSupportActionBar() != null){ getSupportActionBar().setTitle(sourceName); }

        OneSourceNewsVM oneSourceNewsVM = ViewModelProviders.of(this).get(OneSourceNewsVM.class);
        oneSourceNewsVM.getNewsLiveData(key).observe(this, new Observer<List<NewsItem>>() {
            @Override
            public void onChanged(@Nullable List<NewsItem> newsList) {

                if(newsList != null){
                    //Sort the news wrt the time
                    Collections.sort(newsList, new Comparator<NewsItem>() {
                        @Override
                        public int compare(NewsItem news1, NewsItem news2) {
                            return Long.compare(news2.getUpdateTime(), news1.getUpdateTime());
                        }
                    });
                    //First clear the list and add recent news to list
                    recyclerViewItems.clear();
                    recyclerViewItems.addAll(newsList);
                    // Then add the Ads (to related positions)
                    addBannerAds();
                    loadBannerAds();
                    // After both finished, update recyclerview adapter
                    newsAdapter.setNewsList(recyclerViewItems);
                }
                // Hide progressbar when finished
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    //Adds banner ads to the items list.
    private void addBannerAds() {
        // Loop through the items array and place a new banner ad in every ith position in the items List.
        for (int i = 4; i <= recyclerViewItems.size(); i += ITEMS_PER_AD) {
            final AdView adView = new AdView(this);
            adView.setAdSize(AdSize.MEDIUM_RECTANGLE);
            adView.setAdUnitId(getResources().getString(R.string.admob_banner_buyuk_unit_id));
            recyclerViewItems.add(i, adView);
        }
    }

    //Sets up and loads the banner ads.
    private void loadBannerAds() {
        // Load the first banner ad in the items list (subsequent ads will be loaded automatically in sequence)
        loadBannerAd(4);
    }

    // Loads the banner ads in the items list.
    private void loadBannerAd(final int index) {

        if (index >= recyclerViewItems.size()) {
            return;
        }

        Object item = recyclerViewItems.get(index);
        if (!(item instanceof AdView)) {
            throw new ClassCastException("Expected item at index " + index + " to be a banner ad" + " ad.");
        }

        final AdView adView = (AdView) item;

        // Set an AdListener on the AdView to wait for the previous banner ad
        // to finish loading before loading the next ad in the items list.
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                // The previous banner ad loaded successfully. Load the next ad in the items list.
                loadBannerAd(index + ITEMS_PER_AD);
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // The previous banner ad failed to load. Load the next ad in the items list.
                loadBannerAd(index + ITEMS_PER_AD);
            }
        });
        // Load the banner ad.
        adView.loadAd(new AdRequest.Builder().build());
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
        //pause the adMob
        for (Object item : recyclerViewItems) {
            if (item instanceof AdView) {
                AdView adView = (AdView) item;
                adView.pause();
            }
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        //resume the adMob
        for (Object item : recyclerViewItems) {
            if (item instanceof AdView) {
                AdView adView = (AdView) item;
                adView.resume();
            }
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        //destroy the adMob
        for (Object item : recyclerViewItems) {
            if (item instanceof AdView) {
                AdView adView = (AdView) item;
                adView.destroy();
            }
        }
        super.onDestroy();
    }

}
