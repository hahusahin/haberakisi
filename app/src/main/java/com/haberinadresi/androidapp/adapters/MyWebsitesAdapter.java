package com.haberinadresi.androidapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
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
import com.haberinadresi.androidapp.utilities.BackupLogosDrive;

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
                        .load(BackupLogosDrive.getLogoUrl(sourceItem.getKey()))
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .apply(RequestOptions.circleCropTransform())
                        .error(R.drawable.placeholder_icon_round))
            .into(viewHolder.sourceLogo);


        viewHolder.linearLayout.setOnClickListener(view -> {

            // Open the link with Webview
            Intent intentUrl = new Intent(context, ShowInWebviewActivity.class);
            intentUrl.putExtra(context.getResources().getString(R.string.news_url), sourceItem.getSourceKey());
            intentUrl.putExtra(context.getResources().getString(R.string.news_source_for_display), sourceItem.getSourceName());
            intentUrl.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intentUrl);

            // Increment the click counter (used for displaying Interstitial Ad in Main Activity OnResume)
            int counter = customKeys.getInt(context.getResources().getString(R.string.news_click_counter),0);
            customKeys.edit().putInt(context.getResources().getString(R.string.news_click_counter), counter + 1).apply();
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
