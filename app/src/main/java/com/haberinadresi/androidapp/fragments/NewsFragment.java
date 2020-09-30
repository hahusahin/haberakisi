package com.haberinadresi.androidapp.fragments;

import androidx.lifecycle.ViewModelProvider;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.haberinadresi.androidapp.R;
import com.haberinadresi.androidapp.activities.SourceSelectionActivity;
import com.haberinadresi.androidapp.adapters.NewsAdapterWithAds;
import com.haberinadresi.androidapp.adapters.NewsAdapterWithoutAd;
import com.haberinadresi.androidapp.models.NewsItem;
import com.haberinadresi.androidapp.utilities.NetworkUtils;
import com.haberinadresi.androidapp.viewmodels.NewsVM;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class NewsFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private RecyclerView recyclerView;
    private NewsAdapterWithAds newsAdapter;
    private NewsAdapterWithoutAd searchAdapter;
    private NewsVM newsViewModel;
    private SharedPreferences sourcePreferences, customPreferences;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeContainer;
    private Button sourceWarning;
    private View internetAlert;
    private boolean onPauseFlag = false;
    private boolean isPreferenceChanged = false;
    private List<Object> recyclerViewItems = new ArrayList<>(); // List of banner ads and NewsItems that populate the RecyclerView.
    private static final int ITEMS_PER_AD = 13; // A banner ad is placed in every (6 + 13)th position in the RecyclerView.
    private String categoryKey;
    private String categoryName;
    private LinearLayoutManager linearLayoutManager;
    private GridLayoutManager gridLayoutManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent,
                             @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true); // To show menu item on actionbar
        Bundle bundle = getArguments(); // Get the category name and key
        if(bundle != null){
            categoryKey = bundle.getString(getResources().getString(R.string.news_category_key));
            categoryName = bundle.getString(getResources().getString(R.string.news_category_name));
        }
        return inflater.inflate(R.layout.fragment_news, parent, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // The button to display if user didn't select any news source
        sourceWarning = view.findViewById(R.id.btn_source_alert);
        sourceWarning.setVisibility(View.GONE);
        // The ProgressBar that will indicate to the user that news are loading
        progressBar = view.findViewById(R.id.pb_news_loading);
        progressBar.setVisibility(View.GONE);
        // The internet connection error view
        internetAlert = view.findViewById(R.id.layout_internet_alert);
        internetAlert.setVisibility(View.GONE);
        Button checkConnection = view.findViewById(R.id.btn_check_internet);
        //get the sharedpreferences and register
        sourcePreferences = requireActivity().getSharedPreferences(requireActivity().getResources().getString(R.string.source_prefs_key), MODE_PRIVATE);
        sourcePreferences.registerOnSharedPreferenceChangeListener(this);

        customPreferences = requireActivity().getSharedPreferences(getResources().getString(R.string.custom_keys), MODE_PRIVATE);
        customPreferences.registerOnSharedPreferenceChangeListener(this);

        // Initialize recyclerview types
        recyclerView = view.findViewById(R.id.rv_news);
        linearLayoutManager = new LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false);
        gridLayoutManager = new GridLayoutManager(requireActivity(), 2);
        // When it is the ads turn in the recyclerview, only show one item ( don't show news)
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if ((position + 7) % ITEMS_PER_AD == 0) {
                    return 2;
                }
                return 1;
            }
        });
        // If user prefers to view news as grid then set gridlayout, else set normal layout (0 and 1 for normal view, 2 for grid)
        if(customPreferences.getInt(getResources().getString(R.string.news_item_view_preference), 0) == 2){
            recyclerView.setLayoutManager(gridLayoutManager);
        } else {
            recyclerView.setLayoutManager(linearLayoutManager);
        }

        recyclerView.setHasFixedSize(true);
        //recyclerView.setItemViewCacheSize(20); //number of offscreen views to retain

        // İnitialize viewmodel
        newsViewModel = new ViewModelProvider(this).get(NewsVM.class);

        // Configure the swipe refresh
        swipeContainer = view.findViewById(R.id.swiper);
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_dark, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light);
        swipeContainer.setOnRefreshListener(() -> {
            swipeContainer.setRefreshing(true);
            fetchNews(isPreferenceChanged, true);
        });

        // If device is connected to the internet
        if (NetworkUtils.isConnected(requireActivity())) {
            //Then start fetching news
            fetchNews(isPreferenceChanged, false);
        }
        // else show internet connection error
        else {
            internetAlert.setVisibility(View.VISIBLE);
            swipeContainer.setEnabled(false);
            // when user clicks on button, check the network connection again
            checkConnection.setOnClickListener(v -> {
                // if connects after button click, then dismiss internet error and fetch news
                if (NetworkUtils.isConnected(requireActivity())) {
                    internetAlert.setVisibility(View.GONE);
                    fetchNews(isPreferenceChanged, false);
                }
            });
        }
    }

    private void fetchNews(boolean isPreferenceChanged, boolean isRefreshed) {
        // Get the recent source preferences of the user
        final ArrayList<String> mySourceList = getSourceList();
        // if user has at least one source, fetch the news
        if (mySourceList.size() > 0) {
            //disappear source warning
            sourceWarning.setVisibility(View.GONE);
            // Start animating the progressbar
            progressBar.setVisibility(isRefreshed ? View.GONE : View.VISIBLE);
            swipeContainer.setEnabled(true);
            // Start with empty adapter/recyclerview
            newsAdapter = new NewsAdapterWithAds(requireActivity(), recyclerViewItems);
            recyclerView.setAdapter(newsAdapter);
            // adapter for making search on the news
            searchAdapter = new NewsAdapterWithoutAd(requireActivity(), new ArrayList<>());

            newsViewModel.getNewsLivedata(categoryKey, mySourceList, isPreferenceChanged).observe(getViewLifecycleOwner(), newsList -> {
                if(newsList != null){
                    //First clear the list and add recent news to list
                    recyclerViewItems.clear();
                    recyclerViewItems.addAll(newsList);
                    // Then add the Ads (to related positions)
                    addBannerAds();
                    loadBannerAds();
                    // After both finished, update recyclerview adapter
                    newsAdapter.setNewsList(recyclerViewItems);
                }
                // Hide progressbar and swipe Refresher
                progressBar.setVisibility(View.GONE);
                swipeContainer.setRefreshing(false);
            });

        // If the user has no source
        } else {
            //Clear the adapter(if exists) and Show warning text
            if(newsAdapter != null){
                newsAdapter.clearNewsList();
            }
            sourceWarning.setVisibility(View.VISIBLE);
            swipeContainer.setEnabled(false);
            // Open the source selection page when user clicks the warning
            sourceWarning.setOnClickListener(v -> {

                Intent sourceSelection = new Intent(requireActivity(), SourceSelectionActivity.class);
                sourceSelection.putExtra(getResources().getString(R.string.news_category_key), categoryKey);
                sourceSelection.putExtra(getResources().getString(R.string.news_category_name),categoryName);
                startActivity(sourceSelection);
            });
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if(key.contains("_" + categoryKey)){
            isPreferenceChanged = true;
        }

        // If the news view type preference changed, then update the recyclerview layout and adapter
        if (key.equals(getResources().getString(R.string.news_item_view_preference))){
            int news_view_type = customPreferences.getInt(getResources().getString(R.string.news_item_view_preference), 0);
            switch (news_view_type){
                case 0: case 1: default:
                    recyclerView.setLayoutManager(linearLayoutManager);
                    break;
                case 2:
                    recyclerView.setLayoutManager(gridLayoutManager);
                    break;
            }
            recyclerView.setAdapter(newsAdapter);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        onPauseFlag = true;
        //pause the adMob
        for (Object item : recyclerViewItems) {
            if (item instanceof AdView) {
                AdView adView = (AdView) item;
                adView.pause();
            }
        }
    }

    @Override
    public void onResume() {
        // Get the banner ad from main activity and Hide it if this fragment is visible to user
        AdView bannerAdView = requireActivity().findViewById(R.id.bannerAdView);
        if(bannerAdView != null){
            bannerAdView.setVisibility(View.GONE);
        }

        //If coming from onPause (not from onCreate)
        if(onPauseFlag){
            //If source preferences are changed
            if(isPreferenceChanged){
                // Fetch the news again (with new source list)
                fetchNews(true, false);
                isPreferenceChanged = false;
            }
            onPauseFlag = false;
        }
        //resume the adMob
        for (Object item : recyclerViewItems) {
            if (item instanceof AdView) {
                AdView adView = (AdView) item;
                adView.resume();
            }
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        //unregister the sharedpreferences
        sourcePreferences.unregisterOnSharedPreferenceChangeListener(this);
        customPreferences.unregisterOnSharedPreferenceChangeListener(this);
        //destroy the adMob
        for (Object item : recyclerViewItems) {
            if (item instanceof AdView) {
                AdView adView = (AdView) item;
                adView.destroy();
            }
        }
        super.onDestroy();
    }

    // Added to make search in the news list
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.news_fragment_menu, menu);

        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        final MenuItem newsViewMenuItem = menu.findItem(R.id.action_news_view_type);

        // Search Menu Item Operations
        final SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setQueryHint(getResources().getString(R.string.search_news));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                // Get the whole news list from the recyclerview adapter (if initialized)
                if(newsAdapter != null) {
                    List<Object> allNews = newsAdapter.getNewsList();
                    List<NewsItem> filteredNews = new ArrayList<>();
                    final Locale turkish = new Locale("tr", "TR");
                    String searchText = s.toLowerCase(turkish);
                    // Iterate over the news to find the keyword
                    for(Object item : allNews){
                        if (item instanceof NewsItem){
                            String title = ((NewsItem) item).getTitle().toLowerCase(turkish);
                            String summary = ((NewsItem) item).getSummary().toLowerCase(turkish);
                            // if found add the news item to list
                            if(title.contains(searchText) || summary.contains(searchText)){
                                filteredNews.add((NewsItem) item);
                            }
                        }
                    }
                    // set the filtered list to adapter/recyclerview
                    searchAdapter.setNewsList(filteredNews);
                    newsViewMenuItem.setVisible(false);
                    recyclerView.setAdapter(searchAdapter);
                }
                searchView.clearFocus(); //To hide keyboard
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        searchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                swipeContainer.setEnabled(false); // Disable swipe refreshing
                return true;
            }
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                // When backpressed to close search set the original news adapter (load all news)
                newsViewMenuItem.setVisible(true);
                recyclerView.setAdapter(newsAdapter);
                swipeContainer.setEnabled(true); // enable swipe refreshing
                return true;
            }
        });

        // News View Type Menu Item Operations
        int news_view_type = customPreferences.getInt(getResources().getString(R.string.news_item_view_preference), 0);
        // Set the correct image wrt the user's news view preference type
        switch (news_view_type){
            case 0: default:
                newsViewMenuItem.setIcon(R.drawable.ic_newsview2);
                break;
            case 1:
                newsViewMenuItem.setIcon(R.drawable.ic_newsview3);
                break;
            case 2:
                newsViewMenuItem.setIcon(R.drawable.ic_newsview1);
                break;
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @SuppressWarnings("ApplySharedPref") // TO SUPPRESS SHARED PREFERENCE COMMIT WARNING
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_news_view_type) {
            // Get the news view preference and increment it by one (also get the modulus) to show the new view type
            int news_view_type = customPreferences.getInt(getResources().getString(R.string.news_item_view_preference), 0);
            news_view_type = (news_view_type + 1) % 3;
            // Update the shared preferences file immediately
            customPreferences.edit().putInt(getResources().getString(R.string.news_item_view_preference), news_view_type).commit();
            // Update the icon wrt the view preference
            switch (news_view_type){
                case 0: default:
                    item.setIcon(R.drawable.ic_newsview2);
                    break;
                case 1:
                    item.setIcon(R.drawable.ic_newsview3);
                    break;
                case 2:
                    item.setIcon(R.drawable.ic_newsview1);
                    break;
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Gets the source preferences from SharedPreferences file
    private ArrayList<String> getSourceList() {
        ArrayList<String> mySourceList = new ArrayList<>();
        // Get all the source preferences (in map format)
        Map<String, ?> preferences = sourcePreferences.getAll();
        if (preferences != null) {
            for (Map.Entry<String, ?> entry : preferences.entrySet()) {
                // if the item in the sharedpreference is in user's sources add to list
                if (entry.getKey().contains("_" + categoryKey)) {
                    String source = entry.getKey().split("_")[0];
                    mySourceList.add(source);
                }
            }
        }
        return mySourceList;
    }

    //Adds banner ads to the items list.
    private void addBannerAds() {
        // Loop through the items array and place a new banner ad in every ith position in the items List.
        for (int i = 6; i <= recyclerViewItems.size(); i += ITEMS_PER_AD) {
            final AdView adView = new AdView(requireActivity());

            // If  device is tablet use LEADERBOARD Banner Size, For all others use MEDIUM RECTANGLE
            if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) ==
                    Configuration.SCREENLAYOUT_SIZE_LARGE ||
                    (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) ==
                            Configuration.SCREENLAYOUT_SIZE_XLARGE){
                adView.setAdSize(AdSize.LEADERBOARD);
            } else {
                adView.setAdSize(AdSize.MEDIUM_RECTANGLE);
            }
            adView.setAdUnitId(getResources().getString(R.string.admob_banner_buyuk_unit_id));
            recyclerViewItems.add(i, adView);
        }
    }

    //Sets up and loads the banner ads.
    private void loadBannerAds() {
        // Load the first banner ad in the items list (subsequent ads will be loaded automatically in sequence
        loadBannerAd(6);
    }

    // Loads the banner ads in the items list.
    private void loadBannerAd(final int index) {

        if (index >= recyclerViewItems.size()) {
            return;
        }

        Object item = recyclerViewItems.get(index);
        if (!(item instanceof AdView)) {
            throw new ClassCastException("Expected item at index " + index + " to be a banner ad" + " ad.");
        }

        final AdView adView = (AdView) item;

        // Set an AdListener on the AdView to wait for the previous banner ad
        // to finish loading before loading the next ad in the items list.
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                // The previous banner ad loaded successfully. Load the next ad in the items list.
                loadBannerAd(index + ITEMS_PER_AD);
            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                // The previous banner ad failed to load. Load the next ad in the items list.
                loadBannerAd(index + ITEMS_PER_AD);
            }
        });
        // Load the banner ad.
        adView.loadAd(new AdRequest.Builder().build());
    }

}