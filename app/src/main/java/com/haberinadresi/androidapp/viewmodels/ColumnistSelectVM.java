package com.haberinadresi.androidapp.viewmodels;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.annotation.NonNull;

import com.haberinadresi.androidapp.R;
import com.haberinadresi.androidapp.interfaces.API;
import com.haberinadresi.androidapp.models.Columnist;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.haberinadresi.androidapp.utilities.NetworkUtils;
import com.haberinadresi.androidapp.utilities.WebUtils;

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

    private MutableLiveData<List<Columnist>> columnistLiveData;
    private final String path1 = WebUtils.getPath12(getApplication().getApplicationContext());
    private final String path2 = NetworkUtils.getPath22(getApplication().getApplicationContext());

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
        Call<List<Columnist>> call = api.getColumnists(path1, path2);

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
