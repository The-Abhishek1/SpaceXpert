package com.xcloak.spacexpert.data

data class StasisApp(
    val packageName: String,
    val appName: String,
    val cacheSizeBytes: Long,
    val daysInactive: Int,
    val isDebrisLeftover: Boolean = false
)
