package com.haberinadresi.androidapp.interfaces;

import com.haberinadresi.androidapp.models.Columnist;
import com.haberinadresi.androidapp.models.NewsItem;
import com.haberinadresi.androidapp.models.SourceItem;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface API {

    @GET("{category}/{source}")
    Observable<List<NewsItem>> getNews(@Path("category") String category, @Path("source") String source);

    @GET("{category}/{source}")
    Call<List<NewsItem>> getNewsFromOneSource(@Path("category") String category, @Path("source") String source);

    @GET("{category}/{source}")
    Call<List<NewsItem>> getHeadlines(@Path("category") String category, @Path("source") String source);

    @GET("{category}/{source}")
    Call<List<NewsItem>> getTopNews(@Path("category") String category, @Path("source") String source);

    @GET("{category}/{source}")
    Call<List<Columnist>> getColumns(@Path("category") String category, @Path("source") String source);

    @GET("{category}/{source}")
    Call<List<Columnist>> getColumnists(@Path("category") String category, @Path("source") String source);

    @GET("{path}/{category}")
    Call<List<SourceItem>> getSourcesByCategory(@Path("path") String path, @Path("category") String category);

}
