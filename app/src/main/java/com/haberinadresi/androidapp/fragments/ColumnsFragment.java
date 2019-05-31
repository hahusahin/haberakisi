package com.haberinadresi.androidapp.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.android.gms.ads.AdRequest;
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
    private boolean onPauseFlag = false;
    private boolean isPreferenceChanged = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_columns, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false));
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

        //Admob
        bannerAdView = view.findViewById(R.id.bannerAdView);
        AdRequest adRequest = new AdRequest.Builder().build();
        bannerAdView.loadAd(adRequest);

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

    public void fetchColumns(boolean isPreferenceChanged, boolean isRefreshed) {
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
                    }
                    // Hide ProgressBar and swipe refresher when news are loaded
                    progressBar.setVisibility(View.INVISIBLE);
                    swipeContainer.setRefreshing(false);
                }
            });

        //If no columnist is in favorite
        } else {
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

    @Override
    public void onPause() {
        onPauseFlag = true;
        if (bannerAdView != null) {
            bannerAdView.pause();
        }
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
            if (columnAdapter != null){
                columnAdapter.notifyDataSetChanged();
            }
            onPauseFlag = false;
        }
        if (bannerAdView != null) {
            bannerAdView.resume();
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        //unregister the sharedpreferences
        myColumnists.unregisterOnSharedPreferenceChangeListener(this);
        if (bannerAdView != null) {
            bannerAdView.destroy();
        }
        super.onDestroy();
    }


}

