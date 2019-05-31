package com.haberinadresi.androidapp.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.haberinadresi.androidapp.R;
import com.haberinadresi.androidapp.fragments.HeadlinesFragment;
import com.haberinadresi.androidapp.fragments.NewsFragment;
import com.haberinadresi.androidapp.fragments.ColumnsFragment;
import com.haberinadresi.androidapp.fragments.WebsitesFragment;
import com.haberinadresi.androidapp.models.CategoryItem;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TabPagerAdapter extends FragmentStatePagerAdapter {

    private String[] tabArray;
    private Context context;

    public TabPagerAdapter(Context context, FragmentManager fragmentManager) {
        super(fragmentManager);
        this.context = context;

        List<String> tabPreferences = new ArrayList<>();

        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.custom_keys),Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(context.getResources().getString(R.string.category_preferences), null);
        if (json != null) {
            //Convert string to arraylist
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<CategoryItem>>() {}.getType();
            List<CategoryItem> categoryList = gson.fromJson(json, type);
            //Iterate over the list and get the category names that are selected by user
            for (CategoryItem categoryItem : categoryList) {
                if (categoryItem.getIsSelected()) {
                    tabPreferences.add(categoryItem.getCategoryName());
                }
            }
        }
        // Get the users' category preferences
        if(! tabPreferences.isEmpty()) {
            //Convert arraylist to array
            tabArray = tabPreferences.toArray(new String[0]);
        }
        // If can't get the users' category preferences, then show the default tabs
        else {
            tabArray = context.getResources().getStringArray(R.array.tabs);
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {

        return tabArray[position];
    }

    @Override
    public Fragment getItem(int position) {

        String title = tabArray[position];

        if(title.equals(context.getResources().getString(R.string.gundem_normal))){
            return getFragment(context.getResources().getString(R.string.gundem_key), context.getResources().getString(R.string.gundem_normal));
        } else if(title.equals(context.getResources().getString(R.string.ekonomi_normal))){
            return getFragment(context.getResources().getString(R.string.ekonomi_key), context.getResources().getString(R.string.ekonomi_normal));
        } else if(title.equals(context.getResources().getString(R.string.spor_normal))){
            return getFragment(context.getResources().getString(R.string.spor_key), context.getResources().getString(R.string.spor_normal));
        } else if(title.equals(context.getResources().getString(R.string.teknoloji_normal))){
            return getFragment(context.getResources().getString(R.string.teknoloji_key), context.getResources().getString(R.string.teknoloji_normal));
        } else if(title.equals(context.getResources().getString(R.string.kultursanat_normal))){
            return getFragment(context.getResources().getString(R.string.kultursanat_key), context.getResources().getString(R.string.kultursanat_normal));
        } else if(title.equals(context.getResources().getString(R.string.saglik_normal))){
            return getFragment(context.getResources().getString(R.string.saglik_key), context.getResources().getString(R.string.saglik_normal));
        } else if(title.equals(context.getResources().getString(R.string.magazin_normal))){
            return getFragment(context.getResources().getString(R.string.magazin_key), context.getResources().getString(R.string.magazin_normal));

        } else if(title.equals(context.getResources().getString(R.string.koseyazilari_normal))){
            return new ColumnsFragment();
        } else if(title.equals(context.getResources().getString(R.string.manset_normal))){
            return new HeadlinesFragment();
        } else if(title.equals(context.getResources().getString(R.string.habersiteleri_normal))){
            return new WebsitesFragment();
        } else {
            return null;
        }
    }

    @Override
    public int getCount() {
        if(tabArray != null){
            return tabArray.length;
        } else {
            return 0;
        }
    }

    // Adjust the fragment with the related arguments (gundem, ekonomi, spor, teknoloji, kültürsanat, saglik, magazin)
    private Fragment getFragment(String categoryKey, String categoryName){
        Bundle args = new Bundle();
        args.putString(context.getResources().getString(R.string.news_category_key), categoryKey);
        args.putString(context.getResources().getString(R.string.news_category_name), categoryName);
        NewsFragment newsFragment = new NewsFragment();
        newsFragment.setArguments(args);
        return newsFragment;
    }

}
