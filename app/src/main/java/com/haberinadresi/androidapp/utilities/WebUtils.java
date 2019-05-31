package com.haberinadresi.androidapp.utilities;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.customtabs.CustomTabsIntent;

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
        customTabBuilder.setActionButton(bitmap, context.getResources().getString(R.string.share), pendingIntent, true);
        // Color of the chrome tab
        customTabBuilder.setToolbarColor(context.getResources().getColor(R.color.colorPrimary));

        return customTabBuilder.build();
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
}
