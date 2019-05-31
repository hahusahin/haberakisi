package com.haberinadresi.androidapp.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.haberinadresi.androidapp.R;
import com.haberinadresi.androidapp.adapters.HeadlineAdapter;
import com.haberinadresi.androidapp.models.NewsItem;
import com.haberinadresi.androidapp.utilities.NetworkUtils;
import com.haberinadresi.androidapp.viewmodels.HeadlinesVM;

import java.util.List;

public class HeadlinesFragment extends Fragment {

    private RecyclerView recyclerView;
    private HeadlinesVM headlinesVM;
    private HeadlineAdapter headlineAdapter;
    private ProgressBar progressBar;
    private View internetAlert;
    private AdView bannerAdView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_headlines, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // The ProgressBar that will indicate to the user that news are loading
        progressBar = view.findViewById(R.id.pb_manset_fragment);
        progressBar.setVisibility(View.INVISIBLE);
        // The internet connection error view
        internetAlert = view.findViewById(R.id.layout_internet_alert);
        internetAlert.setVisibility(View.INVISIBLE);
        Button checkConnection = view.findViewById(R.id.btn_check_internet);

        recyclerView = view.findViewById(R.id.rv_mansetler);
        recyclerView.setLayoutManager(new GridLayoutManager(requireActivity(), 3));
        recyclerView.setHasFixedSize(true);

        headlinesVM = ViewModelProviders.of(this).get(HeadlinesVM.class);

        //Admob
        bannerAdView = view.findViewById(R.id.bannerAdView);
        AdRequest adRequest = new AdRequest.Builder().build();
        bannerAdView.loadAd(adRequest);

        // If device is connected to the internet, then start fetching data
        if (NetworkUtils.isConnected(requireActivity())){

            fetchHeadlines();

        } // else show internet connection error
        else {
            internetAlert.setVisibility(View.VISIBLE);
            // when user clicks on button, check the network connection again
            checkConnection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // if connects after button click, then dismiss internet error
                    if(NetworkUtils.isConnected(requireActivity())){
                        internetAlert.setVisibility(View.INVISIBLE);
                        fetchHeadlines();
                    }
                }
            });
        }
    }

    private void fetchHeadlines() {

        // Start animating the progressbar
        progressBar.setVisibility(View.VISIBLE);

        headlinesVM.getHeadlinesLivedata().observe(this, new Observer<List<NewsItem>>() {
            @Override
            public void onChanged(@Nullable List<NewsItem> mansetList) {

                if(mansetList != null){
                    headlineAdapter = new HeadlineAdapter(requireActivity(), mansetList);
                    recyclerView.setAdapter(headlineAdapter);
                }
                // Hide ProgressBar when news are loaded
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void onPause() {
        if (bannerAdView != null) {
            bannerAdView.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        if (bannerAdView != null) {
            bannerAdView.resume();
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        if (bannerAdView != null) {
            bannerAdView.destroy();
        }
        super.onDestroy();
    }
}