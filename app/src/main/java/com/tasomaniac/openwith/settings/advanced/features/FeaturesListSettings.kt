package com.tasomaniac.openwith.settings.advanced.features

import androidx.annotation.StringRes
import androidx.preference.Preference
import com.tasomaniac.openwith.R
import com.tasomaniac.openwith.settings.Settings
import com.tasomaniac.openwith.translations.R.string
import javax.inject.Inject

class FeaturesListSettings @Inject constructor(
    private val featurePreferences: FeaturePreferences,
    fragment: FeaturesListFragment
) : Settings(fragment) {

    override fun setup() {
        addPreferencesFromResource(R.xml.pref_features)
    }

    override fun resume() {
        Feature.values().forEach { feature ->
            val enabled = featurePreferences.isEnabled(feature)
            fragment.findPreference<Preference>(feature.prefKey)?.setSummary(enabled.toSummary())
        }
    }

    @StringRes
    private fun Boolean.toSummary() =
        if (this) string.pref_state_feature_enabled else string.pref_state_feature_disabled
}
