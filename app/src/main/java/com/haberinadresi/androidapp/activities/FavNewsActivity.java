package com.haberinadresi.androidapp.activities;

import androidx.lifecycle.ViewModelProvider;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.haberinadresi.androidapp.R;
import com.haberinadresi.androidapp.adapters.FavNewsAdapter;
import com.haberinadresi.androidapp.viewmodels.FavNewsVM;

import java.util.Collections;

public class FavNewsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FavNewsAdapter newsAdapter;
    private ProgressBar progressBar;
    private TextView emptyFavNewsWarning;
    private AdView bannerAdView;

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
        bannerAdView.setAdListener(new AdListener(){
            @Override
            public void onAdLoaded() {
                // If ad is loaded than reset counter that keeps failures
                customPrefs.edit().putInt(getResources().getString(R.string.banner_trial_counter), 0).apply();
            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                // Try for three times only
                int trial = customPrefs.getInt(getResources().getString(R.string.banner_trial_counter), 0);
                trial++;
                if(trial <= 3){
                    customPrefs.edit().putInt(getResources().getString(R.string.banner_trial_counter), trial).apply();
                    // Wait for ... seconds and then try again
                    Handler handler = new Handler();
                    handler.postDelayed(() -> bannerAdView.loadAd(new AdRequest.Builder().build()), 10000);
                }
            }
        });

        // Don't show empty favorite dialog at the beginning
        emptyFavNewsWarning = findViewById(R.id.tv_empty_fav_dialog);
        // If this bookmark is added in XML, it gives error on low API level (19), so added in here
        emptyFavNewsWarning.setCompoundDrawablesWithIntrinsicBounds(null,
                AppCompatResources.getDrawable(this, R.drawable.ic_saved_news_big),null,null );
        emptyFavNewsWarning.setText(getResources().getString(R.string.empty_fav_news_database));
        emptyFavNewsWarning.setVisibility(View.GONE);

        // Initialize Recyclerview
        recyclerView = findViewById(R.id.rv_favorite_news);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        // If user prefers to view news as grid then set gridlayout, else set normal layout (0 and 1 for normal view, 2 for grid)
        if(customPrefs.getInt(getResources().getString(R.string.news_item_view_preference), 0) == 2){
            recyclerView.setLayoutManager(gridLayoutManager);
        } else {
            recyclerView.setLayoutManager(linearLayoutManager);
        }
        recyclerView.setHasFixedSize(true);

        // The ProgressBar that will indicate to the user that news are loading
        progressBar = findViewById(R.id.pb_favorite_news);
        progressBar.setVisibility(View.VISIBLE);

        FavNewsVM favNewsVM = new ViewModelProvider(this).get(FavNewsVM.class);
        favNewsVM.getFavNews().observe(this, newsList -> {
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
