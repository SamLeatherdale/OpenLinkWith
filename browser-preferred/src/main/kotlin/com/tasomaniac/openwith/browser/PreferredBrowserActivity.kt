package com.tasomaniac.openwith.browser

import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.DividerItemDecoration
import com.tasomaniac.openwith.HeaderAdapter
import com.tasomaniac.openwith.SimpleTextViewHolder
import com.tasomaniac.openwith.browser.databinding.BrowserActivityPreferredAppsBinding
import com.tasomaniac.openwith.browser.resolver.BrowserResolver
import com.tasomaniac.openwith.data.Analytics
import com.tasomaniac.openwith.extensions.componentName
import com.tasomaniac.openwith.resolver.DisplayActivityInfo
import com.tasomaniac.openwith.translations.R.string
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

class PreferredBrowserActivity : DaggerAppCompatActivity(), BrowsersAdapter.Listener {

    @Inject lateinit var analytics: Analytics
    @Inject lateinit var viewHolderFactory: BrowserViewHolder.Factory
    @Inject lateinit var browserResolver: BrowserResolver
    @Inject lateinit var browserPreferences: BrowserPreferences

    private val context get() = this

    private val disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = BrowserActivityPreferredAppsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(bottom = systemBars.bottom)
            insets
        }

        analytics.sendScreenView("Browser Apps")
        setupToolbar()

        browserResolver.resolve()
            .subscribeBy(onSuccess = { binding.setupList(it) })
            .addTo(disposable)
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    private fun BrowserActivityPreferredAppsBinding.setupList(browsers: List<DisplayActivityInfo>) {
        recyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        val browsersAdapter = BrowsersAdapter(
            browsers,
            browserPreferences.mode,
            viewHolderFactory,
            listener = this@PreferredBrowserActivity
        )
        recyclerView.adapter = HeaderAdapter(
            browsersAdapter,
            { viewGroup -> SimpleTextViewHolder.create(viewGroup, R.layout.preferred_header) },
            { setText(string.browser_description) }
        )
    }

    override fun onDestroy() {
        disposable.dispose()
        super.onDestroy()
    }

    override fun onBrowserClick(displayResolveInfo: DisplayActivityInfo) {
        val browserMode = BrowserPreferences.Mode.Browser(
            displayResolveInfo.displayLabel.toString(),
            displayResolveInfo.activityInfo.componentName()
        )
        browserPreferences.mode = browserMode
        analytics.sendEvent("Preference", "Browser Mode", browserMode.value)
        analytics.sendEvent("Preference", "Selected Browser", browserMode.componentName.packageName)
        finish()
    }

    override fun onNoneClick() {
        browserPreferences.mode = BrowserPreferences.Mode.None
        analytics.sendEvent("Preference", "Browser Mode", BrowserPreferences.Mode.None.value)
        finish()
    }

    override fun onAlwaysAskClick() {
        browserPreferences.mode = BrowserPreferences.Mode.AlwaysAsk
        analytics.sendEvent("Preference", "Browser Mode", BrowserPreferences.Mode.AlwaysAsk.value)
        finish()
    }
}
