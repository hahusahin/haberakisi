package com.haberinadresi.androidapp.utilities;

public class ColumnJsoupSelectors {

    public static String getSelector(String sourceName){
        switch (sourceName){
            case "Fehmi Koru":
                return "h1.entry-title, .td-post-content p";
            case "Mahfi Eğilmez":
                return "h3.post-title, .entry-content p"; // TABLOLAR DÜZGÜN ÇIKMIYOR AMA YAPACAK DA BİŞEY YOK
            case "Uğur Gürses":
                return "h1.entry-title, .entry-content p";
            case "Akşam":
                return ".newsdetailwrap h1, .col-8 h1, div.texts p, div.news-text p"; // HEM NORMAL HEM SPOR VAR
            case "Artı Gerçek":
                return ".article-title, .article-spot, .article-body-container p";
            case "Aydınlık":
                return ".content-header h1, div.content-text h3, div.content-text p";
            case "Birgün":
                return ".detail__title, .detail__spot, .detail--author p";
            case "Cumhuriyet":
                return "h1.baslik, .haberMetni p";
            case "Diken":
                return "h1.entry-title, main.content > article.post .entry-content p";
            case "Fanatik":
                return ".author-detail__info__title, .news-detail__body__content p";
            case "Fotomaç":
                return "#article-title, .detail-text-content p";
            case "Gazete Duvar": // DENENMEDI HENÜZ
                return "header > h1, div.content-text p";
            case "Habertürk":
                return ".featured h1.title, article.newsArticle p";
            case "Hürriyet": // HEM NORMAL HEM SPOR VAR
                return "h1.title-news-detail, .news-description p, .news-text p, h2.rhd-article-spot, .rhd-all-article-detail  p";
            case "Karar":
                return "h1.content-title, div.text-content p, div.text-content > div:not([class])";
            case "Korkusuz":
                return "div#content h1, .author-the-content p, .author-the-content h2";
            case "Milli Gazete":
                return ".topbox h1, div.post-text p";
            case "Milliyet": // HEM NORMAL HEM SPOR VAR
                return "h1.article__title, .article__detail p, .article__content p";
            case "Odatv":
                return "h1[itemprop='name'], h2[itemprop='description'], span[itemprop='articleBody'] p";
            case "Posta":
                return ".author-column__header__title, .author-column__content p, .author-column__content h2";
            case "Sabah":
                return "h1.postCaption, div.newsBox p";
            case "Sözcü":
                return "h1.news-detail-title, .author-the-content h2, .author-the-content p";
            case "Star":
                return "div.title > h1, .author-content h3, .author-content p";
            case "T24":
                return "._2teaB > h1, ._1NMxy h3, ._1NMxy p";
            case "Takvim":
                return "h1#haberTitle, h2#haberSpot, #haberDescription p";
            case "Türkiye":
                return "h1.page_title, #article_body > div:not([class]), #article_body > p";
            case "Yeni Akit":
                return "h2.title, .article-content > .content p";
            case "Yeniçağ":
                return "h1[itemprop='name'], div[itemprop='articleBody']";
            case "Yeni Şafak":
                return ".title > h1, .main-col > .text p.non-card";
            default:
                return "";
        }
    }
}
