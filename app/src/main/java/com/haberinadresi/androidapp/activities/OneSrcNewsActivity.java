package com.haberinadresi.androidapp.activities;

import androidx.lifecycle.ViewModelProvider;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.haberinadresi.androidapp.R;
import com.haberinadresi.androidapp.adapters.NewsAdapterWithAds;
import com.haberinadresi.androidapp.viewmodels.OneSourceNewsVM;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//This activity loads news from only the selected source
public class OneSrcNewsActivity extends AppCompatActivity {

    private NewsAdapterWithAds newsAdapter;
    private ProgressBar progressBar;
    Intent oneSourceIntent;
    private List<Object> recyclerViewItems = new ArrayList<>(); // List of banner ads and NewsItems that populate the RecyclerView.
    public static final int ITEMS_PER_AD = 13; // A banner ad is placed in every 6 + 13th position in the RecyclerView.

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
                case "medium": default:
                    setTheme(R.style.FontStyle_Medium);
                    break;
                case "large":
                    setTheme(R.style.FontStyle_Large);
                    break;
                case "xlarge":
                    setTheme(R.style.FontStyle_XLarge);
                    break;
            }
        } else {
            setTheme(R.style.FontStyle_Medium);
        }

        setContentView(R.layout.activity_one_src_news);

        Toolbar toolbar = findViewById(R.id.one_source_toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //initialize recyclerview
        RecyclerView recyclerView = findViewById(R.id.rv_one_source_news);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        // When it is the ads turn in the recyclerview, only show one item ( don't show news)
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if ((position + 7) % ITEMS_PER_AD == 0) {
                    return 2;
                }
                return 1;
            }
        });
        // If user prefers to view news as grid then set gridlayout, else set normal layout (0 and 1 for normal view, 2 for grid)
        if(customPrefs.getInt(getResources().getString(R.string.news_item_view_preference), 0) == 2){
            recyclerView.setLayoutManager(gridLayoutManager);
        } else {
            recyclerView.setLayoutManager(linearLayoutManager);
        }
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

        OneSourceNewsVM oneSourceNewsVM = new ViewModelProvider(this).get(OneSourceNewsVM.class);
        oneSourceNewsVM.getNewsLiveData(key).observe(this, newsList -> {
            if(newsList != null){
                //Sort the news wrt the time
                Collections.sort(newsList, (news1, news2) -> Long.compare(news2.getUpdateTime(), news1.getUpdateTime()));
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
        });
    }

    //Adds banner ads to the items list.
    private void addBannerAds() {
        // Loop through the items array and place a new banner ad in every ith position in the items List.
        for (int i = 6; i <= recyclerViewItems.size(); i += ITEMS_PER_AD) {
            final AdView adView = new AdView(this);
            adView.setAdSize(AdSize.MEDIUM_RECTANGLE);
            adView.setAdUnitId(getResources().getString(R.string.admob_banner_buyuk_unit_id));
            recyclerViewItems.add(i, adView);
        }
    }

    //Sets up and loads the banner ads.
    private void loadBannerAds() {
        // Load the first banner ad in the items list (subsequent ads will be loaded automatically in sequence)
        loadBannerAd(6);
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
            public void onAdFailedToLoad(LoadAdError loadAdError) {
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
