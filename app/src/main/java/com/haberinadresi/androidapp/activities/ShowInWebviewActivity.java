package com.haberinadresi.androidapp.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.haberinadresi.androidapp.R;

public class ShowInWebviewActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar progressBar;
    private FrameLayout frameLayout;
    private String source;

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

        // Get the news url from the intent (NewsDetail Activity & News Adapter & Column Adapter)
        Intent intentUrl = getIntent();
        source = intentUrl.getStringExtra(getResources().getString(R.string.news_source_for_display));
        String newsUrl = intentUrl.getStringExtra(getResources().getString(R.string.news_url));

        // Start Webview
        webView.setWebViewClient(new MyWebViewClient());
        webView.setWebChromeClient(new MyChromeClient());

        webView.getSettings().setJavaScriptEnabled(true);
        //webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        //webView.getSettings().setBuiltInZoomControls(true);
        //webView.getSettings().setDisplayZoomControls(true);
        //webView.setVerticalScrollBarEnabled(false);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
        webView.loadUrl(newsUrl);

        progressBar.setProgress(0);
    }

    private class MyWebViewClient extends WebViewClient{
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            frameLayout.setVisibility(View.VISIBLE);
            return true;
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
        getMenuInflater().inflate(R.menu.activity_show_in_webview, menu);
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

}
