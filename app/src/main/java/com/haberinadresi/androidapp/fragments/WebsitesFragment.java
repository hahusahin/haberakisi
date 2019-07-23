package com.haberinadresi.androidapp.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.haberinadresi.androidapp.R;
import com.haberinadresi.androidapp.activities.WebCategoriesActivity;
import com.haberinadresi.androidapp.adapters.MyWebsitesAdapter;
import com.haberinadresi.androidapp.models.SourceItem;
import com.haberinadresi.androidapp.utilities.NetworkUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class WebsitesFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private RecyclerView recyclerView1, recyclerView2, recyclerView3, recyclerView4;
    private View internetAlert, divider1, divider2, divider3;
    private Button sourceWarning;
    private AdView bannerAdView;
    private SharedPreferences sourcePreferences;
    private boolean onPauseFlag = false;
    private boolean isPreferenceChanged = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_websites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Admob
        bannerAdView = view.findViewById(R.id.bannerAdView);
        AdRequest adRequest = new AdRequest.Builder().build();
        bannerAdView.loadAd(adRequest);

        // The button to display if user didn't select any news source
        sourceWarning = view.findViewById(R.id.btn_source_alert);
        sourceWarning.setVisibility(View.GONE);
        // The internet connection error view
        internetAlert = view.findViewById(R.id.layout_internet_alert);
        internetAlert.setVisibility(View.INVISIBLE);
        Button checkConnection = view.findViewById(R.id.btn_check_internet);

        //get the sharedpreferences and register
        sourcePreferences = requireActivity().getSharedPreferences(requireActivity().getResources().getString(R.string.source_prefs_key), Context.MODE_PRIVATE);
        sourcePreferences.registerOnSharedPreferenceChangeListener(this);

        //normal haber siteleri
        recyclerView1 = view.findViewById(R.id.rv_websites_general);
        recyclerView1.setLayoutManager(new GridLayoutManager(requireActivity(), 3));
        recyclerView1.setHasFixedSize(true);
        recyclerView1.setNestedScrollingEnabled(false);
        // spor
        recyclerView2 = view.findViewById(R.id.rv_websites_sport);
        recyclerView2.setLayoutManager(new GridLayoutManager(requireActivity(), 3));
        recyclerView2.setHasFixedSize(true);
        recyclerView2.setNestedScrollingEnabled(false);
        //kültür, sanat vs...
        recyclerView3 = view.findViewById(R.id.rv_websites_others);
        recyclerView3.setLayoutManager(new GridLayoutManager(requireActivity(), 3));
        recyclerView3.setHasFixedSize(true);
        recyclerView3.setNestedScrollingEnabled(false);
        // yabancı basın
        recyclerView4 = view.findViewById(R.id.rv_websites_foreign);
        recyclerView4.setLayoutManager(new GridLayoutManager(requireActivity(), 3));
        recyclerView4.setHasFixedSize(true);
        recyclerView4.setNestedScrollingEnabled(false);
        // Dividers between recyclerview items (don't show at the beginning)
        divider1 = view.findViewById(R.id.divider_1);
        divider1.setVisibility(View.GONE);
        divider2 = view.findViewById(R.id.divider_2);
        divider2.setVisibility(View.GONE);
        divider3 = view.findViewById(R.id.divider_3);
        divider3.setVisibility(View.GONE);

        // If device is connected to the internet, then show website's icons
        if (NetworkUtils.isConnected(requireActivity())){

            loadWebsiteIcons();

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
                        loadWebsiteIcons();
                    }
                }
            });
        }
    }

    private void loadWebsiteIcons() {
        // start with empty list
        List<SourceItem> myWebsites = new ArrayList<>();
        // get all preferences and iterate over them to find website preferences
        Map<String,?> sources = sourcePreferences.getAll();
        if(sources != null){
            for(Map.Entry<String,?> entry : sources.entrySet()){
                if(entry.getKey().contains("_habersiteleri")){
                    String json = (String) entry.getValue();
                    Gson gson = new Gson();
                    Type type = new TypeToken<SourceItem>() {}.getType();
                    SourceItem sourceItem = gson.fromJson(json, type);
                    myWebsites.add(sourceItem);
                }
            }
        }

        // if user has at least one source
        if (myWebsites.size() > 0) {
            //hide source warning
            sourceWarning.setVisibility(View.GONE);
            // Group the website preferences wrt 4 categories
            List<SourceItem> list1 = new ArrayList<>(); // gazete, int.medyası, ekonomi, yerel
            List<SourceItem> list2 = new ArrayList<>(); // spor
            List<SourceItem> list3 = new ArrayList<>(); // kültür sanat, bilim, teknoloji, sağlık, magazin vs...
            List<SourceItem> list4 = new ArrayList<>(); // yabancı basın
            for (SourceItem sourceItem : myWebsites){
                switch (sourceItem.getCategory()){
                    case "gazete": case "internet_medyasi": case "ekonomi": case "yerel":
                        list1.add(sourceItem);
                        break;
                    case "spor":
                        list2.add(sourceItem);
                        break;
                    case "biltek": case "kultursanat": case "saglik": case "magazin":
                        list3.add(sourceItem);
                        break;
                    case "yabanci_basin":
                        list4.add(sourceItem);
                        break;
                    default:
                        list1.add(sourceItem);
                        break;
                }
            }

            // gazete, int.medyası, yerel
            Collections.sort(list1, new Comparator<SourceItem>() {
                @Override
                public int compare(SourceItem item1, SourceItem item2) {
                    return item1.getSourceName().compareTo(item2.getSourceName());
                }
            });
            // load website sources to adapter
            MyWebsitesAdapter adapter1 = new MyWebsitesAdapter(requireActivity(), list1);
            //recyclerView1.addItemDecoration(new DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL));
            recyclerView1.setAdapter(adapter1);
            if(! list1.isEmpty()){
                recyclerView1.setVisibility(View.VISIBLE);
                divider1.setVisibility(View.VISIBLE);
            } else {
                recyclerView1.setVisibility(View.GONE);
                divider1.setVisibility(View.GONE);
            }

            // spor
            Collections.sort(list2, new Comparator<SourceItem>() {
                @Override
                public int compare(SourceItem item1, SourceItem item2) {
                    return item1.getSourceName().compareTo(item2.getSourceName());
                }
            });
            // load website sources to adapter
            MyWebsitesAdapter adapter2 = new MyWebsitesAdapter(requireActivity(), list2);
            recyclerView2.setAdapter(adapter2);
            //Show/Hide the first divider
            if(! list2.isEmpty()){
                recyclerView2.setVisibility(View.VISIBLE);
                divider2.setVisibility(View.VISIBLE);
            } else {
                recyclerView2.setVisibility(View.GONE);
                divider2.setVisibility(View.GONE);
            }

            // kültür sanat, bilim, teknoloji, sağlık vs...
            Collections.sort(list3, new Comparator<SourceItem>() {
                @Override
                public int compare(SourceItem item1, SourceItem item2) {
                    return item1.getSourceName().compareTo(item2.getSourceName());
                }
            });
            // load website sources to adapter
            MyWebsitesAdapter adapter3 = new MyWebsitesAdapter(requireActivity(), list3);
            recyclerView3.setAdapter(adapter3);
            //Show/Hide the second divider
            if(! list3.isEmpty()){
                recyclerView3.setVisibility(View.VISIBLE);
                divider3.setVisibility(View.VISIBLE);
            } else {
                recyclerView3.setVisibility(View.GONE);
                divider3.setVisibility(View.GONE);
            }

            // yabancı basın
            Collections.sort(list4, new Comparator<SourceItem>() {
                @Override
                public int compare(SourceItem item1, SourceItem item2) {
                    return item1.getSourceName().compareTo(item2.getSourceName());
                }
            });
            // load website sources to adapter
            MyWebsitesAdapter adapter4 = new MyWebsitesAdapter(requireActivity(), list4);
            recyclerView4.setAdapter(adapter4);
            //Show/Hide the third divider
            if(! list4.isEmpty()){
                recyclerView4.setVisibility(View.VISIBLE);
            } else {
                recyclerView4.setVisibility(View.GONE);
            }

        // If the user has no source
        } else {
            recyclerView1.setVisibility(View.GONE);
            recyclerView2.setVisibility(View.GONE);
            recyclerView3.setVisibility(View.GONE);
            recyclerView4.setVisibility(View.GONE);
            divider1.setVisibility(View.GONE);
            divider2.setVisibility(View.GONE);
            divider3.setVisibility(View.GONE);
            // show source warning
            sourceWarning.setVisibility(View.VISIBLE);
            // Open the source selection page when user clicks the warning
            sourceWarning.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent categorySelection = new Intent(requireActivity(), WebCategoriesActivity.class);
                    startActivity(categorySelection);
                }
            });
        }

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.contains("_habersiteleri")){
            isPreferenceChanged = true;
        }
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
        if(onPauseFlag){
            //If source preferences are changed
            if(isPreferenceChanged){
                // Load user's sources again
                loadWebsiteIcons();
                isPreferenceChanged = false;
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
        sourcePreferences.unregisterOnSharedPreferenceChangeListener(this);
        if (bannerAdView != null) {
            bannerAdView.destroy();
        }
        super.onDestroy();
    }

}