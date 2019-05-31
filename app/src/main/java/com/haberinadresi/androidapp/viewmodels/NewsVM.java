package com.haberinadresi.androidapp.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.haberinadresi.androidapp.R;
import com.haberinadresi.androidapp.interfaces.API;
import com.haberinadresi.androidapp.models.NewsItem;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.Context.MODE_PRIVATE;

public class NewsVM extends AndroidViewModel {

    private MutableLiveData<List<NewsItem>> newsLiveData;

    public NewsVM(Application application) { super(application); }

    //load news from server OR from shared preferences and attach to livedata
    public LiveData<List<NewsItem>> getNewsLivedata(String category, ArrayList<String> sourceList, boolean isPreferenceChanged) {

        newsLiveData = new MutableLiveData<>();

        if(loadNewsFromSP(category).isEmpty() || isPreferenceChanged){
            // If the cache is empty OR there is change in the preferences then load from server
            loadNewsFromServer(category, sourceList);

        } else {
            // If there exists data in cache and there is no change in preferences then load it
            newsLiveData.postValue(loadNewsFromSP(category));
        }
        return newsLiveData;
    }


    //@SuppressWarnings("unchecked cast")
    private void loadNewsFromServer(final String category, ArrayList<String> sourceList) {

        Gson gson = new GsonBuilder().setLenient().create();

        final OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getApplication().getResources().getString(R.string.api_link))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client)
                .build();

        API api = retrofit.create(API.class);

        //Gathering the requests into list of observables
        List<Observable<?>> requests = new ArrayList<>();
        for(String source: sourceList){
            requests.add(api.getNews(category, source));
        }

        Observable.zip(requests, new Function<Object[], List<NewsItem>>() {
            @Override
            public List<NewsItem> apply(Object[] objects) throws Exception {
                List<NewsItem> combinedNews = new ArrayList<>();
                for (Object response : objects) {
                    if (response instanceof List<?>) {
                        combinedNews.addAll((List<NewsItem>) response);
                    }
                }
                Collections.sort(combinedNews, new Comparator<NewsItem>() {
                    @Override
                    public int compare(NewsItem news1, NewsItem news2) {
                        return Long.compare(news2.getUpdateTime(), news1.getUpdateTime());
                    }
                });
                return combinedNews;
            }
        })
            .subscribeOn(Schedulers.newThread()) //Schedulers.io()
            .observeOn(AndroidSchedulers.mainThread()) //Schedulers.newThread()
            .subscribe(new Observer<List<NewsItem>>() {
                @Override
                public void onSubscribe(Disposable d) {

                }
                @Override
                public void onNext(List<NewsItem> newsList) {
                    // Attach the result of response to the livedata and save it to sharedpreferences
                    newsLiveData.postValue(newsList);
                    saveNewsToSP(category, newsList);
                }
                @Override
                public void onError(Throwable e) {
                    e.printStackTrace();
                }
                @Override
                public void onComplete() {

                }
            });
    }

    private List<NewsItem> loadNewsFromSP(String category) {
        Gson gson = new Gson();
        SharedPreferences sharedPreferences = getApplication().getSharedPreferences(getApplication().getResources().getString(R.string.cached_news_key), MODE_PRIVATE);
        String json = sharedPreferences.getString(category, null);
        long lastFetchTime = sharedPreferences.getLong(getFetchTimeKey(category), 0L);
        long timeSlot = System.currentTimeMillis() - lastFetchTime;
        //If the user fetched news recently (at most 15-30 minutes ago) and there exists data in cache
        if (json != null && timeSlot < maxCacheTime(category)) {
            Type type = new TypeToken<ArrayList<NewsItem>>() {}.getType();
            return gson.fromJson(json, type);
        } else {
            return new ArrayList<>();
        }
    }

    private void saveNewsToSP(final String category, final List<NewsItem> newsList) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sharedPreferences = getApplication().getSharedPreferences(getApplication().getResources().getString(R.string.cached_news_key), MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                Gson gson = new Gson();
                String json = gson.toJson(newsList);
                editor.putString(category, json);
                editor.putLong(getFetchTimeKey(category), System.currentTimeMillis());
                editor.apply();
            }
        }).start();
    }
    
    private String getFetchTimeKey(String category){

        if(category.equals(getApplication().getResources().getString(R.string.gundem_key))){
            return getApplication().getResources().getString(R.string.tops_last_fetch_time);
        } else if(category.equals(getApplication().getResources().getString(R.string.spor_key))){
            return getApplication().getResources().getString(R.string.sport_last_fetch_time);
        } else if(category.equals(getApplication().getResources().getString(R.string.kultursanat_key))){
            return getApplication().getResources().getString(R.string.art_last_fetch_time);
        } else if(category.equals(getApplication().getResources().getString(R.string.saglik_key))){
            return getApplication().getResources().getString(R.string.health_last_fetch_time);
        } else if(category.equals(getApplication().getResources().getString(R.string.magazin_key))){
            return getApplication().getResources().getString(R.string.magazine_last_fetch_time);
        } else if(category.equals(getApplication().getResources().getString(R.string.teknoloji_key))){
            return getApplication().getResources().getString(R.string.techno_last_fetch_time);
        } else {
            return "... Son Kaydedilen Zaman";
        }
    }

    // Keep gundem news in cache for at most 15 minutes, for others 30 minutes
    private long maxCacheTime(String category){
        if(category.equals(getApplication().getResources().getString(R.string.gundem_key))){
            return 900000L; // 15 * 60 * 1000 = 900000 (15 dk)
        } else {
            return 1800000L; // 30 * 60 * 1000 = 900000 (30dk)
        }
    }
}
