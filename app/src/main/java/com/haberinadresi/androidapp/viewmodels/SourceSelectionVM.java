package com.haberinadresi.androidapp.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.haberinadresi.androidapp.R;
import com.haberinadresi.androidapp.interfaces.API;
import com.haberinadresi.androidapp.models.SourceItem;
import com.haberinadresi.androidapp.utilities.WebUtils;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class SourceSelectionVM extends AndroidViewModel {

    public SourceSelectionVM(Application application){
        super(application);
    }

    private MutableLiveData<List<SourceItem>> sourcesLiveData;
    private final String path1 = WebUtils.getPath12(getApplication().getApplicationContext());

    //call this method to get the data
    public LiveData<List<SourceItem>> getSourcesLiveData(String category) {

        //if the list is null
        if (sourcesLiveData == null) {
            sourcesLiveData = new MutableLiveData<>();
            //load it asynchronously from server in this method
            loadSources(category);
        }

        return sourcesLiveData;
    }

    //This method is using Retrofit to get the JSON data from URL
    private void loadSources(final String category) {

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

        //Make the related http request depending on the clicked sources category
        Call<List<SourceItem>> call = api.getSourcesByCategory(path1, category);

        if (call != null) {

            call.enqueue(new Callback<List<SourceItem>>() {
                @Override
                public void onResponse(@NonNull Call<List<SourceItem>> call, @NonNull Response<List<SourceItem>> response) {

                    if(response.isSuccessful() && response.body() != null){
                        //set the list to our MutableLiveData
                        sourcesLiveData.setValue(response.body());
                    }

                }

                @Override
                public void onFailure(@NonNull Call<List<SourceItem>> call, @NonNull Throwable t) {
                    t.printStackTrace();
                }
            });
        }
    }
}
