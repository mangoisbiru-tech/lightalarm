package com.lightalarm.nativeapp

import java.io.Serializable

data class SoundItem(
    val id: Int,
    val displayName: String,
    val resourceId: Int,
    val category: String
) : Serializable





