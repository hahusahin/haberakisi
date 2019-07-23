package com.haberinadresi.androidapp.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.haberinadresi.androidapp.R;
import com.haberinadresi.androidapp.activities.MainActivity;
import com.haberinadresi.androidapp.activities.ShowInWebviewActivity;
import com.haberinadresi.androidapp.models.NotificationItem;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class LastMinuteNotifications extends FirebaseMessagingService {

    //public static final String TAG = "BILDIRIM";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        SharedPreferences customKeys = getSharedPreferences(getResources().getString(R.string.custom_keys), MODE_PRIVATE);
        long lastNotificationTime = customKeys.getLong(getResources().getString(R.string.last_notified_time), 0L);
        String topic = remoteMessage.getFrom();

        //Log.d(TAG, topic);
        //Log.d(TAG, String.valueOf(System.currentTimeMillis() - lastNotificationTime));

        // If message includes topic AND
        // if the last notification is at most 5 minutes old (to avoid showing notification over and over again)
        if(topic != null && System.currentTimeMillis() - lastNotificationTime > 300000L){
            // If message is coming from SON DAKIKA topic
            // Başlık şu şekilde çıkıyor (/topics/Son_Dakika), o yüzden contains kullandım
            if(topic.contains(getResources().getString(R.string.last_minute_notifications_topic))){
                // Check if message contains a data payload
                if (remoteMessage.getData().size() > 0) {
                    // Get the news data from message as String
                    String json = remoteMessage.getData().get("allnews");
                    if(json != null){
                        // Convert string to List of News Object
                        Gson gson = new Gson();
                        Type type = new TypeToken<ArrayList<NotificationItem>>() {}.getType();
                        List<NotificationItem> newsList = gson.fromJson(json, type);

                        // Find the appropriate news for user from the list (if exists)
                        NotificationItem notificationItem = findItem(newsList);

                        if(notificationItem != null){
                            // Intent to Open the clicked news in Webview
                            Intent newsDetailIntent = new Intent(this, ShowInWebviewActivity.class);
                            newsDetailIntent.putExtra(getResources().getString(R.string.news_url), notificationItem.getNewsUrl());
                            newsDetailIntent.putExtra(getResources().getString(R.string.news_source_for_display), notificationItem.getSource());

                            // Create a pattern (When clicked, open in Webview and after that when backpressed return to Main Activity)
                            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                            stackBuilder.addNextIntent(new Intent(this, MainActivity.class));
                            stackBuilder.addNextIntent(newsDetailIntent);

                            // Create pending intent (To give unique request codes to each intent, pass a random integer)
                            PendingIntent pendingIntent = stackBuilder.getPendingIntent(new Random().nextInt(),
                                    PendingIntent.FLAG_ONE_SHOT);

                            // Build the "Son Dakika" notification
                            Notification notification = new NotificationCompat.Builder(this, getResources().getString(R.string.last_minute_channel_id))
                                    .setSmallIcon(R.drawable.app_icon_notification)
                                    .setColor(getResources().getColor(R.color.colorPrimary))
                                    .setContentTitle(notificationItem.getSource()) // + " - Son Dakika"
                                    .setContentText(notificationItem.getTitle())
                                    .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationItem.getTitle()))
                                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                                    .setAutoCancel(true)
                                    .setContentIntent(pendingIntent)
                                    //.setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                    //.setSound()
                                    .build();

                            // Send the notification (To give unique integer id to each notification, pass a random integer)
                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
                            //int notificationId = (int) System.currentTimeMillis()%Integer.MAX_VALUE; (ALTERNATIVE RANDOM NUMBER)
                            notificationManager.notify(new Random().nextInt(), notification);

                            // Update last notification time in sharedpreferences
                            customKeys.edit().putLong(getResources().getString(R.string.last_notified_time), System.currentTimeMillis()).apply();
                        }
                    }
                }
            }

            // ELSE IF message is coming from another topic...
        }


    }

    // Get the first news/column item that is appropriate for the user (i.e. news source is in user's favorite sources)
    private NotificationItem findItem(List<NotificationItem> newsList){

        // Iterate over each news OR columns to check whether it is in user's preference sources
        SharedPreferences mySources = getSharedPreferences(getResources().getString(R.string.source_prefs_key), MODE_PRIVATE);
        SharedPreferences myColumnists = getSharedPreferences(getResources().getString(R.string.columnist_prefs_key), MODE_PRIVATE);
        final Locale turkish = new Locale("tr", "TR");

        List<NotificationItem> filteredList = new ArrayList<>();
        for(NotificationItem newsItem : newsList){
            if(mySources.contains(newsItem.getKey()) || myColumnists.contains(newsItem.getKey().toLowerCase(turkish))){
                filteredList.add(newsItem);
            }
        }


        // If there exists breaking news appropriate for user's preference
        if(! filteredList.isEmpty()){
            // Choose the item to be notified randomly
            int index = new Random().nextInt(filteredList.size());
            return filteredList.get(index);
        // No appropriate news exists
        } else {
            return null;
        }
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

}



