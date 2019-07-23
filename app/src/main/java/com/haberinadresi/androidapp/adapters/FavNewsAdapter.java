package com.haberinadresi.androidapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.haberinadresi.androidapp.R;
import com.haberinadresi.androidapp.activities.ShowInWebviewActivity;
import com.haberinadresi.androidapp.models.NewsItem;
import com.haberinadresi.androidapp.repository.FavNewsRepository;
import com.haberinadresi.androidapp.utilities.GlideApp;
import com.haberinadresi.androidapp.utilities.NetworkUtils;
import com.haberinadresi.androidapp.utilities.WebUtils;

import java.util.List;

public class FavNewsAdapter extends RecyclerView.Adapter<FavNewsAdapter.NewsViewHolder> {

    private Context context;
    private List<NewsItem> newsList;
    private SharedPreferences savedNews, customKeys;
    private boolean displayOnlyInWifi;

    public FavNewsAdapter(Context context, List<NewsItem> newsList) {
        this.context = context;
        this.newsList = newsList;
        savedNews = context.getSharedPreferences(context.getResources().getString(R.string.saved_news_key), Context.MODE_PRIVATE);
        customKeys = context.getSharedPreferences(context.getResources().getString(R.string.custom_keys), Context.MODE_PRIVATE);
        displayOnlyInWifi = NetworkUtils.displayOnlyInWifi(context); // Get the user's mobile data saving preference
    }

    static class NewsViewHolder extends RecyclerView.ViewHolder {

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
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

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
    public void onBindViewHolder(@NonNull final NewsViewHolder newsViewHolder, int position) {

        final NewsItem newsItem = newsList.get(position);

        // News Image
        GlideApp.with(newsViewHolder.itemView.getContext())
                .load(newsItem.getImageUrl())
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .onlyRetrieveFromCache(displayOnlyInWifi)
                .error(R.drawable.placeholder_icon_landscape)
                .into(newsViewHolder.newsImage);

        // Source Logo
        GlideApp.with(newsViewHolder.itemView.getContext())
                .load(newsItem.getLogoUrl())
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .onlyRetrieveFromCache(displayOnlyInWifi)
                .error(R.drawable.placeholder_icon_square)
                .apply(RequestOptions.circleCropTransform())
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

        newsViewHolder.constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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

                // tell the adapter that there is change in the clicked item
                notifyItemChanged(newsViewHolder.getAdapterPosition());
            }
        });

        // POPUP MENU OPERATIONS (Delete & Share)
        newsViewHolder.moreVertical.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create the Popup Menu
                final PopupMenu popup = new PopupMenu(context, v);
                popup.getMenuInflater().inflate(R.menu.news_more_menu, popup.getMenu());
                // Set the title of Save News Menu item (Favorilerden Çıkar)
                MenuItem saveMenuItem = popup.getMenu().getItem(0);
                saveMenuItem.setTitle(context.getResources().getString(R.string.unsave_news_column));

                // Set the click operations
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        // DELETE OPERATIONS
                        if(menuItem.getItemId() == R.id.more_save_news){
                            //Delete news from favorite news database
                            FavNewsRepository repository = new FavNewsRepository(context);
                            repository.delete(newsItem);
                            // Show toast message
                            Toast.makeText(context.getApplicationContext(), context.getResources().getString(R.string.news_deleted_from_favorite), Toast.LENGTH_SHORT).show();
                            // update shared preference file
                            savedNews.edit().remove(newsItem.getNewsUrl()).apply();

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

}
