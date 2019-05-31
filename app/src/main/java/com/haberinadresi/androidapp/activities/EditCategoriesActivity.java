package com.haberinadresi.androidapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.haberinadresi.androidapp.R;
import com.haberinadresi.androidapp.adapters.EditCategoriesAdapter;
import com.haberinadresi.androidapp.models.CategoryItem;
import com.haberinadresi.androidapp.utilities.EditCategoriesDialog;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EditCategoriesActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private List<CategoryItem> categoryList = new ArrayList<>();
    private EditCategoriesAdapter editCategoriesAdapter;
    private SharedPreferences sharedPreferences;
    private Button applyButton;
    private boolean isPreferenceChanged = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_categories);

        final Toolbar toolbar = findViewById(R.id.edit_categories_toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        RecyclerView recyclerView = findViewById(R.id.rv_categories);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setHasFixedSize(true);

        editCategoriesAdapter = new EditCategoriesAdapter(this, categoryList);
        recyclerView.setAdapter(editCategoriesAdapter);

        sharedPreferences = getSharedPreferences(getResources().getString(R.string.custom_keys), MODE_PRIVATE);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        String json = sharedPreferences.getString(getResources().getString(R.string.category_preferences), null);
        // Get the users category preferences and attach to adapter
        if (json != null){
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<CategoryItem>>() {}.getType();
            categoryList = gson.fromJson(json, type);
            List<CategoryItem> updatedList = getUpdatedCategoryList(categoryList);//Update icon images
            editCategoriesAdapter.setCategoryList(updatedList);
        // If the user didn't make any change on the order of categories, then load the default one
        } else {
            categoryList = getDefaultCategoryList();
            editCategoriesAdapter.setCategoryList(categoryList);
        }

        // To drag and drop the items in the recyclerview
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper
                .SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder dragged, @NonNull RecyclerView.ViewHolder target) {

                int positionDragged = dragged.getAdapterPosition();
                int positionTarget = target.getAdapterPosition();

                Collections.swap(categoryList, positionDragged, positionTarget);
                editCategoriesAdapter.notifyItemMoved(positionDragged, positionTarget);
                //Save the changes to Sharedpreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                Gson gson = new Gson();
                String json = gson.toJson(categoryList);
                editor.putString(getResources().getString(R.string.category_preferences), json);
                editor.commit();

                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

            }
        });

        helper.attachToRecyclerView(recyclerView);

        //Button that restarts the activity when clicked (to apply the category order change)
        applyButton = findViewById(R.id.btn_apply_changes);
        applyButton.setVisibility(View.GONE);
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditCategoriesActivity.this, MainActivity.class);
                intent.putExtra(EditCategoriesActivity.class.getName(), true);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();

            }
        });

    }

    public List<CategoryItem> getDefaultCategoryList(){

        ArrayList<CategoryItem> categoryList = new ArrayList<>();
        categoryList.add(new CategoryItem(getResources().getString(R.string.gundem_normal), R.mipmap.letter_g, true));
        categoryList.add(new CategoryItem(getResources().getString(R.string.spor_normal), R.mipmap.letter_s, true));
        categoryList.add(new CategoryItem(getResources().getString(R.string.koseyazilari_normal), R.mipmap.letter_k, true));
        categoryList.add(new CategoryItem(getResources().getString(R.string.ekonomi_normal), R.mipmap.letter_e, true));
        categoryList.add(new CategoryItem(getResources().getString(R.string.teknoloji_normal), R.mipmap.letter_t, true));
        categoryList.add(new CategoryItem(getResources().getString(R.string.kultursanat_normal), R.mipmap.letter_k, true));
        categoryList.add(new CategoryItem(getResources().getString(R.string.saglik_normal), R.mipmap.letter_s, true));
        categoryList.add(new CategoryItem(getResources().getString(R.string.magazin_normal), R.mipmap.letter_m, true));
        categoryList.add(new CategoryItem(getResources().getString(R.string.habersiteleri_normal), R.mipmap.letter_h, true));
        categoryList.add(new CategoryItem(getResources().getString(R.string.manset_normal), R.mipmap.letter_m, true));

        return categoryList;
    }

    // To update the resource file Id's in the sharedpreferences (they may change and give resource not found error)
    public List<CategoryItem> getUpdatedCategoryList(List<CategoryItem> categoryList){

        for(CategoryItem item : categoryList){
            if(item.getCategoryName().equals(getResources().getString(R.string.gundem_normal))){
                item.setCategoryImageId(R.mipmap.letter_g);
            } else if(item.getCategoryName().equals(getResources().getString(R.string.spor_normal))){
                item.setCategoryImageId(R.mipmap.letter_s);
            } else if(item.getCategoryName().equals(getResources().getString(R.string.koseyazilari_normal))){
                item.setCategoryImageId(R.mipmap.letter_k);
            } else if(item.getCategoryName().equals(getResources().getString(R.string.ekonomi_normal))){
                item.setCategoryImageId(R.mipmap.letter_e);
            } else if(item.getCategoryName().equals(getResources().getString(R.string.teknoloji_normal))){
                item.setCategoryImageId(R.mipmap.letter_t);
            } else if(item.getCategoryName().equals(getResources().getString(R.string.kultursanat_normal))){
                item.setCategoryImageId(R.mipmap.letter_k);
            } else if(item.getCategoryName().equals(getResources().getString(R.string.saglik_normal))){
                item.setCategoryImageId(R.mipmap.letter_s);
            } else if(item.getCategoryName().equals(getResources().getString(R.string.magazin_normal))){
                item.setCategoryImageId(R.mipmap.letter_m);
            } else if(item.getCategoryName().equals(getResources().getString(R.string.manset_normal))){
                item.setCategoryImageId(R.mipmap.letter_m);
            } else if(item.getCategoryName().equals(getResources().getString(R.string.habersiteleri_normal))){
                item.setCategoryImageId(R.mipmap.letter_h);
            } else {
                item.setCategoryImageId(0);
            }
        }

        return categoryList;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home){
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals(getResources().getString(R.string.category_preferences))){
            isPreferenceChanged = true;
            //Show the button only if user make changes
            applyButton.setVisibility(View.VISIBLE);
        }
    }

    // This is also needed since user may not click on the apply button
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //If the user changed the categories then restart main activity (to load fragments in correct order)
        if(isPreferenceChanged){
            isPreferenceChanged = false;
            Intent intent = new Intent(EditCategoriesActivity.this, MainActivity.class);
            intent.putExtra(EditCategoriesActivity.class.getName(), true);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        // Show an informative dialog about how to edit categories (just for once)
        if(! sharedPreferences.getBoolean(getResources().getString(R.string.is_category_dialog_seen), false)){
            EditCategoriesDialog dialog = new EditCategoriesDialog();
            dialog.show(getSupportFragmentManager(), "Edit Category Information");
        }

        super.onResume();
    }

    @Override
    public void onDestroy() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

}
