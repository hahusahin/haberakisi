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

public class EditCategoriesDialog extends AppCompatDialogFragment {

    public static final String title = "Bilgilendirme";
    public static final String message = "- Yerini değiştirmek istediğiniz kategoriye UZUN BASIP sürükleyerek istediğiniz " +
            "yere taşıyabilirsiniz.\n\n- İstemediğiniz kategoriyi kutucuklara tıklayarak çıkarabilirsiniz.";
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
                        SharedPreferences.Editor editor = requireActivity().getSharedPreferences(getResources().getString(R.string.custom_keys), Context.MODE_PRIVATE).edit();
                        editor.putBoolean(getResources().getString(R.string.is_category_dialog_seen), true).apply();
                    }
                });

        return builder.create();
    }
}
