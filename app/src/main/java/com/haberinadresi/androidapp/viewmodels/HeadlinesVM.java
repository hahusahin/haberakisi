package com.haberinadresi.androidapp.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.google.gson.reflect.TypeToken;
import com.haberinadresi.androidapp.R;
import com.haberinadresi.androidapp.interfaces.API;
import com.haberinadresi.androidapp.models.NewsItem;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.haberinadresi.androidapp.utilities.NetworkUtils;
import com.haberinadresi.androidapp.utilities.WebUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.Context.MODE_PRIVATE;

public class HeadlinesVM extends AndroidViewModel {

    public HeadlinesVM(@NonNull Application application) {
        super(application);
    }

    private MutableLiveData<List<NewsItem>> headlinesLiveData;
    private final String path1 = WebUtils.getPath11(getApplication().getApplicationContext());
    private final String path2 = NetworkUtils.getPath21(getApplication().getApplicationContext());

    //load news from server OR from shared preferences and attach to livedata
    public LiveData<List<NewsItem>> getHeadlinesLivedata() {

        headlinesLiveData = new MutableLiveData<>();

        if(loadHeadlinesFromSP().isEmpty()){
            // If the cache is empty then load from server
            loadHeadlinesFromServer();

        } else {
            // If there exists data in cache then load it
            headlinesLiveData.postValue(loadHeadlinesFromSP());
        }
        return headlinesLiveData;
    }

    private void loadHeadlinesFromServer() {

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
        Call<List<NewsItem>> call = api.getHeadlines(path1, path2);

        if (call != null) {

            call.enqueue(new Callback<List<NewsItem>>() {
                @Override
                public void onResponse(@NonNull Call<List<NewsItem>> call, @NonNull Response<List<NewsItem>> response) {

                    if(response.isSuccessful() && response.body() != null){
                        //set the list to our MutableLiveData
                        headlinesLiveData.setValue(response.body());
                        //save the response to shared preferences
                        saveHeadlinesToSP(response.body());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<NewsItem>> call, @NonNull Throwable t) {
                    t.printStackTrace();
                }
            });

        }
    }

    private List<NewsItem> loadHeadlinesFromSP() {
        Gson gson = new Gson();
        SharedPreferences sharedPreferences = getApplication().getSharedPreferences(getApplication().getResources().getString(R.string.cached_news_key), MODE_PRIVATE);
        String json = sharedPreferences.getString(getApplication().getResources().getString(R.string.manset_key), null);
        if (json != null) {
            Type type = new TypeToken<ArrayList<NewsItem>>() {}.getType();
            return gson.fromJson(json, type);
        } else {
            return new ArrayList<>();
        }
    }

    private void saveHeadlinesToSP(final List<NewsItem> newsList) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sharedPreferences = getApplication().getSharedPreferences(getApplication().getResources().getString(R.string.cached_news_key), MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                Gson gson = new Gson();
                String json = gson.toJson(newsList);
                editor.putString(getApplication().getResources().getString(R.string.manset_key), json);
                editor.apply();
            }
        }).start();
    }
}