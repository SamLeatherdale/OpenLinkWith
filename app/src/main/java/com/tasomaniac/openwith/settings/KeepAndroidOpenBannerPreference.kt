package com.tasomaniac.openwith.settings

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.AttributeSet
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.tasomaniac.openwith.R
import com.tasomaniac.openwith.translations.R.string as translationString

class KeepAndroidOpenBannerPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : Preference(context, attrs, defStyleAttr) {

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val titleView = holder.findViewById(android.R.id.title) as? TextView ?: return
        val url = context.getString(translationString.keep_android_open_url)
        val bannerText = context.getString(translationString.keep_android_open_banner)
        val spannable = SpannableString(bannerText)
        val urlStart = bannerText.indexOf(url)
        if (urlStart >= 0) {
            val urlEnd = urlStart + url.length
            spannable.setSpan(UnderlineSpan(), urlStart, urlEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(context, R.color.keep_android_open_banner_link)
                ),
                urlStart,
                urlEnd,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        titleView.text = spannable
    }
}
