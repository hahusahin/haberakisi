package com.haberinadresi.androidapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import com.haberinadresi.androidapp.R;

public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    SharedPreferences sharedPreferences;
    boolean isNightModeChanged = false;
    boolean isShowImagesChanged = false;
    boolean isFontSizeChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        sharedPreferences = getSharedPreferences(getResources().getString(R.string.custom_keys), MODE_PRIVATE);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home){
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(getResources().getString(R.string.night_mode_key))){
            isNightModeChanged = true;
        } else if(key.equals(getResources().getString(R.string.show_images_key))){
            isShowImagesChanged = true;
        } else if(key.equals(getResources().getString(R.string.pref_font_key))){
            isFontSizeChanged = true;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the change status of settings to bundle since activity is recreated when nightmode switch toggled
        outState.putBoolean(getResources().getString(R.string.night_mode_key), isNightModeChanged);
        outState.putBoolean(getResources().getString(R.string.show_images_key), isShowImagesChanged);
        outState.putBoolean(getResources().getString(R.string.pref_font_key), isFontSizeChanged);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore the state changes if activity is recreated
        isNightModeChanged = savedInstanceState.getBoolean(getResources().getString(R.string.night_mode_key), false);
        isShowImagesChanged = savedInstanceState.getBoolean(getResources().getString(R.string.show_images_key), false);
        isFontSizeChanged = savedInstanceState.getBoolean(getResources().getString(R.string.pref_font_key), false);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // If a preference changed then restart the main activity (to apply the changes since affects the main activity)
        if(isNightModeChanged || isShowImagesChanged || isFontSizeChanged){
            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            intent.putExtra(SettingsActivity.class.getName(), true);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }
}
