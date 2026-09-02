package com.example.composelearning.temples.platform

import android.app.LocaleManager
import android.content.Context
import android.os.LocaleList
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration
import com.example.composelearning.R
import com.example.composelearning.temples.data.LocalizedText

/**
 * The languages this feature ships translations for.
 *
 * [tag] is the BCP-47 language tag, which is also the `values-<tag>/` resource qualifier
 * and the key [LocalizedText.resolve] switches on — one identifier, three uses.
 */
enum class AppLanguage(
    val tag: String,
    /** Endonym: a language picker should name each language *in* that language. */
    val endonym: String,
    @StringRes val labelRes: Int
) {
    ENGLISH("en", "English", R.string.language_english),
    HINDI("hi", "हिन्दी", R.string.language_hindi),
    KANNADA("kn", "ಕನ್ನಡ", R.string.language_kannada);

    companion object {
        fun fromTag(tag: String?): AppLanguage =
            entries.firstOrNull { it.tag == tag } ?: ENGLISH
    }
}

/**
 * Per-app language, backed by the platform's own store (API 33+, and this module's `minSdk`).
 *
 * Using [LocaleManager] rather than a private preference means the choice shows up in
 * *Settings ▸ Apps ▸ ComposeLearning ▸ Language*, survives reinstall-from-backup, and is
 * applied by the framework before any of our code runs. The trade-off is that setting it
 * recreates the activity — which is exactly what we want, since it re-reads `values-kn/`.
 */
class LanguagePreference(private val context: Context) {

    private val localeManager: LocaleManager?
        get() = context.getSystemService(LocaleManager::class.java)

    /** The language actually in force, falling back to English when the user has not chosen. */
    fun current(): AppLanguage {
        val locales = localeManager?.applicationLocales
        val tag = locales?.takeIf { !it.isEmpty }?.get(0)?.language
        return AppLanguage.fromTag(tag)
    }

    /** Applies [language]; the framework restarts the activity to pick up new resources. */
    fun set(language: AppLanguage) {
        localeManager?.applicationLocales = LocaleList.forLanguageTags(language.tag)
    }

    /** Hands control back to the system language list. */
    fun clear() {
        localeManager?.applicationLocales = LocaleList.getEmptyLocaleList()
    }
}

/**
 * The language tag Compose is currently resolving resources with.
 *
 * Read from [LocalConfiguration] rather than from [LanguagePreference] so that composition
 * and the `values-*` folders can never disagree: if the framework handed us a Kannada
 * configuration, [LocalizedText] resolves Kannada too.
 */
@Composable
@ReadOnlyComposable
fun currentLanguageTag(): String = LocalConfiguration.current.locales[0].language

/** Resolves this text against the active app locale. The workhorse of every temple screen. */
@Composable
@ReadOnlyComposable
fun LocalizedText.local(): String = resolve(currentLanguageTag())
