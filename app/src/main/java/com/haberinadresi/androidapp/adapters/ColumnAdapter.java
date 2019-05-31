package com.haberinadresi.androidapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
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

public class ColumnAdapter extends RecyclerView.Adapter<ColumnAdapter.ColumnViewHolder> {

    private Context context;
    private List<Columnist> columnList;
    private SharedPreferences clickedColumns, savedColumns, customKeys;
    private boolean displayOnlyInWifi;

    public ColumnAdapter(Context context, List<Columnist> columnList) {
        this.context = context;
        this.columnList = columnList;
        clickedColumns = context.getSharedPreferences(context.getResources().getString(R.string.clicked_columns_key), Context.MODE_PRIVATE);
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

        // If column is at most 2 days old, write "BUGÜN, DÜN" instead of the long date string
        String relativeTime = DateUtils.getRelativeTimeSpanString(
                columnItem.getTime(), System.currentTimeMillis(), DateUtils.DAY_IN_MILLIS).toString();
        if(System.currentTimeMillis() - columnItem.getTime() < 48*60*60*1000L) {
            columnViewHolder.columnDay.setText(relativeTime);
        }else {
            columnViewHolder.columnDay.setText(columnItem.getDay());
        }

        // Put the correct image wrt the news state (saved/not saved) when it is first attached
        if (savedColumns.getBoolean(columnItem.getColumnUrl(),false)) {
            columnViewHolder.saveColumn.setImageResource(R.drawable.ic_remove_news);
        } else {
            columnViewHolder.saveColumn.setImageResource(R.drawable.ic_save_news);
        }

        // Highlight the news that are clicked before
        if (clickedColumns.getLong(columnItem.getColumnUrl(),0L) > 0L){

            columnViewHolder.columnTitle.setTextColor(context.getResources().getColor(R.color.colorSecondaryText));
            columnViewHolder.columnistName.setTextColor(context.getResources().getColor(R.color.colorSecondaryText));

            final ColorMatrix grayscaleMatrix = new ColorMatrix();
            grayscaleMatrix.setSaturation(0);
            final ColorMatrixColorFilter filter = new ColorMatrixColorFilter(grayscaleMatrix);
            columnViewHolder.columnistImage.setColorFilter(filter);

        // restore the color of unclicked items (necessary due to recyclerview logic)
        } else {
            columnViewHolder.columnTitle.setTextColor(context.getResources().getColor(R.color.colorPrimaryText));
            columnViewHolder.columnistName.setTextColor(context.getResources().getColor(R.color.colorPrimaryText));

            final ColorMatrix grayscaleMatrix = new ColorMatrix();
            grayscaleMatrix.setSaturation(1);
            final ColorMatrixColorFilter filter = new ColorMatrixColorFilter(grayscaleMatrix);
            columnViewHolder.columnistImage.setColorFilter(filter);
        }

        columnViewHolder.constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Save the clicked item's link to sharedpreference (to gray out it later on)
                clickedColumns.edit().putLong(columnItem.getColumnUrl(), System.currentTimeMillis()).apply();

                // Open the news link on the webpage (With CHROME CUSTOM TABS or WEBVIEW)
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
                    intentUrl.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intentUrl);
                }

                // tell the adapter that there is change in the clicked item
                notifyItemChanged(columnViewHolder.getAdapterPosition());

                // Increment the click counter (used for displaying Interstitial Ad in Main Activity OnResume)
                int counter = customKeys.getInt(context.getResources().getString(R.string.news_click_counter),0);
                customKeys.edit().putInt(context.getResources().getString(R.string.news_click_counter), counter + 1).apply();

            }
        });

        ///// OPERATIONS FOR SHARING AND SAVING OF COLUMN
        columnViewHolder.saveColumn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            // if column is already in the saved list
            if (savedColumns.getBoolean(columnItem.getColumnUrl(),false)) {
                // change the image
                columnViewHolder.saveColumn.setImageResource(R.drawable.ic_save_news);
                //Delete column from favorite columns database
                FavColumnsRepository repository = new FavColumnsRepository(context);
                repository.delete(columnItem);
                // Show toast message
                Toast.makeText(context.getApplicationContext(), context.getResources().getString(R.string.column_deleted_from_favorite), Toast.LENGTH_SHORT).show();
                // update shared preference file
                SharedPreferences.Editor editor = savedColumns.edit();
                editor.remove(columnItem.getColumnUrl());
                editor.apply();

            // Column is not saved before, so save it into database
            } else {
                // change the image
                columnViewHolder.saveColumn.setImageResource(R.drawable.ic_remove_news);
                //Add column to favorite columns database
                FavColumnsRepository repository = new FavColumnsRepository(context);
                repository.insert(columnItem);
                // Show toast message
                Toast.makeText(context.getApplicationContext(), context.getResources().getString(R.string.column_added_to_favorite), Toast.LENGTH_SHORT).show();
                // update shared preference file
                SharedPreferences.Editor editor = savedColumns.edit();
                editor.putBoolean(columnItem.getColumnUrl() ,true);
                editor.apply();
            }
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

    public void setColumnList(List<Columnist> columns){
        this.columnList = columns;
        notifyDataSetChanged();
    }

    public void clearColumnList(){
        columnList.clear();
        notifyDataSetChanged();
    }

}
