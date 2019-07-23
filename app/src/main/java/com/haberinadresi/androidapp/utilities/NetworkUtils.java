package com.haberinadresi.androidapp.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import androidx.annotation.IntRange;

import com.haberinadresi.androidapp.R;

public class NetworkUtils {

    // Returns whether connected to internet or not
    public static boolean isConnected(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // Returns whether user want to display images only on Wifi connection or not
    public static boolean displayOnlyInWifi(Context context){
        // If connection is not via 4G, then return false immediately
        if(getConnectionType(context) != 2){
            return false;
        } else {
            // Else, check the user's mobile data saving preference and return it
            SharedPreferences customKeys = context.getSharedPreferences(context.getResources().getString(R.string.custom_keys), Context.MODE_PRIVATE);
            return customKeys.getBoolean(context.getResources().getString(R.string.show_images_key), false);
        }
    }

    // Returns active connection type of the user. 0: none; 1:wifi ; 2: mobile data
    @IntRange(from = 0, to = 2)
    private static int getConnectionType(Context context) {
        int result = 0;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        // If device is above API level 23
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (cm != null) {
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
                if (capabilities != null) { // if connected to the internet
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        result = 1;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        result = 2;
                    }
                }
            }
            // If device is below API level 23
        } else {
            if (cm != null) {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork != null) { // if connected to the internet
                    if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                        result = 1;
                    } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                        result = 2;
                    }
                }
            }
        }
        return result;
    }

    public static String getPath21(Context context){
        return context.getResources().getString(R.string.manset_path);
    }

    public static String getPath22(Context context){
        return context.getResources().getString(R.string.yazilar_path);
    }

    public static String getPath23(Context context){
        return context.getResources().getString(R.string.gundem_path);
    }

    /*
    new InternetCheck(new InternetCheck.Consumer() {
        @Override
        public void accept(Boolean internet) {
            if(internet){
                fetchColumns(view);
            }
            // else show internet connection error
            else {
                internetAlert.setVisibility(View.VISIBLE);
                // when user clicks on button, check the network connection again
                checkConnection.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // if connects after button click, then dismiss internet error
                        if(isConnected()){
                            fetchColumns(view);
                            internetAlert.setVisibility(View.GONE);
                        }
                    }
                });
            }
        }
    });

    static class InternetCheck extends AsyncTask<Void,Void,Boolean> {

        private Consumer mConsumer;
        public  interface Consumer { void accept(Boolean internet); }

        private InternetCheck(Consumer consumer) {
            mConsumer = consumer;
            execute();
        }

        @Override protected Boolean doInBackground(Void... voids) {
            try {
                Socket sock = new Socket();
                sock.connect(new InetSocketAddress("8.8.8.8", 53), 1500);
                sock.close();
                return true;
            }
            catch (IOException e) {
                return false;
            }
        }

        @Override protected void onPostExecute(Boolean internet) {
            mConsumer.accept(internet);
        }
    }

    */


}
