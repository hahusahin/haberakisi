package com.haberinadresi.androidapp.fragments;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.android.gms.ads.AdView;
import com.haberinadresi.androidapp.R;
import com.haberinadresi.androidapp.activities.SettingsActivity;
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

        // Get the banner ad from main activity and show it if this fragment is visible to user
        bannerAdView = requireActivity().findViewById(R.id.bannerAdView);
        if(getUserVisibleHint() && bannerAdView != null){
            bannerAdView.setVisibility(View.VISIBLE);
        }
        // The ProgressBar that will indicate to the user that news are loading
        progressBar = view.findViewById(R.id.pb_manset_fragment);
        progressBar.setVisibility(View.GONE);
        // The internet connection error view
        internetAlert = view.findViewById(R.id.layout_internet_alert);
        internetAlert.setVisibility(View.GONE);

        Button checkConnection = view.findViewById(R.id.btn_check_internet);

        recyclerView = view.findViewById(R.id.rv_mansetler);
        recyclerView.setLayoutManager(new GridLayoutManager(requireActivity(), 3));
        recyclerView.setHasFixedSize(true);

        headlinesVM = ViewModelProviders.of(this).get(HeadlinesVM.class);

        // The mobile data save alert view
        Button mobileDataAlert = view.findViewById(R.id.btn_mobiledata_warning);
        mobileDataAlert.setVisibility(View.GONE);
        // Open settings activity when clicked
        mobileDataAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settings = new Intent(requireActivity(), SettingsActivity.class);
                startActivity(settings);
            }
        });

        // If device is connected to the internet
        if (NetworkUtils.isConnected(requireActivity())){
            // Get the user's mobile data saving preference
            boolean displayOnlyInWifi = NetworkUtils.displayOnlyInWifi(requireActivity());
            // If mobile data saver is already opened, warn user to close it (don't load headlines)
            if(displayOnlyInWifi){
                mobileDataAlert.setVisibility(View.VISIBLE);
            // Else start fetching data
            } else {
                mobileDataAlert.setVisibility(View.GONE);
                fetchHeadlines();
            }

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

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if(isVisibleToUser && isResumed() && bannerAdView != null){
            bannerAdView.setVisibility(View.VISIBLE);
        }
    }
}