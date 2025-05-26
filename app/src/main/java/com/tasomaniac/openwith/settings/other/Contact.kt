package com.tasomaniac.openwith.settings.other

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.tasomaniac.openwith.base.R

object Contact {
    fun startContactEmailIntent(context: Context) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:play@samleatherdale.com".toUri()
            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name))
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(
                Intent.createChooser(
                    intent, context.getString(com.tasomaniac.openwith.translations.R.string.pref_title_contact)
                )
            )
        }
    }
}