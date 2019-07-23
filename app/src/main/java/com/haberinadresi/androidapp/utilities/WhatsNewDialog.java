package com.haberinadresi.androidapp.utilities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.haberinadresi.androidapp.R;

public class WhatsNewDialog extends AppCompatDialogFragment {

    public static final String title = "Neler Yeni?";
    public static final String message =
            "\t- 3 Farklı Haber Gösterim İmkanı Getirildi" +
            "\n\n\t(Sağ üstteki ikona basarak geçiş yapabilirsiniz)";
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
                        editor.remove(getResources().getString(R.string.whats_new_dialog_v3));
                        editor.remove(getResources().getString(R.string.whats_new_dialog_v4));
                        // Put the new key
                        editor.putBoolean(getResources().getString(R.string.whats_new_dialog_v5), true);
                        editor.apply();
                    }
                });

        return builder.create();
    }
}
