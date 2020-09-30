package com.haberinadresi.androidapp.activities;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.haberinadresi.androidapp.R;

import java.io.ByteArrayInputStream;

public class ShowInWebviewActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar progressBar;
    private FrameLayout frameLayout;
    private String source;
    private String newsUrl;
    public static final String[] adKeywords =
            {"googleadservices", "googleads", "pagead", "pubads", ".click", "/clicks", ".doubleclick",
                "adclick.", "/banner.", "https//ad."};


    @SuppressWarnings("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_in_webview);

        final Toolbar toolbar = findViewById(R.id.show_website_toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        webView =findViewById(R.id.webView);
        frameLayout = findViewById(R.id.fl_progress);
        progressBar = findViewById(R.id.pb_webview_loading);
        progressBar.setMax(100);

        // Get the news url from the intent (NewsDetail Activity & News Adapter & Column Adapter & Notification)
        Intent intentUrl = getIntent();
        source = intentUrl.getStringExtra(getResources().getString(R.string.news_source_for_display));
        newsUrl = intentUrl.getStringExtra(getResources().getString(R.string.news_url));
        // only for notifications (to save the clicked items)
        String sourceKey =  intentUrl.getStringExtra(getResources().getString(R.string.news_source_key));
        if(sourceKey != null){
            saveClickedNotifications(sourceKey, newsUrl);
        }

        // Start Webview
        webView.setWebViewClient(new MyWebViewClient());
        webView.setWebChromeClient(new MyChromeClient());

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true); // To load the page without zoom at initial stage
        webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING); // To avoid big font size
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW); // To show pictures (not seen in some websites)
        }

        webView.getSettings().setBuiltInZoomControls(true); // to enable pinch zooming
        webView.getSettings().setDisplayZoomControls(false); // to hide zoom control buttons
        //webView.setOverScrollMode(WebView.OVER_SCROLL_NEVER);

        webView.loadUrl(newsUrl);

        progressBar.setProgress(0);
    }

    private class MyWebViewClient extends WebViewClient{
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            /* ESKİSİ
            view.loadUrl(url);
            frameLayout.setVisibility(View.VISIBLE);
            return true;
            */
            boolean overrideUrlLoading = false;

            if (url.startsWith("whatsapp://")) {

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, newsUrl);
                intent.setType("text/plain");
                intent.setPackage("com.whatsapp");
                try {
                    startActivity(intent);
                } catch (android.content.ActivityNotFoundException e) {
                    Toast.makeText(getApplicationContext(), "Whatsapp açılamadı", Toast.LENGTH_SHORT).show();
                }

                overrideUrlLoading = true;

            }
            else {
                view.loadUrl(url);
                frameLayout.setVisibility(View.VISIBLE);
            }

            return overrideUrlLoading;
        }

        // To block ads
        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            boolean adFound = false;
            for(String item : adKeywords){
                if(request.getUrl().toString().contains(item)){
                    adFound = true;
                    break;
                }
            }
            if(adFound){
                return new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream("".getBytes()));
            } else {
                return super.shouldInterceptRequest(view, request);
            }
        }
    }

    private class MyChromeClient extends WebChromeClient{
        @Override
        public void onProgressChanged(WebView view, int progress) {
            // Show progressbar
            progressBar.setProgress(progress);
            setTitle(source);
            frameLayout.setVisibility(View.VISIBLE);
            // When loading finished hide progress, show news title
            if(progress >= 95){
                frameLayout.setVisibility(View.GONE);
            }
            super.onProgressChanged(view, progress);
        }

        // To block ads
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            result.cancel();
            return true;
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
            result.cancel();
            return true;
        }

        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
            result.cancel();
            return true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_webview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home){

            onBackPressed();

        } else if(id == R.id.action_gobackward){

            if(webView.canGoBack()){
                webView.goBack();
            }

        } else if(id == R.id.action_goforward){

            if(webView.canGoForward()){
                webView.goForward();
            }

        }else if(id == R.id.action_openinbrowser){

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webView.getUrl()));
            if (browserIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(browserIntent);
            }

        }else if(id == R.id.action_refresh){

            webView.reload();
        }

        else if(id == R.id.action_share){
            // Share the news
            String newsUrl = webView.getUrl();
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, newsUrl);
            intent.setType("text/plain");
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    // To go back to previous page that is visited WHEN clicked on the BACK BUTTON
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if(keyCode == KeyEvent.KEYCODE_BACK){
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    finish();
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
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

        // Clear webView cache
        webView.removeAllViews();
        webView.clearCache(true);
        webView.clearHistory();
        webView = null;

        super.onDestroy();
    }

    // Saves the clicked notifications to shared preferences (to highlight them later on)
    private void saveClickedNotifications(String sourceKey, String newsUrl){
        SharedPreferences clickedColumns, clickedNews;
        clickedColumns = getSharedPreferences(getResources().getString(R.string.clicked_columns_key), Context.MODE_PRIVATE);
        clickedNews = getSharedPreferences(getResources().getString(R.string.clicked_news_key), Context.MODE_PRIVATE);
        if(sourceKey.contains("_gundem") || sourceKey.contains("_ekonomi") || sourceKey.contains("_spor")){
            clickedNews.edit().putLong(newsUrl, System.currentTimeMillis()).apply();
        } else {
            clickedColumns.edit().putLong(newsUrl, System.currentTimeMillis()).apply();
        }
    }
}
