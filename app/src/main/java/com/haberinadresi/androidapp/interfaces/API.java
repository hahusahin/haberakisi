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

    @GET("allnews/mansetler")
    Call<List<NewsItem>> getHeadlines();

    @GET("allnews/yazarlar")
    Call<List<Columnist>> getColumns();

    @GET("allsources/yazarlar")
    Call<List<Columnist>> getColumnistSources();

    @GET("allsources/{category}")
    Call<List<SourceItem>> getSourcesByCategory(@Path("category") String category);


    //////////////////// KULLANILMIYOR//////////////////////////

    @GET("gundem/{source}")
    Observable<List<NewsItem>> getTopNews(@Path("source") String source);

    /*
    @GET("spor/{source}")
    Observable<List<NewsItem>> getSportNews(@Path("source") String source);

    @GET("teknoloji/{source}")
    Observable<List<NewsItem>> getTechnoNews(@Path("source") String source);

    @GET("magazin/{source}")
    Observable<List<NewsItem>> getMagazineNews(@Path("source") String source);

    @GET("kultursanat/{source}")
    Observable<List<NewsItem>> getArtNews(@Path("source") String source);

    @GET("saglik/{source}")
    Observable<List<NewsItem>> getHealthNews(@Path("source") String source);

    @GET("allsources/all")
    Call<List<SourceItem>> getallSources();

    @GET("allnews/gundem")
    Call<List<NewsItem>> getTopNews();

    @GET("allnews/spor")
    Call<List<NewsItem>> getSportNews();

    @GET("allnews/teknoloji")
    Call<List<NewsItem>> getTechnoNews();

    @GET("allnews/magazin")
    Call<List<NewsItem>> getMagazineNews();
    */


}
