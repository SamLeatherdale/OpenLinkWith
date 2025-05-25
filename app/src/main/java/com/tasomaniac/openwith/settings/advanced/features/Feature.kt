package com.tasomaniac.openwith.settings.advanced.features

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.tasomaniac.openwith.R
import com.tasomaniac.openwith.translations.R.string

enum class Feature(
    @StringRes val titleRes: Int,
    @StringRes val detailsRes: Int,
    @DrawableRes val imageRes: Int? = null,
    val className: String? = null,
    val prefKey: String,
    val defaultValue: Boolean = true
) {

    ADD_TO_HOMESCREEN(
        R.string.pref_title_feature_add_to_homescreen,
        string.pref_details_feature_add_to_homescreen,
        com.tasomaniac.openwith.intro.R.drawable.tutorial_4,
        "com.tasomaniac.openwith.homescreen.AddToHomeScreen",
        "pref_feature_add_to_homescreen"
    ),
    TEXT_SELECTION(
        string.pref_title_feature_text_selection,
        string.pref_details_feature_text_selection,
        R.drawable.feature_text_selection,
        "com.tasomaniac.openwith.TextSelectionActivity",
        "pref_feature_text_selection"
    ),
    DIRECT_SHARE(
        string.pref_title_feature_direct_share,
        string.pref_details_feature_direct_share,
        R.drawable.feature_direct_share,
        "androidx.sharetarget.ChooserTargetServiceCompat",
        "pref_feature_direct_share"
    ),
    BROWSER(
        string.pref_title_feature_browser,
        string.pref_details_feature_browser,
        className = "com.tasomaniac.openwith.BrowserActivity",
        prefKey = "pref_feature_browser",
        defaultValue = false
    ),
    CLEAN_URLS(
        string.pref_title_feature_clean_urls,
        string.pref_details_feature_clean_urls,
        prefKey = "pref_feature_clean_urls",
        defaultValue = false
    ),
    CALLER_APP(
        string.pref_title_feature_caller_app,
        string.pref_details_feature_caller_app,
        prefKey = "pref_feature_caller_app",
        defaultValue = false
    )
}

fun String.toFeature() = Feature.values().find { it.prefKey == this }!!
