package com.haberinadresi.androidapp.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.messaging.FirebaseMessaging;
import com.haberinadresi.androidapp.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    SharedPreferences sharedPreferences;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {

        addPreferencesFromResource(R.xml.preferences);

        sharedPreferences = requireActivity().getSharedPreferences(getResources().getString(R.string.custom_keys), Context.MODE_PRIVATE);

        // Day - Night switch Change Listener
        findPreference(getResources().getString(R.string.night_mode_key)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                // Update shared preferences night_mode key
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(getResources().getString(R.string.night_mode_key), (Boolean) newValue).apply();
                // Apply day night theme and restart the settings activity
                AppCompatDelegate.setDefaultNightMode((Boolean) newValue ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
                requireActivity().recreate();
                return true;
            }
        });

        // Show Images Only on WIFI Switch Change Listener
        findPreference(getResources().getString(R.string.show_images_key)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                // Update shared preferences key
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(getResources().getString(R.string.show_images_key), (Boolean) newValue).apply();
                return true;
            }
        });


        // Link that opens Notification Settings (For Android 8+) if notifications are enabled
        final Preference openNotificationSettings = findPreference(getResources().getString(R.string.open_notification_settings_key));
        // If android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            // Show the link
            openNotificationSettings.setVisible(true);
            openNotificationSettings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, requireActivity().getPackageName());
                    intent.putExtra(Settings.EXTRA_CHANNEL_ID, getResources().getString(R.string.last_minute_channel_id));
                    startActivity(intent);
                    return true;
                }
            });
            // If user already enabled the notifications
            if(sharedPreferences.getBoolean(getResources().getString(R.string.subscribed_to_lastminute_notifications), false)){
                // Enable the link that opens notification settings
                openNotificationSettings.setEnabled(true);
                openNotificationSettings.getIcon().setAlpha(255);

            } else {
                // Disable the link
                openNotificationSettings.setEnabled(false);
                openNotificationSettings.getIcon().setAlpha(130);
            }
        // Hide the link
        } else {
            openNotificationSettings.setVisible(false);
        }

        // Son Dakika Notifications Enable/Disable Listener
        findPreference(getResources().getString(R.string.last_minute_notifications_key)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                // Subscribe the client app to the notification topic if enabled
                // AND make the notification settings link visible/invisible (For Android 8+)
                if((Boolean) newValue){
                    FirebaseMessaging.getInstance().subscribeToTopic(getResources().getString(R.string.last_minute_notifications_topic));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                        openNotificationSettings.setEnabled(true);
                        openNotificationSettings.getIcon().setAlpha(255);
                    }
                } else {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(getResources().getString(R.string.last_minute_notifications_topic));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                        openNotificationSettings.setEnabled(false);
                        openNotificationSettings.getIcon().setAlpha(130);
                    }
                }
                // Update shared preferences key
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(getResources().getString(R.string.subscribed_to_lastminute_notifications), (Boolean) newValue).apply();
                return true;
            }
        });

        // Open with Web Browser Switch Change Listener
        Preference chromePreference = findPreference(getResources().getString(R.string.open_with_browser_key));
        chromePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                // Update shared preferences key
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(getResources().getString(R.string.open_with_browser_key), (Boolean) newValue).apply();
                return true;
            }
        });

        // Clear Image Cache Button
        Preference clearCache = findPreference(getResources().getString(R.string.clear_cache_key));
        // Get the cache size and set it to the summary as kB, MB etc.. (ŞİMDİLİK BOYUTU GÖSTERME)
        //long cacheSize = getCacheSize();
        //clearCache.setSummary(readableFileSize(cacheSize));
        clearCache.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                Toast.makeText(requireActivity(), getResources().getString(R.string.clear_cache_toast), Toast.LENGTH_SHORT).show();
                // Start the glide cleaner on background
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.get(requireActivity()).clearDiskCache();
                    }
                }).start();

                return true;
            }
        });

        // Privacy Policy
        Preference privacyPolicy = findPreference(getResources().getString(R.string.privacy_policy_key));
        privacyPolicy.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.privacy_policy_link)));
                startActivity(intent);
                return true;
            }
        });

    }


    /*
    // YAZI BOYUTUNU ARTIRMA/AZALTMA İÇİN

    //bindSummaryValue(findPreference(getResources().getString(R.string.pref_font_key)));


        private static void bindSummaryValue(Preference preference){
            preference.setOnPreferenceChangeListener(listener);
            listener.onPreferenceChange(preference,
                    PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
        }

        private static Preference.OnPreferenceChangeListener listener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String value = newValue.toString();
                if(preference instanceof ListPreference){
                    ListPreference listPreference = (ListPreference) preference;
                    int index = listPreference.findIndexOfValue(value);
                    // set the summary to reflect new value
                    preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
                }
                return true;
            }
        };
     */


    /*
    // 3 Helper Methods to get the Cache Size of the Images
    public long getCacheSize(){
        long size = 0;
        size += getDirSize(requireActivity().getCacheDir());
        size += getDirSize(requireActivity().getExternalCacheDir());
        return size;
    }

    public long getDirSize(File dir){
        long size = 0;
        for (File file : dir.listFiles()) {
            if (file != null && file.isDirectory()) {
                size += getDirSize(file);
            } else if (file != null && file.isFile()) {
                size += file.length();
            }
        }
        return size;
    }
    // Converts the cache size from long to string to display it
    public static String readableFileSize(long size) {
        if (size <= 0) return "0 Bytes";
        final String[] units = new String[]{"Bytes", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
    */

}
