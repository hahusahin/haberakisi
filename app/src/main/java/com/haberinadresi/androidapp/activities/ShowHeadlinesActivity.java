package com.haberinadresi.androidapp.activities;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.haberinadresi.androidapp.R;
import com.github.chrisbanes.photoview.PhotoView;
import com.haberinadresi.androidapp.utilities.GlideApp;
import com.haberinadresi.androidapp.utilities.NetworkUtils;

import java.util.ArrayList;

public class ShowHeadlinesActivity extends AppCompatActivity {

    private int headlinePosition;
    private ArrayList<String> headlineList;
    PhotoView fullScreenImageView;
    boolean displayOnlyInWifi = false;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_headlines);

        final Toolbar toolbar = findViewById(R.id.fullScreen_toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getResources().getString(R.string.manset_normal));
        }

        progressBar = findViewById(R.id.pb_fullscreen_image);

        fullScreenImageView = findViewById(R.id.fullScreenImageView);
        fullScreenImageView.setScaleType(ImageView.ScaleType.FIT_XY);

        // Get the whole list and the clicked items position
        headlineList = getIntent().getStringArrayListExtra(getResources().getString(R.string.headline_list));
        headlinePosition = getIntent().getIntExtra(getResources().getString(R.string.headline_position), -1);

        if(headlineList != null && headlinePosition >= 0) {
            // Get the user's mobile data saving preference
            displayOnlyInWifi = NetworkUtils.displayOnlyInWifi(this);
            // Load the related headline image
            loadHeadline();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_headline_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home){

            onBackPressed();

        // Load the previous headline
        } else if(id == R.id.prev_image){
            // if the current headline is not the first one
            if(headlinePosition > 0){
                headlinePosition --;
                loadHeadline();
            }

        // Load the next headline
        } else if(id == R.id.next_image){
            // if the current headline is not the last one
            if(headlinePosition < headlineList.size() - 1){
                headlinePosition ++;
                loadHeadline();
            }

        }

        return super.onOptionsItemSelected(item);
    }


    public void loadHeadline(){

        progressBar.setVisibility(View.VISIBLE);

        GlideApp.with(ShowHeadlinesActivity.this)
                .load(headlineList.get(headlinePosition))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .onlyRetrieveFromCache(displayOnlyInWifi)
                .error(R.drawable.placeholder_icon_portrait)
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
                .into(fullScreenImageView);

    }
}

