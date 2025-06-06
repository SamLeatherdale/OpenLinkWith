package com.tasomaniac.openwith.redirect

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import com.tasomaniac.openwith.browser.resolver.BrowserResolver
import com.tasomaniac.openwith.extensions.componentName
import com.tasomaniac.openwith.extensions.isHttp
import javax.inject.Inject

class BrowserIntentChecker @Inject constructor(
    private val application: Application,
    private val packageManager: PackageManager,
    private val browserResolver: BrowserResolver
) {

    fun hasOnlyBrowsers(sourceIntent: Intent): Boolean {
        if (!sourceIntent.isHttp()) {
            return false
        }
        val resolved = resolveSource(sourceIntent).toComponents()
        val browsers = browserResolver.queryBrowsers().toComponents()

        return (resolved - browsers).isEmpty()
    }

    private fun resolveSource(sourceIntent: Intent): List<ResolveInfo> {
        val flag = PackageManager.MATCH_ALL
        val resolved = packageManager.queryIntentActivities(sourceIntent, flag)

        resolved.removeAll {
            it.activityInfo.packageName == application.packageName
        }
        return resolved.distinctBy {
            it.activityInfo.packageName
        }
    }

    private fun List<ResolveInfo>.toComponents() = map { it.activityInfo.componentName() }
}
