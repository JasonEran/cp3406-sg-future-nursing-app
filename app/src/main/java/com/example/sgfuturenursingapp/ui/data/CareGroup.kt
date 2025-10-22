package com.example.sgfuturenursingapp.ui.data

data class CareGroup(
    val groupId: String,
    val groupName: String,
    val adminUid: String,
    val members: Map<String, String>,
)
