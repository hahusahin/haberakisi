package com.haberinadresi.androidapp.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;

import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.haberinadresi.androidapp.R;
import com.haberinadresi.androidapp.models.NewsItem;
import com.haberinadresi.androidapp.repository.FavNewsRepository;
import com.haberinadresi.androidapp.utilities.BackupNewsImages;
import com.haberinadresi.androidapp.utilities.GlideApp;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.haberinadresi.androidapp.utilities.NetworkUtils;
import com.haberinadresi.androidapp.utilities.BackupLogosDrive;

import java.lang.reflect.Type;
import java.util.List;

public class NewsDetailActivity extends AppCompatActivity {

    private NewsItem newsItem;
    private AdView bannerAdView;
    Intent newsDetail;
    private SharedPreferences savedNews;
    private boolean displayOnlyInWifi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences customKeys = getSharedPreferences(getResources().getString(R.string.custom_keys), MODE_PRIVATE);
        // Set the text style (size) of user's preference
        String fontPreference = customKeys.getString(getResources().getString(R.string.pref_font_key), "medium");
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

        setContentView(R.layout.activity_news_detail);


        Toolbar toolbar = findViewById(R.id.news_detail_toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //AdMob
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

        TextView newsSource = findViewById(R.id.news_detail_source);
        TextView newsTitle = findViewById(R.id.news_detail_title);
        TextView newsTime = findViewById(R.id.news_detail_time);
        TextView newsSummary = findViewById(R.id.news_detail_summary);
        TextView newsDescription = findViewById(R.id.news_detail_content);
        final ImageView newsImage = findViewById(R.id.news_detail_image);
        ImageView logoImage = findViewById(R.id.news_detail_logo);
        final ProgressBar progressBar = findViewById(R.id.pb_image_loading);
        final NestedScrollView nestedScrollView = findViewById(R.id.nsv_news_detail);

        // Get the news details from the intent that starts this activity (news adapter)
        newsDetail = getIntent();
        if(newsDetail != null){
            progressBar.setVisibility(View.VISIBLE);
            // convert the string into NewsItem custom object
            Gson gson = new Gson();
            String json = newsDetail.getStringExtra(getResources().getString(R.string.news_item));
            Type type = new TypeToken<NewsItem>() {}.getType();
            newsItem = gson.fromJson(json, type);

            // Hide all views until image is loaded
            nestedScrollView.setVisibility(View.INVISIBLE);

            // Get the user's mobile data saving preference
            displayOnlyInWifi = NetworkUtils.displayOnlyInWifi(this);
            // Load news image
            GlideApp.with(this)
                    .load(newsItem.getImageUrl())
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .onlyRetrieveFromCache(displayOnlyInWifi)
                    .dontAnimate()
                    .error(
                            // If failed to load news image, then load backup image in the Google Drive
                            GlideApp.with(this)
                                    .load(BackupNewsImages.getLogoUrl(newsItem.getKey()))
                                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                    .error(R.drawable.placeholder_icon_landscape))
                    // Add listener to hide progressbar when image is loaded and to show all views
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            //newsImage.setVisibility(View.GONE);
                            nestedScrollView.setVisibility(View.VISIBLE);
                            return false;
                        }
                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            nestedScrollView.setVisibility(View.VISIBLE);
                            return false;
                        }
                    })
                    .into(newsImage);

            // News Source Logo (Primary)
            GlideApp.with(this)
                    .load(newsItem.getLogoUrl())
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .apply(RequestOptions.circleCropTransform())
                    .onlyRetrieveFromCache(displayOnlyInWifi)
                    .error(
                            // If failed to load primary logo, then use the secondary logo in the Google Drive
                            GlideApp.with(this)
                                    .load(BackupLogosDrive.getLogoUrl(newsItem.getKey()))
                                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                    .apply(RequestOptions.circleCropTransform())
                                    .error(R.drawable.placeholder_icon_square))
                    .into(logoImage);

            // Set the text/image of related fields
            newsTitle.setText(newsItem.getTitle());
            newsSource.setText(newsItem.getSource());
            newsSummary.setText(newsItem.getSummary());

            // If summary is empty, set the visibility of summary area to GONE
            if(! newsItem.getSummary().equals("")){
                newsSummary.setVisibility(View.VISIBLE);
            } else {
                newsSummary.setVisibility(View.GONE);
            }

            newsDescription.setText(newsItem.getDetail());

            // News Relative Time Ago
            String relativeTime = DateUtils.getRelativeTimeSpanString(
                    newsItem.getUpdateTime(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE).toString();
            newsTime.setText(relativeTime);


            logoImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent oneSourceNews = new Intent(NewsDetailActivity.this, OneSrcNewsActivity.class);
                    oneSourceNews.putExtra(getResources().getString(R.string.news_source_for_display), newsItem.getSource());
                    oneSourceNews.putExtra(getResources().getString(R.string.news_source_key), newsItem.getKey());
                    startActivity(oneSourceNews);
                    finish();
                }
            });

            newsSource.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent oneSourceNews = new Intent(NewsDetailActivity.this, OneSrcNewsActivity.class);
                    oneSourceNews.putExtra(getResources().getString(R.string.news_source_for_display), newsItem.getSource());
                    oneSourceNews.putExtra(getResources().getString(R.string.news_source_key), newsItem.getKey());
                    startActivity(oneSourceNews);
                    finish();
                }
            });

        }

        // Open the news link with Webview
        Button newsLink = findViewById(R.id.news_detail_click);
        newsLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentWebview = new Intent(NewsDetailActivity.this, ShowInWebviewActivity.class);
                intentWebview.putExtra(getResources().getString(R.string.news_source_for_display), newsItem.getSource());
                intentWebview.putExtra(getResources().getString(R.string.news_url), newsItem.getNewsUrl());
                startActivity(intentWebview);

            }
        });

        // Share the news link on whatsapp
        ImageView whatsapp = findViewById(R.id.iv_share_whatsapp);
        whatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String url = newsItem.getNewsUrl();
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, url);
                intent.setType("text/plain");
                intent.setPackage("com.whatsapp");
                try {
                    startActivity(intent);
                } catch (android.content.ActivityNotFoundException e) {
                    Toast.makeText(getApplicationContext(), "Whatsapp açılamadı", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Share the news link on twitter
        ImageView twitter = findViewById(R.id.iv_share_twitter);
        twitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String url = newsItem.getNewsUrl();
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, url);
                intent.setType("text/plain");
                intent.setPackage("com.twitter.android");
                try {
                    startActivity(intent);
                } catch (android.content.ActivityNotFoundException e) {
                    Toast.makeText(getApplicationContext(), "Twitter açılamadı", Toast.LENGTH_SHORT).show();
                }

            }
        });

        // Share the news link on facebook
        ImageView facebook = findViewById(R.id.iv_share_facebook);
        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String url = newsItem.getNewsUrl();
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, url);
                intent.setType("text/plain");
                intent.setPackage("com.facebook.katana");
                try {
                    startActivity(intent);
                } catch (android.content.ActivityNotFoundException e) {
                    Toast.makeText(getApplicationContext(), "Facebook açılamadı", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ///////// EXTRA NEWS FROM THE SAME SOURCE THAT USER CLICKS /////////

        // Seperator line under the "HABERIN DETAYI ICIN TIKLAYINIZ" and text under it
        View seperator = findViewById(R.id.extra_news_seperator);
        TextView moreNewsTextview = findViewById(R.id.tv_more_news);
        // Linear Layout that holds all three extra news
        LinearLayout extraNewsContainer = findViewById(R.id.ll_extra_news);
        // Keep linear layouts as array to iterate over them
        LinearLayout[] layouts = {findViewById(R.id.extra_news_1), findViewById(R.id.extra_news_2), findViewById(R.id.extra_news_3)};

        // If the list that contains extra 3 news can be obtained from intent
        if(newsDetail.getStringExtra(getResources().getString(R.string.extra_news)) != null){
            // Convert string to List
            Gson gson = new Gson();
            String json = newsDetail.getStringExtra(getResources().getString(R.string.extra_news));
            Type type = new TypeToken<List<NewsItem>>() {}.getType();
            final List<NewsItem> extraNewsList = gson.fromJson(json, type);

            String moreNewsTitle = extraNewsList.get(0).getSource() + " Kaynağından Daha Fazla";
            moreNewsTextview.setText(moreNewsTitle);
            // Views to be filled
            TextView[] titleTextviews = new TextView[3];
            TextView[] timeTextviews = new TextView[3];
            ImageView[] imageViews = new ImageView[3];
            // Fill all three views by iterating over each
            for (int i = 0; i < 3; i++) {
                // News Title of i-th extra news
                titleTextviews[i] = layouts[i].findViewById(R.id.tv_news_title);
                titleTextviews[i].setText(extraNewsList.get(i).getTitle());
                // News Time of i-th extra news
                timeTextviews[i] = layouts[i].findViewById(R.id.tv_news_time);
                // News Relative Time Ago
                String relativeTime = DateUtils.getRelativeTimeSpanString(
                        extraNewsList.get(i).getUpdateTime(),
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS).toString();
                timeTextviews[i].setText(relativeTime);
                // News Image of i-th extra news
                imageViews[i] = layouts[i].findViewById(R.id.iv_news_image);
                GlideApp.with(this)
                        .load(extraNewsList.get(i).getImageUrl())
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .onlyRetrieveFromCache(displayOnlyInWifi)
                        .error(
                            // If failed to load news image, then load backup image in the Google Drive
                            GlideApp.with(this)
                                    .load(BackupNewsImages.getLogoUrl(extraNewsList.get(i).getKey()))
                                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                    .error(R.drawable.placeholder_icon_landscape)
                        )
                        .centerCrop()
                        .dontAnimate()
                        .into(imageViews[i]);

                final int index = i;
                layouts[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // Increment the click counter (used for displaying Interstitial Ad in Main Activity OnResume)
                        SharedPreferences customKeys = getSharedPreferences(getResources().getString(R.string.custom_keys), MODE_PRIVATE);
                        int counter = customKeys.getInt(getResources().getString(R.string.news_click_counter),0);
                        customKeys.edit().putInt(getResources().getString(R.string.news_click_counter), counter + 1).apply();

                        // Open the link with Webview
                        Intent intentWebview = new Intent(NewsDetailActivity.this, ShowInWebviewActivity.class);
                        intentWebview.putExtra(getResources().getString(R.string.news_url), extraNewsList.get(index).getNewsUrl());
                        intentWebview.putExtra(getResources().getString(R.string.news_source_for_display), extraNewsList.get(index).getSource());
                        startActivity(intentWebview);
                    }
                });

            }
        // If couldn't get the extra 3 news, then hide the related views
        } else {
            seperator.setVisibility(View.GONE);
            moreNewsTextview.setVisibility(View.GONE);
            extraNewsContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // create the menu items
        getMenuInflater().inflate(R.menu.activity_newsdetail, menu);
        // Set the save image depending on the news' status (saved vs unsaved)
        MenuItem saveMenuItem = menu.findItem(R.id.action_save);
        savedNews = getSharedPreferences(getResources().getString(R.string.saved_news_key), Context.MODE_PRIVATE);
        if(savedNews.contains(newsItem.getNewsUrl())){
            saveMenuItem.setIcon(R.drawable.ic_save_filled_white);
        } else {
            saveMenuItem.setIcon(R.drawable.ic_save_empty_white);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home){

            onBackPressed();

        // SAVE OPERATIONS
        } else if(id == R.id.action_save){
            // if news is already in the saved list
            if (savedNews.contains(newsItem.getNewsUrl())) {
                //Change menu item icon
                item.setIcon(R.drawable.ic_save_empty_white);
                //Delete news from favorite news database
                FavNewsRepository repository = new FavNewsRepository(NewsDetailActivity.this);
                repository.delete(newsItem);
                // Show toast message
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.news_deleted_from_favorite), Toast.LENGTH_SHORT).show();
                // update shared preference file
                savedNews.edit().remove(newsItem.getNewsUrl()).apply();

                // News is not saved before, so save it into database
            } else {
                //Change menu item icon
                item.setIcon(R.drawable.ic_save_filled_white);
                //Add news to favorite news database
                FavNewsRepository repository = new FavNewsRepository(NewsDetailActivity.this);
                repository.insert(newsItem);
                // Show toast message
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.news_added_to_favorite), Toast.LENGTH_SHORT).show();
                // update shared preference file
                savedNews.edit().putBoolean(newsItem.getNewsUrl(), true).apply();
            }

        // SHARE OPERATIONS
        } else if(id == R.id.action_share){

            String url = newsItem.getNewsUrl();
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, url);
            intent.setType("text/plain");
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
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
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bannerAdView != null) {
            bannerAdView.destroy();
        }
    }

}
