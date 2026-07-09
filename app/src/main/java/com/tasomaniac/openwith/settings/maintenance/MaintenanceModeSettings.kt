package com.tasomaniac.openwith.settings.maintenance

import androidx.preference.PreferenceCategory
import com.tasomaniac.openwith.R
import com.tasomaniac.openwith.data.Analytics
import com.tasomaniac.openwith.data.prefs.BooleanPreference
import com.tasomaniac.openwith.data.prefs.MaintenanceModeHidden
import com.tasomaniac.openwith.settings.Settings
import com.tasomaniac.openwith.settings.SettingsFragment
import javax.inject.Inject

class MaintenanceModeSettings @Inject constructor(
    fragment: SettingsFragment,
    @MaintenanceModeHidden private val maintenanceModeHidden: BooleanPreference,
    private val analytics: Analytics
) : Settings(fragment) {

    private var preferenceCategory: PreferenceCategory? = null

    override fun setup() {
        if (maintenanceModeHidden.get()) return

        addPreferencesFromResource(R.xml.pref_maintenance)
        preferenceCategory = findPreference(R.string.pref_key_category_maintenance) as PreferenceCategory

        activity.supportFragmentManager.setFragmentResultListener(
            MaintenanceModeDialogFragment.REQUEST_KEY_HIDDEN,
            activity
        ) { _, _ ->
            hide()
        }

        findPreference(R.string.pref_key_maintenance).setOnPreferenceClickListener {
            MaintenanceModeDialogFragment.newInstance()
                .show(activity.supportFragmentManager, "MaintenanceModeDialog")
            analytics.sendEvent("MaintenanceMode", "Open", "Dialog")
            true
        }
    }

    private fun hide() {
        preferenceCategory?.let {
            removePreference(it)
            preferenceCategory = null
        }
    }
}
