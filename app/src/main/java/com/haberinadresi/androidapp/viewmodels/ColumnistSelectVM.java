package com.haberinadresi.androidapp.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import com.haberinadresi.androidapp.R;
import com.haberinadresi.androidapp.interfaces.API;
import com.haberinadresi.androidapp.models.Columnist;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ColumnistSelectVM extends AndroidViewModel {

    public ColumnistSelectVM(Application application){
        super(application);
    }


    //this is the data that we will fetch asynchronously
    private MutableLiveData<List<Columnist>> columnistLiveData;

    //call this method to get the data
    public LiveData<List<Columnist>> getColumnistLiveData() {

        //if the list is null
        if (columnistLiveData == null) {
            columnistLiveData = new MutableLiveData<>();
            //load it asynchronously from server in this method
            loadColumnists();
        }

        return columnistLiveData;
    }

    //This method is using Retrofit to get the JSON data from URL
    private void loadColumnists() {

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
        Call<List<Columnist>> call = api.getColumnistSources();

        call.enqueue(new Callback<List<Columnist>>() {
            @Override
            public void onResponse(@NonNull Call<List<Columnist>> call, @NonNull Response<List<Columnist>> response) {

                //set the list to our MutableLiveData
                columnistLiveData.setValue(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<List<Columnist>> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
