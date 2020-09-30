package com.haberinadresi.androidapp.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
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

        //SharedPreferences customKeys = getSharedPreferences(getResources().getString(R.string.custom_keys), MODE_PRIVATE);
        //long lastNotificationTime = customKeys.getLong(getResources().getString(R.string.last_notified_time), 0L);
        SharedPreferences notifiedNews = getSharedPreferences(getResources().getString(R.string.notified_news_key), MODE_PRIVATE);

        String topic = remoteMessage.getFrom();

        //Log.d(TAG, topic);
        //Log.d(TAG, String.valueOf(System.currentTimeMillis() - lastNotificationTime));

        // If message includes topic AND
        // if the last notification is at most 1 minutes old (to avoid showing notification over and over again)
        //&& System.currentTimeMillis() - lastNotificationTime > 60000L
        if(topic != null){
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
                        List<NotificationItem> notificationItems = filterList(newsList, notifiedNews);

                        if(notificationItems != null){
                            // Send notification for each news
                            for(NotificationItem notificationItem : notificationItems) {
                                // Intent to Open the clicked news in Webview
                                Intent notificationIntent = new Intent(this, ShowInWebviewActivity.class);
                                notificationIntent.putExtra(getResources().getString(R.string.news_url), notificationItem.getNewsUrl());
                                notificationIntent.putExtra(getResources().getString(R.string.news_source_for_display), notificationItem.getSource());
                                notificationIntent.putExtra(getResources().getString(R.string.news_source_key), notificationItem.getKey());

                                // Create a pattern (When clicked, open in Webview and after that when backpressed return to Main Activity)
                                TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                                stackBuilder.addNextIntent(new Intent(this, MainActivity.class));
                                stackBuilder.addNextIntent(notificationIntent);

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

                                // Update last notification time in sharedpreferences
                                //customKeys.edit().putLong(getResources().getString(R.string.last_notified_time), System.currentTimeMillis()).apply();

                                // Add the news to the notified items
                                notifiedNews.edit().putLong(notificationItem.getNewsUrl(), System.currentTimeMillis()).apply();

                            }
                        }

                    }
                }
            }

            // ELSE IF message is coming from another topic...
        }

    }

    // Get the first news/column item that is appropriate for the user (i.e. news source is in user's favorite sources)
    private List<NotificationItem> filterList(List<NotificationItem> newsList, SharedPreferences notifiedNews){

        // Iterate over each news OR columns to check whether it is in user's preference sources
        SharedPreferences notificationSources = getSharedPreferences(getResources().getString(R.string.notification_prefs_key), MODE_PRIVATE);
        SharedPreferences myColumnists = getSharedPreferences(getResources().getString(R.string.columnist_prefs_key), MODE_PRIVATE);
        final Locale turkish = new Locale("tr", "TR");

        List<NotificationItem> filteredList = new ArrayList<>();
        for(NotificationItem newsItem : newsList){
            // If the news' source is preferred as notification source by user
            // OR column notification is preferred and the columnist is in the user's preferences
            if(notificationSources.contains(newsItem.getKey()) ||
                (notificationSources.contains("koseyazilari_gundem") && myColumnists.contains(newsItem.getKey().toLowerCase(turkish))))
            {
                // If this news is not notified before then add to list
                if(! notifiedNews.contains(newsItem.getNewsUrl())){
                    filteredList.add(newsItem);
                }
            }
        }

        // If there exists breaking news appropriate for user's preference
        if(! filteredList.isEmpty()){
            /*
            // Choose the item to be notified randomly
            int index = new Random().nextInt(filteredList.size());
            return filteredList.get(index);
             */
            return filteredList;
        }
        // No appropriate news exists
        else {
            return null;
        }
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }
}