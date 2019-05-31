package com.haberinadresi.androidapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.haberinadresi.androidapp.R;
import com.haberinadresi.androidapp.activities.ShowInWebviewActivity;
import com.haberinadresi.androidapp.models.SourceItem;
import com.haberinadresi.androidapp.utilities.GlideApp;
import com.haberinadresi.androidapp.utilities.SourceLogos;
import com.haberinadresi.androidapp.utilities.WebUtils;

import java.util.List;

public class MyWebsitesAdapter extends RecyclerView.Adapter<MyWebsitesAdapter.SourceViewHolder> {

    private Context context;
    private List<SourceItem> sourceList;
    private SharedPreferences customKeys;

    public MyWebsitesAdapter(Context context, List<SourceItem> sourceList) {
        this.context = context;
        this.sourceList = sourceList;
        customKeys = context.getSharedPreferences(context.getResources().getString(R.string.custom_keys), Context.MODE_PRIVATE);
    }

    static class SourceViewHolder extends RecyclerView.ViewHolder {

        private ImageView sourceLogo;
        private TextView sourceName;
        private LinearLayout linearLayout;

        private SourceViewHolder(View itemView){

            super(itemView);

            sourceLogo = itemView.findViewById(R.id.iv_my_source);
            sourceName = itemView.findViewById(R.id.tv_my_source);
            linearLayout = itemView.findViewById(R.id.my_source_item);

        }
    }

    @NonNull
    @Override
    public SourceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_sources, parent,false);
        return new SourceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final SourceViewHolder viewHolder, int position) {

        final SourceItem sourceItem = sourceList.get(position);

        viewHolder.sourceName.setText(sourceItem.getSourceName());

        // First try loading primary logo
        GlideApp.with(viewHolder.itemView.getContext())
            .load(sourceItem.getLogoUrl())
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .apply(RequestOptions.circleCropTransform())
            .error(
                    // If failed to load primary logo, then use the secondary logo in the Google Drive
                    GlideApp.with(viewHolder.itemView.getContext())
                        .load(SourceLogos.getLogoUrl(sourceItem.getKey()))
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .apply(RequestOptions.circleCropTransform())
                        .error(R.drawable.placeholder_icon_round))
            .into(viewHolder.sourceLogo);


        viewHolder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Increment the click counter (used for displaying Interstitial Ad in Main Activity OnResume)
                int counter = customKeys.getInt(context.getResources().getString(R.string.news_click_counter),0);
                SharedPreferences.Editor counterEditor = customKeys.edit();
                counterEditor.putInt(context.getResources().getString(R.string.news_click_counter), counter + 1);
                counterEditor.apply();

                // Open the news website (With CHROME CUSTOM TABS or WEBVIEW)
                // If user preferred to open the link with browser, Open with Chrome Custom Tabs
                if (customKeys.getBoolean(context.getResources().getString(R.string.open_with_browser_key), false)){
                    // Create chrome custom tabs in WebUtils class and open
                    CustomTabsIntent customTabsIntent = WebUtils
                            .createChromeTab(context, sourceItem.getSourceKey());
                    customTabsIntent.launchUrl(context, Uri.parse(sourceItem.getSourceKey()));
                // Otherwise open the news link in Webview
                } else {
                    Intent intentUrl = new Intent(context, ShowInWebviewActivity.class);
                    intentUrl.putExtra(context.getResources().getString(R.string.news_url), sourceItem.getSourceKey());
                    intentUrl.putExtra(context.getResources().getString(R.string.news_source_for_display), sourceItem.getSourceName());
                    //intentUrl.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intentUrl);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        if (sourceList != null){
            return sourceList.size();
        } else {
            return 0;
        }
    }

}
