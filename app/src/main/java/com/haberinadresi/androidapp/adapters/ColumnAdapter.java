package com.haberinadresi.androidapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import com.haberinadresi.androidapp.R;
import com.haberinadresi.androidapp.activities.ShowInWebviewActivity;
import com.haberinadresi.androidapp.models.Columnist;
import com.haberinadresi.androidapp.repository.FavColumnsRepository;
import com.haberinadresi.androidapp.utilities.GlideApp;
import com.haberinadresi.androidapp.utilities.NetworkUtils;

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
        private ImageView moreVertical;
        private ConstraintLayout constraintLayout;

        private ColumnViewHolder(View itemView) {

            super(itemView);

            columnTitle = itemView.findViewById(R.id.tv_column_title);
            columnistName = itemView.findViewById(R.id.tv_columnist_name);
            columnistImage = itemView.findViewById(R.id.iv_columnist_image);
            columnDay = itemView.findViewById(R.id.tv_column_day);
            sourceName = itemView.findViewById(R.id.tv_col_source_name);
            moreVertical = itemView.findViewById(R.id.iv_more_vertical);
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
            .fitCenter()
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .onlyRetrieveFromCache(displayOnlyInWifi)
            .error(R.drawable.placeholder_columnist)
            .into(columnViewHolder.columnistImage);

        columnViewHolder.columnTitle.setText(columnItem.getTitle());
        columnViewHolder.columnistName.setText(columnItem.getName());
        columnViewHolder.sourceName.setText(columnItem.getSource());

        // Write relative time instead of real time (bugün, dün, 3 gün önce ...)
        String relativeTime = DateUtils.getRelativeTimeSpanString(
                columnItem.getTime(),
                System.currentTimeMillis(),
                DateUtils.DAY_IN_MILLIS).toString();
        columnViewHolder.columnDay.setText(relativeTime);

        // Highlight the columns that are clicked before
        if (clickedColumns.contains(columnItem.getColumnUrl())){

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

        columnViewHolder.constraintLayout.setOnClickListener(v -> {

            // Save the clicked item's link to sharedpreference (to gray out it later on)
            clickedColumns.edit().putLong(columnItem.getColumnUrl(), System.currentTimeMillis()).apply();

            // Open the link with Webview
            Intent intentUrl = new Intent(context, ShowInWebviewActivity.class);
            intentUrl.putExtra(context.getResources().getString(R.string.news_url), columnItem.getColumnUrl());
            intentUrl.putExtra(context.getResources().getString(R.string.news_source_for_display), columnItem.getName());
            intentUrl.putExtra(context.getResources().getString(R.string.columnist_source), columnItem.getSource());
            intentUrl.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intentUrl);

            // tell the adapter that there is change in the clicked item
            notifyItemChanged(columnViewHolder.getAdapterPosition());

            // Increment the click counter (used for displaying Interstitial Ad in Main Activity OnResume)
            int counter = customKeys.getInt(context.getResources().getString(R.string.news_click_counter),0);
            customKeys.edit().putInt(context.getResources().getString(R.string.news_click_counter), counter + 1).apply();

        });

        // POPUP MENU OPERATIONS (Save & Share)
        columnViewHolder.moreVertical.setOnClickListener(v -> {
            // Create the Popup Menu
            final PopupMenu popup = new PopupMenu(context, v);
            popup.getMenuInflater().inflate(R.menu.column_more_menu, popup.getMenu());
            // Set the title of Save News Menu item ( Yazıyı Kaydet / Favorilerden Çıkar)
            MenuItem saveMenuItem = popup.getMenu().getItem(0);
            if (savedColumns.contains(columnItem.getColumnUrl())) {
                saveMenuItem.setTitle(context.getResources().getString(R.string.unsave_news_column));
            } else {
                saveMenuItem.setTitle(context.getResources().getString(R.string.save_column));
            }
            // Set the click operations
            popup.setOnMenuItemClickListener(menuItem -> {

                // SAVE OPERATIONS
                if(menuItem.getItemId() == R.id.more_save_column){
                    // if column is already in the saved list
                    if (savedColumns.contains(columnItem.getColumnUrl())) {
                        //Delete column from favorite columns database
                        FavColumnsRepository repository = new FavColumnsRepository(context);
                        repository.delete(columnItem);
                        // Show toast message
                        Toast.makeText(context.getApplicationContext(), context.getResources().getString(R.string.column_deleted_from_favorite), Toast.LENGTH_SHORT).show();
                        // update shared preference file
                        savedColumns.edit().remove(columnItem.getColumnUrl()).apply();

                    // Column is not saved before, so save it into database
                    } else {
                        //Add column to favorite columns database
                        FavColumnsRepository repository = new FavColumnsRepository(context);
                        repository.insert(columnItem);
                        // Show toast message
                        Toast.makeText(context.getApplicationContext(), context.getResources().getString(R.string.column_added_to_favorite), Toast.LENGTH_SHORT).show();
                        // update shared preference file
                        savedColumns.edit().putBoolean(columnItem.getColumnUrl() ,true).apply();
                    }

                // SHARE OPERATIONS
                } else if(menuItem.getItemId() == R.id.more_share_column){
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_TEXT, columnItem.getColumnUrl());
                    intent.setType("text/plain");
                    context.startActivity(intent);
                }
                return true;
            });
            popup.show();
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
