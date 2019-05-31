package com.haberinadresi.androidapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.haberinadresi.androidapp.R;
import com.haberinadresi.androidapp.activities.SourceSelectionActivity;
import com.haberinadresi.androidapp.models.CategoryItem;

import java.util.List;

public class WebCategoriesAdapter extends RecyclerView.Adapter<WebCategoriesAdapter.CategoryViewHolder> {

    private Context context;
    private List<CategoryItem> categoryList;

    public WebCategoriesAdapter(Context context, List<CategoryItem> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {

        private ImageView categoryIcon;
        private TextView categoryName;
        private RelativeLayout relativeLayout;

        private CategoryViewHolder(View itemView){

            super(itemView);

            categoryIcon = itemView.findViewById(R.id.iv_category_icon);
            categoryName = itemView.findViewById(R.id.tv_category_name);
            relativeLayout = itemView.findViewById(R.id.layout_category_item);

        }
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_website_categories, parent,false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final CategoryViewHolder viewHolder, int position) {

        final CategoryItem categoryItem = categoryList.get(position);

        viewHolder.categoryName.setText(categoryItem.getCategoryName());
        viewHolder.categoryIcon.setImageResource(categoryItem.getCategoryImageId());
        // Open the activity that shows related sources
        viewHolder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Convert the source name to key
                String key = getCategoryKeys(categoryItem.getCategoryName());
                if (key != null){
                    Intent sourceSelection = new Intent(context, SourceSelectionActivity.class);
                    sourceSelection.putExtra(context.getResources().getString(R.string.news_category_key), key);
                    sourceSelection.putExtra(context.getResources().getString(R.string.news_category_name), categoryItem.getCategoryName());
                    //sourceSelection.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(sourceSelection);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        if (categoryList != null){
            return categoryList.size();
        } else {
            return 0;
        }
    }

    private String getCategoryKeys(String category){
        if(category.equals(context.getResources().getString(R.string.gazete_websiteleri))) {
            return "websiteleri_gazete";
        } else if(category.equals(context.getResources().getString(R.string.spor_websiteleri))){
            return "websiteleri_spor";
        } else if(category.equals(context.getResources().getString(R.string.intmedyasi_websiteleri))){
            return "websiteleri_intmedyasi";
        } else if(category.equals(context.getResources().getString(R.string.ekonomi_websiteleri))){
            return "websiteleri_ekonomi";
        } else if(category.equals(context.getResources().getString(R.string.biltek_websiteleri))){
            return "websiteleri_biltek";
        } else if(category.equals(context.getResources().getString(R.string.kultursanat_websiteleri))){
            return "websiteleri_kultursanat";
        } else if(category.equals(context.getResources().getString(R.string.saglik_websiteleri))){
            return "websiteleri_saglik";
        } else if(category.equals(context.getResources().getString(R.string.magazin_websiteleri))){
            return "websiteleri_magazin";
        } else if(category.equals(context.getResources().getString(R.string.yabancibasin_websiteleri))){
            return "websiteleri_yabancibasin";
        } else if(category.equals(context.getResources().getString(R.string.yerel_websiteleri))){
            return "websiteleri_yerel";
        } else {
            return null;
        }
    }

}
