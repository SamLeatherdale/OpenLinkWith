package com.tasomaniac.openwith.settings

import android.app.backup.BackupManager
import android.content.SharedPreferences
import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.commit
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.tasomaniac.openwith.R
import com.tasomaniac.openwith.data.Analytics
import com.tasomaniac.openwith.data.prefs.BooleanPreference
import com.tasomaniac.openwith.data.prefs.TutorialShown
import com.tasomaniac.openwith.databinding.ActivitySettingsBinding
import com.tasomaniac.openwith.intro.IntroActivity
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

class SettingsActivity :
    DaggerAppCompatActivity(),
    SharedPreferences.OnSharedPreferenceChangeListener,
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    @Inject @TutorialShown lateinit var tutorialShown: BooleanPreference
    @Inject lateinit var analytics: Analytics
    @Inject lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivitySettingsBinding

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!tutorialShown.get()) {
            startActivity(IntroActivity.newIntent(this))
            tutorialShown.set(true)
        }

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(bottom = systemBars.bottom)
            insets
        }

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                add(R.id.fragment_container, SettingsFragment())
            }

            analytics.sendScreenView("Settings")
        }
    }

    override fun setTitle(titleId: Int) {
        super.setTitle(titleId)
        binding.collapsingToolbar.title = getString(titleId)
    }

    override fun onResume() {
        super.onResume()
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat, pref: Preference): Boolean {
        supportFragmentManager.commit {
            val fragment = supportFragmentManager.fragmentFactory.instantiate(classLoader, pref.fragment)
            replace(R.id.fragment_container, fragment)
            addToBackStack(null)
        }
        return true
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        BackupManager(this).dataChanged()
    }
}
