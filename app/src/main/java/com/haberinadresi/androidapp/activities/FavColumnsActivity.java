package com.haberinadresi.androidapp.activities;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.haberinadresi.androidapp.R;
import com.haberinadresi.androidapp.adapters.FavColumnsAdapter;
import com.haberinadresi.androidapp.models.Columnist;
import com.haberinadresi.androidapp.viewmodels.FavColumnsVM;
import java.util.List;

//This activity loads news from only the selected source
public class FavColumnsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FavColumnsAdapter columnsAdapter;
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

        setContentView(R.layout.activity_favorite_news);

        final Toolbar toolbar = findViewById(R.id.favorite_news_toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //Admob
        bannerAdView = findViewById(R.id.bannerAdView);
        AdRequest adRequest = new AdRequest.Builder().build();
        bannerAdView.loadAd(adRequest);
        bannerAdView.setAdListener(new AdListener(){
            @Override
            public void onAdFailedToLoad(int i) {
                // If failed, then load a new one
                bannerAdView.loadAd(new AdRequest.Builder().build());
            }
        });

        // Don't show empty favorite dialog at the beginning
        emptyFavNewsWarning = findViewById(R.id.tv_empty_fav_dialog);
        // If this bookmark is added in XML, it gives error on low API level (19), so added in here
        emptyFavNewsWarning.setCompoundDrawablesWithIntrinsicBounds(null,
                AppCompatResources.getDrawable(this, R.drawable.ic_saved_news_big),null,null );
        emptyFavNewsWarning.setText(getResources().getString(R.string.empty_fav_columns_database));
        emptyFavNewsWarning.setVisibility(View.GONE);

        recyclerView = findViewById(R.id.rv_favorite_news);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        recyclerView.setHasFixedSize(true);

        // The ProgressBar that will indicate to the user that news are loading
        progressBar = findViewById(R.id.pb_favorite_news);
        progressBar.setVisibility(View.VISIBLE);

        FavColumnsVM favColumnsVM = ViewModelProviders.of(this).get(FavColumnsVM.class);
        favColumnsVM.getFavColumns().observe(this, new Observer<List<Columnist>>() {
            @Override
            public void onChanged(@Nullable List<Columnist> columnsList) {

                columnsAdapter = new FavColumnsAdapter(FavColumnsActivity.this, columnsList);
                recyclerView.setAdapter(columnsAdapter);
                // Hide ProgressBar when news are loaded
                progressBar.setVisibility(View.INVISIBLE);
                // If the user didn't add any news to favorite show dialog
                if(columnsAdapter.getItemCount() < 1){
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
