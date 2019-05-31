package com.haberinadresi.androidapp.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsCallback;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
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
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.haberinadresi.androidapp.R;
import com.haberinadresi.androidapp.models.NewsItem;
import com.haberinadresi.androidapp.utilities.GlideApp;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.haberinadresi.androidapp.utilities.NetworkUtils;
import com.haberinadresi.androidapp.utilities.SourceLogos;
import com.haberinadresi.androidapp.utilities.WebUtils;

import java.lang.reflect.Type;
import java.util.List;

public class NewsDetailActivity extends AppCompatActivity {

    private NewsItem newsItem;
    private AdView bannerAdView;
    Intent newsDetail;
    private SharedPreferences customKeys;
    private CustomTabsClient customTabsClient;
    private CustomTabsServiceConnection serviceConnection;
    private boolean displayOnlyInWifi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);

        customKeys = getSharedPreferences(getResources().getString(R.string.custom_keys), MODE_PRIVATE);

        Toolbar toolbar = findViewById(R.id.news_detail_toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //AdMob
        bannerAdView = findViewById(R.id.bannerAdView);
        AdRequest adRequest = new AdRequest.Builder().build();
        bannerAdView.loadAd(adRequest);

        // To hide the text on the image while collapsing
        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar_image);
        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));

        TextView newsSource = findViewById(R.id.news_detail_source);
        TextView newsTitle = findViewById(R.id.news_detail_title);
        TextView newsTime = findViewById(R.id.news_detail_time);
        TextView newsSummary = findViewById(R.id.news_detail_summary);
        TextView newsDescription = findViewById(R.id.news_detail_content);
        ImageView newsImage = findViewById(R.id.news_detail_image);
        ImageView logoImage = findViewById(R.id.news_detail_logo);
        final ProgressBar progressBar = findViewById(R.id.pb_image_loading);

        // Get the news details from the intent that starts this activity (news adapter)
        newsDetail = getIntent();
        if(newsDetail != null){
            progressBar.setVisibility(View.VISIBLE);
            // convert the string into NewsItem custom object
            Gson gson = new Gson();
            String json = newsDetail.getStringExtra(getResources().getString(R.string.news_item));
            Type type = new TypeToken<NewsItem>() {}.getType();
            newsItem = gson.fromJson(json, type);
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
                    DateUtils.MINUTE_IN_MILLIS).toString();
            newsTime.setText(relativeTime);

            // Get the user's mobile data saving preference
            displayOnlyInWifi = NetworkUtils.displayOnlyInWifi(this);

            GlideApp.with(this)
                    .load(newsItem.getImageUrl())
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .onlyRetrieveFromCache(displayOnlyInWifi)
                    .dontAnimate()
                    .error(R.drawable.placeholder_icon_landscape)
                    // Add listener to hide progressbar when image is loaded
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }
                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(newsImage);

            // News Source Logo (Primary)
            GlideApp.with(this)
                    .load(newsItem.getLogoUrl())
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .onlyRetrieveFromCache(displayOnlyInWifi)
                    .error(
                            // If failed to load primary logo, then use the secondary logo in the Google Drive
                            GlideApp.with(this)
                                    .load(SourceLogos.getLogoUrl(newsItem.getKey()))
                                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                    .error(R.drawable.placeholder_icon_square))
                    .into(logoImage);


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

        // Open the news link on the webpage (With CHROME CUSTOM TABS or WEBVIEW)
        Button newsLink = findViewById(R.id.news_detail_click);
        newsLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If user preferred to open the link with browser, Open with Chrome Custom Tabs
                if (customKeys.getBoolean(getResources().getString(R.string.open_with_browser_key), false)){
                    // Create chrome custom tabs in WebUtils class and open
                    CustomTabsIntent customTabsIntent = WebUtils
                            .createChromeTab(NewsDetailActivity.this, newsItem.getNewsUrl());
                    customTabsIntent.launchUrl(NewsDetailActivity.this, Uri.parse(newsItem.getNewsUrl()));
                // Open the news link in Webview
                } else {
                    Intent intentWebview = new Intent(NewsDetailActivity.this, ShowInWebviewActivity.class);
                    intentWebview.putExtra(getResources().getString(R.string.news_source_for_display), newsItem.getSource());
                    intentWebview.putExtra(getResources().getString(R.string.news_url), newsItem.getNewsUrl());
                    startActivity(intentWebview);
                }

            }
        });

        // Share the news link on user's selected app
        FloatingActionButton fabShare = findViewById(R.id.fab_share);
        fabShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String url = newsItem.getNewsUrl();
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, url);
                intent.setType("text/plain");
                startActivity(intent);
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
                        .error(R.drawable.placeholder_icon_landscape)
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

                        // If user preferred to open the link with browser, Open with Chrome Custom Tabs
                        if (customKeys.getBoolean(getResources().getString(R.string.open_with_browser_key), false)){
                            // Create chrome custom tabs in WebUtils class and open
                            CustomTabsIntent customTabsIntent = WebUtils
                                    .createChromeTab(NewsDetailActivity.this, extraNewsList.get(index).getNewsUrl());
                            customTabsIntent.launchUrl(NewsDetailActivity.this, Uri.parse(extraNewsList.get(index).getNewsUrl()));
                            // Open the news link in Webview
                        } else {
                            Intent intentWebview = new Intent(NewsDetailActivity.this, ShowInWebviewActivity.class);
                            intentWebview.putExtra(getResources().getString(R.string.news_url), extraNewsList.get(index).getNewsUrl());
                            intentWebview.putExtra(getResources().getString(R.string.news_source_for_display), extraNewsList.get(index).getSource());
                            startActivity(intentWebview);
                        }
                    }
                });

            }
        // If couldn't get the extra 3 news, then hide the related views
        } else {
            seperator.setVisibility(View.GONE);
            moreNewsTextview.setVisibility(View.GONE);
            extraNewsContainer.setVisibility(View.GONE);
        }

        /////////  Start the Chrome Custom Tabs service  /////////////
        serviceConnection = new CustomTabsServiceConnection() {
            @Override
            public void onCustomTabsServiceConnected(ComponentName name, CustomTabsClient client) {
                // mClient is now valid.
                customTabsClient = client;
                // Warms-up the Chrome Custom Tabs to open the News Link with Chrome faster
                customTabsClient.warmup(0L);
                CustomTabsSession session = customTabsClient.newSession(new CustomTabsCallback());
                if(session != null && newsItem != null){
                    session.mayLaunchUrl(Uri.parse(newsItem.getNewsUrl()), null, null);
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                // mClient is no longer valid. This also invalidates sessions.
                customTabsClient = null;
            }
        };

        CustomTabsClient.bindCustomTabsService(this, "com.android.chrome", serviceConnection);

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
        // Unregister chrome service to avoid memory leak
        if(serviceConnection != null){
            this.unbindService(serviceConnection);
            customTabsClient = null;
            serviceConnection = null;
        }
        if (bannerAdView != null) {
            bannerAdView.destroy();
        }
        super.onDestroy();
    }


}
