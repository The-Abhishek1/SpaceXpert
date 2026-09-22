package com.xcloak.spacexpert.data

data class BrowsableFile(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val sizeBytes: Long,
    val lastModified: Long
)