package com.tasomaniac.openwith.settings.other;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.tasomaniac.openwith.R;
import com.tasomaniac.openwith.translations.R.string;

import javax.annotation.Nullable;

public class LicensesDialogFragment extends AppCompatDialogFragment {

    public static LicensesDialogFragment newInstance() {
        return new LicensesDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_licenses, null);

        WebView licenses = view.findViewById(R.id.licenses);
        licenses.loadUrl("file:///android_asset/open_source_licenses.html");

        return new AlertDialog.Builder(requireContext())
                .setTitle(string.pref_title_open_source)
                .setView(view)
                .setPositiveButton(android.R.string.ok, null)
                .create();
    }

}
