package com.haberinadresi.androidapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.haberinadresi.androidapp.R;
import com.haberinadresi.androidapp.activities.ShowHeadlinesActivity;
import com.haberinadresi.androidapp.models.NewsItem;
import com.haberinadresi.androidapp.utilities.GlideApp;
import com.haberinadresi.androidapp.utilities.NetworkUtils;

import java.util.ArrayList;
import java.util.List;

public class HeadlineAdapter extends RecyclerView.Adapter<HeadlineAdapter.MansetViewHolder> {

    private Context context;
    private List<NewsItem> headlineList;
    private boolean displayOnlyInWifi;

    public HeadlineAdapter(Context context, List<NewsItem> headlineList) {
        this.context = context;
        this.headlineList = headlineList;
        displayOnlyInWifi = NetworkUtils.displayOnlyInWifi(context); // Get the user's mobile data saving preference
    }

    static class MansetViewHolder extends RecyclerView.ViewHolder {

        private ImageView mansetImage;
        private TextView mansetSource;
        private LinearLayout linearLayout;

        private MansetViewHolder(View itemView) {

            super(itemView);

            mansetImage = itemView.findViewById(R.id.iv_manset);
            mansetSource = itemView.findViewById(R.id.tv_manset_source);
            linearLayout = itemView.findViewById(R.id.layout_manset_item);
        }
    }

    @NonNull
    @Override
    public MansetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_headline, parent,false);
        return new MansetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MansetViewHolder mansetViewHolder, int position) {

        final NewsItem mansetItem = headlineList.get(position);

        // Load the thumbnail of the headline to recyclerview
        GlideApp.with(mansetViewHolder.itemView.getContext())
            .load(mansetItem.getNewsUrl())
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .onlyRetrieveFromCache(displayOnlyInWifi)
            .error(R.drawable.placeholder_icon_portrait)
            .into(mansetViewHolder.mansetImage);

        mansetViewHolder.mansetSource.setText(mansetItem.getSource());

        // Open the headline in fullscreen mode (load the big image)
        mansetViewHolder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Write the fullsize image url's of headlines to string arraylist
                // And get the clicked items position
                ArrayList<String> headlineUrls = new ArrayList<>();
                int clickedHeadlinePosition = -1;
                for(NewsItem item : headlineList){
                    if(item.getImageUrl().equals(mansetItem.getImageUrl())){
                        clickedHeadlinePosition = headlineList.indexOf(item);
                    }
                    headlineUrls.add(item.getImageUrl());
                }
                // Pass these values to activity
                Intent fullScreenIntent = new Intent(context, ShowHeadlinesActivity.class);
                fullScreenIntent.putStringArrayListExtra(context.getResources().getString(R.string.headline_list), headlineUrls);
                fullScreenIntent.putExtra(context.getResources().getString(R.string.headline_position), clickedHeadlinePosition);
                context.startActivity(fullScreenIntent);

            }
        });

    }

    @Override
    public int getItemCount() {
        if (headlineList != null){
            return headlineList.size();
        } else {
            return 0;
        }
    }

}
