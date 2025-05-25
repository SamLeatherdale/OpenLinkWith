package com.tasomaniac.openwith.settings.advanced.features

import android.content.Context
import android.os.Bundle
import androidx.annotation.Keep
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.tasomaniac.openwith.data.Analytics
import com.tasomaniac.openwith.translations.R
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

@Keep
class FeaturesListFragment :
    PreferenceFragmentCompat(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    @Inject lateinit var settings: FeaturesListSettings
    @Inject lateinit var analytics: Analytics

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        settings.setup()
        if (savedInstanceState == null) {
            analytics.sendScreenView("FeaturesList")
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().setTitle(R.string.pref_title_features)
        settings.resume()
    }

    override fun onPause() {
        settings.pause()
        super.onPause()
    }

    override fun onDestroy() {
        settings.release()
        super.onDestroy()
    }

    override fun getCallbackFragment() = this

    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat, pref: Preference): Boolean {
        ToggleFeatureActivity.startWith(requireActivity(), pref)
        return true
    }
}
