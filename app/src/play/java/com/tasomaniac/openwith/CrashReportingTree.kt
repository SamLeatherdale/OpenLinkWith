package com.tasomaniac.openwith

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import timber.log.Timber

internal class CrashReportingTree : Timber.Tree() {
    private val crashlytics = Firebase.crashlytics

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return
        }
        crashlytics.log("$tag: $message")
        if (t != null && priority >= Log.WARN) {
            crashlytics.recordException(t)
        }
    }
}
