package com.haberinadresi.androidapp.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.google.gson.reflect.TypeToken;
import com.haberinadresi.androidapp.R;
import com.haberinadresi.androidapp.interfaces.API;
import com.haberinadresi.androidapp.models.Columnist;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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

public class ColumnsVM extends AndroidViewModel {

    public ColumnsVM(Application application){
        super(application);
    }


    private MutableLiveData<List<Columnist>> columnLiveData;

    public LiveData<List<Columnist>> getColumnsLivedata(boolean isPreferenceChanged){

        columnLiveData = new MutableLiveData<>();

        if(loadColumnsFromSP().isEmpty() || isPreferenceChanged){
            // If the cache is empty OR there is change in the preferences then load from server
            loadColumnsFromServer();

        } else {
            // If there exists data in cache and there is no change in preferences then load it
            columnLiveData.postValue(loadColumnsFromSP());
        }
        return columnLiveData;
    }

    private void loadColumnsFromServer() {

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
        Call<List<Columnist>> call = api.getColumns();

        if (call != null) {

            call.enqueue(new Callback<List<Columnist>>() {
                @Override
                public void onResponse(@NonNull Call<List<Columnist>> call, @NonNull Response<List<Columnist>> response) {

                    if(response.isSuccessful() && response.body() != null){

                        //set the list to our MutableLiveData
                        columnLiveData.setValue(response.body());
                        //save the response to shared preferences
                        saveColumnsToSP(response.body());
                    }
                }
                @Override
                public void onFailure(@NonNull Call<List<Columnist>> call, @NonNull Throwable t) {
                    t.printStackTrace();
                }
            });
        }
    }

    private List<Columnist> loadColumnsFromSP() {
        Gson gson = new Gson();
        SharedPreferences cachedNews = getApplication().getSharedPreferences(getApplication().getResources().getString(R.string.cached_news_key), Context.MODE_PRIVATE);
        String json = cachedNews.getString(getApplication().getResources().getString(R.string.yazarlar_key), null);
        if (json != null) {
            Type type = new TypeToken<ArrayList<Columnist>>() {}.getType();
            return gson.fromJson(json, type);
        } else {
            return new ArrayList<>();
        }
    }

    private void saveColumnsToSP(final List<Columnist> columnList) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sharedPreferences = getApplication().getSharedPreferences(getApplication().getResources().getString(R.string.cached_news_key), MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                Gson gson = new Gson();
                String json = gson.toJson(columnList);
                editor.putString(getApplication().getResources().getString(R.string.yazarlar_key), json);
                editor.apply();
            }
        }).start();
    }
}
