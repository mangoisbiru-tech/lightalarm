package com.lightalarm.nativeapp

import java.io.Serializable

data class AlarmItem(
    val id: Int,
    val time: String,
    val repeatDays: String,
    val theme: String,
    val soundCategory: String,
    var isEnabled: Boolean = false
) : Serializable
