package com.haberinadresi.androidapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdView;
import com.haberinadresi.androidapp.activities.NewsDetailActivity;
import com.haberinadresi.androidapp.R;
import com.haberinadresi.androidapp.activities.ShowInWebviewActivity;
import com.haberinadresi.androidapp.models.NewsItem;
import com.haberinadresi.androidapp.repository.FavNewsRepository;
import com.haberinadresi.androidapp.utilities.BackupNewsImages;
import com.haberinadresi.androidapp.utilities.GlideApp;
import com.google.gson.Gson;
import com.haberinadresi.androidapp.utilities.NetworkUtils;
import com.haberinadresi.androidapp.utilities.BackupLogosDrive;
import com.haberinadresi.androidapp.utilities.WebUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NewsAdapterWithAds extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<Object> newsList;
    private SharedPreferences clickedNews, savedNews, customKeys;
    private boolean displayOnlyInWifi;
    private static final int NEWS_ITEM_VIEW_TYPE = 0; //View type for news
    private static final int BANNER_AD_VIEW_TYPE = 1; //View type banner ad
    private static final int ITEMS_PER_AD = 13; // A banner ad is placed in every 6 + 13th position in the RecyclerView

    public NewsAdapterWithAds(Context context, List<Object> newsList) {
        this.context = context;
        this.newsList = newsList;
        clickedNews = context.getSharedPreferences(context.getResources().getString(R.string.clicked_news_key), Context.MODE_PRIVATE);
        savedNews = context.getSharedPreferences(context.getResources().getString(R.string.saved_news_key), Context.MODE_PRIVATE);
        customKeys = context.getSharedPreferences(context.getResources().getString(R.string.custom_keys), Context.MODE_PRIVATE);
        displayOnlyInWifi = NetworkUtils.displayOnlyInWifi(context); // Get the user's mobile data saving preference
    }

    private static class NewsViewHolder extends RecyclerView.ViewHolder {

        private TextView newsTitle;
        private ImageView newsImage;
        private TextView newsTime;
        private ImageView sourceLogo;
        private TextView sourceName;
        private ImageView moreVertical;
        private ConstraintLayout constraintLayout;

        private NewsViewHolder(View itemView) {

            super(itemView);

            newsTitle = itemView.findViewById(R.id.tv_news_title);
            newsImage = itemView.findViewById(R.id.iv_news_image);
            newsTime = itemView.findViewById(R.id.tv_news_time);
            sourceLogo = itemView.findViewById(R.id.iv_source_logo);
            sourceName = itemView.findViewById(R.id.tv_news_source);
            moreVertical = itemView.findViewById(R.id.iv_more_vertical);
            constraintLayout = itemView.findViewById(R.id.cl_news_item); // container that holds the whole news items above
        }
    }

    public static class AdViewHolder extends RecyclerView.ViewHolder {

        AdViewHolder(View view) {
            super(view);
        }
    }

    //Determines the view type for the given position.
    @Override
    public int getItemViewType(int position) {
        // if the remainder of (position+7)/13 operation is 0, it means it is banner Ad
        // want to show at 6th position (beginning from 0) and then on 19th, 32th etc..
        return ((position + 7) % ITEMS_PER_AD == 0) ? BANNER_AD_VIEW_TYPE: NEWS_ITEM_VIEW_TYPE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case NEWS_ITEM_VIEW_TYPE:
                // Inflate the news view type that user preferred
                int news_view_preference = customKeys.getInt(context.getResources().getString(R.string.news_item_view_preference), 0);
                View newsView;
                switch (news_view_preference){
                    case 0:
                        newsView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news_type1, parent, false);
                        break;
                    case 1:
                        newsView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news_type2, parent, false);
                        break;
                    case 2:
                        newsView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news_type3, parent, false);
                        break;
                   default:
                       newsView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news_type1, parent, false);
                }
                return new NewsViewHolder(newsView);
            case BANNER_AD_VIEW_TYPE:
                // fall through
            default:
                View bannerView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner_ad_box, parent, false);
                return new AdViewHolder(bannerView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // Alternative Context
        //final Context context = holder.itemView.getContext();

        // Get the viewtype (Banner Ad vs News)
        int viewType = getItemViewType(position);
        switch (viewType) {
            // If news type
            case NEWS_ITEM_VIEW_TYPE:
                final NewsViewHolder newsViewHolder = (NewsViewHolder) holder;
                final NewsItem newsItem = (NewsItem) newsList.get(position);

                // If mobile data usage is not restricted by user, then show images
                if(! displayOnlyInWifi){
                    //Show news image
                    newsViewHolder.newsImage.setVisibility(View.VISIBLE);
                    GlideApp.with(holder.itemView.getContext())
                            .load(newsItem.getImageUrl())
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .error(
                                // If failed to load news image, then load backup image in the Google Drive
                                GlideApp.with(holder.itemView.getContext())
                                        .load(BackupNewsImages.getLogoUrl(newsItem.getKey()))
                                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                        .error(R.drawable.placeholder_icon_landscape))
                            .centerCrop()
                            .dontAnimate()
                            .into(newsViewHolder.newsImage);

                // Hide the news image
                } else {
                    newsViewHolder.newsImage.setVisibility(View.GONE);
                }

                // Primary Source Logo
                GlideApp.with(holder.itemView.getContext())
                        .load(newsItem.getLogoUrl())
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .onlyRetrieveFromCache(displayOnlyInWifi)
                        .apply(RequestOptions.circleCropTransform())
                        .error(
                            // If failed to load primary logo, then use the secondary logo in the Google Drive
                            GlideApp.with(holder.itemView.getContext())
                                .load(BackupLogosDrive.getLogoUrl(newsItem.getKey()))
                                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                .apply(RequestOptions.circleCropTransform())
                                .error(R.drawable.placeholder_icon_round))
                        .into(newsViewHolder.sourceLogo);

                newsViewHolder.sourceName.setText(newsItem.getSource());
                newsViewHolder.newsTitle.setText(newsItem.getTitle());

                // News Relative Time Ago
                String relativeTime = DateUtils.getRelativeTimeSpanString(
                        newsItem.getUpdateTime(),
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS,
                        DateUtils.FORMAT_ABBREV_RELATIVE).toString();
                newsViewHolder.newsTime.setText(relativeTime);

                // Highlight the news that are clicked before
                if (clickedNews.contains(newsItem.getNewsUrl())){

                    newsViewHolder.newsTitle.setTextColor(context.getResources().getColor(R.color.colorSecondaryText));

                    final ColorMatrix grayscaleMatrix = new ColorMatrix();
                    grayscaleMatrix.setSaturation(0);
                    final ColorMatrixColorFilter filter = new ColorMatrixColorFilter(grayscaleMatrix);
                    newsViewHolder.newsImage.setColorFilter(filter);
                    newsViewHolder.sourceLogo.setColorFilter(filter);

                    // restore the color of unclicked items (necessary due to recyclerview logic)
                } else {
                    newsViewHolder.newsTitle.setTextColor(context.getResources().getColor(R.color.colorPrimaryText));

                    final ColorMatrix grayscaleMatrix = new ColorMatrix();
                    grayscaleMatrix.setSaturation(1);
                    final ColorMatrixColorFilter filter = new ColorMatrixColorFilter(grayscaleMatrix);
                    newsViewHolder.newsImage.setColorFilter(filter);
                    newsViewHolder.sourceLogo.setColorFilter(filter);
                }

                // Operations when a news item is clicked
                newsViewHolder.constraintLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // Save the clicked item's link to sharedpreference (to gray out it later on)
                        SharedPreferences.Editor editor = clickedNews.edit();
                        editor.putLong(newsItem.getNewsUrl(), System.currentTimeMillis());
                        editor.apply();

                        // If detail field is empty, open the link in the web browser (With CHROME CUSTOM TABS or WEBVIEW)
                        if ( newsItem.getDetail() == null || newsItem.getDetail().equals("") ) {
                            // If user preferred to open the link with browser, Open with Chrome Custom Tabs
                            if (customKeys.getBoolean(context.getResources().getString(R.string.open_with_browser_key), false)){
                                // Create chrome custom tabs in WebUtils class and open
                                CustomTabsIntent customTabsIntent = WebUtils
                                        .createChromeTab(context, newsItem.getNewsUrl());
                                customTabsIntent.launchUrl(context, Uri.parse(newsItem.getNewsUrl()));
                            // Otherwise open the news link in Webview
                            } else {
                                Intent intentDetail = new Intent(context, ShowInWebviewActivity.class);
                                intentDetail.putExtra(context.getResources().getString(R.string.news_url), newsItem.getNewsUrl());
                                intentDetail.putExtra(context.getResources().getString(R.string.news_source_for_display), newsItem.getSource());
                                intentDetail.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intentDetail);
                            }
                        // If there is valid detail info, open the newsDetailActivity
                        } else {
                            // Open the activity that shows the news' details, put the necessary items on it
                            Intent newsDetail = new Intent(context, NewsDetailActivity.class);
                            // Convert the clicked newsItem (object) into string
                            Gson gson = new Gson();
                            String clickedNews = gson.toJson(newsItem);
                            newsDetail.putExtra(context.getResources().getString(R.string.news_item), clickedNews);
                            // Generate the extra 3 news from the same source to be displayed at the bottom of NewsDetail Activity
                            List<NewsItem> extraNews = getExtraNews(newsList, newsItem.getKey());
                            if(extraNews != null){
                                newsDetail.putExtra(context.getResources().getString(R.string.extra_news), gson.toJson(extraNews));
                            }
                            newsDetail.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(newsDetail);
                        }

                        // tell the adapter that there is change in the clicked item (gray out)
                        notifyItemChanged(newsViewHolder.getAdapterPosition());

                        // Increment the click counter (used for displaying Interstitial Ad in Main Activity OnResume)
                        int counter = customKeys.getInt(context.getResources().getString(R.string.news_click_counter),0);
                        SharedPreferences.Editor counterEditor = customKeys.edit();
                        counterEditor.putInt(context.getResources().getString(R.string.news_click_counter), counter + 1);
                        counterEditor.apply();
                    }
                });

                // POPUP MENU OPERATIONS (Save & Share)
                newsViewHolder.moreVertical.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Create the Popup Menu
                        final PopupMenu popup = new PopupMenu(context, v);
                        popup.getMenuInflater().inflate(R.menu.news_more_menu, popup.getMenu());
                        // Set the title of Save News Menu item ( Haberi Kaydet / Kayıtlılardan Çıkar)
                        MenuItem saveMenuItem = popup.getMenu().getItem(0);
                        if (savedNews.contains(newsItem.getNewsUrl())) {
                            saveMenuItem.setTitle(context.getResources().getString(R.string.unsave_news_column));
                        } else {
                            saveMenuItem.setTitle(context.getResources().getString(R.string.save_news));
                        }
                        // Set the click operations
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {

                                // SAVE OPERATIONS
                                if(menuItem.getItemId() == R.id.more_save_news){
                                    // if news is already in the saved list
                                    if (savedNews.contains(newsItem.getNewsUrl())) {
                                        //Delete news from favorite news database
                                        FavNewsRepository repository = new FavNewsRepository(context);
                                        repository.delete(newsItem);
                                        // Show toast message
                                        Toast.makeText(context.getApplicationContext(), context.getResources().getString(R.string.news_deleted_from_favorite), Toast.LENGTH_SHORT).show();
                                        // update shared preference file
                                        savedNews.edit().remove(newsItem.getNewsUrl()).apply();

                                    // News is not saved before, so save it into database
                                    } else {
                                        //Add news to favorite news database
                                        FavNewsRepository repository = new FavNewsRepository(context);
                                        repository.insert(newsItem);
                                        // Show toast message
                                        Toast.makeText(context.getApplicationContext(), context.getResources().getString(R.string.news_added_to_favorite), Toast.LENGTH_SHORT).show();
                                        // update shared preference file
                                        savedNews.edit().putBoolean(newsItem.getNewsUrl(), true).apply();
                                    }

                                // SHARE OPERATIONS
                                } else if(menuItem.getItemId() == R.id.more_share_news){
                                    Intent intent = new Intent(Intent.ACTION_SEND);
                                    intent.putExtra(Intent.EXTRA_TEXT, newsItem.getNewsUrl());
                                    intent.setType("text/plain");
                                    context.startActivity(intent);
                                }
                                return true;
                            }
                        });
                        popup.show();
                    }
                });

                /*
                // Open the OneSourceNews activity if source logo is clicked (ŞİMDİLİK İPTAL)
                newsViewHolder.sourceLogo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String source = newsItem.getKey().split("_")[0];
                        String category = newsItem.getKey().split("_")[1];
                        Intent oneSourceNews = new Intent(context, OneSrcNewsActivity.class);
                        oneSourceNews.putExtra(context.getResources().getString(R.string.news_category), category);
                        oneSourceNews.putExtra(context.getResources().getString(R.string.news_source_for_display), newsItem.getSource());
                        oneSourceNews.putExtra(context.getResources().getString(R.string.news_source_for_query), source);
                        oneSourceNews.putExtra(context.getResources().getString(R.string.news_source_key), newsItem.getKey());

                        oneSourceNews.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(oneSourceNews);
                    }
                });
                */
                break;
            case BANNER_AD_VIEW_TYPE:
                // fall through
            default:
                AdViewHolder bannerHolder = (AdViewHolder) holder;
                AdView adView = (AdView) newsList.get(position);
                ViewGroup adCardView = (ViewGroup) bannerHolder.itemView;
                // The AdViewHolder recycled by the RecyclerView may be a different
                // instance than the one used previously for this position. Clear the
                // AdViewHolder of any subviews in case it has a different
                // AdView associated with it, and make sure the AdView for this position doesn't
                // already have a parent of a different recycled AdViewHolder.
                if (adCardView.getChildCount() > 0) {
                    adCardView.removeAllViews();
                }
                if (adView.getParent() != null) {
                    ((ViewGroup) adView.getParent()).removeView(adView);
                }

                // Add the banner ad to the ad view.
                adCardView.addView(adView);
        }
    }

    @Override
    public int getItemCount() {
        if (newsList != null){
            return newsList.size();
        } else {
            return 0;
        }
    }

    public void setNewsList(List<Object> newsList){
        this.newsList = newsList;
        notifyDataSetChanged();
    }

    public void clearNewsList(){
        newsList.clear();
        notifyDataSetChanged();
    }

    public List<Object> getNewsList(){
        if(newsList != null){
            return newsList;
        } else {
            return new ArrayList<>();
        }
    }

    // Generates the extra 3 news
    private List<NewsItem> getExtraNews(List<Object> newsObjects, String key){
        List<NewsItem> newsList = new ArrayList<>();
        // Iterate over all news + Ads to find news from the same source the user clicks
        for(Object item : newsObjects){
            if(item instanceof NewsItem){
                if(((NewsItem) item).getKey().equals(key)){
                    newsList.add((NewsItem) item);
                }
            }
        }
        // Reverse the list to get the oldest news
        Collections.reverse(newsList);
        // Decrease the number of news that returned to 3
        if(newsList.size() >= 3){
            List<NewsItem> finalList = newsList.subList(0,3);
            // Reverse the final list again to put the most recent news at the top
            Collections.reverse(finalList);
            return finalList;
        // If couldn't generated, return null
        } else {
            return null;
        }
    }
}
