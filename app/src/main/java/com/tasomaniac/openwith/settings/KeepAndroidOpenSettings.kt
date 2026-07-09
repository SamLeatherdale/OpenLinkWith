package com.tasomaniac.openwith.settings

import android.content.Intent
import androidx.core.net.toUri
import com.tasomaniac.openwith.R
import com.tasomaniac.openwith.data.Analytics
import com.tasomaniac.openwith.translations.R.string as translationString
import javax.inject.Inject

class KeepAndroidOpenSettings @Inject constructor(
    fragment: SettingsFragment,
    private val analytics: Analytics
) : Settings(fragment) {

    override fun setup() {
        addPreferencesFromResource(R.xml.pref_keep_android_open)

        findPreference(R.string.pref_key_keep_android_open).setOnPreferenceClickListener {
            openKeepAndroidOpen()
            analytics.sendEvent("KeepAndroidOpen", "Click", "Banner")
            true
        }
    }

    private fun openKeepAndroidOpen() {
        val intent = Intent(
            Intent.ACTION_VIEW,
            context.getString(translationString.keep_android_open_url).toUri()
        )
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }
}
