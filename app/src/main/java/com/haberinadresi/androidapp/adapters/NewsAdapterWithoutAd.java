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
import com.google.gson.Gson;
import com.haberinadresi.androidapp.R;
import com.haberinadresi.androidapp.activities.NewsDetailActivity;
import com.haberinadresi.androidapp.activities.ShowInWebviewActivity;
import com.haberinadresi.androidapp.models.NewsItem;
import com.haberinadresi.androidapp.repository.FavNewsRepository;
import com.haberinadresi.androidapp.utilities.BackupNewsImages;
import com.haberinadresi.androidapp.utilities.GlideApp;
import com.haberinadresi.androidapp.utilities.NetworkUtils;
import com.haberinadresi.androidapp.utilities.BackupLogosDrive;
import com.haberinadresi.androidapp.utilities.WebUtils;

import java.util.List;

public class NewsAdapterWithoutAd extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<NewsItem> newsList;
    private SharedPreferences clickedNews, savedNews, customKeys;
    private boolean displayOnlyInWifi;

    public NewsAdapterWithoutAd(Context context, List<NewsItem> newsList) {
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

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

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
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        final NewsViewHolder newsViewHolder = (NewsViewHolder) holder;
        final NewsItem newsItem = newsList.get(position);

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
                    // Convert the newsItem (object) into string
                    Gson gson = new Gson();
                    String json = gson.toJson(newsItem);
                    newsDetail.putExtra(context.getResources().getString(R.string.news_item), json);
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
                // Set the title of Save News Menu item ( Haberi Kaydet / Favorilerden Çıkar)
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
    }

    @Override
    public int getItemCount() {
        if (newsList != null){
            return newsList.size();
        } else {
            return 0;
        }
    }

    public void setNewsList(List<NewsItem> newsList){
        this.newsList = newsList;
        notifyDataSetChanged();
    }

    /*
    public List<NewsItem> getNewsList(){
        if(newsList != null){
            return newsList;
        } else {
            return new ArrayList<>();
        }
    }
    */

}
