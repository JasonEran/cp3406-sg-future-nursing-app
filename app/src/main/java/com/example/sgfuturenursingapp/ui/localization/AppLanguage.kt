package com.example.sgfuturenursingapp.ui.localization

import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

enum class AppLanguage(
    val code: String,
    val localeTag: String,
    @StringRes val displayNameRes: Int,
) {
    ENGLISH("en", "en", com.example.sgfuturenursingapp.R.string.language_english),
    CHINESE_SIMPLIFIED("zh", "zh-CN", com.example.sgfuturenursingapp.R.string.language_chinese_simplified),
    ;

    companion object {
        fun fromCode(code: String?): AppLanguage =
            values().firstOrNull { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
    }
}

object LanguageController {
    private val _languageFlow = kotlinx.coroutines.flow.MutableStateFlow(AppLanguage.ENGLISH)
    val languageFlow: kotlinx.coroutines.flow.StateFlow<AppLanguage> = _languageFlow

    fun initialize() {
        val storedLocales = AppCompatDelegate.getApplicationLocales()
        val storedTag =
            if (!storedLocales.isEmpty) {
                storedLocales[0]?.toLanguageTag()
            } else {
                null
            }
        val resolvedLanguage =
            AppLanguage
                .values()
                .firstOrNull { language ->
                    language.localeTag.equals(storedTag, ignoreCase = true) ||
                        language.code.equals(storedTag, ignoreCase = true)
                } ?: _languageFlow.value
        _languageFlow.value = resolvedLanguage
        val locales = LocaleListCompat.forLanguageTags(resolvedLanguage.localeTag)
        AppCompatDelegate.setApplicationLocales(locales)
    }

    fun updateLanguage(language: AppLanguage) {
        if (_languageFlow.value == language) return
        _languageFlow.value = language
        val locales = LocaleListCompat.forLanguageTags(language.localeTag)
        AppCompatDelegate.setApplicationLocales(locales)
    }
}
