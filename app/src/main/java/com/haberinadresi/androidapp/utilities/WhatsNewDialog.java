package com.haberinadresi.androidapp.utilities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;

import com.haberinadresi.androidapp.R;

public class WhatsNewDialog extends AppCompatDialogFragment {

    public static final String title = "Neler Yeni?";
    public static final String message =
            "\t- Haberlerin bildirim olarak gösterilmesi." +
            "\n\n\t- Haber linklerini Chrome vb. internet tarayıcısı ile açma seçeneği." +
            "\n\n\t(Ana Menü - Ayarlar'dan ulaşabilirsiniz)";
    public static final String okay = "Tamam";

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(okay, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences customPrefs = requireActivity().getSharedPreferences(getResources().getString(R.string.custom_keys), Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = customPrefs.edit();
                        // Delete the previous keys that are used
                        editor.remove(getResources().getString(R.string.is_whats_new_dialog_seen));
                        editor.remove(getResources().getString(R.string.whats_new_dialog_v2));
                        // Put the new key
                        editor.putBoolean(getResources().getString(R.string.whats_new_dialog_v3), true);
                        editor.apply();
                    }
                });

        return builder.create();
    }
}
