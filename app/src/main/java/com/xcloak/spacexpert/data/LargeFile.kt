package com.xcloak.spacexpert.data

data class LargeFile(
    val name: String,
    val path: String,
    val sizeBytes: Long,
    val addedTime: Long = 0L
)