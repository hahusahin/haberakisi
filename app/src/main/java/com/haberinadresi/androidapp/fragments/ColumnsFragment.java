package com.haberinadresi.androidapp.fragments;

import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.android.gms.ads.AdView;
import com.haberinadresi.androidapp.R;
import com.haberinadresi.androidapp.activities.ColumnistSelectActivity;
import com.haberinadresi.androidapp.adapters.ColumnAdapter;
import com.haberinadresi.androidapp.models.Columnist;
import com.haberinadresi.androidapp.utilities.NetworkUtils;
import com.haberinadresi.androidapp.viewmodels.ColumnsVM;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ColumnsFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener{

    private RecyclerView recyclerView;
    private ColumnAdapter columnAdapter;
    private ColumnsVM columnsVM;
    private ProgressBar progressBar;
    private Button sourceWarning;
    private SwipeRefreshLayout swipeContainer;
    private SharedPreferences myColumnists;
    private View internetAlert;
    private AdView bannerAdView;
    private List<Columnist> allColumns;
    private boolean onPauseFlag = false;
    private boolean isPreferenceChanged = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true); // To show menu item on actionbar
        return inflater.inflate(R.layout.fragment_columns, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the banner ad from main activity and show it if this fragment is visible to user
        bannerAdView = requireActivity().findViewById(R.id.bannerAdView);
        if(getUserVisibleHint() && bannerAdView != null){
            bannerAdView.setVisibility(View.VISIBLE);
        }
        // The button to display if user didn't select any news source
        sourceWarning = view.findViewById(R.id.btn_source_alert);
        sourceWarning.setVisibility(View.INVISIBLE);
        // The ProgressBar that will indicate to the user that news are loading
        progressBar = view.findViewById(R.id.pb_news_loading);
        progressBar.setVisibility(View.INVISIBLE);
        // The internet connection error view
        internetAlert = view.findViewById(R.id.layout_internet_alert);
        internetAlert.setVisibility(View.INVISIBLE);
        Button checkConnection = view.findViewById(R.id.btn_check_internet);

        //get the sharedpreferences and register
        myColumnists = requireActivity().getSharedPreferences(getResources().getString(R.string.columnist_prefs_key), Context.MODE_PRIVATE);
        myColumnists.registerOnSharedPreferenceChangeListener(this);

        recyclerView = view.findViewById(R.id.rv_news);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false));
        recyclerView.setHasFixedSize(true);

        columnsVM = ViewModelProviders.of(this).get(ColumnsVM.class);

        // Configure the swipe Refresher and it's refreshing colors
        swipeContainer = view.findViewById(R.id.swiper);
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_dark,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        // Fetch the news when swiped by the user
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeContainer.setRefreshing(true);
                // get the news again
                fetchColumns(isPreferenceChanged, true);
            }
        });

        // If device is connected to the internet
        if (NetworkUtils.isConnected(requireActivity())){
            //Then start fetching news
            fetchColumns(isPreferenceChanged, false);
        }
        // else show internet connection error
        else {
            internetAlert.setVisibility(View.VISIBLE);
            swipeContainer.setEnabled(false);
            // when user clicks on button, check the network connection again
            checkConnection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // if connects after button click, then dismiss internet error
                    if(NetworkUtils.isConnected(requireActivity())){
                        internetAlert.setVisibility(View.INVISIBLE);
                        fetchColumns(isPreferenceChanged, false);
                        swipeContainer.setEnabled(true);
                    }
                }
            });
        }
    }

    private void fetchColumns(boolean isPreferenceChanged, boolean isRefreshed) {
        //Get the recent favorite columnists
        Map<String,?> columnistsMap = myColumnists.getAll();

        //If at least one columnist is in favorite
        if(! columnistsMap.isEmpty()){
            //disappear source warning
            sourceWarning.setVisibility(View.GONE);
            // Start animating the progressbar
            progressBar.setVisibility(isRefreshed ? View.GONE : View.VISIBLE);
            swipeContainer.setEnabled(true);
            // Start with empty adapter/recyclerview
            columnAdapter = new ColumnAdapter(requireActivity(), new ArrayList<Columnist>());
            recyclerView.setAdapter(columnAdapter);

            columnsVM.getColumnsLivedata(isPreferenceChanged).observe(this, new Observer<List<Columnist>>() {
                @Override
                public void onChanged(@Nullable List<Columnist> columnList) {
                    // Filters the column list wrt the users preferences and updates the recyclerview
                    List<Columnist> filteredColumns = new ArrayList<>();
                    if(columnList != null){
                        final Locale turkish = new Locale("tr", "TR");
                        for(Columnist column : columnList){
                            if(myColumnists.contains(column.getKey().toLowerCase(turkish))){
                                filteredColumns.add(column);
                            }
                        }
                        Collections.sort(filteredColumns, new Comparator<Columnist>() {
                            @Override
                            public int compare(Columnist column1, Columnist column2) {
                                return Long.compare(column2.getTime(), column1.getTime());
                            }
                        });
                        //Update the list of adapter
                        columnAdapter.setColumnList(filteredColumns);
                        // Keep the full column list to be used when making search
                        allColumns = filteredColumns;
                    }
                    // Hide ProgressBar and swipe refresher when news are loaded
                    progressBar.setVisibility(View.INVISIBLE);
                    swipeContainer.setRefreshing(false);
                }
            });
        }
        //If no columnist source in favorite
        else {
            //Clear the adapter(if exists) and Show warning text
            if(columnAdapter != null){
                columnAdapter.clearColumnList();
            }
            swipeContainer.setEnabled(false);
            sourceWarning.setVisibility(View.VISIBLE);
            // Open the columnist selection page when user clicks the warning
            sourceWarning.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent sourceSelection = new Intent(requireActivity(), ColumnistSelectActivity.class);
                    startActivity(sourceSelection);

                }
            });
        }

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        isPreferenceChanged = true;
    }

    // Added to make search in the column list
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.news_fragment_menu, menu);

        // Hide the view type icon (not used in columns)
        menu.findItem(R.id.action_news_view_type).setVisible(false);

        // Search Menu Item Operations
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setQueryHint(getResources().getString(R.string.search_columnist));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Get the whole news list from the recyclerview adapter (if initialized)
                if(allColumns != null){
                    List<Columnist> filteredColumns = new ArrayList<>();
                    final Locale turkish = new Locale("tr", "TR");
                    String searchText = query.toLowerCase(turkish);
                    // Iterate over the columns to find the columnist
                    for(Columnist item : allColumns){
                        String columnist = item.getName().toLowerCase(turkish);
                        if(columnist.contains(searchText)){
                            filteredColumns.add(item);
                        }
                    }
                    // set the filtered list to adapter
                    columnAdapter.setColumnList(filteredColumns);
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
                // When backpressed to close search, set the original column list
                if(allColumns != null){
                    columnAdapter.setColumnList(allColumns);
                } else {
                    fetchColumns(false, false);
                }
                swipeContainer.setEnabled(true); // enable swipe refreshing
                return true;
            }
        });

    }

    @Override
    public void onPause() {
        onPauseFlag = true;

        super.onPause();
    }

    @Override
    public void onResume() {
        //If coming from onPause (not from onCreate)
        if (onPauseFlag){
            //If source preferences are changed
            if(isPreferenceChanged){
                // Fetch the columns again (with new source list)
                fetchColumns(true, false);
                isPreferenceChanged = false;
            }
            onPauseFlag = false;
        }

        super.onResume();
    }

    @Override
    public void onDestroy() {
        //unregister the sharedpreferences
        myColumnists.unregisterOnSharedPreferenceChangeListener(this);

        super.onDestroy();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if(isVisibleToUser && isResumed() && bannerAdView != null){
            bannerAdView.setVisibility(View.VISIBLE);
        }
    }
}

