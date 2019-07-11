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
import com.haberinadresi.androidapp.R;
import com.haberinadresi.androidapp.activities.ShowInWebviewActivity;
import com.haberinadresi.androidapp.models.Columnist;
import com.haberinadresi.androidapp.repository.FavColumnsRepository;
import com.haberinadresi.androidapp.utilities.GlideApp;
import com.haberinadresi.androidapp.utilities.NetworkUtils;
import com.haberinadresi.androidapp.utilities.WebUtils;

import java.util.List;

public class FavColumnsAdapter extends RecyclerView.Adapter<FavColumnsAdapter.ColumnViewHolder> {

    private Context context;
    private List<Columnist> columnList;
    private SharedPreferences savedColumns, customKeys;
    private boolean displayOnlyInWifi;

    public FavColumnsAdapter(Context context, List<Columnist> columnList) {
        this.context = context;
        this.columnList = columnList;
        savedColumns = context.getSharedPreferences(context.getResources().getString(R.string.saved_columns_key), Context.MODE_PRIVATE);
        customKeys = context.getSharedPreferences(context.getResources().getString(R.string.custom_keys), Context.MODE_PRIVATE);
        displayOnlyInWifi = NetworkUtils.displayOnlyInWifi(context); // Get the user's mobile data saving preference
    }

    static class ColumnViewHolder extends RecyclerView.ViewHolder {

        private TextView columnTitle;
        private TextView columnistName;
        private ImageView columnistImage;
        private TextView sourceName;
        private TextView columnDay;
        private ImageView shareColumn;
        private ImageView saveColumn;
        private ConstraintLayout constraintLayout;

        private ColumnViewHolder(View itemView) {

            super(itemView);

            columnTitle = itemView.findViewById(R.id.tv_column_title);
            columnistName = itemView.findViewById(R.id.tv_columnist_name);
            columnistImage = itemView.findViewById(R.id.iv_columnist_image);
            columnDay = itemView.findViewById(R.id.tv_column_day);
            sourceName = itemView.findViewById(R.id.tv_col_source_name);
            saveColumn = itemView.findViewById(R.id.iv_save_column);
            shareColumn = itemView.findViewById(R.id.iv_share_column);
            constraintLayout = itemView.findViewById(R.id.layout_column_item);
        }
    }

    @NonNull
    @Override
    public ColumnViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_column, parent,false);
        return new ColumnViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ColumnViewHolder columnViewHolder, int position) {

        final Columnist columnItem = columnList.get(position);

        // Columnist's image
        GlideApp.with(columnViewHolder.itemView.getContext())
                .load(columnItem.getImageUrl())
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .onlyRetrieveFromCache(displayOnlyInWifi)
                .error(R.drawable.placeholder_columnist)
                .into(columnViewHolder.columnistImage);

        columnViewHolder.columnTitle.setText(columnItem.getTitle());
        columnViewHolder.columnistName.setText(columnItem.getName());
        columnViewHolder.sourceName.setText(columnItem.getSource());

        // Write relative time instead of real time (bugün, dün, 3 gün önce ...)
        String relativeTime = DateUtils.getRelativeTimeSpanString(
                columnItem.getTime(), System.currentTimeMillis(), DateUtils.DAY_IN_MILLIS).toString();
        columnViewHolder.columnDay.setText(relativeTime);

        columnViewHolder.saveColumn.setImageResource(R.drawable.ic_remove_news);

        columnViewHolder.constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Open the column link on the webpage (With CHROME CUSTOM TABS or WEBVIEW)
                // If user preferred to open the link with browser, Open with Chrome Custom Tabs
                if (customKeys.getBoolean(context.getResources().getString(R.string.open_with_browser_key), false)){
                    // Create chrome custom tabs in WebUtils class and open
                    CustomTabsIntent customTabsIntent = WebUtils
                            .createChromeTab(context, columnItem.getColumnUrl());
                    customTabsIntent.launchUrl(context, Uri.parse(columnItem.getColumnUrl()));
                // Open the news link in Webview
                } else {
                    Intent intentUrl = new Intent(context, ShowInWebviewActivity.class);
                    intentUrl.putExtra(context.getResources().getString(R.string.news_url), columnItem.getColumnUrl());
                    intentUrl.putExtra(context.getResources().getString(R.string.news_source_for_display), columnItem.getName());
                    context.startActivity(intentUrl);
                }
            }
        });

        ///// OPERATIONS FOR SHARING AND DELETING OF COLUMN
        columnViewHolder.saveColumn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Delete column from favorite columns database
                FavColumnsRepository repository = new FavColumnsRepository(context);
                repository.delete(columnItem);
                // Show toast message
                Toast.makeText(context.getApplicationContext(), context.getResources().getString(R.string.column_deleted_from_favorite), Toast.LENGTH_SHORT).show();
                // update shared preference file
                SharedPreferences.Editor editor = savedColumns.edit();
                editor.remove(columnItem.getColumnUrl());
                editor.apply();
            }
        });

        columnViewHolder.shareColumn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, columnItem.getColumnUrl());
                intent.setType("text/plain");
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        if (columnList != null){
            return columnList.size();
        } else {
            return 0;
        }
    }

}
