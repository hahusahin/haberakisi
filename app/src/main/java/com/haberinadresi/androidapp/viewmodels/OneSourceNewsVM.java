package com.haberinadresi.androidapp.viewmodels;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;

import com.google.gson.reflect.TypeToken;
import com.haberinadresi.androidapp.R;
import com.haberinadresi.androidapp.interfaces.API;
import com.haberinadresi.androidapp.models.NewsItem;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.Context.MODE_PRIVATE;

public class OneSourceNewsVM extends AndroidViewModel {

    public OneSourceNewsVM(Application application){
        super(application);
    }


    private MutableLiveData<List<NewsItem>> newsLiveData;

    //load news from server OR from shared preferences and attach to livedata
    public LiveData<List<NewsItem>> getNewsLiveData(String key) {

        newsLiveData = new MutableLiveData<>();

        if(loadNewsFromSP(key).isEmpty() || getGundemSourceCount() >= 20){
            // If the cache is empty OR user's gundem sources are more than 20 then load from server
            loadNewsFromServer(key);

        } else {
            // If there exists data in cache then load it
            newsLiveData.postValue(loadNewsFromSP(key));
        }

        return newsLiveData;
    }

    //This method is using Retrofit to get the JSON data from URL
    private void loadNewsFromServer(final String key) {

        Gson gson = new GsonBuilder().setLenient().create();

        final OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getApplication().getResources().getString(R.string.api_link))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();

        API api = retrofit.create(API.class);

        //Make the related http request depending on the clicked news category and source
        String source = key.split("_")[0];  // Example: ahaber_gundem
        String category = key.split("_")[1];
        Call<List<NewsItem>> call = api.getNewsFromOneSource(category, source);

        if (call != null) {

            call.enqueue(new Callback<List<NewsItem>>() {
                @Override
                public void onResponse(@NonNull Call<List<NewsItem>> call, @NonNull Response<List<NewsItem>> response) {

                    if(response.isSuccessful() && response.body() != null){

                        //set the list to our MutableLiveData
                        newsLiveData.setValue(response.body());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<NewsItem>> call, @NonNull Throwable t) {

                    t.printStackTrace();
                }
            });
        }

    }

    private List<NewsItem> loadNewsFromSP(String key) {
        String category = key.split("_")[1]; // Example: ahaber_gundem
        Gson gson = new Gson();
        SharedPreferences sharedPreferences = getApplication().getSharedPreferences(getApplication().getResources().getString(R.string.cached_news_key), MODE_PRIVATE);
        String json = sharedPreferences.getString(category, null);
        if (json != null) {
            Type type = new TypeToken<ArrayList<NewsItem>>() {}.getType();
            List<NewsItem> newsList = gson.fromJson(json, type);
            List<NewsItem> filteredNews = new ArrayList<>();
            for(NewsItem newsItem : newsList){
                if(newsItem.getKey().equals(key)){
                    filteredNews.add(newsItem);
                }
            }
            return filteredNews;
        } else {
            return new ArrayList<>();
        }
    }

    // Gets the number of gundem sources (to be used in the getnewslivedata method)
    private int getGundemSourceCount() {
        int sourceCounter = 0;
        // Get all the source preferences (in map format)
        SharedPreferences sourcePreferences = getApplication().getSharedPreferences(getApplication().getResources().getString(R.string.source_prefs_key), MODE_PRIVATE);
        Map<String, ?> preferences = sourcePreferences.getAll();
        if (preferences != null) {
            for (Map.Entry<String, ?> entry : preferences.entrySet()) {
                // if the item in the sharedpreference is in user's sources increment counter
                if (entry.getKey().contains("_gundem")) {
                    sourceCounter ++;
                }
            }
        }
        return sourceCounter;
    }
}
