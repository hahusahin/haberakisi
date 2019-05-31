package com.haberinadresi.androidapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.haberinadresi.androidapp.R;
import com.haberinadresi.androidapp.activities.NewsDetailActivity;
import com.haberinadresi.androidapp.activities.ShowInWebviewActivity;
import com.haberinadresi.androidapp.models.NewsItem;
import com.haberinadresi.androidapp.repository.FavNewsRepository;
import com.haberinadresi.androidapp.utilities.GlideApp;
import com.google.gson.Gson;
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
        private TextView newsSummary;
        private ImageView newsImage;
        private TextView newsTime;
        private ImageView sourceLogo;
        private TextView sourceName;
        private ImageView shareNews;
        private ImageView saveNews;
        private ConstraintLayout constraintLayout;


        private NewsViewHolder(View itemView) {

            super(itemView);

            newsTitle = itemView.findViewById(R.id.tv_news_title);
            newsSummary = itemView.findViewById(R.id.tv_news_summary);
            newsImage = itemView.findViewById(R.id.iv_news_image);
            newsTime = itemView.findViewById(R.id.tv_news_time);
            sourceLogo = itemView.findViewById(R.id.iv_source_logo);
            sourceName = itemView.findViewById(R.id.tv_news_source);
            shareNews = itemView.findViewById(R.id.iv_share_news);
            saveNews = itemView.findViewById(R.id.iv_save_news);
            constraintLayout = itemView.findViewById(R.id.cl_news_item); // container that holds the whole news items above
        }
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news_normal, parent,false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final NewsViewHolder newsViewHolder, int position) {

        final NewsItem newsItem = newsList.get(position);

        GlideApp.with(newsViewHolder.itemView.getContext())
                .load(newsItem.getImageUrl())
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .onlyRetrieveFromCache(displayOnlyInWifi)
                .error(R.drawable.placeholder_icon_landscape)
                .into(newsViewHolder.newsImage);

        GlideApp.with(newsViewHolder.itemView.getContext())
                .load(newsItem.getLogoUrl())
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .onlyRetrieveFromCache(displayOnlyInWifi)
                .error(R.drawable.placeholder_icon_square)
                .apply(RequestOptions.circleCropTransform())
                .into(newsViewHolder.sourceLogo);

        newsViewHolder.sourceName.setText(newsItem.getSource());
        newsViewHolder.newsTitle.setText(newsItem.getTitle());
        newsViewHolder.newsSummary.setText(newsItem.getSummary());

        // If summary is empty, set the visibility of summary area to GONE, to align Title center vertical wrt the image.
        if(! newsItem.getSummary().equals("")){
            newsViewHolder.newsSummary.setVisibility(View.VISIBLE);
        } else {
            newsViewHolder.newsSummary.setVisibility(View.GONE);
        }

        newsViewHolder.saveNews.setImageResource(R.drawable.ic_remove_news);

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

                // tell the adapter that there is change in the clicked item
                notifyItemChanged(newsViewHolder.getAdapterPosition());
            }
        });

        // Delete news from Room Database when clicked on the image
        newsViewHolder.saveNews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            //Delete news from favorite news database
            FavNewsRepository repository = new FavNewsRepository(context);
            repository.delete(newsItem);
            // Show toast message
            Toast.makeText(context.getApplicationContext(), context.getResources().getString(R.string.news_deleted_from_favorite), Toast.LENGTH_SHORT).show();
            // update shared preference file
            SharedPreferences.Editor editor = savedNews.edit();
            editor.remove(newsItem.getNewsUrl());
            editor.apply();
            }
        });

        // Share the news
        newsViewHolder.shareNews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, newsItem.getNewsUrl());
                intent.setType("text/plain");
                context.startActivity(intent);
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
