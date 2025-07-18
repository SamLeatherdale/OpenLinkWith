package com.tasomaniac.openwith.redirect

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.tasomaniac.android.widget.DelayedProgressBar
import com.tasomaniac.openwith.resolver.ResolverActivity
import com.tasomaniac.openwith.rx.SchedulingStrategy
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.Maybe
import io.reactivex.MaybeTransformer
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import javax.inject.Inject

class RedirectFixActivity : DaggerAppCompatActivity() {

    @Inject lateinit var browserIntentChecker: BrowserIntentChecker
    @Inject lateinit var redirectFixer: RedirectFixer
    @Inject lateinit var urlFix: UrlFix
    @Inject lateinit var schedulingStrategy: SchedulingStrategy

    private var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.redirect_activity)

        val rootView = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(bottom = systemBars.bottom)
            insets
        }

        // Check for intent extra
        val unshorten = intent.getBooleanExtra(EXTRA_UNSHORT, false)

        val progress = findViewById<DelayedProgressBar>(R.id.resolver_progress)
        progress.show(true)

        val source = Intent(intent).apply {
            component = null
        }
        disposable = Single.just(source)
            .filter { browserIntentChecker.hasOnlyBrowsers(it) }
            .map { urlFix.fixUrls(it.dataString!!) }
            .flatMap {
                Maybe.fromCallable<HttpUrl> { it.toHttpUrlOrNull() }
            }
            .compose(if (unshorten) redirectTransformer
            else MaybeTransformer { source -> source })
            .map(HttpUrl::toString)
            .toSingle(source.dataString!!) // fall-back to original data if anything goes wrong
            .map(urlFix::fixUrls) // fix again after potential redirect
            .map { source.withUrl(it) }
            .compose(schedulingStrategy.forSingle())
            .subscribe { intent ->
                intent.component = ComponentName(this, ResolverActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
                intent.putExtra(EXTRA_UNSHORT, unshorten)
                startActivity(intent)
                finish()
            }
    }

    private val redirectTransformer = MaybeTransformer<HttpUrl, HttpUrl> { source ->
        source.flatMap { httpUrl ->
            redirectFixer
                .followRedirects(httpUrl)
                .toMaybe()
        }
    }

    override fun onDestroy() {
        disposable?.dispose()
        super.onDestroy()
    }

    companion object {

        @JvmStatic
        fun createIntent(activity: Activity, foundUrl: String): Intent {
            return Intent(activity, RedirectFixActivity::class.java)
                .putExtra(EXTRA_UNSHORT, false)
                .putExtras(activity.intent)
                .setAction(Intent.ACTION_VIEW)
                .setData(Uri.parse(foundUrl))
        }

        @JvmField
        val EXTRA_UNSHORT = "com.tasomaniac.openwith.resolver.UNSHORT"

        private fun Intent.withUrl(url: String): Intent = setData(Uri.parse(url))
    }
}
