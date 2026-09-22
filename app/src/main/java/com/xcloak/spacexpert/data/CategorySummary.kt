package com.xcloak.spacexpert.data

enum class FileCategory { PHOTOS, VIDEOS, DOCUMENTS, DOWNLOADS, AUDIO, OTHER }

data class CategorySummary(
    val category: FileCategory,
    val sizeBytes: Long,
    val count: Int
)