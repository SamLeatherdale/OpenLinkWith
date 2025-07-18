package com.tasomaniac.openwith.resolver

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import com.tasomaniac.openwith.BuildConfig
import com.tasomaniac.openwith.extensions.isHttp
import com.tasomaniac.openwith.rx.SchedulingStrategy
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import javax.inject.Inject

internal class IntentResolver @Inject constructor(
    private val packageManager: PackageManager,
    private val schedulingStrategy: SchedulingStrategy,
    private val callerPackage: CallerPackage,
    private val resolveListGrouper: ResolveListGrouper,
    private val browserHandlerFactory: BrowserHandler.Factory,
    val sourceIntent: Intent
) {

    private var result: IntentResolverResult? = null
    private var listener = Listener.NO_OP
    private var disposable: Disposable = Disposables.empty()

    var lastChosenComponent: ComponentName? = null

    fun bind(listener: Listener) {
        this.listener = listener

        if (result == null) {
            resolve()
        } else {
            listener.onIntentResolved(result!!)
        }
    }

    fun unbind() {
        this.listener = Listener.NO_OP
    }

    fun resolve() {
        disposable = Observable
            .fromCallable { doResolve() }
            .compose(schedulingStrategy.forObservable())
            .subscribe { data ->
                result = data
                listener.onIntentResolved(data)
            }
    }

    fun release() {
        disposable.dispose()
    }

    private fun doResolve(): IntentResolverResult {
        val flag = PackageManager.MATCH_ALL
        val currentResolveList = ArrayList(packageManager.queryIntentActivities(sourceIntent, flag))
        currentResolveList.removeAll {
            it.activityInfo.packageName == BuildConfig.APPLICATION_ID
        }
        if (sourceIntent.isHttp()) {
            browserHandlerFactory.create(currentResolveList).handleBrowsers()
        }

        callerPackage.removeFrom(currentResolveList)

        val resolved = groupResolveList(currentResolveList)
        return IntentResolverResult(
            resolved,
            resolveListGrouper.filteredItem,
            resolveListGrouper.showExtended
        )
    }

    private fun groupResolveList(currentResolveList: List<ResolveInfo>): List<DisplayActivityInfo> {
        return if (currentResolveList.isEmpty()) {
            emptyList()
        } else {
            resolveListGrouper.groupResolveList(currentResolveList, lastChosenComponent)
        }
    }

    interface Listener {

        fun onIntentResolved(result: IntentResolverResult)

        companion object {

            val NO_OP = object : Listener {
                override fun onIntentResolved(result: IntentResolverResult) = Unit
            }
        }
    }
}
