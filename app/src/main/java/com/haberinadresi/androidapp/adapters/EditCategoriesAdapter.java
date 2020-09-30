package com.haberinadresi.androidapp.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.haberinadresi.androidapp.R;
import com.haberinadresi.androidapp.models.CategoryItem;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class EditCategoriesAdapter extends RecyclerView.Adapter<EditCategoriesAdapter.CategoryViewHolder> {

    private Context context;
    private List<CategoryItem> categoryList;
    private SharedPreferences sharedPreferences;

    public EditCategoriesAdapter(Context context, List<CategoryItem> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
        sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.custom_keys), MODE_PRIVATE);
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {

        private ImageView categoryIcon;
        private TextView categoryName;
        private CheckBox categorySelector;

        private CategoryViewHolder(View itemView){

            super(itemView);

            categoryIcon = itemView.findViewById(R.id.iv_category_icon);
            categoryName = itemView.findViewById(R.id.tv_category_name);
            categorySelector = itemView.findViewById(R.id.cb_category_selector);

        }
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_edit_categories, parent,false);
        return new CategoryViewHolder(view);
    }

    @SuppressWarnings("ApplySharedPref") // TO SUPPRESS SHARED PREFERENCE COMMIT WARNING
    @Override
    public void onBindViewHolder(@NonNull final CategoryViewHolder viewHolder, int position) {

        CategoryItem categoryItem = categoryList.get(position);

        viewHolder.categoryName.setText(categoryItem.getCategoryName());
        viewHolder.categoryIcon.setImageResource(categoryItem.getCategoryImageId());
        viewHolder.categorySelector.setChecked(categoryItem.getIsSelected());

        viewHolder.categorySelector.setOnCheckedChangeListener((buttonView, isChecked) -> {
            //Change the boolean value in the current item
            categoryList.get(viewHolder.getAdapterPosition()).setSelected(isChecked);
            //Save the changes to Sharedpreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            Gson gson = new Gson();
            String json = gson.toJson(categoryList);
            editor.putString(context.getResources().getString(R.string.category_preferences), json);
            editor.commit();
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

    public void setCategoryList(List<CategoryItem> categoryList){
        this.categoryList = categoryList;
        notifyDataSetChanged();
    }

}