/*

public class LastMinuteNotifications extends FirebaseMessagingService {


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        // Get the notification settings
        SharedPreferences customKeys = getSharedPreferences(getResources().getString(R.string.custom_keys), MODE_PRIVATE);
        boolean notificationsEnabled = customKeys.getBoolean(getResources().getString(R.string.last_minute_notifications_key), false);

        // Check if message contains a data payload && Notifications are enabled (double check)
        if (remoteMessage.getData().size() > 0 && notificationsEnabled) {

            // CHECK IF TOPIC IS "SON DAKIKA"
            Log.d("BILDIRIM", "mesaj geldi");

            // Fetch the breaking news from each source (that contains one news for each source)
            Gson gson = new GsonBuilder().setLenient().create();

            final OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(getResources().getString(R.string.api_link))
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build();

            API api = retrofit.create(API.class);
            Call<List<NewsItem>> call = api.getNotificationNews();

            call.enqueue(new Callback<List<NewsItem>>() {
                @Override
                public void onResponse(@NonNull Call<List<NewsItem>> call, @NonNull Response<List<NewsItem>> response) {

                    if(response.isSuccessful() && response.body() != null){
                        // Generate the notification message by using the response
                        createNotificationMessage(response.body());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<NewsItem>> call, @NonNull Throwable t) {
                    t.printStackTrace();
                }
            });

        }

    }

    private void createNotificationMessage(List<NewsItem> newsList) {
        // Find the appropriate news for user from the list
        NewsItem notificationItem = findItem(newsList);
        // If exists
        if(notificationItem != null){
            Log.d("BILDIRIM", "son haberler yuklendi");
            if(System.currentTimeMillis() - notificationItem.getUpdateTime() < 1200000L) { // 20 dk = 20*60*1000 = 1.200.000

                // Intent to Open the clicked news in Webview
                Intent newsDetailIntent = new Intent(this, ShowInWebviewActivity.class);
                newsDetailIntent.putExtra(getResources().getString(R.string.news_url), notificationItem.getNewsUrl());
                newsDetailIntent.putExtra(getResources().getString(R.string.news_source_for_display), notificationItem.getSource());

                // First open Main Activity then open Webview (to return the app when backpressed from clicked news)
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                stackBuilder.addNextIntent(new Intent(this, MainActivity.class));
                stackBuilder.addNextIntent(newsDetailIntent);

                // Create pending intent (To give unique request codes to each intent, pass a random integer)
                PendingIntent pendingIntent = stackBuilder.getPendingIntent(new Random().nextInt(), PendingIntent.FLAG_ONE_SHOT);

                // Build the "Son Dakika" notification
                Notification notification = new NotificationCompat.Builder(this, getResources().getString(R.string.last_minute_channel_id))
                        .setSmallIcon(R.drawable.app_icon_notification)
                        .setColor(getResources().getColor(R.color.colorPrimary))
                        .setContentTitle(notificationItem.getSource()) // + " - Son Dakika"
                        .setContentText(notificationItem.getTitle())
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationItem.getTitle()))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        //.setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        //.setSound()
                        .build();

                // Send the notification (To give unique integer id to each notification, pass a random integer)
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
                //int notificationId = (int) System.currentTimeMillis()%Integer.MAX_VALUE; (ALTERNATIVE RANDOM NUMBER)
                notificationManager.notify(new Random().nextInt(), notification);

            }

        }

    }

    // Get the first news item that is appropriate for the user (i.e. news source is in user's favorite sources)
    private NewsItem findItem(List<NewsItem> newsList){

        // Iterate over each news to check whether it is in user's preference sources
        SharedPreferences mySources = getSharedPreferences(getResources().getString(R.string.source_prefs_key), MODE_PRIVATE);
        for(NewsItem newsItem : newsList){
            if(mySources.contains(newsItem.getKey())){
                Log.d("BILDIRIM", newsItem.getNewsUrl());
                return newsItem;
            }
        }
        return null;
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

}

 */
