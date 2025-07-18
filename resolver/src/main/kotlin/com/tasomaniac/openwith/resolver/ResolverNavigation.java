package com.tasomaniac.openwith.resolver;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.tasomaniac.openwith.translations.R;
import com.tasomaniac.openwith.util.Intents;

import javax.inject.Inject;

import timber.log.Timber;

class ResolverNavigation implements ResolverView.Navigation {

    private final Activity activity;

    @Inject ResolverNavigation(ResolverActivity activity) {
        this.activity = activity;
    }

    @Override
    public void startSelected(Intent intent) {
        if (activity.isFinishing()) {
            return;
        }
        try {
            Intents.startActivityFixingIntent(activity, intent);
            dismiss();
        } catch (Exception e) {
            Timber.e(e);
            Toast.makeText(activity, R.string.error_cannot_start_activity, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void startPreferred(Intent intent, CharSequence appLabel) {
        displayWarning(intent, appLabel);
        Intents.startActivityFixingIntent(activity, intent);
    }

    private void displayWarning(Intent intent, CharSequence appLabel) {
        String message = activity.getString(R.string.warning_open_link_with_name, appLabel);
        if (BuildConfig.DEBUG) {
            message += "\nURL: " + intent.getDataString();
        }
        int length = BuildConfig.DEBUG ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
        Toast.makeText(activity, message, length).show();
    }

    @Override
    public void dismiss() {
        if (!activity.isFinishing()) {
            activity.finish();
        }
    }
}
