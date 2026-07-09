package com.tasomaniac.openwith.settings.maintenance

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.preference.PreferenceManager
import com.tasomaniac.openwith.BuildConfig
import com.tasomaniac.openwith.R
import com.tasomaniac.openwith.data.Analytics
import com.tasomaniac.openwith.translations.R.string
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class MaintenanceModeDialogFragment : AppCompatDialogFragment() {

    @Inject lateinit var analytics: Analytics

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_maintenance_mode, null)

        view.findViewById<android.view.View>(R.id.app_urlcheck).setOnClickListener {
            openUrl(urlCheckUrl())
            analytics.sendEvent("MaintenanceMode", "OpenAlternative", "URLCheck")
        }
        view.findViewById<android.view.View>(R.id.app_linksheet).setOnClickListener {
            openUrl(LINKSHEET_URL)
            analytics.sendEvent("MaintenanceMode", "OpenAlternative", "LinkSheet")
        }
        view.findViewById<android.view.View>(R.id.app_sidestep).setOnClickListener {
            openUrl(SIDESTEP_URL)
            analytics.sendEvent("MaintenanceMode", "OpenAlternative", "Sidestep")
        }

        return AlertDialog.Builder(requireContext())
            .setTitle(string.pref_title_maintenance)
            .setView(view)
            .setNeutralButton(string.dont_show_again) { _, _ ->
                PreferenceManager.getDefaultSharedPreferences(requireContext())
                    .edit()
                    .putBoolean(PREF_KEY_HIDDEN, true)
                    .apply()
                analytics.sendEvent("MaintenanceMode", "Dismiss", "DontShowAgain")
                setFragmentResult(REQUEST_KEY_HIDDEN, bundleOf())
            }
            .setPositiveButton(android.R.string.ok, null)
            .create()
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(intent)
        }
    }

    companion object {
        const val REQUEST_KEY_HIDDEN = "maintenance_mode_hidden"
        const val PREF_KEY_HIDDEN = "pref_maintenance_mode_hidden"

        private const val URLCHECK_PLAY_URL =
            "https://play.google.com/store/apps/details?id=com.trianguloy.urlchecker"
        private const val URLCHECK_FLOSS_URL = "https://github.com/TrianguloY/URLCheck"
        private const val LINKSHEET_URL = "https://github.com/LinkSheet/LinkSheet"
        private const val SIDESTEP_URL = "https://github.com/blankdotdev/sidestep"

        private fun urlCheckUrl(): String =
            if (BuildConfig.FLAVOR == "floss") URLCHECK_FLOSS_URL else URLCHECK_PLAY_URL

        fun newInstance() = MaintenanceModeDialogFragment()
    }
}
