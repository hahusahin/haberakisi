package com.haberinadresi.androidapp.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.messaging.FirebaseMessaging;
import com.haberinadresi.androidapp.R;
import com.haberinadresi.androidapp.activities.SourceSelectionActivity;

public class SettingsFragment extends PreferenceFragmentCompat {

    private SharedPreferences sharedPreferences;
    private static final String alertTitle = "Bilgilendirme";
    private static final String alertMessage =
            "\t- Sadece haberlere 'Son Dakika' etiketi ekleyen haber kaynaklarından bildirim gelecektir." +
            "\n\n\t- Listede olmayan haber kaynakları haberlere 'Son Dakika' ifadesi koymadığı için alınmamıştır.";
    private static final String okay = "Tamam";

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

        // Son Dakika Notification Source Selector
        // Link that opens source selection activity to select Notification Sources
        final Preference notificationSourceSelection = findPreference(getResources().getString(R.string.notification_source_selection_key));
        // Enable/Disable the notification source selector
        if(sharedPreferences.getBoolean(getResources().getString(R.string.subscribed_to_lastminute_notifications), false)){
            notificationSourceSelection.setEnabled(true);
            notificationSourceSelection.getIcon().setAlpha(255);
        } else {
            notificationSourceSelection.setEnabled(false);
            notificationSourceSelection.getIcon().setAlpha(130);
        }
        notificationSourceSelection.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // First show the warning, then open source selection when clicked
                if(! sharedPreferences.getBoolean(getResources().getString(R.string.notification_source_info_seen), false)){
                    try {
                        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                        builder.setTitle(alertTitle);
                        builder.setMessage(alertMessage);
                        builder.setPositiveButton(okay, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent sourceSelection = new Intent(requireActivity(), SourceSelectionActivity.class);
                                sourceSelection.putExtra(getResources().getString(R.string.news_category_key), getResources().getString(R.string.bildirim_key));
                                sourceSelection.putExtra(getResources().getString(R.string.news_category_name),getResources().getString(R.string.bildirim_normal));
                                startActivity(sourceSelection);
                            }
                        });
                        builder.create().show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        sharedPreferences.edit().putBoolean(getResources().getString(R.string.notification_source_info_seen), true).apply();
                    }
                }
                // If the warning seen, open source selection directly
                else {
                    Intent sourceSelection = new Intent(requireActivity(), SourceSelectionActivity.class);
                    sourceSelection.putExtra(getResources().getString(R.string.news_category_key), getResources().getString(R.string.bildirim_key));
                    sourceSelection.putExtra(getResources().getString(R.string.news_category_name),getResources().getString(R.string.bildirim_normal));
                    startActivity(sourceSelection);
                }
                return true;
            }
        });

        // Son Dakika Notifications Enable/Disable Listener
        findPreference(getResources().getString(R.string.last_minute_notifications_key)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                // Subscribe the client app to the notification topic if enabled
                // AND make the notification settings link visible/invisible (For Android 8+)
                if((Boolean) newValue){
                    FirebaseMessaging.getInstance().subscribeToTopic(getResources().getString(R.string.last_minute_notifications_topic));
                    notificationSourceSelection.setEnabled(true);
                    notificationSourceSelection.getIcon().setAlpha(255);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                        openNotificationSettings.setEnabled(true);
                        openNotificationSettings.getIcon().setAlpha(255);
                    }
                } else {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(getResources().getString(R.string.last_minute_notifications_topic));
                    notificationSourceSelection.setEnabled(false);
                    notificationSourceSelection.getIcon().setAlpha(130);
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

        // Clear Image Cache Button
        Preference clearCache = findPreference(getResources().getString(R.string.clear_cache_key));
        clearCache.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                // Start the glide cleaner on background
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.get(requireActivity()).clearDiskCache();
                    }
                }).start();
                // Show message to user
                Toast.makeText(requireActivity(), getResources().getString(R.string.clear_cache_toast), Toast.LENGTH_SHORT).show();

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


        // Font Size Preference
        Preference fontSizePreference = findPreference(getResources().getString(R.string.pref_font_key));
        setFontSummary(fontSizePreference);

    }

    private void setFontSummary(Preference preference){
        preference.setOnPreferenceChangeListener(listener);
        listener.onPreferenceChange(preference, sharedPreferences.getString(preference.getKey(), "medium"));
    }

    private Preference.OnPreferenceChangeListener listener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String value = newValue.toString();
            if(preference instanceof ListPreference){
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(value);
                // set the summary to reflect new value
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
                switch (index){
                    case 0:
                        sharedPreferences.edit().putString(preference.getKey(), getResources().getString(R.string.pref_font_value_small)).apply();
                        break;
                    case 1:
                        sharedPreferences.edit().putString(preference.getKey(), getResources().getString(R.string.pref_font_value_medium)).apply();
                        break;
                    case 2:
                        sharedPreferences.edit().putString(preference.getKey(), getResources().getString(R.string.pref_font_value_large)).apply();
                        break;
                    case 3:
                        sharedPreferences.edit().putString(preference.getKey(), getResources().getString(R.string.pref_font_value_extra_large)).apply();
                        break;
                    default:
                        sharedPreferences.edit().putString(preference.getKey(), getResources().getString(R.string.pref_font_value_medium)).apply();
                }
            }
            return true;
        }
    };


    /*
    // Get the cache size and set it to the summary as kB, MB etc.. (ŞİMDİLİK BOYUTU GÖSTERMİYORUM)
    //long cacheSize = getCacheSize();
    //clearCache.setSummary(readableFileSize(cacheSize));

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
