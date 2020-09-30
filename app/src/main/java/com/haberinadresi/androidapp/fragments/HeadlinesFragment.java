package com.haberinadresi.androidapp.fragments;

import androidx.lifecycle.ViewModelProvider;
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
import com.haberinadresi.androidapp.utilities.NetworkUtils;
import com.haberinadresi.androidapp.viewmodels.HeadlinesVM;


public class HeadlinesFragment extends Fragment {

    private RecyclerView recyclerView;
    private HeadlinesVM headlinesVM;
    private HeadlineAdapter headlineAdapter;
    private ProgressBar progressBar;
    private View internetAlert;

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
        progressBar.setVisibility(View.GONE);
        // The internet connection error view
        internetAlert = view.findViewById(R.id.layout_internet_alert);
        internetAlert.setVisibility(View.GONE);

        Button checkConnection = view.findViewById(R.id.btn_check_internet);

        recyclerView = view.findViewById(R.id.rv_mansetler);
        recyclerView.setLayoutManager(new GridLayoutManager(requireActivity(), 3));
        recyclerView.setHasFixedSize(true);

        headlinesVM = new ViewModelProvider(this).get(HeadlinesVM.class);

        // The mobile data save alert view
        Button mobileDataAlert = view.findViewById(R.id.btn_mobiledata_warning);
        mobileDataAlert.setVisibility(View.GONE);
        // Open settings activity when clicked
        mobileDataAlert.setOnClickListener(v -> {
            Intent settings = new Intent(requireActivity(), SettingsActivity.class);
            startActivity(settings);
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
            checkConnection.setOnClickListener(v -> {
                // if connects after button click, then dismiss internet error
                if(NetworkUtils.isConnected(requireActivity())){
                    internetAlert.setVisibility(View.INVISIBLE);
                    fetchHeadlines();
                }
            });
        }
    }

    private void fetchHeadlines() {

        // Start animating the progressbar
        progressBar.setVisibility(View.VISIBLE);

        headlinesVM.getHeadlinesLivedata().observe(getViewLifecycleOwner(), mansetList -> {

            if(mansetList != null){
                headlineAdapter = new HeadlineAdapter(requireActivity(), mansetList);
                recyclerView.setAdapter(headlineAdapter);
            }
            // Hide ProgressBar when news are loaded
            progressBar.setVisibility(View.INVISIBLE);
        });
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        // Get the banner ad from main activity and show it if this fragment is visible to user
        AdView bannerAdView = requireActivity().findViewById(R.id.bannerAdView);
        if(bannerAdView != null){
            bannerAdView.setVisibility(View.VISIBLE);
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}