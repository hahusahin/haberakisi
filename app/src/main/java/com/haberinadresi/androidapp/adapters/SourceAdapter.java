package com.haberinadresi.androidapp.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.haberinadresi.androidapp.models.SourceItem;
import com.haberinadresi.androidapp.R;
import com.haberinadresi.androidapp.utilities.GlideApp;
import com.haberinadresi.androidapp.utilities.NetworkUtils;

import java.util.ArrayList;
import java.util.List;


public class SourceAdapter extends RecyclerView.Adapter<SourceAdapter.SourceViewHolder> {

    private Context context;
    private List<SourceItem> sourceList;
    private SharedPreferences newsSources, notificationSources;
    private boolean displayOnlyInWifi;

    public SourceAdapter(Context context, List<SourceItem> sourceList) {
        this.context = context;
        this.sourceList = sourceList;
        newsSources = context.getSharedPreferences(context.getResources().getString(R.string.source_prefs_key),Context.MODE_PRIVATE);
        notificationSources = context.getSharedPreferences(context.getResources().getString(R.string.notification_prefs_key),Context.MODE_PRIVATE);
        displayOnlyInWifi = NetworkUtils.displayOnlyInWifi(context); // Get the user's mobile data saving preference
    }

    static class SourceViewHolder extends RecyclerView.ViewHolder {

        private TextView sourceName;
        private ImageView sourceLogo;
        private ImageView sourceSelector;
        private RelativeLayout relativeLayout;

        private SourceViewHolder(View itemView) {

            super(itemView);

            sourceName = itemView.findViewById(R.id.tv_source_name);
            sourceLogo = itemView.findViewById(R.id.iv_source_square);
            sourceSelector = itemView.findViewById(R.id.iv_source_selector);
            relativeLayout = itemView.findViewById(R.id.layout_source_item);
        }
    }

    @NonNull
    @Override
    public SourceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_source, parent,false);
        return new SourceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final SourceViewHolder viewHolder, int position) {

        final SourceItem sourceItem = sourceList.get(position);
        final String category = sourceItem.getCategory();

        viewHolder.sourceName.setText(sourceItem.getSourceName());

        // YEDEK LOGOYU GÖSTER DEMEDİM (HANGİLERİ ÇALIŞMIYOR GÖRMEK İÇİN)
        GlideApp.with(viewHolder.itemView.getContext())
                .load(sourceItem.getLogoUrl())
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .onlyRetrieveFromCache(displayOnlyInWifi)
                .error(R.drawable.app_icon_square)
                .into(viewHolder.sourceLogo);

        // PUT THE CORRECT IMAGE WRT THE USER'S PAST PREFERENCES (CHECKED OR NOT CHECKED)
        if(category.equals(context.getResources().getString(R.string.bildirim_key))){
            if (notificationSources.contains(sourceItem.getKey())){
                viewHolder.sourceSelector.setImageResource(R.mipmap.ic_remove_circle);
            } else {
                viewHolder.sourceSelector.setImageResource(R.mipmap.ic_add_circle);
            }
        }
        else {
            if (newsSources.contains(sourceItem.getKey())){
                viewHolder.sourceSelector.setImageResource(R.mipmap.ic_remove_circle);
            } else {
                viewHolder.sourceSelector.setImageResource(R.mipmap.ic_add_circle);
            }
        }
        viewHolder.relativeLayout.setOnClickListener(v -> {

            // CHANGE SHARED PREFERENCES
            SharedPreferences.Editor newsEditor = newsSources.edit();
            SharedPreferences.Editor notificationEditor = notificationSources.edit();

            // If category is BİLDİRİM, change bildirim source prefs
            if(category.equals(context.getResources().getString(R.string.bildirim_key))){
                // check the current preference (if exists then remove it, if not exists then add it)
                if(notificationSources.contains(sourceItem.getKey())){
                    notificationEditor.remove(sourceItem.getKey());
                } else {
                    Gson gson = new Gson();
                    String json = gson.toJson(sourceItem);
                    notificationEditor.putString(sourceItem.getKey(), json);
                }
                notificationEditor.apply();
            }
            // If category is GÜNDEM, SPOR..., change mysources prefs
            else {
                // check the current preference (if exists then remove it, if not exists then add it)
                if(newsSources.contains(sourceItem.getKey())){
                    newsEditor.remove(sourceItem.getKey());
                } else {
                    Gson gson = new Gson();
                    String json = gson.toJson(sourceItem);
                    newsEditor.putString(sourceItem.getKey(), json);
                }
                newsEditor.apply();
            }

            // CHANGE THE PLUS MINUS IMAGE
            if(category.equals(context.getResources().getString(R.string.bildirim_key))){
                if(notificationSources.contains(sourceItem.getKey())){
                    viewHolder.sourceSelector.setImageResource(R.mipmap.ic_remove_circle);
                    Toast.makeText(context.getApplicationContext(), sourceItem.getSourceName() + " " + context.getResources().getString(R.string.added_to_favorite), Toast.LENGTH_SHORT).show();
                } else {
                    viewHolder.sourceSelector.setImageResource(R.mipmap.ic_add_circle);
                    Toast.makeText(context.getApplicationContext(), sourceItem.getSourceName() + " " + context.getResources().getString(R.string.deleted_from_favorite), Toast.LENGTH_SHORT).show();
                }
            }
            else {
                if(newsSources.contains(sourceItem.getKey())){
                    viewHolder.sourceSelector.setImageResource(R.mipmap.ic_remove_circle);
                    Toast.makeText(context.getApplicationContext(), sourceItem.getSourceName() + " " + context.getResources().getString(R.string.added_to_favorite), Toast.LENGTH_SHORT).show();
                } else {
                    viewHolder.sourceSelector.setImageResource(R.mipmap.ic_add_circle);
                    Toast.makeText(context.getApplicationContext(), sourceItem.getSourceName() + " " + context.getResources().getString(R.string.deleted_from_favorite), Toast.LENGTH_SHORT).show();
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

    public void setSourceList(List<SourceItem> newList){
        sourceList = new ArrayList<>();
        sourceList.addAll(newList);
        notifyDataSetChanged();
    }

}
