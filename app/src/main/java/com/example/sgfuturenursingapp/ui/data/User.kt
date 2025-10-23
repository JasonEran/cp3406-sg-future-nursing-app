package com.example.sgfuturenursingapp.ui.data

import com.example.sgfuturenursingapp.ui.localization.AppLanguage

data class User(
    val uid: String = "",
    val email: String = "",
    val role: String = "",
    val careGroupId: String? = null,
    val language: String = AppLanguage.ENGLISH.code,
)
