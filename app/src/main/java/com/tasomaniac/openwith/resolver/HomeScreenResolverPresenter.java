package com.tasomaniac.openwith.resolver;

import android.content.Intent;
import android.content.res.Resources;

import com.tasomaniac.openwith.redirect.RedirectFixActivity;
import com.tasomaniac.openwith.translations.R;

import javax.inject.Inject;

import timber.log.Timber;

class HomeScreenResolverPresenter implements ResolverPresenter {
    private final Resources resources;
    private final IntentResolver intentResolver;

    @Inject HomeScreenResolverPresenter(Resources resources, IntentResolver intentResolver) {
        this.resources = resources;
        this.intentResolver = intentResolver;
    }

    @Override
    public void bind(ResolverActivity view, ResolverView.Navigation navigation) {
        view.setListener(new ViewListener(intentResolver, view));
        intentResolver.bind(new IntentResolverListener(view, navigation));
    }

    @Override
    public void unbind(ResolverActivity view) {
        view.setListener(null);
        intentResolver.unbind();
    }

    @Override
    public void release() {
        intentResolver.release();
    }

    private class IntentResolverListener implements IntentResolver.Listener {

        private final ResolverView view;
        private final ResolverView.Navigation navigation;

        IntentResolverListener(ResolverView view, ResolverView.Navigation navigation) {
            this.view = view;
            this.navigation = navigation;
        }

        @Override
        public void onIntentResolved(IntentResolverResult result) {
            if (result.isEmpty()) {
                Timber.e("No app is found to handle url: %s", intentResolver.getSourceIntent().getDataString());
                view.toast(R.string.empty_resolver_activity);
                navigation.dismiss();
                return;
            }
            view.displayData(result);
            view.setTitle(resources.getString(R.string.add_to_homescreen));
        }

    }

    private static class ViewListener implements ResolverView.Listener {

        private final IntentResolver intentResolver;
        private final ResolverActivity activity;

        ViewListener(IntentResolver intentResolver, ResolverActivity activity) {
            this.intentResolver = intentResolver;
            this.activity = activity;
        }

        @Override
        public void onActionButtonClick(boolean always) {
            throw new IllegalStateException("Action buttons should not be visible for AddToHomeScreen");
        }

        @Override
        public void onItemClick(DisplayActivityInfo activityInfo) {
            Intent intent = activityInfo.intentFrom(intentResolver.getSourceIntent());
            activity.displayAddToHomeScreenDialog(activityInfo, intent);
        }

        @Override public void onUnshorten() {
            Intent intent = RedirectFixActivity.createIntent(activity, activity.getIntent().getDataString())
                    .putExtra(RedirectFixActivity.EXTRA_UNSHORT, true);
            activity.startActivity(intent);
            activity.dismiss();
        }

        @Override
        public void onPackagesChanged() {
            intentResolver.resolve();
        }
    }
}
