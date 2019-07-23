package com.haberinadresi.androidapp.utilities;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.browser.customtabs.CustomTabsIntent;

import com.haberinadresi.androidapp.R;

public class WebUtils {

    // Method that creates Chrome Custom Tab
    public static CustomTabsIntent createChromeTab(Context context, String url){

        CustomTabsIntent.Builder customTabBuilder = new CustomTabsIntent.Builder();
        // Custom Share Button
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_share_white);
        int requestCode = 100;
        Intent intentShare = new Intent(Intent.ACTION_SEND);
        intentShare.putExtra(Intent.EXTRA_TEXT, url);
        intentShare.setType("text/plain");

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, requestCode, intentShare, PendingIntent.FLAG_UPDATE_CURRENT);

        // Set the share button
        customTabBuilder.setActionButton(bitmap, context.getResources().getString(R.string.share_news), pendingIntent, true);
        // Color of the chrome tab
        customTabBuilder.setToolbarColor(context.getResources().getColor(R.color.colorPrimary));

        return customTabBuilder.build();
    }

    // Checks whether the app is updated (instead of newly installed) by comparing the times
    public static boolean isInstallFromUpdate(Context context) {
        try {
            long firstInstallTime = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).firstInstallTime;
            long lastUpdateTime = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).lastUpdateTime;
            return firstInstallTime != lastUpdateTime;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Method to check whether an app is installed in the device (by getting the package name)
    public static boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        }
        catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static String getPath11(Context context){
        return context.getResources().getString(R.string.tumu_path);
    }

    public static String getPath12(Context context){
        return context.getResources().getString(R.string.kaynak_path);
    }

}
