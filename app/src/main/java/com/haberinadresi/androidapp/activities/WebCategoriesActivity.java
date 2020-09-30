package com.haberinadresi.androidapp.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;

import com.haberinadresi.androidapp.R;
import com.haberinadresi.androidapp.adapters.WebCategoriesAdapter;
import com.haberinadresi.androidapp.models.CategoryItem;

import java.util.ArrayList;

public class WebCategoriesActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the text style (size) of user's preference
        SharedPreferences customPrefs = getSharedPreferences(getResources().getString(R.string.custom_keys), MODE_PRIVATE);
        String fontPreference = customPrefs.getString(getResources().getString(R.string.pref_font_key), "medium");
        if(fontPreference != null){
            switch (fontPreference){
                case "small":
                    setTheme(R.style.FontStyle_Small);
                    break;
                case "medium": default:
                    setTheme(R.style.FontStyle_Medium);
                    break;
                case "large":
                    setTheme(R.style.FontStyle_Large);
                    break;
                case "xlarge":
                    setTheme(R.style.FontStyle_XLarge);
                    break;
            }
        } else {
            setTheme(R.style.FontStyle_Medium);
        }

        setContentView(R.layout.activity_website_categories);

        final Toolbar toolbar = findViewById(R.id.website_categories_toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        RecyclerView recyclerView = findViewById(R.id.rv_categories);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        recyclerView.setHasFixedSize(true);

        ArrayList<CategoryItem> categoryList = new ArrayList<>();
        categoryList.add(new CategoryItem(getResources().getString(R.string.gazete_websiteleri), R.mipmap.letter_g, true));
        categoryList.add(new CategoryItem(getResources().getString(R.string.intmedyasi_websiteleri), R.mipmap.letter_i, true));
        categoryList.add(new CategoryItem(getResources().getString(R.string.spor_websiteleri), R.mipmap.letter_s, true));
        categoryList.add(new CategoryItem(getResources().getString(R.string.ekonomi_websiteleri), R.mipmap.letter_e, true));
        categoryList.add(new CategoryItem(getResources().getString(R.string.biltek_websiteleri), R.mipmap.letter_b, true));
        categoryList.add(new CategoryItem(getResources().getString(R.string.kultursanat_websiteleri), R.mipmap.letter_k, true));
        categoryList.add(new CategoryItem(getResources().getString(R.string.saglik_websiteleri), R.mipmap.letter_s, true));
        categoryList.add(new CategoryItem(getResources().getString(R.string.magazin_websiteleri), R.mipmap.letter_m, true));
        categoryList.add(new CategoryItem(getResources().getString(R.string.yerel_websiteleri), R.mipmap.letter_y, true));
        categoryList.add(new CategoryItem(getResources().getString(R.string.yabancibasin_websiteleri), R.mipmap.letter_y, true));

        WebCategoriesAdapter webCategoriesAdapter = new WebCategoriesAdapter(this, categoryList);
        recyclerView.setAdapter(webCategoriesAdapter);

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
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
