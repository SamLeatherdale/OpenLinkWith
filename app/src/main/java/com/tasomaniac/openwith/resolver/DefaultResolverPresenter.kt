package com.tasomaniac.openwith.resolver

import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import com.tasomaniac.openwith.data.PreferredApp
import com.tasomaniac.openwith.redirect.RedirectFixActivity
import com.tasomaniac.openwith.translations.R
import timber.log.Timber
import javax.inject.Inject

internal class DefaultResolverPresenter @Inject constructor(
    private val resources: Resources,
    private val sourceIntent: Intent,
    private val callerPackage: CallerPackage,
    private val useCase: ResolverUseCase,
    private val viewState: ViewState
) : ResolverPresenter {

    override fun bind(view: ResolverActivity, navigation: ResolverView.Navigation) {
        view.setListener(ViewListener(view, navigation))
        useCase.bind(UseCaseListener(view, navigation))
    }

    override fun unbind(view: ResolverActivity) {
        view.setListener(null)
        useCase.unbind()
    }

    override fun release() {
        useCase.release()
    }

    private inner class UseCaseListener(
        private val view: ResolverView,
        private val navigation: ResolverView.Navigation
    ) : ResolverUseCase.Listener {

        @Suppress("TooGenericExceptionCaught")
        override fun onPreferredResolved(
            uri: Uri,
            preferredApp: PreferredApp,
            displayActivityInfo: DisplayActivityInfo
        ) {
            if (preferredApp.preferred.not()) return
            if (displayActivityInfo.packageName().isCaller()) return

            try {
                val preferredIntent = Intent(Intent.ACTION_VIEW, uri)
                    .setComponent(preferredApp.componentName)
                navigation.startPreferred(preferredIntent, displayActivityInfo.displayLabel)
                view.dismiss()
            } catch (e: Exception) {
                Timber.e(e, "Security Exception for the url: %s", uri)
                useCase.deleteFailedHost(uri)
            }
        }

        private fun String.isCaller() = this == callerPackage.callerPackage

        override fun onIntentResolved(result: IntentResolverResult) {
            viewState.filteredItem = result.filteredItem

            val handled = handleQuick(result)
            if (handled) {
                navigation.dismiss()
            } else {
                view.displayData(result)
                view.setTitle(titleForAction(result.filteredItem))
                view.setupActionButtons()
            }
        }

        @Suppress("TooGenericExceptionCaught")
        private fun handleQuick(result: IntentResolverResult) = when {
            result.isEmpty -> {
                Timber.e("No app is found to handle url: %s", sourceIntent.dataString)
                view.toast(R.string.empty_resolver_activity)
                true
            }
            else -> false
        }

        private fun titleForAction(filteredItem: DisplayActivityInfo?): String {
            return if (filteredItem != null) {
                resources.getString(R.string.which_view_application_named, filteredItem.displayLabel)
            } else {
                resources.getString(R.string.which_view_application)
            }
        }
    }

    private inner class ViewListener(
        private val view: ResolverActivity,
        private val navigation: ResolverView.Navigation
    ) : ResolverView.Listener {

        override fun onActionButtonClick(always: Boolean) {
            startAndPersist(viewState.checkedItem(), always)
            navigation.dismiss()
        }

        override fun onItemClick(activityInfo: DisplayActivityInfo) {
            if (viewState.shouldUseAlwaysOption() && activityInfo != viewState.lastSelected) {
                view.enableActionButtons()
                viewState.lastSelected = activityInfo
            } else {
                startAndPersist(activityInfo, alwaysCheck = false)
            }
        }

        override fun onUnshorten() {
            val intent = RedirectFixActivity.createIntent(view, view.intent.dataString ?: throw Error("No data in intent"))
                .putExtra(RedirectFixActivity.EXTRA_UNSHORT, true)
            view.startActivity(intent)
            view.dismiss()
        }

        private fun startAndPersist(activityInfo: DisplayActivityInfo, alwaysCheck: Boolean) {
            val intent = activityInfo.intentFrom(sourceIntent)
            navigation.startSelected(intent)
            useCase.persistSelectedIntent(intent, alwaysCheck)
        }

        override fun onPackagesChanged() {
            useCase.resolve()
        }
    }
}
