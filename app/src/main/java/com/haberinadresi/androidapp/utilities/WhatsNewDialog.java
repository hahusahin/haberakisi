package com.haberinadresi.androidapp.utilities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.haberinadresi.androidapp.activities.SettingsActivity;

public class WhatsNewDialog extends AppCompatDialogFragment {

    public static final String title = "Bilgilendirme";
    public static final String message =  "\tHaber Bildirimi ayarlarında değişikliğe gidilmiştir. Bildirim almaya devam etmek için " +
            "Ana Menü - Ayarlar'dan bildirim kaynağı seçmeniz gerekmektedir.";
    private static final String goToSettings = "Ayarlar'a Git";
    private static final String okay = "Tamam";

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(title)
                .setMessage(message)
                .setNeutralButton(goToSettings, (dialog, which) -> {
                    Intent settings = new Intent(requireActivity(), SettingsActivity.class);
                    startActivity(settings);
                })
                .setPositiveButton(okay, null);

        return builder.create();
    }
}
