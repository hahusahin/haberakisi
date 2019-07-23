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
import com.haberinadresi.androidapp.R;
import com.haberinadresi.androidapp.models.Columnist;
import com.haberinadresi.androidapp.utilities.GlideApp;
import com.haberinadresi.androidapp.utilities.NetworkUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ColumnistAdapter extends RecyclerView.Adapter<ColumnistAdapter.ColumnistViewHolder> {

    private Context context;
    private List<Columnist> columnistList;
    private SharedPreferences myColumnists;
    private boolean displayOnlyInWifi;

    public ColumnistAdapter(Context context, List<Columnist> columnistList) {
        this.context = context;
        this.columnistList = columnistList;
        myColumnists = context.getSharedPreferences(context.getResources().getString(R.string.columnist_prefs_key),Context.MODE_PRIVATE);
        displayOnlyInWifi = NetworkUtils.displayOnlyInWifi(context); // Get the user's mobile data saving preference
    }

    static class ColumnistViewHolder extends RecyclerView.ViewHolder {

        private TextView sourceName;
        private TextView columnistName;
        private ImageView columnistImage;
        private ImageView sourceSelector;
        private RelativeLayout relativeLayout;

        private ColumnistViewHolder(View itemView) {

            super(itemView);

            sourceName = itemView.findViewById(R.id.tv_col_source_name);
            columnistName = itemView.findViewById(R.id.tv_columnist_name);
            columnistImage = itemView.findViewById(R.id.iv_columnist_square);
            sourceSelector = itemView.findViewById(R.id.iv_source_selector);
            relativeLayout = itemView.findViewById(R.id.layout_columnist_item);
        }
    }

    @NonNull
    @Override
    public ColumnistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_columnist, parent,false);
        return new ColumnistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ColumnistViewHolder viewHolder, int position) {

        final Columnist columnist = columnistList.get(position);

        viewHolder.columnistName.setText(columnist.getName());
        viewHolder.sourceName.setText(columnist.getSource());

        // Columnist's image
        GlideApp.with(viewHolder.itemView.getContext())
                .load(columnist.getImageUrl())
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .onlyRetrieveFromCache(displayOnlyInWifi)
                .error(R.drawable.placeholder_columnist)
                .into(viewHolder.columnistImage);

        //Yazar adlarının key'leri bazılarında hepsi büyük, bazılarında sadece baş harfleri büyük,
        // O yüzden hepsini küçük harfe çevirerek kontrol ediyor.
        final Locale turkish = new Locale("tr", "TR");

        // PUT THE CORRECT IMAGE WRT THE USER'S COLUMNIST PREFERENCES (CHECKED OR NOT CHECKED)
        if (myColumnists.contains(columnist.getKey().toLowerCase(turkish))){
            viewHolder.sourceSelector.setImageResource(R.mipmap.ic_remove_circle);
        } else {
            viewHolder.sourceSelector.setImageResource(R.mipmap.ic_add_circle);
        }

        viewHolder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // CHANGE SHARED PREFERENCES
                SharedPreferences.Editor editor = myColumnists.edit();
                // check the current preference (if exists delete it, if not exists add it)
                if (myColumnists.contains(columnist.getKey().toLowerCase(turkish))){
                    editor.remove(columnist.getKey().toLowerCase(turkish));
                } else {
                    editor.putBoolean(columnist.getKey().toLowerCase(turkish), true);
                }
                editor.apply();

                // CHANGE THE PLUS MINUS IMAGE
                if (myColumnists.contains(columnist.getKey().toLowerCase(turkish))){
                    viewHolder.sourceSelector.setImageResource(R.mipmap.ic_remove_circle);
                    Toast.makeText(context.getApplicationContext(), columnist.getName() + " " + context.getResources().getString(R.string.added_to_favorite), Toast.LENGTH_SHORT).show();
                } else {
                    viewHolder.sourceSelector.setImageResource(R.mipmap.ic_add_circle);
                    Toast.makeText(context.getApplicationContext(), columnist.getName() + " " + context.getResources().getString(R.string.deleted_from_favorite), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        if (columnistList != null){
            return columnistList.size();
        } else {
            return 0;
        }
    }

    public void setColumnistList(List<Columnist> newList){
        columnistList = new ArrayList<>();
        columnistList.addAll(newList);
        notifyDataSetChanged();
    }


}
