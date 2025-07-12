package com.tasomaniac.openwith.settings.advanced.features

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.core.text.parseAsHtml
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.preference.Preference
import com.tasomaniac.openwith.data.Analytics
import com.tasomaniac.openwith.databinding.ToggleFeatureActivityBinding
import com.tasomaniac.openwith.settings.advanced.features.custom.view.FeatureToggleCustomView
import com.tasomaniac.openwith.translations.R.string
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

class ToggleFeatureActivity : DaggerAppCompatActivity() {

    @Inject lateinit var featurePreferences: FeaturePreferences
    @Inject lateinit var featureToggler: FeatureToggler
    @Inject lateinit var analytics: Analytics
    @Inject lateinit var sideEffects: Set<@JvmSuppressWildcards FeatureToggleSideEffect>
    @Inject lateinit var customViews: Map<Feature, @JvmSuppressWildcards FeatureToggleCustomView>
    private lateinit var binding: ToggleFeatureActivityBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ToggleFeatureActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(bottom = systemBars.bottom)
            insets
        }

        val feature = intent.featureKey.toFeature()
        setupInitialState(feature)
        setupToggle(feature)
        setupTitle(feature)
        setupDetails(feature)

        if (savedInstanceState == null) {
            analytics.sendEvent("FeatureToggle", "Feature", feature.prefKey)
        }
    }

    private fun setupInitialState(feature: Feature) {
        val enabled = featurePreferences.isEnabled(feature)
        binding.featureToggle.isChecked = enabled
        binding.featureToggle.setText(enabled.toSummary())
    }

    private fun setupToggle(feature: Feature) {
        binding.featureToggle.setOnCheckedChangeListener { _, enabled ->
            featurePreferences.setEnabled(feature, enabled)
            binding.featureToggle.setText(enabled.toSummary())
            featureToggler.toggleFeature(feature, enabled)

            sideEffects.forEach { it.featureToggled(feature, enabled) }
        }
    }

    private fun setupTitle(feature: Feature) {
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        setTitle(feature.titleRes)
    }

    private fun setupDetails(feature: Feature) {
        binding.featureDetails.text = getString(feature.detailsRes).parseAsHtml()
        if (feature.imageRes != null) {
            binding.featureImage.setImageResource(feature.imageRes)
        } else {
            binding.featureImage.visibility = View.GONE
        }
        customViews[feature]?.bindCustomContent(binding.featureCustomContent)
    }

    @StringRes
    private fun Boolean.toSummary() =
        if (this) string.pref_state_feature_enabled else string.pref_state_feature_disabled

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    companion object {

        private const val FEATURE = "FEATURE"

        private var Intent.featureKey: String
            get() = requireNotNull(getStringExtra(FEATURE))
            set(value) {
                putExtra(FEATURE, value)
            }

        fun startWith(activity: Activity, preference: Preference) {
            val intent = Intent(activity, ToggleFeatureActivity::class.java).apply {
                this.featureKey = preference.key
            }
            activity.startActivity(intent)
        }
    }
}
