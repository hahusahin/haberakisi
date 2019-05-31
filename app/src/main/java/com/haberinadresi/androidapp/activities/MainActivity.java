package com.haberinadresi.androidapp.activities;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.haberinadresi.androidapp.R;
import com.haberinadresi.androidapp.adapters.TabPagerAdapter;
import com.haberinadresi.androidapp.models.CategoryItem;
import com.haberinadresi.androidapp.services.UpdateMyColumnists;
import com.haberinadresi.androidapp.utilities.NetworkUtils;
import com.haberinadresi.androidapp.utilities.SharedPreferenceUtils;
import com.haberinadresi.androidapp.utilities.WhatsNewDialog;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private NavigationView navigationView;
    private SharedPreferences customPreferences;
    private InterstitialAd interstitialAd;
    private boolean onPauseFlag = false;
    public static final int MAX_COUNT = 10; //If user clicks more than ... news detail, show Interstitial ad when backpressed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        customPreferences = getSharedPreferences(getResources().getString(R.string.custom_keys), MODE_PRIVATE);
        // Apply Night mode if turned on
        if(customPreferences.getBoolean(getResources().getString(R.string.night_mode_key), false)){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        setContentView(R.layout.activity_main);

        ///////// clear the news that are in the cache
        // (only when activity is created from scratch, not when a configuration change occurs)
        boolean isCategoriesChanged = getIntent().getBooleanExtra(EditCategoriesActivity.class.getName(), false);
        boolean isSettingsChanged = getIntent().getBooleanExtra(SettingsActivity.class.getName(), false);
        if(! isCategoriesChanged && ! isSettingsChanged){
            getSharedPreferences(getResources().getString(R.string.cached_news_key), MODE_PRIVATE).edit().clear().apply();
        }

        //////////////// Clear the old clicked news and columns from sharedprefs
        // All clicked news are cleared in each session
        getSharedPreferences(getResources().getString(R.string.clicked_news_key), Context.MODE_PRIVATE).edit().clear().apply();
        // Columns are cleared wrt their dates (since they are not changing rapidly)
        SharedPreferenceUtils.clearColumns(this);

        /* HATALAR VERİYOR - O YÜZDEN YUKARIDAKİYLE DEĞİŞTİRDİM
        //////////////// start the cleanup service of old clicked news and columns data from sharedprefs
        Intent cleanClickedIntent = new Intent(this, CleanClickedNews.class);
        JobIntentService.enqueueWork(this, CleanClickedNews.class, 100, cleanClickedIntent);
        */

        /////////////İLERLEYEN ZAMANLARDA SİLİNECEK (3 YENİ KATEGORİNİN EKLENMESİ)/////////////
        if(! customPreferences.getBoolean(getResources().getString(R.string.new_categories_added_v1), false)){
            addNewCategories();
        }

        /////////////Initialize Admob
        MobileAds.initialize(this, getResources().getString(R.string.admob_app_id));

        ////////////////
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TabLayout tabLayout = findViewById(R.id.tabs);
        final ViewPager viewPager = findViewById(R.id.viewpager);

        TabPagerAdapter pagerAdapter = new TabPagerAdapter(this, getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        //viewPager.setOffscreenPageLimit(2); //Set the number of pages that should be retained to either side of the current page
        /////////////////////

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // If  device is large screen (tablet) increase text size of items in the navigation view (can't be made from xml)
        if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) ==
                Configuration.SCREENLAYOUT_SIZE_LARGE ||
                (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) ==
                Configuration.SCREENLAYOUT_SIZE_XLARGE){

            setMenuHeadTitleSize(R.id.nav_parent_my_sources);
            setMenuHeadTitleSize(R.id.nav_parent_arrange_sources);
            setMenuHeadTitleSize(R.id.nav_parent_others);
        }

        /////////////Initialize Interstitial Ad and set listeners
        interstitialAd = new InterstitialAd(getApplicationContext());
        interstitialAd.setAdUnitId(getResources().getString(R.string.admob_interstitial_unit_id));
        interstitialAd.loadAd(new AdRequest.Builder().build());
        interstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdFailedToLoad(int i) {
                // If failed, then load a new one
                interstitialAd.loadAd(new AdRequest.Builder().build());
            }
            @Override
            public void onAdOpened() {
                //If Ad is displayed, then reset the counter
                customPreferences.edit().putInt(getResources().getString(R.string.news_click_counter), 0).apply();
            }
            @Override
            public void onAdClosed() {
                // Load the next interstitial
                interstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });

        //////////// Create Notification Channel (For API Level 26+)
        createNotificationChannels();

        /////////// Subscribe all users to General Notifications Topic (For only once)
        if(! customPreferences.getBoolean(getResources().getString(R.string.subscribed_to_general_notifications), false)){
            FirebaseMessaging.getInstance().subscribeToTopic(getResources().getString(R.string.general_notifications_topic));
            customPreferences.edit().putBoolean(getResources().getString(R.string.subscribed_to_general_notifications), true).apply();
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_my_sources) {
            startActivity(new Intent(this,MySourcesActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_kaynaklarim){

            startActivity(new Intent(this, MySourcesActivity.class));

        } else if (id == R.id.nav_favorite_news){

            startActivity(new Intent(this, FavNewsActivity.class));

        } else if (id == R.id.nav_favorite_columns){

            startActivity(new Intent(this, FavColumnsActivity.class));

        } else if (id == R.id.nav_gundem) {

            Intent sourceSelection = new Intent(this, SourceSelectionActivity.class);
            sourceSelection.putExtra(getResources().getString(R.string.news_category_key), getResources().getString(R.string.gundem_key));
            sourceSelection.putExtra(getResources().getString(R.string.news_category_name),getResources().getString(R.string.gundem_normal));
            startActivity(sourceSelection);

        } else if (id == R.id.nav_spor) {

            Intent sourceSelection = new Intent(this, SourceSelectionActivity.class);
            sourceSelection.putExtra(getResources().getString(R.string.news_category_key), getResources().getString(R.string.spor_key));
            sourceSelection.putExtra(getResources().getString(R.string.news_category_name),getResources().getString(R.string.spor_normal));
            startActivity(sourceSelection);

        } else if (id == R.id.nav_ekonomi) {

            Intent sourceSelection = new Intent(this, SourceSelectionActivity.class);
            sourceSelection.putExtra(getResources().getString(R.string.news_category_key), getResources().getString(R.string.ekonomi_key));
            sourceSelection.putExtra(getResources().getString(R.string.news_category_name),getResources().getString(R.string.ekonomi_normal));
            startActivity(sourceSelection);

        } else if (id == R.id.nav_teknoloji) {

            Intent sourceSelection = new Intent(this, SourceSelectionActivity.class);
            sourceSelection.putExtra(getResources().getString(R.string.news_category_key), getResources().getString(R.string.teknoloji_key));
            sourceSelection.putExtra(getResources().getString(R.string.news_category_name),getResources().getString(R.string.teknoloji_normal));
            startActivity(sourceSelection);

        } else if (id == R.id.nav_kultursanat) {

            Intent sourceSelection = new Intent(this, SourceSelectionActivity.class);
            sourceSelection.putExtra(getResources().getString(R.string.news_category_key), getResources().getString(R.string.kultursanat_key));
            sourceSelection.putExtra(getResources().getString(R.string.news_category_name),getResources().getString(R.string.kultursanat_normal));
            startActivity(sourceSelection);

        } else if (id == R.id.nav_saglik) {

            Intent sourceSelection = new Intent(this, SourceSelectionActivity.class);
            sourceSelection.putExtra(getResources().getString(R.string.news_category_key), getResources().getString(R.string.saglik_key));
            sourceSelection.putExtra(getResources().getString(R.string.news_category_name),getResources().getString(R.string.saglik_normal));
            startActivity(sourceSelection);

        } else if (id == R.id.nav_magazin) {

            Intent sourceSelection = new Intent(this, SourceSelectionActivity.class);
            sourceSelection.putExtra(getResources().getString(R.string.news_category_key), getResources().getString(R.string.magazin_key));
            sourceSelection.putExtra(getResources().getString(R.string.news_category_name),getResources().getString(R.string.magazin_normal));
            startActivity(sourceSelection);

        } else if (id == R.id.nav_yazarlar) {

            Intent columnistSelection = new Intent(this, ColumnistSelectActivity.class);
            startActivity(columnistSelection);

        } else if (id == R.id.nav_habersiteleri) {

            Intent categorySelection = new Intent(this, WebCategoriesActivity.class);
            startActivity(categorySelection);

        }  else if (id == R.id.nav_settings) {

            Intent settings = new Intent(this, SettingsActivity.class);
            startActivity(settings);

        } else if (id == R.id.nav_share_app) {

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.app_full_name) +
                    "\n\n" + "https://play.google.com/store/apps/details?id=" + getPackageName());
            startActivity(intent);

        } else if (id == R.id.nav_evaluate_app) {

            final String appPackageName = getPackageName();
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }

        } else if (id == R.id.nav_comment_app) {

            Intent emailIntent = new Intent(Intent.ACTION_SENDTO,
                    Uri.fromParts("mailto",getResources().getString(R.string.app_email_address), null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.about_news_app));
            //emailIntent.putExtra(Intent.EXTRA_TEXT, "Body");
            startActivity(Intent.createChooser(emailIntent, getResources().getString(R.string.send_comment_app)));

        } else if (id == R.id.nav_user_guide) {

            Intent userGuide = new Intent(this, IntroActivity.class);
            startActivity(userGuide);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onPause() {
        onPauseFlag = true;
        super.onPause();
    }

    @Override
    public void onResume() {
        // If coming from onPause (not from onCreate)
        if(onPauseFlag){
            // If user clicked at least ... news AND Ad is loaded, show the interstitial Ad.
            int counter = customPreferences.getInt(getResources().getString(R.string.news_click_counter), 0);
            if(counter > MAX_COUNT && interstitialAd.isLoaded()){
                interstitialAd.show();
            }
            onPauseFlag = false;
        }

        /////////////////// İLERLEYEN ZAMANLARDA SİLİNECEK (GEÇİCİ) ///////////////////////////
        //start the service of updating the keys of favorite columnists for once (to make all keys lowercase)
        if(! customPreferences.getBoolean(getResources().getString(R.string.is_columnist_keys_lowercased), false)){
            Intent updateColumnistsIntent = new Intent(MainActivity.this, UpdateMyColumnists.class);
            startService(updateColumnistsIntent);
            customPreferences.edit().putBoolean(getResources().getString(R.string.is_columnist_keys_lowercased), true).apply();
        }

        /////////////////// YENİ ÖZELLİK EKLEMEDİYSEN SİL ///////////////////////////
        // Show What's New Dialog for once if it is not seen by user AND it is updated (not newly installed)
        if(! customPreferences.getBoolean(getResources().getString(R.string.whats_new_dialog_v3), false) &&
                NetworkUtils.isInstallFromUpdate(MainActivity.this)){
            WhatsNewDialog dialog = new WhatsNewDialog();
            dialog.show(getSupportFragmentManager(), "What's New In This Update");
        }

        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /* TO AVOID OUT OF MEMORY ERRORS
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        GlideApp.get(this).clearMemory();
    }
    */

    // To increase text size of navigation items in tablets
    private void setMenuHeadTitleSize(int itemId) {

        Menu menu = navigationView.getMenu();
        MenuItem menuItem = menu.findItem(itemId);
        MenuItem subMenuItem = menuItem.getSubMenu().getItem();
        String title = String.valueOf(subMenuItem.getTitle());

        SpannableString mNewTitle = new SpannableString(title);
        mNewTitle.setSpan(new RelativeSizeSpan(1.3f), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        subMenuItem.setTitle(mNewTitle);
    }

    /////////////////// İLERLEYEN ZAMANLARDA SİLİNECEK (GEÇİCİ) ///////////////////////////
    // EKONOMİ, KÜLTÜR SANAT, SAĞLIK VE HABER SİTELERİ SEKMELERİNİ KİŞİNİN KATEGORİLERİNE EKLE (SADECE BİR KERE)

    public void addNewCategories(){
        SharedPreferences.Editor editor = customPreferences.edit();
        String json = customPreferences.getString(getResources().getString(R.string.category_preferences), null);
        if (json != null) {
            //Convert string to arraylist
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<CategoryItem>>() {}.getType();
            List<CategoryItem> categoryList = gson.fromJson(json, type);
            //Add the three categories to list
            categoryList.add(new CategoryItem(getResources().getString(R.string.ekonomi_normal), R.mipmap.letter_e, true));
            categoryList.add(new CategoryItem(getResources().getString(R.string.kultursanat_normal), R.mipmap.letter_k, true));
            categoryList.add(new CategoryItem(getResources().getString(R.string.saglik_normal), R.mipmap.letter_s, true));
            categoryList.add(new CategoryItem(getResources().getString(R.string.habersiteleri_normal), R.mipmap.letter_h, true));
            //Convert list to string again and rewrite the file
            editor.putString(getResources().getString(R.string.category_preferences), gson.toJson(categoryList));
        }
        editor.putBoolean(getResources().getString(R.string.new_categories_added_v1), true);
        editor.apply();
    }

    private void createNotificationChannels() {
        // If device API Level is 26+ (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String id = getResources().getString(R.string.last_minute_channel_id);
            CharSequence name = getResources().getString(R.string.last_minute_channel_name);

            NotificationChannel lastMinute = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH);
            //lastMinute.setDescription("Haber Bildirimleri"); GEREK YOK
            lastMinute.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(lastMinute);

        }
    }

}


/*
// If Ad is loaded, then show it
if(interstitialAd.isLoaded()){
    interstitialAd.show();
    interstitialAd.setAdListener(new AdListener() {
        @Override
        public void onAdOpened() {
            //If Ad is opened, then reset the counter
            SharedPreferences.Editor editor = customPreferences.edit();
            editor.putInt(getResources().getString(R.string.news_click_counter), 0);
            editor.apply();
        }
        @Override
        public void onAdClosed() {
            // Load the next interstitial
            interstitialAd.loadAd(new AdRequest.Builder().build());
        }
    });
    // If it fails to load, then load a new one
} else {
    interstitialAd.loadAd(new AdRequest.Builder().build());
}
*/